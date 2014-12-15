/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.android.network.handler;

import com.android.project.game.TicTacToeGame;
import com.android.project.model.T3Protocol;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author davidsoendoro
 */
public class TictactoeHandler extends GenericHandler {

    private final Socket socket;
    private TicTacToeGame tictactoeGame;
    private boolean isEnded;
    private PrintWriter printWriter;
    private final int DISCONNECT_TIME = 3;
    private int disconnectCounter;
    
    public TictactoeHandler(Socket socket) {
        this.socket = socket;
        this.initialize();
    }
    
    public TictactoeHandler(Socket socket, String threadName) {
        this.socket = socket;
        this.initialize();
        this.setName(threadName);
    }
    
    private void initialize() {
        this.tictactoeGame = null;
        this.isEnded = false;
        this.disconnectCounter = 0;        
    }
    
    @Override
    public void run() {
        readString();            
    }
    
    private void readString() {
        try {
            int i = 0;
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
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
                    
                    if(tictactoeGame.getOpponentSocket() != null)
                        opponentDisconnected(tictactoeGame.getOpponentSocket());                        
                    
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

//                executeAPI(completeString, parametersString);
                executeAPI(requestObject);
            }
            
            writeOutput("Thread is closing...");
            
            // close the socket and wait for another connection
            if(socket != null) {
                socket.close();               
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
            printWriter = new PrintWriter(socket.getOutputStream());
            
            // Construct header
            String header = "200 OK";
            
            // Construct body
            String body = this.tictactoeGame.getStringRepresentation();
            
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
            printWriter = new PrintWriter(socket.getOutputStream());
            
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
                body = this.tictactoeGame.getStringRepresentation();
            
                // communicate to the other socket
                printWriter = new PrintWriter(
                        this.tictactoeGame.getOpponentSocket().getOutputStream());

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
            printWriter = new PrintWriter(socket.getOutputStream());
                        
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
            printWriter = new PrintWriter(socket.getOutputStream());
            
            // Construct header
            T3Protocol protocol = new T3Protocol();
            protocol.setRequest("NewSingleGame");
            // Construct body
            protocol.setBody(new JSONObject(this.tictactoeGame.getStringRepresentation()));
            
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
            printWriter = new PrintWriter(socket.getOutputStream());
            
            // Construct header
            T3Protocol protocol = new T3Protocol();
            protocol.setRequest("MakeMove");
            // Construct body
            protocol.setBody(new JSONObject(this.tictactoeGame.getStringRepresentation()));
            
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
            printWriter = new PrintWriter(socket.getOutputStream());
            
            // Construct header
            T3Protocol protocol = new T3Protocol();
            protocol.setRequest("ResetGame");
            // Construct body
            protocol.setBody(new JSONObject(this.tictactoeGame.getStringRepresentation()));
            
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
        if(this.tictactoeGame == null) {
            this.tictactoeGame = new TicTacToeGame();
            this.tictactoeGame.setOpponentSocket(socket);
        }
        else {
            // New TicTacToeGame
//            this.tictactoeGame.newWord();
        }
    }

    private void endGame() {
        // EndGame
        isEnded = true;
    }
    
    private boolean joinGame() {
        // Construct new HangmanGame
        if(this.tictactoeGame == null) {
            // No Vacant Game to join
            writeOutput("No Vacant Game");
            return false;
        }
        else {
            // Join success
            return true;
        } 
    }

    private void newSingleGame() {
        // Construct new HangmanGame
        if(this.tictactoeGame == null) {
            this.tictactoeGame = new TicTacToeGame();
        }
        else {
            // New TicTacToeGame
//            this.tictactoeGame.newWord();
        }
    }

    private String makeMove(JsonObject body) {
        JsonElement element = body.get("position");
        String position = element.getAsString();
        String retVal = this.tictactoeGame.makeMove(position);
        return retVal;
    }

    private void resetGame() {
        this.tictactoeGame.reset();
    }

}
