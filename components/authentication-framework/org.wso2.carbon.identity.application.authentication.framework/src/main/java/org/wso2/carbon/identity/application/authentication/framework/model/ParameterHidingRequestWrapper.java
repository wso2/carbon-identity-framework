/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.model;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Request wrapper that hides the given parameters from everything that reads the request downstream.
 *
 * The wrapper covers the parameter reading methods of the request, which is how the parameters of an authentication
 * request are read. The query string is left as it is, so a caller that parses {@code getQueryString()} itself still
 * sees the hidden parameters.
 */
public class ParameterHidingRequestWrapper extends HttpServletRequestWrapper {

    private final Set<String> hiddenParameters = new HashSet<>();

    /**
     * Wrap the request, hiding the given parameters. Parameter names are matched ignoring case.
     *
     * @param request           Request to wrap.
     * @param parametersToHide  Names of the parameters to hide.
     */
    public ParameterHidingRequestWrapper(HttpServletRequest request, String... parametersToHide) {

        super(request);
        if (parametersToHide != null) {
            for (String parameter : parametersToHide) {
                if (parameter != null) {
                    hiddenParameters.add(parameter.toLowerCase(Locale.ENGLISH));
                }
            }
        }
    }

    private boolean isHidden(String name) {

        return name != null && hiddenParameters.contains(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String getParameter(String name) {

        return isHidden(name) ? null : super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {

        return isHidden(name) ? null : super.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {

        Map<String, String[]> parameterMap = super.getParameterMap();
        if (parameterMap == null || hiddenParameters.isEmpty()) {
            return parameterMap;
        }
        Map<String, String[]> visibleParameters = new HashMap<>();
        parameterMap.forEach((name, values) -> {
            if (!isHidden(name)) {
                visibleParameters.put(name, values);
            }
        });
        return Collections.unmodifiableMap(visibleParameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {

        return Collections.enumeration(getParameterMap().keySet());
    }
}
