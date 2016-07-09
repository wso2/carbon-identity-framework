/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.core.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;

/**
 * This class handles identity database creation in the first start-up. It checks for the
 * SQL scripts for creating the tables inside $CARBON_HOME/dbscripts/identity directory.
 */
public class IdentityDBInitializer {

    private static final String DB_CHECK_SQL = "SELECT * FROM IDN_BASE_TABLE";
    private static Log log = LogFactory.getLog(IdentityDBInitializer.class);
    Statement statement;
    private DataSource dataSource;
    private String delimiter = ";";

    IdentityDBInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static String getDatabaseType(Connection conn) throws IdentityRuntimeException{
        String type = null;
        try {
            if (conn != null && (!conn.isClosed())) {
                DatabaseMetaData metaData = conn.getMetaData();
                String databaseProductName = metaData.getDatabaseProductName();
                if (databaseProductName.matches("(?i).*hsql.*")) {
                    type = "hsql";
                } else if (databaseProductName.matches("(?i).*derby.*")) {
                    type = "derby";
                } else if (databaseProductName.matches("(?i).*mysql.*")) {
                    type = "mysql";
                } else if (databaseProductName.matches("(?i).*oracle.*")) {
                    type = "oracle";
                } else if (databaseProductName.matches("(?i).*microsoft.*")) {
                    type = "mssql";
                } else if (databaseProductName.matches("(?i).*h2.*")) {
                    type = "h2";
                } else if (databaseProductName.matches("(?i).*db2.*")) {
                    type = "db2";
                } else if (databaseProductName.matches("(?i).*postgresql.*")) {
                    type = "postgresql";
                } else if (databaseProductName.matches("(?i).*openedge.*")) {
                    type = "openedge";
                } else if (databaseProductName.matches("(?i).*informix.*")) {
                    type = "informix";
                } else {
                    String msg = "Unsupported database: " + databaseProductName +
                            ". Database will not be created automatically by the WSO2 Identity Server. " +
                            "Please create the database using appropriate database scripts for " +
                            "the database.";
                    throw IdentityRuntimeException.error(msg);
                }
                if (type != null && type.equals("mysql")) {
                    int databaseMajorVersion = metaData.getDatabaseMajorVersion();
                    int databaseMinorVersion = metaData.getDatabaseMinorVersion();
                    if (databaseMajorVersion == 5 && databaseMinorVersion >= 7) {
                        type = type + "-5.7+";
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Failed to create identity database." + e.getMessage();
            throw IdentityRuntimeException.error(msg, e);
        }
        return type;
    }

    /**
     * Checks that a string buffer ends up with a given string. It may sound
     * trivial with the existing
     * JDK API but the various implementation among JDKs can make those
     * methods extremely resource intensive
     * and perform poorly due to massive memory allocation and copying. See
     *
     * @param buffer the buffer to perform the check on
     * @param suffix the suffix
     * @return <code>true</code> if the character sequence represented by the
     * argument is a suffix of the character sequence represented by
     * the StringBuffer object; <code>false</code> otherwise. Note that the
     * result will be <code>true</code> if the argument is the
     * empty string.
     */
    public static boolean checkStringBufferEndsWith(StringBuffer buffer, String suffix) {
        if (suffix.length() > buffer.length()) {
            return false;
        }
        // this loop is done on purpose to avoid memory allocation performance
        // problems on various JDKs
        // StringBuffer.lastIndexOf() was introduced in jdk 1.4 and
        // implementation is ok though does allocation/copying
        // StringBuffer.toString().endsWith() does massive memory
        // allocation/copying on JDK 1.5
        // See http://issues.apache.org/bugzilla/show_bug.cgi?id=37169
        int endIndex = suffix.length() - 1;
        int bufferIndex = buffer.length() - 1;
        while (endIndex >= 0) {
            if (buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
                return false;
            }
            bufferIndex--;
            endIndex--;
        }
        return true;
    }

    void createIdentityDatabase() {
        if (!isDatabaseStructureCreated()) {
            Connection conn = null;
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                statement = conn.createStatement();
                executeSQLScript();
                conn.commit();
                log.debug("Identity tables are created successfully.");
            } catch (SQLException e) {
                String msg = "Failed to create database tables for Identity meta-data store. " + e.getMessage();
                throw IdentityRuntimeException.error(msg, e);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        log.error("Failed to close statement.", e);
                    }
                }

                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        log.error("Failed to close database connection.", e);
                    }
                }

            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Identity Database already exists. Not creating a new database.");
            }
        }
    }

    /**
     * Checks whether database tables are created.
     *
     * @return <code>true</core> if checkSQL is success, else <code>false</code>.
     */
    private boolean isDatabaseStructureCreated() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Running a query to test the database tables existence");
            }
            // check whether the tables are already created with a query
            Connection conn = dataSource.getConnection();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(DB_CHECK_SQL);
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } finally {
                    if (conn != null) {
                        conn.close();
                    }
                }
            }
        } catch (SQLException e) {
            // Doesn't matter if DB scripts are executed again, because doesn't do any harm.
            return false;
        }

        return true;

    }

    private void executeSQLScript() {

        String databaseType = null;
        try {
            databaseType = IdentityDBInitializer.getDatabaseType(dataSource.getConnection());
        } catch (Exception e) {
            throw IdentityRuntimeException.error("Error occurred while getting database type");
        }
        boolean keepFormat = false;
        if ("oracle".equals(databaseType)) {
            delimiter = "/";
        } else if ("db2".equals(databaseType)) {
            delimiter = "/";
        } else if ("openedge".equals(databaseType)) {
            delimiter = "/";
            keepFormat = true;
        }

        String dbScriptLocation = getDbScriptLocation(databaseType);

        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;

        try {
            InputStream is = new FileInputStream(dbScriptLocation);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!keepFormat) {
                    if (line.startsWith("//")) {
                        continue;
                    }
                    if (line.startsWith("--")) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(line);
                    if (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if ("REM".equalsIgnoreCase(token)) {
                            continue;
                        }
                    }
                }
                sql.append(keepFormat ? "\n" : " ").append(line);

                // SQL defines "--" as a comment to EOL
                // and in Oracle it may contain a hint
                // so we cannot just remove it, instead we must end it
                if (!keepFormat && line.contains("--")) {
                    sql.append("\n");
                }
                if ((checkStringBufferEndsWith(sql, delimiter))) {
                    executeSQL(sql.substring(0, sql.length() - delimiter.length()));
                    sql.replace(0, sql.length(), "");
                }
            }
            // Catch any statements not followed by ;
            if (sql.length() > 0) {
                executeSQL(sql.toString());
            }
        } catch (IOException e) {
            throw IdentityRuntimeException.error("Error occurred while executing SQL script for creating identity " +
                    "database", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing stream for Identity SQL script", e);
                }
            }
        }
    }

    protected String getDbScriptLocation(String databaseType) {
        String scriptName = databaseType + ".sql";
        if (log.isDebugEnabled()) {
            log.debug("Loading database script from :" + scriptName);
        }
        String carbonHome = System.getProperty("carbon.home");
        return carbonHome +
                "/dbscripts/identity/" + scriptName;
    }

    /**
     * executes given sql
     *
     * @param sql
     * @throws Exception
     */
    private void executeSQL(String sql) {
        // Check and ignore empty statements
        if ("".equals(sql.trim())) {
            return;
        }

        ResultSet resultSet = null;
        Connection conn = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("SQL : " + sql);
            }

            boolean ret;
            int updateCount, updateCountTotal = 0;
            ret = statement.execute(sql);
            updateCount = statement.getUpdateCount();
            resultSet = statement.getResultSet();
            do {
                if (!ret) {
                    if (updateCount != -1) {
                        updateCountTotal += updateCount;
                    }
                }
                ret = statement.getMoreResults();
                if (ret) {
                    updateCount = statement.getUpdateCount();
                    resultSet = statement.getResultSet();
                }
            } while (ret);

            if (log.isDebugEnabled()) {
                log.debug(sql + " : " + updateCountTotal + " rows affected");
            }
            conn = dataSource.getConnection();
            SQLWarning warning = conn.getWarnings();
            while (warning != null) {
                log.debug(warning + " sql warning");
                warning = warning.getNextWarning();
            }
            conn.clearWarnings();
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32") || e.getSQLState().equals("42710")) {
                // eliminating the table already exception for the derby and DB2 database types
                if (log.isDebugEnabled()) {
                    log.info("Table Already Exists. Hence, skipping table creation");
                }
            } else {
                throw IdentityRuntimeException.error("Error occurred while executing : " + sql, e);
            }
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing result set.", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing sql connection.", e);
                }
            }
        }
    }

}
