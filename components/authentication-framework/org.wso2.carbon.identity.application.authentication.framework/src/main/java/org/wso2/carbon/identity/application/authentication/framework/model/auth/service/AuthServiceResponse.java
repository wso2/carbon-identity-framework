/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.model.auth.service;

import org.wso2.carbon.identity.application.authentication.framework.util.auth.service.AuthServiceConstants;

import java.util.Optional;

/**
 * Class for the response object that is passed from the authentication service.
 */
public class AuthServiceResponse {

    private String sessionDataKey;
    private AuthServiceConstants.FlowStatus flowStatus;
    private AuthServiceResponseData data;
    private AuthServiceErrorInfo errorInfo;

    public String getSessionDataKey() {

        return sessionDataKey;
    }

    public void setSessionDataKey(String sessionDataKey) {

        this.sessionDataKey = sessionDataKey;
    }

    public AuthServiceConstants.FlowStatus getFlowStatus() {

        return flowStatus;
    }

    public void setFlowStatus(AuthServiceConstants.FlowStatus flowStatus) {

        this.flowStatus = flowStatus;
    }

    /**
     * Get the response data related to auth service.
     * Data will be null if the flow status is not
     * {@link AuthServiceConstants.FlowStatus#INCOMPLETE} or
     * {@link AuthServiceConstants.FlowStatus#FAIL_INCOMPLETE}.
     *
     * @return Optional of AuthServiceResponseData.
     */
    public Optional<AuthServiceResponseData> getData() {

        return Optional.ofNullable(data);
    }

    public void setData(AuthServiceResponseData data) {

        this.data = data;
    }

    /**
     * Get the error info related to auth service.
     * Error info will be null if the flow status is
     * not {@link AuthServiceConstants.FlowStatus#FAIL_COMPLETED} or
     * {@link AuthServiceConstants.FlowStatus#FAIL_INCOMPLETE}.
     *
     * @return Optional of AuthServiceErrorInfo.
     */
    public Optional<AuthServiceErrorInfo> getErrorInfo() {

        return Optional.ofNullable(errorInfo);
    }

    public void setErrorInfo(AuthServiceErrorInfo errorInfo) {

        this.errorInfo = errorInfo;
    }
}
