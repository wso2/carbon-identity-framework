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

package org.wso2.carbon.identity.framework.builder;

import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.message.IdentityMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class IdentityMessageBuilder<T extends IdentityMessage> {

    /*
       HTTP Request Headers.
    */
    protected Map<String, String> headers = new HashMap<>();
    //        protected Map<String, Cookie> cookies = new HashMap<>();


    /**
     * Properties
     */
    protected Map<String, Object> properties = new HashMap<>();

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
    protected String queryString;

    protected String requestURI;

    protected StringBuffer requestURL;

    protected String contentType;

    // TODO ADD COOKIE ADDING SUPPORT

    protected String body;

    public IdentityMessageBuilder setHeaders(Map<String, String> requestHeaders) {
        headers = Optional.ofNullable(requestHeaders).orElse(new HashMap<>());
        return this;
    }

    public IdentityMessageBuilder addHeader(String name, String value) {
        // if the header name is not null and is present in the map, we throw an exception
        headers.computeIfPresent(name, (headerName, currentHeaderValue) -> {
            throw FrameworkRuntimeException.error("Headers map trying to override existing header " + headerName);
        });
        headers.put(name, value);
        return this;
    }

    public IdentityMessageBuilder addHeaders(Map<String, String> headers) {
        Optional.ofNullable(headers).ifPresent(x -> x.forEach(this::addHeader));
        return this;
    }

    public IdentityMessageBuilder setProperties(Map<String, Object> properties) {
        properties = Optional.ofNullable(properties).orElse(new HashMap<>());
        return this;
    }

    public IdentityMessageBuilder addProperty(String propertyName, Object propertyValue) {
        // if the header name is not null and is present in the map, we throw an exception
        properties.computeIfPresent(propertyName, (headerName, currentHeaderValue) -> {
            throw FrameworkRuntimeException.error("Properties map trying to override existing property " + headerName);
        });
        properties.put(propertyName, propertyValue);
        return this;
    }

    public IdentityMessageBuilder addProperties(Map<String, Object> attributes) {
        Optional.ofNullable(attributes).ifPresent(propertiesMap -> propertiesMap.forEach(this::addProperty));
        return this;
    }

    public IdentityMessageBuilder setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
        return this;
    }

    public IdentityMessageBuilder setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public IdentityMessageBuilder setMethod(String method) {
        this.method = method;
        return this;
    }

    public IdentityMessageBuilder setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public IdentityMessageBuilder setRequestURI(String requestURI) {
        this.requestURI = requestURI;
        return this;
    }

    public IdentityMessageBuilder setRequestURL(StringBuffer requestURL) {
        this.requestURL = requestURL;
        return this;
    }


    public IdentityMessageBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public abstract T build() throws FrameworkRuntimeException;

    public String getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getProperties() {
        return properties;
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

    public String getQueryString() {
        return queryString;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public StringBuffer getRequestURL() {
        return requestURL;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }
}
