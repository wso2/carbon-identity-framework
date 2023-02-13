/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP identity request factory.
 */
public class HttpIdentityRequestFactory extends AbstractIdentityHandler {

    private static Log log = LogFactory.getLog(HttpIdentityRequestFactory.class);

    protected final Properties properties = new Properties();

    protected InitConfig initConfig;

    public void init(InitConfig initConfig) {

        this.initConfig = initConfig;

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (HttpIdentityRequestFactory.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return;
        }

        if (identityEventListenerConfig.getProperties() != null) {
            for (Map.Entry<Object, Object> property : identityEventListenerConfig.getProperties().entrySet()) {
                String key = (String) property.getKey();
                String value = (String) property.getValue();
                if (!properties.containsKey(key)) {
                    properties.setProperty(key, value);
                } else {
                    log.warn("Property key " + key + " already exists. Cannot add property!!");
                }
            }
        }
    }

    public boolean canHandle(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    public IdentityRequest.IdentityRequestBuilder create(HttpServletRequest request,
                                                         HttpServletResponse response) throws FrameworkClientException {

        IdentityRequest.IdentityRequestBuilder builder = new IdentityRequest.IdentityRequestBuilder(request, response);
        create(builder, request, response);
        return builder;
    }

    public void create(IdentityRequest.IdentityRequestBuilder builder,
                       HttpServletRequest request,
                       HttpServletResponse response) throws FrameworkClientException {

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            builder.addHeader(headerName, request.getHeader(headerName));
        }

        // We need to create a new map with the parameters sent in servlet request to avoid having a reference.
        Map<String, String[]> paramMap = new HashMap<>(request.getParameterMap());
        builder.setParameters(paramMap);

        Enumeration<String> attrNames = request.getAttributeNames();
        while (attrNames.hasMoreElements()) {
            String attrName = attrNames.nextElement();
            builder.addAttribute(attrName, request.getAttribute(attrName));
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                builder.addCookie(cookie.getName(), cookie);
            }
        }

        String requestURI = request.getRequestURI();

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isNotBlank(tenantDomain)) {
            builder.setTenantDomain(tenantDomain);
        } else {
            builder.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        }

        builder.setContentType(request.getContentType());
        builder.setContextPath(request.getContextPath());
        builder.setMethod(request.getMethod());
        builder.setPathInfo(request.getPathInfo());
        builder.setPathTranslated(request.getPathTranslated());
        builder.setQueryString(request.getQueryString());
        builder.setRequestURI(requestURI);
        builder.setRequestURL(request.getRequestURL());
        builder.setServletPath(request.getServletPath());

    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkClientException exception,
                                                                            HttpServletRequest request,
                                                                            HttpServletResponse response) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder
                = new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(400);
        builder.setBody(exception.getMessage());
        return builder;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(
                                                                    FrameworkResourceNotFoundException exception,
                                                                    HttpServletRequest request,
                                                                    HttpServletResponse response) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder =
                new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(404);
        builder.setBody(exception.getMessage());
        return builder;
    }

    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(RuntimeException exception,
                                                                            HttpServletRequest request,
                                                                            HttpServletResponse response) {

        HttpIdentityResponse.HttpIdentityResponseBuilder builder
                = new HttpIdentityResponse.HttpIdentityResponseBuilder();
        builder.setStatusCode(500);
        return builder;
    }
}
