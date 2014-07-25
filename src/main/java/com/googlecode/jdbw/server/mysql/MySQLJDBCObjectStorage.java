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
package com.googlecode.jdbw.server.mysql;

import com.googlecode.jdbw.DatabaseConnection;
import com.googlecode.jdbw.DatabaseTransaction;
import com.googlecode.jdbw.TransactionIsolation;
import com.googlecode.jdbw.objectstorage.ObjectFactory;
import com.googlecode.jdbw.objectstorage.Storable;
import com.googlecode.jdbw.objectstorage.TableMapping;
import com.googlecode.jdbw.objectstorage.TableMappingFactory;
import com.googlecode.jdbw.objectstorage.impl.DefaultTableMappingFactory;
import com.googlecode.jdbw.objectstorage.impl.JDBCObjectStorage;
import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized {@code JDBCObjectStorage} which is optimizing {@code putAll} calls by using the
 * <b>INSERT INTO ... ON DUPLICATE UPDATE ...</b> syntax in MySQL.
 * @author Martin Berglund
 */
public class MySQLJDBCObjectStorage extends JDBCObjectStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLJDBCObjectStorage.class);
    
    public MySQLJDBCObjectStorage(DatabaseConnection databaseConnection) {
        this(databaseConnection, new DefaultTableMappingFactory());
    }

    public MySQLJDBCObjectStorage(DatabaseConnection databaseConnection, TableMappingFactory tableMappingFactory) {
        super(databaseConnection, new MySQLTableMappingFactory(tableMappingFactory));
    }

    public MySQLJDBCObjectStorage(DatabaseConnection databaseConnection, TableMappingFactory tableMappingFactory, ObjectFactory objectFactory) {
        super(databaseConnection, new MySQLTableMappingFactory(tableMappingFactory), objectFactory);
    }

    public MySQLJDBCObjectStorage(DatabaseConnection databaseConnection, TableMappingFactory tableMappingFactory, ObjectFactory objectFactory, int retryAttempts) {
        super(databaseConnection, new MySQLTableMappingFactory(tableMappingFactory), objectFactory, retryAttempts);
    }
    
    @Override
    protected <O extends Object & Storable> void doPutAll(Class<O> objectType, Collection<O> objects, TableMapping tableMapping) throws SQLException {
        DatabaseTransaction transaction = null;
        try {
            String sql = ((MySQLTableMapping)tableMapping).getMySQLInsertOrUpdate();
            transaction = getDatabaseConnection().beginTransaction(TransactionIsolation.REPEATABLE_READ);
            List<Object[]> batch = new ArrayList<Object[]>();
            for(O o: objects) {
                batch.add(transform(tableMapping, o));
            }
            transaction.batchWrite(new BatchUpdateHandlerAdapter(), sql, batch);            
            transaction.commit();
        }
        catch(SQLException e) {
            try {
                if(transaction != null) {
                    transaction.rollback();
                }
            }
            catch(SQLException e2) {
                //We don't really care about this
                LOGGER.debug("Database error when trying to rollback transaction after previous error (logged below)", e2);
            }
            throw e;
        }
    }
}
