package com.android.project;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeLobbyAPIImpl;
import com.android.project.helper.TicTacToeHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class TicTacToeCreateJoinActivity extends TicTacToeGenericActivity implements OnClickListener,Runnable {
	
	int flag;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.lobby_pvp);
		Button buttonCreate=(Button) findViewById(R.id.button_create);
		buttonCreate.setOnClickListener(this);
		Button buttonJoin=(Button) findViewById(R.id.button_join);
		buttonJoin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.button_create){
			EditText text=(EditText) findViewById(R.id.editText1);
			if(text.getText().toString()!=""){
				flag = TicTacToeHelper.COMMAND_CREATEGAME;
				TicTacToeHelper.lobby = new TicTacToeLobbyAPIImpl(TicTacToeCreateJoinActivity.this);
				TicTacToeHelper.lobby.setActivity(this);
				TicTacToeHelper.lobby.setCallback(this);
				TicTacToeHelper.lobby.createGame(text.getText().toString());
			}
		}
		else if(v.getId()==R.id.button_join){
			Intent intent= new Intent(this, TicTacToeLobbyPvP.class);
			startActivity(intent);
		}
	}

	@Override
	public void run() {
		if(getDialog() != null && getDialog().isShowing()) {
			getDialog().dismiss();
		}

		try {
			if(flag == TicTacToeHelper.COMMAND_CREATEGAME){
				JSONObject obj= new JSONObject(TicTacToeHelper.lobby.getResult());
				if(obj.has("GameId")){
					flag = TicTacToeHelper.COMMAND_STARTGAME;
					int gameId = obj.getInt("GameId");
					System.out.println("Received game Id: " + gameId);
					
					TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeCreateJoinActivity.this, 
							TicTacToeHelper.serverAddress, 8090);
					TicTacToeHelper.game.setCallback(TicTacToeCreateJoinActivity.this);
					TicTacToeHelper.game.createGame(gameId);
				}
			}
			else if(flag == TicTacToeHelper.COMMAND_STARTGAME){
				JSONObject obj= new JSONObject(TicTacToeHelper.game.getResult());
				if(obj.getString("Request").equals("createGame")) {
					flag = TicTacToeHelper.COMMAND_WAITFORNEWGAME;
					TicTacToeHelper.game.waitForNewGame();
				}
			}
			else if(flag == TicTacToeHelper.COMMAND_WAITFORNEWGAME) {
				// Callback for waitForNewGame
				Intent i = new Intent(this, TicTacToeOnline.class);
				i.putExtra("mode", TicTacToeHelper.PVP_1stplayer);
				startActivity(i);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
