/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.edu.icm.comac.vis.server.service;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
public class UnknownNodeException extends Exception {

    public UnknownNodeException() {
    }

    public UnknownNodeException(String message) {
        super(message);
    }
    
}
