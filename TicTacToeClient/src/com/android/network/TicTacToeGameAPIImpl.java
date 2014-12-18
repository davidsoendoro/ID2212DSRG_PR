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
import com.android.project.helper.TicTacToeHelper;
import com.android.project.model.T3Protocol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

/**
 * TicTacToeGameAPIImpl is an implementation of TicTacToeGameAPI interface
 * to handle Client - Game Server networking functions of TicTacToe game. 
 * It encapsulates the thread-based network functions into a simple function.
 * @author davidsoendoro-rohitgoyal
 *
 */
public class TicTacToeGameAPIImpl implements TicTacToeGameAPI {

	private String ip;
	private int port;
	private Socket socket;

	private TicTacToeGenericActivity activity;
	private Runnable callback;
	private String result;
	
	private boolean isCalling = false;
	
	/**
	 * Constructor for network game functions
	 * @param activity
	 * @param ip
	 * @param port
	 */
	public TicTacToeGameAPIImpl(TicTacToeGenericActivity activity, final String ip, final int port) {
		this.activity = activity;
		this.ip = ip;
		this.port = port;
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
	private void setResult(String result) {
		this.result = result;
	}

	/**
	 * Get the socket of the network function
	 * @return
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Create a new game vs AI
	 */
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

	/**
	 * Create a new game, if success then wait for contender
	 */
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

	/**
	 * If a game is open, join that game, if no game is opened then return error
	 */
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

	/**
	 * Cancel to Create a game
	 */
	@Override
	public void cancelGame() {
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CANCELGAME);
		connectionThread.start();
	}

	/**
	 * Wait for peer to start a new game
	 */
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

	/**
	 * Wait for opponent move, display indeterminate progress dialog
	 */
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

	/**
	 * Make a move to position
	 * @param position
	 */
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

	/**
	 * Clear the board and start a new game
	 */
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

	/**
	 * ConnectionThread is the thread that is used to run the networking function
	 * itself after it receives command from the TicTacToeGameAPIImpl class.
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
			if(socket == null) {
				try {
					socket = new Socket(ip, port);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}		
			}
			if (command == TicTacToeHelper.COMMAND_CREATESINGLEGAME) {
				createSingleGameRequest();
			}
			else if (command == TicTacToeHelper.COMMAND_CREATEGAME) {
				createGameRequest();
			}
			else if (command == TicTacToeHelper.COMMAND_JOINGAME) {
				joinGameRequest();
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

		/**
		 * Logic implementation of waitForOpponentMove 
		 */
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

		/**
		 * Logic implementation of cancelGame
		 */
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

		/**
		 * Logic implementation of waitForNewGame
		 */
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

		/**
		 * Logic implementation of createGame
		 */
		private void createGameRequest() {
			String str;
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("createGame");
				JSONObject body = new JSONObject();
				body.put("GameId", this.arguments);
				body.put("username", TicTacToeHelper.IMEI);
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

		/**
		 * Logic implementation of createSingleGame
		 */
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

		/**
		 * Logic implementation of joinGame
		 */
		private void joinGameRequest() {
			String str;
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("joinGame");
				JSONObject body = new JSONObject();
				body.put("GameId", this.arguments);
				body.put("username", TicTacToeHelper.IMEI);
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

		/**
		 * Logic implementation of makeMove
		 */
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

		/**
		 * Logic implementation of resetGame
		 */
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
