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

import javax.sql.DataSource;

/**
 * This interface is used for closing a {@code DataSource}. Since the {@code DataSource} interface doesn't expose any
 * close method, we need to delegate closing it to an external object. Normally, this object would be the same that both
 * creates and closes data sources, so it knows that implementation of {@code DataSource} that is used.
 * @author Martin Berglund
 */
public interface DataSourceCloser {
    /**
     * Called when a data source should be closed, trusting the implementing class knows how to close this
     * {@code DataSource} implementation.
     * @param dataSource Data source to close
     */
    void closeDataSource(DataSource dataSource);
}
