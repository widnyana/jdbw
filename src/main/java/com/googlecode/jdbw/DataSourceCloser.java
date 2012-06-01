/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.jdbw;

import javax.sql.DataSource;

/**
 *
 * @author mberglun
 */
public interface DataSourceCloser {
    void closeDataSource(DataSource dataSource);
}
