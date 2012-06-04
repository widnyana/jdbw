/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.googlecode.jdbw.util;

import com.googlecode.jdbw.ExecuteResultHandler;
import java.sql.SQLWarning;
import java.util.List;

/**
 *
 * @author mberglun
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
