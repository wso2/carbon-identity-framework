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
import org.apache.commons.lang.StringUtils;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.utils.HTTPClientUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Client which retrieves preferences.
 */
public class PreferenceRetrievalClient {

    private static final String CLIENT = "Client ";
    private static final String PROPERTIES = "properties";
    private static final Log log = LogFactory.getLog(PreferenceRetrievalClient.class);
    private static final String PREFERENCE_API_RELATIVE_PATH = "/api/server/v1/identity-governance/preferences";
    private static final String GOVERNANCE_API_RELATIVE_PATH = "/api/server/v1/identity-governance";
    private static final String SELF_REGISTRATION_PROPERTY = "SelfRegistration.Enable";
    private static final String USERNAME_RECOVERY_PROPERTY = "Recovery.Notification.Username.Enable";
    private static final String QUESTION_PASSWORD_RECOVERY_PROPERTY = "Recovery.Question.Password.Enable";
    private static final String SELF_SIGN_UP_LOCK_ON_CREATION_PROPERTY = "SelfRegistration.LockOnCreation";
    private static final String MULTI_ATTRIBUTE_LOGIN_PROPERTY = "account.multiattributelogin.handler.enable";
    private static final String CONNECTOR_NAME = "connector-name";
    private static final String SELF_SIGN_UP_CONNECTOR = "self-sign-up";
    private static final String RECOVERY_CONNECTOR = "account-recovery";
    private static final String LITE_USER_CONNECTOR = "lite-user-sign-up";
    private static final String MULTI_ATTRIBUTE_LOGIN_HANDLER = "multiattribute.login.handler";
    private static final String MULTI_ATTRIBUTE_LOGIN_ALLOWED_ATTRIBUTES_PROPERTY =
            "account.multiattributelogin.handler.allowedattributes";
    private static final String PROPERTY_NAME = "name";
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_VALUE = "value";
    private static final String TYPING_DNA_CONNECTOR = "typingdna-config";
    private static final String TYPING_DNA_PROPERTY = "adaptive_authentication.tdna.enable";
    private static final String AUTO_LOGIN_AFTER_SELF_SIGN_UP = "SelfRegistration.AutoLogin.Enable";
    public static final String SEND_CONFIRMATION_ON_CREATION = "SelfRegistration.SendConfirmationOnCreation";
    public static final String SHOW_USERNAME_UNAVAILABILITY = "SelfRegistration.ShowUsernameUnavailability";
    private static final String AUTO_LOGIN_AFTER_PASSWORD_RECOVERY = "Recovery.AutoLogin.Enable";
    private static final String RECOVERY_CALLBACK_REGEX_PROP = "Recovery.CallbackRegex";
    private static final String SELF_REG_CALLBACK_REGEX_PROP = "SelfRegistration.CallbackRegex";
    private static final String LITE_REG_CALLBACK_REGEX_PROP = "LiteRegistration.CallbackRegex";
    private static final String ACCOUNT_MGT_GOVERNANCE = "Account Management";
    private static final String USER_ONBOARDING_GOVERNANCE = "User Onboarding";
    private static final String CONNECTORS = "connectors";

