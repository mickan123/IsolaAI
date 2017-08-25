import java.io.*;
import java.util.*;
import java.lang.*;

//State class for holding information
//about the game
public class GameState
{
	long board;
	boolean playerTurn;
	long playerOnePos;
	long playerTwoPos;
	int curDepth;
	int maxDepth;

	GameState(long board, boolean playerTurn, long playerOnePos, long playerTwoPos, int depth, int maxDepth)
	{
		this.board = board;
		this.playerTurn = playerTurn;
		this.playerOnePos = playerOnePos;
		this.playerTwoPos = playerTwoPos;
		this.curDepth = depth;
		this.maxDepth = maxDepth;
	}

	GameState(GameState another)
	{
		this.board = another.board;
		this.playerTurn = another.playerTurn;
		this.playerOnePos = another.playerOnePos;
		this.playerTwoPos = another.playerTwoPos;
		this.curDepth = another.curDepth;
		this.maxDepth = another.maxDepth;
	}

	@Override
	public int hashCode()
	{
		int result = 17;
		result = 31 * result + Long.hashCode(board);
		result = 31 * result + Long.hashCode(playerOnePos);
		result = 31 * result + Long.hashCode(playerTwoPos);
		result = 31 * result + maxDepth;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
        if (!(obj instanceof GameState)) return false;

		final GameState state = (GameState)obj;

		return		(this.board == state.board)
        		&&	(this.playerTurn == state.playerTurn)
        		&&	(this.playerOnePos == state.playerOnePos)
        		&&	(this.playerTwoPos == state.playerTwoPos)
        		&& 	(this.curDepth == state.curDepth);
	}
}


	