package com.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.android.project.TicTacToeGenericActivity;
import com.android.project.helper.TicTacToeHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.telephony.TelephonyManager;

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
