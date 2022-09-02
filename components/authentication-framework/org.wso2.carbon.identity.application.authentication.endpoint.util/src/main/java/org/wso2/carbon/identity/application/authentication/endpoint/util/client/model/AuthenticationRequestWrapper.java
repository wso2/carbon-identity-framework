/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.endpoint.util.client.model;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Authentication endpoint request wrapper.
 */
public class AuthenticationRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, Object> authParams;

    public AuthenticationRequestWrapper(HttpServletRequest request, Map<String, Object> authParams) {

        super(request);
        if (authParams != null) {
            this.authParams = authParams;
        } else {
            this.authParams = new HashMap<>();
        }

        if (super.getParameterMap() != null) {
            Map<String, Object> superParams = super.getParameterMap();
            for (String paramName : superParams.keySet()) {
                if (super.getParameter(paramName) != null) {
                    this.authParams.put(paramName, super.getParameter(paramName));
                }
            }
        }

    }

    /**
     * Get the parameter from the map.
     *
     * @param name Name of the parameter.
     * @return
     */
    public String getParameter(String name) {

        if (authParams != null) {
            return (authParams.get(name) != null) ? (String) authParams.get(name) : super.getParameter(name);
        } else {
            return super.getParameter(name);
        }
    }

    /**
     * Get the parameter map.
     *
     * @return
     */
    public Map<String, Object> getParameterMap() {

        Map<String, Object> paramMap;
        if (authParams != null) {
            paramMap = authParams;
            Map<String, Object> superParams = super.getParameterMap();
            for (String paramName : superParams.keySet()) {
                authParams.put(paramName, super.getParameter(paramName));
            }
        } else {
            paramMap = super.getParameterMap();
        }
        return paramMap;
    }

    /**
     * Get the parameter names.
     *
     * @return
     */
    public Enumeration<String> getParameterNames() {

        return Collections.enumeration(authParams.keySet());
    }


    /**
     * Get values of a parameter from the map.
     *
     * @param name Name of the parameter.
     * @return
     */
    public String[] getParameterValues(String name) {


        return (String[]) authParams.get(name);
    }
}
