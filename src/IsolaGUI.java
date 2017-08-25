import java.io.*;
import java.util.*;
import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.concurrent.*;

public class IsolaGUI extends JPanel
{
	//Game class
	private Isola game;

	//GUI variables
	private JButton boardButtons[][] = new JButton[7][7]; 
	private JButton playerOneButton;
	private JButton playerTwoButton;

	IsolaGUI(Isola game)
	{
		this.game = game;
	}

	void setupGUI()
	{
		JFrame mainWindow = new JFrame("Isola");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setSize(800,800);
		mainWindow.setVisible(true);
		mainWindow.setLayout(new GridLayout(7,7));

		for (int i=0; i<7; i++)
		{
			for (int j=0; j<7; j++)
			{
				boardButtons[i][j] = new JButton();
	            boardButtons[i][j].setText("");
	            boardButtons[i][j].addActionListener(new boardListener());
	            mainWindow.add(boardButtons[i][j]);
			}
		}
		boardButtons[0][3].setText("A");
		boardButtons[6][3].setText("P");
		playerOneButton = boardButtons[6][3];
		playerTwoButton = boardButtons[0][3];
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
	
	//Gives a choice to the player for game type
	int gameTypeChoice()
	{
		Object[] options = {"Player vs Player", "Player vs AI", "AI vs AI"};
		int choice = JOptionPane.showOptionDialog(null, "Choose a Game Type", "", 
												JOptionPane.YES_NO_CANCEL_OPTION,
												JOptionPane.QUESTION_MESSAGE,
												null, 
												options, 
												"Player vs Player");
		return choice;
	}

	//Resets GUI board to default state
	void resetGUI()
	{
		for (int i=0; i<7; i++)
		{
			for (int j=0; j<7; j++)
			{
				boardButtons[i][j].setText("");
				boardButtons[i][j].setBackground(null);
			}
		}
		boardButtons[0][3].setText("A");
		boardButtons[6][3].setText("P");
		playerTwoButton = boardButtons[0][3];
		playerOneButton = boardButtons[6][3];
	}

	//Updates buttons corresponding to given move/remove
	void makeMove(boolean playerOneTurn, int xMove, int yMove, int xRemove, int yRemove)
	{
		String playerSymbol = playerOneTurn ? "P" : "A";
		boardButtons[xMove][yMove].setText(playerSymbol); 
		if (playerOneTurn)
		{
			playerOneButton.setText("");
			playerOneButton = boardButtons[xMove][yMove];
		}
		else
		{
			playerTwoButton.setText("");
			playerTwoButton = boardButtons[xMove][yMove];
		}
		if (xRemove != -1)
		{
			boardButtons[xRemove][yRemove].setBackground(Color.BLACK);
		}
		
	}

	private class boardListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			JButton buttonClicked = (JButton)e.getSource();
			int buttonID = getButtonID(buttonClicked);
			long x = buttonID/7 + 1;
			long y = buttonID%7 + 1;
			game.buttonPressed(buttonClicked, x, y);
		}
	}
}