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

package org.wso2.carbon.identity.framework.async.status.mgt.models.dos;

import java.sql.Timestamp;

public class ResponseUnitOperationRecord {
    private String unitOperationId;
    private String operationId;
    private String operationInitiatedResourceId;
    private String targetOrgId;
    private String unitOperationStatus;
    private String statusMessage;
    private Timestamp createdTime;

    public ResponseUnitOperationRecord(String unitOperationId, String operationId, String operationInitiatedResourceId,
                                       String targetOrgId, String unitOperationStatus, String statusMessage,
                                       Timestamp createdTime) {

        this.unitOperationId = unitOperationId;
        this.operationId = operationId;
        this.operationInitiatedResourceId = operationInitiatedResourceId;
        this.targetOrgId = targetOrgId;
        this.unitOperationStatus = unitOperationStatus;
        this.statusMessage = statusMessage;
        this.createdTime = createdTime;
    }

    public ResponseUnitOperationRecord() {

    }

    public String getUnitOperationId() {

        return unitOperationId;
    }

    public void setUnitOperationId(String unitOperationId) {

        this.unitOperationId = unitOperationId;
    }

    public String getOperationId() {

        return operationId;
    }

    public void setOperationId(String operationId) {

        this.operationId = operationId;
    }

    public String getOperationInitiatedResourceId() {

        return operationInitiatedResourceId;
    }

    public void setOperationInitiatedResourceId(String operationInitiatedResourceId) {

        this.operationInitiatedResourceId = operationInitiatedResourceId;
    }

    public String getTargetOrgId() {

        return targetOrgId;
    }

    public void setTargetOrgId(String targetOrgId) {

        this.targetOrgId = targetOrgId;
    }

    public String getUnitOperationStatus() {

        return unitOperationStatus;
    }

    public void setUnitOperationStatus(String unitOperationStatus) {

        this.unitOperationStatus = unitOperationStatus;
    }

    public String getStatusMessage() {

        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {

        this.statusMessage = statusMessage;
    }

    public Timestamp getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {

        this.createdTime = createdTime;
    }
}
