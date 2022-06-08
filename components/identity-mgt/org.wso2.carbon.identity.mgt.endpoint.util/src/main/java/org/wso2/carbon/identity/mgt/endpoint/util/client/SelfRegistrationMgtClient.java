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

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.mgt.constants.SelfRegistrationStatusCodes;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Client which invokes consent mgt remote operations.
 */
public class SelfRegistrationMgtClient {

    private static final String CLIENT = "Client ";
    public static final String CODE = "code";
    public static final String STATUS_CODE = "statusCode";
    private static final Log log = LogFactory.getLog(SelfRegistrationMgtClient.class);
    private static final String CONSENT_API_RELATIVE_PATH = "/api/identity/consent-mgt/v1.0";
    private static final String USERNAME_VALIDATE_API_RELATIVE_PATH = "/api/identity/user/v1.0/validate-username";
    private static final String USERSTORE_API_RELATIVE_PATH = "/api/server/v1/userstores";
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

    private String getPurposesEndpoint(String tenantDomain) throws SelfRegistrationMgtClientException {

        return getEndpoint(tenantDomain, CONSENT_API_RELATIVE_PATH + PURPOSES_ENDPOINT_RELATIVE_PATH);
    }

    private String getPurposeCategoriesEndpoint(String tenantDomain) throws SelfRegistrationMgtClientException {

        return getEndpoint(tenantDomain, CONSENT_API_RELATIVE_PATH + PURPOSES_CATEGORIES_ENDPOINT_RELATIVE_PATH);
    }

    private String getUserAPIEndpoint(String tenantDomain) throws SelfRegistrationMgtClientException {

        return getEndpoint(tenantDomain, USERNAME_VALIDATE_API_RELATIVE_PATH);
    }

    private String getUserstoresEndpoint(String tenantDomain) throws SelfRegistrationMgtClientException {

        return getEndpoint(tenantDomain, USERSTORE_API_RELATIVE_PATH);
    }

