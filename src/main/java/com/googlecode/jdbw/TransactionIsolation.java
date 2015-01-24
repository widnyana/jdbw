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

import java.sql.Connection;

/**
 * This enum contains the various levels of transactional isolation supported by JDBC. These values are all derived from
 * constants on the {@code java.sql.Connection} class.
 * 
 * For more information, please see
 * <a href="http://en.wikipedia.org/wiki/Transaction_isolation">wikipedia</a>.
 * @author Martin Berglund
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

    /**
     * Returns the integer value repressenting this isolation level in java.sql.Connection
     * @return Isolation level constant in java.sql.Connection
     */
    public int getConstant() {
        return level;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Given a label (same as what toString() returns), returns the matching isolation level object.
     * @param label Label to find the isolation level for
     * @return The isolation level this label is describing, or {@code null} if you passed in {@code null}
     * @throws IllegalArgumentException If the label passed in wasn't {@code null} and didn't match any of the isolation
     * levels
     */
    public static TransactionIsolation fromLabel(String label) {
        if(label == null) {
            return null;
        }
        for(TransactionIsolation isolation : values()) {
            if(label.equals(isolation.label)) {
                return isolation;
            }
        }
        throw new IllegalArgumentException("'" + label + "' is not a valid transaction isolation level");
    }

    /**
     * Returns the isolation level associated with an integer constant in java.sql.Connection describing isolation
     * levels. Will throw IllegalArgumentException if the level didn't match any of the constants in Connection.
     * @param level Level to get the {@TransactionIsolation} for
     * @return The isolation level object associated with this value
     * @throw IllegalArgumentException If the integer value passed in doesn't match any of the isolation level constants
     * in java.sql.Connection.
     */
    public static TransactionIsolation fromLevel(int level) {
        for(TransactionIsolation isolation : values()) {
            if(level == isolation.level) {
                return isolation;
            }
        }
        throw new IllegalArgumentException("Integer value " + level + " doesn't match any transaction isolation level in " +
                "java.sql.Connection");
    }
}
