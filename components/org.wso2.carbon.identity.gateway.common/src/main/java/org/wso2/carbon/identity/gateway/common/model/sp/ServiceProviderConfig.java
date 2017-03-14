/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.common.model.sp;

/**
 * ServiceProviderConfig is a SP model class.
 */
public class ServiceProviderConfig {

    private String name;

    private RequestValidationConfig requestValidationConfig;
    private ClaimConfig claimConfig;
    private AuthenticationConfig authenticationConfig;
    private ResponseBuildingConfig responseBuildingConfig;


    public AuthenticationConfig getAuthenticationConfig() {
        return authenticationConfig;
    }

    public void setAuthenticationConfig(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestValidationConfig getRequestValidationConfig() {
        return requestValidationConfig;
    }

    public void setRequestValidationConfig(RequestValidationConfig requestValidationConfig) {
        this.requestValidationConfig = requestValidationConfig;
    }

    public ResponseBuildingConfig getResponseBuildingConfig() {
        return responseBuildingConfig;
    }

    public void setResponseBuildingConfig(ResponseBuildingConfig responseBuildingConfig) {
        this.responseBuildingConfig = responseBuildingConfig;
    }
}


