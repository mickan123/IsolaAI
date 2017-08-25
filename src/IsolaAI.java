import java.io.*;
import java.util.*;
import java.lang.*;

public class IsolaAI
{
	final static long LEFTWALL = 0x020408102040FFFFL;	
	final static long RIGHTWALL = 0x8102040810207FFFL;

	final static int BOARDWIDTH = 7;
	final static int SEARCHDISTANCE = 2; //Checks for spaces within searchdistance of opponent

	//Left shifts of all values represent all 8 possible directions of movement
	final static int moveShifts[] = {1,6,7,8,1,6,7,8}; 

	//Holds all 49 possible square locations
	long removeLocations[];
	volatile boolean searchMoves;

	IsolaAI()
	{
		searchMoves = true;
		//Fill in all possible 49 coordinates
		removeLocations = new long[49];
		for (int i=1; i<8; i++)
		{
			for (int j=1; j<8; j++)
			{
				removeLocations[(i-1)*7 + (j-1)] = GameLogic.convertCoords(i,j);
			}
		}
	}

	void invertSearchBoolean()
	{
		searchMoves = !searchMoves;
	}

	//Checks whether a remove location is within 2 squares of opponent
	boolean closeToPlayer(long playerOnePos, long remove)
	{
		//Get most significant bits
		int playerBitVal = 64 - Long.numberOfLeadingZeros(playerOnePos); 
		int removeBitVal = 64 - Long.numberOfLeadingZeros(remove);
		
		int difference = Math.abs(playerBitVal-removeBitVal);
		boolean close = (difference <= BOARDWIDTH*2 + SEARCHDISTANCE && difference >= BOARDWIDTH*2 - SEARCHDISTANCE)
						|| (difference <=BOARDWIDTH+SEARCHDISTANCE && difference >= BOARDWIDTH-SEARCHDISTANCE) 
						|| (difference <= SEARCHDISTANCE); 

		//Checks if we are on opposite sides of boards
		if (Math.abs(((playerBitVal - SEARCHDISTANCE) % BOARDWIDTH) - ((removeBitVal - SEARCHDISTANCE) % BOARDWIDTH)) >= BOARDWIDTH - SEARCHDISTANCE) return false; 
		return close;
	}
	
	//Min max algorithm using heuristic function with alpha beta pruning
	GameState calculateBestMove(GameState curState, int alpha, int beta)
	{

		//Check that we haven't been called to exit early
		if (searchMoves == false)
		{
			curState.curDepth = curState.maxDepth;
			return curState;
		}	

		long curPlayerPos = (curState.playerTurn) ? curState.playerOnePos : curState.playerTwoPos;
		long oppPlayerPos = (curState.playerTurn) ? curState.playerTwoPos : curState.playerOnePos;

		GameState bestState = new GameState(curState);	
		bestState.curDepth++;	

		//Create list of moves ordered by heuristic
		Map<Integer, List<GameState>> moveList;
		if (curState.playerTurn) moveList = new TreeMap<Integer, List<GameState>>();
		else moveList = new TreeMap<Integer, List<GameState>>(Collections.reverseOrder());

		//Iterate through 49 possible removes and 8 possible moves
		for (int j=0; j<49; j++)
		{
			long removePos = removeLocations[j];
			if  ((GameLogic.validRemove(curState.board, removePos, curState.playerOnePos, curState.playerTwoPos)
				&& closeToPlayer(oppPlayerPos, removePos))
				|| (closeToPlayer(oppPlayerPos, removePos) && removePos == curPlayerPos)) //Case where we move and remove previous spot
			{
				for (int i=0; i<8; i++)
				{
					long move = (i > 3) ? curPlayerPos >>> moveShifts[i] : curPlayerPos << moveShifts[i];
					if (GameLogic.validMove(curState.board, curState.playerTurn, move, curState.playerOnePos, curState.playerTwoPos)
						&& (move & removePos) == 0)
					{	
						GameState copyState = new GameState(curState);
						copyState = makeMove(copyState, move, removePos);
						int winVal = GameLogic.gameWinner(copyState.board, copyState.playerOnePos, copyState.playerTwoPos);
						int heuristic = heuristic(copyState, winVal);

						List<GameState> list = moveList.get(heuristic);
						if (list != null)
						{
							list.add(copyState);
						}
						else
						{
							list = new ArrayList<GameState>();
							list.add(copyState);
							moveList.put(heuristic, list);
						}
					}
				}
			}
		}

		//Iterate over list of moves and test them
		for (Map.Entry<Integer, List<GameState>> entry : moveList.entrySet())
		{
			for (GameState state : entry.getValue())
			{
				if (beta > alpha)
				{
					GameState nextState = new GameState(state);
					int winVal = -1;

					while (state.curDepth < state.maxDepth)
					{
						state = new GameState(calculateBestMove(state, alpha, beta));
						winVal = GameLogic.gameWinner(state.board, state.playerOnePos, state.playerTwoPos);
						if (winVal != -1) break; //Check if game is over
					}

					int newHeuristic = heuristic(state, winVal);
					if (!curState.playerTurn && newHeuristic > alpha)
					{
						bestState = new GameState(nextState);
						alpha = newHeuristic;
					}
					else if (curState.playerTurn && newHeuristic < beta)
					{
						bestState = new GameState(nextState);
						beta = newHeuristic;
					}
				}
			}
		}
		return bestState;
	}

