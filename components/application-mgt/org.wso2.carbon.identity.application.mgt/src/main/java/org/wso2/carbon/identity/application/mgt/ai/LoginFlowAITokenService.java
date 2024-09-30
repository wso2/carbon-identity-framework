/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.identity.core.util.IdentityTenantUtil.getTenantDomainFromContext;

/**
 * This class is responsible for obtaining the access token for the Login Flow AI service.
 */
public class LoginFlowAITokenService {

    private static final Log log = LogFactory.getLog(LoginFlowAITokenService.class);

    public static final String LOGIN_FLOW_AI_KEY = IdentityUtil.getProperty("AIServices.Key");
    public static final String LOGIN_FLOW_AI_TOKEN_ENDPOINT = IdentityUtil.getProperty("AIServices.TokenEndpoint");

    private static LoginFlowAITokenService instance;
    private AccessTokenRequestHelper accessTokenRequestHelper;

    private String accessToken;
    private String clientId;

    private LoginFlowAITokenService() {
        // Prevent from initialization.
    }

    /**
     * Get the singleton instance of the LoginFlowAITokenService.
     *
     * @return The singleton instance.
     */
    public static LoginFlowAITokenService getInstance() {

        if (instance == null) {
            synchronized (LoginFlowAITokenService.class) {
                if (instance == null) {
                    instance = new LoginFlowAITokenService();
                }
            }
        }
        return instance;
    }

    /**
     * Set the access token request helper.
     *
     * @param helper The access token request helper.
     */
    protected void setAccessTokenRequestHelper(AccessTokenRequestHelper helper) {

        this.accessTokenRequestHelper = helper;
    }

    /**
     * Get the access token.
     *
     * @param renewAccessToken Whether to renew the access token.
     * @return The access token.
     * @throws LoginFlowAIServerException If an error occurs while obtaining the access token.
     */
    public String getAccessToken(boolean renewAccessToken) throws LoginFlowAIServerException {

        if (StringUtils.isEmpty(accessToken) || renewAccessToken) {
            synchronized (LoginFlowAITokenService.class) {
                if (StringUtils.isEmpty(accessToken) || renewAccessToken) {
                    this.accessToken = accessTokenRequestHelper != null ?
                            accessTokenRequestHelper.requestAccessToken() : createDefaultHelper().requestAccessToken();
                }
            }
        }
        return this.accessToken;
    }

    private AccessTokenRequestHelper createDefaultHelper() {

        return new AccessTokenRequestHelper(LOGIN_FLOW_AI_KEY, LOGIN_FLOW_AI_TOKEN_ENDPOINT,
                HttpAsyncClients.createDefault());
    }

    /**
     * Set the client ID.
     *
     * @param clientId The client ID.
     */
    public void setClientId(String clientId) {

        this.clientId = clientId;
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
     * Helper class to request access token from the Login Flow AI service.
     */
    protected static class AccessTokenRequestHelper {

        private final CloseableHttpAsyncClient client;
        private final Gson gson;
        private final String key;
        private final String aiServiceTokenEndpoint;
        private static final int MAX_RETRIES = IdentityUtil.getProperty(
                "AIServices.LoginFlow.TokenRequestMaxRetries") != null ?
                Integer.parseInt(IdentityUtil.getProperty("AIServices.LoginFlow.TokenRequestMaxRetries")) : 3;
        private static final long TIMEOUT = IdentityUtil.getProperty(
                "AIServices.LoginFlow.TokenRequestTimeout") != null ?
                Long.parseLong(IdentityUtil.getProperty("AIServices.LoginFlow.TokenRequestTimeout")) : 3000;

        AccessTokenRequestHelper(String key, String tokenEndpoint, CloseableHttpAsyncClient client) {

            this.client = client;
            this.gson = new GsonBuilder().create();
            this.key = key;
            this.aiServiceTokenEndpoint = tokenEndpoint;
        }

        /**
         * Request access token to access the Login Flow AI service.
         *
         * @return the JWT access token.
         * @throws LoginFlowAIServerException If an error occurs while requesting the access token.
         */
        public String requestAccessToken() throws LoginFlowAIServerException {

            String tenantDomain = getTenantDomainFromContext();
            log.info("Initiating access token request for Login Flow AI service from tenant: " + tenantDomain);
            try {
                client.start();
                for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
                    HttpPost post = new HttpPost(aiServiceTokenEndpoint);
                    post.setHeader("Authorization", "Basic " + key);
                    post.setHeader("Content-Type", "application/x-www-form-urlencoded");

                    StringEntity entity = new StringEntity("grant_type=client_credentials");
                    entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded"));
                    post.setEntity(entity);

                    CountDownLatch latch = new CountDownLatch(1);
                    final String[] accessToken = new String[1];
                    client.execute(post, new FutureCallback<HttpResponse>() {
                        @Override
                        public void completed(HttpResponse response) {

                            try {
                                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                    String responseBody = EntityUtils.toString(response.getEntity());
                                    Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);
                                    accessToken[0] = (String) responseMap.get("access_token");

                                    // Decode the JWT to extract client ID.
                                    String[] jwtParts = accessToken[0].split("\\.");
                                    if (jwtParts.length == 3) {
                                        String payloadJson = new String(Base64.getUrlDecoder().decode(jwtParts[1]),
                                                StandardCharsets.UTF_8);
                                        Map<String, Object> payloadMap = gson.fromJson(payloadJson, Map.class);
                                        String clientId = (String) payloadMap.get("client_id");
                                        LoginFlowAITokenService.getInstance().setClientId(clientId);
                                    }
                                } else {
                                    log.error("Token request failed with status code: " +
                                            response.getStatusLine().getStatusCode());
                                }
                            } catch (IOException e) {
                                log.error("Error parsing token response: " + e.getMessage(), e);
                            } finally {
                                latch.countDown();
                            }
                        }

                        @Override
                        public void failed(Exception e) {

                            log.error("Token request failed: " + e.getMessage(), e);
                            latch.countDown();
                        }

                        @Override
                        public void cancelled() {

                            log.warn("Token request was cancelled");
                            latch.countDown();
                        }
                    });

                    if (latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
                        if (accessToken[0] != null) {
                            return accessToken[0];
                        }
                    } else {
                        log.error("Token request timed out");
                    }
                    // Wait before retrying.
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Retry sleep interrupted: " + e.getMessage(), e);
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new LoginFlowAIServerException("Failed to close HTTP client: " + e.getMessage(), e);
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                    log.error("Failed to close HTTP client: " + e.getMessage(), e);
                }
            }
            // If it reaches this point.
            throw new LoginFlowAIServerException("Failed to obtain access token after " + MAX_RETRIES +
                    " attempts.", "MAXIMUM_RETRIES_EXCEEDED");
        }
    }
}
