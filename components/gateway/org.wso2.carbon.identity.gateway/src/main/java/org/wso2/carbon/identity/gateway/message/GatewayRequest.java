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

package org.wso2.carbon.identity.gateway.message;

import org.wso2.carbon.identity.framework.message.Request;

public class GatewayRequest extends Request {

    String method;

    String requestUri;


    public String getMethod() {

        return method;
    }

    public String getRequestUri() {

        return requestUri;
    }

    private GatewayRequest(GatewayRequestBuilder requestBuilder) {

        super(requestBuilder);

        method = requestBuilder.method;
        requestUri = requestBuilder.requestUri;
        // fill the rest
    }

    public static class GatewayRequestBuilder extends RequestBuilder {

        private String method;
        private String requestUri;
        private String contentType;


        public GatewayRequestBuilder setMethod(String method) {

            this.method = method;
            return this;
        }

        public GatewayRequestBuilder setRequestUri(String requestUri) {

            this.requestUri = requestUri;
            return this;
        }

        public GatewayRequestBuilder setContentType(String contentType) {

            this.contentType = contentType;
            return this;
        }

        @Override
        public GatewayRequest build() {

            return new GatewayRequest(this);
        }

        public GatewayRequestBuilder addProperty(String key, Object value) {

            this.properties.put(key, value);
            return this;
        }

        public GatewayRequestBuilder setBody(String body) {

            this.body = body;
            return this;
        }
    }
}
