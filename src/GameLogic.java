
public class GameLogic
{
	//Used to stop movement from left side of board to right and vice versa
	final static long LEFTWALL = 0x020408102040FFFFL;
	final static long RIGHTWALL = 0x8102040810207FFFL;


	//left and right shifts of these values 
	//represent all 8 possible directions of movement
	final static int dydx[] = {1,6,7,8}; 

	//Checks if the game is finished
	static boolean gameOver(long board, long playerPos, long aiPos)
	{
		
		boolean noMovesPlayer = true;
		boolean noMovesAI = true;
		for (int i=0; i<4; i++)
		{
			if (validMove(board, true, playerPos >>> dydx[i], playerPos, aiPos)
				|| validMove(board, true, playerPos << dydx[i], playerPos, aiPos))
			{
				noMovesPlayer = false;
			}
			if (validMove(board, false, aiPos >>> dydx[i], playerPos, aiPos)
				|| validMove(board, false, aiPos << dydx[i], playerPos, aiPos))
			{
				noMovesAI = false;
			}
		}
		return noMovesPlayer || noMovesAI;
	}

	//Returns 1 if player one wins, 2 if player two wins, 0 if draw
	static int gameWinner(long board, long playerPos, long aiPos)
	{
		boolean noMovesPlayer = true;
		boolean noMovesAI = true;
		for (int i=0; i<4; i++)
		{
			if (validMove(board, true, playerPos >>> dydx[i], playerPos, aiPos)
				|| validMove(board, true, playerPos << dydx[i], playerPos, aiPos))
			{
				noMovesPlayer = false;
			}
			if (validMove(board, false, aiPos >>> dydx[i], playerPos, aiPos)
				|| validMove(board, false, aiPos << dydx[i], playerPos, aiPos))
			{
				noMovesAI = false;
			}
		}
		if (!noMovesPlayer && !noMovesAI) return -1;
		else if (noMovesPlayer && noMovesAI) return 0;
		else if (noMovesAI) return 1;
		else if (noMovesPlayer) return 2;
		else return -1;
	}

	//Returns if a move is valid
	static boolean validMove(long board, boolean playerTurn, long move, long playerPos, long aiPos)
	{

		if ((board & move) != 0 //Check if valid spot to move to
			|| (move & playerPos) != 0 //Check if move doesn't overlap with playerPos
			|| (move & aiPos) != 0 //Check if move doesn't overlap with aiPos
			|| 	Long.bitCount(move) != 1)  //Check only 1 bit set
		{
			return false;
		}

		long curPlayerPos = (playerTurn) ? playerPos : aiPos;
		
		int curPlayerBitVal = 64 - Long.numberOfLeadingZeros(curPlayerPos); 
		int moveBitVal = 64 - Long.numberOfLeadingZeros(move); 
		int difference = Math.abs(curPlayerBitVal - moveBitVal);
		boolean oneSpace = difference == 1 || (difference <= 8 && difference >= 6);

		//Check if we aren't moving from left side of board to right side
		//Need to check as these bits are adjacent with our representation

		if ((LEFTWALL & move) != 0 && (RIGHTWALL & curPlayerPos) != 0) return false;
		else if ((LEFTWALL & curPlayerPos) != 0 && (RIGHTWALL & move) != 0) return false;
	
		return oneSpace;
	}

	//Returns if removing a certain piece is valid
	static boolean validRemove(long board, long remove, long playerPos, long aiPos)
	{	
		if 	((remove & board) != 0 //Check if already removed and in 7x7 grid
			|| 	(remove & playerPos) != 0 //Check if player is on piece
			|| 	(remove & aiPos) != 0 //Check if  ai is on piece
			|| 	Long.bitCount(remove) != 1) //Check that we are only removing 1 piece
		{
			return false;
		}
		return true;	
	}

	//Converts x and y coordinates to a 64 bit 
	//value with 1 bit set in appropriate spot
	static long convertCoords(long x, long y)
	{
		x = x-1;
		y = y-1;
		long sum = y + 7*x + 15;
		long returnVal = 0x1L << sum;
		return returnVal;
	}

	//Prints board to stdout, useful for debugging
	static void printBoard(long gameBoard, long playerPos, long aiPos)
	{
		//Holds chars to print according to bit values
		char array[] = new char[49];

		long boardIter = gameBoard >>> 15;
		long playerIter = playerPos >>> 15;
		long aiIter = aiPos >>> 15;

		//Fill up array with values according to
		//board and player bit values
		for (int i=0; i<49; i++)
		{
			array[i] = ((boardIter & 1) == 1) ? 'X' : '0';
			array[i] = ((playerIter & 1) == 1) ? 'P' : array[i];
			array[i] = ((aiIter & 1) == 1) ? 'A' : array[i];
			boardIter = boardIter >>> 1;
			playerIter = playerIter >>> 1;
			aiIter = aiIter >>> 1;
		}

		//Print out array
		System.out.print("   ");
		for (int i=0; i<7; i++)
		{
			System.out.print(i+1 + " ");
		}
		System.out.println();
		System.out.println();
		for (int i=0; i<7; i++)
		{
			System.out.print(i+1 + "  ");
			for (int j=0; j<7; j++)
			{
				System.out.print(array[i*7 + j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

}