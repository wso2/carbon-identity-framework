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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class for the request object that is passed to the authentication service.
 */
public class AuthServiceRequest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Map<String, String[]> parameters = new HashMap<>();

    public AuthServiceRequest(HttpServletRequest request, HttpServletResponse response) {

        this.request = request;
        this.response = response;
    }

    public AuthServiceRequest(HttpServletRequest request, HttpServletResponse response,
                              Map<String, String[]> parameters) {

        this.request = request;
        this.response = response;
        this.parameters = parameters;
    }

    public HttpServletRequest getRequest() {

        return request;
    }

    public HttpServletResponse getResponse() {

        return response;
    }

    public void setParameters(Map<String, String[]> parameters) {

        this.parameters = parameters;
    }

    public Map<String, String[]> getParameters() {

        return parameters;
    }
}
