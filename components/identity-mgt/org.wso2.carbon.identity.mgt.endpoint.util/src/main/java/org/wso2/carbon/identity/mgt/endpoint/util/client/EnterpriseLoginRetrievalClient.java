/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.http.client.methods.HttpPost;
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
 * Client to check whether enterprise login is enabled or not.
 */
public class EnterpriseLoginRetrievalClient {

    private static final Log log = LogFactory.getLog(EnterpriseLoginRetrievalClient.class);
    private static final String CLIENT = "Client ";
    private static final String SUPER_TENANT = "carbon.super";
    private static final String ENTERPRISE_LOGIN_KEY = "enterpriseLoginEnabled";
    private static final String ENTERPRISE_API_RELATIVE_PATH = "api/asgardeo-enterprise-login/v1/org/business-login/";

    /**
     * Check enterprise login is enabled or not.
     *
     * @param tenantDomain tenant domain name.
     * @return true if enterprise login is enabled.
     * @throws EnterpriseLoginRetrievalClientException if there are errors in checking enterprise login of a tenant.
     */
    public boolean isEnterpriseLoginEnabled(String tenantDomain) throws EnterpriseLoginRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet get = new HttpGet(getEnterpriseLoginManagementEndpoint(tenantDomain));
            setAuthorizationHeader(get);

            try (CloseableHttpResponse response = httpclient.execute(get)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    if (jsonResponse.has(ENTERPRISE_LOGIN_KEY)) {
                        return Boolean.valueOf(jsonResponse.getString(ENTERPRISE_LOGIN_KEY));
                    }
                }
                return false;
            } finally {
                get.releaseConnection();
            }
        } catch (IOException e) {
            // Logging and throwing since this is a client.
            String msg = "Error while checking enterprise login for tenant : " + tenantDomain;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new EnterpriseLoginRetrievalClientException(msg, e);
        }
    }
    private String getEnterpriseLoginManagementEndpoint(String tenantDomain)
                throws EnterpriseLoginRetrievalClientException {

        return getEndpoint(SUPER_TENANT, ENTERPRISE_API_RELATIVE_PATH + tenantDomain);
    }

    private String getEndpoint(String tenantDomain, String context) throws EnterpriseLoginRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new EnterpriseLoginRetrievalClientException("Error while building url for context: " + context);
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
