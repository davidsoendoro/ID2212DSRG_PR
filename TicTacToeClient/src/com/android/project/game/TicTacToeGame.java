/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.android.project.game;

import com.google.gson.JsonObject;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author davidsoendoro
 */
public class TicTacToeGame {
    private int id;
    private int arr[][] = 
	    {{0,0,0},{0,0,0},{0,0,0}};	// array which stores the movements made.
    int game_mode;	// default 0 : h Vs h ; 1 : h Vs Comp
    int player = 2;	// sets the player no. to 2 by default.
    int count = 0;	
    int turn = 1; //= 1 for player 1's turn, = 2 for player 2's turn
    int map_arr[][] = 
            {{1,1,1},{1,1,1},{1,1,1}};	// friend and enemy map initialization.

    int analysis_arr[][] = 
            {{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0}};	// analysis_arr
    
    // score initialized to 0.
    int score_player_1 = 0;
    int score_player_2 = 0;
    
    private int restartNum = 0;
    private boolean isP1Restarted = false;
    private boolean isP2Restarted = false;
    
    private Socket player1Socket;
    private Socket player2Socket;
    private String message = "";

    public TicTacToeGame() {
        this.id = 0;
        this.game_mode = 1;
    }

    public TicTacToeGame(int id, Socket socket) {
        this.id = id;
        this.game_mode = 0;
        this.player1Socket = socket;
    }

    public int getId() {
        return id;
    }

    public int getTurn() {
        return turn;
    }
    
    public int getGame_mode() {
        return game_mode;
    }
    
    public Socket getPlayer1Socket() {
        return player1Socket;
    }

    public void setPlayer1Socket(Socket player1Socket) {
        this.player1Socket = player1Socket;
    }

    public Socket getPlayer2Socket() {
        return player2Socket;
    }

    public void setPlayer2Socket(Socket player2Socket) {
        this.player2Socket = player2Socket;
    }
    
    public String getStringRepresentation() {
        JsonObject jsonObject = new JsonObject();
        
        // Define Position
        String position = "";
        for(int j = 0; j < arr.length; j++) {
            for(int i = 0; i < arr[j].length; i++) {
                position += arr[i][j];
                if(i < arr.length - 1)
                    position += ",";
            }
            if(j < arr[j].length - 1)
                position += "/";
        }
        
        // Define Scores

        jsonObject.addProperty("position", position);
        jsonObject.addProperty("scoreP1", score_player_1);
        jsonObject.addProperty("scoreP2", score_player_2);
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("turn", turn);

        return jsonObject.toString();
    }

    public void processParameter(String inputParameters) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        String[] parametersSplit = inputParameters.split("&");
        
