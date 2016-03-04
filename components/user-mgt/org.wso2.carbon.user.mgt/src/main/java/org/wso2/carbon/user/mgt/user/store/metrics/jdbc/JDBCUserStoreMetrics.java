/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.mgt.user.store.metrics.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.mgt.user.store.metrics.AbstractUserStoreMetrics;
import org.wso2.carbon.user.mgt.user.store.metrics.exception.UserStoreMetricsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class JDBCUserStoreMetrics extends AbstractUserStoreMetrics {

    private static Log log = LogFactory.getLog(JDBCUserStoreMetrics.class);
    private final RealmConfiguration realmConfiguration;
    private final int tenantId;


    public JDBCUserStoreMetrics(RealmConfiguration realmConfiguration, int tenantId) throws UserStoreException {
        this.realmConfiguration = realmConfiguration;
        this.tenantId = tenantId;
    }

    @Override
    public Long countUsersInDomain(String filter, String domain) throws UserStoreMetricsException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = getDBConnection(realmConfiguration);
            sqlStmt = JDBCUserStoreMetricsConstants.COUNT_USERS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("COUNT(UM_ID)");
            } else {
                log.error("No count is retrieved from the user store");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreMetricsException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UserStoreMetricsException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
    }

    @Override
    public Long countRolesInDomain(String filter, String domain) throws UserStoreMetricsException {
        return null;
    }

    @Override
    public Long countClaimInDomain(String claimURI, String valueFilter, String domain) throws UserStoreMetricsException {
        return null;
    }

    @Override
    public Long countClaimsInDomain(Map<String, String> claimSetToFilter, String domain) throws UserStoreMetricsException {
        return null;
    }

    private Connection getDBConnection(RealmConfiguration realmConfiguration) throws SQLException, UserStoreException {
        Connection dbConnection = DatabaseUtil.getDBConnection(DatabaseUtil.getRealmDataSource(realmConfiguration));
        dbConnection.setAutoCommit(false);
        dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return dbConnection;
    }
}
