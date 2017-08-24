import java.io.*;
import java.util.*;

public class SharedVariable
{
	volatile boolean continueSearch;
	volatile HashMap<GameState, GameState> moveCache;

	SharedVariable(boolean incDepth)
	{
		this.continueSearch = incDepth;
		moveCache = new HashMap<GameState, GameState>();
	}

	void invertBoolean()
	{
		this.continueSearch = !this.continueSearch;
	}
}