        for(String parameter : parametersSplit) {
            String[] keyValue = parameter.split("=");
            if(keyValue.length >= 2) {
                parameters.put(keyValue[0], keyValue[1]);                
            }
            else if(keyValue.length >= 1){
                parameters.put(keyValue[0], "");
            }
        }
    }
    
    public void makeMove(String position, int player) {
        System.out.println(player + " vs " + this.turn);
        if(player == turn) {
            this.player = player;
            afterMove(position);
        }
        else {
            message = "Not your turn!";
        }
    }
    
    public void afterMove(String position) {
    	int pos;
    	boolean result;
  
    	pos = (int) position.charAt(0) - 48;		// char to integer conversion.
	
    	// set the values in the array according to the player number.
    	if (player == 1) {
            if (pos < 4)				
    		arr[0][pos - 1] = 1;
            else if (pos < 7) 
    		arr[1][(pos - 1) % 3] = 1;
            else if (pos < 10)
    		arr[2][(pos - 1) % 3] = 1;
            }
    	else {
            if (pos < 4)				
    		arr[0][pos - 1] = 2;
            else if (pos < 7) 
    		arr[1][(pos - 1) % 3] = 2;
            else if (pos < 10)
    		arr[2][(pos - 1) % 3] = 2;
    	}
    	
    	// Check for the game result.
    	result = result_check(player);
    		
    	// Result check section.
    	if (result == true) {
            // check for the player number.
            if (player == 1) {
                score_player_1 += 1;
                if (game_mode == 0) {
                    message = "Congrats player 1 wins !!";
                }
                else {
                    message = "Congrats You have won !!";
                }
            }
            else {
                score_player_2 += 1;
                if (game_mode == 0) {	// human vs human  
                    message = "Congrats player 2 wins !!";
                }
                else {	// human vs computer
                    message = "Computer Wins !!";
                }
            }
    	}
    	else if ((result == false) && arr_isFull()) {
            message = "    Game Draw !    "; // leave the space, or else dialog becomes cramped.
    	}
        else {
            message = "";
            // Next Player select section.
            if ((game_mode == 1) && (player == 1) && (result == false)) {  // player 2 : next is computer (player 1)'s chance.
                // CompGame - plays the computer's chance.
                CompGame();
                // Check for the game result.
                result = result_check(player);

                // Result check section.
                if (result == true) {
                    // check for the player number.
                    if (player == 1) {
                        score_player_1 += 1;
                        if (game_mode == 0) {
                            message = "Congrats 1 wins !!";
                        }
                        else {  // human vs computer
                            message = "Congrats You have won !!";
                        }
                    }
                    else {
                        score_player_2 += 1;
                        if (game_mode == 0) {	// human vs human  
                            message = "Congrats 2 wins !!";
                        }
                        else {	
                            message = "Computer Wins !!";
                        }
                    }
                }
                else if ((result == false) && arr_isFull()) {
                    message = "    Game Draw !    "; // leave the space, or else dialog becomes cramped.
                }
            }
            else { 
                turn = (turn % 2) + 1;
            } // continue game.
        }    	
    }
    
    /**
     * Check the array 'arr' and returns the result.
     * @return True if array is full.
     */
    public boolean arr_isFull () {
    	for (int i = 0; i < 3; i++)
    		for (int j = 0; j < 3; j++)
    			if (arr[i][j] == 0)
    				return false;				
    	return true;
    }

    /**
     * Checks the result after each move.
     * @param player_local : the player number who has played the move.
     * @return True is any player has won.
     */
    public boolean result_check(int player_local) {
    	boolean win = true;
    	int k = 0;
    	
    	// check for horizontal condition only.
    	for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (arr[i][j] != player_local) {		// check with player number.
                    win = false;
                    break;
    		}
            } // column loop.
            if (win == true) {
		return true;
            }
            win = true;
    	} // row loop.
    	
    	win = true;			// resetting win to true.
    	
    	// checking for vertical condition only.
    	for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (arr[j][i] != player_local) {
                    win = false;
                    break;
    		}
            } // column loop.
            if (win == true) {
                return true;
            }
            win = true;
    	} // row loop.
    	
    	win = true;			// reset win to true.
    	
    	// check for diagonal condition 1.
    	for (int i = 0; i < 3; i++)
            if (arr[i][k++] != player_local) {
                win = false;
    		break;
            }

        if (win == true) {
            return true;
        }
    	
        k = 2;
        win = true;			// reset win to true;
    	
        // check for diagonal condition 2.
        for (int i = 0; i < 3; i++)
            if (arr[i][k--] != player_local) {
                win = false;
                break;
            }
    	
        if (win == true) {
            return true;
        }
    	
        return false;
    }
    
    /**
     * Master function for the computer's play (AI).
     */
    public void CompGame() {
    	player = 2;
    	count++;
    	analysis_array();
    	if (easy_move_win() == true) {
        } 
        else if (easy_move_block() == true) {
        } 
        else {
            f_e_map();
            best_move();
    	}    	
    }

    public int incrementCount(){
    	count++;
    	return count;
    }
    /**
     * best move calculation : the f_e_map is traversed to see the highest numbered
     * (x, y) position and the move is made.
     */
    public String best_move() {
    	int highest = 0, k = 0;	// k - increment the x_pos, y_pos.
    	int pos[][] = {{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0}};
    	int random_index = 0;	// stores the random index number.
    	int x = 0, y = 0;		// compatibility with comp_play (int, int)
    	
    	// calculate the highest score in the map_arr.
    	for (int i = 0; i < 3; i++)
    		for (int j = 0; j < 3; j++)
    			if (map_arr[i][j] > highest)
    				highest = map_arr[i][j];
    	
    	// traverse map_arr and store all the highest score indices (x, y) in pos[][].
    	for(int i = 0; i < 3; i++)
    		for (int j = 0; j < 3; j++)
    			if (map_arr[i][j] == highest) {
    				pos[k][0] = i;
    				pos[k][1] = j;
    				k++;
    			}
    	
    	// get a random index ( <= k ).
    	random_index = ((int) (Math.random() * 10)) % (k);
    	x = pos[random_index][0];
    	y = pos[random_index][1];			
    	
        arr[x][y] = player;
    	return x + "," + y;
    }

    /**
     * Creates a friend and enemy map, based on all available moves
     * and the current position of the game.
     * 
     * Searches for (1, 0) combination in analysis_array and then increment
     * the corresponding row/col/diagonal in map_arr by 1. Also, the elements
     * in map_arr with value = 0, are not changed.
     * 
     */
    public void f_e_map() {
    	int k = 0;	// for diagonal traversal.
    	
    	// reset map_arr to all 1's every time function is called.
    	for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                map_arr[i][j] = 1;
            }
        }
    	
    	// search for existing moves and mark 0 in map_arr, if found in arr.
    	for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if ((arr[i][j] == 1) || (arr[i][j] == 2)) {
                        map_arr[i][j] = 0;
                }
            }
        }

    	for (int i = 0; i < 8; i++) {
            if (((analysis_arr[i][0] == 1) && (analysis_arr[i][1] == 0)) || ((analysis_arr[i][0] == 0) && (analysis_arr[i][1] == 1))) {
                if (i < 3) { 
                    for (int j = 0; j < 3; j++)
                        if (map_arr[i][j] != 0)
                            map_arr[i][j] += 1;
                }
                else if (i < 6) {
                    for (int j = 0; j < 3; j++)
                        if (map_arr[j][i - 3] != 0)
                            map_arr[j][i - 3] += 1;
                }
                else if (i == 6) {
                    k = 0;
                    for (int m = 0; m < 3; m++) {
                        if (map_arr[m][k] != 0)
                            map_arr[m][k] += 1;
                        k++;
                    }
                }
                else if (i == 7) {
                    k = 2;
                    for (int m = 0; m < 3; m++) {
                        if (map_arr[m][k] != 0)
                            map_arr[m][k] += 1;
                        k--;
                    }
                }
            }
    	}
    }
    
    /**
     * Easy move block function : searches the analysis_arr for (0, 2) combination
     * and makes the move if found, returning a true value.
     * 
     * @return True if an easy Block Move is available.
     */
    public boolean easy_move_block () {
    	boolean flag = false;		// temporary flag to indicate a (0, 2) find.
    	int i, k = 0;		// k used for diagonal search.
    	// search analysis_arr for (0, 2) combination.
    	for (i = 0; i < 8; i++) {
            if ((analysis_arr[i][0] == 0) && (analysis_arr[i][1] == 2)) {
                flag = true;
                break;
            }
        }
    	
    	if (flag == true) {
            // when position < 3, it is one of the 3 rows.
            if (i < 3)	{
                // search for the vacant position
                for (int j = 0; j < 3; j++)
                    if (arr[i][j] == 0) {
                        arr[i][j] = player;
                        return true;
                    }
            }
            else if (i < 6) {
                for (int j = 0; j < 3; j++)
                    if (arr[j][i - 3] == 0) {
                        arr[j][i-3] = player;
                        return true;
                    }
            }
            else if (i == 6) {
                for (int j = 0; j < 3; j++) {
                    if (arr[j][k] == 0) {
                        arr[j][k] = player;
                        return true;
                    }
                    k++;
                }
            }
            else if (i == 7) {
                k = 2;
                for (int j = 0; j < 3; j++) {
                    if (arr[j][k] == 0) {
                        arr[j][k] = player;
                        return true;
                    }
                    k--;
                }
            }
    	}
    	return false;	// false if easy move win is NOT available.
    }

    /**
     * Easy move win function : searches the analysis_arr for (2,0) combination
     * and makes the move if found, returning a true value.
     * @return True if an easy Win Move is available.
     */
    public boolean easy_move_win () {
    	boolean flag = false;		// temporary flag to indicate a (2,0) find.
    	int i, k = 0;		// k used for diagonal search.
    	// search analysis_arr for (2,0) combination.
    	for (i = 0; i < 8; i++) {
            if ((analysis_arr[i][0] == 2) && (analysis_arr[i][1] == 0)) {
                flag = true;
                break;
            }
        }
    	
    	if (flag) {
            // when position < 3, it is one of the 3 rows.
            if (i < 3)	{
                // search for the vacant position
                for (int j = 0; j < 3; j++)
                    if (arr[i][j] == 0) {
                        arr[i][j] = player;
                        return true;
                    }
            }
            else if (i < 6) {
                for (int j = 0; j < 3; j++)
                    if (arr[j][i - 3] == 0) {
                        arr[j][i - 3] = player;
                        return true;
                    }
            }
            else if (i == 6) {
                for (int j = 0; j < 3; j++) {
                    if (arr[j][k] == 0) {
                        arr[j][k] = player;
                        return true;
                    }
                    k++;
                }
            }
            else if (i == 7) {
                k = 2;
                for (int j = 0; j < 3; j++) {
                    if (arr[j][k] == 0) {
                        arr[j][k] = player;
                        return true;
                    }
                    k--;
                }
            }
    	}
    	return false;	// false if easy move win is NOT available.
    }
    
    
	
    
    /**
     * Function to set the analysis array.
     * The analysis array stores the count of Friendly Positions and the Enemy Positions in an 
     * 8 x 2 array. The first 3 rows refer to the 3 rows in the original 'arr' array. The next 3
     * refers to the 3 columns of the 'arr' and the last 2 rows of the analysis array refers
     * to the 2 diagonals in 'arr'. The original array 'arr' is traversed 3 times and then 
     * the values of the analysis array are incremented when and if an enemy or friend is found.
     */
     /*
      *		  		  F	  E
      * 			---------
      * 		R1	| 0	| 0	|
      * 		R2	| 0	| 0	|
      * 		R3	| 0	| 0	|
      * 		C1	| 0	| 0	|
      * 		C2	| 0	| 0	|
      * 		C3	| 0	| 0	|
      * 		D1	| 0	| 0	|
      * 		D2	| 0	| 0	|
      * 			---------	
      */
    public void analysis_array() {
    	
    	// initialize to zero every time this function is called.
    	for (int i = 0; i < 8; i++)
    		analysis_arr[i][0] = analysis_arr[i][1] = 0;
    	
    	// row-wise traversal and increment the value.
    	for (int i = 0; i < 3; i++)
    		for (int j = 0; j < 3; j++)
    			if (arr[i][j] == 1) // 1 = player 1 : computer
    				analysis_arr[i][0] += 1;
    			else if(arr[i][j] == 2) // 2 = player 2 : human
    				analysis_arr[i][1] += 1;
    	
    	
    	
    	// column-wise traversal and increment the value.
    	for (int i = 0; i < 3; i++)
    		for (int j = 0; j < 3; j++)
    			if (arr[j][i] == 1) 			// 1 = player 1
    				analysis_arr[i + 3][0] += 1;
    			else if(arr[j][i] == 2) 		// 2 = player 2, i + 3 to change index to refer to column.
    				analysis_arr[i + 3][1] += 1;
    	
    	
    	// diagonal 1 traversal.
    	int k = 0;
    	for (int i = 0; i < 3; i++) {
    		if (arr[i][k] == 1)
    			analysis_arr[6][0] +=1;
    		else if (arr[i][k] == 2)
    			analysis_arr[6][1] +=1;
    		k++;
    	}
    	
    	// diagonal 2 traversal.
    	// --> reset k to point to the 1st row, and last(3rd) element.
    	k = 2;						
    	for (int i = 0; i < 3; i++) {
    		if (arr[i][k] == 1)
    			analysis_arr[7][0] +=1;
    		else if (arr[i][k] == 2)
    			analysis_arr[7][1] +=1;
    		k--;
    	}
    		
    	// ------ end of analysis array initialization ------------- //
    }

    public void reset(int caller) {
        count = 0;
        turn = 1;
        message = "";
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                arr[i][j] = 0;
                map_arr[i][j] = 1;
            }
        }
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 2; j++) {
                analysis_arr[i][j] = 0;
            }
        }
        
        System.out.println("RestartNum: " + restartNum);
        if((restartNum % 2) == 0) {
            if(game_mode == 0) {
                turn = 2;
            }
            else {
                CompGame();
            }
        }
        if(caller == 1) {
            isP1Restarted = true;
        }
        else if(caller == 2) {
            isP2Restarted = true;
        }
        if(game_mode == 1) {
            isP2Restarted = true;
        }
        if(isP1Restarted && isP2Restarted) {
            isP1Restarted = false;
            isP2Restarted = false;
            restartNum++;
        }
    }

}
