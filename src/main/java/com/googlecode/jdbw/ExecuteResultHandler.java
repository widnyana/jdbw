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
package com.googlecode.jdbw;

import java.sql.SQLWarning;
import java.util.List;

/**
 *
 * @author mabe02
 */
public interface ExecuteResultHandler {

    int getMaxRowsToFetch();

    void onResultSet(List<String> columnNames, List<Integer> columnTypes);

    boolean nextRow(Object[] row);

    boolean nextResultSet();

    void onUpdateCount(int updateCount);

    void onGeneratedKey(Object object);

    void onWarning(SQLWarning warning);

    void onDone();
}
