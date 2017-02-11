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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityRequest implements Serializable {

    private static final long serialVersionUID = 5418537216546873566L;

    protected Map<String, String> headers = new HashMap();
    protected Map<String, String[]> parameters = new HashMap();
    protected Map<String, Object> properties = new HashMap();
    protected List<ByteBuffer> fullMessageBody;
    protected ByteBuffer messageBody;
    protected String httpMethod;
    protected String requestURI;
    protected String contentType;

    protected IdentityRequest(IdentityRequestBuilder builder) {
        this.headers = builder.headers;
        this.parameters = builder.parameters;
        this.properties = builder.properties;
        this.fullMessageBody = builder.fullMessageBody;
        this.messageBody = builder.messageBody;
        this.httpMethod = builder.httpMethod;
        this.requestURI = builder.requestURI;
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

    public String getParameter(String paramName) {
        String[] values = parameters.get(paramName);
        if(values!=null) {
            if (values.length > 0) {
                return values[0];
            }
        }
        return null;
    }

    public Map<String, Object> getPropertyMap() {
        return Collections.unmodifiableMap(properties);
    }

    public Enumeration<String> getPropertyNames() {
        return Collections.enumeration(properties.keySet());
    }

    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    public List<ByteBuffer> getFullMessageBody() {
        return fullMessageBody;
    }

    public ByteBuffer getMessageBody() {
        return messageBody;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getContentType() {
        return contentType;
    }

    public static class IdentityRequestBuilder {

        private Map<String, String> headers = new HashMap();
        private Map<String, String[]> parameters = new HashMap();
        private Map<String, Object> properties = new HashMap();
        protected List<ByteBuffer> fullMessageBody;
        protected ByteBuffer messageBody;
        private String httpMethod;
        private String requestURI;
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

        public IdentityRequestBuilder setProperties(Map<String, Object> properties) {
            if (properties == null) {
                throw FrameworkRuntimeException.error("Properties map is null.");
            }
            this.properties = properties;
            return this;
        }

        public IdentityRequestBuilder addProperty(String name, Object value) {
            if (this.properties.containsKey(name)) {
                throw FrameworkRuntimeException.error("Properties map trying to override existing " +
                                                      "key " + name);
            }
            this.properties.put(name, value);
            return this;
        }

        public IdentityRequestBuilder addAttributes(Map<String, Object> properties) {
            for (Map.Entry<String, Object> property : properties.entrySet()) {
                addProperty(property.getKey(), property.getValue());
            }
            return this;
        }

        public IdentityRequestBuilder setFullMessageBody(List<ByteBuffer> fullMessageBody) {
            this.fullMessageBody = fullMessageBody;
            return this;
        }

        public IdentityRequestBuilder setMessageBody(ByteBuffer messageBody) {
            this.messageBody = messageBody;
            return this;
        }

        public IdentityRequestBuilder setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public IdentityRequestBuilder setRequestURI(String requestURI) {
            this.requestURI = requestURI;
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
