/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.SpTemplate;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationTemplateDAO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Default implementation of {@link ApplicationTemplateDAO}. This handles {@link SpTemplate} related db layer
 * operations.
 */
public class ApplicationTemplateDAOImpl implements ApplicationTemplateDAO {

    private static final Log log = LogFactory.getLog(ApplicationTemplateDAOImpl.class);

    @Override
    public void createApplicationTemplate(SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Creating application template: %s in tenant: %s", spTemplate.getName(),
                    tenantDomain));
        }

        try {
            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            jdbcTemplate.executeInsert(ApplicationMgtDBQueries.ADD_SP_TEMPLATE, (preparedStatement -> {
                preparedStatement.setInt(1, getTenantID(tenantDomain));
                preparedStatement.setString(2, spTemplate.getName());
                preparedStatement.setString(3, spTemplate.getDescription());
                try {
                    setBlobValue(spTemplate.getContent(), preparedStatement, 4);
                } catch (IOException e) {
                    throw new SQLException(String.format("Could not set application template: %s content as " +
                            "a Blob in tenant: %s.", spTemplate.getName(), tenantDomain), e);
                }
            }), null, true);
        } catch (DataAccessException e) {
            throw new IdentityApplicationManagementException(String.format("Error while creating application " +
                    "template: %s in tenant: %s", spTemplate.getName(), tenantDomain), e);
        }
    }

    @Override
    public SpTemplate getApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Retrieving application template: %s in tenant: %s", templateName, tenantDomain));
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        SpTemplate spTemplate;
        try {
            spTemplate = jdbcTemplate.fetchSingleRecord(ApplicationMgtDBQueries.GET_SP_TEMPLATE,
                    (resultSet, rowNumber) -> {
                        try {
                            return new SpTemplate(templateName, resultSet.getString(1),
                                    IOUtils.toString(resultSet.getBinaryStream(2)));
                        } catch (IOException e) {
                            throw new SQLException(String.format("Could not get application template: %s content as " +
                            "a Blob in tenant: %s.", templateName, tenantDomain), e);
                        }
                     },
                    (PreparedStatement preparedStatement) -> {
                        preparedStatement.setString(1, templateName);
                        preparedStatement.setInt(2, getTenantID(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw new IdentityApplicationManagementException(
                    String.format("Could not read the template information for template: %s in tenant: %s" +
                            templateName, tenantDomain), e);
        }
        return spTemplate;
    }

    @Override
    public void deleteApplicationTemplate(String templateName, String tenantDomain) throws
            IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Deleting application template: %s in tenant: %s", templateName, tenantDomain));
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(ApplicationMgtDBQueries.DELETE_SP_TEMPLATE_BY_NAME,
                    preparedStatement -> {
                        preparedStatement.setString(1, templateName);
                        preparedStatement.setInt(2, getTenantID(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw new IdentityApplicationManagementException(String.format("An error occurred while deleting the " +
                    "application template : %s in tenant: %s", templateName, tenantDomain), e);
        }
    }

    @Override
    public void updateApplicationTemplate(String templateName, SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Updating application template: %s in tenant: %s", spTemplate.getName(),
                    tenantDomain));
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(ApplicationMgtDBQueries.UPDATE_SP_TEMPLATE_BY_NAME,
                    preparedStatement -> {
                        preparedStatement.setString(1, spTemplate.getName());
                        preparedStatement.setString(2, spTemplate.getDescription());
                        try {
                            setBlobValue(spTemplate.getContent(), preparedStatement, 3);
                        } catch (IOException e) {
                            throw new SQLException(String.format("Could not set application template: %s content as " +
                                    "a Blob in tenant: %s.", spTemplate.getName(), tenantDomain), e);
                        }
                        preparedStatement.setString(4, templateName);
                        preparedStatement.setInt(5, getTenantID(tenantDomain));
                    });
        } catch (DataAccessException e) {
            throw new IdentityApplicationManagementException(String.format("An error occurred while updating the" +
                    " application template : %s in tenant: %s", spTemplate.getName(), tenantDomain), e);
        }
    }

    @Override
    public boolean isExistingTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Checking application template exists for name: %s in tenant: %s", templateName,
                    tenantDomain));
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            Integer count = jdbcTemplate.fetchSingleRecord(
                    ApplicationMgtDBQueries.IS_SP_TEMPLATE_EXISTS, (resultSet, rowNumber) ->
                            resultSet.getInt(1),
                    preparedStatement -> {
                        preparedStatement.setString(1, templateName);
                        preparedStatement.setInt(2, getTenantID(tenantDomain));
                    });
            if (count == null) {
                return false;
            }
            return (count > 0);
        } catch (DataAccessException e) {
            throw new IdentityApplicationManagementException(String.format("Error while checking existence of " +
                    "application template: %s in tenant: %s", templateName, tenantDomain), e);
        }
    }

    @Override
    public List<SpTemplate> getAllApplicationTemplateInfo(String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting all the application template basic info of tenant: " + tenantDomain);
        }

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeQuery(ApplicationMgtDBQueries.GET_ALL_SP_TEMPLATES_BASIC_INFO,
                    (resultSet, i) -> new SpTemplate(resultSet.getString(1), resultSet.getString(2),
                            null),
                    preparedStatement -> preparedStatement.setInt(1, getTenantID(tenantDomain)));
        } catch (DataAccessException e) {
            throw new IdentityApplicationManagementException("Error while Loading all the application template basic " +
                    "info of tenant: " + tenantDomain, e);
        }
    }

    /**
     * Set given string as Blob for the given index into the prepared-statement.
     *
     * @param value string value to be converted to blob
     * @param prepStmt Prepared statement
     * @param index column index
     * @throws SQLException
     * @throws IOException
     */
    private void setBlobValue(String value, PreparedStatement prepStmt, int index) throws SQLException, IOException {

        if (value != null) {
            InputStream inputStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
            prepStmt.setBinaryStream(index, inputStream, inputStream.available());
        } else {
            prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
        }
    }

    private int getTenantID(String tenantDomain) {

        // get logged-in users tenant identifier.
        int tenantID = MultitenantConstants.INVALID_TENANT_ID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }
        return tenantID;
    }
}

