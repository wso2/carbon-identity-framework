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

package org.wso2.carbon.identity.gateway.api;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class IdentityRequest implements Serializable {

    private static final long serialVersionUID = 5418537216546873566L;

    protected Map<String, String> headers = new HashMap<>();
    protected Map<String, String[]> parameters = new HashMap<>();
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


    protected IdentityRequest(IdentityRequestBuilder builder) {
        this.headers = builder.headers;
        this.parameters = builder.parameters;
        this.tenantDomain = builder.tenantDomain;
        this.contextPath = builder.contextPath;
        this.method = builder.method;
        this.pathInfo = builder.pathInfo;
        this.pathTranslated = builder.pathTranslated;
        this.queryString = builder.queryString;
        this.requestURI = builder.requestURI;
        this.requestURL = builder.requestURL;
        this.servletPath = builder.servletPath;
        this.contentType = builder.contentType;
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



    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    public String[] getParameterValues(String paramName) {
        return parameters.get(paramName);
    }

    public String getTenantDomain() {
        return this.tenantDomain;
    }

    public String getParameter(String paramName) {
        String[] values = parameters.get(paramName);
        if(values!=null) {
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



    public static class IdentityRequestBuilder {

        private Map<String, String> headers = new HashMap<>();
        private Map<String, String[]> parameters = new HashMap<>();
        private String tenantDomain;
        private String contextPath;
        private String method;
        private String pathInfo;
        private String pathTranslated;
        private String queryString;
        private String requestURI;
        private StringBuffer requestURL;
        private String servletPath;
        private String contentType;


        public IdentityRequestBuilder() {

        }

        public IdentityRequestBuilder setHeaders(Map<String, String> responseHeaders) {
            this.headers = responseHeaders;
            return this;
        }

        public IdentityRequestBuilder addHeaders(Map<String, String> headers) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (this.headers.containsKey(header.getKey())) {
                    throw FrameworkRuntimeException.error("Headers map trying to override existing " +
                                                          "header " + header.getKey());
                }
                this.headers.put(header.getKey(), header.getValue());
            }
            return this;
        }

        public IdentityRequestBuilder addHeader(String name, String value) {
            if (this.headers.containsKey(name)) {
                throw FrameworkRuntimeException.error("Headers map trying to override existing " +
                                                      "header " + name);
            }
            this.headers.put(name, value);
            return this;
        }


        public IdentityRequestBuilder setParameters(Map<String, String[]> parameters) {
            this.parameters = parameters;
            return this;
        }

        public IdentityRequestBuilder addParameter(String name, String[] values) {
            if (this.parameters.containsKey(name)) {
                throw FrameworkRuntimeException.error("Parameters map trying to override existing " +
                                                      "key " + name);
            }
            this.parameters.put(name, values);
            return this;
        }

        public IdentityRequestBuilder addParameter(String name, String value) {
            if (this.parameters.containsKey(name)) {
                throw FrameworkRuntimeException.error("Parameters map trying to override existing " +
                                                      "key " + name);
            }
            this.parameters.put(name, new String[]{value});
            return this;
        }

        public IdentityRequestBuilder addParameters(Map<String, String[]> parameters) {
            for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                if (this.parameters.containsKey(parameter.getKey())) {
                    throw FrameworkRuntimeException.error("Parameters map trying to override existing key " +
                                                          parameter.getKey());
                }
                this.parameters.put(parameter.getKey(), parameter.getValue());
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


    }

    public static class IdentityRequestConstants {
        public static final String BROWSER_COOKIE = "SIOWTOSW";
    }
}