	GameState makeMove(GameState curState, long move, long remove)
	{
		curState.board += remove;
		if (curState.playerTurn) curState.playerOnePos = move;
		else curState.playerTwoPos = move;
		curState.playerTurn = !curState.playerTurn;
		curState.curDepth++;
		return curState;
	}

	//All shift values to move within 2 squares of a position
	final static int moveShifts2[] = {1,6,7,8,2,5,9,12,13,14,15,16}; 

	int heuristic(GameState state, int winVal)
	{
		int heuristic = 0;

		//Weight all possible moves for AI and Player
		for (int i=0; i<4; i++)
		{
			//Player Moves
			if (GameLogic.validMove(state.board, true, state.playerOnePos >>> moveShifts2[i], state.playerOnePos, state.playerTwoPos)) heuristic-=3;
			if (GameLogic.validMove(state.board, true, state.playerOnePos << moveShifts2[i], state.playerOnePos, state.playerTwoPos)) heuristic-=3;

			//Ai moves
			if (GameLogic.validMove(state.board, false, state.playerTwoPos >>> moveShifts2[i], state.playerOnePos, state.playerTwoPos)) heuristic+=3;		
			if (GameLogic.validMove(state.board, false, state.playerTwoPos << moveShifts2[i], state.playerOnePos, state.playerTwoPos)) heuristic+=3;
		}

		//Weight possible empty squares within 2 spaces of player and AI
		for (int i=4; i<12; i++)
		{
			//Player 
			if (GameLogic.validRemove(state.board, state.playerOnePos >>> moveShifts2[i], state.playerOnePos, state.playerTwoPos)
				&& closeToPlayer(state.playerOnePos, state.playerOnePos >>> moveShifts2[i])) heuristic--;
			if (GameLogic.validRemove(state.board, state.playerOnePos << moveShifts2[i], state.playerOnePos, state.playerTwoPos)
				&& closeToPlayer(state.playerOnePos, state.playerOnePos >>> moveShifts2[i])) heuristic--;

			//Ai
			if (GameLogic.validRemove(state.board, state.playerTwoPos >>> moveShifts2[i], state.playerOnePos, state.playerTwoPos)
				&& closeToPlayer(state.playerTwoPos, state.playerTwoPos >>> moveShifts2[i])) heuristic++;		
			if (GameLogic.validRemove(state.board, state.playerTwoPos << moveShifts2[i], state.playerOnePos, state.playerTwoPos)
				&& closeToPlayer(state.playerTwoPos, state.playerTwoPos >>> moveShifts2[i])) heuristic++;
		}

		//Weight a winning board very heavily
		if (winVal != -1) 
		{
			int multiplier = (winVal*2)-3; //-1 if player wins, 1 if ai wins
			heuristic = 1000*multiplier; //Adds 1000 if AI wins -1000 if player wins
			heuristic -= 10*state.curDepth*multiplier; //Favors finishing the game quickly
		}
		return heuristic;
	}
}