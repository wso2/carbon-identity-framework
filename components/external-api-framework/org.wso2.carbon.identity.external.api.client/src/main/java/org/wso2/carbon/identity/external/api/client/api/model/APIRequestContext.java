/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.external.api.client.api.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for API request context.
 */
public class APIRequestContext {

    private static final Log LOG = LogFactory.getLog(APIRequestContext.class);

    private final HttpMethod httpMethod;
    private final APIAuthentication apiAuthentication;
    private final String endpointUrl;
    private final Map<String, String> headers;
    private final String payload;

    public APIRequestContext(APIRequestContext.Builder builder) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating APIRequestContext with HTTP method: %s, endpoint URL: %s, " +
                            "headers count: %d, payload length: %d",
                    builder.httpMethod, builder.endpointUrl, builder.headers.size(),
                    builder.payload != null ? builder.payload.length() : 0
            ));
        }

        this.httpMethod = builder.httpMethod;
        this.apiAuthentication = builder.apiAuthentication;
        this.endpointUrl = builder.endpointUrl;
        this.headers = builder.headers;
        this.payload = builder.payload;
    }

    public HttpMethod getHttpMethod() {

        return httpMethod;
    }

    public APIAuthentication getApiAuthentication() {

        return apiAuthentication;
    }

    public String getEndpointUrl() {

        return endpointUrl;
    }
    
    public Map<String, String> getHeaders() {

        return Collections.unmodifiableMap(headers);
    }
    
    public String getPayload() {

        return payload;
    }

    /**
     * Builder class for APIRequestContext.
     */
    public static class Builder {

        private HttpMethod httpMethod;
        private APIAuthentication apiAuthentication;
        private String endpointUrl;
        private Map<String, String> headers = new HashMap<>();
        private String payload;

        public APIRequestContext.Builder httpMethod(HttpMethod httpMethod) {

            this.httpMethod = httpMethod;
            return this;
        }

        public APIRequestContext.Builder apiAuthentication(APIAuthentication apiAuthentication) {

            this.apiAuthentication = apiAuthentication;
            return this;
        }

        public APIRequestContext.Builder endpointUrl(String endpointUrl) {

            this.endpointUrl = endpointUrl;
            return this;
        }

        public APIRequestContext.Builder headers(Map<String, String> headers) {

            this.headers = headers;
            return this;
        }

        public APIRequestContext.Builder payload(String payload) {

            this.payload = payload;
            return this;
        }

        public APIRequestContext build() throws APIClientRequestException {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Building APIRequestContext with validation checks.");
            }

            if (httpMethod == null) {
                LOG.error("HTTP Method is missing in APIRequestContext build.");
                throw new APIClientRequestException(ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD, "HTTP Method");
            }
            if (headers == null) {
                LOG.error("HTTP header cannot be set to null.");
                throw new APIClientRequestException(ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD, "HTTP headers");
            }
            if (apiAuthentication == null) {
                LOG.error("Authentication is missing in APIRequestContext build.");
                throw new APIClientRequestException(ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD, "authentication");
            }
            if (endpointUrl == null || StringUtils.isBlank(endpointUrl)) {
                LOG.error("Endpoint URL is missing or blank in APIRequestContext build.");
                throw new APIClientRequestException(ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD, "endpoint URL");
            }
            /* Todo: Payload can be optional for certain HTTP methods like GET.
                     Adjust validation accordingly when introducing new HTTP Method supports
             */
            if (payload == null || StringUtils.isBlank(payload)) {
                LOG.error("Payload is missing or blank in APIRequestContext build.");
                throw new APIClientRequestException(ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD, "payload");
            }

            return new APIRequestContext(this);
        }
    }

    /**
     * Enum for supported HTTP methods.
     */
    public enum HttpMethod {

        POST("post");

        private final String name;

        HttpMethod(String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }
    }
}
