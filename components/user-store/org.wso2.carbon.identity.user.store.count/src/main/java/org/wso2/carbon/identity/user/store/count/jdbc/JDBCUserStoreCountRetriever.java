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
package org.wso2.carbon.identity.user.store.count.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.store.count.AbstractUserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

public class JDBCUserStoreCountRetriever extends AbstractUserStoreCountRetriever {

    private static Log log = LogFactory.getLog(JDBCUserStoreCountRetriever.class);
    private RealmConfiguration realmConfiguration = null;
    private int tenantId = -1234;

    public JDBCUserStoreCountRetriever() {

    }

    public void init(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
        this.tenantId = realmConfiguration.getTenantId();
    }


    @Override
    public Long countUsers(String filter) throws UserStoreCounterException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = IdentityDatabaseUtil.getUserDBConnection();
            sqlStmt = JDBCUserStoreMetricsConstants.COUNT_USERS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, "%" + filter + "%");
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("COUNT(UM_ID)");
            } else {
                log.error("No user count is retrieved from the user store");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Using sql : " + sqlStmt);
            }
            throw new UserStoreCounterException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UserStoreCounterException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
    }

    @Override
    public Long countRoles(String filter) throws UserStoreCounterException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = IdentityDatabaseUtil.getUserDBConnection();
            sqlStmt = JDBCUserStoreMetricsConstants.COUNT_ROLES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, "%" + filter + "%");
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("COUNT(UM_ID)");
            } else {
                log.error("No role count is retrieved from the user store");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Using sql : " + sqlStmt);
            }
            throw new UserStoreCounterException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UserStoreCounterException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
    }

    @Override
    public Long countClaim(String claimURI, String valueFilter) throws UserStoreCounterException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = IdentityDatabaseUtil.getUserDBConnection();
            sqlStmt = JDBCUserStoreMetricsConstants.COUNT_CLAIM_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, claimURI);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, "%" + valueFilter + "%");
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("COUNT(UM_USER_ID)");
            } else {
                log.error("No claim count is retrieved from the user store");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Using sql : " + sqlStmt);
            }
            throw new UserStoreCounterException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UserStoreCounterException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
    }

    @Override
    public Long countClaims(Map<String, String> claimSetToFilter) throws UserStoreCounterException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = IdentityDatabaseUtil.getUserDBConnection();
            sqlStmt = JDBCUserStoreMetricsConstants.SELECT_COUNT_SQL;

            for (int i = 0; i < claimSetToFilter.size(); i++) {
                //if the last one to append
                if (i == claimSetToFilter.size() - 1) {
                    sqlStmt = sqlStmt + JDBCUserStoreMetricsConstants.SELECT_CLAIM_SQL + ")";
                } else {
                    sqlStmt = sqlStmt + JDBCUserStoreMetricsConstants.SELECT_CLAIM_SQL + " " +
                            JDBCUserStoreMetricsConstants.INTERSECT_SQL + " ";
                }
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            Iterator iterator = claimSetToFilter.entrySet().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();

                i = i++;
                prepStmt.setString(i, (String) pair.getKey());

                i = i++;
                prepStmt.setString(i, "%" + pair.getValue() + "%");

                i = i++;
                prepStmt.setInt(i, (Integer) pair.getValue());
            }

            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("COUNT(UM_USER_ID)");
            } else {
                log.error("No claim count is retrieved from the user store");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Using sql : " + sqlStmt);
            }
            throw new UserStoreCounterException(e.getMessage(), e);
        } catch (Exception e) {
            throw new UserStoreCounterException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
    }

}
