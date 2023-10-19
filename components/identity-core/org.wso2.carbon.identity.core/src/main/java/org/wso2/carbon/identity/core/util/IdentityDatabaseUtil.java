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

package org.wso2.carbon.identity.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.persistence.UmPersistenceManager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Utility class for database operations.
 */
public class IdentityDatabaseUtil {

    private static final Log log = LogFactory.getLog(IdentityDatabaseUtil.class);

    @Deprecated
    public static Connection getDBConnection() throws IdentityRuntimeException {

        return getDBConnection(true);
    }

    /**
     * Get a database connection instance from the Identity Persistence Manager
     *
     * @return Database Connection
     * @throws IdentityRuntimeException Error when getting a database connection to Identity database
     */
    public static Connection getDBConnection(boolean shouldApplyTransaction) throws IdentityRuntimeException {

        return JDBCPersistenceManager.getInstance().getDBConnection(shouldApplyTransaction);
    }

    /**
     * Get a database connection instance from the Session Persistence.
     *
     * @return Database Connection
     * @throws IdentityRuntimeException Error when getting a database connection to Session database
     */
    public static Connection getSessionDBConnection(boolean shouldApplyTransaction) throws IdentityRuntimeException {

        return JDBCPersistenceManager.getInstance().getSessionDBConnection(shouldApplyTransaction);
    }

    /**
     * Get database source instance from the Identity Persistence Manager.
     *
     * @return Database Source
     */
    public static DataSource getDataSource() {

        return JDBCPersistenceManager.getInstance().getDataSource();
    }

    /**
     * Get session database source instance from the Identity Persistence Manager.
     *
     * @return Database Source
     */
    public static DataSource getSessionDataSource() {

        return JDBCPersistenceManager.getInstance().getSessionDataSource();
    }


    public static void closeAllConnections(Connection dbConnection, ResultSet rs, PreparedStatement prepStmt) {

        closeResultSet(rs);
        closeStatement(prepStmt);
        closeConnection(dbConnection);
    }

    public static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage(), e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close result set  - " + e.getMessage(), e);
            }
        }

    }

    public static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage(), e);
            }
        }

    }

    @Deprecated
    public static void rollBack(Connection dbConnection) {

        rollbackTransaction(dbConnection);
    }

    public static void rollbackTransaction(Connection dbConnection) {

        JDBCPersistenceManager.getInstance().rollbackTransaction(dbConnection);
    }

    public static void commitTransaction(Connection dbConnection) {

        JDBCPersistenceManager.getInstance().commitTransaction(dbConnection);
    }

    /**
     * Get a database connection instance for the User DB
     *
     * @return Database Connection
     * @throws IdentityRuntimeException Error when getting a database connection to Identity database
     * @Deprecated The logic is improved with the transaction isolation. Hence deprecating this method to
     * use {@link IdentityDatabaseUtil#getUserDBConnection(boolean)} method.
     */
    @Deprecated
    public static Connection getUserDBConnection() throws IdentityRuntimeException {
        Connection connection;
        try {
            connection = UmPersistenceManager.getInstance().getDataSource().getConnection();
        } catch (SQLException e) {
            throw IdentityRuntimeException.error("Database error. Could not get a connection", e);
        }
        return connection;
    }

    /**
     * Get a database connection instance from the Identity Persistence Manager
     *
     * @return Database Connection
     * @throws IdentityRuntimeException Error when getting a database connection to Identity database
     */
    public static Connection getUserDBConnection(boolean shouldApplyTransaction) throws IdentityRuntimeException {

        return UmPersistenceManager.getInstance().getDBConnection(shouldApplyTransaction);
    }

    /**
     * Commit the User DB transaction.
     *
     * @param dbConnection Database Connection.
     */
    public static void commitUserDBTransaction(Connection dbConnection) {

        UmPersistenceManager.getInstance().commitTransaction(dbConnection);
    }

    /**
     * Rollback the User DB transaction.
     *
     * @param dbConnection Database Connection.
     */
    public static void rollbackUserDBTransaction(Connection dbConnection) {

        UmPersistenceManager.getInstance().rollbackTransaction(dbConnection);
    }

    /**
     * Check whether the specified table exists in the Identity database.
     *
     * @param tableName The name of the table.
     * @return true if table exists.
     */
    public static boolean isTableExists(String tableName) {

        try (Connection connection = getDBConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData.storesLowerCaseIdentifiers()) {
                tableName = tableName.toLowerCase();
            }
            String schemaName = connection.getSchema();
            String catalogName = connection.getCatalog();
            try (ResultSet resultSet = metaData.getTables(catalogName, schemaName, tableName, new String[]{"TABLE"})) {
                if (resultSet.next()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Table - " + tableName + " available in the Identity database.");
                    }
                    commitTransaction(connection);
                    return true;
                }
                commitTransaction(connection);
            } catch (SQLException e) {
                rollbackTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug("Table - " + tableName + " not available in the Identity database.");
                }
                return false;
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Table - " + tableName + " not available in the Identity database.");
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("Table - " + tableName + " not available in the Identity database.");
        }
        return false;
    }
}
