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
import org.apache.commons.collections.CollectionUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client which retrieves IDP data required by endpoints.
 */
public class IdentityProviderDataRetrievalClient {

    private static final Log log = LogFactory.getLog(IdentityProviderDataRetrievalClient.class);
    
    private static final String CLIENT = "Client ";
    private static final String IDP_API_RELATIVE_PATH = "/api/server/v1/identity-providers";
    private static final String IDP_FILTER = "?filter=name+eq+";
    private static final String IDP_KEY = "identityProviders";
    private static final String IMAGE_KEY = "image";
    private static final String SELF_LINK = "self";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String PROPERTIES = "properties";
    private static final String NAME = "name";
    private static final String AUTHENTICATORS = "authenticators";
    private static final String FEDERATED_AUTHENTICATORS = "federatedAuthenticators";

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
     * This function retrieves the given configurations of the given federated identity provider using carbon API.
     * Three HTTP GET calls will be executed as below.
     * 1. <host>/t/carbon.super/api/server/v1/identity-providers?filter=name+eq+Google
     * 2. <host>/t/carbon.super/api/server/v1/identity-providers/6719c5cc-5162-44c7-9190-cb74d500f5fc
     * 3. <host>/t/carbon.super/api/server/v1/identity-providers/6719c5cc-5162-44c7-9190-cb74d500f5fc/
     * federated-authenticators/R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I
     *
     * @param tenant     String. The tenant name of the session i.e. carbon.super.
     * @param idpCode    String. The declared identity provide code i.e. GoogleOIDCAuthenticator.
     * @param idpName    String. The identity provider name setup at carbon console i.e. Google.
     * @param configKeys List<String>. The list of keys of required configurations i.e. ClientId, callbackUrl
     * @return Map<String, String>. Empty String if the configuration is not found, the configuration value otherwise.
     * @throws IdentityProviderDataRetrievalClientException When there is an error with executing the get calls or
     *                                                      processing JSON objects.
     */
    public Map<String, String> getFederatedIdpConfigs(String tenant, String idpCode, String idpName,
                                                      List<String> configKeys)
            throws IdentityProviderDataRetrievalClientException {

        Map<String, String> configMap = new HashMap<>();
        if (StringUtils.isEmpty(tenant) || StringUtils.isEmpty(idpCode) || StringUtils.isEmpty(idpName) ||
                CollectionUtils.isEmpty(configKeys)) {
            return configMap;
        }
        // i.e. /t/carbon.super/api/server/v1/identity-providers?filter=name+eq+Google
        JSONObject idpSummaryResult = executePath(tenant, IDP_API_RELATIVE_PATH + IDP_FILTER +
                Encode.forUriComponent(idpName));
        if (idpSummaryResult == null) {
            return configMap;
        }

        try {
            JSONArray idpSummary = idpSummaryResult.getJSONArray(IDP_KEY);
            if (idpSummary.length() != 1) {
                return configMap;
            }
            JSONObject idpSummaryProperties = (JSONObject) idpSummary.get(0);
            if (idpSummaryProperties == null) {
                return configMap;
            }
            // i.e. /t/carbon.super/api/server/v1/identity-providers/6719c5cc-5162-44c7-9190-cb74d500f5fc
            String idpURLWithId = idpSummaryProperties.getString(SELF_LINK);
            if (StringUtils.isEmpty(idpURLWithId)) {
                return configMap;
            }
            JSONObject idpDetailedResult = executePath(StringUtils.EMPTY, idpURLWithId);
            if (idpDetailedResult == null) {
                return configMap;
            }
            JSONObject federatedAuthObject = idpDetailedResult.getJSONObject(FEDERATED_AUTHENTICATORS);
            if (federatedAuthObject == null) {
                return configMap;
            }
            JSONArray federatedAuthenticators = federatedAuthObject.getJSONArray(AUTHENTICATORS);
            if (federatedAuthenticators == null) {
                return configMap;
            }
            String federatedIDPURLWithID = null;
            for (int i = 0; i < federatedAuthenticators.length(); i++) {
                JSONObject property = (JSONObject) federatedAuthenticators.get(i);
                if (idpCode.equals(property.getString(NAME))) {
                    federatedIDPURLWithID = property.getString(SELF_LINK);
                    break;
                }
            }
            if (StringUtils.isEmpty(federatedIDPURLWithID)) {
                return configMap;
            }

            // i.e. /t/carbon.super/api/server/v1/identity-providers/6719c5cc-5162-44c7-9190-cb74d500f5fc/
            // federated-authenticators/R29vZ2xlT0lEQ0F1dGhlbnRpY2F0b3I
            JSONObject federatedIdpResult = executePath(StringUtils.EMPTY, federatedIDPURLWithID);
            if (federatedIdpResult == null) {
                return configMap;
            }
            JSONArray federatedIdpProperties = federatedIdpResult.getJSONArray(PROPERTIES);
            if (federatedIdpProperties.length() == 0) {
                return configMap;
            }
            for (int i = 0; i < federatedIdpProperties.length(); i++) {
                JSONObject property = (JSONObject) federatedIdpProperties.get(i);
                if (configKeys.contains(property.getString(KEY))) {
                    configMap.putIfAbsent(property.getString(KEY), property.getString(VALUE));
                }
            }
        } catch (JSONException ex) {
            throw new IdentityProviderDataRetrievalClientException(
                    "Error while decoding the JSON object for federated IDP configs", ex);
        }
        return configMap;
    }

    /**
     * This function executes the HTTP GET calls of given path on carbon API and returns the resulted JSON object
     *
     * @param tenant String. The tenant name of the session i.e. carbon.super.
     * @param path   String. The context or to call without host details. i.e.
     *               api/server/v1/identity-providers?filter=name+eq+Google
     *               api/server/v1/identity-providers/6719c5cc-5162-44c7-9190-cb74d500f5fc
     * @return JSONObject. The resulted JSONObject of the GET call
     * @throws IdentityProviderDataRetrievalClientException When there is an error with executing the get calls
     */
    private JSONObject executePath(String tenant, String path) throws IdentityProviderDataRetrievalClientException {

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            String url = getEndpoint(tenant, path);
            HttpGet httpGet = new HttpGet(url);
            setAuthorizationHeader(httpGet);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                }
            } finally {
                httpGet.releaseConnection();
            }
        } catch (IdentityProviderDataRetrievalClientException | IOException e) {
            throw new IdentityProviderDataRetrievalClientException(
                    "Error while executing the path " + path + " in tenant : " + tenant, e);
        }
        return null;
    }

    /**
     * Get the IDP endpoint.
     *
     * @param tenantDomain Tenant Domain.
     * @return A tenant qualified endpoint.
     * @throws IdentityProviderDataRetrievalClientException
     */
    private String getIdPEndpoint(String tenantDomain)
            throws IdentityProviderDataRetrievalClientException {

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
    private String getEndpoint(String tenantDomain, String context)
            throws IdentityProviderDataRetrievalClientException {

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
