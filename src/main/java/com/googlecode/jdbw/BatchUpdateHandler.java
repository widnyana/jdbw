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

import com.googlecode.jdbw.util.BatchUpdateHandlerAdapter;
import java.sql.SQLWarning;

/**
 * This interface is used as a callback mechanism for handling results coming
 * out of a batch query. If you don't need all methods, you can override 
 * {@code BatchUpdateHandlerAdapter} instead.
 * @see BatchUpdateHandlerAdapter
 * @author mabe02
 */
public interface BatchUpdateHandler {

    /**
     * Callback method for SQLWarnings that occurred during the batch
     * @param warning
     */
    public void onWarning(SQLWarning warning);

    /**
     * Callback method for the return codes of the batch
     * @param returnCodes
     */
    public void onBatchResult(int []returnCodes);

    /**
     * Callback method for generated keys that happened during the batch.
     * Not all database engines supports this!
     * @param object
     */
    public void onGeneratedKey(Object object);
}
