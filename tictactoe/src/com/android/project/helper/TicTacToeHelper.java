package com.android.project.helper;

import com.android.network.TicTacToeGameAPIImpl;

public class TicTacToeHelper {

	public static TicTacToeGameAPIImpl game;
	
	public static final int COMMAND_CREATEGAME = 1;
	public static final int COMMAND_JOINGAME = 2;
	public static final int COMMAND_STARTGAME = 3;
	public static final int COMMAND_CANCELGAME = 4;

	public static final int COMMAND_WAITFORNEWGAME = 10;
	public static final int COMMAND_PREVENTDISCONNECTION = 11;
	
}
