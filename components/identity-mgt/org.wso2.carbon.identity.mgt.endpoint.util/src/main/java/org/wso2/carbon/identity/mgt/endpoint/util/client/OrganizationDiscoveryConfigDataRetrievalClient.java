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

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Client which interacts with the organization discovery configuration API
 * to retrieve organization discovery configuration data.
 */
public class OrganizationDiscoveryConfigDataRetrievalClient {

    private static final String CLIENT = "Client ";
    private static final String ORG_DISCOVERY_CONFIG_ENDPOINT = "/api/server/v1/organization-configs/discovery";
    private static final String PROPERTIES = "properties";
    private static final String VALUE = "value";
    private static final String KEY = "key";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Retrieves organization discovery configuration data for a given organization.
     *
     * @param tenantDomain Tenant domain.
     * @return Organization discovery configuration data.
     * @throws OrganizationDiscoveryConfigDataRetrievalClientException If an error occurs while retrieving organization
     *                                                                 discovery configuration data.
     */
    public Map<String, String> getDiscoveryConfiguration(String tenantDomain)
            throws OrganizationDiscoveryConfigDataRetrievalClientException {

        Map<String, String> organizationDiscoveryConfig = new HashMap<>();

        try {
            HttpGet request = new HttpGet(getOrganizationDiscoveryConfigEndpoint(tenantDomain));
            setAuthorizationHeader(request);

            String responseString = IdentityManagementEndpointUtil.getHttpClientResponseString(request);

            if (!StringUtils.isEmpty(responseString)) {
                JSONObject configObject = new JSONObject(new JSONTokener(responseString));

                if (configObject.has(PROPERTIES) && configObject.get(PROPERTIES) instanceof JSONArray) {
                    JSONArray properties = configObject.getJSONArray(PROPERTIES);
                    for (int i = 0; i < properties.length(); i++) {
                        JSONObject property = properties.getJSONObject(i);
                        organizationDiscoveryConfig.put(property.getString(KEY), property.getString(VALUE));
                    }
                }
            }
            return organizationDiscoveryConfig;
        } catch (IOException e) {
            throw new OrganizationDiscoveryConfigDataRetrievalClientException("Error while retrieving organization " +
                    "discovery configuration for tenant: " + tenantDomain, e);
        }

    }

    private String getOrganizationDiscoveryConfigEndpoint(String tenantDomain)
            throws OrganizationDiscoveryConfigDataRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, ORG_DISCOVERY_CONFIG_ENDPOINT);
        } catch (ApiException e) {
            throw new OrganizationDiscoveryConfigDataRetrievalClientException("Error while building url for context: " +
                    ORG_DISCOVERY_CONFIG_ENDPOINT);
        }
    }

    private void setAuthorizationHeader(HttpUriRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(AUTHORIZATION_HEADER, CLIENT + authHeader);
    }
}
