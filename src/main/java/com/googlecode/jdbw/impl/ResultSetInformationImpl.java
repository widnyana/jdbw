package com.googlecode.jdbw.impl;

import com.googlecode.jdbw.ResultSetInformation;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of ResultSetInformation
 * @author Martin Berglund
 */
public class ResultSetInformationImpl implements ResultSetInformation {

    private final ResultSetMetaData resultSetMetaData;
    private final List<String> columnLabels;
    private final List<Integer> columnTypes;
    private final int resultSetIndex;

    public ResultSetInformationImpl(ResultSetMetaData resultSetMetaData, int index) throws SQLException {
        this.resultSetMetaData = resultSetMetaData;
        this.resultSetIndex = index;
        this.columnLabels = new ArrayList<String>(resultSetMetaData.getColumnCount());
        this.columnTypes = new ArrayList<Integer>(resultSetMetaData.getColumnCount());

        for(int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
            columnLabels.add(resultSetMetaData.getColumnLabel(i + 1));
            columnTypes.add(resultSetMetaData.getColumnType(i + 1));
        }
    }

    @Override
    public ResultSetMetaData getResultSetMetaData() {
        return resultSetMetaData;
    }

    @Override
    public int getResultSetIndex() {
        return resultSetIndex;
    }

    @Override
    public int getNumberOfColumns() {
        return columnLabels.size();
    }

    @Override
    public String getColumnLabel(int columnIndex) throws ArrayIndexOutOfBoundsException {
        return columnLabels.get(columnIndex);
    }

    @Override
    public List<String> getColumnLabels() {
        return Collections.unmodifiableList(columnLabels);
    }

    @Override
    public int getColumnSQLType(int columnIndex) throws ArrayIndexOutOfBoundsException {
        return columnTypes.get(columnIndex);
    }
}
