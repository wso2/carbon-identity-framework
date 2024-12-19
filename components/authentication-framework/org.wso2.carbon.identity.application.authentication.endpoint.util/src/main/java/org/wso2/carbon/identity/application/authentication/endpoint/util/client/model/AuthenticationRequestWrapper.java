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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
        this.authParams = authParams;
    }

    /**
     * Get the parameter. If the parameter is not found in the map, will return the query parameter.
     *
     * @param name Name of the parameter.
     * @return Parameter value.
     */
    public String getParameter(String name) {

        if (authParams != null && authParams.get(name) != null) {
            return (String) authParams.get(name);
        } else {
            return super.getParameter(name);
        }
    }

    /**
     * Get the auth parameter from the map.
     *
     * @param name Name of the parameter.
     * @return Parameter value.
     */
    public String getAuthParameter(String name) {

        if (authParams != null) {
            return (String) authParams.get(name);
        }
        return null;
    }

    /**
     * Get authParams.
     *
     * @return authParams Map Object.
     */
    public Map<String, Object> getAuthParams() {

        if (authParams != null) {
            return authParams;
        }
        return Collections.emptyMap();
    }

    /**
     * Get the parameter map.
     *
     * @return Parameter map.
     */
    public Map<String, Object> getParameterMap() {

        Map<String, Object> paramMap = new HashMap<>();
        if (authParams != null) {
            paramMap.putAll(authParams);
            paramMap.replaceAll((k, v) -> new String[]{String.valueOf(v)});
        }
        paramMap.putAll(super.getParameterMap());
        return paramMap;
    }

    /**
     * Get the parameter names.
     *
     * @return Parameter names.
     */
    public Enumeration<String> getParameterNames() {

        List<String> paramNameList = new ArrayList<>();
        Enumeration<String> paramEnum = super.getParameterNames();
        while(paramEnum.hasMoreElements())
            paramNameList.add(String.valueOf(paramEnum.nextElement()));
        if (authParams != null) {
            paramNameList.addAll(authParams.keySet());
        }
        return Collections.enumeration(paramNameList);
    }


    /**
     * Get values of a parameter from the map.
     *
     * @param name Name of the parameter.
     * @return Parameter values.
     */
    public String[] getParameterValues(String name) {

        if (authParams != null && authParams.get(name) != null) {
            return ((String[]) authParams.get(name));
        } else {
            return super.getParameterValues(name);
        }
    }
}
