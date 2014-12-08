/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoe.gameserver.handler;

/**
 *
 * @author davidsoendoro
 */
public class GenericHandler extends Thread {
    
    private final boolean isDebug = true;
    private final boolean isUsingName = true;
    
    protected void writeOutput(String output) {
        String printLine;
        if(isUsingName) {
            printLine = this.getName() + ": " + output;
        }
        else {
            printLine = output;
        }
        if(isDebug) {
            System.out.println(printLine);
        }
    }
    
}
