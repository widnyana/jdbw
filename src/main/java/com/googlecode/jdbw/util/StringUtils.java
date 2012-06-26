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

package com.googlecode.jdbw.util;

import java.util.List;

/**
 * You shouldn't need to care about this class, we're just using it internally
 * for some common string operations.
 * @author mabe02
 */
public class StringUtils
{
    public static String concatenateStringList(List<String> list, String separator)
    {
        StringBuilder sb = new StringBuilder();
        for(String replicatedDatabase: list)
            sb.append(replicatedDatabase).append(separator);
        sb.delete(sb.length()-separator.length(), sb.length());
        return sb.toString();
    }
}
