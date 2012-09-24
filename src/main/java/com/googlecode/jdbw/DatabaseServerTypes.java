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

import com.googlecode.jdbw.server.h2.H2ServerTypes;
import com.googlecode.jdbw.server.mysql.MySQLServerType;
import com.googlecode.jdbw.server.postgresql.PostgresqlServerType;
import com.googlecode.jdbw.server.sybase.SybaseASEServerType;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This is a repository of known database server types. You can add your own
 * types at runtime if you need to.
 * @author Martin Berglund
 */
public class DatabaseServerTypes {
    private DatabaseServerTypes() {}
    
    public static final Set<DatabaseServerType> ALL_KNOWN_SERVER_TYPES = new CopyOnWriteArraySet<DatabaseServerType>();
    
    public static final DatabaseServerType MYSQL = MySQLServerType.INSTANCE;
    public static final DatabaseServerType SYBASE_ASE = SybaseASEServerType.INSTANCE;
    public static final DatabaseServerType POSTGRESQL = PostgresqlServerType.INSTANCE;
    public static final DatabaseServerType H2_IN_MEMORY = H2ServerTypes.InMemory.INSTANCE;
    public static final DatabaseServerType H2_FILE = H2ServerTypes.FileBased.INSTANCE;
    public static final DatabaseServerType H2_NETWORK = H2ServerTypes.Network.INSTANCE;
    
    static {
        ALL_KNOWN_SERVER_TYPES.add(MYSQL);
        ALL_KNOWN_SERVER_TYPES.add(SYBASE_ASE);
        ALL_KNOWN_SERVER_TYPES.add(POSTGRESQL);
        ALL_KNOWN_SERVER_TYPES.add(H2_IN_MEMORY);
        ALL_KNOWN_SERVER_TYPES.add(H2_FILE);
        ALL_KNOWN_SERVER_TYPES.add(H2_NETWORK);
    }
}
