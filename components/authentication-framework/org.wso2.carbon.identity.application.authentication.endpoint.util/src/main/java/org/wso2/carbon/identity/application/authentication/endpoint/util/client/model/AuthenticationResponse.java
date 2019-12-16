/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.util.client.model;

/**
 * Object model that represents the response returned from /api/identity/auth/v1.0 API.
 */
public class AuthenticationResponse {

    private int statusCode;

    /**
     * Returns the HTTP status code of the response.
     *
     * @return status code
     */
    public int getStatusCode() {

        return statusCode;
    }

    /**
     * Sets the HTTP status code of the response.
     *
     * @param statusCode status code
     */
    public void setStatusCode(int statusCode) {

        this.statusCode = statusCode;
    }
}
