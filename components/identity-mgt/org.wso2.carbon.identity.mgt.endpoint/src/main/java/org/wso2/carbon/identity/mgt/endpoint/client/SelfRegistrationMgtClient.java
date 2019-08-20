/*
 *
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.mgt.endpoint.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants.KEY;
import static org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants.SKIP_SIGN_UP_ENABLE_CHECK;
import static org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants.VALUE;

/**
 * Client which invokes consent mgt remote operations.
 */
public class SelfRegistrationMgtClient {

    private static final String CLIENT = "Client ";
    private static final Log log = LogFactory.getLog(SelfRegistrationMgtClient.class);
    private static final String CONSENT_API_RELATIVE_PATH = "/api/identity/consent-mgt/v1.0";
    private static final String USERNAME_VALIDATE_API_RELATIVE_PATH = "/api/identity/user/v1.0/validate-username";
    private static final String PURPOSE_ID = "purposeId";
    private static final String PURPOSES_ENDPOINT_RELATIVE_PATH = "/consents/purposes";
    private static final String PURPOSES_CATEGORIES_ENDPOINT_RELATIVE_PATH = "/consents/purpose-categories";
    private static final String PURPOSES = "purposes";
    private static final String PURPOSE = "purpose";
    private static final String PII_CATEGORIES = "piiCategories";
    private static final String PURPOSE_CATEGORY = "purposeCategory";
    private static final String PURPOSE_CATEGORY_ID = "purposeCategoryId";
    private static final String DEFAULT = "DEFAULT";
    private static final String USERNAME = "username";
    private static final String PROPERTIES = "properties";

    /**
     * Returns a JSON which contains a set of purposes with piiCategories
     *
     * @param tenantDomain Tenant Domain.
     * @return A JSON string which contains purposes.
     * @throws SelfRegistrationMgtClientException SelfRegistrationMgtClientException
     */
    public String getPurposes(String tenantDomain, String group, String groupType) throws
            SelfRegistrationMgtClientException {

        String purposesEndpoint;
        String purposesJsonString = "";

        purposesEndpoint = getPurposesEndpoint(tenantDomain);
        purposesEndpoint = purposesEndpoint + "?group=" + group + "&groupType=" + groupType;
        try {
            String purposesResponse = executeGet(purposesEndpoint);
            JSONArray purposes = new JSONArray(purposesResponse);
            JSONArray purposesResponseArray = new JSONArray();

            for (int purposeIndex = 0; purposeIndex < purposes.length(); purposeIndex++) {
                JSONObject purpose = (JSONObject) purposes.get(purposeIndex);
                if (!isDefaultPurpose(purpose)) {
                    purpose = retrievePurpose(purpose.getInt(PURPOSE_ID), tenantDomain);
                    if (hasPIICategories(purpose)) {
                        purposesResponseArray.put(purpose);
                    }
                }
            }
            if (purposesResponseArray.length() != 0) {
                JSONObject purposesJson = new JSONObject();
                purposesJson.put(PURPOSES, purposesResponseArray);
                purposesJsonString = purposesJson.toString();
            }
            return purposesJsonString;
        } catch (IOException e) {
            throw new SelfRegistrationMgtClientException("Error while retrieving purposes", e);
        }
    }

    public int getDefaultPurposeId(String tenantDomain) throws SelfRegistrationMgtClientException {

        try {
            String purposesCategoriesResponse = executeGet(getPurposeCategoriesEndpoint(tenantDomain));
            JSONArray purposesCategories = new JSONArray(purposesCategoriesResponse);
            for (int purpseCatIndex = 0; purpseCatIndex < purposesCategories.length(); purpseCatIndex++) {
                JSONObject purposeCategory = (JSONObject) purposesCategories.get(purpseCatIndex);
                if (DEFAULT.equals(purposeCategory.getString(PURPOSE_CATEGORY))) {
                    return purposeCategory.getInt(PURPOSE_CATEGORY_ID);
                }
            }
        } catch (IOException e) {
            throw new SelfRegistrationMgtClientException("Error while retrieving default purpose for tenant: " +
                    tenantDomain, e);
        }
        throw new SelfRegistrationMgtClientException("Couldn't find default purpose for tenant: " + tenantDomain);
    }