    /**
     * Check self registration is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if self registration enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkSelfRegistration(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, SELF_SIGN_UP_CONNECTOR, SELF_REGISTRATION_PROPERTY);
    }

    /**
     * Check lock on self registration is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if lock on self registration enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkSelfRegistrationLockOnCreation(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, SELF_SIGN_UP_CONNECTOR, SELF_SIGN_UP_LOCK_ON_CREATION_PROPERTY);
    }

    /**
     * Check send confirmation on account creation is enabled or not.
     *
     * @param tenant Tenant domain name.
     * @return returns True if send confirmation on creation is enabled.
     * @throws PreferenceRetrievalClientException If any PreferenceRetrievalClientException occurs.
     */
    public boolean checkSelfRegistrationSendConfirmationOnCreation(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, SELF_SIGN_UP_CONNECTOR, SEND_CONFIRMATION_ON_CREATION);
    }

    /**
     * Check if show username unavailability is enabled.
     *
     * @param tenant Tenant domain name.
     * @return returns True if show username unavailability is enabled.
     * @throws PreferenceRetrievalClientException If any PreferenceRetrievalClientException occurs.
     */
    public boolean checkSelfRegistrationShowUsernameUnavailability(String tenant)
            throws PreferenceRetrievalClientException {

        return checkPreference(tenant, SELF_SIGN_UP_CONNECTOR, SHOW_USERNAME_UNAVAILABILITY);
    }

    /**
     * Check username recovery is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if  username recovery enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkUsernameRecovery(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, RECOVERY_CONNECTOR, USERNAME_RECOVERY_PROPERTY);
    }

    /**
     * Check notification based password recovery is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if  notification based password recovery enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkNotificationBasedPasswordRecovery(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, RECOVERY_CONNECTOR,
                IdPManagementConstants.NOTIFICATION_PASSWORD_ENABLE_PROPERTY);
    }

    /**
     * Check email link based password recovery is enabled or not.
     *
     * @param tenant Tenant domain name.
     * @return Returns true if email link based password recovery enabled.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public boolean checkEmailLinkBasedPasswordRecovery(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, RECOVERY_CONNECTOR,
                IdPManagementConstants.EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY);
    }

    /**
     * Check SMS OTP based password recovery is enabled or not.
     *
     * @param tenant Tenant domain name.
     * @return Returns true if SMS OTP based password recovery enabled.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public boolean checkSMSOTPBasedPasswordRecovery(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, RECOVERY_CONNECTOR, IdPManagementConstants.SMS_OTP_PASSWORD_RECOVERY_PROPERTY);
    }

    /**
     * Check question based password recovery is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if  question based password recovery enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkQuestionBasedPasswordRecovery(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, RECOVERY_CONNECTOR, QUESTION_PASSWORD_RECOVERY_PROPERTY);
    }

    /**
     * Check password recovery is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if password recovery enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkPasswordRecovery(String tenant) throws PreferenceRetrievalClientException {

        List<String> propertyNameList = new ArrayList<String>();
        propertyNameList.add(IdPManagementConstants.NOTIFICATION_PASSWORD_ENABLE_PROPERTY);
        propertyNameList.add(QUESTION_PASSWORD_RECOVERY_PROPERTY);
        return checkMultiplePreference(tenant, RECOVERY_CONNECTOR, propertyNameList);
    }

    /**
     * Check multiple attribute login is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if password multi-attribute login enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkMultiAttributeLogin(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, MULTI_ATTRIBUTE_LOGIN_HANDLER, MULTI_ATTRIBUTE_LOGIN_PROPERTY);
    }

    /**
     * Get the multi attribute login allowed attributes
     *
     * @param tenant tenant domain name.
     * @return set of attributes allowed for multi attribute login.
     * @throws PreferenceRetrievalClientException
     */
    public String checkMultiAttributeLoginProperty(String tenant) throws PreferenceRetrievalClientException {

        Optional<String> optional = getPropertyValue(tenant, ACCOUNT_MGT_GOVERNANCE, MULTI_ATTRIBUTE_LOGIN_HANDLER,
                MULTI_ATTRIBUTE_LOGIN_ALLOWED_ATTRIBUTES_PROPERTY);
        if (optional.isPresent()) {
            return optional.get();
        } 
        return null;
    }

    /**
     * Check typingDNA authentication is enabled or not.
     *
     * @param tenant Tenant domain name.
     * @return returns true if typingDNA enabled.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public boolean checkTypingDNA(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, TYPING_DNA_CONNECTOR, TYPING_DNA_PROPERTY, false);
    }

    /**
     * Check auto login after self sign up is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if auto login after self sign up is enabled .
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkAutoLoginAfterSelfRegistrationEnabled(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, SELF_SIGN_UP_CONNECTOR, AUTO_LOGIN_AFTER_SELF_SIGN_UP);
    }

    /**
     * Check auto login after password recovery is enabled or not.
     *
     * @param tenant tenant domain name.
     * @return returns true if auto login after password recover is enabled .
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkAutoLoginAfterPasswordRecoveryEnabled(String tenant) throws PreferenceRetrievalClientException {

        return checkPreference(tenant, RECOVERY_CONNECTOR, AUTO_LOGIN_AFTER_PASSWORD_RECOVERY);
    }

    /**
     * Check whether the provided callbackURL of the account recovery is valid or not.
     *
     * @param tenant      Tenant domain name.
     * @param callbackURL Callback URL of the account recovery.
     * @return returns true if the URL is valid.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public boolean checkIfRecoveryCallbackURLValid(String tenant, String callbackURL)
            throws PreferenceRetrievalClientException {

        return getPropertyValue(tenant, ACCOUNT_MGT_GOVERNANCE, RECOVERY_CONNECTOR, RECOVERY_CALLBACK_REGEX_PROP)
                .filter(callbackURL::matches).isPresent();
    }

    /**
     * Check whether the provided callbackURL of the self registration is valid or not.
     *
     * @param tenant      Tenant domain name.
     * @param callbackURL Callback URL of the self registration.
     * @return returns true if the URL is valid.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public boolean checkIfSelfRegCallbackURLValid(String tenant, String callbackURL)
            throws PreferenceRetrievalClientException {

        return getPropertyValue(tenant, USER_ONBOARDING_GOVERNANCE, SELF_SIGN_UP_CONNECTOR,
                SELF_REG_CALLBACK_REGEX_PROP).filter(callbackURL::matches).isPresent();
    }

    /**
     * Check whether the provided callbackURL of the lite registration is valid or not.
     *
     * @param tenant      Tenant domain name.
     * @param callbackURL Callback URL of the lite registration.
     * @return returns true if the URL is valid.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public boolean checkIfLiteRegCallbackURLValid(String tenant, String callbackURL)
            throws PreferenceRetrievalClientException {

        return getPropertyValue(tenant, USER_ONBOARDING_GOVERNANCE, LITE_USER_CONNECTOR, LITE_REG_CALLBACK_REGEX_PROP)
                .filter(callbackURL::matches).isPresent();
    }

    /**
     * Check for preference in the given tenant.
     *
     * @param tenant        tenant domain name.
     * @param connectorName name of the connector.
     * @param propertyName  property to check.
     * @return returns true if the property is enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkPreference(String tenant, String connectorName, String propertyName)
            throws PreferenceRetrievalClientException {

        return checkPreference(tenant, connectorName, propertyName, true);
    }

    /**
     * Check for preference in the given tenant.
     *
     * @param tenant           Tenant domain name.
     * @param governanceDomain The governance domain.
     * @param connectorName    Name of the connector.
     * @param propertyName     Property name to check.
     * @return returns value of the property.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public Optional<String> getPropertyValue(String tenant, String governanceDomain, String connectorName,
                                             String propertyName)
            throws PreferenceRetrievalClientException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {
            String endpoint = getUserGovernanceEndpoint(tenant);
            HttpGet get = new HttpGet(endpoint);
            setAuthorizationHeader(get);

            String governanceId = null;
            try (CloseableHttpResponse response = httpclient.execute(get)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONArray jsonResponse = new JSONArray(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    for (int itemIndex = 0, totalObject = jsonResponse.length();
                         itemIndex < totalObject; itemIndex++) {
                        JSONObject config = jsonResponse.getJSONObject(itemIndex);
                        if (StringUtils.equalsIgnoreCase(
                                jsonResponse.getJSONObject(itemIndex).getString(PROPERTY_NAME), governanceDomain)) {
                            governanceId = config.getString(PROPERTY_ID);
                            break;
                        }
                    }
                }
            } finally {
                get.releaseConnection();
            }

            endpoint = endpoint + "/" + governanceId;
            HttpGet getConnectorConfig = new HttpGet(endpoint);
            setAuthorizationHeader(getConnectorConfig);

            try (CloseableHttpResponse response = httpclient.execute(getConnectorConfig)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONObject jsonResponse = new JSONObject(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    JSONArray connectorArray = jsonResponse.getJSONArray(CONNECTORS);
                    for (int itemIndex = 0, totalObject = connectorArray.length();
                         itemIndex < totalObject; itemIndex++) {
                        JSONObject config = connectorArray.getJSONObject(itemIndex);
                        if (StringUtils.equalsIgnoreCase(config.getString(PROPERTY_NAME), connectorName)) {
                            JSONArray responseProperties = config.getJSONArray(PROPERTIES);
                            for (int propIndex = 0, totalProp = responseProperties.length();
                                 propIndex < totalProp; propIndex++) {
                                JSONObject property = responseProperties.getJSONObject(propIndex);
                                if (StringUtils.equalsIgnoreCase(property.getString(PROPERTY_NAME), propertyName)) {
                                    return Optional.ofNullable(property.getString(PROPERTY_VALUE));
                                }
                            }
                        }
                    }
                }
            } finally {
                get.releaseConnection();
            }

        } catch (IOException e) {
            // Logging and throwing since this is a client.
            String msg = "Error while obtaining config values for connector : " + connectorName + " in tenant : "
                    + tenant;
            log.debug(msg, e);
            throw new PreferenceRetrievalClientException(msg, e);
        }
        return Optional.empty();
    }

    /**
     * Check for preference in the given tenant.
     *
     * @param tenant        Tenant domain name.
     * @param connectorName Name of the connector.
     * @param propertyName  Property name to check.
     * @param defaultValue  Default value to be returned if there is any error.
     * @return returns true if the property is enabled.
     * @throws PreferenceRetrievalClientException PreferenceRetrievalClientException.
     */
    public boolean checkPreference(String tenant, String connectorName, String propertyName, boolean defaultValue)
            throws PreferenceRetrievalClientException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {
            JSONArray main = new JSONArray();
            JSONObject preference = new JSONObject();
            preference.put(CONNECTOR_NAME, connectorName);
            JSONArray properties = new JSONArray();
            properties.put(propertyName);
            preference.put(PROPERTIES, properties);
            main.put(preference);
            HttpPost post = new HttpPost(getUserGovernancePreferenceEndpoint(tenant));
            setAuthorizationHeader(post);
            post.setEntity(new StringEntity(main.toString(), ContentType.create(HTTPConstants
                    .MEDIA_TYPE_APPLICATION_JSON, Charset.forName(StandardCharsets.UTF_8.name()))));

            try (CloseableHttpResponse response = httpclient.execute(post)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONArray jsonResponse = new JSONArray(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    JSONObject connector = (JSONObject) jsonResponse.get(0);
                    JSONArray responseProperties = connector.getJSONArray(PROPERTIES);
                    for (int itemIndex = 0, totalObject = responseProperties.length();
                         itemIndex < totalObject; itemIndex++) {
                        JSONObject config = responseProperties.getJSONObject(itemIndex);
                        if (StringUtils.equalsIgnoreCase(
                                responseProperties.getJSONObject(itemIndex).getString(PROPERTY_NAME), propertyName)) {
                            return Boolean.valueOf(config.getString(PROPERTY_VALUE));
                        }
                    }
                }
                return defaultValue;
            } finally {
                post.releaseConnection();
            }
        } catch (IOException e) {
            // Logging and throwing since this is a client.
            String msg = "Error while checking preference for connector : " + connectorName + " in tenant : " + tenant;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new PreferenceRetrievalClientException(msg, e);
        }
    }

    /**
     * Check multiple preferences and returns true if one preference is enabled.
     *
     * @param tenant        tenant domain name.
     * @param connectorName name of the connector.
     * @param propertyNames list of properties to check.
     * @return return true of one property is enabled.
     * @throws PreferenceRetrievalClientException
     */
    public boolean checkMultiplePreference(String tenant, String connectorName, List<String> propertyNames)
            throws PreferenceRetrievalClientException {

        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {
            JSONArray requestBody = new JSONArray();
            JSONObject preference = new JSONObject();
            preference.put(CONNECTOR_NAME, connectorName);
            JSONArray properties = new JSONArray();
            for (String propertyName : propertyNames) {
                properties.put(propertyName);
            }
            preference.put(PROPERTIES, properties);
            requestBody.put(preference);
            HttpPost post = new HttpPost(getUserGovernancePreferenceEndpoint(tenant));
            setAuthorizationHeader(post);
            post.setEntity(new StringEntity(requestBody.toString(), ContentType.create(HTTPConstants
                    .MEDIA_TYPE_APPLICATION_JSON, Charset.forName(StandardCharsets.UTF_8.name()))));

            try (CloseableHttpResponse response = httpclient.execute(post)) {

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    JSONArray jsonResponse = new JSONArray(
                            new JSONTokener(new InputStreamReader(response.getEntity().getContent())));
                    JSONObject connector = (JSONObject) jsonResponse.get(0);
                    JSONArray responseProperties = connector.getJSONArray(PROPERTIES);
                    for (int itemIndex = 0, totalObject = responseProperties.length(); itemIndex < totalObject; itemIndex++) {
                        JSONObject config = responseProperties.getJSONObject(itemIndex);
                        if (Boolean.valueOf(config.getString(PROPERTY_VALUE))) {
                            return true;
                        }
                    }
                    return false;
                }
                return false;
            } finally {
                post.releaseConnection();
            }
        } catch (IOException e) {
            // Logging and throwing since this is a client.
            String msg = "Error while check preference for connector : " + connectorName + " in tenant : " + tenant;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new PreferenceRetrievalClientException(msg, e);
        }
    }

    private String getUserGovernancePreferenceEndpoint(String tenantDomain) throws PreferenceRetrievalClientException {

        return getEndpoint(tenantDomain, PREFERENCE_API_RELATIVE_PATH);
    }

    private String getUserGovernanceEndpoint(String tenantDomain) throws PreferenceRetrievalClientException {

        return getEndpoint(tenantDomain, GOVERNANCE_API_RELATIVE_PATH);
    }

    private String getEndpoint(String tenantDomain, String context) throws PreferenceRetrievalClientException {

        try {
            return IdentityManagementEndpointUtil.getBasePath(tenantDomain, context);
        } catch (ApiException e) {
            throw new PreferenceRetrievalClientException("Error while building url for context: " + context);
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