    private String getEndpoint(String tenantDomain, String context) throws SelfRegistrationMgtClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new SelfRegistrationMgtClientException("Error while building url for context: " + context);
        }
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
     * To check the availability of the userstore.
     *
     * @param userStoreDomain Userstore domain.
     * @param tenantDomain Tenant domain.
     * @return A boolean with the userstore availability.
     * @throws SelfRegistrationMgtClientException Self Registration Management Exception.
     */
    public Boolean isUserstoreAvailable(String userStoreDomain, String tenantDomain)
            throws SelfRegistrationMgtClientException {

        byte[] encoding = Base64.encodeBase64(userStoreDomain.getBytes());
        String userstoreId = new String(encoding, Charset.defaultCharset());

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            HttpGet request = new HttpGet(getUserstoresEndpoint(tenantDomain) + "/" + userstoreId);
            setAuthorizationHeader(request);

            try (CloseableHttpResponse response = httpclient.execute(request)) {
                return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
            } finally {
                request.releaseConnection();
            }
        } catch (IOException e) {
            String msg = "Error while retrieving userstore " + userStoreDomain + " in tenant : " + tenantDomain;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new SelfRegistrationMgtClientException(msg, e);
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
     * @deprecated Use {@link #checkUsernameValidity(User user, boolean skipSignUpCheck)}
     */
    @Deprecated
    public Integer checkUsernameValidity(String username, boolean skipSignUpCheck) throws
            SelfRegistrationMgtClientException {

        User user = new User();
        user.setUsername(username);
        return checkUsernameValidity(user, skipSignUpCheck);
    }

    /**
     * Validates user attributes.
     *
     * @param user User object to validate.
     * @param skipSignUpCheck To specify whether to enable or disable the check whether sign up is enabled for this
     *                        tenant.
     * @return An integer with status code.
     * @throws SelfRegistrationMgtClientException Self Registration Management Exception.
     */
    public Integer checkUsernameValidity(User user, boolean skipSignUpCheck) throws
            SelfRegistrationMgtClientException {

        return checkUserNameValidityInternal(user, skipSignUpCheck).getInt(CODE);
    }

    /**
     * Checks whether a given username is valid or not and return a JSON object with API response.
     *
     * @param user            User.
     * @param skipSignUpCheck To specify whether to enable or disable the check whether sign up is enabled for this
     *                        tenant.
     * @return A JSON object with API response data.
     * @throws SelfRegistrationMgtClientException Self Registration Management Exception.
     */
    public JSONObject checkUsernameValidityStatus(User user, boolean skipSignUpCheck)
            throws SelfRegistrationMgtClientException {

        return checkUserNameValidityInternal(user, skipSignUpCheck);
    }

    private JSONObject checkUserNameValidityInternal(User user, boolean skipSignUpCheck) throws
            SelfRegistrationMgtClientException {

        if (log.isDebugEnabled()) {
            log.debug("Checking username validating for username: " + user.getUsername()
                    + ". SkipSignUpCheck flag is set to " + skipSignUpCheck);
        }

        try (CloseableHttpClient httpclient = HttpClientBuilder.create().useSystemProperties().build()) {
            JSONObject userObject = new JSONObject();
            userObject.put(USERNAME, user.getUsername());

            JSONArray properties = new JSONArray();
            JSONObject property = new JSONObject();
            property.put(
                    IdentityManagementEndpointConstants.KEY, IdentityManagementEndpointConstants.SKIP_SIGN_UP_ENABLE_CHECK);
            property.put(IdentityManagementEndpointConstants.VALUE, skipSignUpCheck);
            properties.put(property);

            if (StringUtils.isNotBlank(user.getTenantDomain())) {
                JSONObject tenantProperty = new JSONObject();
                tenantProperty.put(IdentityManagementEndpointConstants.KEY,
                        IdentityManagementEndpointConstants.TENANT_DOMAIN);
                tenantProperty.put(IdentityManagementEndpointConstants.VALUE, user.getTenantDomain());
                properties.put(tenantProperty);
            }

            userObject.put(PROPERTIES, properties);
            // Get tenant qualified endpoint.
            HttpPost post = new HttpPost(getUserAPIEndpoint(user.getTenantDomain()));
            setAuthorizationHeader(post);

            post.setEntity(new StringEntity(userObject.toString(), ContentType.create(HTTPConstants
                    .MEDIA_TYPE_APPLICATION_JSON, Charset.forName(StandardCharsets.UTF_8.name()))));

            try (CloseableHttpResponse response = httpclient.execute(post)) {

                if (log.isDebugEnabled()) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode() + " when validating username: "
                            + user.getUsername());
                }

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK ||
                        response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    if (log.isDebugEnabled()) {
                        log.debug("Username validation response: " + jsonResponse.toString(2) + " for username: " + user
                                .getUsername());
                    }
                    // Adding "code" attribute since in 200 OK instances, we're getting only statusCode
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && jsonResponse.has(STATUS_CODE)) {
                        if (jsonResponse.has(CODE)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Trying to add code attribute in a success instance but the attribute " +
                                        "already exists with the value: " + jsonResponse.get(CODE));
                            }
                        } else {
                            jsonResponse.put(CODE, jsonResponse.get(STATUS_CODE));
                        }
                    }
                    return jsonResponse;
                } else {
                    // Handle invalid tenant domain error thrown by the TenantContextRewriteValve.
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        JSONObject jsonResponse = new JSONObject(
                                new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                        String content = null;
                        if (jsonResponse.get("message") != null) {
                            content = (String) jsonResponse.get("message");
                        } else if (jsonResponse.get("description") != null) {
                            content = (String) jsonResponse.get("description");
                        }
                        if (StringUtils.isNotBlank(content) && content.contains("invalid tenant domain")) {
                            jsonResponse.put(CODE, SelfRegistrationStatusCodes.ERROR_CODE_INVALID_TENANT);
                            return jsonResponse;
                        }
                    }
                    // Logging and throwing since this is a client
                    if (log.isDebugEnabled()) {
                        log.debug("Unexpected response code found: " + response.getStatusLine().getStatusCode()
                                + " when validating username: " + user.getUsername());
                    }
                    throw new SelfRegistrationMgtClientException("Error while checking username validity for user : "
                            + user.getUsername());
                }

            } finally {
                post.releaseConnection();
            }
        } catch (IOException e) {
            // Logging and throwing since this is a client.
            String msg = "Error while check username validity for user : " + user.getUsername();
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
        return new JSONObject(purposeResponse);
    }

    private boolean isDefaultPurpose(JSONObject purpose) {

        return DEFAULT.equalsIgnoreCase(purpose.getString(PURPOSE));
    }

    private boolean hasPIICategories(JSONObject purpose) {

        JSONArray piiCategories = (JSONArray) purpose.get(PII_CATEGORIES);
        return piiCategories.length() > 0;
    }
}
