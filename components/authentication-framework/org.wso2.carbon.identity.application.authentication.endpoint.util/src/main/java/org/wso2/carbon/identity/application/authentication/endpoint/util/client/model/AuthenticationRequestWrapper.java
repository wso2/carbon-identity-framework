/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Authentication endpoint request wrapper.
 */
public class AuthenticationRequestWrapper extends HttpServletRequestWrapper {
    private Map<String, Object> cachedParams;

    public AuthenticationRequestWrapper(HttpServletRequest request, Map<String, Object> cachedParams) {
        super(request);
        this.cachedParams = cachedParams;
    }

    /**
     * Get the parameter from the map.
     * @param name - Name of the parameter.
     * @return
     */
    public String getParameter(String name) {
//        return cachedParams.get(name) == null ? super.getParameter(name) : (String) cachedParams.get(name);
        try {
            return cachedParams.get(name) == null ? super.getParameter(name) : (String) cachedParams.get(name);
        } catch (NullPointerException e) {
            return super.getParameter(name);
        }
    }

    /**
     * Get the parameter map.
     * @return
     */
    public Map<String, Object> getParameterMap() {
        return cachedParams;
    }

    /**
     * Get the parameter names.
     * @return
     */
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(cachedParams.keySet());
    }


    /**
     * Get values of a parameter from the map.
     * @param name - Name of the parameter.
     * @return
     */
    public String[] getParameterValues(String name) {
        return (String[]) cachedParams.get(name);
    }
}
