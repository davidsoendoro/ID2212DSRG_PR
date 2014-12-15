package com.android.project;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.project.helper.TicTacToeHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class TicTacToeOnline extends TicTacToeGenericActivity implements Runnable {

	private int currentState[][] = 
	    {{0,0,0},{0,0,0},{0,0,0}};	// array which stores the movements made.
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_online);
		
		TicTacToeHelper.game.setActivity(this);
		TicTacToeHelper.game.setCallback(this);
//		TicTacToeHelper.game.preventDisconnection();
		initiate();
	}
	
	private void initiate() {
		setContentView(R.layout.main);
		
    	final ImageButton b3 = (ImageButton) findViewById(R.id.b3);
        final ImageButton b2 = (ImageButton) findViewById(R.id.b2);
        final ImageButton b1 = (ImageButton) findViewById(R.id.b1);

        final ImageButton b6 = (ImageButton) findViewById(R.id.b6);
        final ImageButton b5 = (ImageButton) findViewById(R.id.b5);
        final ImageButton b4 = (ImageButton) findViewById(R.id.b4);
        
        final ImageButton b9 = (ImageButton) findViewById(R.id.b9);
        final ImageButton b8 = (ImageButton) findViewById(R.id.b8);
        final ImageButton b7 = (ImageButton) findViewById(R.id.b7);
        
        // set the OnClickListeners.
        b1.setOnClickListener(button_listener);
        b2.setOnClickListener(button_listener);
        b3.setOnClickListener(button_listener);
        b4.setOnClickListener(button_listener);
        b5.setOnClickListener(button_listener);
        b6.setOnClickListener(button_listener);
        b7.setOnClickListener(button_listener);
        b8.setOnClickListener(button_listener);
        b9.setOnClickListener(button_listener);
        
        // Re-enable the Click-able property of buttons.
        b1.setClickable(true);
        b2.setClickable(true);
        b3.setClickable(true);
        b4.setClickable(true);
        b5.setClickable(true);
        b6.setClickable(true);
        b7.setClickable(true);
        b8.setClickable(true);
        b9.setClickable(true);
        
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                currentState[i][j] = 0;
            }
        }
	}

	/**
	 * Common onClickListener for all the ImageButtons in the Game. 
	 * */
    OnClickListener button_listener = new View.OnClickListener() {
        public void onClick(View v) {
            ImageButton ibutton = (ImageButton) v;
        	
        	// Button inactive for further clicks until a result is obtained.
        	ibutton.setClickable(false);
        	
        	// Increment Count on clicking the button.
        	TicTacToeHelper.game.makeMove((String)ibutton.getTag());
        }
    };

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		TicTacToeHelper.game.cancelGame();
	}

	@Override
	public void run() {
		System.out.println("CALLED");
		if(getDialog() != null && getDialog().isShowing())
			getDialog().dismiss();
		String result = TicTacToeHelper.game.getResult();
		
		try {
			JSONObject resultObj = new JSONObject(result);
			String request = resultObj.getString("Request");
			if(request.equals("MakeMove")) {
				System.out.println("make move");
				doGame(resultObj);				
			}
			else if(request.equals("ResetGame")) {
				initiate();
				
				doGame(resultObj);
			}
		} catch (JSONException e) {
			if(result.contains("preventDisconnection")) {
				AlertDialog.Builder alert = new AlertDialog.Builder(TicTacToeOnline.this);
				
				alert.setTitle("Opponent disconnected!");
				alert.setMessage("Opponent is disconnected! You win the game!");
				
				alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						TicTacToeOnline.this.finish();
						TicTacToeHelper.game.cancelGame();
					}
				});
				
				alert.create().show();
			}
		}
		
		
	}

	private void doGame(JSONObject resultObj) throws JSONException {
		JSONObject bodyObj = new JSONObject(resultObj.getString("Body"));
		
		// Redraw Board
		if(bodyObj.has("position")) {
			redrawMap(bodyObj.getString("position"));
		}

		// Redraw Score
		if(bodyObj.has("scoreP1") && bodyObj.has("scoreP2")) {
			int score1 = bodyObj.getInt("scoreP1");
			int score2 = bodyObj.getInt("scoreP2");
			updateScore(score1, score2);
		}
		
		// Popup Box
		if(bodyObj.has("message") && bodyObj.getString("message").length() > 0) {					
			String message = bodyObj.getString("message");
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage(message)
	        			.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
	        				public void onClick(DialogInterface dialog, int id) {
	        		    		// reset the game environment.
	        					TicTacToeHelper.game.resetGame();
	        				}
	        			});
	        AlertDialog alert = builder.create();
	        alert.show();
		}
	}

	private void redrawMap(String body) {
		String rows[] = body.split("/");
		Integer arr[][] = new Integer[3][3];
		
		for(int j = 0; j < 3; j++) {
			String splitResult[] = rows[j].split(",");
			for(int i = 0; i < 3; i++) {
				arr[i][j] = Integer.valueOf(splitResult[i]);
			}
		}
		
		for(int j = 0; j < 3; j++) {
			for(int i = 0; i < 3; i++) {
				if(currentState[i][j] != arr[i][j]) {
					play(i, j, arr[i][j]);
				}
			}
		}
	}

    /**
	 * Make the computer's move.
	 * @param x : the x co-ordinate of the move to made.
	 * @param y : the y co-ordinate of the move to made.
	 */
    public void play (int x, int y, int player) {
       	final ImageButton ib_tmp = (ImageButton) findViewById(R.id.b1);
       	int ib_id = ib_tmp.getId();		// initialize with 1st button's id.
       	
       	// set ib_id to exact ImageButton Id
       	if ((x == 0) && (y == 0)) {	
       		// ib_id same as initialized value.
       	}
       	else {
       		if (x == 0)
       			ib_id -= y;			// minus '-' : because id number not in proper order.
       		else if (x == 1)
       			ib_id += (3 - y);
       		else if (x == 2)
       			ib_id += (6 - y);	
       	}
       	
       	// bind new ib_id Image Button to variable ib.
       	final ImageButton ib = (ImageButton) findViewById (ib_id);
       	
       	// draw the symbol on the button
       	if(player == 1) {
       		ib.setImageResource(R.drawable.default_dot);
       	}
       	else if(player == 2) {
       		ib.setImageResource(R.drawable.default_cross);
       	}
       	
       	// make the button un-clickable.
       	ib.setClickable(false);
    }

    public void updateScore(int score1, int score2){
    	TextView tv = (TextView) findViewById(R.id.scoreboard);

    	CharSequence score_txt = 
    		"Computer : " + score1 + "                   Player : " + score2;
    	
    	tv.setText(score_txt);
    }
}
