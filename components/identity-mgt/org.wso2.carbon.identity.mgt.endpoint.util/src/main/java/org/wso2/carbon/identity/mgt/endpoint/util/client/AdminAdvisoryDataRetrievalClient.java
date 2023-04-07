/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Client to interact with the Admin Advisory Management API.
 */
public class AdminAdvisoryDataRetrievalClient {

    private static final String CLIENT = "Client";
    private static final Log LOG = LogFactory.getLog(AdminAdvisoryDataRetrievalClient.class);
    private static final String ADMIN_BANNER_API_RELATIVE_PATH = "/api/server/v1/admin-advisory-management/banner";

    /**
     * Check for admin advisory banner configs in the given tenant.
     *
     * @param tenant Tenant Domain.
     * @return A JSON Object containing admin advisory banner configs.
     * @throws PreferenceRetrievalClientException Error while retrieving the admin advisory banner configs.
     */
    public JSONObject getAdminAdvisoryBannerData(String tenant)
            throws PreferenceRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {

            String uri = getAdminAdvisoryBannerEndpoint(tenant);

            HttpGet request = new HttpGet(uri);
            setAuthorizationHeader(request);

            JSONObject jsonResponse = new JSONObject();

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                }

                return jsonResponse;
            } finally {
                request.releaseConnection();
            }
        } catch (IOException e) {
            String msg = "Error while getting admin advisory banner preference for tenant : " + tenant;

            if (LOG.isDebugEnabled()) {
                LOG.debug(msg, e);
            }

            throw new PreferenceRetrievalClientException(msg, e);
        }
    }

    /**
     * Get the tenant admin advisory banner config endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @return A tenant qualified endpoint.
     * @throws PreferenceRetrievalClientException Error while getting the endpoint.
     */
    private String getAdminAdvisoryBannerEndpoint(String tenantDomain)
            throws PreferenceRetrievalClientException {

        return getEndpoint(tenantDomain, ADMIN_BANNER_API_RELATIVE_PATH);
    }

    /**
     * Resolve the tenant admin advisory banner config endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @param context API context Path.
     * @return A resolved endpoint.
     * @throws PreferenceRetrievalClientException Error while getting the base path.
     */
    private String getEndpoint(String tenantDomain, String context)
            throws PreferenceRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new PreferenceRetrievalClientException("Error while building url for context: " + context);
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
