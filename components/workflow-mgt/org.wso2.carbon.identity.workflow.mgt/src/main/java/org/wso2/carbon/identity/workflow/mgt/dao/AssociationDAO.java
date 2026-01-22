/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowClientException;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.wso2.carbon.identity.workflow.mgt.util.Utils.generatePrepStmt;

/**
 * Association related DAO operation provides by this class.
 */
public class AssociationDAO {

    private static final String errorMessage = "Error when executing the SQL query ";
    private static final Log log = LogFactory.getLog(AssociationDAO.class);

    /**
     * Add a new association between workflow and event.
     *
     * @param associationName Name of the association
     * @param workflowId Workflow identifier
     * @param eventId Event identifier
     * @param condition Association condition
     * @throws InternalWorkflowException If an error occurs during database operations
     */
    public void addAssociation(String associationName, String workflowId, String eventId, String condition)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.ASSOCIATE_WF_TO_EVENT;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, eventId);
            prepStmt.setString(2, associationName);
            prepStmt.setString(3, condition);
            prepStmt.setString(4, workflowId);
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
     * Update an existing association.
     *
     * @param associationDTO Association data transfer object containing updated information
     * @throws InternalWorkflowException If an error occurs during database operations
     * @throws WorkflowClientException If the association data is invalid
     */
    public void updateAssociation(Association associationDTO)
            throws InternalWorkflowException, WorkflowClientException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        PreparedStatement prepStmt = null;

        String query = SQLConstants.UPDATE_ASSOCIATE_WF_TO_EVENT;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, associationDTO.getEventId());
            prepStmt.setString(2, associationDTO.getAssociationName());
            prepStmt.setString(3, associationDTO.getCondition());
            prepStmt.setString(4, associationDTO.getWorkflowId());
            if (associationDTO.isEnabled()) {
                prepStmt.setString(5, "1");
            } else {
                prepStmt.setString(5, "0");
            }
            // As the WF_WORKFLOW_ASSOCIATION.ID is integer, this has to be set as an int to work with postgres.
            prepStmt.setInt(6, Integer.parseInt(associationDTO.getAssociationId()));
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
            if (log.isDebugEnabled()) {
                log.debug("Successfully updated association with ID: " + associationDTO.getAssociationId());
            }
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException(errorMessage, e);
        } catch (NumberFormatException e) {
            throw new WorkflowClientException("Invalid association ID: " + associationDTO.getAssociationId(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Retrieve associations of a tenant with pagination.
     *
     * @param tenantId Tenant ID
     * @param filter Filter string
     * @param offset Offset for pagination
     * @param limit Limit for pagination
     * @return List<Association> List of associations
     * @throws InternalWorkflowException
     * @throws WorkflowClientException
     */
    public List<Association> listPaginatedAssociations(int tenantId, String filter, int offset, int limit)
            throws InternalWorkflowException, WorkflowClientException {

        if (log.isDebugEnabled()) {
            log.debug("Listing paginated associations for tenant: " + tenantId + " with filter: " + filter +
                    ", offset: " + offset + ", limit: " + limit);
        }

        String sqlQuery;
        List<Association> associations = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            String filterField = null;
            String filterValue = StringUtils.EMPTY;
            if (filter != null && filter.contains(" eq ")) {
                String[] parts = filter.split(" eq ", 2);
                filterField = parts[0].trim();
                filterValue = parts[1].trim();
            } else if (!Objects.equals(filter, "*") && filter != null) {
                filterField = filter.trim();
            }
            String filterResolvedForSQL = resolveSQLFilter(filterValue);
            sqlQuery = getSqlQuery(filterField);
            PreparedStatement prepStmt;

            if (SQLConstants.WORKFLOW_ID_FILTER.equalsIgnoreCase(filterField)) {
                prepStmt = generatePrepStmtWithoutLimitOffset(connection, sqlQuery, tenantId, filterResolvedForSQL);
            } else {
                prepStmt = generatePrepStmt(connection, sqlQuery, tenantId, filterResolvedForSQL, offset, limit);
            }

            try (PreparedStatement stmt = prepStmt) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        String condition = resultSet.getString(SQLConstants.CONDITION_COLUMN);
                        String eventId = resultSet.getString(SQLConstants.EVENT_ID_COLUMN);
                        String associationId = String.valueOf(resultSet.getInt(SQLConstants.ID_COLUMN));
                        String associationName = resultSet.getString(SQLConstants.ASSOCIATION_NAME_COLUMN);
                        String workflowName = resultSet.getString(SQLConstants.WF_NAME_COLUMN);
                        String isEnable = resultSet.getString(SQLConstants.ASSOCIATION_IS_ENABLED);
                        Association associationDTO = createAssociation(condition, associationId, eventId,
                                associationName, workflowName);
                        associationDTO.setEnabled(isEnable.equals("1"));
                        associations.add(associationDTO);
                    }
                }
            }
        } catch (SQLException e) {
            handleException(WFConstant.Exceptions.SQL_ERROR_LISTING_ASSOCIATIONS, e);
        } catch (DataAccessException e) {
            handleException(e.getMessage(), e);
        }
        return associations;
    }

    /**
     * @param tenantId Tenant ID
     * @return
     * @throws InternalWorkflowException
     * @Deprecated Use {@link #listPaginatedAssociations(int, String, int, int)} instead.
     */
    @Deprecated
    public List<Association> listAssociations(int tenantId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs;
        List<Association> associations = new ArrayList<>();
        String query = SQLConstants.GET_ALL_ASSOCIATIONS_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String condition = rs.getString(SQLConstants.CONDITION_COLUMN);
                String eventId = rs.getString(SQLConstants.EVENT_ID_COLUMN);
                String associationId = String.valueOf(rs.getInt(SQLConstants.ID_COLUMN));
                String associationName = rs.getString(SQLConstants.ASSOCIATION_NAME_COLUMN);
                String workflowName = rs.getString(SQLConstants.WF_NAME_COLUMN);
                String isEnable = rs.getString(SQLConstants.ASSOCIATION_IS_ENABLED);
                Association associationDTO = createAssociation(condition, associationId, eventId, associationName,
                        workflowName);
                associationDTO.setEnabled(isEnable.equals("1"));
                associations.add(associationDTO);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return associations;
    }

    /**
     * Get associations count of a tenant.
     *
     * @param tenantId
     * @param filter
     * @return Return associations count
     * @throws InternalWorkflowException
     */
    public int getAssociationsCount(int tenantId, String filter) throws InternalWorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Getting associations count for tenant: " + tenantId + " with filter: " + filter);
        }

        int count = 0;
        String filterField = null;
        String filterValue = StringUtils.EMPTY;
        if (filter != null && filter.contains(" eq ")) {
            String[] parts = filter.split(" eq ", 2);
            filterField = parts[0].trim();
            filterValue = parts[1].trim();
        } else if (!Objects.equals(filter, "*") && filter != null) {
            filterField = filter.trim();
        }
        String sql = SQLConstants.GET_ASSOCIATIONS_COUNT_QUERY_BY_OPERATION;
        if (SQLConstants.WORKFLOW_ID_FILTER.equals(filterField)) {
            sql = SQLConstants.GET_ASSOCIATIONS_COUNT_QUERY_BY_WORKFLOW_ID;
        }
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            String filterResolvedForSQL = resolveSQLFilter(filterValue);
            try (PreparedStatement prepStmt = connection
                    .prepareStatement(sql)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.setString(2, filterResolvedForSQL);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            handleException(WFConstant.Exceptions.SQL_ERROR_GETTING_ASSOC_COUNT, e);
        }
        return count;
    }

    /**
     * @param associationId
     * @return
     * @throws InternalWorkflowException WorkflowClientException
     */
    public Association getAssociation(String associationId) throws InternalWorkflowException, WorkflowClientException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs;
        Association associationDTO = null;
        String query = SQLConstants.GET_ASSOCIATION_FOR_ASSOC_ID_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            // As the WF_WORKFLOW_ASSOCIATION.ID is integer, this has to be set as an int to work with postgre.
            prepStmt.setInt(1, Integer.parseInt(associationId));
            prepStmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                String condition = rs.getString(SQLConstants.CONDITION_COLUMN);
                String eventId = rs.getString(SQLConstants.EVENT_ID_COLUMN);
                String associationName = rs.getString(SQLConstants.ASSOCIATION_NAME_COLUMN);
                String workflowName = rs.getString(SQLConstants.WF_NAME_COLUMN);
                String workflowId = rs.getString(SQLConstants.WORKFLOW_ID_COLUMN);
                String isEnable = rs.getString(SQLConstants.ASSOCIATION_IS_ENABLED);
                associationDTO = createAssociation(condition, associationId, eventId, associationName, workflowName);
                associationDTO.setWorkflowId(workflowId);
                associationDTO.setEnabled(isEnable.equals("1"));
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } catch (NumberFormatException e) {
            throw new WorkflowClientException("Invalid association ID: " + associationId, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return associationDTO;
    }

    /**
     * @param id
     * @throws InternalWorkflowException
     */
    public void removeAssociation(int id) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_ASSOCIATION_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, id);
            prepStmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
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
     * @param workflowId
     * @return
     * @throws InternalWorkflowException
     */
    public List<Association> listAssociationsForWorkflow(String workflowId)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<Association> associations = new ArrayList<>();
        String query = SQLConstants.GET_ASSOCIATIONS_FOR_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            prepStmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String condition = rs.getString(SQLConstants.CONDITION_COLUMN);
                String eventId = rs.getString(SQLConstants.EVENT_ID_COLUMN);
                String associationId = String.valueOf(rs.getInt(SQLConstants.ID_COLUMN));
                String associationName = rs.getString(SQLConstants.ASSOCIATION_NAME_COLUMN);
                String workflowName = rs.getString(SQLConstants.WF_NAME_COLUMN);
                associations.add(createAssociation(condition, associationId, eventId, associationName, workflowName));
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, prepStmt);
        }
        return associations;
    }

    private Association createAssociation(String condition, String associationId, String eventId,
                                          String associationName, String workflowName) {

        Association associationDTO = new Association();
        associationDTO.setCondition(condition);
        associationDTO.setAssociationId(associationId);
        associationDTO.setEventId(eventId);
        associationDTO.setAssociationName(associationName);
        associationDTO.setWorkflowName(workflowName);
        return associationDTO;
    }

    /**
     * Generate an SQL query that allows filtering using either the association name or the workflow ID.
     *
     * @param filterField The field to filter by
     * @return The appropriate SQL query string
     * @throws InternalWorkflowException
     * @throws DataAccessException
     * @throws WorkflowClientException
     */
    private String getSqlQuery(String filterField)
            throws InternalWorkflowException, DataAccessException, WorkflowClientException {

        String sqlQuery;
        if (filterField == null || SQLConstants.OPERATION_FILTER.equalsIgnoreCase(filterField)) {
            if (JdbcUtils.isH2DB() || JdbcUtils.isMySQLDB() || JdbcUtils.isMariaDB()) {
                sqlQuery = SQLConstants.GET_ASSOCIATIONS_BY_TENANT_AND_ASSOC_NAME_MYSQL;
            } else if (JdbcUtils.isOracleDB()) {
                sqlQuery = SQLConstants.GET_ASSOCIATIONS_BY_TENANT_AND_ASSOC_NAME_ORACLE;
            } else if (JdbcUtils.isMSSqlDB()) {
                sqlQuery = SQLConstants.GET_ASSOCIATIONS_BY_TENANT_AND_ASSOC_NAME_MSSQL;
            } else if (JdbcUtils.isPostgreSQLDB()) {
                sqlQuery = SQLConstants.GET_ASSOCIATIONS_BY_TENANT_AND_ASSOC_NAME_POSTGRESQL;
            } else if (JdbcUtils.isDB2DB()) {
                sqlQuery = SQLConstants.GET_ASSOCIATIONS_BY_TENANT_AND_ASSOC_NAME_DB2SQL;
            } else if (JdbcUtils.isInformixDB()) {
                sqlQuery = SQLConstants.GET_ASSOCIATIONS_BY_TENANT_AND_ASSOC_NAME_INFORMIX;
            } else {
                throw new InternalWorkflowException(WFConstant.Exceptions.ERROR_WHILE_LOADING_ASSOCIATIONS);
            }
        } else if (SQLConstants.WORKFLOW_ID_FILTER.equalsIgnoreCase(filterField)) {
            sqlQuery = SQLConstants.GET_ASSOCIATIONS_BY_TENANT_AND_WORKFLOW;
        } else {
            throw new WorkflowClientException("Unsupported filter field: " + filterField);
        }

        return sqlQuery;
    }

    /**
     * Create PreparedStatement.
     *
     * @param connection           db connection
     * @param sqlQuery             SQL query
     * @param tenantId             Tenant ID
     * @param filterResolvedForSQL resolved filter for sql
     * @return PreparedStatement
     * @throws SQLException
     * @throws DataAccessException
     */
    private PreparedStatement generatePrepStmtWithoutLimitOffset(Connection connection, String sqlQuery, int tenantId,
                                                                 String filterResolvedForSQL)
            throws SQLException, DataAccessException {

        PreparedStatement prepStmt;
        prepStmt = connection.prepareStatement(sqlQuery);
        prepStmt.setInt(1, tenantId);
        prepStmt.setString(2, filterResolvedForSQL);
        return prepStmt;
    }

    /**
     * Resolve SQL Filter.
     *
     * @param filter
     * @return Return SQL filter
     * @throws InternalWorkflowException
     */
    private String resolveSQLFilter(String filter) {

        // To avoid any issues when the filter string is blank or null, assigning "%" to SQLFilter.
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
     * @param e        Exception
     * @throws InternalWorkflowException
     */
    private void handleException(String errorMsg, Exception e) throws InternalWorkflowException {

        if (log.isDebugEnabled()) {
            log.debug(errorMsg, e);
        }
        throw new InternalWorkflowException(errorMsg, e);
    }
}
