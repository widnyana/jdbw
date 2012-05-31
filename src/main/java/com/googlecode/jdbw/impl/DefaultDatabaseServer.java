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

package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.DatabaseServer;
import java.sql.*;
import java.util.Properties;

/**
 *
 * @author mabe02
 */
public abstract class DefaultDatabaseServer implements DatabaseServer
{
    private final String hostname;
    private final int port;
    private final String catalog;
    private final String username;
    private final String password;

    protected DefaultDatabaseServer(String hostname, int port, String catalog, String username, String password)
    {
        this.hostname = hostname;
        this.port = port;
        this.catalog = catalog;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getCatalog()
    {
        return catalog;
    }

    @Override
    public String getHostname()
    {
        return hostname;
    }

    @Override
    public int getPort()
    {
        return port;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    protected abstract String getJDBCUrl();
    protected abstract Properties getConnectionProperties();
    protected abstract void loadDriver();

    protected void loadDriver(String className)
    {
        try {
            //LOGGER.log(Level.FINER, "Loading JDBC driver: {0}", className);
            Class.forName(className);
        }
        catch(ClassNotFoundException e) {
            //LOGGER.log(Level.WARNING, "Couldn''t load JDBC driver \"{0}\", did you include the right .jar file?", className);
        }
    }

    protected boolean isConnectionError(SQLException e)
    {
        if(e instanceof SQLTransientException)
            return true;
        if(e instanceof SQLNonTransientException)
            return true;    //Try again...
        if(e instanceof SQLRecoverableException)
            return true;
        
        if(e instanceof SQLSyntaxErrorException)
            return false;

        //Other than that, dunno...! You'll have to implement this for every database vendor!
        return false;
    }

    protected DefaultDatabaseConnection createDatabaseConnection() {
        return new DefaultDatabaseConnection(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null) {
            return false;
        }
        if(!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        final DefaultDatabaseServer other = (DefaultDatabaseServer) obj;
        if((this.hostname == null) ? (other.hostname != null) : !this.hostname.equals(other.hostname)) {
            return false;
        }
        if(this.port != other.port) {
            return false;
        }
        if((this.catalog == null) ? (other.catalog != null) : !this.catalog.equals(other.catalog)) {
            return false;
        }
        if((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
            return false;
        }
        if((this.password == null) ? (other.password != null) : !this.password.equals(other.password)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + (this.hostname != null ? this.hostname.hashCode() : 0);
        hash = 41 * hash + this.port;
        hash = 41 * hash + (this.catalog != null ? this.catalog.hashCode() : 0);
        hash = 41 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 41 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return getJDBCUrl();
    }
}
