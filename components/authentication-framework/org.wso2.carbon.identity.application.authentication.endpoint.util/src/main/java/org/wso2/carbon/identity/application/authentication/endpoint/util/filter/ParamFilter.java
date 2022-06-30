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

package org.wso2.carbon.identity.application.authentication.endpoint.util.filter;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.endpoint.util.AuthContextAPIClient;
import org.wso2.carbon.identity.application.authentication.endpoint.util.client.model.AuthenticationRequestWrapper;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 */
public class ParamFilter implements Filter {

    private static final Log log = LogFactory.getLog(ParamFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to implement.
    }

    /**
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        // Check if request contains 'sessionDataKey'.
        if (servletRequest.getParameter("sessionDataKey") != null) {
            if (servletRequest instanceof HttpServletRequest) {
                // Cast ServletRequest to HttpServletRequest and get the wrapped request.
                HttpServletRequest httpServletRequest = cacheParams((HttpServletRequest) servletRequest);
                filterChain.doFilter(httpServletRequest, servletResponse);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Cache the parameters retrieved from the API using SessionDataKey.
     *
     * @param servletRequest - Received servlet request.
     * @return - HttpServletRequestWrapper with cached parameter map.
     */
    private HttpServletRequest cacheParams(HttpServletRequest servletRequest) {

        // Define Authentication API URL.
        String authAPIURL = IdentityUtil.getServerURL("/api/identity/auth/v1.1/", true, true);

        // Add '/' if missing.
        if (!authAPIURL.endsWith("/")) {
            authAPIURL += "/";
        }

        // Add 'context' path to url and set 'sessionDataKey'.
        authAPIURL += "context/" + servletRequest.getParameter("sessionDataKey");

        // Get login parameters from the API.
        String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);
        Gson gson = new Gson();
        Map<String, Object> cachedParams = gson.fromJson(contextProperties, Map.class);

//        // Add 'sessionDataKey' to the parameter map.
//        cachedParams.put("sessionDataKey", servletRequest.getParameter("sessionDataKey"));
        return new AuthenticationRequestWrapper(servletRequest, cachedParams);
    }

    @Override
    public void destroy() {
        // Nothing to implement.
    }
}
