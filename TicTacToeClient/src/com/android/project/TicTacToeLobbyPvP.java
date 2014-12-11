package com.android.project;

import com.android.network.TicTacToeLobbyAPIImpl;
import com.android.project.helper.TicTacToeHelper;

import android.os.Bundle;

public class TicTacToeLobbyPvP extends TicTacToeGenericActivity implements Runnable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.lobby_pvp);
		
		TicTacToeHelper.lobby = new TicTacToeLobbyAPIImpl(TicTacToeLobbyPvP.this);
		TicTacToeHelper.lobby.setActivity(this);
		TicTacToeHelper.lobby.setCallback(this);

		getAllGames();
	}

	private void getAllGames() {
		TicTacToeHelper.lobby.getGameList();
	}

	@Override
	public void run() {
		if(getDialog() != null && getDialog().isShowing()) {
			getDialog().dismiss();
		}
		
		System.out.println(TicTacToeHelper.lobby.getResult());
	}
	
}
