/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.endpoint.client.exception.ServiceClientException;
import org.wso2.carbon.identity.application.authentication.endpoint.client.model.AuthenticationErrorResponse;
import org.wso2.carbon.identity.application.authentication.endpoint.client.model.AuthenticationResponse;
import org.wso2.carbon.identity.application.authentication.endpoint.client.model.AuthenticationSuccessResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Service client class to invoke /api/identity/auth/v1.0 API.
 */
public class AuthAPIServiceClient {

    private static final Log log = LogFactory.getLog(AuthAPIServiceClient.class);

    private static final String RESPONSE_PARAM_TOKEN = "token";
    private static final String RESPONSE_PARAM_CODE = "code";
    private static final String RESPONSE_PARAM_MESSAGE = "message";
    private static final String RESPONSE_PARAM_DESCRIPTION = "description";
    private static final String RESPONSE_PARAM_PROPERTIES = "properties";

    private String basePath;
    private CloseableHttpClient httpClient;

    /**
     * Constructor to initialize service client.
     *
     * @param apiBasePath base bath of the API.
     */
    public AuthAPIServiceClient(String apiBasePath) {

        httpClient = HttpClients.createDefault();
        basePath = apiBasePath;
    }

    /**
     * Authenticate with username and password.
     *
     * @param username username
     * @param password password
     * @return AuthenticationSuccessResponse upon a successful authentication response. AuthenticationErrorResponse
     * upon an error.
     * @throws ServiceClientException
     */
    public AuthenticationResponse authenticate(String username, Object password) throws ServiceClientException {

        String endpointURL = basePath + "authenticate";

        AuthenticationResponse authenticationResponse;
        HttpPost httpPostRequest = new HttpPost(endpointURL);
        httpPostRequest.setHeader(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader(username, password));
        httpPostRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (CloseableHttpResponse response = httpClient.execute(httpPostRequest)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String responseString = extractResponse(response);
            if (log.isDebugEnabled()) {
                log.debug("Response: { status: " + response.getStatusLine().getStatusCode() + "\n data: " +
                        responseString + " }");
            }

            JSONObject responseObj = new JSONObject(responseString);
            if (responseObj.has(RESPONSE_PARAM_TOKEN)) {
                authenticationResponse = populateAuthenticationSuccessResponse(responseObj);
            } else {
                authenticationResponse = populateAuthenticationErrorResponse(responseObj);
            }
            authenticationResponse.setStatusCode(statusCode);

        } catch (IOException e) {
            String msg = "Error while invoking " + endpointURL;
            log.error(msg, e);
            throw new ServiceClientException(msg, e);
        }

        return authenticationResponse;
    }

    private String extractResponse(CloseableHttpResponse httpResponse) throws ServiceClientException {

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            String msg = "Error while reading HTTP response from service endpoint: " + basePath;
            log.error(msg, e);
            throw new ServiceClientException(msg, e);
        }

        return response.toString();
    }

    private String buildBasicAuthHeader(String username, Object password) {

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private AuthenticationSuccessResponse populateAuthenticationSuccessResponse(JSONObject responseObj) {

        AuthenticationSuccessResponse authenticationSuccessResponse = new AuthenticationSuccessResponse();
        authenticationSuccessResponse.setToken(responseObj.getString(RESPONSE_PARAM_TOKEN));
        return authenticationSuccessResponse;
    }

    private AuthenticationErrorResponse populateAuthenticationErrorResponse(JSONObject responseObj) {

        AuthenticationErrorResponse authenticationErrorResponse = new AuthenticationErrorResponse();
        authenticationErrorResponse.setCode(responseObj.getString(RESPONSE_PARAM_CODE));
        authenticationErrorResponse.setMessage(responseObj.getString(RESPONSE_PARAM_MESSAGE));
        authenticationErrorResponse.setDescription(responseObj.getString(RESPONSE_PARAM_DESCRIPTION));
        Map<String, String> propertyMap = new HashMap<>();
        if (responseObj.has(RESPONSE_PARAM_PROPERTIES) && !responseObj.isNull(RESPONSE_PARAM_PROPERTIES)) {
            JSONObject propertyObj = responseObj.getJSONObject(RESPONSE_PARAM_PROPERTIES);
            if (propertyObj != null) {
                Iterator<String> keys = propertyObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    propertyMap.put(key, String.valueOf(propertyObj.get(key)));
                }
            }
        }
        authenticationErrorResponse.setProperties(propertyMap);

        return authenticationErrorResponse;
    }
}
