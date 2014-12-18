package com.android.project.helper;

import android.os.Build;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeGameAPIP2PImpl;
import com.android.network.TicTacToeLobbyAPIImpl;

public class TicTacToeHelper {

	public static final String serverAddress = "192.168.1.18";
	public static final String serverAddressLobby = "192.168.1.18";
	
	public static TicTacToeGameAPIImpl game;
	public static TicTacToeLobbyAPIImpl lobby;
	public static TicTacToeGameAPIP2PImpl gameP2p;
	public static final int COMMAND_CREATEGAME = 1;
	public static final int COMMAND_JOINGAME = 2;
	public static final int COMMAND_STARTGAME = 3;
	public static final int COMMAND_CANCELGAME = 4;
	public static final int COMMAND_CREATESINGLEGAME = 5;
	
	public static final int COMMAND_WAITFORNEWGAME = 10;
	public static final int COMMAND_WAITFORMOVE = 12;

	public static final int COMMAND_MAKEMOVE = 20;
	public static final int COMMAND_RESETGAME = 21;

	public static final int COMMAND_GETGAMELIST = 30;
	public static final int COMMAND_GETSCORE = 31;
	public static final int PVC = 0;
	public static final int PVP_1stplayer = 1;
	public static final int PVP_2ndplayer = 2;
	
	public static final int P2P_SERVERDONE = 0;
	public static final int P2P_CLIENTDONE = 1;
	
	public static String IMEI;

	/**
	 * Get the phone model
	 * @return
	 */
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}
	/**
	 * Set everything in Capital letter
	 * @param s
	 * @return word with all capital letter
	 */
	private static String capitalize(String s) {
	    if (s == null || s.length() == 0) {
	        return "";
	    }
	    char first = s.charAt(0);
	    if (Character.isUpperCase(first)) {
	        return s;
	    } else {
	        return Character.toUpperCase(first) + s.substring(1);
	    }
	}
}
