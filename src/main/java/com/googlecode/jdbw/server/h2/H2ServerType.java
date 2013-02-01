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
package com.googlecode.jdbw.server.h2;

import com.googlecode.jdbw.SQLDialect;
import com.googlecode.jdbw.metadata.ServerMetaData;
import com.googlecode.jdbw.server.AbstractDatabaseType;
import javax.sql.DataSource;

/**
 *
 * @author Martin Berglund
 */
public abstract class H2ServerType extends AbstractDatabaseType {
    
    protected H2ServerType() {
    }

    @Override
    public SQLDialect getSQLDialect() {
        return new H2SQLDialect();
    }

    @Override
    public ServerMetaData createMetaDataResolver(DataSource dataSource) {
        return new H2MetaDataResolver(dataSource);
    }
    
    public static class InMemory extends H2ServerType {
        
        public static final InMemory INSTANCE = new InMemory();
        private InMemory() { }
        
        public String getName() {
            return "H2 in-memory";
        }        
    }
    public static class FileBased extends H2ServerType {
        
        public static final FileBased INSTANCE = new FileBased();        
        private FileBased() { }
        
        public String getName() {
            return "H2 file based";
        }        
    }
    public static class Network extends H2ServerType {
        
        public static final Network INSTANCE = new Network();        
        private Network() { }
        
        public String getName() {
            return "H2 TCP/IP";
        }        
    }
}
