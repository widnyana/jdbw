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
import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import com.googlecode.jdbw.util.SQLWorker;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    private static class EntityMapping {
        Class<? extends JORMEntity> entityType;
        ClassTableMapping tableMapping;
        Class idType;
    }
    
    private final DatabaseConnection databaseConnection;
    private final EntityCacheManager cacheManager;
    private final Map<Class<? extends JORMEntity>, EntityMapping> tableMapping;

    public JORMDatabase(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        this.cacheManager = new EntityCacheManager();
        this.tableMapping = new HashMap<Class<? extends JORMEntity>, EntityMapping>();
    }
    
    public <U, T extends JORMEntity<U>> ArrayList<T> getAll(Class<T> type) {
        return new ArrayList<T>(cacheManager.getCache(type).allValues());
    }
    
    public <U, T extends JORMEntity<U>> T get(Class<T> type, U key) {
        return get(type, key, SearchPolicy.CHECK_DATABASE_IF_MISSING);
    }
    
    public <U, T extends JORMEntity<U>> T get(Class<T> type, U key, SearchPolicy searchPolicy) {
        if(searchPolicy == SearchPolicy.REFRESH_FIRST) {
            refresh(type, key);
        }
        T entity = cacheManager.getCache(type).get(key);
        if(entity == null && searchPolicy == SearchPolicy.CHECK_DATABASE_IF_MISSING) {
            return get(type, key, SearchPolicy.REFRESH_FIRST);
        }
        return entity;
    }
    
    public <U, T extends JORMEntity<U>> T newEntity(Class<T> type) throws SQLException {
        return newEntity(type, (U)null);
    }
    
    public <U, T extends JORMEntity<U>> T newEntity(Class<T> type, U id) throws SQLException {
        return newEntities(type, Arrays.asList(id)).get(0);
    }
    
    public <U, T extends JORMEntity<U>> List<T> newEntities(final Class<T> type, int numberOfEntities) throws SQLException {
        if(numberOfEntities < 0) {
            throw new IllegalArgumentException("Cannot call JORMDatabase.newEntities with < 0 entities to create");
        }
        if(numberOfEntities == 0) {
            return Collections.emptyList();
        }
        List<U> nulls = new ArrayList<U>();
        for(int i = 0; i < numberOfEntities; i++) {
            nulls.add(null);
        }
        return newEntities(type, nulls);
    }
    
    public <U, T extends JORMEntity<U>> List<T> newEntities(final Class<T> type, U... ids) throws SQLException {
        return newEntities(type, Arrays.asList(ids));
    }
    
    private <U, T extends JORMEntity<U>> List<T> newEntities(final Class<T> type, List<U> ids) throws SQLException {
        if(ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error creating newEntity of type " + type.getSimpleName() + 
                    "; id parameter was empty");
        }
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(sqlDialect.escapeIdentifier(getTableName(type)));
        sb.append(" (");
        sb.append(sqlDialect.escapeIdentifier("id"));
        sb.append(") VALUES(?)");
        
        List<Object[]> keysToBatchInsert = new ArrayList<Object[]>();
        for(U id: ids) {
            if(id == null) {
               continue; 
            }
            if(id != null && !getIdType(type).isAssignableFrom(id.getClass())) {
                throw new IllegalArgumentException("Error creating newEntity of type " + type.getSimpleName() + 
                        "; expected id type " + getIdType(type) + " but got a " + id.getClass());
            }
            keysToBatchInsert.add(new Object[] { id });
        }
        if(keysToBatchInsert.size() > 0) {
            databaseConnection.createAutoExecutor().batchWrite(sb.toString(), keysToBatchInsert);
        }
         
        final List<U> keyToCreateEntitiesFrom = new ArrayList<U>();
        for(U id: ids) {
            if(id != null) {
                keyToCreateEntitiesFrom.add(id);
            }
            else {
                U newId = (U)new SQLWorker(databaseConnection.createAutoExecutor()).insert(sb.toString(), (Object)null);
                newId = (U)normalizeGeneratedId(getIdType(type), newId);
                if(newId != null) {
                    keyToCreateEntitiesFrom.add(newId);
                }
            }
        }
        if(keyToCreateEntitiesFrom.size() < ids.size()) {
            throw new IllegalStateException("After inserting row into " + getTableName(type) + ", couldn't "
                    + "figure out what primary key was assigned, your JDBC driver or database server "
                    + "probably don't support the feature to return auto-generated keys or the column "
                    + "isn't set up to auto generate keys");
        }
        
        List<T> newEntities = new ArrayList<T>();
        for(U id: keyToCreateEntitiesFrom) {
            if(id == null)
                continue;
            
            if(!getIdType(type).isAssignableFrom(id.getClass())) {
                throw new IllegalArgumentException("Error creating newEntity of type " + type.getSimpleName() + 
                        "; expected id type " + getIdType(type).getName() + " but supplied (or auto-generated) id type was a " + id.getClass().getName());
            }
            T entity = newEntityProxy(type, id);
            cacheManager.getCache(type).put(entity);
            newEntities.add(entity);
        }
        return newEntities;
    }
    
    public <U, T extends JORMEntity<U>> T persist(T entity) throws SQLException {
        persist(Arrays.asList(entity));
        return entity;
    }
    
    public <U, T extends JORMEntity<U>> void persist(T... entities) throws SQLException {
        persist(Arrays.asList(entities));
    }
    
    public <U, T extends JORMEntity<U>> void persist(Collection<T> entities) throws SQLException {
        if(entities == null || entities.isEmpty()) {
            return;
        }
        
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        EntityProxy.Resolver<U, T> asResolver = (EntityProxy.Resolver<U, T>)entities.iterator().next();
        EntityProxy<U, T> proxy = asResolver.__underlying_proxy();
        Class<T> entityType = proxy.getEntityType();
        
        String[] columnName = getClassTableMapping(entityType).getNonIdColumns(entityType);
        if(columnName.length == 0)
            return;
        
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
        sb.append(" WHERE ");
        sb.append(sqlDialect.escapeIdentifier("id"));
        sb.append(" = ?");
        
        List<Object[]> batches = new ArrayList<Object[]>();
        for(T entity: entities) {
            if(entity == null)
                continue;
            
            Object []values = new Object[columnName.length + 1];
            for(int i = 0; i < columnName.length; i++)
                values[i] = proxy.getValue(columnName[i]);
            values[columnName.length] = entity.getId();
            batches.add(values);
        }
        if(batches.isEmpty()) {
            return;
        }
        databaseConnection.createAutoExecutor().batchWrite(new BatchUpdateHandlerAdapter(), sb.toString(), batches);
    }
    
    public <U, T extends JORMEntity<U>> void remove(T... entities) throws SQLException {
        remove(Arrays.asList(entities));
    }
    
    public <U, T extends JORMEntity<U>> void remove(Collection<T> entities) throws SQLException {
        entities = removeNullElementsFromCollection(entities);
        if(entities == null || entities.isEmpty()) {
            return;
        }
        
        EntityProxy.Resolver<U, T> asResolver = (EntityProxy.Resolver<U, T>)entities.iterator().next();
        EntityProxy<U, T> proxy = asResolver.__underlying_proxy();
        Class<T> entityType = proxy.getEntityType();
        List<U> keysToRemove = new ArrayList<U>(entities.size());
        for(T entity: entities) {
            keysToRemove.add(entity.getId());
        }
        remove(entityType, keysToRemove);
    }
    
    public <U, T extends JORMEntity<U>> void remove(Class<T> entityType, U... ids) throws SQLException {
        remove(entityType, Arrays.asList(ids));
    }
    
    public <U, T extends JORMEntity<U>> void remove(Class<T> entityType, Collection<U> ids) throws SQLException {
        if(entityType == null) {
            throw new IllegalArgumentException("Cannot call remove(...) with null entityType");
        }
        ids = removeNullElementsFromCollection(ids);    //Transforms the collection to a list, but we'll keep the name
        if(ids == null || ids.isEmpty()) {
            return;
        }
        
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");        
        sb.append(sqlDialect.escapeIdentifier(getTableName(entityType)));
        sb.append(" WHERE ");
        sb.append(sqlDialect.escapeIdentifier("id"));
        sb.append(" IN (");
        for(int i = 0; i < ids.size(); i++) {
            if(i > 0)
                sb.append(", ");
            U id = ((List<U>)ids).get(i);
            sb.append(formatKey(id));
        }        
        sb.append(")");
        new SQLWorker(databaseConnection.createAutoExecutor()).write(sb.toString());
        cacheManager.getCache(entityType).removeAll(ids);
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
        for(final Class entityType: (List<Class>)cacheManager.getAllKnownEntityTypes()) {
            executor.execute(new Runnable() {
                public void run() {
                    refresh(entityType);
                }
            });
        }
    }
    
    public <U, T extends JORMEntity<U>> void refresh(T... entities) {
        if(entities.length == 0)
            return;
        
        List<U> keys = new ArrayList<U>();
        for(T entity: entities) {
            keys.add(entity.getId());
        }
        refresh(entities[0].getClass(), keys);
    }
    
    public <U, T extends JORMEntity<U>> void refresh(Class<T> entityType) {
        SQLDialect sqlDialect = databaseConnection.getServerType().getSQLDialect();
        String sql = "SELECT " +
                        sqlDialect.escapeIdentifier("id") + 
                        getNonIdColumnsForSelect(entityType) +
                        " FROM " + 
                        sqlDialect.escapeIdentifier(getTableName(entityType));
        queryAndProcess(entityType, sql, null);
    }
    
    public <U, T extends JORMEntity<U>> void refresh(Class<T> entityType, U... keys) {
        refresh(entityType, Arrays.asList(keys));
    }
    
    private <U, T extends JORMEntity<U>> void refresh(Class<T> entityType, List<U> keys) {
        if(keys.isEmpty())
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
        for(int i = 0; i < keys.size(); i++) {
            if(i > 0)
                sql += ", ";
            if(keys.get(i) == null)
                throw new IllegalArgumentException("Trying to refresh a " + entityType.getName() + " with key null");
            sql += formatKey(keys.get(i));
        }        
        sql += ")";
        queryAndProcess(entityType, sql, keys);
    }
    
    public <U, T extends JORMEntity<U>> void register(Class<T> entityType) {
        register(entityType, new DefaultClassTableMapping());
    }
    
    public <U, T extends JORMEntity<U>> void register(Class<T> entityType, ClassTableMapping classTableMapping) {
        synchronized(tableMapping) {
            if(tableMapping.containsKey(entityType)) {
                throw new IllegalArgumentException("Can't register " + entityType.getName() + 
                        " because it's already registered");
            }
            
            //Try to determine the type of the id
            Type idType = null;
            for(Type type: entityType.getGenericInterfaces()) {
                if(type instanceof ParameterizedType) {
                    ParameterizedType ptype = (ParameterizedType)type;
                    if(ptype.getRawType() == JORMEntity.class) {
                        idType = (Class)ptype.getActualTypeArguments()[0];
                        break;
                    }
                }
            }
            if(idType == null) {
                throw new IllegalArgumentException("Could not determine the id type for " + entityType.getSimpleName());
            }
            else if(idType instanceof Class == false) {
                throw new IllegalArgumentException("Illegal id type for " + entityType.getSimpleName() + " (" + idType.toString() + " isn't a class)");
            }
            
            EntityMapping entityMapping = new EntityMapping();
            entityMapping.entityType = entityType;
            entityMapping.tableMapping = classTableMapping;
            entityMapping.idType = (Class)idType;
            
            tableMapping.put(entityType, entityMapping);
            cacheManager.createDataCache(entityType);
        }
    }
    
    private <U, T extends JORMEntity<U>> void queryAndProcess(Class<T> entityType, String sql, List<U> expectedKeys) {
        try {
            List<Object[]> rows = new SQLWorker(databaseConnection.createAutoExecutor()).query(sql);
            Set<U> idsReturned = new HashSet<U>();
            DataCache<U,T> entityDataMap = cacheManager.getCache(entityType);
            for(Object[] row: rows) {
                U id = (U)row[0];
                idsReturned.add(id);
                T entity = null;
                if(!entityDataMap.contains(id)) {
                    entity = newEntityProxy(entityType, id);
                    entityDataMap.put(entity);
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
            if(expectedKeys == null)
                missingKeys.addAll(entityDataMap.allIds());
            else
                missingKeys.addAll(expectedKeys);
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
    
    private <U, T extends JORMEntity<U>> T newEntityProxy(Class<T> entityType, U id) {
        EntityProxy<U, T> proxy = new EntityProxy<U, T>(entityType, getClassTableMapping(entityType), id);
        return (T)Proxy.newProxyInstance(
                    ClassLoader.getSystemClassLoader(), 
                    new Class[] { entityType, EntityProxy.Resolver.class }, 
                    proxy);
    }
    
    private <U, T extends JORMEntity<U>> String getNonIdColumnsForSelect(Class<T> entityType) {
        String[] columns = getClassTableMapping(entityType).getNonIdColumns(entityType);
        StringBuilder sb = new StringBuilder();
        for(String columnName: columns) {
            sb.append(", ");
            sb.append(databaseConnection.getServerType().getSQLDialect().escapeIdentifier(columnName));
        }
        return sb.toString();
    }
    
    private <U> U normalizeGeneratedId(Class<U> type, U generatedId) {
        if(type.isAssignableFrom(generatedId.getClass())) {
            return generatedId;
        }
        else if(type == Integer.class) {
            if(generatedId.getClass() == Long.class) {
                return (U)new Integer((int)((Long)generatedId).longValue());
            }
            else if(generatedId.getClass() == BigInteger.class) {
                return (U)new Integer(((BigInteger)generatedId).intValue());
            }
        }
        else if(type == Long.class) {
            if(generatedId.getClass() == Integer.class) {
                return (U)new Long(((Integer)generatedId).longValue());
            }
            else if(generatedId.getClass() == BigInteger.class) {
                return (U)new Long(((BigInteger)generatedId).longValue());
            }
        }
        return generatedId;
    }
    
    private <T> List<T> removeNullElementsFromCollection(Collection<T> collection) {
        if(collection == null)
            return null;
        
        List<T> list = new ArrayList<T>(collection.size());
        for(T element: collection) {
            if(element != null) {
                list.add(element);
            }
        }
        return list;
    }
    
    private <U, T extends JORMEntity<U>> String getTableName(Class<T> entityType) {
        return getClassTableMapping(entityType).getTableName(entityType);
    }
    
    private <U, T extends JORMEntity<U>> ClassTableMapping getClassTableMapping(Class<T> entityType) {
        return getMapping(entityType).tableMapping;
    }
    
    private <U, T extends JORMEntity<U>> Class getIdType(Class<T> entityType) {
        return getMapping(entityType).idType;
    }
    
    private <U, T extends JORMEntity<U>> EntityMapping getMapping(Class<T> entityType) {
        synchronized(tableMapping) {
            if(!tableMapping.containsKey(entityType))
                throw new IllegalArgumentException("Trying to access the table name of an unregistered entity type " + entityType.getName());
            return tableMapping.get(entityType);
        }
    }
}
