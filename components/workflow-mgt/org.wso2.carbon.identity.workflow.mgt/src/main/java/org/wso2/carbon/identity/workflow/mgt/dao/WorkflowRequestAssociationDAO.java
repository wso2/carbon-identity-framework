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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowAssociation;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WorkflowRequestAssociationDAO {

    private static final Log log = LogFactory.getLog(WorkflowRequestAssociationDAO.class);

    /**
     * Adds new workflow-request relationship to database
     *
     * @param relationshipId
     * @param workflowId
     * @param requestId
     * @param status
     * @throws InternalWorkflowException
     */
    public void addNewRelationship(String relationshipId, String workflowId, String requestId, String status,
                                   int tenantId) throws InternalWorkflowException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_WORKFLOW_REQUEST_RELATIONSHIP;
        try {
            Timestamp createdDateStamp = new Timestamp(System.currentTimeMillis());
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, relationshipId);
            prepStmt.setString(2, workflowId);
            prepStmt.setString(3, requestId);
            prepStmt.setTimestamp(4, createdDateStamp);
            prepStmt.setString(5, status);
            prepStmt.setInt(6, tenantId);
            prepStmt.execute();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Get requestId of a relationship.
     *
     * @param relationshipId
     * @return
     * @throws InternalWorkflowException
     */
    public String getRequestIdOfRelationship(String relationshipId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_REQUEST_ID_OF_RELATIONSHIP;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, relationshipId);
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(SQLConstants.REQUEST_ID_COLUMN);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return "";
    }

    /**
     * Update state of workflow of a request
     *
     * @param relationshipId
     * @throws InternalWorkflowException
     */
    public void updateStatusOfRelationship(String relationshipId, String status) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_STATUS_OF_RELATIONSHIP;
        try {
            Timestamp updatedDateStamp = new Timestamp(System.currentTimeMillis());
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, status);
            prepStmt.setTimestamp(2, updatedDateStamp);
            prepStmt.setString(3, relationshipId);
            prepStmt.execute();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Update state of workflow of a request
     *
     * @param requestId requestId to update relationships of.
     * @throws InternalWorkflowException
     */
    public void updateStatusOfRelationshipsOfPendingRequest(String requestId, String status) throws
                                                                                             InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_STATUS_OF_RELATIONSHIPS_OF_REQUEST;
        try {
            Timestamp updatedDateStamp = new Timestamp(System.currentTimeMillis());
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, status);
            prepStmt.setTimestamp(2, updatedDateStamp);
            prepStmt.setString(3, requestId);
            prepStmt.setString(4, WFConstant.HT_STATE_PENDING);
            prepStmt.execute();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Get list of states of workflows of a request
     *
     * @param requestId
     * @return
     * @throws InternalWorkflowException
     */
    public List<String> getWorkflowStatesOfRequest(String requestId) throws InternalWorkflowException {

        List<String> states = new ArrayList<>();
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_STATES_OF_REQUEST;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, requestId);
            resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                states.add(resultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN));
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return states;
    }

    /**
     * Get requestId of a relationship.
     *
     * @param relationshipId
     * @return
     * @throws InternalWorkflowException
     */
    public String getStatusOfRelationship(String relationshipId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_STATUS_OF_RELATIONSHIP;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, relationshipId);
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return "";
    }

    /**
     * Get array of Workflows of a request
     *
     * @param requestId
     * @return
     * @throws InternalWorkflowException
     */
    public WorkflowRequestAssociation[] getWorkflowsOfRequest(String requestId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_WORKFLOWS_OF_REQUEST;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, requestId);
            resultSet = prepStmt.executeQuery();
            ArrayList<WorkflowRequestAssociation> workflowDTOs = new ArrayList<>();
            while (resultSet.next()) {
                WorkflowRequestAssociation workflowDTO = new WorkflowRequestAssociation();
                workflowDTO.setWorkflowId(resultSet.getString(SQLConstants.ID_COLUMN));
                workflowDTO.setWorkflowName(resultSet.getString(SQLConstants.WF_NAME_COLUMN));
                workflowDTO.setLastUpdatedTime(resultSet.getTimestamp(SQLConstants.REQUEST_UPDATED_AT_COLUMN)
                                                       .toString());
                workflowDTO.setStatus(resultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN));
                workflowDTOs.add(workflowDTO);
            }
            WorkflowRequestAssociation[] requestArray = new WorkflowRequestAssociation[workflowDTOs.size()];
            for (int i = 0; i < workflowDTOs.size(); i++) {
                requestArray[i] = workflowDTOs.get(i);
            }
            return requestArray;
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
    }

    /**
     *
     * @param eventId
     * @param tenantId
     * @return
     * @throws InternalWorkflowException
     */
    public List<WorkflowAssociation> getWorkflowAssociationsForRequest(String eventId, int tenantId)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs;
        List<WorkflowAssociation> associations = new ArrayList<>();
        String query = SQLConstants.GET_ASSOCIATIONS_FOR_EVENT_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, eventId);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {

                int id = rs.getInt(SQLConstants.ID_COLUMN);
                String condition = rs.getString(SQLConstants.CONDITION_COLUMN);
                String workflowId = rs.getString(SQLConstants.WORKFLOW_ID_COLUMN);
                String associationName = rs.getString(SQLConstants.ASSOCIATION_NAME_COLUMN);

                WorkflowAssociation association = new WorkflowAssociation();
                association.setWorkflowId(workflowId);
                association.setAssociationCondition(condition);
                association.setEventId(eventId);
                association.setAssociationId(id);
                association.setAssociationName(associationName);

                associations.add(association);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return associations;
    }

}
