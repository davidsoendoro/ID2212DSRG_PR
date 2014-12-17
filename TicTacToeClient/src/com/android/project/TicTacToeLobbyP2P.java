package com.android.project;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeGameAPIP2PImpl;
import com.android.project.helper.TicTacToeHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.nsd.NsdManager;
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
	private boolean isServer = false;
	DiscoverService service;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GUI
		
		/*setContentView(R.layout.lobby_p2p);
		
		textViewUserIp = (TextView) findViewById(R.id.textView_lobby_ip);
		WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
		textViewUserIp.setText("Your IP: " + ip + " port: 8090");
		
		buttonLobbyConnect = (Button) findViewById(R.id.button_lobby_create);
		buttonLobbyConnect.setOnClickListener(TicTacToeLobbyP2P.this);
		Button buttonLobbyJoin = (Button) findViewById(R.id.button_lobby_join);
		buttonLobbyJoin.setOnClickListener(TicTacToeLobbyP2P.this);
//		checkBoxVsPlayer = (CheckBox) findViewById(R.id.checkBox_vsplayer);
 */
		setContentView(R.layout.lobby_pvp);
		Button buttonCreate=(Button) findViewById(R.id.button_create);
		buttonCreate.setOnClickListener(this);
		Button buttonJoin=(Button) findViewById(R.id.button_join);
		buttonJoin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String ip = "130.229.154.233";
		int port = 8090;
		/*
		EditText editTextIp = (EditText) findViewById(R.id.editText_lobby_ip);
		EditText editTextPort = (EditText) findViewById(R.id.editText_lobby_port);
		
		if(editTextIp.getText().length() > 0) {
			ip = editTextIp.getText().toString();
		}
		if(editTextPort.getText().length() > 0) {
			port = Integer.valueOf(editTextPort.getText().toString());
		}
		*/
		// Threading
		if(v.getId() == R.id.button_create) {
			TicTacToeHelper.gameP2p = new TicTacToeGameAPIP2PImpl(TicTacToeLobbyP2P.this);
			TicTacToeHelper.gameP2p.setCallback(TicTacToeLobbyP2P.this);
			isServer = true;
			RegisterService service=new RegisterService((NsdManager) getSystemService(Context.NSD_SERVICE));
			service.registerService(8090);
			TicTacToeHelper.gameP2p.createGame(0);
		}
		else if(v.getId() == R.id.button_join) {
			service=new DiscoverService((NsdManager) getSystemService(Context.NSD_SERVICE));
			
			service.setCallback(this);
			service.setActivity(this);
			service.discover();
			/*
			TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobbyP2P.this, 
					ip, port);
			if(TicTacToeHelper.game.getSocket() != null) {
				TicTacToeHelper.game.setCallback(TicTacToeLobbyP2P.this);
				TicTacToeHelper.game.createGame(0);
			}
			else {
				Toast.makeText(this, "Unable to connect!", Toast.LENGTH_SHORT).show();
			}*/
		}
	}

	public void createGame(){
		TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobbyP2P.this, 
				service.hostAddress, service.hostPort);
		if(TicTacToeHelper.game.getSocket() != null) {
			TicTacToeHelper.game.setCallback(TicTacToeLobbyP2P.this);
			TicTacToeHelper.game.createGame(0);
		}
		else {
			this.runOnUiThread(new Runnable() {
				  public void run() {
				    Toast.makeText(TicTacToeLobbyP2P.this, "Unable to connect!", Toast.LENGTH_SHORT).show();
				  }
				});
			
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
		if(getDialog() != null && getDialog().isShowing()) {
			getDialog().dismiss();
		}
		
		String result = "";
		if(isServer) {
			result = TicTacToeHelper.gameP2p.getResult();			
		}
		else {
			if(TicTacToeHelper.game==null){
				Thread t= new Thread(){
					public void run(){
						createGame();
					}
			};
			t.start();
			return;
			}
			else
				result = TicTacToeHelper.game.getResult();			
		}
		try {
			JSONObject resultObj = new JSONObject(result);
			
			if(resultObj.getString("Request").equals("P2PcreateGame")) {
				// Callback for P2PcreateGame
				Intent i = new Intent(TicTacToeLobbyP2P.this, TicTacToeServer.class);
				i.putExtra("mode", TicTacToeHelper.PVP_1stplayer);
				startActivity(i);
			}
			else if(resultObj.getString("Request").equals("createGame")) {
				// Callback for joinGame
				Intent i = new Intent(this, TicTacToeOnline.class);
				i.putExtra("mode", TicTacToeHelper.PVP_2ndplayer);
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
				i.putExtra("mode", TicTacToeHelper.PVP_1stplayer);
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
					i.putExtra("mode", TicTacToeHelper.PVP_2ndplayer);
					startActivity(i);				
				}
			}
		}
		
	}

}
