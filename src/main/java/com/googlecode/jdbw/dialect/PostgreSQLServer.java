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
 * Copyright (C) 2009-2012 mabe02
 */

package com.googlecode.jdbw.dialect;

import com.googlecode.jdbw.DatabaseServer;
import com.googlecode.jdbw.DatabaseServerType;
import com.googlecode.jdbw.impl.DefaultDatabaseServer;
import java.util.Properties;

/**
 *
 * @author mabe02
 */
public class PostgreSQLServer extends DefaultDatabaseServer
{
    private PostgreSQLServer(String hostname, int port, String catalog, String username, String password)
    {
        super(hostname, port, catalog, username, password);
    }

    public static PostgreSQLServer newInstance(String hostname, String catalog, String username, String password)
    {
        return newInstance(hostname, "5432", catalog, username, password);
    }

    public static PostgreSQLServer newInstance(String hostname, String port, String catalog, String username, String password)
    {
        return newInstance(hostname, new Integer(port), catalog, username, password);
    }

    public static PostgreSQLServer newInstance(String hostname, int port, String catalog, String username, String password)
    {
        return new PostgreSQLServer(hostname, port, catalog, username, password);
    }

    @Override
    public DatabaseServerType getServerType()
    {
        return DatabaseServerType.POSTGRESQL;
    }

    @Override
    protected Properties getConnectionProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("user", getUsername());
        properties.setProperty("password", getPassword());
        return properties;
    }

    @Override
    protected String getJDBCUrl()
    {
        return "jdbc:postgresql://" + getHostname() + ":" + getPort() + "/" + getCatalog();
                //+ "?useUnicode=yes&characterEncoding=UTF-8";
    }

    @Override
    protected void loadDriver()
    {
        loadDriver("org.postgresql.Driver");
    }

    @Override
    public DatabaseServerTraits getServerTraits()
    {
        return new PostgreSQLTraits();
    }
}