    private String getPurposesEndpoint(String tenantDomain) {

        String purposesEndpoint;
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            purposesEndpoint = IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantDomain +
                    CONSENT_API_RELATIVE_PATH + PURPOSES_ENDPOINT_RELATIVE_PATH);
        } else {
            purposesEndpoint = IdentityManagementEndpointUtil.buildEndpointUrl(CONSENT_API_RELATIVE_PATH +
                    PURPOSES_ENDPOINT_RELATIVE_PATH);
        }
        return purposesEndpoint;
    }

    private String getPurposeCategoriesEndpoint(String tenantDomain) {

        String purposesEndpoint;
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            purposesEndpoint = IdentityManagementEndpointUtil.buildEndpointUrl("t/" + tenantDomain +
                    CONSENT_API_RELATIVE_PATH + PURPOSES_CATEGORIES_ENDPOINT_RELATIVE_PATH);
        } else {
            purposesEndpoint = IdentityManagementEndpointUtil.buildEndpointUrl(CONSENT_API_RELATIVE_PATH +
                    PURPOSES_CATEGORIES_ENDPOINT_RELATIVE_PATH);
        }
        return purposesEndpoint;
    }

    private String getUserAPIEndpoint() {

        String purposesEndpoint = IdentityManagementEndpointUtil.buildEndpointUrl(USERNAME_VALIDATE_API_RELATIVE_PATH);
        return purposesEndpoint;
    }

    private String executeGet(String url) throws SelfRegistrationMgtClientException, IOException {

        boolean isDebugEnabled = log.isDebugEnabled();
        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {

            HttpGet httpGet = new HttpGet(url);
            setAuthorizationHeader(httpGet);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                if (isDebugEnabled) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode() + " when invoking GET for URL: "
                            + url);
                }
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String inputLine;
                    StringBuilder responseString = new StringBuilder();

                    while ((inputLine = reader.readLine()) != null) {
                        responseString.append(inputLine);
                    }
                    return responseString.toString();
                } else {
                    throw new SelfRegistrationMgtClientException("Error while retrieving data from " + url + ". " +
                            "Found http status " + response.getStatusLine());
                }
            } finally {
                httpGet.releaseConnection();
            }
        }
    }

    /**
     * To check the validity of the user name.
     *
     * @param username Name of the user.
     * @return the status code of user name validity check.
     * @throws SelfRegistrationMgtClientException SelfRegistrationMgtClientException will be thrown.
     */
    public Integer checkUsernameValidity(String username) throws SelfRegistrationMgtClientException {
        return checkUsernameValidity(username, false);
    }

    /**
     * Checks whether a given username is valid or not.
     *
     * @param username Username.
     * @param skipSignUpCheck To specify whether to enable or disable the check whether sign up is enabled for this
     *                        tenant.
     * @return An integer with status code.
     * @throws SelfRegistrationMgtClientException Self Registration Management Exception.
     */
    public Integer checkUsernameValidity(String username, boolean skipSignUpCheck) throws
            SelfRegistrationMgtClientException {

        if (log.isDebugEnabled()) {
            log.debug("Checking username validating for username: " + username + ". SkipSignUpCheck flag is set to "
                    + skipSignUpCheck + ".");
        }

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            JSONObject user = new JSONObject();
            user.put(USERNAME, username);

            JSONArray properties = new JSONArray();
            JSONObject property = new JSONObject();
            property.put(KEY, SKIP_SIGN_UP_ENABLE_CHECK);
            property.put(VALUE, skipSignUpCheck);
            properties.put(property);
            user.put(PROPERTIES, properties);

            HttpPost post = new HttpPost(getUserAPIEndpoint());
            setAuthorizationHeader(post);

            post.setEntity(new StringEntity(user.toString(), ContentType.create(HTTPConstants
                    .MEDIA_TYPE_APPLICATION_JSON, Charset.forName(StandardCharsets.UTF_8.name()))));

            try (CloseableHttpResponse response = httpclient.execute(post)) {

                if (log.isDebugEnabled()) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode() + " when validating username: "
                            + username);
                }

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    if (log.isDebugEnabled()) {
                        log.debug("Username validation response: " + jsonResponse.toString(2) + " for username: "
                                + username);
                    }
                    return jsonResponse.getInt("statusCode");
                } else {
                    // Logging and throwing since this is a client
                    if (log.isDebugEnabled()) {
                        log.debug("Unexpected response code found: " + response.getStatusLine().getStatusCode()
                                + " when validating username: " + username);
                    }
                    throw new SelfRegistrationMgtClientException("Error while checking username validity for user : "
                            + username);
                }

            } finally {
                post.releaseConnection();
            }
        } catch (IOException e) {
            // Logging and throwing since this is a client.
            String msg = "Error while check username validity for user : " + username;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new SelfRegistrationMgtClientException(msg, e);
        }
    }

    /**
     * adding OAuth authorization headers to a httpMethod
     *
     * @param httpMethod method which wants to add Authorization header
     */
    private void setAuthorizationHeader(HttpRequestBase httpMethod) {

        String toEncode = IdentityManagementServiceUtil.getInstance().getAppName() + ":"
                + String.valueOf(IdentityManagementServiceUtil.getInstance().getAppPassword());
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION,
                CLIENT + authHeader);

    }

    private JSONObject retrievePurpose(int purposeId, String tenantDomain) throws SelfRegistrationMgtClientException,
            IOException {

        String purposeResponse = executeGet(getPurposesEndpoint(tenantDomain) + "/" + purposeId);
        JSONObject purpose = new JSONObject(purposeResponse);
        return purpose;
    }

    private boolean isDefaultPurpose(JSONObject purpose) {

        if (DEFAULT.equalsIgnoreCase(purpose.getString(PURPOSE))) {
            return true;
        }
        return false;
    }

    private boolean hasPIICategories(JSONObject purpose) {

        JSONArray piiCategories = (JSONArray) purpose.get(PII_CATEGORIES);
        return piiCategories.length() > 0;
    }
}
