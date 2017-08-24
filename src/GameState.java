import java.io.*;
import java.util.*;
import java.lang.*;

//State class for holding information
//about the game
public class GameState
{
	long board;
	boolean playerTurn;
	long playerPos;
	long aiPos;
	int curDepth;
	int maxDepth;

	GameState(long board, boolean playerTurn, long playerPos, long aiPos, int depth, int maxDepth)
	{
		this.board = board;
		this.playerTurn = playerTurn;
		this.playerPos = playerPos;
		this.aiPos = aiPos;
		this.curDepth = depth;
		this.maxDepth = maxDepth;
	}

	GameState(GameState another)
	{
		this.board = another.board;
		this.playerTurn = another.playerTurn;
		this.playerPos = another.playerPos;
		this.aiPos = another.aiPos;
		this.curDepth = another.curDepth;
		this.maxDepth = another.maxDepth;
	}

	@Override
	public int hashCode()
	{
		int result = 17;
		result = 31 * result + Long.hashCode(board);
		result = 31 * result + Long.hashCode(playerPos);
		result = 31 * result + Long.hashCode(aiPos);
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
        		&&	(this.playerPos == state.playerPos)
        		&&	(this.aiPos == state.aiPos)
        		&& 	(this.curDepth == state.curDepth);
	}
}


	