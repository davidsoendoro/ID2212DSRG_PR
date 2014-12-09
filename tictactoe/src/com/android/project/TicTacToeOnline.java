package com.android.project;

import com.android.project.helper.TicTacToeHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class TicTacToeOnline extends TicTacToeGenericActivity implements Runnable {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_online);
		
		TicTacToeHelper.game.setActivity(this);
		TicTacToeHelper.game.setCallback(this);
		TicTacToeHelper.game.preventDisconnection();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		TicTacToeHelper.game.cancelGame();
	}

	@Override
	public void run() {
		String result = TicTacToeHelper.game.getResult();
		
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
