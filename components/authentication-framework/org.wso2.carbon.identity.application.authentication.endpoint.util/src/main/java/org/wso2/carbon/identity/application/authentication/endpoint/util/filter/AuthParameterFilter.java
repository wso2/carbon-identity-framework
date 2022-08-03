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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.endpoint.util.AuthContextAPIClient;
import org.wso2.carbon.identity.application.authentication.endpoint.util.Constants;
import org.wso2.carbon.identity.application.authentication.endpoint.util.client.model.AuthenticationRequestWrapper;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

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

/**
 * Cache the parameters required in the authentication flow.
 */
public class AuthParameterFilter implements Filter {

    private static final Log log = LogFactory.getLog(AuthParameterFilter.class);
    private ServletContext context = null;

    @Override
    public void init(FilterConfig filterConfig) {
        context = filterConfig.getServletContext();
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

        // Check if request contains 'sessionDataKey', 'sessionDataKeyConsent', 'errorKey', 'promptId'.
        if (servletRequest.getParameter("sessionDataKey") != null) {
            if (servletRequest instanceof HttpServletRequest) {
                // Cast ServletRequest to HttpServletRequest and get the wrapped request.
                HttpServletRequest httpServletRequest = cacheParamsFromSessionDataKey((HttpServletRequest) servletRequest);
                filterChain.doFilter(httpServletRequest, servletResponse);
                return;
            }
        } else if (servletRequest.getParameter("sessionDataKeyConsent") != null) {
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = cacheParamsFromConsentKey((HttpServletRequest) servletRequest);
                filterChain.doFilter(httpServletRequest, servletResponse);
                return;
            }
        } else if (servletRequest.getParameter("errorKey") != null) {
            if (servletRequest instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = cacheParamsFromErrorKey((HttpServletRequest) servletRequest);
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
    private HttpServletRequest cacheParamsFromSessionDataKey(HttpServletRequest servletRequest) {

        String authAPIURL = buildAPIPath("sessionDataKey", servletRequest.getParameter("sessionDataKey"));
        String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);
        Gson gson = new Gson();
        Map<String, Object> cachedParams = gson.fromJson(contextProperties, Map.class);

        return new AuthenticationRequestWrapper(servletRequest, cachedParams);
    }

    /**
     * Cache the parameters retrieved from the API using SessionDataKey.
     *
     * @param servletRequest - Received servlet request.
     * @return - HttpServletRequestWrapper with cached parameter map.
     */
    private HttpServletRequest cacheParamsFromConsentKey(HttpServletRequest servletRequest) {

        String authAPIURL = buildAPIPath("sessionDataKeyConsent", servletRequest.getParameter("sessionDataKeyConsent"));
        String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);
        Gson gson = new Gson();
        Map<String, Object> cachedParams = gson.fromJson(contextProperties, Map.class);
        return new AuthenticationRequestWrapper(servletRequest, cachedParams);
    }

    /**
     * Cache the parameters retrieved from the API using Error Key.
     *
     * @param servletRequest - Received servlet request.
     * @return - HttpServletRequestWrapper with cached parameter map.
     */
    private HttpServletRequest cacheParamsFromErrorKey(HttpServletRequest servletRequest) {
        String authAPIURL = buildAPIPath("errorKey", servletRequest.getParameter("errorKey"));
        String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);
        Gson gson = new Gson();
        Map<String, Object> cachedParams = gson.fromJson(contextProperties, Map.class);
        return new AuthenticationRequestWrapper(servletRequest, cachedParams);
    }

    /**
     * Build the API Path using the key and value.
     *
     * @param key   - SessionDataKey or SessionDataKeyConsent.
     * @param value - Value of the key.
     * @return - API Path.
     */
    private String buildAPIPath(String key, String value) {

        String authAPIURL = context.getInitParameter(Constants.AUTHENTICATION_REST_ENDPOINT_URL);

        if (!StringUtils.isBlank(authAPIURL)) {
            if (resolveTenantDomain().equals("carbon.super")) {
                authAPIURL = authAPIURL.replace("t/${tenant}/", "");
            } else {
                authAPIURL = authAPIURL.replace("${tenant}", resolveTenantDomain());
            }
        } else {
            try {
                String contextPath = "/api/identity/auth/v1.1/data";
                authAPIURL = ServiceURLBuilder.create().addPath(contextPath).build().getAbsolutePublicURL();
            } catch (URLBuilderException e) {
                throw new RuntimeException(e);
            }
        }

        if (!authAPIURL.endsWith("/")) {
            authAPIURL += "/";
        }

        switch (key) {
            case "sessionDataKey":
                // Add 'sessionDataKey' to URL.
                authAPIURL += "AuthRequestKey/" + value;
                break;
            case "sessionDataKeyConsent":
                // Add 'sessionDataKeyConsent' to URL.
                authAPIURL += "OauthConsentKey/" + value;
                break;
            case "errorKey":
                authAPIURL += "AuthenticationError/" + value;
                break;
        }
        return authAPIURL;
    }

    /**
     * Get the tenant domain.
     *
     * @return - Tenant Domain.
     */
    private String resolveTenantDomain() {

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        return tenantDomain;
    }

    @Override
    public void destroy() {
    }
}
