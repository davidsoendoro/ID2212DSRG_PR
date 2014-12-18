package com.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import android.app.ProgressDialog;
import android.net.http.AndroidHttpClient;

import com.android.project.TicTacToeGenericActivity;
import com.android.project.helper.TicTacToeHelper;

public class TicTacToeLobbyAPIImpl implements TicTacToeLobbyAPI {

	private static final String URI_SERVERADDR = "http://" + TicTacToeHelper.serverAddressLobby + ":8080";
	private static final String URI_GETLIST = URI_SERVERADDR + "/TicTacToe_LobbyServer/LobbyServlet";
	private static final String URI_CREATEGAME = URI_SERVERADDR + "/TicTacToe_LobbyServer/CreateGame?name=";
	private static final String URI_JOINGAME = URI_SERVERADDR + "/TicTacToe_LobbyServer/JoinGame?name=";
	private static final String URI_GETSCORE = URI_SERVERADDR + "/TicTacToe_LobbyServer/GetScore?username=";

	private boolean isCalling = false;
	private AndroidHttpClient androidHttpClient;
	private TicTacToeGenericActivity activity;
	private Runnable callback;
	private String result;

	/**
	 * Constructor for network game lobby functions
	 * @param activity
	 */
	public TicTacToeLobbyAPIImpl(TicTacToeGenericActivity activity) {
		this.activity = activity;
		
	}

	/**
	 * Get activity of the network function caller - used for GUI purposes
	 * @return the activity caller
	 */
	public TicTacToeGenericActivity getActivity() {
		return activity;
	}

	/**
	 * Set activity of the network function caller - used for GUI purposes
	 * @param activity
	 */
	public void setActivity(TicTacToeGenericActivity activity) {
		this.activity = activity;
	}

	/**
	 * Set callback for the network function
	 * @param callback
	 */
	public void setCallback(Runnable callback) {
		this.callback = callback;
	}

	/**
	 * Get the result of the latest process
	 * @return result in JSON format in String
	 */
	public String getResult() {
		return result;
	}

	/**
	 * Set the result of the latest process
	 * @param result
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Create a new game, if success then it will set the result to a GameId
	 */
	@Override
	public void createGame(String name) {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Creating Game", "Creating..."));
		getActivity();
		
		ConnectionThread connectionThread = new ConnectionThread(name);
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CREATEGAME);
		connectionThread.start();
	}

	/**
	 * If a game is open, join that game and set result to GameId, 
	 * if no game is opened then display error
	 */
	@Override
	public void joinGame(String name) {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Joining Game", "Joining..."));
		
		ConnectionThread connectionThread = new ConnectionThread(name);
		connectionThread.setCommand(TicTacToeHelper.COMMAND_JOINGAME);
		connectionThread.start();
		

	}

	/**
	 * Get all open game from lobby server
	 */
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
	
	/**
	 * Get caller score from lobby server
	 */
	public void getScore() {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Getting Score", "Populating..."));
		getActivity();

		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_GETSCORE);
		connectionThread.start();
	}

	/**
	 * ConnectionThread is the thread that is used to run the networking function
	 * itself after it receives command from the TicTacToeLobbyAPIImpl class.
	 * @author davidsoendoro-rohitgoyal
	 *
	 */
	private class ConnectionThread extends Thread {
		
		private int command;
		private String arguments;

		/**
		 * Plain ConnectionThread constructor
		 */
		public ConnectionThread() {
			
		}

		/**
		 * ConnectionThread constructor with arguments
		 * @param arguments
		 */
		public ConnectionThread(String arguments) {
			this.arguments = arguments;
		}

		/**
		 * For other class to set the command need to be used by ConnectionThread
		 * @param command
		 */
		public void setCommand(int command) {
			this.command = command;
		}

		/**
		 * Main algorithm of ConnectionThread
		 */
		@Override
		public void run() {
			if (command == TicTacToeHelper.COMMAND_GETGAMELIST) {
				getGameListRequest();
			}
			else if(command == TicTacToeHelper.COMMAND_CREATEGAME){
				createGameRequest();
			}
			else if(command == TicTacToeHelper.COMMAND_JOINGAME){
				joinGameRequest();
			}
			else if(command == TicTacToeHelper.COMMAND_GETSCORE){
				getScoreRequest();
			}
		}

		/**
		 * To Send HTTP request towards Lobby Server to get the list of games
		 */
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

		/**
		 * To Send HTTP request towards Lobby Server to get requester score
		 */
		private void getScoreRequest() {
			String stringBuffer = "";
			androidHttpClient = AndroidHttpClient.newInstance("Android");
			HttpGet httpGet = new HttpGet(URI_GETSCORE + TicTacToeHelper.IMEI);
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
				System.out.println(stringBuffer);
				setResult(stringBuffer);
				getActivity().runOnUiThread(callback);
				androidHttpClient.close();
				isCalling = false;
			}
		}

		/**
		 * To Send HTTP request towards Lobby Server to create a new game
		 */
		private void createGameRequest() {
			String stringBuffer = "";
			androidHttpClient = AndroidHttpClient.newInstance("Android");
			HttpGet httpGet = new HttpGet(URI_CREATEGAME+this.arguments+"&username=" + TicTacToeHelper.IMEI);
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

		/**
		 * To Send HTTP request towards Lobby Server to join a game
		 */
		private void joinGameRequest() {
			String stringBuffer = "";
			androidHttpClient = AndroidHttpClient.newInstance("Android");
			HttpGet httpGet = new HttpGet(URI_JOINGAME+this.arguments+"&username=" + TicTacToeHelper.IMEI);
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
