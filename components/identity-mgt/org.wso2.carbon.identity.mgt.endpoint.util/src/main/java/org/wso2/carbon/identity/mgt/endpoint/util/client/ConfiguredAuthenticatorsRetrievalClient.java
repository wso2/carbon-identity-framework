/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.owasp.encoder.Encode;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Client which retrieves configure authenticators of an application.
 */
public class ConfiguredAuthenticatorsRetrievalClient {

    private static final Log log = LogFactory.getLog(ConfiguredAuthenticatorsRetrievalClient.class);
    private static final String APPLICATION_API_RELATIVE_PATH = "/api/server/v1/applications";
    private static final String AUTHENTICATORS = "/authenticators";
    private static final String CLIENT = "Client ";

    /**
     * Gets the authenticators configured for an application.
     *
     * @param applicationId ID of an application.
     * @return the list of configured Authenticators
     * @throws ConfiguredAuthenticatorsRetrievalClientException if exception occurs when retrieving configured
     * authenticators.
     */
    public JSONArray getConfiguredAuthenticators(String applicationId)
            throws ConfiguredAuthenticatorsRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet request =
                    new HttpGet(getApplicationsEndpoint() + "/" + Encode.forUriComponent(applicationId) +
                            AUTHENTICATORS);
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONArray jsonResponse = new JSONArray(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    return jsonResponse;
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            //JSONException may occur if the application don't have an access URL configured
            String msg = "Error while getting authenticators configured for application Id: " + applicationId;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new ConfiguredAuthenticatorsRetrievalClientException(msg, e);
        }
        return null;
    }

    private String getApplicationsEndpoint() throws ConfiguredAuthenticatorsRetrievalClientException {

        return getEndpoint(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, APPLICATION_API_RELATIVE_PATH);
    }

    private String getEndpoint(String tenantDomain, String context)
            throws ConfiguredAuthenticatorsRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new ConfiguredAuthenticatorsRetrievalClientException("Error while building url for context: " +
                    context);
        }
    }

    private void setAuthorizationHeader(HttpRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
    }
}
