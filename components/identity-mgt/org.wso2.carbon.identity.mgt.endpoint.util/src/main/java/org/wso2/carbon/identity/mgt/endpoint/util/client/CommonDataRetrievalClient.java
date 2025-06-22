/*
 * Copyright (c) 2022-2024, WSO2 LLC. (http://www.wso2.com).
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
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Client which can retrieve data from given endpoint.
 */
public class CommonDataRetrievalClient {

    private static final Log log = LogFactory.getLog(CommonDataRetrievalClient.class);
    private static final String CLIENT = "Client ";

    /**
     * Check the boolean value for given property.
     *
     * @param tenantDomain tenant domain name.
     * @return true if enterprise login is enabled.
     * @throws CommonDataRetrievalClientException
     */
    public boolean checkBooleanProperty(String apiContextPath, String tenantDomain, String propertyName,
                                        boolean defaultValue, boolean isEndpointTenantAware)
            throws CommonDataRetrievalClientException {

        try {

            String uri = getEndpoint(tenantDomain, apiContextPath, isEndpointTenantAware);
            HttpGet get = new HttpGet(uri);
            setAuthorizationHeader(get);

            String responseString = IdentityManagementEndpointUtil.getHttpClientResponseString(get);

            if (!StringUtils.isEmpty(responseString)) {
                JSONObject jsonResponse = new JSONObject(new JSONTokener(responseString));
                if (jsonResponse.has(propertyName)) {
                    return jsonResponse.getBoolean(propertyName);
                }
            }
            return defaultValue;
        } catch (IOException e) {
            // Logging and throwing since this is a client.
            String msg = "Error while checking property in tenant " + tenantDomain + "for api " + apiContextPath;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new CommonDataRetrievalClientException(msg, e);
        }
    }

    /**
     * Resolve the tenant for given endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @param context API context Path.
     * @param isEndpointTenantAware Whether the endpoint is tenant aware.
     * @return A resolved endpoint.
     * @throws CommonDataRetrievalClientException
     */
    private String getEndpoint(String tenantDomain, String context, boolean isEndpointTenantAware)
            throws CommonDataRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context, isEndpointTenantAware);
        } catch (ApiException e) {
            throw new CommonDataRetrievalClientException("Error while building url for context: " + context);
        }
    }

    /**
     * Set the Authorization header for a given HTTP request.
     *
     * @param httpMethod HTTP request method.
     */
    private void setAuthorizationHeader(HttpUriRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
    }
}
