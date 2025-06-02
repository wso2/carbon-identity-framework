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

package org.wso2.carbon.identity.flow.engine.model;

import java.util.List;
import java.util.Map;

/**
 * Model class to represent the response of an executor.
 */
public class ExecutorResponse {

    private String result;
    private List<String> requiredData;
    private Map<String, Object> updatedUserClaims;
    private Map<String, char[]> userCredentials;
    private Map<String, Object> contextProperties;
    private Map<String, String> additionalInfo;
    private String errorMessage;

    public ExecutorResponse() {

    }

    public ExecutorResponse(String result) {

        this.result = result;
    }

    public String getResult() {

        return result;
    }

    public void setResult(String result) {

        this.result = result;
    }

    public List<String> getRequiredData() {

        return requiredData;
    }

    public void setRequiredData(List<String> requiredData) {

        this.requiredData = requiredData;
    }

    public Map<String, Object> getUpdatedUserClaims() {

        return updatedUserClaims;
    }

    public void setUpdatedUserClaims(Map<String, Object> updatedUserClaims) {

        this.updatedUserClaims = updatedUserClaims;
    }

    public Map<String, char[]> getUserCredentials() {

        return userCredentials;
    }

    public void setUserCredentials(Map<String, char[]> userCredentials) {

        this.userCredentials = userCredentials;
    }

    public Map<String, Object> getContextProperties() {

        return contextProperties;
    }

    public void setContextProperty(Map<String, Object> contextProperties) {

        this.contextProperties = contextProperties;
    }

    public Map<String, String> getAdditionalInfo() {

        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {

        this.additionalInfo = additionalInfo;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {

        this.errorMessage = errorMessage;
    }
}
