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

import java.io.File;
import java.util.Properties;
import com.googlecode.jdbw.DatabaseServer;
import com.googlecode.jdbw.DatabaseServerType;

/**
 *
 * @author mabe02
 */
public class H2Server extends DefaultDatabaseServer {

    private final H2ServerType serverType;
    private final String fileKey;

    private H2Server(H2ServerType serverType, String hostname, int port, String catalog,
            String username, String password, String fileKey) {
        super(hostname, port, catalog, username, password);
        this.fileKey = fileKey;
        this.serverType = serverType;
    }

    @Override
    public DatabaseServerType getServerType() {
        return DatabaseServerType.H2;
    }

    public H2ServerType getH2ServerType() {
        return serverType;
    }

    @Override
    public DatabaseServerTraits getServerTraits() {
        return new H2Traits();
    }

    @Override
    protected Properties getConnectionProperties() {
        Properties properties = new Properties();
        properties.setProperty("user", getUsername());
        if(fileKey == null && getPassword() != null) {
            properties.setProperty("password", getPassword());
        } else if(getPassword() != null) {
            properties.setProperty("password", fileKey + " " + getPassword());
        }
        return properties;
    }

    @Override
    protected void loadDriver() {
        loadDriver("org.h2.Driver");
    }

    @Override
    public String getCatalog() {
        if(serverType == H2ServerType.LOCAL_FILE) {
            return new File(super.getCatalog()).getName();
        } else {
            return super.getCatalog();
        }
    }

    @Override
    protected String getJDBCUrl() {
        switch(serverType) {
            case IN_MEMORY:
                if(getCatalog() == null) {
                    return "jdbc:h2:mem:";
                } else {
                    return "jdbc:h2:mem:" + super.getCatalog() + ";DB_CLOSE_DELAY=-1";
                }

            case LOCAL_FILE:
                return "jdbc:h2:file:" + super.getCatalog();

            case ENCRYPTED_FILE_AES:
                return "jdbc:h2:file:" + super.getCatalog() + ";CIPHER=AES";

            case ENCRYPTED_FILE_XTEA:
                return "jdbc:h2:file:" + super.getCatalog() + ";CIPHER=XTEA";

            case REMOTE_TCP:
                return "jdbc:h2:tcp://" + getHostname() + ":" + getPort() + "/" + super.getCatalog();

            case REMOTE_SSL:
                return "jdbc:h2:ssl://" + getHostname() + ":" + getPort() + "/" + super.getCatalog();
        }
        throw new RuntimeException("Unhandled type " + serverType + "!");
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        final H2Server other = (H2Server) obj;
        if(this.serverType != other.serverType) {
            return false;
        }
        if((this.fileKey == null) ? (other.fileKey != null) : !this.fileKey.equals(other.fileKey)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 53 * hash + (this.serverType != null ? this.serverType.hashCode() : 0);
        hash = 53 * hash + (this.fileKey != null ? this.fileKey.hashCode() : 0);
        return hash;
    }

    public static H2Server newInMemoryServer() {
        return newInMemoryServer(null);
    }

    public static H2Server newInMemoryServer(String catalog) {
        return newInMemoryServer(catalog, "SA", null);
    }

    public static H2Server newInMemoryServer(String catalog, String username, String password) {
        return new H2Server(H2ServerType.IN_MEMORY, null, 0, catalog, username, password, null);
    }

    public static H2Server newLocalASEEncryptedFileServer(String databasePath, String fileKey) {
        return newLocalASEEncryptedFileServer(databasePath, fileKey, "SA", null);
    }

    public static H2Server newLocalASEEncryptedFileServer(String databasePath, String fileKey, String username, String password) {
        return new H2Server(H2ServerType.ENCRYPTED_FILE_AES, null, 0, databasePath, username, password, fileKey);
    }

    public static H2Server newLocalXTEAEncryptedFileServer(String databasePath, String fileKey) {
        return newLocalXTEAEncryptedFileServer(databasePath, fileKey, "SA", null);
    }

    public static H2Server newLocalXTEAEncryptedFileServer(String databasePath, String fileKey, String username, String password) {
        return new H2Server(H2ServerType.ENCRYPTED_FILE_XTEA, null, 0, databasePath, username, password, fileKey);
    }

    public static H2Server newLocalFileServer(String databasePath) {
        return newLocalFileServer(databasePath, "SA", null);
    }

    public static H2Server newLocalFileServer(String databasePath, String username, String password) {
        return new H2Server(H2ServerType.LOCAL_FILE, null, 0, databasePath, username, password, null);
    }

    public static H2Server newSSLServer(String hostname, String catalog, String username, String password) {
        return newSSLServer(hostname, "9092", catalog, username, password);
    }

    public static H2Server newSSLServer(String hostname, String port, String catalog, String username, String password) {
        return newSSLServer(hostname, Integer.parseInt(port), catalog, username, password);
    }

    public static H2Server newSSLServer(String hostname, int port, String catalog, String username, String password) {
        return new H2Server(H2ServerType.REMOTE_SSL, hostname, port, catalog, username, password, null);
    }

    public static H2Server newTCPServer(String hostname, String catalog, String username, String password) {
        return newTCPServer(hostname, "9092", catalog, username, password);
    }

    public static H2Server newTCPServer(String hostname, String port, String catalog, String username, String password) {
        return newTCPServer(hostname, Integer.parseInt(port), catalog, username, password);
    }

    public static H2Server newTCPServer(String hostname, int port, String catalog, String username, String password) {
        return new H2Server(H2ServerType.REMOTE_TCP, hostname, port, catalog, username, password, null);
    }

    @Override
    protected DefaultDatabaseConnection createDatabaseConnection() {
        return new H2DatabaseConnectionPool(serverType, getCatalog(), this);
    }

    public static class H2DatabaseConnectionPool extends DefaultDatabaseConnection {

        private final H2ServerType serverType;
        private final String catalog;

        public H2DatabaseConnectionPool(H2ServerType serverType, String catalog, DefaultDatabaseServer databaseServer) {
            super(databaseServer);
            this.serverType = serverType;
            this.catalog = catalog;
        }

        @Override
        public void setPoolSize(int poolSize) {
            //Local file doesn't support multiple connections
            if(serverType == H2ServerType.LOCAL_FILE
                    || serverType == H2ServerType.ENCRYPTED_FILE_AES
                    || serverType == H2ServerType.ENCRYPTED_FILE_XTEA) {
                return;
            }

            //Anonymous in-memory doesn't support multiple connections either
            if(serverType == H2ServerType.IN_MEMORY
                    && catalog == null) {
                return;
            }

            super.setPoolSize(poolSize);
        }
    }

    public static class Factory extends DatabaseServerFactory {

        @Override
        public DatabaseServer createDatabaseServer(String hostname, int port, String catalog, String username, String password) {
            return new H2Server(H2ServerType.REMOTE_TCP, hostname, port, catalog, username, password, null);
        }
    }
}
