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
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Association related DAO operation provides by this class
 *
 */
public class AssociationDAO {

    private final String errorMessage = "Error when executing the SQL query ";

    /**
     *
     * @param associationName
     * @param workflowId
     * @param eventId
     * @param condition
     * @throws InternalWorkflowException
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
            connection.commit();
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }


    /**
     *
     * @param associationDTO
     * @throws InternalWorkflowException
     */
    public void updateAssociation(Association associationDTO)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        String query = SQLConstants.UPDATE_ASSOCIATE_WF_TO_EVENT;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, associationDTO.getEventId());
            prepStmt.setString(2, associationDTO.getAssociationName());
            prepStmt.setString(3, associationDTO.getCondition());
            prepStmt.setString(4, associationDTO.getWorkflowId());
            if(associationDTO.isEnabled()) {
                prepStmt.setString(5, "1");
            }else{
                prepStmt.setString(5, "0");
            }
            prepStmt.setString(6, associationDTO.getAssociationId());
            prepStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     *
     * @return
     * @throws InternalWorkflowException
     */
    public List<Association> listAssociations(int tenantId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
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
                Association associationDTO = new Association();
                associationDTO.setCondition(condition);
                associationDTO.setAssociationId(associationId);
                associationDTO.setEventId(eventId);
                associationDTO.setAssociationName(associationName);
                associationDTO.setWorkflowName(workflowName);
                associations.add(associationDTO);
                if(isEnable.equals("1")){
                    associationDTO.setEnabled(true);
                }else{
                    associationDTO.setEnabled(false);
                }
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return associations;
    }


    /**
     *
     * @param associationId
     * @return
     * @throws InternalWorkflowException
     */
    public Association getAssociation(String associationId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs;
        Association associationDTO = null ;
        String query = SQLConstants.GET_ASSOCIATION_FOR_ASSOC_ID_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, associationId);

            rs = prepStmt.executeQuery();

            while (rs.next()) {
                String condition = rs.getString(SQLConstants.CONDITION_COLUMN);
                String eventId = rs.getString(SQLConstants.EVENT_ID_COLUMN);
                String associationName = rs.getString(SQLConstants.ASSOCIATION_NAME_COLUMN);
                String workflowName = rs.getString(SQLConstants.WF_NAME_COLUMN);
                String workflowId = rs.getString(SQLConstants.WORKFLOW_ID_COLUMN);
                String isEnable = rs.getString(SQLConstants.ASSOCIATION_IS_ENABLED);
                associationDTO = new Association();
                associationDTO.setCondition(condition);
                associationDTO.setAssociationId(associationId);
                associationDTO.setEventId(eventId);
                associationDTO.setWorkflowId(workflowId);
                associationDTO.setAssociationName(associationName);
                associationDTO.setWorkflowName(workflowName);

                if(isEnable.equals("1")){
                    associationDTO.setEnabled(true);
                }else{
                    associationDTO.setEnabled(false);
                }
            }


        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return associationDTO;
    }

    /**
     *
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
            prepStmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }


    /**
     *
     * @param workflowId
     * @return
     * @throws InternalWorkflowException
     */
    public List<Association> listAssociationsForWorkflow(String workflowId)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rs;
        List<Association> associations = new ArrayList<>();
        String query = SQLConstants.GET_ASSOCIATIONS_FOR_WORKFLOW_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String condition = rs.getString(SQLConstants.CONDITION_COLUMN);
                String eventId = rs.getString(SQLConstants.EVENT_ID_COLUMN);
                String associationId = String.valueOf(rs.getInt(SQLConstants.ID_COLUMN));
                String associationName = rs.getString(SQLConstants.ASSOCIATION_NAME_COLUMN);
                String workflowName = rs.getString(SQLConstants.WF_NAME_COLUMN);
                Association associationDTO = new Association();
                associationDTO.setCondition(condition);
                associationDTO.setAssociationId(associationId);
                associationDTO.setEventId(eventId);
                associationDTO.setAssociationName(associationName);
                associationDTO.setWorkflowName(workflowName);
                associations.add(associationDTO);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException(errorMessage, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return associations;
    }

}
