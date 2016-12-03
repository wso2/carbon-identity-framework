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

package org.wso2.carbon.identity.framework.request.builder;

import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.request.IdentityRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IdentityRequestBuilder {

    /*
        HTTP Request Headers.
     */
    protected Map<String, String> headers = new HashMap<>();
    //        protected Map<String, Cookie> cookies = new HashMap<>();


    /*
        Parameters sent in the request.
     */
    protected Map<String, String[]> parameters = new HashMap<>();

    protected Map<String, Object> attributes = new HashMap<>();

    /*
        Service Provider Identifier.
     */
    protected String serviceProvider;

    /*
        TenantDomain of the Request.
     */
    protected String tenantDomain;

    // TODO : Do we need this?
    protected String contextPath;

    /*
        HTTP Request Method.
     */
    protected String method;

    // TODO : Do we need these?
    protected String pathInfo;
    protected String pathTranslated;
    protected String queryString;
    protected String requestURI;
    protected StringBuffer requestURL;
    protected String servletPath;
    protected String contentType;


    public IdentityRequestBuilder() {

    }

    public IdentityRequestBuilder setHeaders(Map<String, String> requestHeaders) {
        headers = Optional.ofNullable(requestHeaders).orElse(new HashMap<>());
        return this;
    }

    public IdentityRequestBuilder addHeader(String name, String value) {
        // if the header name is not null and is present in the map, we throw an exception
        headers.computeIfPresent(name, (headerName, currentHeaderValue) -> {
            throw FrameworkRuntimeException.error("Headers map trying to override existing header " + headerName);
        });
        headers.put(name, value);
        return this;
    }

    public IdentityRequestBuilder addHeaders(Map<String, String> headers) {
        Optional.ofNullable(headers).ifPresent(x -> x.forEach(this::addHeader));
        return this;
    }


//        public IdentityRequestBuilder setCookies(Map<String, Cookie> cookies) {
//            this.cookies = cookies;
//            return this;
//        }
//
//        public IdentityRequestBuilder addCookie(String name, Cookie value) {
//            if (this.cookies.containsKey(name)) {
//                throw FrameworkRuntimeException.error("Cookies map trying to override existing " +
//                        "cookie " + name);
//            }
//            this.cookies.put(name, value);
//            return this;
//        }

//        public IdentityRequestBuilder addCookies(Map<String, Cookie> cookies) {
//            if (cookies != null) {
//                for (Map.Entry<String, Cookie> cookie : cookies.entrySet()) {
//                    addCookie(cookie.getKey(), cookie.getValue());
//                }
//            }
//            return this;
//        }

    public IdentityRequestBuilder setParameters(Map<String, String[]> parameters) {
        this.parameters = Optional.ofNullable(parameters).orElse(new HashMap<>());
        return this;
    }

    public IdentityRequestBuilder addParameter(String name, String[] values) {
        // if the parameter name is not null and is present in the map, we throw an exception
        parameters.computeIfPresent(name, (paramName, currentHeaderValue) -> {
            throw FrameworkRuntimeException.error("Parameters map trying to override existing key: " + paramName);
        });

        parameters.put(name, values);
        return this;
    }

    public IdentityRequestBuilder addParameter(String name, String value) {
        // if the parameter name is not null and is present in the map, we throw an exception
        parameters.computeIfPresent(name, (paramName, currentHeaderValue) -> {
            throw FrameworkRuntimeException.error("Parameters map trying to override existing key: " + paramName);
        });

        parameters.put(name, new String[]{value});
        return this;
    }

    public IdentityRequestBuilder addParameters(Map<String, String[]> parameters) {
        Optional.ofNullable(parameters).ifPresent(x -> x.forEach(this::addParameter));
        return this;
    }

    public IdentityRequestBuilder setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    public IdentityRequestBuilder addAttribute(String name, Object value) {
        if (this.attributes.containsKey(name)) {
            throw FrameworkRuntimeException.error("Attributes map trying to override existing " +
                    "key " + name);
        }
        this.attributes.put(name, value);
        return this;
    }

    public IdentityRequestBuilder addAttributes(Map<String, Object> attributes) {
        if (attributes != null) {
            for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                addAttribute(attribute.getKey(), attribute.getValue());
            }
        }
        return this;
    }

    public IdentityRequestBuilder setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
        return this;
    }

    public IdentityRequestBuilder setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public IdentityRequestBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public IdentityRequestBuilder setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
        return this;
    }

    public IdentityRequestBuilder setPathTranslated(String pathTranslated) {
        this.pathTranslated = pathTranslated;
        return this;
    }

    public IdentityRequestBuilder setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public IdentityRequestBuilder setRequestURI(String requestURI) {
        this.requestURI = requestURI;
        return this;
    }

    public IdentityRequestBuilder setRequestURL(StringBuffer requestURL) {
        this.requestURL = requestURL;
        return this;
    }

    public IdentityRequestBuilder setServletPath(String servletPath) {
        this.servletPath = servletPath;
        return this;
    }

    public IdentityRequestBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public IdentityRequest build() throws FrameworkRuntimeException {
        return new IdentityRequest(this);
    }

    public String getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getTenantDomain() {
        return tenantDomain;
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
}