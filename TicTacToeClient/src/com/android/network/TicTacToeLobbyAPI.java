package com.android.network;

public interface TicTacToeLobbyAPI {

	/**
	 * Create the game in server via API
	 */
	public void createGame(String name);

	/**
	 * Join the game in server via API
	 */
	public void joinGame(String name);
	
	/**
	 * Get Game List
	 */
	public void getGameList();
}
