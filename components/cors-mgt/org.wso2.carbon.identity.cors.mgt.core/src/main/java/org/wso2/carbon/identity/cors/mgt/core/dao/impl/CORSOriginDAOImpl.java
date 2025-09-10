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
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.Template;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSOriginDAO;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSApplication;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

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
import static org.wso2.carbon.identity.cors.mgt.core.constant.SchemaConstants.CORSOriginTableColumns.UNIQUE_ID;
import static org.wso2.carbon.identity.cors.mgt.core.internal.util.ErrorUtils.handleServerException;

/**
 * {@link CORSOriginDAO} implementation.
 */
public class CORSOriginDAOImpl implements CORSOriginDAO {

    private static final Log log = LogFactory.getLog(CORSOriginDAOImpl.class);

    @Override
    public int getPriority() {

        return 10;
    }

    @Override
    public List<CORSOrigin> getCORSOriginsByTenantId(int tenantId) throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(GET_CORS_ORIGINS_BY_TENANT_ID,
                    (resultSet, rowNumber) -> {
                        CORSOrigin corsOrigin = new CORSOrigin();
                        corsOrigin.setOrigin(resultSet.getString(ORIGIN));
                        corsOrigin.setId(resultSet.getString(UNIQUE_ID));
                        return corsOrigin;
                    },
                    preparedStatement -> preparedStatement.setInt(1, tenantId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CORS_RETRIEVE, e, tenantDomain);
        }
    }

    @Override
    public List<CORSOrigin> getCORSOriginsByTenantDomain(String tenantDomain)
            throws CORSManagementServiceServerException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return getCORSOriginsByTenantId(tenantId);
    }

    @Override
    public List<CORSOrigin> getCORSOriginsByApplicationId(int applicationId, int tenantId)
            throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(GET_CORS_ORIGINS_BY_APPLICATION_ID,
                    (resultSet, rowNumber) -> {
                        CORSOrigin corsOrigin = new CORSOrigin();
                        corsOrigin.setOrigin(resultSet.getString(ORIGIN));
                        corsOrigin.setId(resultSet.getString(UNIQUE_ID));
                        return corsOrigin;
                    },
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setInt(2, applicationId);
                    });
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CORS_RETRIEVE, e, tenantDomain);
        }
    }

    @Override
    public void setCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                // Delete existing application associations.
                List<Integer> originIds = template.executeQuery(GET_CORS_ORIGINS_BY_TENANT_ID,
                        (rs, row) -> rs.getInt(ID),
                        ps -> ps.setInt(1, tenantId));
                for (Integer originId : originIds) {
                    template.executeUpdate(DELETE_CORS_APPLICATION_ASSOCIATION,
                            ps -> {
                                ps.setInt(1, originId);
                                ps.setInt(2, applicationId);
                            });
                }

                for (CORSOrigin corsOrigin : corsOrigins) {
                    Integer corsOriginId = template.fetchSingleRecord(GET_CORS_ORIGIN_ID,
                            (rs, row) -> rs.getInt(CORSOriginTableColumns.ID),
                            ps -> {
                                ps.setInt(1, tenantId);
                                ps.setString(2, corsOrigin.getOrigin());
                            });
                    if (corsOriginId == null) {
                        corsOriginId = template.executeInsert(INSERT_CORS_ORIGIN, ps -> {
                            ps.setInt(1, tenantId);
                            ps.setString(2, corsOrigin.getOrigin());
                            ps.setString(3, UUID.randomUUID().toString());
                        }, corsOrigin, true);
                    }

                    final int finalCorsOriginId = corsOriginId;
                    template.executeUpdate(INSERT_CORS_ASSOCIATION,
                            ps -> {
                                ps.setInt(1, finalCorsOriginId);
                                ps.setInt(2, applicationId);
                            });
                }

                // Cleanup dangling origins (origins without any association to an application) is disabled temporary.
                // Even the CORS Origins are stored for each application separately, the CORS valve filters them
                // based on the tenant level. Because of that there might be other applications which are not configured
                // allowed origins but still working as another application has already set is as an allowed origin.
                // Related issue: https://github.com/wso2/product-is/issues/11241
                // cleanupDanglingOrigins(template, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
        }
    }

    @Override
    public void addCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                for (CORSOrigin corsOrigin : corsOrigins) {
                    Integer corsOriginId = template.fetchSingleRecord(GET_CORS_ORIGIN_ID,
                            (rs, row) -> rs.getInt(CORSOriginTableColumns.ID),
                            ps -> {
                                ps.setInt(1, tenantId);
                                ps.setString(2, corsOrigin.getOrigin());
                            });
                    if (corsOriginId == null) {
                        template.executeInsert(INSERT_CORS_ORIGIN, ps -> {
                            ps.setInt(1, tenantId);
                            ps.setString(2, corsOrigin.getOrigin());
                            ps.setString(3, UUID.randomUUID().toString());
                        }, corsOrigin, false);

                        corsOriginId = template.fetchSingleRecord(GET_CORS_ORIGIN_ID,
                                (rs, row) -> rs.getInt(CORSOriginTableColumns.ID),
                                ps -> {
                                    ps.setInt(1, tenantId);
                                    ps.setString(2, corsOrigin.getOrigin());
                                });
                        if (corsOriginId == null) {
                            throw handleServerException(ERROR_CODE_CORS_ADD, tenantDomain);
                        }
                    }

                    final int finalCorsOriginId = corsOriginId;
                    template.executeUpdate(INSERT_CORS_ASSOCIATION,
                            ps -> {
                                ps.setInt(1, finalCorsOriginId);
                                ps.setInt(2, applicationId);
                            });
                }
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_CORS_ADD, e, tenantDomain);
        }
    }

    @Override
    public void deleteCORSOrigins(int applicationId, List<String> corsOriginIds, int tenantId)
            throws CORSManagementServiceServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        final String[] currentId = new String[1];
        try {
            jdbcTemplate.withTransaction(template -> {
                for (String corsOriginId : corsOriginIds) {
                    currentId[0] = corsOriginId;
                    Integer corsOriginDbId = template.fetchSingleRecord(GET_CORS_ORIGIN_ID_BY_UUID,
                            (rs, row) -> rs.getInt(CORSOriginTableColumns.ID),
                            ps -> ps.setString(1, corsOriginId));
                    if (corsOriginDbId == null) {
                        throw handleServerException(ERROR_CODE_CORS_DELETE, corsOriginId);
                    }
                    final int originDbId = corsOriginDbId;
                    template.executeUpdate(DELETE_CORS_APPLICATION_ASSOCIATION,
                            ps -> {
                                ps.setInt(1, originDbId);
                                ps.setInt(2, applicationId);
                            });
                }

                // Cleanup dangling origins (origins without any association to an application) is disabled temporary.
                // Even the CORS Origins are stored for each application separately, the CORS valve filters them
                // based on the tenant level. Because of that there might be other applications which are not configured
                // allowed origins but still working as another application has already set is as an allowed origin.
                // Related issue: https://github.com/wso2/product-is/issues/11241
                // cleanupDanglingOrigins(template, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_CORS_DELETE, e, currentId[0]);
        }
    }

    @Override
    public List<CORSApplication> getCORSOriginApplications(String corsOriginId)
            throws CORSManagementServiceServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(GET_CORS_APPLICATIONS_BY_CORS_ORIGIN_ID,
                    (resultSet, rowNumber) -> new CORSApplication(resultSet.getString(UNIQUE_ID),
                            resultSet.getString("APP_NAME")),
                    preparedStatement -> preparedStatement.setString(1, corsOriginId));
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_CODE_CORS_APPLICATIONS_RETRIEVE, e, String.valueOf(corsOriginId));
        }
    }

    private void cleanupDanglingOrigins(Template<?> template, int tenantId) throws DataAccessException {

        List<Integer> originIds = template.executeQuery(GET_CORS_ORIGINS_BY_TENANT_ID,
                (rs, row) -> rs.getInt(ID),
                ps -> ps.setInt(1, tenantId));

        for (Integer corsOriginId : originIds) {
            Integer associatedId = template.fetchSingleRecord(GET_CORS_APPLICATION_IDS_BY_CORS_ORIGIN_ID,
                    (rs, row) -> rs.getInt(1),
                    ps -> ps.setInt(1, corsOriginId));
            if (associatedId == null) {
                template.executeUpdate(DELETE_ORIGIN, ps -> ps.setInt(1, corsOriginId));
            }
        }
    }
}

