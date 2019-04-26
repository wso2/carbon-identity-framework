/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.dao.impl;

import org.apache.commons.io.IOUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.dao.TemplateManagerDAO;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementSQLException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementServerException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;
import org.wso2.carbon.identity.template.mgt.util.JdbcUtils;
import org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_DELETE_TEMPLATE;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_INSERT_TEMPLATE;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_LIST_TEMPLATES;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_SELECT_TEMPLATE_BY_NAME;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_TEMPLATE;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.SqlQueries.DELETE_TEMPLATE;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.SqlQueries.GET_TEMPLATE_BY_NAME;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.SqlQueries.LIST_PAGINATED_TEMPLATES_DB2;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.SqlQueries.LIST_PAGINATED_TEMPLATES_MSSQL;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.SqlQueries.LIST_PAGINATED_TEMPLATES_MYSQL;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.SqlQueries.LIST_PAGINATED_TEMPLATES_ORACLE;
import static org.wso2.carbon.identity.template.mgt.util.JdbcUtils.isDB2DB;
import static org.wso2.carbon.identity.template.mgt.util.JdbcUtils.isH2MySqlOrPostgresDB;
import static org.wso2.carbon.identity.template.mgt.util.JdbcUtils.isMSSqlDB;

/**
 * Perform CRUD operations for {@link Template}.
 *
 * @since 1.0.0
 */
public class TemplateManagerDAOImpl implements TemplateManagerDAO {

