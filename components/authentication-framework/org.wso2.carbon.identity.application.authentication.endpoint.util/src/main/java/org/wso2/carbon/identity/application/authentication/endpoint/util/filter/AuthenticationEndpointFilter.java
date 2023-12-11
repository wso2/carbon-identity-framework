/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.endpoint.util.AuthenticationEndpointUtil;
import org.wso2.carbon.identity.application.authentication.endpoint.util.Constants;

import java.io.IOException;
import java.util.HashMap;
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

import static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.REQUEST_PARAM_SP;

/**
 * AuthenticationEndpointFilter acts as a front controller for all incoming requests to the authenticationendpoint
 * web application. If there are any custom page configurations added to the web.xml as servlet context parameters,
 * AuthenticationEndpointFilter will look for configurations matching the request uri.
 * If a match is found AuthenticationEndpointFilter will redirect to the custom url defined or else will call the
 * next resource in the chain.
 */
public class AuthenticationEndpointFilter implements Filter {
    private static final Log log = LogFactory.getLog(AuthenticationEndpointFilter.class);

    private static final String REQUEST_PARAM_APPLICATION = "application";
    private static final String REQUEST_PARAM_AUTHENTICATORS = "authenticators";
    private static final String REQUEST_PARAM_HRD = "hrd";
    private static final String REQUEST_PARAM_TYPE = "type";
    private static final String REQUEST_PARAM_REFERRER = "referer";
    private static final String QUERY_SEPARATOR = "&";
    private static final String EQUAL = "=";

    private static final String SAMLSSO = "samlsso";
    private static final String OPENID = "openid";
    private static final String PASSIVESTS = "passivests";
    private static final String OAUTH2 = "oauth2";
    private static final String OIDC = "oidc";
    private static final String FIDO = "fido";

    private static final String URI_LOGIN = "login.do";
    private static final String URI_SAMLSSO_LOGIN = "samlsso_login.do";
    private static final String URI_OPENID_LOGIN = "openid_login.do";
    private static final String URI_PASSIVESTS_LOGIN = "passivests_login.do";
    private static final String URI_OAUTH2_LOGIN = "oauth2_login.do";
    public static final String ATTRIBUTE_SKIP_PROPERTY = "skip";
    private static final String ORGANIZATION_AUTHENTICATOR = "OrganizationAuthenticator";
    public static final String CONSOLE_APPLICATION_NAME = "Console";
    public static final String MY_ACCOUNT_APPLICATION_NAME = "My Account";

