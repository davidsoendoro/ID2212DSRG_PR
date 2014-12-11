package com.android.project;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class lobbyListActivity extends TicTacToeGenericActivity {
ListView list;
String[] gameNames;
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.lobby_list);
	        list=(ListView) findViewById(R.id.list);
	        
	        ArrayAdapter< String> adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,gameNames);
	        list.setAdapter(adapter);
	        list.setOnItemClickListener(new OnItemClickListener() {
	        	
	        	 public void onItemClick(AdapterView<?> parent, View view,
	                   int position, long id) {
	                  
	             //    gameNames[position]
	                
	                    
	                  
	               
	                }
			});
	 }
	 
	 
	
}
