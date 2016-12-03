/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.framework.request;


import org.wso2.carbon.identity.framework.request.builder.IdentityRequestBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Canonical representation of the request received by the Framework.
 */
public class IdentityRequest implements Serializable {

    private static final long serialVersionUID = -5383873912447501747L;

    protected Map<String, String> headers = new HashMap<>();
    //    protected Map<String, Cookie> cookies = new HashMap<>();
    protected Map<String, String[]> parameters = new HashMap<>();
    protected Map<String, Object> properties = new HashMap<>();
    protected String tenantDomain;
    protected String contextPath;
    protected String method;
    protected String pathInfo;
    protected String pathTranslated;
    protected String queryString;
    protected String requestURI;
    protected StringBuffer requestURL;
    protected String servletPath;
    protected String contentType;


    public IdentityRequest(IdentityRequestBuilder builder) {
        this.headers = builder.getHeaders();
//        this.cookies = builder.cookies;
        this.parameters = builder.getParameters();
        this.properties = builder.getAttributes();
        this.tenantDomain = builder.getTenantDomain();
        this.contextPath = builder.getContextPath();
        this.method = builder.getMethod();
        this.pathInfo = builder.getPathInfo();
        this.pathTranslated = builder.getPathTranslated();
        this.queryString = builder.getQueryString();
        this.requestURI = builder.getRequestURI();
        this.requestURL = builder.getRequestURL();
        this.servletPath = builder.getServletPath();
        this.contentType = builder.getContentType();
    }

    public Map<String, String> getHeaderMap() {
        return Collections.unmodifiableMap(headers);
    }

    public Enumeration<String> getHeaders(String name) {
        String headerValue = headers.get(name);
        String[] multiValuedHeader = headerValue.split(",");
        return Collections.enumeration(Arrays.asList(multiValuedHeader));
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

//    public Map<String, Cookie> getCookieMap() {
//        return Collections.unmodifiableMap(cookies);
//    }
//
//    public Cookie[] getCookies() {
//        Collection<Cookie> cookies = getCookieMap().values();
//        return cookies.toArray(new Cookie[cookies.size()]);
//    }

    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    public String[] getParameterValues(String paramName) {
        return parameters.get(paramName);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Enumeration<String> getPropertyNames() {
        return Collections.enumeration(properties.keySet());
    }

    public Object getProperty(String paramName) {
        return properties.get(paramName);
    }


    public String getTenantDomain() {
        return this.tenantDomain;
    }

    public String getParameter(String paramName) {
        String[] values = parameters.get(paramName);
        if (values != null) {
            if (values.length > 0) {
                return values[0];
            }
        }
        return null;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getMethod() {
        return method;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getPathTranslated() {
        return pathTranslated;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public StringBuffer getRequestURL() {
        return requestURL;
    }

    public String getServletPath() {
        return servletPath;
    }

    public String getContentType() {
        return contentType;
    }

//    public String getBrowserCookieValue() {
//        String cookieValue = null;
//        Cookie cookie = this.getCookieMap().get(IdentityRequestConstants.BROWSER_COOKIE);
//        if (cookie != null) {
//            cookieValue = cookie.getValue();
//        }
//        return cookieValue;
//    }


    public static class IdentityRequestConstants {
        public static final String BROWSER_COOKIE = "SIOWTOSW";
    }
}
