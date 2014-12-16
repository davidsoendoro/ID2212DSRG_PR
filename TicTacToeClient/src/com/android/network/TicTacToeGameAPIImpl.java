package com.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.project.TicTacToeGenericActivity;
import com.android.project.TicTacToeOnline;
import com.android.project.helper.TicTacToeHelper;
import com.android.project.model.T3Protocol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

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

	public Socket getSocket() {
		return socket;
	}

	@Override
	public void createSingleGame() {
		while(isCalling);
		isCalling = true;
		
		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Creating Game", "Now Creating..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CREATESINGLEGAME);
		connectionThread.start();
	}

	@Override
	public void createGame(int id) {
		while(isCalling);
		isCalling = true;
		
		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Creating Game", "Now Creating..."));
		
		ConnectionThread connectionThread = new ConnectionThread(""+id);
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CREATEGAME);
		connectionThread.start();
	}

	@Override
	public void joinGame(int id) {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Joining Game", "Now joining..."));

		ConnectionThread connectionThread = new ConnectionThread(""+id);
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

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Wait for Opponent", "Now Waiting..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_WAITFORNEWGAME);
		connectionThread.start();
	}

	@Override
	public void waitForOpponentMove() {
		while(isCalling);
		isCalling = true;

		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setCancelable(true);
		dialog.setTitle("Wait for Opponent");
		dialog.setMessage("Now Waiting...");
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(final DialogInterface dialog) {
				AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

				alert.setTitle("Quit?");
				alert.setMessage("Are you sure you want to quit the game?");
				
				alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface alertDialog, int which) {
						TicTacToeHelper.game.cancelGame();
						getActivity().finish();
					}
				});
				alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface alertDialog, int which) {
						alertDialog.dismiss();
						getActivity().getDialog().show();
					}
				});
				
				alert.create().show();
			}
		});
		getActivity().setDialog(dialog);
		getActivity().getDialog().show();
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_WAITFORMOVE);
		connectionThread.start();
	}

	@Override
	public void preventDisconnection() {
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_PREVENTDISCONNECTION);
		connectionThread.start();
	}

	@Override
	public void makeMove(String position) {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Waiting for opponent move", "Now Waiting..."));
		
		ConnectionThread connectionThread = new ConnectionThread(position);
		connectionThread.setCommand(TicTacToeHelper.COMMAND_MAKEMOVE);
		connectionThread.start();
	}
	
	@Override
	public void resetGame() {
		while(isCalling);
		isCalling = true;

		getActivity().setDialog(ProgressDialog.show(getActivity(), 
				"Resetting Game", "Now Resetting..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_RESETGAME);
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
			if (command == TicTacToeHelper.COMMAND_CREATESINGLEGAME) {
				createSingleGameRequest();
			}
			else if (command == TicTacToeHelper.COMMAND_CREATEGAME) {
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
			else if (command == TicTacToeHelper.COMMAND_WAITFORMOVE) {
				waitForOpponentMoveResponse();
			}
			else if (command == TicTacToeHelper.COMMAND_PREVENTDISCONNECTION) {
				preventDisconnectionResponse();
			}
			else if (command == TicTacToeHelper.COMMAND_MAKEMOVE) {
				try {
					socket.setSoTimeout(10000);
					makeMoveRequest(arguments);
					socket.setSoTimeout(0);
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
			else if (command == TicTacToeHelper.COMMAND_RESETGAME) {
				resetGameRequest();
			}
		}

		private void waitForOpponentMoveResponse() {
			String str;
			BufferedReader rd;
			try {
				rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            while ((str = rd.readLine()) != null && !str.trim().equals("")) {
	                System.out.println(str);
                    setResult(str);
	                getActivity().runOnUiThread(callback);
	            }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				isCalling = false;
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
                    setResult(str);
	                getActivity().runOnUiThread(callback);
	            }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				isCalling = false;
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
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("createGame");
				JSONObject body = new JSONObject();
				body.put("GameId", this.arguments);
				protocol.setBody(body);
				
				PrintWriter wr = new PrintWriter(socket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();

                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                    System.out.println(str);
                    setResult(str);
                    getActivity().runOnUiThread(callback);
                }
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
		}

		private void createSingleGameRequest() {
			String str;
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("NewSingleGame");
				
				PrintWriter wr = new PrintWriter(socket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                    System.out.println(str);
                    setResult(str);
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
			String str;
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("joinGame");
				JSONObject body = new JSONObject();
				body.put("GameId", this.arguments);
				protocol.setBody(body);
				
				PrintWriter wr = new PrintWriter(socket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                    System.out.println(str);
                    setResult(str);
                    getActivity().runOnUiThread(callback);
                }
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
		}

		private void makeMoveRequest(String position) {
			String str;
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("MakeMove");
				
				JSONObject body = new JSONObject();
				body.put("position", position);
				protocol.setBody(body);
				
				PrintWriter wr = new PrintWriter(socket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();
                
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                int i = rd.read();
                if(i != -1) {
                	char c = (char) i;
	                while ((str = c + rd.readLine()) != null && !str.trim().equals("")) {
	                	c = '\0';
	                    System.out.println(str);
	                    setResult(str);
	                    getActivity().runOnUiThread(callback);
	                }
				}
				else {
					// Server is down, display finish game
					try {
		                JSONObject cancelCommand = new JSONObject();
						cancelCommand.put("Request", "ServerUnreachable");
			            setResult(cancelCommand.toString());
			            getActivity().runOnUiThread(callback);
					} catch (JSONException ex) {
						ex.printStackTrace();
					}					
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
		}

		private void resetGameRequest() {
			String str;
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("ResetGame");
								
				PrintWriter wr = new PrintWriter(socket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                    System.out.println(str);
                    setResult(str);
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
