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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Response implements Serializable {


    private static final long serialVersionUID = -4486397485075114597L;

    protected Map<String, String> headers;

    protected Map<String, Object> properties;

    protected Map<String, Object> attributes;

    protected String body;

    protected int statusCode;


    protected Response(ResponseBuilder responseBuilder) {

        headers = responseBuilder.headers;
        properties = responseBuilder.properties;
        attributes = responseBuilder.attributes;
        body = responseBuilder.body;
        statusCode = responseBuilder.statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
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


    public static class ResponseBuilder {

        protected Map<String, String> headers = new HashMap<>();

        protected Map<String, Object> properties = new HashMap<>();

        protected Map<String, Object> attributes = new HashMap<>();

        protected String body;

        protected int statusCode;


        public ResponseBuilder setBody(String body) {

            this.body = body;
            return this;
        }

        public ResponseBuilder setStatusCode(int statusCode) {

            this.statusCode = statusCode;
            return this;
        }


        public ResponseBuilder addHeader(String name, String value) {

            headers.put(name, value);
            return this;
        }

        public ResponseBuilder addHeaders(Map<String, String> headers) {

            Optional.ofNullable(headers).ifPresent(x -> x.forEach(this::addHeader));
            return this;
        }

        public ResponseBuilder addProperty(String name, Object value) {

            properties.put(name, value);
            return this;
        }

        public ResponseBuilder addProperties(Map<String, String> properties) {

            Optional.ofNullable(properties).ifPresent(x -> x.forEach(this::addProperty));
            return this;
        }

        public ResponseBuilder addAttribute(String name, Object value) {

            attributes.put(name, value);
            return this;
        }

        public ResponseBuilder addAttributes(Map<String, String> attributes) {

            Optional.ofNullable(attributes).ifPresent(x -> x.forEach(this::addAttribute));
            return this;
        }


        public Response build() {

            return new Response(this);
        }


    }
}
