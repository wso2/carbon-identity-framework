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

package org.wso2.carbon.identity.framework.response;

import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpIdentityResponse {

    protected Map<String, String> headers = new HashMap<String, String>();
//    protected Map<String, Cookie> cookies = new HashMap<String, Cookie>();
    protected String contentType;
    protected Map<String, String[]> parameters = new HashMap<>();
    protected String body;
    protected int statusCode;
    protected String redirectURL;

    protected HttpIdentityResponse(HttpIdentityResponseBuilder builder) {
        this.headers = builder.headers;
//        this.cookies = builder.cookies;
        this.contentType = builder.contentType;
        this.parameters = builder.parameters;
        this.statusCode = builder.statusCode;
        this.redirectURL = builder.redirectURL;
        this.body = builder.body;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

//    public Map<String, Cookie> getCookies() {
//        return Collections.unmodifiableMap(cookies);
//    }

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
        if (values.length > 0) {
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


    public static class HttpIdentityResponseBuilder {

        protected Map<String, String> headers = new HashMap<String, String>();
//        protected Map<String, Cookie> cookies = new HashMap<String, Cookie>();
        protected String contentType;
        protected Map<String, String[]> parameters = new HashMap<>();
        protected int statusCode;
        protected String redirectURL;
        protected String body;

        public HttpIdentityResponseBuilder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public HttpIdentityResponseBuilder addHeader(String name, String value) {
            String newValue = value;
            if (this.headers.containsKey(name)) {
                String existingValue = headers.get(name);
                newValue = existingValue + "," + value;
            }
            this.headers.put(name, newValue);
            return this;
        }

        public HttpIdentityResponseBuilder addHeaders(Map<String, String> headers) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (this.headers.containsKey(header.getKey())) {
                    throw FrameworkRuntimeException.error("Headers map trying to override existing " +
                            "header " + header.getKey());
                }
                this.headers.put(header.getKey(), header.getValue());
            }
            return this;
        }

//        public HttpIdentityResponseBuilder setCookies(Map<String, Cookie> cookies) {
//            this.cookies = cookies;
//            return this;
//        }
//
//        public HttpIdentityResponseBuilder addCookie(Cookie cookie) {
//            if (this.cookies.containsKey(cookie.getName())) {
//                throw FrameworkRuntimeException.error("Cookies map trying to override existing " +
//                        "cookie " + cookie.getName());
//            }
//            this.cookies.put(cookie.getName(), cookie);
//            return this;
//        }
//
//        public HttpIdentityResponseBuilder addCookies(Map<String, Cookie> cookies) {
//            if (cookies != null) {
//                for (Cookie cookie : cookies.values()) {
//                    addCookie(cookie);
//                }
//            }
//            return this;
//        }

        public HttpIdentityResponseBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public HttpIdentityResponseBuilder setParameters(Map<String, String[]> parameters) {
            if (parameters != null) {
                this.parameters = parameters;
            }
            return this;
        }

        public HttpIdentityResponseBuilder addParameter(String name, String value) {
            if (this.parameters.containsKey(name)) {
                throw FrameworkRuntimeException.error("Parameters map trying to override existing " +
                        "key " + name);
            }
            this.parameters.put(name, new String[]{value});
            return this;
        }

        public HttpIdentityResponseBuilder addParameter(String name, String[] values) {
            if (this.parameters.containsKey(name)) {
                throw FrameworkRuntimeException.error("Parameters map trying to override existing " +
                        "key " + name);
            }
            this.parameters.put(name, values);
            return this;
        }

        public HttpIdentityResponseBuilder addParameters(Map<String, String[]> parameters) {
            if (parameters != null) {
                for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                    addParameter(parameter.getKey(), parameter.getValue());
                }
            }
            return this;
        }

        public HttpIdentityResponseBuilder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public HttpIdentityResponseBuilder setRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
            return this;
        }

        public HttpIdentityResponseBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public HttpIdentityResponse build() {
            return new HttpIdentityResponse(this);
        }

    }
}
