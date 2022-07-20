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
import org.apache.commons.lang.StringUtils;
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
import org.json.JSONObject;
import org.json.JSONTokener;
import org.owasp.encoder.Encode;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Client which retrieves IDP data required by endpoints.
 */
public class IdentityProviderDataRetrievalClient {

    private static final String CLIENT = "Client ";
    private static final Log log = LogFactory.getLog(IdentityProviderDataRetrievalClient.class);
    private static final String IDP_API_RELATIVE_PATH = "/api/server/v1/identity-providers";
    private static final String IDP_FILTER = "?filter=name+eq+";
    private static final String IDP_KEY = "identityProviders";
    private static final String IMAGE_KEY = "image";

    /**
     * Gets the Image configured for the given IDP.
     *
     * @param tenant          tenant domain of the IDP.
     * @param idpName name of the IDP.
     * @return The Image configured for the given IDP.
     * @throws IdentityProviderDataRetrievalClientException if IO exception occurs or Image is not configured.
     */
    public String getIdPImage(String tenant, String idpName)
            throws IdentityProviderDataRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet request = new HttpGet(getIdPEndpoint(tenant) + IDP_FILTER +
                            Encode.forUriComponent(idpName));
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    JSONArray idps = jsonResponse.getJSONArray(IDP_KEY);

                    if (idps.length() != 1) {
                        return StringUtils.EMPTY;
                    }

                    JSONObject idp = (JSONObject) idps.get(0);

                    return idp.getString(IMAGE_KEY);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            String msg = "Error while getting image of " + idpName + " in tenant : " + tenant;

            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }

            throw new IdentityProviderDataRetrievalClientException(msg, e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * Get the IDP endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @return A tenant qualified endpoint.
     * @throws IdentityProviderDataRetrievalClientException
     */
    private String getIdPEndpoint(String tenantDomain) throws IdentityProviderDataRetrievalClientException {

        return getEndpoint(tenantDomain, IDP_API_RELATIVE_PATH);
    }

    /**
     * Resolve the IDP endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @param context API context Path.
     * @return A resolved endpoint.
     * @throws IdentityProviderDataRetrievalClientException
     */
    private String getEndpoint(String tenantDomain, String context) throws IdentityProviderDataRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new IdentityProviderDataRetrievalClientException("Error while building url for context: " + context);
        }
    }

    /**
     * Set the Authorization header for a given HTTP request.
     *
     * @param httpMethod HTTP request method.
     */
    private void setAuthorizationHeader(HttpRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
    }
}
