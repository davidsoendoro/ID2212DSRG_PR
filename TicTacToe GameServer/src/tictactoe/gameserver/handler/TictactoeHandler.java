/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoe.gameserver.handler;

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
import tictactoe.gameserver.TicTacToeGameServer;
import tictactoe.gameserver.game.TicTacToeGame;
import tictactoe.gameserver.helper.SettingsHelper;
import tictactoe.gameserver.model.T3Protocol;

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
    private int player;
    
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
        if(SettingsHelper.isReadString) {
            readString();            
        }
        else {
        }
    }
    
    private void readString() {
        try {
            int i = 0;
            BufferedReader rd=new BufferedReader(new InputStreamReader(
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
                    if(TicTacToeGameServer.vacantGames.contains(tictactoeGame)) {
                        TicTacToeGameServer.vacantGames.remove(tictactoeGame);
                    }
                    
                    if(socket.equals(tictactoeGame.getPlayer1Socket())) {
                        if(tictactoeGame.getPlayer2Socket() != null)
                            opponentDisconnected(tictactoeGame.getPlayer2Socket());
                    }
                    else {
                        if(tictactoeGame.getPlayer1Socket() != null)
                            opponentDisconnected(tictactoeGame.getPlayer1Socket());                        
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
                
                if(completeString.contains("updateGame")) {
                    // GET BODY
                    while((str = rd.readLine()) != null && !str.trim().equals("")) {
                        completeString += str + "\n";
                    }                
                }
                
                writeOutput(completeString);
                writeOutput("===========");

                executeAPI(requestObject);
            }
            
            writeOutput("Thread is closing...");
            
            // close the socket and wait for another connection
            if(socket != null) {
                socket.close();               
                writeOutput("Thread is closed!");
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(printWriter != null) {
                printWriter.close();
            }
        }
    }

    private void executeAPI(JsonObject requestObject) {
        JsonElement element = requestObject.get("Request");
        String str = element.getAsString();
        if(str.equals("createGame")) {
            JsonElement bodyElement = requestObject.get("Body");
            JsonObject body = bodyElement.getAsJsonObject();
            createGameString(body);
        }
        else if(str.equals("NewSingleGame")) {
            newSingleGameString();
        }
        else if(str.contains("joinGame")) {
            JsonElement bodyElement = requestObject.get("Body");
            JsonObject body = bodyElement.getAsJsonObject();
            joinGameString(body);
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

    private void makeMoveString(JsonObject body) {
        printWriter = null;
            
        try {
            // Do the process
            makeMove(body);
            
            // Construct header
            T3Protocol protocol = new T3Protocol();
            protocol.setRequest("MakeMove");
            // Construct body
            protocol.setBody(this.tictactoeGame.getStringRepresentation());

            // communicate with a client via clientSocket
            writeOutput("TicTacToe Game Mode: " + this.tictactoeGame.getGame_mode());
            
            if(this.tictactoeGame.getGame_mode() == 0) {
                // Send update to opponent
                Socket opponentSocket;
                if(player == 1) {
                    opponentSocket = this.tictactoeGame.getPlayer2Socket();
                }
                else {
                    opponentSocket = this.tictactoeGame.getPlayer1Socket();
                }
                
                // Check if opponentSocket is opened
                if(opponentSocket.isClosed()) {
                    // Send disconnection message to player
                    opponentDisconnected(socket);
                }
                else {
                    // Return result to player
                    printWriter = new PrintWriter(socket.getOutputStream());
            
                    printWriter.println(protocol.toString());
                    printWriter.println();

                    printWriter.flush();
                
                    // Give result to opponent
                    printWriter = new PrintWriter(opponentSocket.getOutputStream());
                    printWriter.println(protocol.toString());
                    printWriter.println();

                    printWriter.flush();
                }                
            }
            else {
                // VS Com
                printWriter = new PrintWriter(socket.getOutputStream());
            
                printWriter.println(protocol.toString());
                printWriter.println();

                printWriter.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void makeMove(JsonObject body) {
        JsonElement element = body.get("position");
        String position = element.getAsString();
        this.tictactoeGame.makeMove(position, player);
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
            protocol.setBody(this.tictactoeGame.getStringRepresentation());
            
            printWriter.println(protocol.toString());
            printWriter.println();
            
            printWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void resetGame() {
        this.tictactoeGame.reset(player);
    }

    private void opponentDisconnected(Socket playerSocket) {
        printWriter = null;
            
        try {
            // Do the process
                        
            // communicate with a client via clientSocket
            printWriter = new PrintWriter(playerSocket.getOutputStream());
            
            // Construct header
            T3Protocol protocol = new T3Protocol();
            protocol.setRequest("CancelGame");
            // Construct body
            protocol.setBody(this.tictactoeGame.getStringRepresentation());
            
            printWriter.println(protocol.toString());
            printWriter.println();
            
            printWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // VS PLAYER

    private void createGameString(JsonObject body) {
        printWriter = null;
        try {
            // Do the process
            createGame(body);
                        
            // communicate with a client via clientSocket
            printWriter = new PrintWriter(socket.getOutputStream());
            
            // Construct header
            T3Protocol protocol = new T3Protocol();
            protocol.setRequest("createGame");
            // Construct body
            protocol.setBody(this.tictactoeGame.getStringRepresentation());
            
            printWriter.println(protocol.toString());
            printWriter.println();
            
            printWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createGame(JsonObject body) {
        int id = body.get("GameId").getAsInt();
        
        // Construct new HangmanGame
        if(this.tictactoeGame == null) {
            this.player = 1;
            this.tictactoeGame = new TicTacToeGame(id, socket);
            this.tictactoeGame.IMEI_1=body.get("username").getAsString();
            
        }
        TicTacToeGameServer.map.put(id, this.tictactoeGame);
    }

    private void joinGameString(JsonObject body) {
        printWriter = null;
            
        try {
            // Do the process
            if(joinGame(body)) {
                // Construct header
                T3Protocol protocol = new T3Protocol();
                protocol.setRequest("joinGame");
                // Construct body
                protocol.setBody(this.tictactoeGame.getStringRepresentation());

                printWriter = new PrintWriter(
                        this.tictactoeGame.getPlayer1Socket().getOutputStream());
                
                printWriter.println(protocol.toString());
                printWriter.println();

                printWriter.flush();
                
                // communicate to the other socket
                printWriter = new PrintWriter(
                        this.tictactoeGame.getPlayer2Socket().getOutputStream());

                printWriter.println(protocol.toString());
                printWriter.println();

                printWriter.flush();
            }
            else {
                // Construct header
                T3Protocol protocol = new T3Protocol();
                protocol.setRequest("joinGameError");
                // Construct body
                protocol.setBody("Error Game not Found!");

                printWriter = new PrintWriter(socket.getOutputStream());
                
                printWriter.println(protocol.toString());
                printWriter.println();

                printWriter.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean joinGame(JsonObject body) {
        int id = body.get("GameId").getAsInt();
        
        if(TicTacToeGameServer.map.containsKey(id)) {
            this.player = 2;
            this.tictactoeGame = TicTacToeGameServer.map.get(id);
            this.tictactoeGame.setPlayer2Socket(socket);
            this.tictactoeGame.IMEI_2=body.get("username").getAsString();
            return true;
        }
        else {
            return false;
        }
    }

    private void cancelGameString() {   
        printWriter = null;
            
        try {
            // Do the process
            cancelGame();
                        
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

    private void cancelGame() {
        TicTacToeGameServer.map.remove(tictactoeGame.getId());
    }
    
    // VS COM
    
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
            protocol.setBody(this.tictactoeGame.getStringRepresentation());
            
            printWriter.println(protocol.toString());
            printWriter.println();
            
            printWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(TictactoeHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void newSingleGame() {
        // Construct new HangmanGame
        if(this.tictactoeGame == null) {
            this.player = 1;
            this.tictactoeGame = new TicTacToeGame();
        }
        else {
            // New TicTacToeGame
//            this.tictactoeGame.newWord();
        }
    }
    
}
