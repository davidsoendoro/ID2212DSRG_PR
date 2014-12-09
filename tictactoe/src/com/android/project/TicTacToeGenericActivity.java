package com.android.project;

import android.app.Activity;
import android.app.ProgressDialog;

public class TicTacToeGenericActivity extends Activity {

	private ProgressDialog dialog;
	
	public void setDialog(ProgressDialog show) {
		this.dialog = show;
	}

	public ProgressDialog getDialog() {
		return dialog;
	}

}
