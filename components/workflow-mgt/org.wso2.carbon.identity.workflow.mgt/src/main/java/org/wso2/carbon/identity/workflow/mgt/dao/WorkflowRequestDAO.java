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
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class WorkflowRequestDAO {

    public static final String UPDATED_AT_FILTER = "updatedAt";
    public static final String ALL_TASKS_FILTER = "allTasks";
    private static final Log log = LogFactory.getLog(WorkflowRequestDAO.class);

    /**
     * Persists WorkflowRequest to be used when workflow is completed
     *
     * @param workflow    The workflow object to be persisted
     * @param currentUser Currently logged in user
     * @param tenantId    Tenant ID of the currently Logged user.
     * @throws WorkflowException
     */
    public void addWorkflowEntry(WorkflowRequest workflow, String currentUser, int tenantId) throws WorkflowException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.ADD_WORKFLOW_REQUEST_QUERY;
        try {
            Timestamp createdDateStamp = new Timestamp(System.currentTimeMillis());
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflow.getUuid());
            prepStmt.setString(2, currentUser);
            prepStmt.setString(3, workflow.getEventType());
            prepStmt.setTimestamp(4, createdDateStamp);
            prepStmt.setTimestamp(5, createdDateStamp);
            prepStmt.setBytes(6, serializeWorkflowRequest(workflow));
            prepStmt.setString(7, WorkflowRequestStatus.PENDING.toString());
            prepStmt.setInt(8, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } catch (IOException e) {
            throw new InternalWorkflowException("Error when serializing the workflow request: " + workflow, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Serialize the workflow request to be persisted as blob
     *
     * @param workFlowRequest The workflow request to be persisted
     * @return
     * @throws IOException
     */
    private byte[] serializeWorkflowRequest(WorkflowRequest workFlowRequest) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(workFlowRequest);
        oos.close();
        return baos.toByteArray();
    }

    /**
     * Retrieve workflow request specified by the given uuid
     *
     * @param uuid The uuid of the request to be retrieved
     * @return
     * @throws WorkflowException
     */
    public WorkflowRequest retrieveWorkflow(String uuid) throws InternalWorkflowException {
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.GET_WORKFLOW_REQUEST_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, uuid);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                byte[] requestBytes = rs.getBytes(SQLConstants.REQUEST_COLUMN);
                return deserializeWorkflowRequest(requestBytes);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } catch (ClassNotFoundException | IOException e) {
            throw new InternalWorkflowException("Error when deserializing the workflow request. uuid = " + uuid, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return null;
    }

    /**
     * Get status of a request.
     *
     * @param uuid
     * @return
     * @throws InternalWorkflowException
     */
    public String retrieveStatusOfWorkflow(String uuid) throws InternalWorkflowException {
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        String query = SQLConstants.GET_WORKFLOW_REQUEST_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, uuid);
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                String status = resultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN);
                return status;
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return "";
    }

    /**
     * Get user who created the request.
     *
     * @param uuid
     * @return
     * @throws InternalWorkflowException
     */
    public String retrieveCreatedUserOfRequest(String uuid) throws InternalWorkflowException {
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        String query = SQLConstants.GET_WORKFLOW_REQUEST_QUERY;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, uuid);
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(SQLConstants.CREATED_BY_COLUMN);
            }
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
        return "";
    }

    /**
     * Deserialize the persisted Workflow request
     *
     * @param serializedData Serialized request
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private WorkflowRequest deserializeWorkflowRequest(byte[] serializedData) throws IOException,
                                                                                     ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object objectRead = ois.readObject();
        if (objectRead != null && objectRead instanceof WorkflowRequest) {
            return (WorkflowRequest) objectRead;
        }
        return null;
    }

    /**
     * Update state of a existing workflow request
     *
     * @param requestId
     * @param newState
     * @throws InternalWorkflowException
     */
    public void updateStatusOfRequest(String requestId, String newState) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_STATUS_OF_REQUEST;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, newState);
            prepStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(3, requestId);
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
     * Get requests of a given user.
     *
     * @param userName user name of user to get requests
     * @param tenantId user's tenant id
     * @return
     * @throws InternalWorkflowException
     */
    public org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[] getRequestsOfUser(String userName, int tenantId)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = SQLConstants.GET_REQUESTS_OF_USER;
        ResultSet resultSet = null;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, userName);
            prepStmt.setInt(2, tenantId);
            resultSet = prepStmt.executeQuery();
            ArrayList<org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest> requestDTOs = new ArrayList<>();
            while (resultSet.next()) {
                org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest
                        requestDTO = new org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest();
                requestDTO.setRequestId(resultSet.getString(SQLConstants.REQUEST_UUID_COLUMN));
                requestDTO.setEventType(resultSet.getString(SQLConstants.REQUEST_OPERATION_TYPE_COLUMN));
                requestDTO.setCreatedAt(resultSet.getTimestamp(SQLConstants.REQUEST_CREATED_AT_COLUMN).toString());
                requestDTO.setUpdatedAt(resultSet.getTimestamp(SQLConstants.REQUEST_UPDATED_AT_COLUMN).toString());
                requestDTO.setStatus(resultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN));
                requestDTO.setRequestParams((deserializeWorkflowRequest(resultSet.getBytes(SQLConstants
                                                                                                   .REQUEST_COLUMN)))
                                                    .getRequestParameterAsString());
                requestDTO.setCreatedBy(resultSet.getString(SQLConstants.CREATED_BY_COLUMN));
                requestDTOs.add(requestDTO);
            }
            org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[] requestArray =
                    new org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[requestDTOs.size()];
            for (int i = 0; i < requestDTOs.size(); i++) {
                requestArray[i] = requestDTOs.get(i);
            }
            return requestArray;
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } catch (ClassNotFoundException | IOException e) {
            throw new InternalWorkflowException("Error when deserializing a workflow request.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
    }

    /**
     * Get requests of a user created/updated in given time period
     *
     * @param userName     User to get requests of, empty String to retrieve requests of all users
     * @param beginTime    lower limit of date range to filter
     * @param endTime      upper limit of date range to filter
     * @param timeCategory filter by created time or last updated time ?
     * @param tenantId     tenant id of currently logged in user
     * @return
     * @throws InternalWorkflowException
     */
    public org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[] getRequestsOfUserFilteredByTime(String
            userName, Timestamp beginTime, Timestamp endTime, String timeCategory, int tenantId, String status) throws
            InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = "";

        ResultSet resultSet = null;
        try {

            String driverName = connection.getMetaData().getDriverName();
            if (driverName.contains("MySQL")
                    || driverName.contains("MariaDB")
                    || driverName.contains("H2")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_MYSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_AND_STATUS_MYSQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_MYSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_AND_STATUS_MYSQL;
                    }
                }
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_DB2SQl;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_AND_STATUS_DB2SQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_DB2SQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_AND_STATUS_DB2SQL;
                    }
                }
            } else if (driverName.contains("MS SQL")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_AND_STATUS_MSSQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_AND_STATUS_MSSQL;
                    }
                }
            } else if (driverName.contains("Microsoft") || driverName.contains("microsoft")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_AND_STATUS_MSSQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_AND_STATUS_MSSQL;
                    }
                }
            } else if (driverName.contains("PostgreSQL")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_POSTGRESQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_AND_STATUS_POSTGRESQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_POSTGRESQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_AND_STATUS_POSTGRESQL;
                    }
                }
            } else if (driverName.contains("Informix")) {
                // Driver name = "IBM Informix JDBC Driver for IBM Informix Dynamic Server"
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_INFORMIX;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_AND_STATUS_INFORMIX;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_INFORMIX;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_AND_STATUS_INFORMIX;
                    }
                }

            } else {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_ORACLE;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_UPDATED_TIME_AND_STATUS_ORACLE;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_ORACLE;
                    } else {
                        query = SQLConstants.GET_REQUESTS_OF_USER_FILTER_FROM_CREATED_TIME_AND_STATUS_ORACLE;
                    }
                }
            }
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, userName);
            prepStmt.setTimestamp(2, beginTime);
            prepStmt.setTimestamp(3, endTime);
            prepStmt.setInt(4, tenantId);
            if (!status.equals(ALL_TASKS_FILTER) && !status.equals("")) {
                prepStmt.setString(5, status);
            }
            resultSet = prepStmt.executeQuery();
            ArrayList<org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest> requestDTOs = new ArrayList<>();
            while (resultSet.next()) {
                org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest
                        requestDTO = new org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest();
                requestDTO.setRequestId(resultSet.getString(SQLConstants.REQUEST_UUID_COLUMN));
                requestDTO.setEventType(resultSet.getString(SQLConstants.REQUEST_OPERATION_TYPE_COLUMN));
                requestDTO.setCreatedAt(resultSet.getTimestamp(SQLConstants.REQUEST_CREATED_AT_COLUMN).toString());
                requestDTO.setUpdatedAt(resultSet.getTimestamp(SQLConstants.REQUEST_UPDATED_AT_COLUMN).toString());
                requestDTO.setStatus(resultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN));
                requestDTO.setRequestParams((deserializeWorkflowRequest(resultSet.getBytes(SQLConstants
                        .REQUEST_COLUMN)))
                        .getRequestParameterAsString());
                requestDTO.setCreatedBy(resultSet.getString(SQLConstants.CREATED_BY_COLUMN));
                requestDTOs.add(requestDTO);
            }
            org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[] requestArray =
                    new org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[requestDTOs.size()];
            for (int i = 0; i < requestDTOs.size(); i++) {
                requestArray[i] = requestDTOs.get(i);
            }
            return requestArray;
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } catch (ClassNotFoundException | IOException e) {
            throw new InternalWorkflowException("Error when deserializing a workflow request.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
    }

    /**
     * Get requests created/updated in given time period
     *
     * @param beginTime    lower limit of date range to filter
     * @param endTime      upper limit of date range to filter
     * @param timeCategory filter by created time or last updated time ?
     * @param tenant       tenant id of currently logged in user
     * @return
     * @throws InternalWorkflowException
     */
    public org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[]
    getRequestsFilteredByTime(Timestamp beginTime, Timestamp endTime, String timeCategory, int tenant, String status)
            throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement prepStmt = null;
        String query = "";

        ResultSet resultSet = null;

        try {
            String driverName = connection.getMetaData().getDriverName();
            if (driverName.contains("MySQL")
                    || driverName.contains("MariaDB")
                    || driverName.contains("H2")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_MYSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_AND_STATUS_MYSQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_MYSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_AND_STATUS_MYSQL;
                    }
                }
            } else if (connection.getMetaData().getDatabaseProductName().contains("DB2")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_DB2SQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_AND_STATUS_DB2SQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_DB2SQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_AND_STATUS_DB2SQL;
                    }
                }
            } else if (driverName.contains("MS SQL")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_AND_STATUS_MSSQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_AND_STATUS_MSSQL;
                    }
                }
            } else if (driverName.contains("Microsoft") || driverName.contains("microsoft")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_AND_STATUS_MSSQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_MSSQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_AND_STATUS_MSSQL;
                    }
                }
            } else if (driverName.contains("PostgreSQL")) {
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_POSTGRESQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_AND_STATUS_POSTGRESQL;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_POSTGRESQL;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_AND_STATUS_POSTGRESQL;
                    }
                }
            } else if (driverName.contains("Informix")) {
                // Driver name = "IBM Informix JDBC Driver for IBM Informix Dynamic Server"
                if (UPDATED_AT_FILTER.equals(timeCategory)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_INFORMIX;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_AND_STATUS_INFORMIX;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_INFORMIX;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_AND_STATUS_INFORMIX;
                    }
                }

            } else {
                if (timeCategory.equals(UPDATED_AT_FILTER)) {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_ORACLE;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_UPDATED_TIME_AND_STATUS_ORACLE;
                    }
                } else {
                    if (status.equals(ALL_TASKS_FILTER) || status.equals("")) {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_ORACLE;
                    } else {
                        query = SQLConstants.GET_REQUESTS_FILTER_FROM_CREATED_TIME_AND_STATUS_ORACLE;
                    }
                }
            }
            prepStmt = connection.prepareStatement(query);
            prepStmt.setTimestamp(1, beginTime);
            prepStmt.setTimestamp(2, endTime);
            prepStmt.setInt(3, tenant);
            if (!status.equals(ALL_TASKS_FILTER) && !status.equals("")) {
                prepStmt.setString(4, status);
            }
            resultSet = prepStmt.executeQuery();
            ArrayList<org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest> requestDTOs = new ArrayList<>();
            while (resultSet.next()) {
                org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest
                        requestDTO = new org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest();
                requestDTO.setRequestId(resultSet.getString(SQLConstants.REQUEST_UUID_COLUMN));
                requestDTO.setEventType(resultSet.getString(SQLConstants.REQUEST_OPERATION_TYPE_COLUMN));
                requestDTO.setCreatedAt(resultSet.getTimestamp(SQLConstants.REQUEST_CREATED_AT_COLUMN).toString());
                requestDTO.setUpdatedAt(resultSet.getTimestamp(SQLConstants.REQUEST_UPDATED_AT_COLUMN).toString());
                requestDTO.setStatus(resultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN));
                requestDTO.setRequestParams((deserializeWorkflowRequest(resultSet.getBytes(SQLConstants
                        .REQUEST_COLUMN)))
                        .getRequestParameterAsString());
                requestDTO.setCreatedBy(resultSet.getString(SQLConstants.CREATED_BY_COLUMN));
                requestDTOs.add(requestDTO);
            }
            org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[] requestArray =
                    new org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest[requestDTOs.size()];
            for (int i = 0; i < requestDTOs.size(); i++) {
                requestArray[i] = requestDTOs.get(i);
            }
            return requestArray;
        } catch (SQLException e) {
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } catch (ClassNotFoundException | IOException e) {
            throw new InternalWorkflowException("Error when deserializing a workflow request.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }
    }

    /**
     * update last updated time of a request
     *
     * @param requestId
     * @throws InternalWorkflowException
     */
    public void updateLastUpdatedTimeOfRequest(String requestId) throws InternalWorkflowException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String query = SQLConstants.UPDATE_UPDATED_AT_OF_REQUEST;
        try {
            prepStmt = connection.prepareStatement(query);
            prepStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            prepStmt.setString(2, requestId);
            prepStmt.execute();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new InternalWorkflowException("Error when executing the sql query:" + query, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

}
