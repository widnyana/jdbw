/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.jdbw.orm;

import java.util.concurrent.Executor;

/**
 *
 * @author Martin
 */
public interface ExternalObjectStorage extends ObjectStorage {
    
    public static enum CachePolicy {
        SHALLOW_GET,
        SHALLOW_AND_DEEP_GET,
        DEEP_GET
    }
        
    <U, T extends Identifiable<U>> T get(Class<T> type, U key, CachePolicy searchPolicy);
    
    void refresh();

    void refresh(Executor executor);

    <U, T extends Identifiable<U>> void refresh(T... objects);

    <U, T extends Identifiable<U>> void refresh(Class<T> objectType, U... keys);
}
