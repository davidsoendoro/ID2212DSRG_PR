package com.android.project.helper;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeLobbyAPIImpl;

public class TicTacToeHelper {

	public static TicTacToeGameAPIImpl game;
	public static TicTacToeLobbyAPIImpl lobby;
	
	public static final int COMMAND_CREATEGAME = 1;
	public static final int COMMAND_JOINGAME = 2;
	public static final int COMMAND_STARTGAME = 3;
	public static final int COMMAND_CANCELGAME = 4;
	public static final int COMMAND_CREATESINGLEGAME = 5;
	
	public static final int COMMAND_WAITFORNEWGAME = 10;
	public static final int COMMAND_PREVENTDISCONNECTION = 11;

	public static final int COMMAND_MAKEMOVE = 20;
	public static final int COMMAND_RESETGAME = 21;

	public static final int COMMAND_GETGAMELIST = 30;

}
