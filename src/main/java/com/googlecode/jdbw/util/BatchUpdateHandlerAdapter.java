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

import com.googlecode.jdbw.BatchUpdateHandler;

import java.sql.SQLWarning;

/**
 * This is a convenience class that implements BatchUpdateHandler and provides a default method implementation for this
 * interface. You can then override only the methods that you care about.
 *
 * @author Martin Berglund
 */
public class BatchUpdateHandlerAdapter implements BatchUpdateHandler {
    @Override
    public void onGeneratedKey(Object object) {
    }

    @Override
    public void onWarning(SQLWarning warning) {
    }

    @Override
    public void onBatchResult(int[] returnCodes) {
    }
}
