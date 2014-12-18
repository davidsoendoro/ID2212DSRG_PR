/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tictactoe.model;

import com.tictactoe.model.jpa.Game;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author davidsoendoro
 */
@Stateless
public class GameFacade extends AbstractFacade<Game> {

    @PersistenceContext(unitName = "TicTacToe_LobbyServerPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public GameFacade() {
        super(Game.class);
    }
    /**
     * List all valid games in the database
     * @return List of Game objects
     */
    public List getValidGames() {
        return em.createNamedQuery("Game.findByCount").setParameter("count", 1).getResultList();
    }
    /**
     * Inserts game into database
     * @param name: Game name
     * @return id of the row updated
     */
    public int insertGame(String name) {
        Game g = new Game(null, name, 1);
        em.persist(g);
        em.flush();
        System.out.println(g.getId());
        return g.getId();
    }
    /**
    * To join existing valid game
    * @param name: Game name to be joined
    * @return id of the game joined
    */
    public int joinGame(String name) {
        TypedQuery<Game> query = em.createNamedQuery(
                "Game.findByName", Game.class)
                .setParameter("name", name);
        Game g = query.getSingleResult();
        if (g != null) {
            g.setCount(2);
            em.persist(g);
            return g.getId();
        } else {
            return -1;
        }
    }
}