    private ServletContext context = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        Object skipFilterAttribute = servletRequest.getAttribute(ATTRIBUTE_SKIP_PROPERTY);
        if (skipFilterAttribute != null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String redirectUrl = null;
        String appSpecificCustomPageConfigKey = null;
        String refererHeader = ((HttpServletRequest) servletRequest).getHeader(REQUEST_PARAM_REFERRER);

        String serviceProviderName = null;
        if (servletRequest.getParameter(Constants.REQUEST_PARAM_SP) != null) {
            serviceProviderName = servletRequest.getParameter(Constants.REQUEST_PARAM_SP);
        } else if (servletRequest.getParameter(REQUEST_PARAM_APPLICATION) != null) {
            serviceProviderName = servletRequest.getParameter(REQUEST_PARAM_APPLICATION);
        } else if (refererHeader != null) {
            String[] queryParams = refererHeader.split(QUERY_SEPARATOR);
            for (String queryParam : queryParams) {
                if (queryParam.contains(REQUEST_PARAM_SP + EQUAL) || queryParam.contains(REQUEST_PARAM_APPLICATION +
                        EQUAL)) {
                    serviceProviderName = queryParam.substring(queryParam.lastIndexOf(EQUAL) + 1);
                    break;
                }
            }
        }

        String relativePath = ((HttpServletRequest) servletRequest).getRequestURI().substring(
                ((HttpServletRequest) servletRequest).getContextPath().length());
        if (StringUtils.isNotBlank(serviceProviderName)) {
            appSpecificCustomPageConfigKey = AuthenticationEndpointUtil.getApplicationSpecificCustomPageConfigKey
                    (serviceProviderName, relativePath);
        }

        if (appSpecificCustomPageConfigKey != null) {
            // Check for application specific custom page mappings matching the request uri.
            redirectUrl =
                    AuthenticationEndpointUtil.getCustomPageRedirectUrl(context.getInitParameter(
                            appSpecificCustomPageConfigKey), ((HttpServletRequest) servletRequest).getQueryString());
        }

        if (redirectUrl == null) {
            // No application specific custom page mappings.
            // Check for global custom page mappings matching the request uri.
            redirectUrl =
                    AuthenticationEndpointUtil.getCustomPageRedirectUrl(context.getInitParameter(
                            relativePath), ((HttpServletRequest) servletRequest).getQueryString());
        }

        if (redirectUrl != null) {
            // There is a custom configuration matching the request uri. Redirect.
            if (log.isDebugEnabled()) {
                log.debug("There is a custom configuration matching the request uri. Redirecting to : " + redirectUrl);
            }
            ((HttpServletResponse) servletResponse).sendRedirect(redirectUrl);
            return;
        }

        if (((HttpServletRequest) servletRequest).getRequestURI().contains(URI_LOGIN)) {
            String hrdParam = servletRequest.getParameter(REQUEST_PARAM_HRD);
            if (hrdParam != null && "true".equalsIgnoreCase(hrdParam)) {
                servletRequest.getRequestDispatcher("domain.jsp").forward(servletRequest, servletResponse);
                return;
            }

            Map<String, String> idpAuthenticatorMapping = new HashMap<String, String>();
            String authenticators = servletRequest.getParameter(REQUEST_PARAM_AUTHENTICATORS);
            if (authenticators != null) {
                String[] authenticatorIdPMappings = authenticators.split(";");
                for (String authenticatorIdPMapping : authenticatorIdPMappings) {
                    String[] authenticatorIdPMapArr = authenticatorIdPMapping.split(":");
                    for (int i = 1; i < authenticatorIdPMapArr.length; i++) {
                        // Organization authenticator is not showed for Console and My Account applications.
                        if (StringUtils.equalsIgnoreCase(ORGANIZATION_AUTHENTICATOR, authenticatorIdPMapArr[0])
                                && (StringUtils.equalsIgnoreCase(CONSOLE_APPLICATION_NAME, serviceProviderName)
                                || StringUtils.equalsIgnoreCase(MY_ACCOUNT_APPLICATION_NAME, serviceProviderName))) {
                            continue;
                        }
                        if (idpAuthenticatorMapping.containsKey(authenticatorIdPMapArr[i])) {
                            idpAuthenticatorMapping.put(authenticatorIdPMapArr[i],
                                                        idpAuthenticatorMapping.get(authenticatorIdPMapArr[i]) + "," +
                                                        authenticatorIdPMapArr[0]);
                        } else {
                            idpAuthenticatorMapping.put(authenticatorIdPMapArr[i], authenticatorIdPMapArr[0]);
                        }
                    }
                }
            }

            if (!idpAuthenticatorMapping.isEmpty()) {
                servletRequest.setAttribute(Constants.IDP_AUTHENTICATOR_MAP, idpAuthenticatorMapping);
            }

            String loadPage;
            String protocolType = servletRequest.getParameter(REQUEST_PARAM_TYPE);
            if (SAMLSSO.equals(protocolType)) {
                loadPage = URI_SAMLSSO_LOGIN;
            } else if (OPENID.equals(protocolType)) {
                loadPage = URI_OPENID_LOGIN;
            } else if (PASSIVESTS.equals(protocolType)) {
                loadPage = URI_PASSIVESTS_LOGIN;
            } else if (OAUTH2.equals(protocolType) || OIDC.equals(protocolType)) {
                loadPage = URI_OAUTH2_LOGIN;
            } else if (FIDO.equals(protocolType)) {
                loadPage = "fido-auth.jsp";
            } else {
                loadPage = "login.jsp";
            }
            // This is done to prevent the recursive dispatching of the filter
            servletRequest.setAttribute(ATTRIBUTE_SKIP_PROPERTY, true);
            servletRequest.getRequestDispatcher(loadPage).forward(servletRequest, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        // Nothing to implement
    }
}
