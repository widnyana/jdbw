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
package com.googlecode.jdbw.metadata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mabe02
 */
public abstract class MetaDataResolver {

    protected abstract MetaDataFactory getMetaDataFactory();

    protected abstract List<String> getCatalogNames() throws SQLException;

    protected abstract List<String> getSchemaNames(String catalogName) throws SQLException;

    protected abstract List<String> getUserTableNames(String catalogName, String schemaName) throws SQLException;

    protected abstract List<String> getSystemTableNames(String catalogName, String schemaName) throws SQLException;

    protected abstract List<String> getViewNames(String catalogName, String schemaName) throws SQLException;

    protected abstract List<String> getStoredProcedureNames(String catalogName, String schemaName) throws SQLException;

    protected abstract List<String> getFunctionNames(String catalogName, String schemaName) throws SQLException;

    protected abstract List<Column> getColumns(String catalogName, String schemaName, Table table) throws SQLException;

    protected abstract List<Index> getIndexes(String catalogName, String schemaName, Table table) throws SQLException;

    protected abstract List<String> getProcedureInputParameterNames(String catalogName, String schemaName, StoredProcedure procedure) throws SQLException;

    public List<Catalog> getCatalogs() throws SQLException {
        List<Catalog> result = new ArrayList<Catalog>();
        for(String catalogName : getCatalogNames()) {
            result.add(getMetaDataFactory().createCatalog(catalogName));
        }
        return result;
    }

    public Catalog getCatalog(String catalogName) throws SQLException {
        List<Catalog> catalogs = getCatalogs();
        for(Catalog catalog : catalogs) {
            if(catalog.getName().equals(catalogName)) {
                return catalog;
            }
        }

        for(Catalog catalog : catalogs) {
            if(catalog.getName().toLowerCase().equals(catalogName.toLowerCase())) {
                return catalog;
            }
        }

        return null;
    }

    public String getStoredProcedureCode(String catalogName, String schemaName, String procedureName) throws SQLException {
        return "";
    }
}
