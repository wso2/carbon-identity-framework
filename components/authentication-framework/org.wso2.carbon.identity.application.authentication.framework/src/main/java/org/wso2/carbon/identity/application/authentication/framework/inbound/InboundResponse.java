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

import javax.servlet.http.Cookie;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InboundResponse implements Serializable {

    private static final long serialVersionUID = 263727280546827577L;

    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, Cookie> cookies = new HashMap<String, Cookie>();
    private String contentType;
    private Map<String, String[]> parameters = new HashMap<>();
    private String body;
    private int statusCode;
    private String redirectURL;

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public Map<String, Cookie> getCookies() {
        return Collections.unmodifiableMap(cookies);
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String[]> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public String[] getParameterValues(String paramName) {
        return parameters.get(paramName);
    }

    public String getParameterValue(String paramName) {
        String[] values = parameters.get(paramName);
        if(values.length > 0){
            return values[0];
        }
        return null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public String getBody() {
        return body;
    }

    protected InboundResponse(InboundResponseBuilder builder) {
        this.headers = builder.headers;
        this.cookies = builder.cookies;
        this.contentType = builder.contentType;
        this.parameters = builder.parameters;
        this.statusCode = builder.statusCode;
        this.redirectURL = builder.redirectURL;
        this.body = builder.body;
    }

    public static class InboundResponseBuilder {

        private Map<String, String> headers = new HashMap<String, String>();
        private Map<String, Cookie> cookies = new HashMap<String, Cookie>();
        private String contentType;
        private Map<String, String[]> parameters = new HashMap<>();
        private int statusCode;
        private String redirectURL;
        private String body;

        public String getName(){
            return "InboundResponseBuilder";
        }

        public int getPriority() {
            return 0;
        }

        public InboundResponseBuilder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public InboundResponseBuilder addHeader(String name, String value) {
            String newValue = value;
            if(this.headers.containsKey(name)) {
                String existingValue = headers.get(name);
                newValue =  existingValue +"," + value;
            }
            this.headers.put(name, newValue);
            return this;
        }

        public InboundResponseBuilder addHeaders(Map<String, String> headers) {
            for(Map.Entry<String,String> header:headers.entrySet()) {
                if(this.headers.containsKey(header.getKey())) {
                    throw FrameworkRuntimeException.error("Headers map trying to override existing " +
                            "header " + header.getKey());
                }
                this.headers.put(header.getKey(), header.getValue());
            }
            return this;
        }

        public InboundResponseBuilder setCookies(Map<String, Cookie> cookies) {
            this.cookies = cookies;
            return this;
        }

        public InboundResponseBuilder addCookie(Cookie cookie) {
            if(this.cookies.containsKey(cookie.getName())) {
                throw FrameworkRuntimeException.error("Cookies map trying to override existing " +
                        "cookie " + cookie.getName());
            }
            this.cookies.put(cookie.getName(), cookie);
            return this;
        }

        public InboundResponseBuilder addCookies(Map<String,Cookie> cookies) {
            for(Map.Entry<String,Cookie> cookie:cookies.entrySet()) {
                if(this.cookies.containsKey(cookie.getKey())) {
                    throw FrameworkRuntimeException.error("Cookies map trying to override existing " +
                            "cookie " + cookie.getKey());
                }
                this.cookies.put(cookie.getKey(), cookie.getValue());
            }
            return this;
        }

        public InboundResponseBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public InboundResponseBuilder setParameters(Map<String,String[]> parameters) {
            this.parameters = parameters;
            return this;
        }

        public InboundResponseBuilder addParameter(String name, String value) {
            if(this.parameters.containsKey(name)) {
                throw FrameworkRuntimeException.error("Parameters map trying to override existing " +
                        "key " + name);
            }
            this.parameters.put(name, new String[]{value});
            return this;
        }

        public InboundResponseBuilder addParameter(String name, String[] values) {
            if(this.parameters.containsKey(name)) {
                throw FrameworkRuntimeException.error("Parameters map trying to override existing " +
                        "key " + name);
            }
            this.parameters.put(name, values);
            return this;
        }

        public InboundResponseBuilder addParameters(Map<String,String[]> parameters) {
            for(Map.Entry<String,String[]> parameter:parameters.entrySet()) {
                if(this.parameters.containsKey(parameter.getKey())) {
                    throw FrameworkRuntimeException.error("Parameters map trying to override existing " +
                            "key " + parameter.getKey());
                }
                this.parameters.put(parameter.getKey(), parameter.getValue());
            }
            return this;
        }

        public InboundResponseBuilder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public InboundResponseBuilder setRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
            return this;
        }

        public InboundResponseBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public InboundResponse build() {
            return new InboundResponse(this);
        }

    }
}
