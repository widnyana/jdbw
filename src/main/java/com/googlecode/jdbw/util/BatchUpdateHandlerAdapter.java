/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.googlecode.jdbw.util;

import com.googlecode.jdbw.BatchUpdateHandler;
import java.sql.SQLWarning;

/**
 *
 * @author mberglun
 */
public class BatchUpdateHandlerAdapter implements BatchUpdateHandler
{
    @Override
    public void onGeneratedKey(Object object)
    {
    }

    @Override
    public void onWarning(SQLWarning warning)
    {
    }

    @Override
    public void onBatchResult(int[] returnCodes)
    {
    }
}
