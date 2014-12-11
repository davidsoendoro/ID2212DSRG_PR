package com.android.network;

public interface TicTacToeLobbyAPI {

	/**
	 * Create the game in server via API
	 */
	public void createGame();

	/**
	 * Join the game in server via API
	 */
	public void joinGame();
	
	/**
	 * Get Game List
	 */
	public void getGameList();
}
