# IsolaAI

# Board Game Description:

Isola is a simple board game played on a 7x7 board where players start in the middle square on opposite sides of the board. Each move consists of the players piece moving to an adjacent piece (including diagonally) and removing a piece from the board. The goal of the game is to stop your opponent from being able to move.

# AI Description

The AI implemented uses min-max with alpha-beta pruning to calculate the best move. The moves are ordered according to the heuristic in order to maximize the pruning from alpha-beta. The AI is given 3 seconds to calculate it's move and during this time it performs an iterative deepening search using the mentioned algorithm. Best moves for states already calculated are stored in a hashtable in order to save computation time. 
