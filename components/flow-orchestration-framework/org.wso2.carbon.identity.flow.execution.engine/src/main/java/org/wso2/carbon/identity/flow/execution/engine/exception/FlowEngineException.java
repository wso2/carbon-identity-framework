/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.flow.execution.engine.exception;

/**
 * Represents the exception that occurs in the flow engine.
 */
public class FlowEngineException extends Exception {

    private String flowType = null;
    private String errorCode;
    private String description;

    public FlowEngineException(String message) {

        super(message);
    }

    public FlowEngineException(String errorCode, String message, String description, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
    }

    public FlowEngineException(String errorCode, String message, String description) {

        super(message);
        this.errorCode = errorCode;
        this.description = description;
    }

    public FlowEngineException(String flowType, String message) {

        super(message);
        this.flowType = flowType;
    }

    public FlowEngineException(String flowType, String errorCode, String message, String description, Throwable cause) {

        super(message, cause);
        this.flowType = flowType;
        this.errorCode = errorCode;
        this.description = description;
    }

    public FlowEngineException(String flowType, String errorCode, String message, String description) {

        super(message);
        this.flowType = flowType;
        this.errorCode = errorCode;
        this.description = description;
    }

    public String getFlowType() {

        return flowType;
    }

    public void setFlowType(String flowType) {

        this.flowType = flowType;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }
}
