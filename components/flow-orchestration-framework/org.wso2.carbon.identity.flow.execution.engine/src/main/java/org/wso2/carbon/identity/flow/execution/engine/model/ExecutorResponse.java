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

package org.wso2.carbon.identity.flow.execution.engine.model;

import java.util.List;
import java.util.Map;

/**
 * Model class to represent the response of an executor.
 */
public class ExecutorResponse {

    private String result;
    private List<String> requiredData;
    private List<String> optionalData;
    private Map<String, Object> updatedUserClaims;
    private Map<String, char[]> userCredentials;
    private Map<String, Object> contextProperties;
    private Map<String, String> additionalInfo;
    private ErrorObject errorObject;

    public ExecutorResponse() {

        this.errorObject = new ErrorObject();
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

    public List<String> getOptionalData() {

        return optionalData;
    }

    public void setOptionalData(List<String> optionalData) {

        this.optionalData = optionalData;
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

        return errorObject.getMessage();
    }

    public void setErrorMessage(String errorMessage) {

        this.errorObject.setMessage(errorMessage);
    }

    public String getErrorCode() {

        return errorObject.getCode();
    }

    public void setErrorCode(String errorCode) {

        this.errorObject.setCode(errorCode);
    }

    public String getErrorDescription() {

        return errorObject.getDescription();
    }

    public void setErrorDescription(String errorDescription) {

        this.errorObject.setDescription(errorDescription);
    }

    public Throwable getThrowable() {

        return errorObject.getThrowable();
    }

    public void setThrowable(Throwable throwable) {

        this.errorObject.setThrowable(throwable);
    }

    /**
     * Model class to represent an error object.
     */
    public static class ErrorObject {

        private String code;
        private String message;
        private String description;
        private Throwable throwable;

        public ErrorObject() {

        }

        public String getCode() {

            return code;
        }

        public void setCode(String code) {

            this.code = code;
        }

        public String getMessage() {

            return message;
        }

        public void setMessage(String message) {

            this.message = message;
        }

        public String getDescription() {

            return description;
        }

        public void setDescription(String description) {

            this.description = description;
        }

        public Throwable getThrowable() {

            return throwable;
        }

        public void setThrowable(Throwable throwable) {

            this.throwable = throwable;
        }
    }
}
