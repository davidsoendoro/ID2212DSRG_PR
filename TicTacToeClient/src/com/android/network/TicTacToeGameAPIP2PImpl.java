package com.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.project.TicTacToeGenericActivity;
import com.android.project.game.TicTacToeGame;
import com.android.project.helper.TicTacToeHelper;
import com.android.project.model.T3Protocol;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class TicTacToeGameAPIP2PImpl implements TicTacToeGameAPI {

	private int port = 8090;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	
	private TicTacToeGenericActivity activity;
	private Runnable callback;
	private String result = "";
	
	private boolean isCalling = false;

    private TicTacToeGame tictactoeGame;
    private boolean isEnded;
    private PrintWriter printWriter;
    private final int DISCONNECT_TIME = 3;
    private int disconnectCounter;
	
    /**
     * Constructor for server game
     * @param activity
     */
	public TicTacToeGameAPIP2PImpl(TicTacToeGenericActivity activity) {
		this.activity = activity;
		
		try {
			serverSocket = new ServerSocket(0);
			serverSocket.setReuseAddress(true);
//			serverSocket.bind(new InetSocketAddress(port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		return clientSocket;
	}

	/**
	 * Not used for P2P
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
//		getActivity().setDialog(ProgressDialog.show(getActivity(), 
//				"Wait for Opponent", "Now waiting..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CREATEGAME);
		connectionThread.start();
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
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

		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_JOINGAME);
		connectionThread.start();
	}

	/**
	 * Cancel to Create a game or cancel existing game
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

		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_WAITFORNEWGAME);
		connectionThread.start();
	}

	/**
	 * Wait for opponent move, display indeterminate progress dialog
	 */
	@Override
	public void waitForOpponentMove() {
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
						TicTacToeHelper.gameP2p.cancelGame();
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
	 * itself after it receives command from the TicTacToeGameAPIP2PImpl class.
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
			if(this.command == TicTacToeHelper.COMMAND_CREATEGAME) {
				try {
					if(serverSocket == null) {
						serverSocket = new ServerSocket(0);
						serverSocket.setReuseAddress(true);
//						serverSocket.bind(new InetSocketAddress(port));
					}
					clientSocket = serverSocket.accept();
					readString();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(this.command == TicTacToeHelper.COMMAND_MAKEMOVE) {
				serverMakeMoveString(arguments);
			}
			else if(this.command == TicTacToeHelper.COMMAND_RESETGAME) {
				serverResetGameString();
			}
			else if(this.command == TicTacToeHelper.COMMAND_CANCELGAME) {
				opponentDisconnected();

				try {
					clientSocket.close();
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Acting as a server - parsing strings input
		 */
		private void readString() {
	        try {
	            int i = 0;
	            BufferedReader rd = new BufferedReader(new InputStreamReader(
	                clientSocket.getInputStream()));
	            while(!isEnded) {
	                String str, completeString;
	                completeString = "";

	                writeOutput("Waiting for packet - " + ++i);
	                int in = rd.read();
	                if(in == -1) {
	                    while(in == -1 && disconnectCounter < DISCONNECT_TIME) {
	                        in = rd.read();
	                        writeOutput("Try to wait... " + disconnectCounter);
	                        Thread.sleep(1000);
	                        disconnectCounter += 1;
	                    }
	                }
	                
	                completeString += (char) in;
	                
	                if(disconnectCounter >= DISCONNECT_TIME) {
	                    isEnded = true;
	                    
	                    displayOpponentDisconnected();
	                    
	                    writeOutput("USER IS DISCONNECTED!");
	                    break;
	                }
	                disconnectCounter = 0;
	                
	                // GET HEADER
	                while((str = rd.readLine()) != null && !str.trim().equals("")) {
	                    completeString += str + "\n";
	                }
	                
	                JsonParser parser = new JsonParser();
	                JsonObject requestObject = (JsonObject) parser.parse(completeString);
	                   
	                requestObject.get("Request");
	                
	                writeOutput(completeString);
	                writeOutput("===========");

	                executeAPI(requestObject);
	            }
	            
	            writeOutput("Thread is closing...");
	            
	            // close the socket and wait for another connection
	            if(clientSocket != null) {
	                clientSocket.close();               
	                writeOutput("Thread is closed!");
	            }
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
	            if(printWriter != null) {
	                printWriter.close();
	            }
	        }
		}

		/**
		 * Here is where the parsed input command call the method requested
		 * @param requestObject
		 */
	    private void executeAPI(JsonObject requestObject) {
	        JsonElement element = requestObject.get("Request");
	        String str = element.getAsString();
	        if(str.equals("createGame")) {
	            JsonElement bodyElement = requestObject.get("Body");
	            JsonObject body = bodyElement.getAsJsonObject();
	            createGameString(body);
	        }
	        else if(str.contains("cancelGame")) {
	            cancelGameString();
	        }
	        else if(str.equals("MakeMove")) {
	            JsonElement bodyElement = requestObject.get("Body");
	            JsonObject body = bodyElement.getAsJsonObject();
	            makeMoveString(body);
	        }
	        else if(str.equals("ResetGame")) {
	            resetGameString();
	        }
	    }

	    /**
	     * Server action when it receives makeMove from server player
	     * @param position
	     */
	    private void serverMakeMoveString(String position) {
	    	printWriter = null;
            
	        try {
	            // Do the process
	            serverMakeMove(position);
	            
	            // Construct header
	            T3Protocol protocol = new T3Protocol();
	            protocol.setRequest("MakeMove");
	            // Construct body
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.tictactoeGame.
	            		getStringRepresentation()));

	            // communicate with a client via clientSocket
	            writeOutput("TicTacToe Game Mode: " + TicTacToeGameAPIP2PImpl.
	            		this.tictactoeGame.getGame_mode());
	            
                // Return result to player
                printWriter = new PrintWriter(clientSocket.getOutputStream());
        
                printWriter.println(protocol.toString());
                printWriter.println();

                printWriter.flush();

	            // Run callback of MakeMove
	            JSONObject p2pCommand = new JSONObject();
	            p2pCommand.put("Request", "P2PserverMove");
	            p2pCommand.put("Body", TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation());
                setResult(p2pCommand.toString());
                getActivity().runOnUiThread(callback);
	        } catch (IOException e) {
				e.printStackTrace();
	        } catch (JSONException e) {
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
	    }
	    
	    /**
	     * The logic of serverMakeMoveString
	     * @param position
	     */
	    private void serverMakeMove(String position) {
	        TicTacToeGameAPIP2PImpl.this.tictactoeGame.makeMove(position, 1);
	    }

	    /**
	     * Server action when it receives makeMove from client player
	     * @param body
	     */
	    private void makeMoveString(JsonObject body) {
	        printWriter = null;
	            
	        try {
	            // Do the process
	            makeMove(body);
	            
	            // Construct header
	            T3Protocol protocol = new T3Protocol();
	            protocol.setRequest("MakeMove");
	            // Construct body
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.tictactoeGame.
	            		getStringRepresentation()));

	            // communicate with a client via clientSocket
	            writeOutput("TicTacToe Game Mode: " + TicTacToeGameAPIP2PImpl.
	            		this.tictactoeGame.getGame_mode());
	            
	            // TODO Wait for server input
	            
                // Return result to player
                printWriter = new PrintWriter(clientSocket.getOutputStream());
        
                printWriter.println(protocol.toString());
                printWriter.println();

                printWriter.flush();

	            // Run callback of MakeMove
	            JSONObject p2pCommand = new JSONObject();
	            p2pCommand.put("Request", "P2PclientMove");
	            p2pCommand.put("Body", TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation());
                setResult(p2pCommand.toString());
                getActivity().runOnUiThread(callback);
	        } catch (IOException e) {
				e.printStackTrace();
	        } catch (JSONException e) {
				e.printStackTrace();
			}
	    }

	    /**
	     * The logic of makeMoveString
	     * @param position
	     */
	    private void makeMove(JsonObject body) {
	        JsonElement element = body.get("position");
	        String position = element.getAsString();
	        TicTacToeGameAPIP2PImpl.this.tictactoeGame.makeMove(position, 2);
	    }

	    /**
	     * Server action when it receives resetGame from server player
	     * @param body
	     */
	    private void serverResetGameString() {
            try {
    	    	serverResetGame();

    	    	// Run callback of MakeMove
                JSONObject p2pCommand = new JSONObject();
				p2pCommand.put("Request", "P2PresetGame");
	            p2pCommand.put("Body", TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation());
	            setResult(p2pCommand.toString());
	            getActivity().runOnUiThread(callback);
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				isCalling = false;
			}
	    }
	    
	    /**
	     * The logic of serverResetGameString
	     * @param position
	     */
	    private void serverResetGame() {
	    	TicTacToeGameAPIP2PImpl.this.tictactoeGame.reset(1);
	    }
	    
	    /**
	     * Server action when it receives resetGame from client player
	     * @param position
	     */
	    private void resetGameString() {
	        printWriter = null;
	            
	        try {
	            // Do the process
	            resetGame();
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	            
	            // Construct header
	            T3Protocol protocol = new T3Protocol();
	            protocol.setRequest("ResetGame");
	            // Construct body
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation()));
	            
	            printWriter.println(protocol.toString());
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException e) {
				e.printStackTrace();
	        } catch (JSONException e) {
				e.printStackTrace();
			}
	    }

	    /**
	     * The logic of resetGameString
	     * @param position
	     */
	    private void resetGame() {
	    	TicTacToeGameAPIP2PImpl.this.tictactoeGame.reset(2);
	    }

	    /**
	     * Showing the opponent is disconnected for server player
	     */
	    private void displayOpponentDisconnected() {
            // Run callback to move to TicTacToeOnline Activity
            try {
                JSONObject p2pCommand = new JSONObject();
				p2pCommand.put("Request", "P2PcancelGame");
	            p2pCommand.put("Body", TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation());
	            setResult(p2pCommand.toString());
	            getActivity().runOnUiThread(callback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }

	    /**
	     * Showing the opponent is disconnected for client player
	     */
	    private void opponentDisconnected() {
	        printWriter = null;
	            
	        try {
	            // Do the process
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	            
	            // Construct header
	            T3Protocol protocol = new T3Protocol();
	            protocol.setRequest("CancelGame");
	            // Construct body
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation()));
	            
	            printWriter.println(protocol.toString());
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException e) {
	        	e.printStackTrace();
	        } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    /**
	     * Server action when it receives createGame from client player
	     * @param body
	     */
	    private void createGameString(JsonObject body) {
	        printWriter = null;
	        try {
	            // Do the process
	            createGame(body);
	            
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	            
	            // Construct header
	            T3Protocol protocol = new T3Protocol();
	            protocol.setRequest("createGame");
	            // Construct body
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation()));
	            
	            printWriter.println(protocol.toString());
	            printWriter.println();
	            
	            printWriter.flush();
	            
	            // Run callback to move to TicTacToeOnline Activity
	            JSONObject p2pCommand = new JSONObject();
	            p2pCommand.put("Request", "P2PcreateGame");
	            p2pCommand.put("Body", TicTacToeGameAPIP2PImpl.this.
	            		tictactoeGame.getStringRepresentation());
                setResult(p2pCommand.toString());
                getActivity().runOnUiThread(callback);
	        } catch (IOException e) {
				e.printStackTrace();
	        } catch (JSONException e) {
				e.printStackTrace();
			}
	    }
	    
	    /**
	     * The logic of createGameString
	     * @param body
	     */
	    private void createGame(JsonObject body) {
	        int id = body.get("GameId").getAsInt();
	        
	        // Construct new TicTacToeGame
	        if(TicTacToeGameAPIP2PImpl.this.tictactoeGame == null) {
	            TicTacToeGameAPIP2PImpl.this.tictactoeGame = new TicTacToeGame(id, 
	            		clientSocket);
	        }
	    }

	    /**
	     * Server action when it receives cancelGame from client player
	     * @param body
	     */
	    private void cancelGameString() {   
	        printWriter = null;
	            
	        try {
	            // Do the process
	            cancelGame();
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	            
	            // Construct header
	            String header = "200 OK";
	            
	            // Construct body
	            String body = "";
	            
	            printWriter.println(header); // send GET request
	            printWriter.println();
	            printWriter.println(body);
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException e) {
				e.printStackTrace();
	        }
	    }

	    /**
	     * The logic of cancelGameString
	     * @param body
	     */
	    private void cancelGame() {
	    	// Display opponent disconnected
	    	try {
				clientSocket.close();
		    	serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	}

	/**
	 * Write an output
	 * @param string
	 */
	public void writeOutput(String string) {
		System.out.println(string);
	}

}
