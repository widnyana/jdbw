/*
 * This file is part of jdbw (http://code.google.com/p/jdbw/).
 * 
 * jdbw is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2007-2012 Martin Berglund
 */
package com.googlecode.jdbw.jorm;

import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.SQLDialect;
import com.googlecode.jdbw.util.SQLWorker;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JORMDatabase {
    public static enum SearchPolicy {
        LOCAL_ONLY,
        CHECK_DATABASE_IF_MISSING,
        REFRESH_FIRST
    }
    
    private final DatabaseConnection databaseConnection;
    private final Map<Class<? extends JORMEntity>, Map> datastore;
    private final Map<Class<? extends JORMEntity>, ClassTableMapping> tableMapping;

    public JORMDatabase(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        this.datastore = new HashMap<Class<? extends JORMEntity>, Map>();
        this.tableMapping = new HashMap<Class<? extends JORMEntity>, ClassTableMapping>();
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> ArrayList<T> getAll(Class<T> type) {
        Map<U, T> entityMap = getEntityDataMap(type);
        return new ArrayList<T>(entityMap.values());
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> T get(Class<T> type, U key) {
        return get(type, key, SearchPolicy.CHECK_DATABASE_IF_MISSING);
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> T get(Class<T> type, U key, SearchPolicy searchPolicy) {
        if(searchPolicy == SearchPolicy.REFRESH_FIRST) {
            refresh(type, key);
        }
        Map<U, T> entityMap = getEntityDataMap(type);
        T entity = entityMap.get(key);
        if(entity == null && searchPolicy == SearchPolicy.CHECK_DATABASE_IF_MISSING) {
            return get(type, key, SearchPolicy.REFRESH_FIRST);
        }
        return entity;
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> T newEntity(Class<T> type) throws SQLException {
        return newEntity(type, null);
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> T newEntity(Class<T> type, U id) throws SQLException {
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(sqlDialect.escapeIdentifier(getTableName(type)));
        sb.append(" (");
        sb.append(sqlDialect.escapeIdentifier("id"));
        sb.append(") VALUES(?)");
        U newId = (U)new SQLWorker(databaseConnection.createAutoExecutor()).insert(sb.toString(), id);
        if(newId != null)
            id = newId;
        else if(id == null) {
            throw new IllegalStateException("After inserting row into " + getTableName(type) + ", couldn't "
                    + "figure out what primary key was assigned, your JDBC driver or database server "
                    + "probably don't support the feature to return auto-generated keys or the column "
                    + "isn't set up to auto generate keys");
        }        
        Map<U, T> entityDataMap = getEntityDataMap(type);
        T entity = newEntityProxy(type, id);
        entityDataMap.put(id, entity);
        return entity;
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> T persist(T entity) throws SQLException {
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        EntityProxy.Resolver<U, T> asResolver = (EntityProxy.Resolver<U, T>)entity;
        EntityProxy<U, T> proxy = asResolver.__underlying_proxy();
        Class<T> entityType = proxy.getEntityType();
        
        String[] columnName = getClassTableMapping(entityType).getNonIdColumns();
        if(columnName.length == 0)
            return entity;
        
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(sqlDialect.escapeIdentifier(getTableName(entityType)));
        sb.append(" SET ");
        for(int i = 0; i < columnName.length; i++) {
            if(i > 0) {
                sb.append(", ");
            }
            sb.append(sqlDialect.escapeIdentifier(columnName[i]));
            sb.append(" = ?");
        }
        
        Object []values = new Object[columnName.length];
        for(int i = 0; i < columnName.length; i++)
            values[i] = proxy.getValue(columnName[i]);
        new SQLWorker(databaseConnection.createAutoExecutor()).write(sb.toString(), values);
        return entity;
    }
        
    public void refresh() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        refresh(executorService);
        executorService.shutdown();
        try {
            executorService.awaitTermination(17, TimeUnit.DAYS);
        }
        catch(InterruptedException e) {
        }
    }
    
    public void refresh(Executor executor) {
        for(final Class entityType: getAllKnownEntityTypes()) {
            executor.execute(new Runnable() {
                public void run() {
                    refresh(entityType);
                }
            });
        }
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> void refresh(Class<T> entityType) {
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        String sql = "SELECT " +
                        sqlDialect.escapeIdentifier("id") + 
                        getNonIdColumnsForSelect(entityType) +
                        " FROM " + 
                        sqlDialect.escapeIdentifier(getTableName(entityType));
        queryAndProcess(entityType, sql, (U[])null);
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> void refresh(Class<T> entityType, U... keys) {
        if(keys.length == 0)
            return;
        
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        String sql = "SELECT " +
                        sqlDialect.escapeIdentifier("id") + 
                        getNonIdColumnsForSelect(entityType) +
                        " FROM " + 
                        sqlDialect.escapeIdentifier(getTableName(entityType)) +
                        " WHERE " +
                        sqlDialect.escapeIdentifier("id") +
                        " IN (";
        for(int i = 0; i < keys.length; i++) {
            if(i > 0)
                sql += ", ";
            if(keys[i] == null)
                throw new IllegalArgumentException("Trying to refresh a " + entityType.getName() + " with key null");
            sql += formatKey(keys[i]);
        }        
        sql += ")";
        queryAndProcess(entityType, sql, keys);
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> void register(Class<T> entityType) {
        register(entityType, new DefaultClassTableMapping(entityType));
    }
    
    public <U extends Comparable<U>, T extends JORMEntity<U>> void register(Class<T> entityType, ClassTableMapping classTableMapping) {
        synchronized(tableMapping) {
            if(tableMapping.containsKey(entityType)) {
                throw new IllegalArgumentException("Can't register " + entityType.getName() + 
                        " because it's already registered");
            }
            tableMapping.put(entityType, classTableMapping);
            synchronized(datastore) {
                datastore.put(entityType, new ConcurrentHashMap());
            }
        }
    }
    
    private <U extends Comparable<U>, T extends JORMEntity<U>> void queryAndProcess(Class<T> entityType, String sql, U... keys) {
        try {
            List<Object[]> rows = new SQLWorker(databaseConnection.createAutoExecutor()).query(sql);
            Set<U> idsReturned = new HashSet<U>();
            Map<U, T> entityDataMap = getEntityDataMap(entityType);
            for(Object[] row: rows) {
                U id = (U)row[0];
                idsReturned.add(id);
                T entity = null;
                if(!entityDataMap.containsKey(id)) {
                    entity = newEntityProxy(entityType, id);
                    entityDataMap.put(id, entity);
                }
                else {
                    entity = entityDataMap.get(id);
                }
                
                if(entity instanceof EntityProxy.Resolver == false) {
                    throw new IllegalStateException("Encountered an entity which doesn't implement "
                            + "EntityProxy.Resolver!");
                }
                EntityProxy.Resolver<U, T> asResolver = (EntityProxy.Resolver<U, T>)entity;
                EntityProxy<U, T> proxy = asResolver.__underlying_proxy();
                proxy.populate(row);
            }
            
            //Remove missing rows
            Set<U> missingKeys = new HashSet<U>();
            if(keys == null)
                missingKeys.addAll(entityDataMap.keySet());
            else
                missingKeys.addAll(Arrays.asList(keys));
            missingKeys.removeAll(idsReturned);
            for(U key: missingKeys) {
                entityDataMap.remove(key);
            }
        }
        catch(SQLException e) {
            //TODO: CHANGE ME!!!
            e.printStackTrace();
        }
    }
    
    private String formatKey(Object key) {
        if(key instanceof String) {
            return "'" + databaseConnection.getServerType().getSQLDialect().escapeString((String)key) + "'";
        }
        else {
            return key.toString();
        }
    }
    
    private <U extends Comparable<U>, T extends JORMEntity<U>> T newEntityProxy(Class<T> entityType, U id) {
        EntityProxy<U, T> proxy = new EntityProxy<U, T>(entityType, this, getClassTableMapping(entityType), id);
        return (T)Proxy.newProxyInstance(
                    ClassLoader.getSystemClassLoader(), 
                    new Class[] { entityType, EntityProxy.Resolver.class }, 
                    proxy);
    }
    
    private <U extends Comparable<U>, T extends JORMEntity<U>> String getNonIdColumnsForSelect(Class<T> entityType) {
        String[] columns = getClassTableMapping(entityType).getNonIdColumns();
        StringBuilder sb = new StringBuilder();
        for(String columnName: columns) {
            sb.append(", ");
            sb.append(databaseConnection.getServerType().getSQLDialect().escapeIdentifier(columnName));
        }
        return sb.toString();
    }
    
    private <U extends Comparable<U>, T extends JORMEntity<U>> String getTableName(Class<T> entityType) {
        return getClassTableMapping(entityType).getTableName();
    }
    
    private <U extends Comparable<U>, T extends JORMEntity<U>> ClassTableMapping getClassTableMapping(Class<T> entityType) {
        synchronized(tableMapping) {
            if(!tableMapping.containsKey(entityType))
                throw new IllegalArgumentException("Trying to access the table name of an unregistered entity type " + entityType.getName());
            return tableMapping.get(entityType);
        }
    }
    
    private <U extends Comparable<U>, T extends JORMEntity<U>> List<Class<T>> getAllKnownEntityTypes() {
        synchronized(datastore) {
            return Collections.unmodifiableList(new ArrayList(datastore.keySet()));
        }
    }
    
    private <U extends Comparable<U>, T extends JORMEntity<U>> Map<U, T> getEntityDataMap(Class<T> entityType) {
        synchronized(datastore) {
            if(!datastore.containsKey(entityType))
                throw new IllegalArgumentException("Trying to access unregistered entity type " + entityType.getName());
            return datastore.get(entityType);
        }
    }
}
