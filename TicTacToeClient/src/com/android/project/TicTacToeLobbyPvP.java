package com.android.project;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.network.TicTacToeGameAPIImpl;
import com.android.network.TicTacToeLobbyAPIImpl;
import com.android.project.helper.TicTacToeHelper;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TicTacToeLobbyPvP extends TicTacToeGenericActivity implements Runnable {
	ListView list;
	int flag=0;
	ArrayList<String> gameNames=new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.lobby_list);

		TicTacToeHelper.lobby = new TicTacToeLobbyAPIImpl(TicTacToeLobbyPvP.this);
		TicTacToeHelper.lobby.setActivity(this);
		TicTacToeHelper.lobby.setCallback(this);

		getAllGames();

	}

	private void getAllGames() {
		flag=1;
		TicTacToeHelper.lobby.getGameList();
	}

	@Override
	public void run() {
		if(getDialog() != null && getDialog().isShowing()) {
			getDialog().dismiss();
		}

		try {
			if(flag==1) {
				JSONObject obj= new JSONObject(TicTacToeHelper.lobby.getResult());
				if(obj.has("Games")){
					flag=0;
					JSONArray arr= new JSONArray(obj.getString("Games"));
					for(int i=0;i<arr.length();i++){
						JSONObject item= (JSONObject) arr.get(i);
						gameNames.add(item.getString("name"));
					}
				}
				else if(obj.has("GameId")){
					flag=0;
					System.out.println("Received game Id: "+obj.getInt("GameId"));
					TicTacToeHelper.game = new TicTacToeGameAPIImpl(TicTacToeLobbyPvP.this, 
						TicTacToeHelper.serverAddress, 8090);
					TicTacToeHelper.game.setCallback(TicTacToeLobbyPvP.this);
					TicTacToeHelper.game.joinGame(obj.getInt("GameId"));
				}
			}
			else if(flag==0){
				JSONObject obj= new JSONObject(TicTacToeHelper.game.getResult());
				if(obj.getString("Request").equals("joinGame")) {
					// Callback for NewSingleGame
					System.out.println("I am here");
					Intent i = new Intent(this, TicTacToeOnline.class);
					i.putExtra("mode", TicTacToeHelper.PVP_2ndplayer);
					startActivity(i);
				}
			}
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		list=(ListView) findViewById(R.id.list);
		ArrayAdapter< String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,gameNames);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				flag=1;
				TicTacToeHelper.lobby.joinGame(gameNames.get(position));

			}
		});
	}

}
