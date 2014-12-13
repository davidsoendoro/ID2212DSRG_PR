package com.android.project;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.network.TicTacToeLobbyAPIImpl;
import com.android.project.helper.TicTacToeHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class TicTacToeCreateJoinActivity extends TicTacToeGenericActivity implements OnClickListener,Runnable {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.lobby_pvp);
		Button b1=(Button) findViewById(R.id.button1);
		b1.setOnClickListener(this);
		Button b2=(Button) findViewById(R.id.button2);
		b2.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.button1){
			EditText text=(EditText) findViewById(R.id.editText1);
			if(text.getText().toString()!=""){
				TicTacToeHelper.lobby = new TicTacToeLobbyAPIImpl(TicTacToeCreateJoinActivity.this);
				TicTacToeHelper.lobby.setActivity(this);
				TicTacToeHelper.lobby.setCallback(this);
				TicTacToeHelper.lobby.createGame(text.getText().toString());
			}
		}
		else if(v.getId()==R.id.button2){
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
			JSONObject obj= new JSONObject(TicTacToeHelper.lobby.getResult());
			if(obj.has("GameId")){
				System.out.println("Received game Id: "+obj.getInt("GameId"));
				TicTacToeHelper.game.setCallback(TicTacToeCreateJoinActivity.this);
				//TicTacToeHelper.game.createGame(""+obj.getInt("GameId"));
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
