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
package org.wso2.carbon.identity.user.store.count.jdbc.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.store.count.AbstractUserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class that retrieve the role counts from internal domains
 */
public class InternalCountRetriever extends AbstractUserStoreCountRetriever {
    private static final Log log = LogFactory.getLog(InternalCountRetriever.class);
    private RealmConfiguration realmConfiguration = null;
    private int tenantId = -1234;

    public InternalCountRetriever() {

    }

    public void init(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
        this.tenantId = realmConfiguration.getTenantId();
    }

    public Long countRoles(String filter) throws UserStoreCounterException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = getDBConnection(false);
            if (filter.startsWith(UserCoreConstants.INTERNAL_DOMAIN)) {
                sqlStmt = InternalStoreCountConstants.COUNT_INTERNAL_ONLY_ROLES_SQL;
                filter = filter.replace(UserCoreConstants.INTERNAL_DOMAIN, "");
            } else {
                sqlStmt = InternalStoreCountConstants.COUNT_INTERNAL_ROLES_SQL;
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("RESULT");
            } else {
                log.error("No role count is retrieved for Internal domain filter:" + filter);
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

    public Long countInternalRoles(String filter) throws UserStoreCounterException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = getDBConnection(false);
            sqlStmt = InternalStoreCountConstants.COUNT_INTERNAL_ROLES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("RESULT");
            } else {
                log.error("No role count is retrieved for Internal domain filter:" + filter);
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

    private Connection getDBConnection(boolean shouldApplyTransaction) throws SQLException, UserStoreException {

        Connection dbConnection = IdentityDatabaseUtil.getUserDBConnection(shouldApplyTransaction);
        if (dbConnection == null) {
            throw new UserStoreException("Could not create a database connection to User database");
        }
        return dbConnection;
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param dbConnection database connection.
     */
    private void rollbackTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions. ", e1);
        }
    }
}
