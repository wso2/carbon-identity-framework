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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.CLIENT_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.ERROR_RETRIEVING_ACCESS_TOKEN;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.ERROR_WHILE_GENERATING_AUTHENTICATION_SEQUENCE;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.SERVER_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN;

/**
 * Implementation of the LoginFlowAIManager interface to communicate with the LoginFlowAI service.
 */
public class LoginFlowAIManagerImpl implements LoginFlowAIManager {

    private static final String LOGINFLOW_AI_ENDPOINT = IdentityUtil.getProperty(
            "AIServices.LoginFlowAI.LoginFlowAIEndpoint");
    private static final String LOGINFLOW_AI_GENERATE_ENDPOINT = "/api/server/v1/applications/loginflow/generate";
    private static final String LOGINFLOW_AI_STATUS_ENDPOINT = "/api/server/v1/applications/loginflow/status";
    private static final String LOGINFLOW_AI_RESULT_ENDPOINT = "/api/server/v1/applications/loginflow/result";

    private static final Log LOG = LogFactory.getLog(LoginFlowAIManagerImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates an authentication sequence using the LoginFlow AI service.
     *
     * @param userQuery               The user query. This is a string that contain the requested authentication
     *                                flow by the user.
     * @param userClaims              The user claims. This is a JSON array that contains the user claims available
     *                                for that organization.
     * @param availableAuthenticators The available authenticators of the organization.
     * @return Operation ID of the generated authentication sequence.
     * @throws LoginFlowAIServerException When an error occurs while connecting to the LoginFlow AI service.
     * @throws LoginFlowAIClientException When an error occurs while generating the authentication sequence.
     */
    @Override
    public String generateAuthenticationSequence(String userQuery, JSONArray userClaims,
                                                 JSONObject availableAuthenticators) throws LoginFlowAIServerException,
            LoginFlowAIClientException {

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_query", userQuery);
        try {
            // Convert JSONArray to List.
            List<Object> userClaimsList = objectMapper.readValue(userClaims.toString(), List.class);
            requestBody.put("user_claims", userClaimsList);

            // Convert JSONObject to Map.
            Map<String, Object> authenticatorsMap = objectMapper.readValue(availableAuthenticators.toString(),
                    Map.class);
            requestBody.put("available_authenticators", authenticatorsMap);
        } catch (JsonSyntaxException | IOException e) {
            throw new LoginFlowAIClientException("Error occurred while parsing the user claims or available " +
                    "authenticators.", e);
        }

        Object response = executeRequest(LOGINFLOW_AI_GENERATE_ENDPOINT, HttpPost.class, requestBody);
        return ((Map<String, String>) response).get("operation_id");
    }

    /**
     * Retrieves the status of the authentication sequence generation operation.
     *
     * @param operationId The operation ID of the authentication sequence generation operation.
     * @return A Json representation of the status' that are completed, pending, or failed.
     * @throws LoginFlowAIServerException When an error occurs while connecting to the LoginFlow AI service.
     * @throws LoginFlowAIClientException When an error occurs while retrieving the authentication sequence
     *                                    generation status.
     */
    @Override
    public Object getAuthenticationSequenceGenerationStatus(String operationId) throws LoginFlowAIServerException,
            LoginFlowAIClientException {

        return executeRequest(LOGINFLOW_AI_STATUS_ENDPOINT + "/" + operationId, HttpGet.class, null);
    }

    /**
     * Retrieves the result of the authentication sequence generation operation.
     *
     * @param operationId The operation ID of the authentication sequence generation operation.
     * @return The result of the authentication sequence generation operation.
     * @throws LoginFlowAIServerException When an error occurs while connecting to the LoginFlow AI service.
     * @throws LoginFlowAIClientException When an error occurs while retrieving the authentication sequence
     *                                    generation result.
     */
    @Override
    public Object getAuthenticationSequenceGenerationResult(String operationId) throws LoginFlowAIServerException,
            LoginFlowAIClientException {

        return executeRequest(LOGINFLOW_AI_RESULT_ENDPOINT + "/" + operationId, HttpGet.class, null);
    }

    private Object executeRequest(String endpoint, Class<? extends HttpUriRequest> requestType, Object requestBody)
            throws LoginFlowAIServerException, LoginFlowAIClientException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            String accessToken = LoginFlowAITokenService.getInstance().getAccessToken(false);
            String orgName = LoginFlowAITokenService.getInstance().getClientId();

            HttpUriRequest request = createRequest(LOGINFLOW_AI_ENDPOINT + "/t/" + orgName + endpoint, requestType,
                    accessToken, requestBody);
            HttpResponseWrapper loginFlowAIServiceResponse = executeRequestWithRetry(client, request);

            int statusCode = loginFlowAIServiceResponse.getStatusCode();
            String responseBody = loginFlowAIServiceResponse.getResponseBody();

            if (statusCode >= 400) {
                handleErrorResponse(statusCode, responseBody, tenantDomain);
            }
            return convertJsonStringToObject(responseBody);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new LoginFlowAIServerException("An error occurred while connecting to the LoginFlow AI Service.",
                    ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE.getCode(), e);
        }
    }

    private HttpUriRequest createRequest(String url, Class<? extends HttpUriRequest> requestType, String accessToken,
                                         Object requestBody)
            throws IOException {

        HttpUriRequest request;
        if (requestType == HttpPost.class) {
            HttpPost post = new HttpPost(url);
            if (requestBody != null) {
                post.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody)));
            }
            request = post;
        } else if (requestType == HttpGet.class) {
            request = new HttpGet(url);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + requestType.getName());
        }

        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/json");
        return request;
    }

    private HttpResponseWrapper executeRequestWithRetry(CloseableHttpAsyncClient client, HttpUriRequest request)
            throws InterruptedException, ExecutionException, IOException, LoginFlowAIServerException {

        HttpResponseWrapper response = HttpClientHelper.executeRequest(client, request);

        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            String newAccessToken = LoginFlowAITokenService.getInstance().getAccessToken(true);
            if (newAccessToken == null) {
                throw new LoginFlowAIServerException("Failed to renew access token.",
                        ERROR_RETRIEVING_ACCESS_TOKEN.getCode());
            }
            request.setHeader("Authorization", "Bearer " + newAccessToken);
            response = HttpClientHelper.executeRequest(client, request);
        }

        return response;
    }

    private void handleErrorResponse(int statusCode, String responseBody, String tenantDomain)
            throws LoginFlowAIServerException, LoginFlowAIClientException {

        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new LoginFlowAIServerException("Failed to access AI service with renewed access token for " +
                    "the tenant domain: " + tenantDomain,
                    UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN.getCode());
        } else if (statusCode >= 400 && statusCode < 500) {
            throw new LoginFlowAIClientException(new HttpResponseWrapper(statusCode, responseBody), String.format(
                    CLIENT_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE.getMessage(), tenantDomain),
                    CLIENT_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE.getCode());
        } else if (statusCode >= 500) {
            throw new LoginFlowAIServerException(new HttpResponseWrapper(statusCode, responseBody),
                   String.format(SERVER_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE.getMessage(), tenantDomain),
                    SERVER_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE.getCode());
        }
    }

    private Object convertJsonStringToObject(String jsonString) throws LoginFlowAIServerException {

        try {
            return objectMapper.readValue(jsonString, Object.class);
        } catch (IOException e) {
            throw new LoginFlowAIServerException("Error occurred while parsing the JSON response from the AI service.",
                    ERROR_WHILE_GENERATING_AUTHENTICATION_SEQUENCE.getCode(), e);
        }
    }

    /**
     * Wrapper class to hold the HTTP response status code and the response body.
     */
    public static class HttpResponseWrapper {
        private final int statusCode;
        private final String responseBody;

        public HttpResponseWrapper(int statusCode, String responseBody) {

            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        public int getStatusCode() {

            return statusCode;
        }

        public String getResponseBody() {

            return responseBody;
        }
    }

    /**
     * Helper class to execute HTTP requests asynchronously.
     */
    public static class HttpClientHelper {
        public static HttpResponseWrapper executeRequest(CloseableHttpAsyncClient client, HttpUriRequest httpRequest)
                throws InterruptedException, ExecutionException, IOException {

            Future<HttpResponse> apiResponse = client.execute(httpRequest, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse response) {

                    LOG.info("API request completed with status code: " + response.getStatusLine().getStatusCode());
                }

                @Override
                public void failed(Exception e) {

                    LOG.error("API request failed: " + e.getMessage(), e);
                }

                @Override
                public void cancelled() {

                    LOG.warn("API request was cancelled");
                }
            });

            HttpResponse httpResponse = apiResponse.get();
            int status = httpResponse.getStatusLine().getStatusCode();
            String response = EntityUtils.toString(httpResponse.getEntity());
            return new HttpResponseWrapper(status, response);
        }
    }
}
