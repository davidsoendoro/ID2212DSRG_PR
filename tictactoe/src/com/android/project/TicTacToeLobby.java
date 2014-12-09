package com.android.project;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.project.helper.TicTacToeHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TicTacToeLobby extends TicTacToeGenericActivity implements OnClickListener, Runnable, OnCancelListener {

	private Button buttonLobbyConnect;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GUI
		setContentView(R.layout.lobby);
		
		buttonLobbyConnect = (Button) findViewById(R.id.button_lobby_create);
		buttonLobbyConnect.setOnClickListener(TicTacToeLobby.this);
		Button buttonLobbyJoin = (Button) findViewById(R.id.button_lobby_join);
		buttonLobbyJoin.setOnClickListener(TicTacToeLobby.this);
	}

	@Override
	public void onClick(View v) {
		// Threading
		TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobby.this, 
				"192.168.1.18", 8080);

		if(v.getId() == R.id.button_lobby_create) {
			TicTacToeHelper.game.setCallback(TicTacToeLobby.this);
			TicTacToeHelper.game.createGame();
		}
		else if(v.getId() == R.id.button_lobby_join) {
			TicTacToeHelper.game.setCallback(TicTacToeLobby.this);
			TicTacToeHelper.game.joinGame();
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if(dialog.equals(getDialog())) {
			Log.d(TicTacToeLobby.class.getName(), "CANCELED!");
			TicTacToeHelper.game.setCallback(TicTacToeLobby.this);
			TicTacToeHelper.game.cancelGame();
		}
	}
	
	/**
	 * Thread Callback
	 */
	@Override
	public void run() {
		getDialog().dismiss();
		
		String result = TicTacToeHelper.game.getResult();
		if(result.contains("startGame")) {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle("Waiting for Opponent");
			dialog.setMessage("Waiting...");
			dialog.setCancelable(true);
			dialog.setOnCancelListener(TicTacToeLobby.this);
			setDialog(dialog);
			getDialog().show();
			TicTacToeHelper.game.waitForNewGame();
		}
		else if(result.contains("waitForNewGame")) {
			Intent i = new Intent(TicTacToeLobby.this, TicTacToeOnline.class);
			startActivity(i);
		}
		else if(result.contains("joinGame")) {
			if(result.contains("404")) {
				AlertDialog.Builder alert = new AlertDialog.Builder(TicTacToeLobby.this);
				
				alert.setTitle("Vacant Games not Found!");
				alert.setMessage("Can't find any vacant game! Please create new or try again later!");
				
				alert.create().show();
			}
			else {
				Intent i = new Intent(TicTacToeLobby.this, TicTacToeOnline.class);
				startActivity(i);				
			}
		}
	}

}
