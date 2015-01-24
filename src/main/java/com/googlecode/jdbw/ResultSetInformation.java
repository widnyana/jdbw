package com.googlecode.jdbw;

import com.googlecode.jdbw.metadata.Column;
import java.sql.ResultSetMetaData;
import java.util.List;

/**
 * This interface contains extracted values from ResultSetMetaData describing a ResultSet that JDBW has received. It can
 * be safely examined without requiring to catch SQLExceptions and it doesn't do any round-trips to the database server
 * for any of these method calls.
 * @author Martin Berglund
 */
public interface ResultSetInformation {
    /**
     * Returns the underlying ResultSetMetaData object that was used to construct this ResultSetInformation.
     * @return ResultSetMetaData that was used to construct this ResultSetInformation
     */
    ResultSetMetaData getResultSetMetaData();

    /**
     * For queries with only one ResultSet, this always returns 0. For queries with multiple ResultSets, this will be 0
     * for the first, 1 for the second and so on.
     * @return Index of the ResultSet in a multi-ResultSet context
     */
    int getResultSetIndex();

    /**
     * Returns how many columns the result set has.
     * @return How many columns has the ResultSet
     */
    int getNumberOfColumns();

    /**
     * Returns the name (or label) of the columnIndex:th column (first column has index 0)
     * @param columnIndex Index of the column to get the name for
     * @return Name of the column with index columnIndex.
     * @throws java.lang.ArrayIndexOutOfBoundsException If columnIndex was out of bounds
     */
    String getColumnLabel(int columnIndex) throws ArrayIndexOutOfBoundsException;

    /**
     * Returns all the label of all columns in the result set, ordered. This list is not modifiable.
     * @return List of all column names
     */
    List<String> getColumnLabels();

    /**
     * Returns the type code, as specified by java.sql.Types, of the column with index columnIndex (first column has
     * index 0).
     * @param columnIndex Index of the column to get the type for
     * @return Constant from java.sql.Types representing the column's data type
     * @throws ArrayIndexOutOfBoundsException If columnIndex was out of bounds
     */
    int getColumnSQLType(int columnIndex) throws ArrayIndexOutOfBoundsException;

    /**
     * Returns all the columns in the result set in Column format. This representation contains more meta data that
     * isn't available unless you retrieve the ResultSetMetaData directly.
     * @return List of the columns in the result set
     * @see Column
     */
    List<Column> getColumns();
}
