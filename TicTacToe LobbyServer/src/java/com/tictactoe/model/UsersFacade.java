/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tictactoe.model;

import com.tictactoe.model.jpa.Users;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author rohitgoyal
 */
@Stateless
public class UsersFacade extends AbstractFacade<Users> {
    @PersistenceContext(unitName = "TicTacToe_LobbyServerPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsersFacade() {
        super(Users.class);
    }
    
    public int insertUser(String name) {
        System.out.println("here");
        Users u = new Users(null, name);
        em.persist(u);
        em.flush();
        return u.getId();
    }
    
    public int updateScore(String name,int win, int lose, int draw) {
        TypedQuery<Users> query = em.createNamedQuery(
                "Users.findByName", Users.class)
                .setParameter("name", name);
        Users u = query.getSingleResult();
        if (u != null) {
            u.setWin(win);
            u.setDraw(draw);
            u.setLose(lose);
            em.persist(u);
            return u.getId();
        } else {
            return -1;
        }
    }
}