    /**
     * Add a {@link Template}.
     *
     * @param template {@link Template} to insert.
     * @return Inserted {@link TemplateInfo}.
     * @throws TemplateManagementException If error occurs while adding the {@link Template}.
     */
    public Template addTemplate(Template template) throws TemplateManagementException {

        Template templateResult;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            jdbcTemplate.executeUpdate(TemplateMgtConstants.SqlQueries.INSERT_TEMPLATE, preparedStatement -> {
                preparedStatement.setInt(1, template.getTenantId());
                preparedStatement.setString(2, template.getTemplateName());
                preparedStatement.setString(3, template.getDescription());
                InputStream inputStream = IOUtils.toInputStream(template.getTemplateScript());
                try {
                    preparedStatement.setBinaryStream(4, inputStream, inputStream.available());
                } catch (IOException e) {
                    // SQLException is thrown since the QueryFilter throws an SQLException
                    throw TemplateMgtUtils.handleSQLException(ERROR_CODE_INSERT_TEMPLATE,
                            template.getTemplateName(), e);
                }
            });
        } catch (DataAccessException e) {
            throw TemplateMgtUtils.handleServerException(ERROR_CODE_INSERT_TEMPLATE, template.getTemplateName(), e);
        }
        templateResult = new Template(template.getTenantId(), template.getTemplateName(), template.getDescription(),
                template.getTemplateScript());
        return templateResult;
    }

    /**
     * Retrieve {@link Template} by template name and tenant Id.
     *
     * @param templateName name of the {@link Template} to retrieve.
     * @param tenantId     tenant Id of the tenant which the {@link Template} resides.
     * @return {@link Template} for the given name and tenant Id.
     * @throws TemplateManagementException If error occurs while retrieving {@link Template}.
     */
    public Template getTemplateByName(String templateName, Integer tenantId) throws TemplateManagementException {

        Template template;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            template = jdbcTemplate.fetchSingleRecord(GET_TEMPLATE_BY_NAME, ((resultSet, rowNumber) ->
                    {
                        Template templateResult;
                        try {
                            templateResult = new Template(
                                    resultSet.getString(1),
                                    resultSet.getString(2),
                                    IOUtils.toString(resultSet.getBinaryStream(3)));
                        } catch (IOException e) {
                            // SQLException is thrown since the QueryFilter throws an SQLException
                            throw new TemplateManagementSQLException(String
                                    .format(ERROR_CODE_SELECT_TEMPLATE_BY_NAME.getMessage(), tenantId, templateName),
                                    ERROR_CODE_SELECT_TEMPLATE_BY_NAME.getCode(), e);
                        }
                        return templateResult;
                    }),
                    preparedStatement -> {
                        preparedStatement.setString(1, templateName);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw new TemplateManagementServerException(String.format(ERROR_CODE_SELECT_TEMPLATE_BY_NAME.getMessage(),
                    tenantId, templateName),
                    ERROR_CODE_SELECT_TEMPLATE_BY_NAME.getCode(), e);
        }
        return template;
    }

    /**
     * List {@link TemplateInfo} items for a given search criteria.
     *
     * @param tenantId Tenant Id to be searched.
     * @param limit    Maximum number of results expected.
     * @param offset   Result offset.
     * @return List of {@link TemplateInfo} entries.
     * @throws TemplateManagementException If error occurs while searching the Templates.
     */
    public List<TemplateInfo> getAllTemplates(Integer tenantId, Integer limit, Integer offset)
            throws TemplateManagementException {

        List<TemplateInfo> templates;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            String query;
            if (isH2MySqlOrPostgresDB()) {
                query = LIST_PAGINATED_TEMPLATES_MYSQL;
            } else if (isDB2DB()) {
                query = LIST_PAGINATED_TEMPLATES_DB2;
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (isMSSqlDB()) {
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
                query = LIST_PAGINATED_TEMPLATES_MSSQL;
            } else { //oracle
                query = LIST_PAGINATED_TEMPLATES_ORACLE;
                limit = offset + limit;
            }
            int finalLimit = limit;
            int finalOffset = offset;

            templates = jdbcTemplate.executeQuery(query, (resultSet, rowNumber) ->
                            new TemplateInfo(resultSet.getString(1),
                                    resultSet.getString(2)),
                    preparedStatement -> {
                        preparedStatement.setInt(1, tenantId);
                        preparedStatement.setInt(2, finalLimit);
                        preparedStatement.setInt(3, finalOffset);
                    });
        } catch (DataAccessException e) {
            throw new TemplateManagementServerException(String.format(ERROR_CODE_LIST_TEMPLATES.getMessage(),
                    tenantId, limit, offset),
                    ERROR_CODE_LIST_TEMPLATES.getCode(), e);
        }
        return templates;
    }

    /**
     * Update a {@link Template}.
     *
     * @param templateName name of the to be updated {@link Template}.
     * @param newTemplate  new {@link Template} to insert.
     * @return Inserted {@link Template}.
     * @throws TemplateManagementException If error occurs while adding the {@link Template}.
     */
    public Template updateTemplate(String templateName, Template newTemplate)
            throws TemplateManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(TemplateMgtConstants.SqlQueries.UPDATE_TEMPLATE, (preparedStatement -> {
                preparedStatement.setString(1, newTemplate.getTemplateName());
                preparedStatement.setString(2, newTemplate.getDescription());
                try {
                    InputStream inputStream = IOUtils.toInputStream(newTemplate.getTemplateScript());
                    preparedStatement.setBinaryStream(3, inputStream, inputStream.available());

                } catch (IOException e) {
                    // SQLException is thrown since the QueryFilter throws an SQLException
                    throw TemplateMgtUtils.handleSQLException(ERROR_CODE_UPDATE_TEMPLATE,
                            newTemplate.getTemplateName(), e);

                }
                preparedStatement.setInt(4, newTemplate.getTenantId());
                preparedStatement.setString(5, templateName);
            }));
        } catch (DataAccessException e) {
            throw TemplateMgtUtils.handleServerException(ERROR_CODE_UPDATE_TEMPLATE, newTemplate.getTemplateName(), e);
        }
        return new Template(newTemplate.getTenantId(), newTemplate.getTemplateName(),
                newTemplate.getDescription(), newTemplate.getTemplateScript());
    }

    /**
     * Delete {@link Template} for a given template name and a tenant Id.
     *
     * @param templateName name of the {@link Template} to be deleted.the tenant
     * @param tenantId     tenant Id of the tenant which the {@link Template} resides.
     * @return TemplateInfo of the deleted {@link Template}.
     * @throws TemplateManagementException If error occurs while deleting the {@link Template}
     */
    public TemplateInfo deleteTemplate(String templateName, Integer tenantId) throws TemplateManagementException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_TEMPLATE, preparedStatement -> {
                preparedStatement.setString(1, templateName);
                preparedStatement.setInt(2, tenantId);
            });
        } catch (DataAccessException e) {
            throw new TemplateManagementServerException(String.format(ERROR_CODE_DELETE_TEMPLATE.getMessage(),
                    tenantId.toString(), templateName),
                    ERROR_CODE_DELETE_TEMPLATE.getCode(), e);
        }
        return new TemplateInfo(tenantId, templateName);
    }
}
