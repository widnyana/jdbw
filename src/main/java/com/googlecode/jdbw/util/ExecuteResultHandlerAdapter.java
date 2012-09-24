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

package com.googlecode.jdbw.util;

import com.googlecode.jdbw.ExecuteResultHandler;
import java.sql.SQLWarning;
import java.util.List;

/**
 * This is a convenience class that implements ExecuteResultHandler and provides
 * a default method implementation for this interface. You can then override 
 * only the methods that you care about.
 * @author Martin Berglund
 */
public class ExecuteResultHandlerAdapter implements ExecuteResultHandler
{
    @Override
    public int getMaxRowsToFetch()
    {
        return 0;   //Fetch all
    }

    @Override
    public boolean nextResultSet()
    {
        return true;
    }

    @Override
    public boolean nextRow(Object[] row)
    {
        return true;
    }

    @Override
    public void onDone()
    {
    }

    @Override
    public void onGeneratedKey(Object object)
    {
    }

    @Override
    public void onResultSet(List<String> columnNames, List<Integer> columnTypes)
    {
    }

    @Override
    public void onUpdateCount(int updateCount)
    {
    }

    @Override
    public void onWarning(SQLWarning warning)
    {
    }
}
