/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSOriginDAO;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSApplication;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_ADD;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_APPLICATIONS_RETRIEVE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_DELETE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages.ERROR_CODE_CORS_RETRIEVE;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.DELETE_CORS_APPLICATION_ASSOCIATION;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.DELETE_ORIGIN;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.GET_CORS_APPLICATIONS_BY_CORS_ORIGIN_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.GET_CORS_APPLICATION_IDS_BY_CORS_ORIGIN_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.GET_CORS_ORIGINS_BY_APPLICATION_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.GET_CORS_ORIGINS_BY_TENANT_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.GET_CORS_ORIGIN_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.GET_CORS_ORIGIN_ID_BY_UUID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.INSERT_CORS_ASSOCIATION;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SQLQueries.INSERT_CORS_ORIGIN;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SchemaConstants.CORSOriginTableColumns;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SchemaConstants.CORSOriginTableColumns.ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SchemaConstants.CORSOriginTableColumns.ORIGIN;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SchemaConstants.CORSOriginTableColumns.TENANT_ID;
import static org.wso2.carbon.identity.cors.mgt.core.constant.SchemaConstants.CORSOriginTableColumns.UNIQUE_ID;
import static org.wso2.carbon.identity.cors.mgt.core.internal.util.ErrorUtils.handleServerException;

/**
 * {@link CORSOriginDAO} implementation.
 */
public class CORSOriginDAOImpl implements CORSOriginDAO {

