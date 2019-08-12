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

import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestEntityRelationshipDAO {

    /**
     * Add a new relationship between a workflow request and an entity.
     *
     * @param entity
     * @param uuid
     * @throws InternalWorkflowException
     */
    public void addRelationship(Entity entity, String uuid) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_REQUEST_ENTITY_RELATIONSHIP;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, uuid);
            prepStmt.setString(2, entity.getEntityId());
            prepStmt.setString(3, entity.getEntityType());
            prepStmt.setInt(4, entity.getTenantId());
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException("Error when executing the sql query", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Delete existing relationships of a request.
     *
     * @param uuid
     * @throws InternalWorkflowException
     */
    public void deleteRelationshipsOfRequest(String uuid) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.DELETE_REQUEST_ENTITY_RELATIONSHIP;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, uuid);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException("Error when executing the sql query", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Check if a given entity has any pending workflow requests associated with it.
     *
     * @param entity
     * @return
     * @throws InternalWorkflowException
     */
    public boolean entityHasPendingWorkflows(Entity entity) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_PENDING_RELATIONSHIPS_OF_ENTITY;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, entity.getEntityType());
            prepStmt.setString(2, entity.getEntityId());
            prepStmt.setString(3, WorkflowRequestStatus.PENDING.toString());
            prepStmt.setInt(4, entity.getTenantId());
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return false;
    }

    /**
     * Check if a given entity as any pending workflows of a given type associated with it.
     *
     * @param entity
     * @param requsetType
     * @return
     * @throws InternalWorkflowException
     */
    public boolean entityHasPendingWorkflowsOfType(Entity entity, String requsetType) throws
            InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_PENDING_RELATIONSHIPS_OF_GIVEN_TYPE_FOR_ENTITY;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, entity.getEntityType());
            prepStmt.setString(2, entity.getEntityId());
            prepStmt.setString(3, WorkflowRequestStatus.PENDING.toString());
            prepStmt.setString(4, requsetType);
            prepStmt.setInt(5, entity.getTenantId());
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return false;
    }

    /**
     * Check if there are any requests the associated with both entities.
     *
     * @param entity1
     * @param entity2
     * @return
     * @throws InternalWorkflowException
     */
    public boolean twoEntitiesAreRelated(Entity entity1, Entity entity2) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_REQUESTS_OF_TWO_ENTITIES;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, entity1.getEntityId());
            prepStmt.setString(2, entity1.getEntityType());
            prepStmt.setString(3, entity2.getEntityId());
            prepStmt.setString(4, entity2.getEntityType());
            prepStmt.setInt(5, entity1.getTenantId());
            prepStmt.setInt(6, entity2.getTenantId());
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }

        return false;
    }

    /**
     * Retrieve List of associated Entity-types of the workflow requests.
     *
     * @param wfOperationType Operation Type of the Work-flow.
     * @param wfStatus        Current Status of the Work-flow.
     * @param entityType      Entity Type of the Work-flow.
     * @param idFilter        Entity ID filter to search
     * @param tenantID        Tenant ID of the currently Logged user.
     * @return
     * @throws InternalWorkflowException
     */
    public List<String> getEntityNamesOfRequest(String wfOperationType, String wfStatus, String entityType, String
            idFilter, int tenantID) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String query = SQLConstants.GET_REQUEST_ENTITY_NAMES;
        List<String> entityNames = new ArrayList<String>();
        idFilter = idFilter.replaceAll("\\*","%");
        idFilter = idFilter.replaceAll("\\\\%", "*");

        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, wfOperationType);
            prepStmt.setString(2, wfStatus);
            prepStmt.setString(3, entityType);
            prepStmt.setInt(4, tenantID);
            prepStmt.setString(5, idFilter);
            resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                String entityName = resultSet.getString(SQLConstants.ENTITY_NAME_COLUMN);
                entityNames.add(entityName);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error occurred when executing the sql query", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return entityNames;
    }

}
