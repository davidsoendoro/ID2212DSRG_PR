package com.android.project.helper;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeGameAPIP2PImpl;
import com.android.network.TicTacToeLobbyAPIImpl;

public class TicTacToeHelper {

	public static final String serverAddress = "192.168.0.100";
	public static final String serverAddressLobby = "192.168.0.100";
	
	public static TicTacToeGameAPIImpl game;
	public static TicTacToeLobbyAPIImpl lobby;
	public static TicTacToeGameAPIP2PImpl gameP2p;
	
	public static final int COMMAND_CREATEGAME = 1;
	public static final int COMMAND_JOINGAME = 2;
	public static final int COMMAND_STARTGAME = 3;
	public static final int COMMAND_CANCELGAME = 4;
	public static final int COMMAND_CREATESINGLEGAME = 5;
	
	public static final int COMMAND_WAITFORNEWGAME = 10;
	public static final int COMMAND_PREVENTDISCONNECTION = 11;
	public static final int COMMAND_WAITFORMOVE = 12;

	public static final int COMMAND_MAKEMOVE = 20;
	public static final int COMMAND_RESETGAME = 21;

	public static final int COMMAND_GETGAMELIST = 30;

	public static final int PVC = 0;
	public static final int PVP_1stplayer = 1;
	public static final int PVP_2ndplayer = 2;

}
