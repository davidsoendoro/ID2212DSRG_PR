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
	 * Start a created game
	 */
	public void startGame();
	
	/**
	 * Cancel to Create a game
	 */
	public void cancelGame();
	
	/**
	 * Wait for peer to start a new game
	 */
	public void waitForNewGame();
	
	public void waitForOpponentMove();
	
	/**
	 * Prevent opponent disconnection
	 */
	public void preventDisconnection();
	
	/**
	 * Make a move
	 * @param position
	 */
	public void makeMove(String position);
	
	public void resetGame();
}
