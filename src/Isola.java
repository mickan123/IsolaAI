import java.io.*;
import java.util.*;
import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.concurrent.*;

public class Isola extends JPanel
{
	final static long initBoardState = 0x0000000000007FFFL;
	final static long playerOneStartPos = 0x1000000000000000L;
	final static long playerTwoStartPos = 0x0000000000040000L;
	final static int aiTurnTime = 3000;

	//Represent board as 64 bit value
	//First 7 x 7 bits are zero to represent board spots
	long board = initBoardState;

	//True if it's the players turn
	boolean playerTurn; 
	boolean playVSai;

	//Keep track of Player and AI pos
	long playerPos = playerOneStartPos;
	long aiPos = playerTwoStartPos;

	//GUI variables
	private JButton boardButtons[][] = new JButton[7][7]; 
	private JButton playerButton;
	private JButton aiButton;
	private boolean waitingForMove = true;

	volatile SharedVariable searchMoves;

	Isola(boolean turn, boolean playVSai)
	{	
		searchMoves = new SharedVariable(true);
		this.playerTurn = turn;
		setLayout(new GridLayout(7,7));
		setupGUI();
	}

	public static void main(String[] args)
	{
		JFrame mainWindow = new JFrame("Isola");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setSize(800,800);
		mainWindow.setVisible(true);
		mainWindow.getContentPane().add(new Isola(true, true));
	}

	void setupGUI()
	{
		for (int i=0; i<7; i++)
		{
			for (int j=0; j<7; j++)
			{
				boardButtons[i][j] = new JButton();
	            boardButtons[i][j].setText("");
	            boardButtons[i][j].addActionListener(new boardListener());
	            add(boardButtons[i][j]);
			}
		}
		boardButtons[0][3].setText("A");
		boardButtons[6][3].setText("P");
		playerButton = boardButtons[6][3];
		aiButton = boardButtons[0][3];
	}

	//Returns the buttonID corresponding to the button object
	int getButtonID(JButton buttonClicked)
	{
		for (int i=0; i<7; i++)
		{
			for (int j=0; j<7; j++)
			{
				if (buttonClicked == boardButtons[i][j])
				{
					return i*7 + j;
				}
			}
		}
		return -1;
	}

	private class boardListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			JButton buttonClicked = (JButton)e.getSource();
			
			int buttonID = getButtonID(buttonClicked);
			long x = buttonID/7 + 1;
			long y = buttonID%7 + 1;
			if (playerTurn && waitingForMove)
			{
				long movePos = GameLogic.convertCoords(x, y);
				if (GameLogic.validMove(board, playerTurn, movePos, playerPos, aiPos))
				{
					buttonClicked.setText("P");
					playerButton.setText("");
					playerButton = buttonClicked;
					playerPos = movePos;
					waitingForMove = false;
				} 
			}
			else if (playerTurn && !waitingForMove)
			{
				long removePos = GameLogic.convertCoords(x, y);
				if (GameLogic.validRemove(board, removePos, playerPos, aiPos))
				{
					buttonClicked.setBackground(Color.BLACK);
					board += removePos;
					playerTurn = false;
					waitingForMove = true;
					if (GameLogic.gameOver(board, playerPos, aiPos))
					{
						gameOver();
					} 
					else
					{
						//Run AI turn
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
			}
		}
	}

	//Executes the ai turn, calculations are done for amount of time defined by aiTurnTime class variable
	void aiTurn()
	{	
		GameState curState = new GameState(board, playerTurn, playerPos, aiPos, 0, 5);
		
		//GameState nextState = new GameState(new IsolaAI(searchMoves).calculateBestMove(curState, -1000, 1000));
		GameState nextState = new GameState(curState);
		
		final int maxDepth = 20;

		//Iterative deepening search that runs for set time
		class aiCalculateMove implements Callable<GameState>
		{
			volatile SharedVariable moveCache;

			aiCalculateMove(SharedVariable sv)
			{
				this.moveCache = sv;
			}
			public GameState call()
			{
				int initialDepth = 3;
				GameState returnState = new GameState(board, playerTurn, playerPos, aiPos, 0, initialDepth);
				GameState myState = new GameState(returnState);
				while (true)
				{
					GameState tempState = new GameState(new IsolaAI(moveCache).calculateBestMove(myState, -10000, 10000));
					if (searchMoves.continueSearch && myState.maxDepth < maxDepth) returnState = new GameState(tempState);
					else break;
					System.out.println("Depth: " + myState.maxDepth);
					myState.maxDepth++;
				}
				return returnState;
			}
		}

		final ExecutorService service;
        final Future<GameState> getMove;
        service = Executors.newFixedThreadPool(1);
        getMove = service.submit(new aiCalculateMove(searchMoves));    
		
		//Wait for a set time before fetching best result
		try
		{
			Thread.sleep(aiTurnTime);
			searchMoves.invertBoolean();
			nextState = getMove.get();
			searchMoves.invertBoolean();
		}
		catch(Exception e)
		{
			System.out.println("Error Sleeping");
		}

		long movePos = nextState.aiPos;
		long removePos = nextState.board - board;
		int removeID = 48 - Long.numberOfLeadingZeros(removePos); 
		int moveID = 48 - Long.numberOfLeadingZeros(movePos); 

		int xMove = moveID/7;
		int yMove = moveID%7;
		int xRemove = removeID/7;
		int yRemove = removeID%7;

		boardButtons[xMove][yMove].setText("A");
		aiButton.setText("");
		aiButton = boardButtons[xMove][yMove];

		boardButtons[xRemove][yRemove].setBackground(Color.BLACK);

		//Make AI move
		aiPos = movePos;
		board = nextState.board;
		playerTurn = true;
		
		if (GameLogic.gameOver(board, playerPos, aiPos))
		{
			gameOver();
		}
	}


	void gameOver()
	{
		int winner = GameLogic.gameWinner(board, playerPos, aiPos);
		int response = -1;
		if (winner == 1)
		{
			response = JOptionPane.showConfirmDialog(null, "Replay?", "You Win!", 1);
		} 
		else if (winner == 2) 
		{
			response = JOptionPane.showConfirmDialog(null, "Replay?", "You Lose!", 1);
		}
		else if (winner == 0 || winner == -1) 
		{
			response = JOptionPane.showConfirmDialog(null, "Replay?", "Draw!", 1);
		}
		else 
		{
			System.out.println("Error, something went wrong");
			System.exit(0);
		}
		
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
		for (int i=0; i<7; i++)
		{
			for (int j=0; j<7; j++)
			{
				boardButtons[i][j].setText("");
				boardButtons[i][j].setBackground(null);
			}
		}
		searchMoves = new SharedVariable(true);
		boardButtons[0][3].setText("A");
		boardButtons[6][3].setText("P");
		aiButton = boardButtons[0][3];
		playerButton = boardButtons[6][3];
		playerTurn = true;
		waitingForMove = true;
		board = initBoardState;
		playerPos = playerOneStartPos;
		aiPos = playerTwoStartPos;
	}
}