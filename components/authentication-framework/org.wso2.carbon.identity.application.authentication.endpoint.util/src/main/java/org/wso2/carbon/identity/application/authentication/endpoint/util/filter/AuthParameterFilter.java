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

package org.wso2.carbon.identity.application.authentication.endpoint.util.filter;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.endpoint.util.AuthContextAPIClient;
import org.wso2.carbon.identity.application.authentication.endpoint.util.AuthenticationEndpointUtil;
import org.wso2.carbon.identity.application.authentication.endpoint.util.Constants;
import org.wso2.carbon.identity.application.authentication.endpoint.util.client.exception.AuthenticationEndpointException;
import org.wso2.carbon.identity.application.authentication.endpoint.util.client.model.AuthenticationRequestWrapper;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cache the parameters required in the authentication flow.
 */
public class AuthParameterFilter implements Filter {

    private static final Log log = LogFactory.getLog(AuthParameterFilter.class);
    private ServletContext context = null;

    private static final String DATA_API_PATH = "/api/identity/auth/v1.1/";
    private static final String SESSION_DATA_KEY = "sessionDataKey";
    private static final String SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";
    private static final String ERROR_KEY = "errorKey";

    @Override
    public void init(FilterConfig filterConfig) {
        context = filterConfig.getServletContext();
    }

    /**
     * Retrieve the parameters for the received servlet request and forward the request wrapped with the parameters.
     *
     * @param servletRequest    Servlet Request
     * @param servletResponse   Servlet Response
     * @param filterChain       Filter Chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        try {
            if (servletRequest instanceof HttpServletRequest) {
                // Check if request contains 'sessionDataKey', 'sessionDataKeyConsent', 'errorKey', 'promptId'.
                if (servletRequest.getParameter(SESSION_DATA_KEY) != null) {
                    // Cast ServletRequest to HttpServletRequest and get the wrapped request.
                    HttpServletRequest httpServletRequest = getServletRequestWithParams(servletRequest, SESSION_DATA_KEY);
                    filterChain.doFilter(httpServletRequest, servletResponse);
                    return;
                }
                if (servletRequest.getParameter(SESSION_DATA_KEY_CONSENT) != null) {
                    HttpServletRequest httpServletRequest =
                            getServletRequestWithParams(servletRequest, SESSION_DATA_KEY_CONSENT);
                    filterChain.doFilter(httpServletRequest, servletResponse);
                    return;
                }
                if (servletRequest.getParameter(ERROR_KEY) != null) {
                    HttpServletRequest httpServletRequest = getServletRequestWithParams(servletRequest, ERROR_KEY);
                    filterChain.doFilter(httpServletRequest, servletResponse);
                    return;
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (AuthenticationEndpointException e) {
            ((HttpServletResponse) servletResponse).sendError(500, e.getMessage());
        }
    }

    /**
     * Get HttpServletRequest with parameters retrieved from the API.
     *
     * @param servletRequest Received HttpServletRequest.
     * @param key            Name of the key.
     * @return HttpServletRequest with parameters.
     */
    private HttpServletRequest getServletRequestWithParams(ServletRequest servletRequest, String key)
            throws AuthenticationEndpointException {

        String authAPIURL = buildAPIPath(key, servletRequest.getParameter(key));
        String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);
        Gson gson = new Gson();
        Map<String, Object> cachedParams = gson.fromJson(contextProperties, Map.class);
        return new AuthenticationRequestWrapper((HttpServletRequest) servletRequest, cachedParams);
    }

    /**
     * Build the API Path using the key and value.
     *
     * @param key   SessionDataKey or SessionDataKeyConsent.
     * @param value Value of the key.
     * @return API Path.
     */
    private String buildAPIPath(String key, String value) throws AuthenticationEndpointException {

        try {

            String authAPIURL = context.getInitParameter(Constants.AUTHENTICATION_REST_ENDPOINT_URL);
            if (StringUtils.isBlank(authAPIURL)) {
                authAPIURL = ServiceURLBuilder.create().addPath(DATA_API_PATH).build().getAbsoluteInternalURL();
            } else {
                authAPIURL = AuthenticationEndpointUtil.resolveTenantDomain(authAPIURL);
            }

            if (!authAPIURL.endsWith("/")) {
                authAPIURL += "/";
            }

            switch (key) {
                case SESSION_DATA_KEY:
                    // Add 'sessionDataKey' to URL.
                    authAPIURL += "data/AuthRequestKey/" + value;
                    break;
                case SESSION_DATA_KEY_CONSENT:
                    // Add 'sessionDataKeyConsent' to URL.
                    authAPIURL += "data/OauthConsentKey/" + value;
                    break;
                case ERROR_KEY:
                    authAPIURL += "data/AuthenticationError/" + value;
                    break;
            }
            return authAPIURL;
        } catch (URLBuilderException e) {
            String msg = "Error while building Authentication REST Endpoint URL.";
            throw new AuthenticationEndpointException(msg, e);
        }
    }

    @Override
    public void destroy() {
    }
}
