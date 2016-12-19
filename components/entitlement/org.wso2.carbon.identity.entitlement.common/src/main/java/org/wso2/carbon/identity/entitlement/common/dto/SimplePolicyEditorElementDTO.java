/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.common.dto;

/**
 *
 */
public class SimplePolicyEditorElementDTO {

    private String userAttributeId;

    private String userAttributeValue;

    private String actionValue;

    private String resourceValue;

    private String environmentId;

    private String environmentValue;

    private String operationType;

    private String functionOnResources;

    private String functionOnActions;

    private String functionOnUsers;

    private String functionOnEnvironments;

    public String getUserAttributeId() {
        return userAttributeId;
    }

    public void setUserAttributeId(String userAttributeId) {
        this.userAttributeId = userAttributeId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getEnvironmentValue() {
        return environmentValue;
    }

    public void setEnvironmentValue(String environmentValue) {
        this.environmentValue = environmentValue;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(String resourceValue) {
        this.resourceValue = resourceValue;
    }

    public String getUserAttributeValue() {
        return userAttributeValue;
    }

    public void setUserAttributeValue(String userAttributeValue) {
        this.userAttributeValue = userAttributeValue;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    public String getFunctionOnUsers() {
        return functionOnUsers;
    }

    public void setFunctionOnUsers(String functionOnUsers) {
        this.functionOnUsers = functionOnUsers;
    }

    public String getFunctionOnActions() {
        return functionOnActions;
    }

    public void setFunctionOnActions(String functionOnActions) {
        this.functionOnActions = functionOnActions;
    }

    public String getFunctionOnResources() {
        return functionOnResources;
    }

    public void setFunctionOnResources(String functionOnResources) {
        this.functionOnResources = functionOnResources;
    }

    public String getFunctionOnEnvironments() {
        return functionOnEnvironments;
    }

    public void setFunctionOnEnvironments(String functionOnEnvironments) {
        this.functionOnEnvironments = functionOnEnvironments;
    }
}
