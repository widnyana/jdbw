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
 * Copyright (C) 2007-2012 mabe02
 */

package com.googlecode.jdbw;

import java.util.Properties;
import javax.sql.DataSource;

/**
 * This interface is used by a {@code DatabaseServer} to construct a {@code 
 * DataSource} from its configuration. Implementations of this class will 
 * typically use the configuration (jdbc url and extra properties) in 
 * combination with a third-party database connection pool library to 
 * instantiate a {@code DataSource} object. The implementation must also 
 * provide a way to close this data source.
 * @author mabe02
 */
public interface DataSourceCreator {
    /**
     * Creates a new {@code DataSource} using supplied connection configuration
     * @param jdbcUrl JDBC url to connect to
     * @param properties Extra connection properties (should contain username
     * and password, at least)
     * @return DataSource implementation created from the supplied configuration
     */
    DataSource newDataSource(String jdbcUrl, Properties properties);
    
    /**
     * Closes a {@code DataSource} previously created by newDataSource on this
     * object. 
     * @param previouslyConstructedDataSource Data source to close
     */
    void close(DataSource previouslyConstructedDataSource);
}
