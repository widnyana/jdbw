/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.jdbw.orm;

import java.util.List;

/**
 *
 * @author Martin
 */
public interface AutoIdAssignableObjectStorage {
    
    <U, T extends Identifiable<U> & Modifiable> T newObject(Class<T> type);
    
    <U, T extends Identifiable<U> & Modifiable> List<T> newObjects(final Class<T> type, int numberOfObjects);
}
