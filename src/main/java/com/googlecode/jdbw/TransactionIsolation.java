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

import java.sql.Connection;

/**
 * This enum contains the various levels of transactional isolation supported by
 * JDBC. These values are all derived from constants on the 
 * {@code java.sql.Connection} class.
 * 
 * For more information, please see
 * <a href="http://en.wikipedia.org/wiki/Transaction_isolation">wikipedia</a>.
 * @author mabe02
 */
public enum TransactionIsolation {

    /**
     * Taken from <a href="http://docs.oracle.com/javase/6/docs/api/java/sql/Connection.html">Java 6 API documentation:</a>
     * A constant indicating that dirty reads are prevented; non-repeatable
     * reads and phantom reads can occur. This level only prohibits a
     * transaction from reading a row with uncommitted changes in it.
     */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED, "Read committed"),
    /**
     * Taken from <a href="http://docs.oracle.com/javase/6/docs/api/java/sql/Connection.html">Java 6 API documentation:</a>
     * A constant indicating that dirty reads, non-repeatable reads and phantom
     * reads can occur. This level allows a row changed by one transaction to be
     * read by another transaction before any changes in that row have been
     * committed (a "dirty read"). If any of the changes are rolled back, the
     * second transaction will have retrieved an invalid row.
     */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED, "Read uncommited"),
    /**
     * Taken from <a href="http://docs.oracle.com/javase/6/docs/api/java/sql/Connection.html">Java 6 API documentation:</a>
     * A constant indicating that dirty reads and non-repeatable reads are
     * prevented; phantom reads can occur. This level prohibits a transaction
     * from reading a row with uncommitted changes in it, and it also prohibits
     * the situation where one transaction reads a row, a second transaction
     * alters the row, and the first transaction rereads the row, getting
     * different values the second time (a "non-repeatable read").
     */
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ, "Repeatable read"),
    /**
     * Taken from <a href="http://docs.oracle.com/javase/6/docs/api/java/sql/Connection.html">Java 6 API documentation:</a>
     * A constant indicating that dirty reads, non-repeatable reads and phantom
     * reads are prevented. This level includes the prohibitions in
     * <code>TRANSACTION_REPEATABLE_READ</code> and further prohibits the
     * situation where one transaction reads all rows that satisfy a
     * <code>WHERE</code> condition, a second transaction inserts a row that
     * satisfies that
     * <code>WHERE</code> condition, and the first transaction rereads for the
     * same condition, retrieving the additional "phantom" row in the second
     * read.
     */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE, "Serializable");
    
    private final int level;
    private final String label;

    private TransactionIsolation(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int getConstant() {
        return level;
    }

    @Override
    public String toString() {
        return label;
    }

    public static TransactionIsolation fromLabel(String label) {
        for(TransactionIsolation isolation : values()) {
            if(label.equals(isolation.label)) {
                return isolation;
            }
        }
        return null;
    }

    public static TransactionIsolation fromLevel(int level) {
        for(TransactionIsolation isolation : values()) {
            if(level == isolation.level) {
                return isolation;
            }
        }
        return null;
    }
}
