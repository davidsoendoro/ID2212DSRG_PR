/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tictactoe.model;

import com.tictactoe.model.jpa.Game;
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

    public int insertGame(String name) {
        Game g = new Game(null, name, 1);
        em.persist(g);
        em.flush();
        System.out.println(g.getId());
        return g.getId();
    }

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
