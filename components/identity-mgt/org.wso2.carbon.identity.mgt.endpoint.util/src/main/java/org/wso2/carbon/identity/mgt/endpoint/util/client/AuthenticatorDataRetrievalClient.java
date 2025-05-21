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

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Client which retrieves Authenticator data.
 */
public class AuthenticatorDataRetrievalClient {

    private static final Log LOG = LogFactory.getLog(AuthenticatorDataRetrievalClient.class);

    private static final String CLIENT = "Client ";
    private static final String AUTHENTICATOR_API_RELATIVE_PATH = "/api/server/v1/configs/authenticators";
    private static final String IMAGE_KEY = "image";
    private static final String DISPLAY_NAME_KEY = "displayName";
    private static final String DEFINED_BY_KEY = "definedBy";

    /**
     * Gets the configurations for the given local authenticator.
     *
     * @param tenant                  Tenant domain of the local authenticator.
     * @param authenticatorIdentifier Identifier of the local authenticator.
     * @return The image configured for the given local authenticator.
     * @throws AuthenticatorDataRetrievalClientException If an error occurs while retrieving configurations.
     */
    public Map<String, String> getAuthenticatorConfig(String tenant, String authenticatorIdentifier)
            throws AuthenticatorDataRetrievalClientException {

        Map<String, String> authenticatorConfig = new HashMap<>();
        String authenticatorId = base64URLEncode(authenticatorIdentifier);
        try {
            HttpGet request = new HttpGet(getAuthenticatorEndpoint(tenant) + "/" + authenticatorId);
            setAuthorizationHeader(request);

            String responseString = IdentityManagementEndpointUtil.getHttpClientResponseString(request);

            if (!StringUtils.isEmpty(responseString)) {
                JSONObject jsonResponse = new JSONObject(new JSONTokener(responseString));

                if (jsonResponse.has(IMAGE_KEY)) {
                    // Image is an optional attribute.
                    authenticatorConfig.put(IMAGE_KEY, jsonResponse.getString(IMAGE_KEY));
                }
                authenticatorConfig.put(DISPLAY_NAME_KEY, jsonResponse.getString(DISPLAY_NAME_KEY));
                authenticatorConfig.put(DEFINED_BY_KEY, jsonResponse.getString(DEFINED_BY_KEY));
            }
            return authenticatorConfig;
        } catch (IOException | JSONException e) {
            String msg = "Error while getting configs of " + authenticatorIdentifier + " in tenant : " + tenant;
            LOG.debug(msg, e);
            throw new AuthenticatorDataRetrievalClientException(msg, e);
        }
    }

    public static String base64URLEncode(String value) {

        return java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String getAuthenticatorEndpoint(String tenantDomain)
            throws AuthenticatorDataRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, AUTHENTICATOR_API_RELATIVE_PATH);
        } catch (ApiException e) {
            throw new AuthenticatorDataRetrievalClientException("Error while building url for context: " +
                    AUTHENTICATOR_API_RELATIVE_PATH);
        }
    }

    private void setAuthorizationHeader(HttpUriRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
    }
}
