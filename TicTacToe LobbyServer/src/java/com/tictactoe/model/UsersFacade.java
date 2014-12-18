/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tictactoe.model;

import com.tictactoe.model.jpa.Users;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author davidsoendoro
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
/**
 * Inserts player in database
 * @param name:player's unique ID
 */
    public void insertUser(String name) {
        TypedQuery<Users> query = em.createNamedQuery(
                "Users.findByUsername", Users.class)
                .setParameter("username", name);
        try{
            query.getSingleResult();
        }
        catch(NoResultException r){
        Users user = new Users(null, name, 0, 0, 0);
        em.persist(user);
        em.flush();
    }
    }
    /**
     * To get score of a player
     * @param name: player's unique ID
     * @return User object
     */
    public Users getScore(String name) {

        TypedQuery<Users> query = em.createNamedQuery(
                "Users.findByUsername", Users.class)
                .setParameter("username", name);
        Users u = query.getSingleResult();

        return u;
    }

    /**
     * Updates score of the player in the database
     * @param name: player's unique ID
     * @param update: 1 for win, -1 for lost, 0 for draw
     * @return id of row edited
     */
    public int updateScore(String name, int update) {
        
        switch (update) {
            case 1: {
                TypedQuery<Users> query = em.createNamedQuery(
                        "Users.findByUsername", Users.class)
                        .setParameter("username", name);
                Users u = query.getSingleResult();
                if (u != null) {
                    u.setWin(u.getWin() + 1);
                    em.persist(u);
                    return u.getId();
                } else {
                    return -1;
                }
            }
            case -1: {

                TypedQuery<Users> query = em.createNamedQuery(
                        "Users.findByUsername", Users.class)
                        .setParameter("username", name);
                Users u = query.getSingleResult();
                if (u != null) {
                    u.setLose(u.getLose() + 1);
                    em.persist(u);
                    return u.getId();
                } else {
                    return -1;
                }

            }
            case 0: {

                TypedQuery<Users> query = em.createNamedQuery(
                        "Users.findByUsername", Users.class)
                        .setParameter("username", name);
                Users u = query.getSingleResult();
                if (u != null) {
                    u.setDraw(u.getDraw() + 1);
                    em.persist(u);
                    return u.getId();
                } else {
                    return -1;
                }

            }

        }
        return -1;
    }
}
