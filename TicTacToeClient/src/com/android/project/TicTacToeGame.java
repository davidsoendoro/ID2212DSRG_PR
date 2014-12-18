package com.android.project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class TicTacToeGame extends TicTacToeGenericActivity {

	GameLogic gameLogic;

	// **************************** Global variables ********************************************
	int count = 0;			// to count the number of moves made.
    
    int game_mode = 1;		// default 0 : h Vs h ; 1 : h Vs Comp
    int player = 1;			// sets the player no. to 1 by default.
    int skin_cross = R.drawable.default_cross;	// default values.
	int skin_dot = R.drawable.default_dot;		// default values.
	
	int user_symbol = 0;	// default 0: 0 to user, X to computer.

	// player names initialized with default values.
	CharSequence player_name_1 = "Player X";
	CharSequence player_name_2 = "Player O";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        gameLogic = new GameLogic(this);

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
        gameLogic.new_game();
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
        	count = gameLogic.incrementCount();
        	
            gameLogic.setPlayer(2);
            ibutton.setImageResource(skin_dot);
            
            // after_move function to check the result and decide.
            gameLogic.after_move((CharSequence)ibutton.getTag());
        }
    };

    /**
	 * Make the computer's move.
	 * @param x : the x co-ordinate of the move to made.
	 * @param y : the y co-ordinate of the move to made.
	 */
    public void comp_play (int x, int y) {
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
       	if (user_symbol == 0)
       		ib.setImageResource(skin_cross);
       	else
       		ib.setImageResource(skin_dot);
       	
       	// make the button un-clickable.
       	ib.setClickable(false);
   
       	// call the after_move function with the arguments.
       	gameLogic.after_move((CharSequence)ib.getTag());
    }

	//************************ End of Global Variable Declaration **********************************     
    /**
     * Displays the message in dialog
     * @param message received from server
     * @return True if dialog is shown and false if dialog is not shown
     */
    public boolean show_result(CharSequence message)		//function to select the game mode
    {   
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
        			.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int id) {
        		    		// reset the game environment.
        					initiate();
        				}
        			});
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }
    
    /**
     * Updates score on board
     * @param score1: Player 1's score
     * @param score2: Players 2's score
     */
    public void updateScore(int score1, int score2){
    	TextView tv = (TextView) findViewById(R.id.scoreboard);

    	if (game_mode == 1) {
    		player_name_1 = "Computer";
    	}
    		
    	CharSequence score_txt = 
    		player_name_1 + " : " + score1 + "                   " + player_name_2 + " : " + score2;
    	
    	tv.setText(score_txt);
    }
}
