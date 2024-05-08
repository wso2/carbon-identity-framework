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
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.owasp.encoder.Encode;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;
import org.wso2.carbon.utils.HTTPClientUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Client which retrieves application data required by endpoints.
 */
public class ApplicationDataRetrievalClient {

    private static final String CLIENT = "Client ";
    private static final Log log = LogFactory.getLog(ApplicationDataRetrievalClient.class);
    private static final String APPLICATION_API_RELATIVE_PATH = "/api/server/v1/applications";
    private static final String APP_FILTER = "?filter=name+eq+";
    private static final String APPLICATIONS_KEY = "applications";
    private static final String APP_NAME = "name";
    private static final String ACCESS_URL_KEY = "accessUrl";
    private static final String APP_ID = "id";

    /**
     * Gets the access url configured for the given application.
     *
     * @param tenant tenant domain of the application.
     * @param applicationName name of the application.
     * @return the access url configured for the given application.
     * @throws ApplicationDataRetrievalClientException if IO exception occurs or access URL is not configured.
     */
    public String getApplicationAccessURL(String tenant, String applicationName)
            throws ApplicationDataRetrievalClientException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {
            HttpGet request =
                    new HttpGet(getApplicationsEndpoint(tenant) + APP_FILTER +
                            Encode.forUriComponent(applicationName));
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    JSONArray applications = jsonResponse.getJSONArray(APPLICATIONS_KEY);
                    if (applications.length() != 1) {
                        return StringUtils.EMPTY;
                    }

                    JSONObject application = (JSONObject) applications.get(0);
                    if (application.has(ACCESS_URL_KEY)) {
                        return application.getString(ACCESS_URL_KEY);
                    }
                    /*
                    If access URL is not stored in the DB but resolved from a listener, need to get the access url by
                    invoking application get by id.
                     */
                    if (application.has(APP_ID)) {
                        return getApplicationAccessURLByAppId(tenant, application.getString(APP_ID));
                    }
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            //JSONException may occur if the application don't have an access URL configured
            String msg = "Error while getting access URL of " + applicationName + " in tenant : " + tenant;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new ApplicationDataRetrievalClientException(msg, e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the access url configured for the given application.
     *
     * @param tenant Tenant domain of the application.
     * @param applicationId UUID of the application.
     * @return The access url configured for the given application
     * @throws ApplicationDataRetrievalClientException If IO exception occurs or access URL is not configured.
     */
    public String getApplicationAccessURLByAppId(String tenant, String applicationId)
            throws ApplicationDataRetrievalClientException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {
            HttpGet request = new HttpGet(getApplicationsEndpoint(tenant) + "/" + applicationId);
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    return jsonResponse.getString(ACCESS_URL_KEY);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            //JSONException may occur if the application don't have an access URL configured
            String msg = "Error while getting access URL of " + applicationId + " in tenant : " + tenant;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new ApplicationDataRetrievalClientException(msg, e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the application name for given UUID.
     *
     * @param tenant tenant domain of the application
     * @param applicationId UUID of the application
     * @return the access url configured for the given application
     * @throws ApplicationDataRetrievalClientException if IO exception occurs or access URL is not configured
     */
    public String getApplicationName(String tenant, String applicationId)
            throws ApplicationDataRetrievalClientException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {
            HttpGet request =
                    new HttpGet(getApplicationsEndpoint(tenant) + "/" + applicationId);
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    return jsonResponse.getString(APP_NAME);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            //JSONException may occur if the application don't have an access URL configured
            String msg = "Error while getting application name for " + applicationId + " in tenant : " + tenant;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new ApplicationDataRetrievalClientException(msg, e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Gets the application ID for the given application.
     *
     * @param tenant          tenant domain of the application
     * @param applicationName name of the application
     * @return the application UUID for the given application
     * @throws ApplicationDataRetrievalClientException if IO exception occurs or access URL is not configured
     */
    public String getApplicationID(String tenant, String applicationName)
            throws ApplicationDataRetrievalClientException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {
            HttpGet request =
                    new HttpGet(getApplicationsEndpoint(tenant) + APP_FILTER +
                            Encode.forUriComponent(applicationName));
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    JSONArray applications = jsonResponse.getJSONArray(APPLICATIONS_KEY);
                    if (applications.length() != 1) {
                        return StringUtils.EMPTY;
                    }

                    JSONObject application = (JSONObject) applications.get(0);
                    return application.getString(APP_ID);
                }
            } finally {
                request.releaseConnection();
            }
        } catch (IOException | JSONException e) {
            //JSONException may occur if the application don't have an access URL configured
            String msg = "Error while getting application ID for " + applicationName + " in tenant : " + tenant;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new ApplicationDataRetrievalClientException(msg, e);
        }
        return StringUtils.EMPTY;
    }


    private String getApplicationsEndpoint(String tenantDomain) throws ApplicationDataRetrievalClientException {

        return getEndpoint(tenantDomain, APPLICATION_API_RELATIVE_PATH);
    }

    private String getEndpoint(String tenantDomain, String context) throws ApplicationDataRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new ApplicationDataRetrievalClientException("Error while building url for context: " + context);
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
