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
import org.wso2.carbon.identity.mgt.dto.IdentityMetadataDO;

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
public class UserIdentityMetadataStore {

    /**
     * Set the validity to invalid
     *
     * @param metadata       TODO
     * @param tempCredential
     * @throws IdentityException
     */
    public void invalidateMetadata(IdentityMetadataDO metadata) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.INVALIDATE_METADATA);
            prepStmt.setString(1, metadata.getUserName());
            prepStmt.setInt(2, metadata.getTenantId());
            prepStmt.setString(3, metadata.getMetadataType());
            prepStmt.setString(4, metadata.getMetadata());
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
     * @param metadataSet
     * @throws IdentityException
     */
    public void invalidateMetadataSet(IdentityMetadataDO[] metadataSet) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.INVALIDATE_METADATA);
            for (IdentityMetadataDO metadata : metadataSet) {
                prepStmt.setString(1, metadata.getUserName());
                prepStmt.setInt(2, metadata.getTenantId());
                prepStmt.setString(3, metadata.getMetadataType());
                prepStmt.setString(4, metadata.getMetadata());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
            connection.setAutoCommit(false);
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
     * @param metadata
     * @throws IdentityException
     */
    public void storeMetadata(IdentityMetadataDO metadata) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.STORE_META_DATA);
            prepStmt.setString(1, metadata.getUserName());
            prepStmt.setInt(2, metadata.getTenantId());
            prepStmt.setString(3, metadata.getMetadataType());
            prepStmt.setString(4, metadata.getMetadata());
            prepStmt.setString(5, Boolean.toString(metadata.isValid()));
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
     * Stores identity data set.
     *
     * @param metadataSet
     * @throws IdentityException
     */
    public void storeMetadataSet(IdentityMetadataDO[] metadataSet) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        try {
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(SQLQuery.STORE_META_DATA);
            for (IdentityMetadataDO metadata : metadataSet) {
                prepStmt.setString(1, metadata.getUserName());
                prepStmt.setInt(2, metadata.getTenantId());
                prepStmt.setString(3, metadata.getMetadataType());
                prepStmt.setString(4, metadata.getMetadata());
                prepStmt.setString(5, Boolean.toString(metadata.isValid()));
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
     * @param userName
     * @param tenantId
     * @param metadataType
     * @param metadata
     * @return
     * @throws IdentityException
     */
    public IdentityMetadataDO loadMetadata(String userName, int tenantId, String metadataType,
                                           String metadata) throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet results = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.LOAD_META_DATA);
            prepStmt.setString(1, userName);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, metadataType);
            prepStmt.setString(4, metadata);
            results = prepStmt.executeQuery();
            connection.commit();
            if (results.next()) {
                return new IdentityMetadataDO(results.getString(1), results.getInt(2),
                        results.getString(3), results.getString(4),
                        Boolean.parseBoolean(results.getString(5)));
            }
            if (results.next()) {
                throw IdentityException.error("Duplicate entry found for " + metadataType);
            }
            return null;
        } catch (SQLException e) {
            throw IdentityException.error("Error while reading user identity data", e);
        } finally {
            IdentityDatabaseUtil.closeResultSet(results);
            IdentityDatabaseUtil.closeStatement(prepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * @param userName
     * @param tenantId
     * @return
     * @throws IdentityException
     */
    public IdentityMetadataDO[] loadMetadata(String userName, int tenantId)
            throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet results = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.LOAD_USER_METADATA);
            prepStmt.setString(1, userName);
            prepStmt.setInt(2, IdentityTenantUtil.getTenantIdOfUser(userName));

            results = prepStmt.executeQuery();
            List<IdentityMetadataDO> metada = new ArrayList<IdentityMetadataDO>();
            while (results.next()) {
                metada.add(new IdentityMetadataDO(results.getString(1), results.getInt(2),
                        results.getString(3), results.getString(4),
                        Boolean.parseBoolean(results.getString(5))));
            }
            IdentityMetadataDO[] resultMetadata = new IdentityMetadataDO[metada.size()];
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

    /**
     * Can be used to return primary security questions etc
     *
     * @param userName
     * @param tenantId
     * @param metadataType
     * @return
     * @throws IdentityException
     */
    public IdentityMetadataDO[] loadMetadata(String userName, int tenantId, String metadataType)
            throws IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet results = null;
        try {
            prepStmt = connection.prepareStatement(SQLQuery.LOAD_TENANT_METADATA);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, metadataType);
            results = prepStmt.executeQuery();
            List<IdentityMetadataDO> metada = new ArrayList<IdentityMetadataDO>();
            while (results.next()) {
                metada.add(new IdentityMetadataDO(results.getString(1), results.getInt(2),
                        results.getString(3), results.getString(4),
                        Boolean.parseBoolean(results.getString(5))));
            }
            IdentityMetadataDO[] resultMetadata = new IdentityMetadataDO[metada.size()];
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
