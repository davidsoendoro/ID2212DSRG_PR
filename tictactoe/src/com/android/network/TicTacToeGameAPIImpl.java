package com.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.android.project.TicTacToeGenericActivity;
import com.android.project.helper.TicTacToeHelper;

import android.app.ProgressDialog;

public class TicTacToeGameAPIImpl implements TicTacToeGameAPI {

	private String ip;
	private int port;
	private Socket socket;

	private TicTacToeGenericActivity activity;
	private Runnable callback;
	private String result;
	
	private boolean isCalling = false;
	
	public TicTacToeGameAPIImpl(TicTacToeGenericActivity activity, String ip, int port) {
		this.activity = activity;
		this.ip = ip;
		this.port = port;
		
		try {
			socket = new Socket(ip, port);
//			socket.setSoTimeout(TIMEOUT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TicTacToeGenericActivity getActivity() {
		return activity;
	}

	public void setActivity(TicTacToeGenericActivity activity) {
		this.activity = activity;
	}

	public void setCallback(Runnable callback) {
		this.callback = callback;
	}

	public String getResult() {
		return result;
	}

	private void setResult(String result) {
		this.result = result;
	}
	
	@Override
	public void createGame() {
		while(isCalling);
		isCalling = true;
		
		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Creating Game", "Now Creating..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CREATEGAME);
		connectionThread.start();
	}

	@Override
	public void joinGame() {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Joining Game", "Now joining..."));

		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_JOINGAME);
		connectionThread.start();
	}

	@Override
	public void startGame() {
		while(isCalling);
		isCalling = true;
		
		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Start Game", "Now Starting..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_STARTGAME);
		connectionThread.start();
	}

	@Override
	public void cancelGame() {
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CANCELGAME);
		connectionThread.start();
	}

	@Override
	public void waitForNewGame() {
		while(isCalling);
		isCalling = true;

		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_WAITFORNEWGAME);
		connectionThread.start();
	}

	@Override
	public void preventDisconnection() {
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_PREVENTDISCONNECTION);
		connectionThread.start();
	}

	private class ConnectionThread extends Thread {
		
		private int command;
		
		public void setCommand(int command) {
			this.command = command;
		}

		@Override
		public void run() {
			if (command == TicTacToeHelper.COMMAND_CREATEGAME) {
				createGameRequest();
			}
			else if (command == TicTacToeHelper.COMMAND_JOINGAME) {
				joinGameRequest();
			}
			else if (command == TicTacToeHelper.COMMAND_STARTGAME) {
				
			}
			else if (command == TicTacToeHelper.COMMAND_CANCELGAME) {
				cancelGameRequest();
			}
			else if (command == TicTacToeHelper.COMMAND_WAITFORNEWGAME) {
				waitForNewGameResponse();
			}
			else if (command == TicTacToeHelper.COMMAND_PREVENTDISCONNECTION) {
				preventDisconnectionResponse();
			}
		}

		private void cancelGameRequest() {
			try {		
				socket.close();
				setResult(command + " - " + "cancelGame");
				
				socket = new Socket(ip, port);
				
                getActivity().runOnUiThread(callback);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
		}

		private void waitForNewGameResponse() {
			String str;
			BufferedReader rd;
			try {
				rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            while ((str = rd.readLine()) != null && !str.trim().equals("")) {
	                System.out.println(str);
	            }
	            while ((str = rd.readLine()) != null && !str.trim().equals("")) {
	                System.out.println(str);
                    setResult(command + " - " + "waitForNewGame" + " - " + str);
	                getActivity().runOnUiThread(callback);
	            }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void preventDisconnectionResponse() {
			String str;
			BufferedReader rd;
			try {
				rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            while ((str = rd.readLine()) != null && !str.trim().equals("")) {
	                System.out.println(str);
	            }
	            while ((str = rd.readLine()) != null && !str.trim().equals("")) {
	                System.out.println(str);
                    setResult(command + " - " + "preventDisconnection" + " - " + str);
	                getActivity().runOnUiThread(callback);
	            }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void createGameRequest() {
			String str;
			try {				
				PrintWriter wr = new PrintWriter(socket.getOutputStream());
                wr.println("GET startGame HTTP/1.0");
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                    System.out.println(str);
                }
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                    System.out.println(str);
                    setResult(command + " - " + "startGame" + " - " + str);
                    getActivity().runOnUiThread(callback);
                }
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
		}

		private void joinGameRequest() {
			String str, fullMsg = "";
			try {				
				PrintWriter wr = new PrintWriter(socket.getOutputStream());
                wr.println("GET joinGame HTTP/1.0");
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                	fullMsg += str;
                    System.out.println(str);
                }
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                	fullMsg += str;
                    System.out.println(str);
                    setResult(command + " - " + "joinGame" + " - " + fullMsg);
                    getActivity().runOnUiThread(callback);
                }
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
		}

	}

}
