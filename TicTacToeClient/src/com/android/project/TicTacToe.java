package com.android.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;


public class TicTacToe extends Activity {
	
    private static final int MENU_QUIT = 0;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        final ImageView iv_new_game_online = (ImageView) findViewById(R.id.new_game_pvc_online);
        final ImageView iv_new_game = (ImageView) findViewById(R.id.new_game_pvc_offline);
        final ImageView iv_new_game_pvp_online = (ImageView) findViewById(R.id.new_game_pvp_online);
        final ImageView iv_new_game_pvp_p2p = (ImageView) findViewById(R.id.new_game_pvp_p2p);
        final ImageView iv_quit = (ImageView) findViewById(R.id.quit);

        iv_new_game_online.setOnClickListener(welcome_listener);
        iv_new_game.setOnClickListener(welcome_listener);
        iv_new_game_pvp_online.setOnClickListener(welcome_listener);
        iv_new_game_pvp_p2p.setOnClickListener(welcome_listener);
        iv_quit.setOnClickListener(welcome_listener);
    }
    
    /** 
     * Common onClickListener for Welcome Screen Buttons 
     * */
    OnClickListener welcome_listener = new View.OnClickListener() {
    	public void onClick(View v) {
    		final ImageView iv = (ImageView) v;
    		if (iv.getId() == R.id.new_game_pvc_online) {
    			Intent i = new Intent(TicTacToe.this, TicTacToeLobby.class);
    			startActivity(i);
    		}
    		else if (iv.getId() == R.id.new_game_pvc_offline) {
    			Intent i = new Intent(TicTacToe.this, TicTacToeGame.class);
    			startActivity(i);
    		}
    		else if (iv.getId() == R.id.new_game_pvp_online) {
    			Intent i = new Intent(TicTacToe.this, TicTacToeCreateJoinActivity.class);
    			startActivity(i);
    		}
    		else if (iv.getId() == R.id.new_game_pvp_p2p) {
    			Intent i = new Intent(TicTacToe.this, TicTacToeLobbyP2P.class);
    			startActivity(i);
    		}
    		
    		
    		else if (iv.getId() == R.id.quit) {
    			finish();
    		}
    	}
    };
    
    public void startNewGame(CharSequence player_name){
    	// reset the game view. (this must be the first line in this function)
    }
    /**
     * Creates the menu items 
     * */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
    }
   
    /**
     * Handles item selections in the options menu. 
     * */
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == MENU_QUIT) {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do You Really Want to Quit?");
            builder.setCancelable(true);
              builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            	  public void onClick(DialogInterface dialog, int id) {
    					// do nothing, since setCancelable is true, canceling is enabled
    				}
            	});
              builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				finish();
    				
    			}
    		});
            AlertDialog alert = builder.create();
            alert.show();    		
    	}
    	return true;
    }
    
}