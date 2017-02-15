/*
 *
 *  * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * WSO2 Inc. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package org.wso2.carbon.identity.gateway.api.request;

import org.wso2.carbon.identity.gateway.api.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.util.Constants;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class IdentityRequest implements Serializable {

    private static final long serialVersionUID = 5418537216546873566L;

    protected Map<String, String> headers = new HashMap();
    protected Map<String, Object> parameters = new HashMap();
    protected Map<String, Object> attributes = new HashMap();
    protected String httpMethod;
    protected String requestURI;
    protected String contentType;
    protected String queryString;

    protected IdentityRequest(IdentityRequestBuilder builder) {
        this.headers = builder.headers;
        this.parameters = builder.parameters;
        this.attributes = builder.attributes;
        this.httpMethod = builder.httpMethod;
        this.requestURI = builder.requestURI;
        this.contentType = builder.contentType;
        this.queryString = builder.queryString;
    }

    public Object getAttribute(String attributeName) {
        return parameters.get(attributeName);
    }

    public Map<String, Object> getAttributeMap() {
        return Collections.unmodifiableMap(attributes);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public String getBodyParameter(String paramName) {
        Map<String, String> queryParams = (Map<String, String>) parameters.get(Constants.BODY_PARAMETERS);
        return queryParams.get(paramName);
    }

    public String getContentType() {
        return contentType;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getHeaderMap() {
        return Collections.unmodifiableMap(headers);
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    public Enumeration<String> getHeaders(String name) {
        String headerValue = headers.get(name);
        String[] multiValuedHeader = headerValue.split(",");
        return Collections.enumeration(Arrays.asList(multiValuedHeader));
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getParameter(String paramName) {
        Map<String, String> queryParams = (Map<String, String>) parameters.get(Constants.QUERY_PARAMETERS);
        Map<String, String> bodyParams = (Map<String, String>) parameters.get(Constants.BODY_PARAMETERS);
        if (queryParams.get(paramName) != null) {
            return queryParams.get(paramName);
        } else {
            return bodyParams.get(paramName);
        }
    }

    public Map<String, Object> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    public String getQueryParameter(String paramName) throws UnsupportedEncodingException {
        Map<String, String> queryParams = (Map<String, String>) parameters.get(Constants.QUERY_PARAMETERS);
        URLDecoder.decode(queryParams.get(paramName), StandardCharsets.UTF_8.name());

        return queryParams.get(paramName);
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public static class IdentityRequestBuilder {

        private Map<String, String> headers = new HashMap();
        private Map<String, Object> parameters = new HashMap();
        private Map<String, Object> attributes = new HashMap();
        private String httpMethod;
        private String requestURI;
        private String contentType;
        private String queryString;

        public IdentityRequestBuilder() {

        }

        public IdentityRequestBuilder addAttribute(String name, Object value) {
            if (this.attributes.containsKey(name)) {
                throw new FrameworkRuntimeException("Attributes map trying to override existing key " + name);
            }
            this.attributes.put(name, value);
            return this;
        }

        public IdentityRequestBuilder addAttributes(Map<String, Object> attributes) {
            for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                if (this.attributes.containsKey(attribute.getKey())) {
                    throw new FrameworkRuntimeException("Attributes map trying to override existing key " + attribute
                            .getKey());
                }
                this.attributes.put(attribute.getKey(), attribute.getValue());
            }
            return this;
        }

        public IdentityRequestBuilder addHeader(String name, String value) {
            if (this.headers.containsKey(name)) {
                throw new FrameworkRuntimeException("Headers map trying to override existing header " + name);
            }
            this.headers.put(name, value);
            return this;
        }

        public IdentityRequestBuilder addHeaders(Map<String, String> headers) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (this.headers.containsKey(header.getKey())) {
                    throw new FrameworkRuntimeException("Headers map trying to override existing header " + header
                            .getKey());
                }
                this.headers.put(header.getKey(), header.getValue());
            }
            return this;
        }

        public IdentityRequestBuilder addParameter(String name, Object value) {
            if (this.parameters.containsKey(name)) {
                throw new FrameworkRuntimeException("Parameters map trying to override existing key " + name);
            }
            this.parameters.put(name, value);
            return this;
        }

        public IdentityRequestBuilder addParameters(Map<String, Object> parameters) {
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                if (this.parameters.containsKey(parameter.getKey())) {
                    throw new FrameworkRuntimeException("Parameters map trying to override existing key " + parameter
                            .getKey());
                }
                this.parameters.put(parameter.getKey(), parameter.getValue());
            }
            return this;
        }

        public IdentityRequest build() throws FrameworkRuntimeException {
            return new IdentityRequest(this);
        }

        public IdentityRequestBuilder setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public IdentityRequestBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public IdentityRequestBuilder setHeaders(Map<String, String> responseHeaders) {
            this.headers = responseHeaders;
            return this;
        }

        public IdentityRequestBuilder setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public IdentityRequestBuilder setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
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
    }

    public static class IdentityRequestConstants {

    }
}
