/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tictactoe.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tictactoe.model.GameFacade;
import com.tictactoe.model.jpa.Game;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author davidsoendoro
 */
@WebServlet(name = "LobbyServlet", urlPatterns = {"/LobbyServlet", "/CreateGame", "/JoinGame"})
public class LobbyServlet extends HttpServlet {
    
    @EJB
    private GameFacade gameFacade;

    @Override
    public void init() throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        if(request.getServletPath().equals("/LobbyServlet")) {
            doLobbyServlet(request, response);
        }
        else if(request.getServletPath().equals("/CreateGame")) {
            doCreateGame(request, response);
        }
        else if(request.getServletPath().equals("/JoinGame")) {
            doJoinGame(request, response);
        }        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void doLobbyServlet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            JsonObject jsonObject = new JsonObject();
            
            List<Game> games = gameFacade.findAll();
            Gson gson = new Gson();
            JsonElement element = gson.toJsonTree(games, 
                    new TypeToken<List<Game>>(){}.getType());
            
            jsonObject.addProperty("Games", element.getAsJsonArray().toString());
            
            out.println(jsonObject.toString());
        }
    }

    private void doCreateGame(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        int gameId = gameFacade.insertGame(request.getParameter("name"));
        
        try (PrintWriter out = response.getWriter()) {
            JsonObject jsonObject = new JsonObject();
            
            jsonObject.addProperty("GameId", gameId);
            
            out.println(jsonObject.toString());
        }
    }

    private void doJoinGame(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int gameId = gameFacade.joinGame(request.getParameter("name"));
        if(gameId > 0){
            try (PrintWriter out = response.getWriter()) {
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("GameId", gameId);

                out.println(jsonObject.toString());
            }
        }
        else{
            
        }
    
    
    }

}
