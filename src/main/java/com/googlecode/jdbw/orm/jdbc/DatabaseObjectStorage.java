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
package com.googlecode.jdbw.orm.jdbc;

import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.DatabaseTransaction;
import com.googlecode.jdbw.SQLExecutor;
import com.googlecode.jdbw.TransactionIsolation;
import com.googlecode.jdbw.orm.AutoTriggeredObjectStorage;
import com.googlecode.jdbw.orm.DefaultObjectInitializer;
import com.googlecode.jdbw.orm.Identifiable;
import com.googlecode.jdbw.orm.Modifiable;
import com.googlecode.jdbw.orm.ObjectInitializer;
import com.googlecode.jdbw.orm.Persistable;
import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import com.googlecode.jdbw.util.SQLWorker;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseObjectStorage extends AutoTriggeredObjectStorage{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseObjectStorage.class);

    private final DatabaseConnection databaseConnection;
    
    private final TableMappings tableMappings;
    private final DatabaseTableDataStorage databaseTableDataStorage;
    private final ObjectInitializers objectInitializers;

    public DatabaseObjectStorage(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        this.databaseTableDataStorage = new DatabaseTableDataStorage();
        this.objectInitializers = new ObjectInitializers();
        this.tableMappings = new TableMappings();
    }
    
    @Override
    public <U, T extends Identifiable<U>> void register(
            Class<T> objectType, 
            ObjectInitializer initializer) throws SQLException {
        
        if(initializer == null) {
            initializer = new DefaultObjectInitializer();
        }
        register(objectType, initializer, new DefaultTableMapping());
    }
    
    public <U, T extends Identifiable<U>> void register(
            Class<T> objectType, 
            ObjectInitializer initializer, 
            TableMapping tableMapping) throws SQLException {
        
        if(objectType == null) {
            throw new IllegalArgumentException("Cannot call register(...) with null objectType");
        }
        if(initializer == null) {
            throw new IllegalArgumentException("Cannot register " + objectType.getSimpleName() + 
                    " with a null object initializer");
        }
        if(tableMapping == null) {
            throw new IllegalArgumentException("Cannot register " + objectType.getSimpleName() + 
                    " with a null table mapping");
        }
        if(isRegistered(objectType)) {
            return;
        }
        
        objectInitializers.add(objectType, initializer);
        databaseTableDataStorage.add(objectType, 
                tableMapping.getTableName(objectType), 
                tableMapping.getFieldNames(objectType), 
                tableMapping.getFieldTypes(objectType));
        tableMappings.add(objectType, tableMapping);
    }
    
    @Override
    public <U, T extends Identifiable<U>> T get(Class<T> type, U key, CachePolicy searchPolicy) throws SQLException {
        if(searchPolicy == CachePolicy.EXTERNAL_GET) {
            refresh(type, key);
        }
        return databaseTableDataStorage.get(type).getProxyObject(key);
    }

    @Override
    public <U, T extends Identifiable<U>> List<T> getAll(Class<T> type, CachePolicy searchPolicy) throws SQLException {
        if(searchPolicy == CachePolicy.EXTERNAL_GET) {
            refresh(type);
        }
        return databaseTableDataStorage.get(type).getAllProxyObjects();
    }

    @Override
    public void refresh(Executor executor) {
        List<Class<? extends Identifiable>> registeredObjectTypes = databaseTableDataStorage.getAllObjectTypes();
        final CountDownLatch countDownLatch = new CountDownLatch(registeredObjectTypes.size());
        for(final Class objectType: registeredObjectTypes) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        refresh(objectType);
                    }
                    catch(Throwable e) {
                        LOGGER.error("Error refreshing " + objectType.getSimpleName(), e);
                    }
                    finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await();
        }
        catch(InterruptedException e) {}
    }

    @Override
    public <U, T extends Identifiable<U>> void refresh(T... objects) throws SQLException {
        List<T> nonNullObjects = removeNullElementsFromCollection(Arrays.asList(objects));
        if(nonNullObjects.isEmpty())
            return;
        
        List<U> keys = new ArrayList<U>();
        for(T object: nonNullObjects) {
            keys.add(object.getId());
        }
        
        Class<T> objectType = getObjectType(nonNullObjects.get(0));
        refresh(objectType, keys);
    }

    @Override
    public <U, T extends Identifiable<U>> void refresh(Class<T> objectType) throws SQLException {
        if(!isRegistered(objectType)) {
            throw new IllegalArgumentException("Cannot refresh non-registered type " + objectType.getSimpleName());
        }
        
        String sql = tableMappings.get(objectType).getSelectAll(
                databaseConnection.getServerType().getSQLDialect(), 
                objectType, 
                tableMappings.get(objectType));
        List<Object[]> rows = new SQLWorker(databaseConnection.createAutoExecutor()).query(sql);
        databaseTableDataStorage.get(objectType).renewAll(rows);
    }
    
    @Override
    public <U, T extends Identifiable<U>> void refresh(Class<T> objectType, U... keys) throws SQLException {
        refresh(objectType, Arrays.asList(keys));
    }
    
    private <U, T extends Identifiable<U>> void refresh(Class<T> objectType, List<U> keys) throws SQLException {
        if(!isRegistered(objectType)) {
            throw new IllegalArgumentException("Cannot refresh non-registered type " + objectType.getSimpleName());
        }
        
        String sql = tableMappings.get(objectType).getSelectSome(
                databaseConnection.getServerType().getSQLDialect(), 
                objectType, 
                tableMappings.get(objectType),
                keys);
        List<Object[]> rows = new SQLWorker(databaseConnection.createAutoExecutor()).query(sql);
        databaseTableDataStorage.get(objectType).renewSome(rows);
    }

    @Override
    public <U, T extends Identifiable<U> & Modifiable> T newObject(Class<T> type, U id) throws SQLException {
        return newObjects(type, Arrays.asList(id)).get(0);
    }

    @Override
    public <U, T extends Identifiable<U> & Modifiable> List<T> newObjects(Class<T> type, int numberOfObjects) throws SQLException {
        if(numberOfObjects < 0) {
            throw new IllegalArgumentException("Cannot call DatabaseObjectStorage.newObjects(...) with < 0 objects to create");
        }
        if(numberOfObjects == 0) {
            return Collections.emptyList();
        }
        List<U> nulls = new ArrayList<U>();
        for(int i = 0; i < numberOfObjects; i++) {
            nulls.add(null);
        }
        return newObjects(type, nulls);
    }

    @Override
    public <U, T extends Identifiable<U> & Modifiable> List<T> newObjects(Class<T> type, U... ids) throws SQLException {
        return newObjects(type, Arrays.asList(ids));
    }
    
    @Override
    public <U, T extends Identifiable<U> & Modifiable> List<T> newObjects(final Class<T> objectType, Collection<U> ids) throws SQLException {
        if(ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        if(!isRegistered(objectType)) {
            throw new IllegalArgumentException("Cannot create new objects of unregistered type " + objectType.getSimpleName());
        }
        
        Map<String, Object> objectInitData = getObjectInitializationData(objectType);
        List<T> newObjects = new ArrayList<T>();
        for(U id: ids) {
            if(id != null && !getIdentifiableIdType(objectType).isAssignableFrom(id.getClass())) {
                throw new IllegalArgumentException("Error creating new object of type " + objectType.getSimpleName() + 
                        "; expected id type " + getIdentifiableIdType(objectType).getSimpleName() + 
                        " but supplied (or auto-generated) id type was a " + id.getClass().getName());
            }
            T entity = newInsertableObjectProxy(objectType, id, objectInitData);
            newObjects.add(entity);
        }
        return newObjects;
    }

    @Override
    public <U, T extends Identifiable<U> & Modifiable> List<T> persist(Collection<Persistable<U, T>> persistables) throws SQLException {
        if(persistables == null ||persistables.isEmpty()) {
            return Collections.emptyList();
        }
        Class<T> type = persistables.iterator().next().getObjectType();
        List<InsertableObjectProxyHandler.Finalized<U, T>> toInsert = new ArrayList<InsertableObjectProxyHandler.Finalized<U, T>>();
        List<UpdatableObjectProxyHandler.Finalized<U, T>> toUpdate = new ArrayList<UpdatableObjectProxyHandler.Finalized<U, T>>();        
        for(Persistable<U, T> persistable: persistables) {
            if(persistable instanceof InsertableObjectProxyHandler.Finalized) {
                toInsert.add((InsertableObjectProxyHandler.Finalized)persistable);
            }
            else if(persistable instanceof UpdatableObjectProxyHandler.Finalized) {
                toUpdate.add((UpdatableObjectProxyHandler.Finalized)persistable);
            }
        }
        List<T> result = new ArrayList<T>();
        List<U> keys = new ArrayList<U>();
        DatabaseTransaction transaction = databaseConnection.beginTransaction(TransactionIsolation.READ_UNCOMMITTED);
        try {
            keys.addAll(insert(transaction, toInsert));
            keys.addAll(update(transaction, toUpdate));
            transaction.commit();
        }
        catch(SQLException e) {
            try {
                transaction.rollback();
            }
            catch(SQLException e2) {}
            throw e;
        }
        
        for(U key: keys) {
            result.add((T)get(type, key, CachePolicy.LOCAL_GET));
        }
        return result;
    }
    
    private <U, T extends Identifiable<U> & Modifiable> List<U> insert(
            SQLExecutor executor, 
            List<InsertableObjectProxyHandler.Finalized<U, T>> persistables) throws SQLException {
        
        List<InsertableObjectProxyHandler.Finalized<U, T>> insertionsWithAutoGeneratedIds 
                = new ArrayList<InsertableObjectProxyHandler.Finalized<U, T>>();
        List<InsertableObjectProxyHandler.Finalized<U, T>> insertionsWithoutAutoGeneratedIds 
                = new ArrayList<InsertableObjectProxyHandler.Finalized<U, T>>();
        
        for(InsertableObjectProxyHandler.Finalized<U, T> persistable: persistables) {
            if(persistable.getId() == null) {
                insertionsWithAutoGeneratedIds.add(persistable);
            }
            else {
                insertionsWithoutAutoGeneratedIds.add(persistable);
            }
        }
        
        List<U> keys = new ArrayList<U>();
        keys.addAll(insertAutoGeneratedIdRows(executor, insertionsWithAutoGeneratedIds));
        keys.addAll(insertNormalRows(executor, insertionsWithoutAutoGeneratedIds));
        return keys;
    }
    
    private <U, T extends Identifiable<U> & Modifiable> List<U> insertAutoGeneratedIdRows(
            SQLExecutor executor,
            List<InsertableObjectProxyHandler.Finalized<U, T>> persistables) throws SQLException {
        
        if(persistables == null || persistables.isEmpty()) {
            return Collections.emptyList();
        }
        Class<T> objectType = persistables.get(0).getObjectType();
        String sql = tableMappings.get(objectType).getInsert(
                databaseConnection.getServerType().getSQLDialect(), 
                objectType,
                tableMappings.get(objectType));
        List<U> keys = new ArrayList<U>();
        for(InsertableObjectProxyHandler.Finalized<U, T> persistable: persistables) {
            Object[] values = persistable.getValues();
            U newId = (U)new SQLWorker(executor).insert(sql, values);
            
            if(newId != null) {
                values[0] = newId;
                databaseTableDataStorage.get(objectType).addRow(values);
                keys.add(newId);
            }
        }
        return keys;
    }
    
    private <U, T extends Identifiable<U> & Modifiable> List<U> insertNormalRows(
            SQLExecutor executor,
            List<InsertableObjectProxyHandler.Finalized<U, T>> persistables) throws SQLException {
        
        if(persistables == null || persistables.isEmpty()) {
            return Collections.emptyList();
        }
        Class<T> objectType = persistables.get(0).getObjectType();
        String sql = tableMappings.get(objectType).getInsert(
                databaseConnection.getServerType().getSQLDialect(), 
                objectType,
                tableMappings.get(objectType));
        List<U> keys = new ArrayList<U>();
        List<Object[]> batchParameters = new ArrayList<Object[]>();
        for(InsertableObjectProxyHandler.Finalized<U, T> persistable: persistables) {
            batchParameters.add(persistable.getValues());
            keys.add(persistable.getId());
        }
        executor.batchWrite(new BatchUpdateHandlerAdapter(), sql, batchParameters);
        databaseTableDataStorage.get(objectType).renewSome(batchParameters);
        return keys;
    }
    
    private <U, T extends Identifiable<U> & Modifiable> List<U> update(
            SQLExecutor executor, 
            List<UpdatableObjectProxyHandler.Finalized<U, T>> persistables) throws SQLException {
        
        if(persistables == null || persistables.isEmpty()) {
            return Collections.emptyList();
        }
        Class<T> objectType = persistables.get(0).getObjectType();
        String sql = tableMappings.get(objectType).getUpdate(
                databaseConnection.getServerType().getSQLDialect(), 
                objectType,
                tableMappings.get(objectType));
        List<U> keys = new ArrayList<U>();
        List<Object[]> batchParameters = new ArrayList<Object[]>();
        for(UpdatableObjectProxyHandler.Finalized<U, T> persistable: persistables) {
            batchParameters.add(persistable.getValues());
            keys.add(persistable.getId());
        }
        executor.batchWrite(new BatchUpdateHandlerAdapter(), sql, batchParameters);
        databaseTableDataStorage.get(objectType).renewSome(batchParameters, true);
        return keys;
    }

    @Override
    public <U, T extends Identifiable<U>> void delete(Collection<T> objects) throws SQLException {
        objects = removeNullElementsFromCollection(objects);
        if(objects == null || objects.isEmpty()) {
            return;
        }
        
        Class<T> objectType = getObjectType(objects.iterator().next());
        List<U> keysToRemove = new ArrayList<U>(objects.size());
        for(T object: objects) {
            keysToRemove.add(object.getId());
        }
        delete(objectType, keysToRemove);
    }

    @Override
    public <U, T extends Identifiable<U>> void delete(Class<T> objectType, Collection<U> ids) throws SQLException {
        if(objectType == null) {
            throw new IllegalArgumentException("Cannot call delete(...) with null objectType");
        }
        ids = removeNullElementsFromCollection(ids);    //Transforms the collection to a list, but we'll keep the name
        if(ids == null || ids.isEmpty()) {
            return;
        }
        
        DatabaseTransaction databaseTransaction = databaseConnection.beginTransaction(TransactionIsolation.READ_UNCOMMITTED);
        try {
            String sql = tableMappings.get(objectType).getDelete(
                databaseConnection.getServerType().getSQLDialect(), 
                objectType,
                tableMappings.get(objectType), 
                ids.size());
            Object[] parameters = new Object[ids.size()];
            for(int i = 0; i < ids.size(); i++) {
                parameters[i] = ((List<U>)ids).get(i);
            }
            new SQLWorker(databaseTransaction).write(sql, parameters);
        }
        catch(SQLException e) {
            try {
                databaseTransaction.rollback();
            }
            catch(SQLException e2) {}
            throw e;
        }
        
        databaseTableDataStorage.get(objectType).remove((List<U>)ids);
    }
    
    public <U, T extends Identifiable<U>> boolean isRegistered(Class<T> objectType) {
        return tableMappings.get(objectType) != null;
    }

    private Class getIdentifiableIdType(Class<? extends Identifiable> objectType) {
        //Try to determine the type of the id
        for(Type type: objectType.getGenericInterfaces()) {
            if(type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType)type;
                if(ptype.getRawType() == Identifiable.class) {
                    return (Class)ptype.getActualTypeArguments()[0];
                }
            }
            else if(type instanceof Class) {
                Class idType = getIdentifiableIdType((Class)type);
                if(idType != null)
                    return idType;
            }
        }
        return null;
    }
    
    private <U, T extends Identifiable<U>> Class<T> getObjectType(T proxyObject) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxyObject);
        if(invocationHandler instanceof CommonProxyHandler == false) {
            throw new IllegalArgumentException("Proxy object " + proxyObject + " had an unknown "
                    + "invocation handler (" + invocationHandler.getClass().getName() + ")");
        }
        return ((CommonProxyHandler)invocationHandler).getObjectType();
    }
    
    private <U, T extends Identifiable<U> & Modifiable> T newInsertableObjectProxy(
            Class<T> objectType,
            U key,
            Map<String, Object> initialValues) {
        
        InsertableObjectProxyHandler<U, T> handler = new InsertableObjectProxyHandler<U, T>(
                tableMappings.get(objectType), 
                objectType, 
                key,
                initialValues);
        return (T)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] {objectType}, handler);
    }
    
    private <U, T extends Identifiable<U>> Map<String, Object> getObjectInitializationData(Class<T> objectType) {
        TableMapping tableMapping = tableMappings.get(objectType);
        ObjectInitializer objectInitializer = objectInitializers.get(objectType);
        Map<String, Object> initData = new HashMap<String, Object>();
        for(String fieldName: tableMapping.getFieldNames(objectType)) {
            initData.put(fieldName, objectInitializer.getInitialValue(objectType, fieldName));
        }
        return initData;
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
}
