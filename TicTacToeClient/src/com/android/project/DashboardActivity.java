package com.android.project;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.widget.TextView;

import com.android.network.TicTacToeLobbyAPIImpl;
import com.android.project.helper.TicTacToeHelper;

public class DashboardActivity extends TicTacToeGenericActivity implements Runnable{

	/**
	 * Called when activity is started, implementing the GUI
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		TicTacToeHelper.lobby = new TicTacToeLobbyAPIImpl(DashboardActivity.this);
		TicTacToeHelper.lobby.setActivity(this);
		TicTacToeHelper.lobby.setCallback(this);
		TicTacToeHelper.lobby.getScore();
	}

	/**
	 * Callback to draw the resulted request of HTTP getScore
	 */
	public void run(){
		if(getDialog() != null && getDialog().isShowing()) {
			getDialog().dismiss();
		}
		TextView win=(TextView) findViewById(R.id.gamesWon);
		TextView lost=(TextView) findViewById(R.id.gamesLost);
		TextView draw=(TextView) findViewById(R.id.gamesDraw);
		try{
			JSONObject obj= new JSONObject(TicTacToeHelper.lobby.getResult());
			if(obj.has("User")){
				String str=obj.getString("User");
				JSONObject item=new JSONObject(str);
				win.setText(item.getString("win"));
				lost.setText(item.getString("lose"));
				draw.setText(item.getString("draw"));
			}
		}
		catch(JSONException jex){
			System.out.println("Exception"+ jex);
		}
	}
}