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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.store.count.AbstractUserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.internal.UserStoreCountDSComponent;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Use to retrieve users and roles count from the JDBC user store.
 */
public class JDBCUserStoreCountRetriever extends AbstractUserStoreCountRetriever {

    private static final Log log = LogFactory.getLog(JDBCUserStoreCountRetriever.class);
    private RealmConfiguration realmConfiguration = null;
    private int tenantId = MultitenantConstants.SUPER_TENANT_ID;

    public JDBCUserStoreCountRetriever() {

    }

    public void init(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }


    @Override
    public Long countUsers(String filter) throws UserStoreCounterException {
        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = getDBConnection(realmConfiguration);
            sqlStmt = JDBCUserStoreMetricsConstants.COUNT_USERS_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, "%" + filter + "%");
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            dbConnection.commit();
            if (resultSet.next()) {
                return resultSet.getLong("RESULT");
            } else {
                log.error("No user count is retrieved from the user store");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            rollbackTransaction(dbConnection);
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
            dbConnection = getDBConnection(realmConfiguration);
            sqlStmt = JDBCUserStoreMetricsConstants.COUNT_ROLES_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, "%" + filter + "%");
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            dbConnection.commit();
            if (resultSet.next()) {
                return resultSet.getLong("RESULT");
            } else {
                log.error("No role count is retrieved from the user store.");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            rollbackTransaction(dbConnection);
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
        String mappedAttribute = null;

        try {
            String domainName = realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig
                    .PROPERTY_DOMAIN_NAME);
            if (StringUtils.isEmpty(domainName)) {
                domainName = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
            }

            UserRealm userRealm = UserStoreCountDSComponent.getRealmService().getTenantUserRealm(tenantId);

            if (StringUtils.isNotEmpty(claimURI)) {
                mappedAttribute = userRealm.getClaimManager().getAttributeName(domainName, claimURI);
            }

            dbConnection = getDBConnection(realmConfiguration);
            sqlStmt = JDBCUserStoreMetricsConstants.COUNT_CLAIM_SQL;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, mappedAttribute);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, "%" + valueFilter + "%");
            prepStmt.setString(4, UserCoreConstants.DEFAULT_PROFILE);
            prepStmt.setQueryTimeout(searchTime);

            resultSet = prepStmt.executeQuery();
            dbConnection.commit();
            if (resultSet.next()) {
                return resultSet.getLong("RESULT");
            } else {
                log.error("No claim count is retrieved from the user store.");
                return Long.valueOf(-1);
            }

        } catch (SQLException e) {
            rollbackTransaction(dbConnection);
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

    private Connection getDBConnection(RealmConfiguration realmConfiguration) throws SQLException, UserStoreException {

        Connection dbConnection = null;
        DataSource dataSource = DatabaseUtil.createUserStoreDataSource(realmConfiguration);

        if (dataSource != null) {
            dbConnection = DatabaseUtil.getDBConnection(dataSource);
        }

        //if primary user store, DB connection can be same as realm data source.
        if (dbConnection == null && realmConfiguration.isPrimary()) {
            dbConnection = IdentityDatabaseUtil.getUserDBConnection(true);
        } else if (dbConnection == null) {
            throw new UserStoreException("Could not create a database connection to " +
                    realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
        } else {
            // db connection is present
        }
        return dbConnection;
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     *
     * @param dbConnection database connection.
     * @throws SQLException SQL Exception.
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
