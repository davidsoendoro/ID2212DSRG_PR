package com.android.project;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.network.TicTacToeLobbyAPIImpl;
import com.android.project.helper.TicTacToeHelper;



import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TicTacToeLobbyPvP extends TicTacToeGenericActivity implements Runnable {
	ListView list;
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
		TicTacToeHelper.lobby.getGameList();
	}

	@Override
	public void run() {
		if(getDialog() != null && getDialog().isShowing()) {
			getDialog().dismiss();
		}


		try {
			JSONObject obj= new JSONObject(TicTacToeHelper.lobby.getResult());
			if(obj.has("Games")){
			JSONArray arr= new JSONArray(obj.getString("Games"));
			for(int i=0;i<arr.length();i++){
				JSONObject item= (JSONObject) arr.get(i);
				gameNames.add(item.getString("name"));
			}
			}
			else if(obj.has("GameId")){
				System.out.println("Received game Id: "+obj.getInt("GameId"));
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		list=(ListView) findViewById(R.id.list);
		ArrayAdapter< String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,gameNames);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TicTacToeHelper.lobby.joinGame(gameNames.get(position));

			}
		});
	}

}
