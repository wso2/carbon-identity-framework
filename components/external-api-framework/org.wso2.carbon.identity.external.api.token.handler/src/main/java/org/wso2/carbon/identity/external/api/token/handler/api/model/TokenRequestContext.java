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

package org.wso2.carbon.identity.external.api.token.handler.api.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;

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
    private String payload;
    private String refreshGrantPayload;

    public TokenRequestContext(TokenRequestContext.Builder builder) {

        this.grantContext = builder.grantContext;
        this.tokenEndpointUrl = builder.endpointUrl;
        this.headers = builder.headers;
        this.payload = builder.payload;

        if (LOG.isDebugEnabled()) {
            LOG.debug("TokenRequestContext created successfully for endpoint: " + tokenEndpointUrl);
        }
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
    public void setPayLoad(String payload) throws TokenRequestException {

        if (StringUtils.isBlank(payload)) {
            throw new TokenRequestException("Payload cannot be null or empty.");
        }
        this.payload = payload;
    }

    /**
     * Get Payload.
     *
     * @return Payload.
     */
    public String getPayLoad() {

        return payload;
    }

    /**
     * Set Refresh Grant Payload.
     *
     * @param refreshGrantPayload Refresh Grant Payload.
     * @throws TokenRequestException TokenRequestException.
     */
    public void setRefreshGrantPayload(String refreshGrantPayload) throws TokenRequestException {

        if (StringUtils.isBlank(refreshGrantPayload)) {
            throw new TokenRequestException("Payload cannot be null or empty.");
        }
        this.refreshGrantPayload = refreshGrantPayload;
    }

    /**
     * Get Refresh Grant Payload.
     *
     * @return Refresh Grant Payload.
     */
    public String getRefreshGrantPayload() {

        return refreshGrantPayload;
    }

    /**
     * Builder class for APIRequestContext.
     */
    public static class Builder {

        private GrantContext grantContext;
        private String endpointUrl;
        private Map<String, String> headers = new HashMap<>();
        private String payload;

        public Builder grantContext(GrantContext grantContext) {

            this.grantContext = grantContext;
            return this;
        }

        public Builder endpointUrl(String endpointUrl) {

            this.endpointUrl = endpointUrl;
            return this;
        }

        public Builder headers(Map<String, String> headers) {

            this.headers = headers;
            return this;
        }

        public Builder payload(String payload) {

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
                throw new TokenRequestException("Grant context cannot be null.");
            }
            if (StringUtils.isBlank(endpointUrl)) {
                throw new TokenRequestException("Endpoint URL cannot be null or empty.");
            }
            return new TokenRequestContext(this);
        }
    }
}

