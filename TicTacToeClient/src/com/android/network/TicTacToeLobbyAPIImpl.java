package com.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.android.project.TicTacToeGenericActivity;
import com.android.project.helper.TicTacToeHelper;

import android.app.ProgressDialog;
import android.net.http.AndroidHttpClient;

public class TicTacToeLobbyAPIImpl implements TicTacToeLobbyAPI {

	private static final String URI_GETLIST = "http://130.229.154.233:8080/TicTacToe_LobbyServer/LobbyServlet";
	
	private boolean isCalling = false;
	
	private AndroidHttpClient androidHttpClient;
	private TicTacToeGenericActivity activity;
	private Runnable callback;
	private String result;

	public TicTacToeLobbyAPIImpl(TicTacToeGenericActivity activity) {
		this.activity = activity;
	}
	
	public TicTacToeGenericActivity getActivity() {
		return activity;
	}

	public void setActivity(TicTacToeGenericActivity activity) {
		this.activity = activity;
	}
	
	public Runnable getCallback() {
		return callback;
	}

	public void setCallback(Runnable callback) {
		this.callback = callback;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public void createGame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void joinGame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getGameList() {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Getting Game List", "Populating..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_GETGAMELIST);
		connectionThread.start();
	}
	private class ConnectionThread extends Thread {
		
		private int command;
		private String arguments;
		
		public ConnectionThread() {
			
		}
		
		public ConnectionThread(String arguments) {
			this.arguments = arguments;
		}
		
		public void setCommand(int command) {
			this.command = command;
		}

		@Override
		public void run() {
			if (command == TicTacToeHelper.COMMAND_GETGAMELIST) {
				getGameListRequest();
			}
		}

		private void getGameListRequest() {
			String stringBuffer = "";
			androidHttpClient = AndroidHttpClient.newInstance("Android");
			HttpGet httpGet = new HttpGet(URI_GETLIST);
			try {
				HttpResponse response = androidHttpClient.execute(httpGet);
				
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader bufferedReader = new BufferedReader(new 
						InputStreamReader(inputStream),1024);
			    String readLine=bufferedReader.readLine();
			    while (readLine != null) {
			    	stringBuffer += readLine;
			    	readLine=bufferedReader.readLine();
			    }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				setResult(stringBuffer);
				getActivity().runOnUiThread(callback);
				androidHttpClient.close();
				isCalling = false;
			}
		}
	}
}