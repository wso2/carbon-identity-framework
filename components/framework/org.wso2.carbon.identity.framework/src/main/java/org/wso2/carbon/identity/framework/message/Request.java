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


import org.wso2.carbon.identity.framework.FrameworkRuntimeException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Immutable request message.
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 2187881873309155315L;

    protected Map<String, String> headers = new HashMap<>();

    protected Map<String, Object> properties = new HashMap<>();

    protected Map<String, Object> attributes = new HashMap<>();

    protected String body;


    protected Request(RequestBuilder requestBuilder) {

        this.headers = requestBuilder.headers;
        this.properties = requestBuilder.properties;
        this.attributes = requestBuilder.attributes;
        this.body = requestBuilder.body;
    }

    public String getBody() {

        return body;
    }


    public Map<String, String> getHeaders() {

        return headers;
    }

    public Object getProperty(String key) {

        return properties.get(key);
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public Map<String, Object> getAttributes() {

        return attributes;
    }


    public static class RequestBuilder {

        protected Map<String, String> headers = new HashMap<>();

        protected Map<String, Object> properties = new HashMap<>();

        protected Map<String, Object> attributes = new HashMap<>();

        protected String body;


        public RequestBuilder setHeaders(Map<String, String> requestHeaders) {

            headers = Optional.ofNullable(requestHeaders).orElse(new HashMap<>());
            return this;
        }

        public RequestBuilder addHeader(String name, String value) {
            // if the header name is not null and is present in the map, we throw an exception
            headers.computeIfPresent(name, (headerName, currentHeaderValue) -> {
                throw FrameworkRuntimeException.error("Headers map trying to override existing header : " + headerName);
            });
            headers.put(name, value);
            return this;
        }

        public RequestBuilder addHeaders(Map<String, String> headers) {

            Optional.ofNullable(headers).ifPresent(x -> x.forEach(this::addHeader));
            return this;
        }

        public Request build() {

            return new Request(this);
        }
    }

}
