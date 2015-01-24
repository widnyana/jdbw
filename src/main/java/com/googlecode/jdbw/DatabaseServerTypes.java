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

package com.googlecode.jdbw;

import com.googlecode.jdbw.server.h2.H2ServerType;
import com.googlecode.jdbw.server.mysql.MySQLServerType;
import com.googlecode.jdbw.server.postgresql.PostgreSQLServerType;
import com.googlecode.jdbw.server.sybase.SybaseASEServerType;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This is a repository of known database server types. If you have created your own database server type, you can add
 * it in here to the {@ALL_KNOWN_SERVER_TYPES} constants (this isn't required though).
 * @author Martin Berglund
 */
public class DatabaseServerTypes {
    private DatabaseServerTypes() {}

    /**
     * This constant keeps track of all known database server types. The list is statically defined but you can add your
     * own types to it if you wish; the Set can be added to.
     */
    public static final Set<DatabaseServerType> ALL_KNOWN_SERVER_TYPES = new AddOnlySet<DatabaseServerType>();

    /**
     * DatabaseServerType implementation for MySQL (mostly compatible with MariaDB and other derivatives)
     */
    public static final MySQLServerType MYSQL = MySQLServerType.INSTANCE;

    /**
     * DatabaseServerType implementation for Sybase ASE
     */
    public static final SybaseASEServerType SYBASE_ASE = SybaseASEServerType.INSTANCE;

    /**
     * DatabaseServerType implementation for PostgreSQL
     */
    public static final PostgreSQLServerType POSTGRESQL = PostgreSQLServerType.INSTANCE;

    /**
     * DatabaseServerType implementation for the H2 database, using its in-memory mode that is operating without any
     * disk persistence.
     */
    public static final H2ServerType H2_IN_MEMORY = H2ServerType.InMemory.INSTANCE;

    /**
     * DatabaseServerType implementation for the H2 database, using its file-based mode that stores the database in a
     * file and allows only a single database connection to it at any time.
     */
    public static final H2ServerType H2_FILE = H2ServerType.FileBased.INSTANCE;

    /**
     * DatabaseServerType implementation for the H2 database, using its network mode where it connects to a database
     * server over TCP/IP, much like traditional database servers.
     */
    public static final H2ServerType H2_NETWORK = H2ServerType.Network.INSTANCE;
    
    static {
        ALL_KNOWN_SERVER_TYPES.add(MYSQL);
        ALL_KNOWN_SERVER_TYPES.add(SYBASE_ASE);
        ALL_KNOWN_SERVER_TYPES.add(POSTGRESQL);
        ALL_KNOWN_SERVER_TYPES.add(H2_IN_MEMORY);
        ALL_KNOWN_SERVER_TYPES.add(H2_FILE);
        ALL_KNOWN_SERVER_TYPES.add(H2_NETWORK);
    }

    private static class AddOnlySet<V> extends CopyOnWriteArraySet<V> {
        //Prevent removing items
        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("You cannot remove elements from DatabaseServerTypes.ALL_KNOWN_SERVER_TYPES");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("You cannot remove elements from DatabaseServerTypes.ALL_KNOWN_SERVER_TYPES");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("You cannot remove elements from DatabaseServerTypes.ALL_KNOWN_SERVER_TYPES");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("You cannot remove elements from DatabaseServerTypes.ALL_KNOWN_SERVER_TYPES");
        }
    }
}
