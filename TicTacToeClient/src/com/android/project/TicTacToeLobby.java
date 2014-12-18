package com.android.project;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.project.helper.TicTacToeHelper;

public class TicTacToeLobby extends TicTacToeGenericActivity implements OnClickListener, Runnable, OnCancelListener {

	private Button buttonLobbyConnect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GUI
		setContentView(R.layout.lobby);
		
		buttonLobbyConnect = (Button) findViewById(R.id.button_lobby_create);
		buttonLobbyConnect.setOnClickListener(TicTacToeLobby.this);
	}

	/**
	 * Listener for create game button
	 */
	@Override
	public void onClick(View v) {
		String ip = TicTacToeHelper.serverAddress;
		int port = 8090;
		
		EditText editTextIp = (EditText) findViewById(R.id.editText_lobby_ip);
		EditText editTextPort = (EditText) findViewById(R.id.editText_lobby_port);
		
		if(editTextIp.getText().length() > 0) {
			ip = editTextIp.getText().toString();
		}
		if(editTextPort.getText().length() > 0) {
			port = Integer.valueOf(editTextPort.getText().toString());
		}
		
		// Threading
		TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobby.this, 
				ip, port);

		if(v.getId() == R.id.button_lobby_create) {
			TicTacToeHelper.game.setCallback(TicTacToeLobby.this);
			TicTacToeHelper.game.createSingleGame();					
		}
		else if(v.getId() == R.id.button_lobby_join) {
			TicTacToeHelper.game.setCallback(TicTacToeLobby.this);
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
		try {
			JSONObject resultObj = new JSONObject(result);
			
			if(resultObj.getString("Request").equals("NewSingleGame")) {
				// Callback for NewSingleGame
				Intent i = new Intent(TicTacToeLobby.this, TicTacToeOnline.class);
				startActivity(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

}
