/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

/**
 * This class is responsible for holding the flow event context.
 */
public class FlowEventContext {

    private String contextIdentifier;
    private String flowType;
    private String applicationId;
    private String tenantDomain;
    private NodeConfig currentNode;
    private String userId;
    private String errorCode;
    private FlowExecutionStep step;
    private NodeResponse currentNodeResponse;

    public String getContextIdentifier() {
        return contextIdentifier;
    }

    public void setContextIdentifier(String contextIdentifier) {
        this.contextIdentifier = contextIdentifier;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public NodeConfig getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(NodeConfig currentNode) {
        this.currentNode = currentNode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public FlowExecutionStep getStep() {
        return step;
    }

    public void setStep(FlowExecutionStep step) {
        this.step = step;
    }

    public NodeResponse getCurrentNodeResponse() {
        return currentNodeResponse;
    }

    public void setCurrentNodeResponse(NodeResponse currentNodeResponse) {
        this.currentNodeResponse = currentNodeResponse;
    }
}
