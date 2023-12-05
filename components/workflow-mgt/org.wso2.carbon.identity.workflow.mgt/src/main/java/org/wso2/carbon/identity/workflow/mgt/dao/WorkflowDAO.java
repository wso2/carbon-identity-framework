/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.workflow.mgt.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow related DAO operation provides by this class
 *
 */
public class WorkflowDAO {

    private final String errorMessage = "Error when executing the SQL query ";
    private static final Log log = LogFactory.getLog(WorkflowDAO.class);

    /**
     * Adding a workflow
     *
     * @param workflow Workflow bean object
     * @param tenantId Tenant ID
     * @throws InternalWorkflowException
     */
    public void addWorkflow(Workflow workflow, int
            tenantId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflow.getWorkflowId());
            prepStmt.setString(2, workflow.getWorkflowName());
            prepStmt.setString(3, workflow.getWorkflowDescription());
            prepStmt.setString(4, workflow.getTemplateId());
            prepStmt.setString(5, workflow.getWorkflowImplId());
            prepStmt.setInt(6, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage , e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Get a Workflow object for given workflowid
     *
     * @param workflowId Workflow unique id
     * @return Workflow object
     * @throws InternalWorkflowException
     */
    public Workflow getWorkflow(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = SQLConstants.GET_WORKFLOW;

        Workflow workflow = null;

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String workflowName = rs.getString(SQLConstants.WF_NAME_COLUMN);
                String description = rs.getString(SQLConstants.DESCRIPTION_COLUMN);
                String templateId = rs.getString(SQLConstants.TEMPLATE_ID_COLUMN);
                String implId = rs.getString(SQLConstants.TEMPLATE_IMPL_ID_COLUMN);
                workflow = new Workflow();
                workflow.setWorkflowId(workflowId);
                workflow.setWorkflowName(workflowName);
                workflow.setWorkflowDescription(description);
                workflow.setTemplateId(templateId);
                workflow.setWorkflowImplId(implId);

                break;
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return workflow;
    }

    /**
     * Remove Workflow from the DB
     *
     * @param workflowId workflow Id
     * @throws InternalWorkflowException
     */
    public void removeWorkflow(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Remove all workflows of a given tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws InternalWorkflowException throws when an error occurs in removing workflows.
     */
    public void removeWorkflows(int tenantId) throws InternalWorkflowException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                    .DELETE_WORKFLOW_BY_TENANT_ID_QUERY)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                throw new InternalWorkflowException(errorMessage, e);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        }
    }

    /**
     * Update current workflow
     *
     * @param workflow Workflow object
     * @throws InternalWorkflowException
     */
    public void updateWorkflow(Workflow workflow)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflow.getWorkflowName());
            prepStmt.setString(2, workflow.getWorkflowDescription());
            prepStmt.setString(3, workflow.getTemplateId());
            prepStmt.setString(4, workflow.getWorkflowImplId());
            prepStmt.setString(5, workflow.getWorkflowId());
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Retrieve Workflows of a tenant with pagination.
     *
     * @param tenantId
     * @param filter
     * @param offset
     * @param limit
     * @return List<Workflow>
     * @throws InternalWorkflowException
     */
    public List<Workflow> listPaginatedWorkflows(int tenantId, String filter, int offset, int limit) throws InternalWorkflowException {

        String sqlQuery;
        List<Workflow> workflowList = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            String filterResolvedForSQL = resolveSQLFilter(filter);
            sqlQuery = getSqlQuery();
            try (PreparedStatement prepStmt = generatePrepStmt(connection, sqlQuery, tenantId, filterResolvedForSQL, offset, limit);) {
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    while (resultSet.next()) {
                        String id = resultSet.getString(SQLConstants.ID_COLUMN);
                        String name = resultSet.getString(SQLConstants.WF_NAME_COLUMN);
                        String description = resultSet.getString(SQLConstants.DESCRIPTION_COLUMN);
                        String templateId = resultSet.getString(SQLConstants.TEMPLATE_ID_COLUMN);
                        String templateImplId = resultSet.getString(SQLConstants.TEMPLATE_IMPL_ID_COLUMN);
                        Workflow workflowDTO = new Workflow();
                        workflowDTO.setWorkflowId(id);
                        workflowDTO.setWorkflowName(name);
                        workflowDTO.setWorkflowDescription(description);
                        workflowDTO.setTemplateId(templateId);
                        workflowDTO.setWorkflowImplId(templateImplId);
                        workflowList.add(workflowDTO);
                    }
                }
            }
        } catch (SQLException e) {
            handleException(WFConstant.Exceptions.SQL_ERROR_LISTING_WORKFLOWS, e);
        } catch (DataAccessException e) {
            handleException(e.getMessage(), e);
        }
        return workflowList;
    }

    /**
     * Retrieve all the Workflows for a tenant
     *
     * @Deprecated Use {@link #listPaginatedWorkflows(int, String, int, int)} instead.
     * @param tenantId Tenant ID
     * @return List<Workflow>
     * @throws InternalWorkflowException
     */
    @Deprecated
    public List<Workflow> listWorkflows(int tenantId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<Workflow> workflowList = new ArrayList<>();
        String query = SQLConstants.LIST_WORKFLOWS_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString(SQLConstants.ID_COLUMN);
                String name = rs.getString(SQLConstants.WF_NAME_COLUMN);
                String description = rs.getString(SQLConstants.DESCRIPTION_COLUMN);
                String templateId = rs.getString(SQLConstants.TEMPLATE_ID_COLUMN);
                String templateImplId = rs.getString(SQLConstants.TEMPLATE_IMPL_ID_COLUMN);
                Workflow workflowDTO = new Workflow();
                workflowDTO.setWorkflowId(id);
                workflowDTO.setWorkflowName(name);
                workflowDTO.setWorkflowDescription(description);
                workflowDTO.setTemplateId(templateId);
                workflowDTO.setWorkflowImplId(templateImplId);
                workflowList.add(workflowDTO);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return workflowList;
    }

    /**
     * Get workflows count of a tenant.
     *
     * @param tenantId
     * @param filter
     * @return Return workflows count
     * @throws InternalWorkflowException
     */
    public int getWorkflowsCount(int tenantId, String filter) throws InternalWorkflowException{

        int count = 0;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            String filterResolvedForSQL = resolveSQLFilter(filter);
            try (PreparedStatement prepStmt = connection
                    .prepareStatement(SQLConstants.GET_WORKFLOWS_COUNT_QUERY)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.setString(2, filterResolvedForSQL);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            handleException(WFConstant.Exceptions.SQL_ERROR_GETTING_WORKFLOW_COUNT, e);
        }
        return count;
    }


    /**
     * Clear all the parameters that stored under workflow Id
     *
     * @param workflowId WorkflowId
     * @throws InternalWorkflowException
     */
    public void removeWorkflowParams(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_WORKFLOW_PARAMS_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Clear all the parameters of all the workflows of a given tenant.
     *
     * @param tenantId The id of the tenant.
     * @throws InternalWorkflowException throws when an error occurs in removing workflows.
     */
    public void removeWorkflowParams(int tenantId) throws InternalWorkflowException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants
                    .DELETE_WORKFLOW_PARAMS_BY_TENANT_ID_QUERY)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                throw new InternalWorkflowException(errorMessage, e);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        }
    }
    
    /**
     * Add new parameter List to given workflow id
     *
     * @param parameterList Paramter List
     * @param workflowId Workflow Id
     * @throws InternalWorkflowException
     */
    public void addWorkflowParams(List<Parameter> parameterList, String workflowId, int tenantId) throws
            InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ADD_WORKFLOW_PARAMS_QUERY;
        try {
            for (Parameter parameter : parameterList) {
                prepStmt = connection.prepareStatement(query);
                prepStmt.setString(1, workflowId);
                prepStmt.setString(2, parameter.getParamName());
                prepStmt.setString(3, parameter.getParamValue());
                prepStmt.setString(4, parameter.getqName());
                prepStmt.setString(5, parameter.getHolder());
                prepStmt.setInt(6, tenantId);

                prepStmt.executeUpdate();
            }
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Retrieve List of Parameters for given workflow id
     *
     * @param workflowId
     * @return
     * @throws InternalWorkflowException
     */
    public List<Parameter> getWorkflowParams(String workflowId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<Parameter> parameterList = new ArrayList<>();
        String query = SQLConstants.GET_WORKFLOW_PARAMS;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String paramName = rs.getString(SQLConstants.PARAM_NAME_COLUMN);
                String paramValue = rs.getString(SQLConstants.PARAM_VALUE_COLUMN);
                String paramQName = rs.getString(SQLConstants.PARAM_QNAME_COLUMN);
                String paramHolder = rs.getString(SQLConstants.PARAM_HOLDER_COLUMN);
                if (StringUtils.isNotBlank(paramName)) {
                    Parameter parameter = new Parameter(workflowId, paramName, paramValue, paramQName, paramHolder);
                    parameterList.add(parameter);
                }
            }
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return parameterList;
    }

    /**
     *
     * @throws InternalWorkflowException
     * @throws DataAccessException
     */
    private String getSqlQuery() throws InternalWorkflowException, DataAccessException {

        String sqlQuery ;
        if (JdbcUtils.isH2DB() || JdbcUtils.isMySQLDB() || JdbcUtils.isMariaDB()) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_MYSQL;
        } else if (JdbcUtils.isOracleDB()) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_ORACLE;
        } else if (JdbcUtils.isMSSqlDB()) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_MSSQL;
        } else if (JdbcUtils.isPostgreSQLDB()) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_POSTGRESQL;
        } else if (JdbcUtils.isDB2DB()) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_DB2SQL;
        } else if (JdbcUtils.isInformixDB()) {
            sqlQuery = SQLConstants.GET_WORKFLOWS_BY_TENANT_AND_WF_NAME_INFORMIX;
        } else {
            throw new InternalWorkflowException(WFConstant.Exceptions.ERROR_WHILE_LOADING_WORKFLOWS);
        }
        return sqlQuery;
    }

    /**
     * Create PreparedStatement.
     *
     * @param connection db connection
     * @param sqlQuery SQL query
     * @param tenantId Tenant ID
     * @param filterResolvedForSQL resolved filter for sql
     * @param offset offset
     * @param limit limit
     * @return PreparedStatement
     * @throws SQLException
     * @throws DataAccessException
     */
    private PreparedStatement generatePrepStmt(Connection connection, String sqlQuery, int tenantId, String filterResolvedForSQL, int offset, int limit) throws SQLException, DataAccessException {

        PreparedStatement prepStmt;
        if (JdbcUtils.isPostgreSQLDB()) {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
        } else {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, offset);
            prepStmt.setInt(4, limit);
        }
        return prepStmt;
    }

    /**
     * Resolve SQL Filter.
     *
     * @param filter
     * @return Return SQL filter.
     * @throws InternalWorkflowException
     */
    private String resolveSQLFilter(String filter) {

        //To avoid any issues when the filter string is blank or null, assigning "%" to SQLFilter.
        String sqlFilter = "%";
        if (StringUtils.isNotBlank(filter)) {
            sqlFilter = filter.trim()
                    .replace("*", "%")
                    .replace("?", "_");
        }
        return sqlFilter;
    }

    /**
     * Logs and wraps the given exception.
     *
     * @param errorMsg Error message
     * @param e   Exception
     * @throws InternalWorkflowException
     */
    private void handleException(String errorMsg, Exception e) throws InternalWorkflowException {

        if (log.isDebugEnabled()) {
            log.debug(errorMsg, e);
        }
        throw new InternalWorkflowException(errorMsg, e);
    }
}
