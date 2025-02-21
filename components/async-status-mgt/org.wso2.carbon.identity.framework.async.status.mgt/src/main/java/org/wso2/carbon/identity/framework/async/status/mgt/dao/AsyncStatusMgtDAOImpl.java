/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt.dao;

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtService;
import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtServiceImpl;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationStatus;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationType;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.AsyncStatusMgtServiceComponent;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;

import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.CREATE_ASYNC_OPERATION;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationDBContext;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategy;
import org.wso2.carbon.identity.framework.async.status.mgt.util.OperationStatusStrategyFactory;

/**
 * DAO implementation for Asynchronous Operation Status Management.
 */
public class AsyncStatusMgtDAOImpl implements AsyncStatusMgtDAO {
    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtDAOImpl.class.getName());

    @Override
    public void createB2BResourceSharingOperation(String operationType, String residentResourceId, String resourceType, String sharingPolicy, String residentOrgId, String initiatorId, String operationStatus) {
        LOGGER.info("CREATE_B2B_RESOURCE_SHARING_OPERATION Started...");

//        Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
//        try{
//            try{
//                NamedPreparedStatement statement = new NamedPreparedStatement(connection,CREATE_B2B_RESOURCE_SHARING_OPERATION, SQLConstants.UMSharingOperationTableColumns.UM_SHARING_OPERATION_ID);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_SHARING_OPERATION_TYPE, operationType);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_RESIDENT_RESOURCE_ID, residentResourceId);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_RESOURCE_TYPE, resourceType);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_SHARING_POLICY, sharingPolicy);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_RESIDENT_ORG_ID, residentOrgId);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_OPERATION_INITIATOR_ID, initiatorId);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_SHARING_OPERATION_STATUS, operationStatus);
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_CREATED_TIME, "2025-02-18 15:24:44.167");
//                statement.setString(SQLConstants.UMSharingOperationTableColumns.UM_LAST_MODIFIED, "2025-02-18 15:24:44.167");
//                statement.executeUpdate();
//                LOGGER.info("CREATE_B2B_RESOURCE_SHARING_OPERATION Success.");
//
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        } catch (RuntimeException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public void registerB2BUserSharingAsyncOperation(OperationDBContext context) {
        LOGGER.info("B2BUserSharingAsyncOperation Registering Started.");
        Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
        try{
            try{
                String currentTimestamp = new Timestamp(new Date().getTime()).toString();
                NamedPreparedStatement statement = new NamedPreparedStatement(connection,CREATE_ASYNC_OPERATION, SQLConstants.UMAsyncOperationStatusTableColumns.UM_OPERATION_ID);
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_OPERATION_TYPE, context.getOperationContext().getOperationType());
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_OPERATION_SUBJECT_ID, context.getOperationContext().getOperationSubjectId());
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_RESOURCE_TYPE, context.getOperationContext().getOperationType());
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_OPERATION_POLICY, context.getOperationContext().getSharingPolicy());
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_RESIDENT_ORG_ID, context.getOperationContext().getResidentOrgId());
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_OPERATION_INITIATOR_ID, context.getOperationContext().getInitiatorId());
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_OPERATION_STATUS, OperationStatus.ONGOING.toString());
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_CREATED_TIME, currentTimestamp);
                statement.setString(SQLConstants.UMAsyncOperationStatusTableColumns.UM_LAST_MODIFIED, currentTimestamp);
                statement.executeUpdate();
                LOGGER.info("B2BUserSharingAsyncOperation Registering Success.");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
