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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Utility class for database operations.
 */
public class IdentityDatabaseUtil {

    private static final Log log = LogFactory.getLog(IdentityDatabaseUtil.class);

    /**
     * Get a database connection instance from the Identity Persistence Manager
     *
     * @return Database Connection
     * @throws IdentityRuntimeException Error when getting a database connection to Identity database
     */
    public static Connection getDBConnection() throws IdentityRuntimeException {
        return JDBCPersistenceManager.getInstance().getDBConnection();
    }

    /**
     * Get database source instance from the Identity Persistence Manager
     *
     * @return Database Source
     */
    public static DataSource getDataSource() {

        return JDBCPersistenceManager.getInstance().getDataSource();
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

    public static void rollBack(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions. ", e1);
        }
    }

    /**
     * Get a database connection instance for the User DB
     *
     * @return Database Connection
     * @throws IdentityRuntimeException Error when getting a database connection to Identity database
     */
    public static Connection getUserDBConnection() throws IdentityRuntimeException {
        Connection connection;
        try {
            connection = UmPersistenceManager.getInstance().getDataSource().getConnection();
        } catch (SQLException e) {
            throw IdentityRuntimeException.error("Database error. Could not get a connection", e);
        }
        return connection;
    }

}
