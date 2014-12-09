/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoe.gameserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import tictactoe.gameserver.game.TicTacToeGame;
import tictactoe.gameserver.handler.TictactoeHandler;
import tictactoe.gameserver.helper.SettingsHelper;

/**
 *
 * @author davidsoendoro
 */
public class TicTacToeGameServer {

    private static boolean isListening = true;
    private static ArrayList<Socket> clientSockets;
    private static ServerSocket serverSocket;
    public static ArrayList<TicTacToeGame> vacantGames;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int port = 8080;
        int threadNameCounter = 0;
        clientSockets = new ArrayList<>();
        vacantGames = new ArrayList<>();
        
        InputHandler inputHandler = new InputHandler();
        inputHandler.start();
        
        try {
            serverSocket = new ServerSocket(port);
            while(isListening) {
                try {
                    // wait for a client connection request
                    Socket clientSocket;
                    clientSocket = serverSocket.accept();
                    clientSockets.add(clientSocket);
                    
                    TictactoeHandler handler = new TictactoeHandler(clientSocket, 
                            "Thread-" + ++threadNameCounter);
                    handler.setPriority(handler.getPriority() + 1);
                    handler.start();
                } catch (SocketException ex) {
//                    Logger.getLogger(JavaServer.class.getName()).log(
//                            Level.SEVERE, null, ex);
                    System.out.println("Thread is closed!");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TicTacToeGameServer.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        
        System.out.println("Thank you for running JavaServer! See you soon "
                + "in the future");
    }
    
    private static class InputHandler extends Thread {

        @Override
        public void run() {
            Scanner in = new Scanner(System.in);
            String input;
            
            while(isListening) {
                System.out.print("> ");
                input = in.nextLine();
                
                String[] commands = input.split(" ");
                
                if(commands[0].equals("quit")) {
                    try {
                        isListening = false;
                        serverSocket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(TicTacToeGameServer.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
                else if(commands[0].equals("method")) {
                    if(commands[1].equals("string")) {
                        SettingsHelper.isReadString = true;
                    }
                    else if(commands[1].equals("object")) {
                        SettingsHelper.isReadString = false;
                    }
                    System.out.println("Read method changed!");
                    System.out.println("Value of isReadString: " + SettingsHelper.isReadString);
                }
                else {
                    System.out.println("Command not realized - type 'help' for "
                            + "list of commands");
                }
            }
        }

    }

}
