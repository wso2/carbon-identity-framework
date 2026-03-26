/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.external.api.token.handler.api.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for Token Request Context.
 */
public class TokenRequestContext {

    private static final Log LOG = LogFactory.getLog(TokenRequestContext.class);

    private final GrantContext grantContext;
    private final String tokenEndpointUrl;
    private final Map<String, String> headers;
    private HttpEntity payload;
    private HttpEntity refreshGrantPayload;

    public TokenRequestContext(TokenRequestContext.Builder builder) {

        this.grantContext = builder.grantContext;
        this.tokenEndpointUrl = builder.endpointUrl;
        this.headers = Collections.unmodifiableMap(
                new HashMap<>(builder.headers != null ? builder.headers : Collections.emptyMap()));
        this.payload = builder.payload;
    }

    /**
     * Get Grant Context.
     *
     * @return Grant Context.
     */
    public GrantContext getGrantContext() {

        return grantContext;
    }

    /**
     * Get Token Endpoint URL.
     *
     * @return Token Endpoint URL.
     */
    public String getTokenEndpointUrl() {

        return tokenEndpointUrl;
    }

    /**
     * Get Headers.
     *
     * @return Headers.
     */
    public Map<String, String> getHeaders() {

        return headers;
    }

    /**
     * Set Payload.
     *
     * @param payload Payload.
     * @throws TokenRequestException TokenRequestException.
     */
    public void setPayLoad(HttpEntity payload) throws TokenRequestException {

        if (payload == null) {
            throw new TokenRequestException(ErrorMessage.ERROR_CODE_INVALID_PAYLOAD, "token");
        }
        this.payload = payload;
    }

    /**
     * Get Payload.
     *
     * @return Payload.
     */
    public HttpEntity getPayLoad() {

        return payload;
    }

    /**
     * Set Refresh Grant Payload.
     *
     * @param refreshGrantPayload Refresh Grant Payload.
     * @throws TokenRequestException TokenRequestException.
     */
    public void setRefreshGrantPayload(HttpEntity refreshGrantPayload) throws TokenRequestException {

        if (refreshGrantPayload == null) {
            throw new TokenRequestException(ErrorMessage.ERROR_CODE_INVALID_PAYLOAD, "refresh grant");
        }
        this.refreshGrantPayload = refreshGrantPayload;
    }

    /**
     * Get Refresh Grant Payload.
     *
     * @return Refresh Grant Payload.
     */
    public HttpEntity getRefreshGrantPayload() {

        return refreshGrantPayload;
    }

    /**
     * Builder class for TokenRequestContext.
     */
    public static class Builder {

        private GrantContext grantContext;
        private String endpointUrl;
        private Map<String, String> headers = new HashMap<>();
        private HttpEntity payload;

        /**
         * Set Grant Context.
         *
         * @param grantContext Grant Context.
         * @return Builder.
         */
        public Builder grantContext(GrantContext grantContext) {

            this.grantContext = grantContext;
            return this;
        }

        /**
         * Set Endpoint URL.
         *
         * @param endpointUrl Endpoint URL.
         * @return Builder.
         */
        public Builder endpointUrl(String endpointUrl) {

            this.endpointUrl = endpointUrl;
            return this;
        }

        /**
         * Set Headers.
         *
         * @param headers Headers.
         * @return Builder.
         */
        public Builder headers(Map<String, String> headers) {

            this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
            return this;
        }

        /**
         * Set Payload.
         *
         * @param payload Payload.
         * @return Builder.
         */
        public Builder payload(HttpEntity payload) {

            this.payload = payload;
            return this;
        }

        /**
         * Build TokenRequestContext.
         *
         * @return TokenRequestContext.
         * @throws TokenRequestException TokenRequestException.
         */
        public TokenRequestContext build() throws TokenRequestException {

            if (grantContext == null) {
                throw new TokenRequestException(ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD, "grant context");
            }
            if (StringUtils.isBlank(endpointUrl)) {
                throw new TokenRequestException(ErrorMessage.ERROR_CODE_MISSING_REQUEST_FIELD, "token endpoint url");
            }
            return new TokenRequestContext(this);
        }
    }
}

