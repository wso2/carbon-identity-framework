/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Client to interact with the Tenant branding preferences API.
 */
public class BrandingPreferenceRetrievalClient {

    private static final String CLIENT = "Client ";
    private static final Log log = LogFactory.getLog(PreferenceRetrievalClient.class);
    private static final String BRANDING_PREFERENCE_API_RELATIVE_PATH = "/api/server/v1/branding-preference";
    private static final String RESOURCE_TYPE_URL_SEARCH_PARAM = "type";
    private static final String RESOURCE_NAME_URL_SEARCH_PARAM = "name";
    private static final String RESOURCE_LOCALE_URL_SEARCH_PARAM = "locale";

    /**
     * Check for branding preference in the given tenant.
     *
     * @param tenant Tenant Domain.
     * @param type Resource Type. ex: ORG, APP, etc.
     * @param name Resource Name. ex: Console, My Account, etc.
     * @param locale ISO language code of the resource. ex: en-US, pt-BR, etc.
     * @return A JSON Object containing branding preferences.
     * @throws BrandingPreferenceRetrievalClientException
     */
    public JSONObject getPreference(String tenant, String type, String name, String locale)
            throws BrandingPreferenceRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {

            String uri = getBrandingPreferenceEndpoint(tenant);

            try {
                URIBuilder uriBuilder = new URIBuilder(uri);

                if (StringUtils.isNotBlank(type)) {
                    uriBuilder.addParameter(RESOURCE_TYPE_URL_SEARCH_PARAM, type);
                }

                if (StringUtils.isNotBlank(name)) {
                    uriBuilder.addParameter(RESOURCE_NAME_URL_SEARCH_PARAM, name);
                }

                if (StringUtils.isNotBlank(locale)) {
                    uriBuilder.addParameter(RESOURCE_LOCALE_URL_SEARCH_PARAM, locale);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Preferences endpoint URI for tenant " + tenant
                            + " was constructed with params - type : "
                            + type + ", name :" + name + ", locale :" + locale);
                }
            } catch (URISyntaxException e) {
                if (log.isDebugEnabled()) {
                    String msg = "Error while building the branding preference endpoint URI with params for : "
                            + tenant + ". Falling back to default endpoint URI: " + uri;
                    log.debug(msg, e);
                }
            }

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
            String msg = "Error while getting branding preference for tenant : " + tenant;

            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }

            throw new BrandingPreferenceRetrievalClientException(msg, e);
        }
    }

    /**
     * Get the tenant branding preferences endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @return A tenant qualified endpoint.
     * @throws BrandingPreferenceRetrievalClientException
     */
    private String getBrandingPreferenceEndpoint(String tenantDomain)
            throws BrandingPreferenceRetrievalClientException {

        return getEndpoint(tenantDomain, BRANDING_PREFERENCE_API_RELATIVE_PATH);
    }

    /**
     * Resolve the tenant branding preferences endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @param context API context Path.
     * @return A resolved endpoint.
     * @throws BrandingPreferenceRetrievalClientException
     */
    private String getEndpoint(String tenantDomain, String context)
            throws BrandingPreferenceRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new BrandingPreferenceRetrievalClientException("Error while building url for context: " + context);
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
