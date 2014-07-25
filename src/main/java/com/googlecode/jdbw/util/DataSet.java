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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Martin Berglund
 * @param <V>
 */
public class DataSet<V> implements Iterable<V[]> {

    private final List<String> columnNames;
    private final List<V[]> rows;

    private DataSet(List<String> columnNames, List<V[]> rows) {
        this.columnNames = Collections.unmodifiableList(new ArrayList<String>(columnNames));
        this.rows = Collections.unmodifiableList(new ArrayList<V[]>(rows));
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    @Override
    public Iterator<V[]> iterator() {
        return rows.iterator();
    }

    @Override
    public String toString() {
        List<Integer> columnSizes = new ArrayList<Integer>(columnNames.size());
        for (String columnName : columnNames) {
            columnSizes.add(columnName.length());
        }
        for(V[] row: rows) {
            for(int i = 0; i < row.length; i++) {
                String element = "<null>";
                if(row[i] != null) {
                    element = row[i].toString();
                }

                if(columnSizes.get(i) < element.length()) {
                    columnSizes.set(i, element.length());
                }
            }
        }

        StringBuilder bob = new StringBuilder();
        //Header
        bob.append("+");
        for(Integer length: columnSizes) {
            for(int i = 0; i < length; i++) {
                bob.append("-");
            }
            bob.append("+");
        }
        bob.append("\n");
        bob.append("|");
        for(int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            bob.append(columnName);
            for(int j = columnName.length(); j < columnSizes.get(i); j++) {
                bob.append(" ");
            }
            bob.append("|");
        }
        bob.append("\n");
        bob.append("+");
        for(Integer length: columnSizes) {
            for(int i = 0; i < length; i++) {
                bob.append("-");
            }
            bob.append("+");
        }
        bob.append("\n");
        //Rows
        for(V[] row: rows) {
            bob.append("|");
            for(int i = 0; i < row.length; i++) {
                V item = row[i];
                String element = "<null>";
                if(item != null) {
                    element = item.toString();
                }
                bob.append(element);
                for(int j = element.length(); j < columnSizes.get(i); j++) {
                    bob.append(" ");
                }
                bob.append("|");
            }
            bob.append("\n");
        }
        //Footer
        bob.append("+");
        for(Integer length: columnSizes) {
            for(int i = 0; i < length; i++) {
                bob.append("-");
            }
            bob.append("+");
        }
        bob.append("\n");
        return bob.toString();
    }

    public static class Builder<V> {
        private final List<String> columnNames;
        private final List<V[]> rows;

        public Builder(List<String> columnNames) {
            this.columnNames = Collections.unmodifiableList(new ArrayList<String>(columnNames));
            this.rows = new ArrayList<V[]>();
        }

        public void addRow(V[] row) {
            if(row.length != columnNames.size()) {
                throw new IllegalArgumentException("Incorrect size, expected " + columnNames.size() + " values but got " + row.length);
            }
            rows.add(row);
        }

        public DataSet<V> build() {
            return new DataSet<V>(columnNames, rows);
        }
    }
}
