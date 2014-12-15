package com.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.network.handler.TictactoeHandler;
import com.android.project.TicTacToeGenericActivity;
import com.android.project.game.TicTacToeGame;
import com.android.project.helper.TicTacToeHelper;
import com.android.project.model.T3Protocol;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.ProgressDialog;

public class TicTacToeGameAPIP2PImpl implements TicTacToeGameAPI {

	private int port = 8090;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	
	private TicTacToeGenericActivity activity;
	private Runnable callback;
	private String result;
	
	private boolean isCalling = false;

    private TicTacToeGame tictactoeGame;
    private boolean isEnded;
    private PrintWriter printWriter;
    private final int DISCONNECT_TIME = 3;
    private int disconnectCounter;
	
	public TicTacToeGameAPIP2PImpl(TicTacToeGenericActivity activity) {
		this.activity = activity;
		
		try {
			serverSocket = new ServerSocket(port);
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
		return clientSocket;
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
				"Wait for Opponent", "Now waiting..."));
		
		ConnectionThread connectionThread = new ConnectionThread();
		connectionThread.setCommand(TicTacToeHelper.COMMAND_CREATEGAME);
		connectionThread.start();
	}

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
			if(this.command == TicTacToeHelper.COMMAND_CREATEGAME) {
				try {
					clientSocket= serverSocket.accept();
					readString();
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}

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
	                    
	                    if(tictactoeGame.getOpponentSocket() != null) {
	                    	opponentDisconnected(tictactoeGame.getOpponentSocket());                        
	                    }
	                    
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

	    private void executeAPI(JsonObject requestObject) {
	        JsonElement element = requestObject.get("Request");
	        String str = element.getAsString();
	        if(str.contains("startGame")) {
	            startGameString();
	        }
	        else if(str.contains("endGame")) {
	            endGameString();
	        }
	        else if(str.contains("joinGame")) {
	            joinGameString();
	        }
	        else if(str.equals("NewSingleGame")) {
	            newSingleGameString();
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
	     * Handler that will be called if server receives "startGame" message
	     * If it is the start of the game it will initialize hangmanGame
	     * This function will send back score, attempt, and new word to client
	     */
	    private void startGameString() {
	        printWriter = null;
	            
	        try {
	            // Do the process
	            startGame();
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	            
	            // Construct header
	            String header = "200 OK";
	            
	            // Construct body
	            String body = TicTacToeGameAPIP2PImpl.this.tictactoeGame.
	            		getStringRepresentation();
	            
	            printWriter.println(header); // send GET request
	            printWriter.println();
	            printWriter.println(body);
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException ex) {
	            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
	        }
	    }

	    /**
	     * Handler that will be called if server receives "endGame" message
	     * Server will reply with 200 OK and empty body
	     * Server will close the socket and stop the thread
	     */
	    private void endGameString() {        
	        printWriter = null;
	            
	        try {
	            // Do the process
	            endGame();
	                        
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
	        } catch (IOException ex) {
	            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
	        }
	    }

	    private void joinGameString() {
	        printWriter = null;
	            
	        try {
	            // Do the process
	            String header, body;
	            if(joinGame()) {
	                // Construct header
	                header = "200 OK";

	                // Construct body
	                body = TicTacToeGameAPIP2PImpl.this.tictactoeGame.getStringRepresentation();
	            
	                // communicate to the other socket
	                printWriter = new PrintWriter(
	                		TicTacToeGameAPIP2PImpl.this.tictactoeGame.getOpponentSocket().getOutputStream());

	                printWriter.println(header); // send GET request
	                printWriter.println();
	                printWriter.println(body);
	                printWriter.println();

	                printWriter.flush();
	            }
	            else {
	                // Construct header
	                header = "404 Not Found";

	                // Construct body
	                body = "Vacant games not found";
	            }
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	                        
	            printWriter.println(header); // send GET request
	            printWriter.println();
	            printWriter.println(body);
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException ex) {
	            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
	        }
	    }

	    /**
	     * Handler that will be called if server receives "startGame" message
	     * If it is the start of the game it will initialize hangmanGame
	     * This function will send back score, attempt, and new word to client
	     */
	    private void newSingleGameString() {
	        printWriter = null;
	            
	        try {
	            // Do the process
	            newSingleGame();
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	            
	            // Construct header
	            T3Protocol protocol = new T3Protocol();
	            protocol.setRequest("NewSingleGame");
	            // Construct body
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.tictactoeGame.getStringRepresentation()));
	            
	            printWriter.println(protocol.toString());
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException ex) {
	            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
	        } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    private void makeMoveString(JsonObject body) {
	        printWriter = null;
	            
	        try {
	            // Do the process
	            makeMove(body);
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(clientSocket.getOutputStream());
	            
	            // Construct header
	            T3Protocol protocol = new T3Protocol();
	            protocol.setRequest("MakeMove");
	            // Construct body
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.tictactoeGame.getStringRepresentation()));
	            
	            printWriter.println(protocol.toString());
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException ex) {
	            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
	        } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
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
	            protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.tictactoeGame.getStringRepresentation()));
	            
	            printWriter.println(protocol.toString());
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException ex) {
	            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
	        } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    private void opponentDisconnected(Socket playerSocket) {
	        printWriter = null;
	            
	        try {
	            // Do the process
	                        
	            // communicate with a client via clientSocket
	            printWriter = new PrintWriter(playerSocket.getOutputStream());
	            
	            // Construct header
	            String header = "200 OK";
	            
	            // Construct body
	            String body = "Opponent disconnected, you won!";
	            
	            printWriter.println(header); // send GET request
	            printWriter.println();
	            printWriter.println(body);
	            printWriter.println();
	            
	            printWriter.flush();
	        } catch (IOException ex) {
	            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
	        }
	    }

	    private void startGame() {
	        // Construct new HangmanGame
	        if(TicTacToeGameAPIP2PImpl.this.tictactoeGame == null) {
	        	TicTacToeGameAPIP2PImpl.this.tictactoeGame = new TicTacToeGame();
	        	TicTacToeGameAPIP2PImpl.this.tictactoeGame.setOpponentSocket(clientSocket);
	        }
	        else {
	            // New TicTacToeGame
//	            this.tictactoeGame.newWord();
	        }
	    }

	    private void endGame() {
	        // EndGame
	        isEnded = true;
	    }
	    
	    private boolean joinGame() {
	        // Construct new HangmanGame
            return false;
	    }

	    private void newSingleGame() {
	        // Construct new HangmanGame
	        if(TicTacToeGameAPIP2PImpl.this.tictactoeGame == null) {
	        	TicTacToeGameAPIP2PImpl.this.tictactoeGame = new TicTacToeGame();
	        }
	        else {
	            // New TicTacToeGame
//	            this.tictactoeGame.newWord();
	        }
	    }

	    private String makeMove(JsonObject body) {
	        JsonElement element = body.get("position");
	        String position = element.getAsString();
	        String retVal = TicTacToeGameAPIP2PImpl.this.tictactoeGame.makeMove(position);
	        return retVal;
	    }

	    private void resetGame() {
	    	TicTacToeGameAPIP2PImpl.this.tictactoeGame.reset();
	    }

		private void waitForNewGameResponse() {
			String str;
			BufferedReader rd;
			try {
				rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
				rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
				clientSocket= serverSocket.accept();
                // communicate with a client via clientSocket
                printWriter = new PrintWriter(clientSocket.getOutputStream());
                
                // Construct header
                T3Protocol protocol = new T3Protocol();
                protocol.setRequest("NewSingleGame");
                // Construct body
                protocol.setBody(new JSONObject(TicTacToeGameAPIP2PImpl.this.tictactoeGame.getStringRepresentation()));
                
                printWriter.println(protocol.toString());
                printWriter.println();
                
                printWriter.flush();
				
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
			String completeStr = "";
			String str = "";
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("NewSingleGame");
				
				PrintWriter wr = new PrintWriter(clientSocket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while ((str = rd.readLine()) != null && !str.trim().equals("")) {
                    System.out.println(str);
                    completeStr += str;
                }			
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
                setResult(completeStr);
                getActivity().runOnUiThread(callback);	
				isCalling = false;
			}
		}

		private void joinGameRequest() {
			String str, fullMsg = "";
			try {				
				PrintWriter wr = new PrintWriter(clientSocket.getOutputStream());
                wr.println("GET joinGame HTTP/1.0");
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

		private void makeMoveRequest(String position) {
			String str;
			try {				
				// Prepare Message
				T3Protocol protocol = new T3Protocol();
				protocol.setRequest("MakeMove");
				
				JSONObject body = new JSONObject();
				body.put("position", position);
				protocol.setBody(body);
				
				PrintWriter wr = new PrintWriter(clientSocket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
								
				PrintWriter wr = new PrintWriter(clientSocket.getOutputStream());
                wr.println(protocol.toString());
                wr.println();
                wr.flush();
                BufferedReader rd = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

	public void writeOutput(String string) {
		System.out.println(string);
	}

}