    private static final Log log = LogFactory.getLog(CORSOriginDAOImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {

        return 10;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSOrigin> getCORSOriginsByTenantId(int tenantId) throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                     GET_CORS_ORIGINS_BY_TENANT_ID)) {
            namedPreparedStatement.setInt(1, tenantId);

            try (ResultSet resultSet = namedPreparedStatement.executeQuery()) {
                List<CORSOrigin> corsOrigins = new ArrayList<>();
                while (resultSet.next()) {
                    CORSOrigin corsOrigin = new CORSOrigin();
                    corsOrigin.setOrigin(resultSet.getString(ORIGIN));
                    corsOrigin.setId(resultSet.getString(UNIQUE_ID));

                    corsOrigins.add(corsOrigin);
                }

                return corsOrigins;
            }
        } catch (SQLException e) {
            throw handleServerException(ERROR_CODE_CORS_RETRIEVE, e, tenantDomain);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSOrigin> getCORSOriginsByApplicationId(int applicationId, int tenantId)
            throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                     GET_CORS_ORIGINS_BY_APPLICATION_ID)) {
            namedPreparedStatement.setInt(1, tenantId);
            namedPreparedStatement.setInt(2, applicationId);

            try (ResultSet resultSet = namedPreparedStatement.executeQuery()) {
                List<CORSOrigin> corsOrigins = new ArrayList<>();
                while (resultSet.next()) {
                    CORSOrigin corsOrigin = new CORSOrigin();
                    corsOrigin.setOrigin(resultSet.getString(ORIGIN));
                    corsOrigin.setId(resultSet.getString(UNIQUE_ID));

                    corsOrigins.add(corsOrigin);
                }

                return corsOrigins;
            }
        } catch (SQLException e) {
            throw handleServerException(ERROR_CODE_CORS_RETRIEVE, e, tenantDomain);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement namedPreparedStatement1 = new NamedPreparedStatement(connection,
                    GET_CORS_ORIGINS_BY_TENANT_ID)) {
                // Delete existing application associations.
                namedPreparedStatement1.setInt(1, tenantId);
                try (ResultSet resultSet = namedPreparedStatement1.executeQuery()) {
                    while (resultSet.next()) {
                        try (PreparedStatement preparedStatement =
                                     connection.prepareStatement(DELETE_CORS_APPLICATION_ASSOCIATION)) {
                            preparedStatement.setInt(1, resultSet.getInt("ID"));
                            preparedStatement.setInt(2, applicationId);
                            preparedStatement.executeUpdate();
                        }
                    }
                }

                // Cleanup dangling origins (origins without any association to an application) is disabled temporary.
                // Even the CORS Origins are stored for each application separately, the CORS valve filters them
                // based on the tenant level. Because of that there might be other applications which are not configured
                // allowed origins but still working as another application has already set is as an allowed origin.
                // Related issue: https://github.com/wso2/product-is/issues/11241

                // cleanupDanglingOrigins(connection, tenantId);

                for (CORSOrigin corsOrigin : corsOrigins) {
                    // Check if the origins is there.
                    try (NamedPreparedStatement namedPreparedStatement2 =
                                 new NamedPreparedStatement(connection, GET_CORS_ORIGIN_ID)) {
                        namedPreparedStatement2.setInt(TENANT_ID, tenantId);
                        namedPreparedStatement2.setString(ORIGIN, corsOrigin.getOrigin());
                        try (ResultSet resultSet1 = namedPreparedStatement2.executeQuery()) {
                            int corsOriginId = -1;
                            if (!resultSet1.next()) {
                                try (NamedPreparedStatement namedPreparedStatement3 =
                                             new NamedPreparedStatement(connection, INSERT_CORS_ORIGIN, ID)) {
                                    // Origin is not present. Therefore add an origin.
                                    namedPreparedStatement3.setInt(TENANT_ID, tenantId);
                                    namedPreparedStatement3.setString(ORIGIN, corsOrigin.getOrigin());
                                    namedPreparedStatement3.setString(UNIQUE_ID, UUID.randomUUID().toString());
                                    namedPreparedStatement3.executeUpdate();

                                    // Get origin id.
                                    try (ResultSet resultSet2 = namedPreparedStatement3.getGeneratedKeys()) {
                                        if (resultSet2.next()) {
                                            corsOriginId = resultSet2.getInt(1);
                                        }
                                    }
                                }
                            } else {
                                // Get origin id.
                                corsOriginId = resultSet1.getInt(CORSOriginTableColumns.ID);
                            }

                            // Add application associations.
                            try (PreparedStatement preparedStatement4 =
                                         connection.prepareStatement(INSERT_CORS_ASSOCIATION)) {
                                preparedStatement4.setInt(1, corsOriginId);
                                preparedStatement4.setInt(2, applicationId);
                                preparedStatement4.executeUpdate();
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
            }

            // Commit the transaction as no errors were thrown.
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                for (CORSOrigin corsOrigin : corsOrigins) {
                    // Check if the origins is there.
                    try (NamedPreparedStatement namedPreparedStatement1 = new NamedPreparedStatement(connection,
                            GET_CORS_ORIGIN_ID)) {
                        namedPreparedStatement1.setInt(1, tenantId);
                        namedPreparedStatement1.setString(2, corsOrigin.getOrigin());
                        try (ResultSet resultSet1 = namedPreparedStatement1.executeQuery()) {
                            if (!resultSet1.next()) {
                                // Origin is not present. Therefore add an origin without the tenant association.
                                try (NamedPreparedStatement namedPreparedStatement2 =
                                             new NamedPreparedStatement(connection, INSERT_CORS_ORIGIN)) {
                                    namedPreparedStatement2.setInt(TENANT_ID, tenantId);
                                    namedPreparedStatement2.setString(ORIGIN, corsOrigin.getOrigin());
                                    namedPreparedStatement2.setString(UNIQUE_ID, UUID.randomUUID().toString());
                                    namedPreparedStatement2.executeUpdate();
                                }
                            }
                        }
                    }

                    try (NamedPreparedStatement namedPreparedStatement3 = new NamedPreparedStatement(connection,
                            GET_CORS_ORIGIN_ID)) {
                        // Get origin id.
                        namedPreparedStatement3.setInt(TENANT_ID, tenantId);
                        namedPreparedStatement3.setString(ORIGIN, corsOrigin.getOrigin());
                        try (ResultSet resultSet2 = namedPreparedStatement3.executeQuery()) {
                            if (resultSet2.next()) {
                                int corsOriginId = resultSet2.getInt("ID");

                                // Add application associations.
                                try (PreparedStatement preparedStatement4 =
                                             connection.prepareStatement(INSERT_CORS_ASSOCIATION)) {
                                    preparedStatement4.setInt(1, corsOriginId);
                                    preparedStatement4.setInt(2, applicationId);
                                    preparedStatement4.executeUpdate();
                                }
                            } else {
                                IdentityDatabaseUtil.rollbackTransaction(connection);
                                throw handleServerException(ERROR_CODE_CORS_ADD, tenantDomain);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
            }

            // Commit the transaction as no errors were thrown.
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCORSOrigins(int applicationId, List<String> corsOriginIds, int tenantId)
            throws CORSManagementServiceServerException {

        String currentId = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                for (String corsOriginId : corsOriginIds) {
                    currentId = corsOriginId;

                    try (NamedPreparedStatement namedPreparedStatement1 =
                                 new NamedPreparedStatement(connection, GET_CORS_ORIGIN_ID_BY_UUID)) {
                        namedPreparedStatement1.setString(1, corsOriginId);
                        try (ResultSet resultSet = namedPreparedStatement1.executeQuery()) {
                            if (resultSet.next()) {
                                int corsOriginDbId = resultSet.getInt(CORSOriginTableColumns.ID);

                                // Delete application association.
                                try (PreparedStatement preparedStatement2 =
                                             connection.prepareStatement(DELETE_CORS_APPLICATION_ASSOCIATION)) {
                                    preparedStatement2.setInt(1, corsOriginDbId);
                                    preparedStatement2.setInt(2, applicationId);
                                    preparedStatement2.executeUpdate();
                                }
                            } else {
                                IdentityDatabaseUtil.rollbackTransaction(connection);
                                throw handleServerException(ERROR_CODE_CORS_DELETE, currentId);
                            }
                        }
                    }
                }

                // Cleanup dangling origins (origins without any association to an application) is disabled temporary.
                // Even the CORS Origins are stored for each application separately, the CORS valve filters them
                // based on the tenant level. Because of that there might be other applications which are not configured
                // allowed origins but still working as another application has already set is as an allowed origin.
                // Related issue: https://github.com/wso2/product-is/issues/11241

                // cleanupDanglingOrigins(connection, tenantId);

            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw handleServerException(ERROR_CODE_CORS_DELETE, e, currentId);
            }

            // Commit the transaction as no errors were thrown.
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            throw handleServerException(ERROR_CODE_CORS_DELETE, e, currentId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CORSApplication> getCORSOriginApplications(String corsOriginId)
            throws CORSManagementServiceServerException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement namedPreparedStatement =
                     new NamedPreparedStatement(connection, GET_CORS_APPLICATIONS_BY_CORS_ORIGIN_ID)) {
            namedPreparedStatement.setString(1, corsOriginId);
            try (ResultSet resultSet = namedPreparedStatement.executeQuery()) {
                List<CORSApplication> corsApplications = new ArrayList<>();
                while (resultSet.next()) {
                    CORSApplication corsApplication = new CORSApplication(resultSet.getString(UNIQUE_ID),
                            resultSet.getString("APP_NAME"));
                    corsApplications.add(corsApplication);
                }
                return corsApplications;
            }
        } catch (SQLException e) {
            throw handleServerException(ERROR_CODE_CORS_APPLICATIONS_RETRIEVE, e, String.valueOf(corsOriginId));
        }
    }

    private void cleanupDanglingOrigins(Connection connection, int tenantId) throws SQLException {

        // Get tenant CORS origins.
        try (NamedPreparedStatement namedPreparedStatement1 = new NamedPreparedStatement(connection,
                GET_CORS_ORIGINS_BY_TENANT_ID)) {
            namedPreparedStatement1.setInt(1, tenantId);

            try (ResultSet resultSet1 = namedPreparedStatement1.executeQuery()) {
                while (resultSet1.next()) {
                    int corsOriginId = resultSet1.getInt("ID");

                    try (NamedPreparedStatement namedPreparedStatement2 = new NamedPreparedStatement(connection,
                            GET_CORS_APPLICATION_IDS_BY_CORS_ORIGIN_ID)) {
                        namedPreparedStatement2.setInt(1, corsOriginId);
                        try (ResultSet resultSet2 = namedPreparedStatement2.executeQuery()) {
                            // Get the associated applications.
                            if (!resultSet2.next()) {
                                try (NamedPreparedStatement namedPreparedStatement3 =
                                             new NamedPreparedStatement(connection, DELETE_ORIGIN)) {
                                    namedPreparedStatement3.setInt(1, corsOriginId);
                                    namedPreparedStatement3.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw e;
        }
    }
}
