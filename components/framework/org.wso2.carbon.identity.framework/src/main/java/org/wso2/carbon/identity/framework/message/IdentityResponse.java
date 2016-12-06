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

package org.wso2.carbon.identity.framework.message;

import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class IdentityResponse extends IdentityMessage implements Serializable {


    private static final long serialVersionUID = -4486397485075114597L;
    protected String body;
    protected int statusCode;


    public IdentityResponse() {
    }

    public void setHeaders(Map<String, String> requestHeaders) {
        headers = Optional.ofNullable(requestHeaders).orElse(new HashMap<>());
    }

    public void addHeader(String name, String value) {
        // if the header name is not null and is present in the map, we throw an exception
        headers.computeIfPresent(name, (headerName, currentHeaderValue) -> {
            throw FrameworkRuntimeException.error("Headers map trying to override existing header " + headerName);
        });
        headers.put(name, value);
    }

    public void addHeaders(Map<String, String> headers) {
        Optional.ofNullable(headers).ifPresent(x -> x.forEach(this::addHeader));
    }


    public void addProperty(String propertyName, Object propertyValue) {
        // if the header name is not null and is present in the map, we throw an exception
        properties.computeIfPresent(propertyName, (headerName, currentHeaderValue) -> {
            throw FrameworkRuntimeException.error("Properties map trying to override existing property " + headerName);
        });
        properties.put(propertyName, propertyValue);
    }

    public void addProperties(Map<String, Object> attributes) {
        Optional.ofNullable(attributes).ifPresent(propertiesMap -> propertiesMap.forEach(this::addProperty));
    }


    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }


    public void setMethod(String method) {
        this.method = method;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public void setPathTranslated(String pathTranslated) {
        this.pathTranslated = pathTranslated;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public void setRequestURL(StringBuffer requestURL) {
        this.requestURL = requestURL;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
