package com.android.network;

public interface TicTacToeGameAPI {

	/**
	 * Create a new game vs AI
	 */
	public void createSingleGame();
	
	/**
	 * Create a new game, if success then wait for contender
	 */
	public void createGame(int id);
	
	/**
	 * If a game is open, join that game, if no game is opened then return error
	 */
	public void joinGame(int id);
	
	/**
	 * Cancel to Create a game
	 */
	public void cancelGame();
	
	/**
	 * Wait for peer to start a new game
	 */
	public void waitForNewGame();
	
	/**
	 * Wait for opponent move, display indeterminate progress dialog
	 */
	public void waitForOpponentMove();
	
	/**
	 * Make a move to position
	 * @param position
	 */
	public void makeMove(String position);
	
	/**
	 * Clear the board and start a new game
	 */
	public void resetGame();
}
