/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.thrift.authentication.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.thrift.authentication.internal.generatedCode.AuthenticationException;
import org.wso2.carbon.identity.thrift.authentication.internal.persistance.ThriftAuthenticationJDBCPersistenceManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ThriftAuthenticationDatabaseUtil {
    private ThriftAuthenticationDatabaseUtil(){

    }

    private static final Log log = LogFactory.getLog(ThriftAuthenticationDatabaseUtil.class);

        @Deprecated
        public static Connection getDBConnection() throws AuthenticationException {
           return getDBConnection(true);
        }

    /**
     * Get a database connection instance from the Thrift Identity Persistence Manager
     *
     * @return Database Connection
     * @throws AuthenticationException Error when getting an instance of the identity Persistence Manager
     */
    public static Connection getDBConnection(Boolean shouldApplyTransaction) throws AuthenticationException {
        try {
            return ThriftAuthenticationJDBCPersistenceManager.getInstance().getDBConnection(shouldApplyTransaction);
        } catch (AuthenticationException e) {
            String errMsg = "Error when getting a database connection from the Thrift Identity Persistence Manager";
            log.error(errMsg, e);
            throw e;
        }
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
                log.error("Database error. Could not close statement. Continuing with others. - " +
                        e.getMessage(), e);
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
        try {
            ThriftAuthenticationJDBCPersistenceManager.getInstance().rollbackTransaction(dbConnection);
        } catch (AuthenticationException e) {
            String errMsg = "Error when rollback from the Thrift Identity Persistence Manager";
            log.error(errMsg, e);
        }
    }

    public static void commitTransaction(Connection dbConnection) {
        try {
            ThriftAuthenticationJDBCPersistenceManager.getInstance().commitTransaction(dbConnection);
        } catch (AuthenticationException e) {
            String errMsg = "Error when rollback from the Thrift Identity Persistence Manager";
            log.error(errMsg, e);
        }
    }

}
