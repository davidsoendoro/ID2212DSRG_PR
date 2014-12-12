package com.android.project;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeGameAPIP2PImpl;
import com.android.project.helper.TicTacToeHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TicTacToeLobbyP2P extends TicTacToeGenericActivity implements OnClickListener, Runnable, OnCancelListener {

	private Button buttonLobbyConnect;
	private TextView textViewUserIp;
//	private CheckBox checkBoxVsPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GUI
		setContentView(R.layout.lobby_p2p);
		
		textViewUserIp = (TextView) findViewById(R.id.textView_lobby_ip);
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		textViewUserIp.setText("Your IP: " + ip + " port: 8090");
		
		buttonLobbyConnect = (Button) findViewById(R.id.button_lobby_create);
		buttonLobbyConnect.setOnClickListener(TicTacToeLobbyP2P.this);
		Button buttonLobbyJoin = (Button) findViewById(R.id.button_lobby_join);
		buttonLobbyJoin.setOnClickListener(TicTacToeLobbyP2P.this);
//		checkBoxVsPlayer = (CheckBox) findViewById(R.id.checkBox_vsplayer);
	}

	@Override
	public void onClick(View v) {
		String ip = "130.229.154.233";
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
		if(TicTacToeHelper.game.getSocket() != null) {
			if(v.getId() == R.id.button_lobby_create) {
				TicTacToeHelper.gameP2p = new TicTacToeGameAPIP2PImpl(TicTacToeLobbyP2P.this, 
						ip, port);
				TicTacToeHelper.gameP2p.setCallback(TicTacToeLobbyP2P.this);
				TicTacToeHelper.gameP2p.createGame();					
			}
			else if(v.getId() == R.id.button_lobby_join) {
				TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobbyP2P.this, 
						ip, port);
				TicTacToeHelper.game.setCallback(TicTacToeLobbyP2P.this);
				TicTacToeHelper.game.joinGame();
			}
		}
		else {
			Toast.makeText(this, "Unable to connect!", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if(dialog.equals(getDialog())) {
			Log.d(TicTacToeLobbyP2P.class.getName(), "CANCELED!");
			TicTacToeHelper.game.setCallback(TicTacToeLobbyP2P.this);
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
				Intent i = new Intent(TicTacToeLobbyP2P.this, TicTacToeOnline.class);
				startActivity(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			
			if(result.contains("startGame")) {
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setTitle("Waiting for Opponent");
				dialog.setMessage("Waiting...");
				dialog.setCancelable(true);
				dialog.setOnCancelListener(TicTacToeLobbyP2P.this);
				setDialog(dialog);
				getDialog().show();
				TicTacToeHelper.game.waitForNewGame();
			}
			else if(result.contains("waitForNewGame")) {
				Intent i = new Intent(TicTacToeLobbyP2P.this, TicTacToeOnline.class);
				startActivity(i);
			}
			else if(result.contains("joinGame")) {
				if(result.contains("404")) {
					AlertDialog.Builder alert = new AlertDialog.Builder(TicTacToeLobbyP2P.this);
					
					alert.setTitle("Vacant Games not Found!");
					alert.setMessage("Can't find any vacant game! Please create new or try again later!");
					
					alert.create().show();
				}
				else {
					Intent i = new Intent(TicTacToeLobbyP2P.this, TicTacToeOnline.class);
					startActivity(i);				
				}
			}
		}
		
	}

}
