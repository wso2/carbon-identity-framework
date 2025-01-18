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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.ai.service.mgt.token;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.apache.axis2.transport.http.HTTPConstants.HEADER_CONTENT_TYPE;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.ACCESS_TOKEN_KEY;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.AI_SERVICE_KEY_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.AI_TOKEN_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.AI_TOKEN_CONNECTION_TIMEOUT_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.AI_TOKEN_ENDPOINT_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.AI_TOKEN_SERVICE_MAX_RETRIES_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.AI_TOKEN_SOCKET_TIMEOUT_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.CONTENT_TYPE_FORM_URLENCODED;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_TOKEN_CONNECTION_REQUEST_TIMEOUT;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_TOKEN_CONNECTION_TIMEOUT;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_TOKEN_REQUEST_MAX_RETRIES;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_TOKEN_SOCKET_TIMEOUT;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.ErrorMessages.MAXIMUM_RETRIES_EXCEEDED;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.HTTP_BASIC;
import static org.wso2.carbon.identity.core.util.IdentityTenantUtil.getTenantDomainFromContext;

/**
 * The purpose of this class is to retrieve an active token to access the AI service.
 */
public class AIAccessTokenManager {

    private static volatile AIAccessTokenManager instance;
    private static final Object lock = new Object();  // Lock for synchronization.

    private static final Log LOG = LogFactory.getLog(AIAccessTokenManager.class);

    private static final String AI_KEY = IdentityUtil.getProperty(AI_SERVICE_KEY_PROPERTY_NAME);
    private static final String AI_TOKEN_ENDPOINT = IdentityUtil.getProperty(AI_TOKEN_ENDPOINT_PROPERTY_NAME);

    private final AccessTokenRequestHelper accessTokenRequestHelper;

    private String accessToken;
    private final String clientId;

    private AIAccessTokenManager() {

        byte[] decodedBytes = Base64.getDecoder().decode(AI_KEY);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        String[] parts = decodedString.split(":");
        if (parts.length == 2) {
            this.clientId = parts[0];
        } else {
            throw new IllegalArgumentException("Invalid AI service key.");
        }
        this.accessTokenRequestHelper = new AccessTokenRequestHelper(AI_KEY, AI_TOKEN_ENDPOINT);
    }

    /**
     * Get the singleton instance of the AIAccessTokenManager.
     *
     * @return The singleton instance.
     */
    public static AIAccessTokenManager getInstance() {

        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new AIAccessTokenManager();
                }
            }
        }
        return instance;
    }

    /**
     * Get the access token.
     *
     * @param renewAccessToken Whether to renew the access token.
     * @return The access token.
     * @throws AIServerException If an error occurs while obtaining the access token.
     */
    public String getAccessToken(boolean renewAccessToken) throws AIServerException {

        if (StringUtils.isEmpty(accessToken) || renewAccessToken) {
            synchronized (AIAccessTokenManager.class) {
                if (StringUtils.isEmpty(accessToken) || renewAccessToken) {
                    this.accessToken = accessTokenRequestHelper.requestAccessToken();
                }
            }
        }
        return this.accessToken;
    }

    /**
     * Get the client ID.
     *
     * @return The client ID.
     */
    public String getClientId() {

        return this.clientId;
    }

    /**
     * Helper class to request access token from the AI services.
     */
    private static class AccessTokenRequestHelper {

        private final CloseableHttpClient client;
        private final Gson gson;
        private final String key;
        private final HttpPost tokenRequest;
        private static final int MAX_RETRIES = readIntProperty(AI_TOKEN_SERVICE_MAX_RETRIES_PROPERTY_NAME,
                DEFAULT_TOKEN_REQUEST_MAX_RETRIES);
        private static final int CONNECTION_TIMEOUT = readIntProperty(AI_TOKEN_CONNECTION_TIMEOUT_PROPERTY_NAME,
                DEFAULT_TOKEN_CONNECTION_TIMEOUT);
        private static final int CONNECTION_REQUEST_TIMEOUT = readIntProperty(
                AI_TOKEN_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME, DEFAULT_TOKEN_CONNECTION_REQUEST_TIMEOUT);
        private static final int SOCKET_TIMEOUT = readIntProperty(AI_TOKEN_SOCKET_TIMEOUT_PROPERTY_NAME,
                DEFAULT_TOKEN_SOCKET_TIMEOUT);

        AccessTokenRequestHelper(String key, String tokenEndpoint) {

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(CONNECTION_TIMEOUT)
                    .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .build();
            this.client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig).build();
            this.gson = new GsonBuilder().create();
            this.key = key;
            this.tokenRequest = new HttpPost(tokenEndpoint);
        }

        /**
         * Request access token to access the AI services.
         *
         * @return the JWT access token.
         * @throws AIServerException If an error occurs while requesting the access token.
         */
        public String requestAccessToken() throws AIServerException {

            String tenantDomain = getTenantDomainFromContext();
            LOG.info("Initiating access token request for AI services from tenant: " + tenantDomain);
            for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
                try {
                    tokenRequest.setHeader(AUTHORIZATION, HTTP_BASIC + " " + key);
                    tokenRequest.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED);

                    StringEntity entity = new StringEntity("grant_type=client_credentials&tokenBindingId=" +
                            UUID.randomUUID());
                    entity.setContentType(new BasicHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URLENCODED));
                    tokenRequest.setEntity(entity);

                    HttpResponse response = client.execute(tokenRequest);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);
                        return (String) responseMap.get(ACCESS_TOKEN_KEY);
                    } else {
                        LOG.error("Token request failed with status code: " +
                                response.getStatusLine().getStatusCode());
                    }
                } catch (IOException e) {
                    throw new AIServerException("Error executing token request: " + e.getMessage(), e);
                } finally {
                    tokenRequest.releaseConnection();
                }
            }
            throw new AIServerException("Failed to obtain access token after " + MAX_RETRIES +
                " attempts.", MAXIMUM_RETRIES_EXCEEDED.getCode());
        }

        private static int readIntProperty(String key, int defaultValue) {

            String value = IdentityUtil.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        }
    }
}
