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

import com.googlecode.jdbw.DatabaseServerTypes;

/**
 * Represents a H2 in-memory database that is normally deleted once all connections have been closed. These are two
 * separate modes for this, either anonymous or named. In anonymous mode, the database is completely private and can
 * only be seen by the one connection that connected to it. If you open multiple connections, they will each have one
 * private database each and cannot see each other. Because of this, database connection pooling is not possible. In
 * named mode, you create a virtual database in-memory that multiple connections can connect to (any connections opened
 * using the same name within the same JVM will be pointing at the same database).
 * @author Martin Berglund
 */
public class H2InMemoryServer extends H2DatabaseServer {

    private final String name;

    /**
     * Defines an anonymous in-memory H2 server
     */
    public H2InMemoryServer() {
        this(null);
    }

    /**
     * Defines a named in-memory H2 server (or an anonymous if name is {@code null})
     * @param name Name of the in-memory server, of {@code null} for an anonymous server
     */
    public H2InMemoryServer(String name) {
        super(DatabaseServerTypes.H2_IN_MEMORY, name != null);
        this.name = name;
    }

    @Override
    String getJDBCUrl(H2JDBCDriverDescriptor driverDescriptor) {
        if(name == null) {
            return driverDescriptor.formatJDBCUrlForAnonymousInMemory();
        }
        else {
            return driverDescriptor.formatJDBCUrlForInMemory(name);
        }
    }
}
