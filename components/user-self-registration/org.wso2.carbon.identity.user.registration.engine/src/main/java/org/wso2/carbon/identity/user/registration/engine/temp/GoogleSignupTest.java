/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.temp;

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_ACTION_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_EXTERNAL_REDIRECTION;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.user.registration.engine.executor.action.AttributeCollection;
import org.wso2.carbon.identity.user.registration.engine.executor.action.Authentication;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.util.Constants;

public class GoogleSignupTest implements Authentication, AttributeCollection {

    private static final String GOOGLE_CODE = "code";
    private static final Log LOG = LogFactory.getLog(GoogleSignupTest.class);

    public String getName() {

        return Constants.GOOGLE_EXECUTOR_NAME;
    }

    @Override
    public ExecutorResponse authenticate(RegistrationContext context) {

        return null;
    }

    @Override
    public ExecutorResponse collect(RegistrationContext context) {

        Map<String, String> userInputs = context.getUserInputData();
        ExecutorResponse response = new ExecutorResponse();

        // Implement the actual task logic here
//        if (STATUS_ATTR_REQUIRED.equals(context.getExecutorStatus())) {
        if (userInputs != null && !userInputs.isEmpty() && userInputs.containsKey(GOOGLE_CODE)) {
            response.setResult(STATUS_ACTION_COMPLETE);
            Map<String, Object> updatedClaims = new HashMap<>();
            updatedClaims = doTokenCall(userInputs.get(GOOGLE_CODE));
            if (updatedClaims != null) {
                response.setUpdatedUserClaims(updatedClaims);
            }
            return response;
        } else {
//        }
//        if (STATUS_NEXT_ACTION_PENDING.equals(context.getExecutorStatus())) {
            response.setResult(STATUS_EXTERNAL_REDIRECTION);
            response.setRequiredData(getIdTokenRequirement());
            response.setAdditionalInfo(getConfigurations(context));
            return response;
        }
//        response.setResult("ERROR");
//        return response;
    }


    @Override
    public List<String> getInitiationData() {

        return null;
    }

    private List<String> getIdTokenRequirement() {

        // Define a new list of InputMetaData and add the data object and return the list.
        List<String> inputMetaData = new ArrayList<>();
        inputMetaData.add(GOOGLE_CODE);
        return inputMetaData;
    }

    private Map<String, String> getConfigurations(RegistrationContext context) {

        Map<String, String> googleProperties = new HashMap<>();
        // Generate a random state using random UUID.
        String state = UUID.randomUUID().toString();
        googleProperties.put("redirectUrl", "https://accounts.google.com/o/oauth2/auth?response_type=code" +
                "&redirect_uri=https://localhost:9443/authenticationendpoint/self-registration.jsp" +
                "&state=" + state +
                "&client_id=" +
                "&scope=openid+email+profile");
        googleProperties.put("state", state);
        return googleProperties;
    }

    private Map<String, Object> doTokenCall(String authorizationCode) {

        String tokenUrl = "https://oauth2.googleapis.com/token";
        String clientId = "";
        String clientSecret = "";
        String redirectUri =
                "https://localhost:9443/authenticationendpoint/self-registration.jsp"; // Must match the one used in
        // OAuth

        try {
            // Prepare the request body
            String requestBody = "code=" + authorizationCode
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&redirect_uri=" + redirectUri
                    + "&grant_type=authorization_code";

            // Open connection to Google's token endpoint
            URL url = new URL(tokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read the response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = conn.getInputStream().toString();
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.has("id_token")) {
                    String idToken = jsonResponse.getString("id_token");

                    // Decode the ID Token
                    String[] tokenParts = idToken.split("\\.");
                    if (tokenParts.length == 3) {
                        String payload =
                                new String(Base64.getUrlDecoder().decode(tokenParts[1]), StandardCharsets.UTF_8);

                        // Extract email, given_name, and family_name
                        JSONObject payloadJson = new JSONObject(payload);
                        String email = payloadJson.optString("email", "Not found");
                        String givenName = payloadJson.optString("given_name", "Not found");
                        String familyName = payloadJson.optString("family_name", "Not found");

                        Map<String, Object> claimSet = new HashMap<>();
                        claimSet.put("http://wso2.org/claims/username", email);
                        claimSet.put("http://wso2.org/claims/givenname", givenName);
                        claimSet.put("http://wso2.org/claims/lastname", familyName);
                        return claimSet;
                    } else {
                        LOG.error("Invalid ID Token format");
                    }
                }
            } else {
                LOG.error("Failed to get token from Google. Response code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}