/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.store;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to access the identity metadata. Schema of the identity
 * metadata is as follows :
 * ====================================================
 * ||UserName|TenantID|MetadataType|Metadata|Valid||
 * ====================================================
 */
public class JDBCUserRecoveryDataStore implements UserRecoveryDataStore {

    /**
     * invalidate recovery data. it means delete user recovery data entry from store
     *
     * @param recoveryDataDO
     * @throws IdentityException
     */
    @Override
    public void invalidate(UserRecoveryDataDO recoveryDataDO) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.INVALIDATE_METADATA); // TODO Delete entry
            prepStmt.setString(1, recoveryDataDO.getUserName());
            prepStmt.setInt(2, recoveryDataDO.getTenantId());
            prepStmt.setString(3, recoveryDataDO.getCode());
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            throw IdentityException.error("Error while storing user identity data", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * @param userId
     * @param tenant
     * @throws IdentityException
     */
    @Override
    public void invalidate(String userId, int tenant) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.INVALIDATE_METADATA);
            prepStmt.setString(1, userId);
            prepStmt.setInt(2, tenant);
            connection.commit();
        } catch (SQLException e) {
            throw IdentityException.error("Error while invalidating user identity data", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }

    }

    /**
     * Stores identity data.
     *
     * @throws IdentityException
     */
    @Override
    public void store(UserRecoveryDataDO recoveryDataDO) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.STORE_META_DATA);
            prepStmt.setString(1, recoveryDataDO.getUserName());
            prepStmt.setInt(2, recoveryDataDO.getTenantId());
            prepStmt.setString(3, recoveryDataDO.getCode());
            prepStmt.setString(4, recoveryDataDO.getSecret());
            prepStmt.execute();
            connection.setAutoCommit(false);
            connection.commit();
        } catch (SQLException e) {
            throw IdentityException.error("Error while storing user identity data", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Stores identity data set.
     *
     * @throws IdentityException
     */
    @Override
    public void store(UserRecoveryDataDO[] recoveryDataDOs) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(SQLQuery.STORE_META_DATA);
            for (UserRecoveryDataDO dataDO : recoveryDataDOs) {
                prepStmt.setString(1, dataDO.getUserName());
                prepStmt.setInt(2, dataDO.getTenantId());
                prepStmt.setString(3, dataDO.getCode());
                prepStmt.setString(4, dataDO.getSecret());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            throw IdentityException.error("Error while storing user identity data", e);
        } finally {
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * This method should return only one result. An exception will be thrown if
     * duplicate entries found.
     * This can be used to check if the given metada exist in the database or to
     * check the validity.
     *
     * @return
     * @throws IdentityException
     */


    /**
     * @param userName
     * @param tenantId
     * @return
     * @throws IdentityException
     */
    @Override
    public UserRecoveryDataDO[] load(String userName, int tenantId) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet results = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.LOAD_USER_METADATA);
            prepStmt.setString(1, userName);
            prepStmt.setInt(2, IdentityTenantUtil.getTenantIdOfUser(userName));

            results = prepStmt.executeQuery();
            List<UserRecoveryDataDO> metada = new ArrayList<UserRecoveryDataDO>();
            while (results.next()) {
                metada.add(new UserRecoveryDataDO(results.getString(1), results.getInt(2),
                        results.getString(3), results.getString(4)));
            }
            UserRecoveryDataDO[] resultMetadata = new UserRecoveryDataDO[metada.size()];
            connection.commit();
            return metada.toArray(resultMetadata);
        } catch (SQLException e) {
            throw IdentityException.error("Error while reading user identity data", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(results);
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public UserRecoveryDataDO load(String code) throws IdentityException {
        return null;
    }

    /**
     * This class contains the SQL queries.
     */
    private static class SQLQuery {

        /**
         * Query to load temporary passwords and confirmation codes
         */
        public static final String LOAD_META_DATA =
                "SELECT "
                        + "USER_NAME, TENANT_ID, METADATA_TYPE, METADATA, VALID "
                        + "FROM IDN_IDENTITY_META_DATA "
                        + "WHERE USER_NAME = ? AND TENANT_ID = ? AND METADATA_TYPE = ? AND METADATA = ?";

        /**
         * Query to load user metadata
         */
        public static final String LOAD_USER_METADATA =
                "SELECT "
                        + "USER_NAME, TENANT_ID, METADATA_TYPE, METADATA, VALID "
                        + "FROM IDN_IDENTITY_META_DATA "
                        + "WHERE USER_NAME = ? AND TENANT_ID = ? ";

        /**
         * Query to load security questions
         */
        public static final String LOAD_TENANT_METADATA =
                "SELECT "
                        + "USER_NAME, TENANT_ID, METADATA_TYPE, METADATA, VALID "
                        + "FROM IDN_IDENTITY_META_DATA "
                        + "WHERE TENANT_ID = ? AND METADATA_TYPE = ?";

        public static final String STORE_META_DATA =
                "INSERT "
                        + "INTO IDN_IDENTITY_META_DATA "
                        + "(USER_NAME, TENANT_ID, METADATA_TYPE, METADATA, VALID)"
                        + "VALUES (?,?,?,?,?)";

        public static final String INVALIDATE_METADATA =
                "UPDATE "
                        + "IDN_IDENTITY_META_DATA "
                        + "SET VALID = 'false' "
                        + "WHERE USER_NAME = ? AND TENANT_ID = ? AND METADATA_TYPE = ? AND METADATA = ?";

        private SQLQuery() {
        }
    }

}
