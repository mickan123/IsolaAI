import java.io.*;
import java.util.*;
import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.concurrent.*;

public class Isola
{
	final static long initBoardState = 0x0000000000007FFFL;
	final static long playerOneStartPos = 0x1000000000000000L;
	final static long playerTwoStartPos = 0x0000000000040000L;
	final static int aiTurnTime = 3000; 
	final static int maxDepth = 20; //Max depth willing to search

	//Represent board and player positions as 64 bit values 
	//First 7 x 7 bits are zero to represent board spots
	private long board = initBoardState;
	private long playerOnePos = playerOneStartPos;
	private long playerTwoPos = playerTwoStartPos;

	//Gametype and player turn booleans
	private boolean playerOneTurn; 
	private boolean playVSai;
	private boolean aiVSai;
	private boolean playVSplay;

	//Keep track of whether moving piece or removing
	private boolean waitingForMove;

	//AI logic class && gui class
	private IsolaAI ai;	
	private IsolaGUI gui;


	Isola()
	{	
		playerOneTurn = true;
		aiVSai = false;
		playVSai = false;
		playVSplay = false;
		waitingForMove = true;
		ai = new IsolaAI();
		gui = new IsolaGUI(this);
		gui.setupGUI();
		startGame();
	}

	public void startGame()
	{
		int choice = gui.gameTypeChoice();

		if (choice == 0) playVSplay = true;
		else if (choice == 1) playVSai = true;
		else if (choice == 2) aiVSai = true;
		else System.exit(0);

		if (aiVSai)
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					long start = System.nanoTime();
					aiTurn();
					long end = System.nanoTime();
					long difference = (end-start)/1000000;
					System.out.println("Time for move: " + difference + "ms");
				}
			}).start();
		}
	}

	public static void main(String[] args)
	{
		Isola game = new Isola();
		
	}

	//Iterative deepening search thread that runs for set time
	class aiCalculateMove implements Callable<GameState>
	{
		public GameState call()
		{
			int initialDepth = 3;
			GameState returnState = new GameState(board, playerOneTurn, playerOnePos, playerTwoPos, 0, initialDepth);
			GameState myState = new GameState(returnState);
			while (true)
			{
				GameState tempState = new GameState(ai.calculateBestMove(myState, -10000, 10000));
				if (ai.searchMoves && myState.maxDepth < maxDepth)
				{
					returnState = new GameState(tempState);
				} 
				else break;
				System.out.println("Depth: " + myState.maxDepth);
				myState.maxDepth++;
			}
			return returnState;
		}
	}

	//Executes the ai turn, calculations are done for amount of time defined by aiTurnTime class variable
	void aiTurn()
	{	
		GameState nextState = new GameState(board, playerOneTurn, playerOnePos, playerTwoPos, 0, 5);
		final ExecutorService service;
        final Future<GameState> getMove;
        service = Executors.newFixedThreadPool(1);
        getMove = service.submit(new aiCalculateMove());    
		
		//Wait for a set time before fetching best result
		try
		{
			Thread.sleep(aiTurnTime);
			ai.invertSearchBoolean();
			nextState = getMove.get();
			ai.invertSearchBoolean();
		}
		catch(Exception e)
		{
			System.out.println("Error Sleeping");
		}

		//Get corresponding move and remove buttons and update
		long movePos = playerOneTurn ? nextState.playerOnePos : nextState.playerTwoPos;
		long removePos = nextState.board - board;
		int removeID = 48 - Long.numberOfLeadingZeros(removePos); 
		int moveID = 48 - Long.numberOfLeadingZeros(movePos); 

		int xMove = moveID/7;
		int yMove = moveID%7;
		int xRemove = removeID/7;
		int yRemove = removeID%7;

		//Make AI move
		gui.makeMove(playerOneTurn, xMove, yMove, xRemove, yRemove);

		if (playerOneTurn) playerOnePos = movePos;
		else playerTwoPos = movePos;

		board = nextState.board;
		playerOneTurn = !playerOneTurn;

		if (GameLogic.gameOver(board, playerOnePos, playerTwoPos))
		{
			gameOver();
		}
		else if (aiVSai) aiTurn();
	}

	void buttonPressed(JButton buttonClicked, long x, long y)
	{
		//Check if button is appropriate move
		if (waitingForMove && ((playerOneTurn && playVSai) || playVSplay))
		{
			long movePos = GameLogic.convertCoords(x, y);
			if (GameLogic.validMove(board, playerOneTurn, movePos, playerOnePos, playerTwoPos))
			{
				//Update GUI
				int xMove = (int)x - 1;
				int yMove = (int)y - 1;
				gui.makeMove(playerOneTurn, xMove, yMove, -1, -1);

				//Update move
				if (playerOneTurn) playerOnePos = movePos;
				else playerTwoPos = movePos;
				waitingForMove = false;
			} 
		}

		//Check if appropriate remove button
		else if (!waitingForMove && ((playerOneTurn && playVSai) || playVSplay))
		{
			long removePos = GameLogic.convertCoords(x, y);
			if (GameLogic.validRemove(board, removePos, playerOnePos, playerTwoPos))
			{
				buttonClicked.setBackground(Color.BLACK);
				board += removePos;
				playerOneTurn = !playerOneTurn;
				waitingForMove = true;
				if (GameLogic.gameOver(board, playerOnePos, playerTwoPos))
				{
					gameOver();
				} 
			}
		}

		//Run AI turn if playerVSai
		if (playVSai && !playerOneTurn)
		{
			
			new Thread(new Runnable()
			{
				public void run()
				{
					long start = System.nanoTime();
					aiTurn();
					long end = System.nanoTime();
					long difference = (end-start)/1000000;
					System.out.println("Time for move: " + difference + "ms");
				}
			}).start();
		}
	}


	void gameOver()
	{
		int winner = GameLogic.gameWinner(board, playerOnePos, playerTwoPos);
		int response = -1;

		//Get response
		if (winner == 0) 
		{
			response = JOptionPane.showConfirmDialog(null, "Replay?", "Draw!", 1);
		}
		else if (winner == 1)
		{
			response = JOptionPane.showConfirmDialog(null, "Replay?", "Player One Wins!", 1);
		} 
		else if (winner == 2) 
		{
			response = JOptionPane.showConfirmDialog(null, "Replay?", "Player Two Wins!", 1);
		}
		else 
		{
			System.out.println("Error, something went wrong");
			System.exit(0);
		}
		
		//Handle response
		if (response == JOptionPane.YES_OPTION)
		{
			resetGame();
		}
		else
		{
			System.exit(0);
		}
	}
	
	void resetGame()
	{
		//Set everything to default
		gui.resetGUI();
		playerOneTurn = true;
		waitingForMove = true;
		board = initBoardState;
		playerOnePos = playerOneStartPos;
		playerTwoPos = playerTwoStartPos;
		startGame();
	}
}