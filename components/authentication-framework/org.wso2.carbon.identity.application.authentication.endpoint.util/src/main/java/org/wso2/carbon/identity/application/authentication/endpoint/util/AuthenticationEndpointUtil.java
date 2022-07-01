/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.wso2.carbon.identity.application.authentication.endpoint.util.bean.UserDTO;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

/**
 * AuthenticationEndpointUtil defines utility methods used across the authenticationendpoint web application.
 */
public class AuthenticationEndpointUtil {

    private static final Log log = LogFactory.getLog(AuthenticationEndpointUtil.class);
    private static final String CUSTOM_PAGE_APP_SPECIFIC_CONFIG_KEY_SEPARATOR = "-";
    private static final String QUERY_STRING_APPENDER = "&";
    private static final String QUERY_STRING_INITIATOR = "?";
    private static final String PADDING_CHAR = "=";
    private static final String UNDERSCORE = "_";

    private AuthenticationEndpointUtil() {
    }

    /**
     * Returns the application specific custom page configuration servlet context parameter key given the service
     * provider name and the relative URL path.
     *
     * @param serviceProviderName name of the service provider configured at IdP
     * @param relativePath        relative URL path
     * @return the possible servlet context parameter key configured for the given application
     */
    public static String getApplicationSpecificCustomPageConfigKey(String serviceProviderName, String relativePath) {
        return serviceProviderName + CUSTOM_PAGE_APP_SPECIFIC_CONFIG_KEY_SEPARATOR + relativePath;
    }

    /**
     * Populate and return the redirect url for the given context parameter configuration value and the given
     * query string. Returns null if the given context param configuration value is null.
     *
     * @param customPageConfigValue configured custom page url value as a servlet context param
     * @param queryString           query string of the incoming request
     * @return redirect url of the custom page configuration
     */
    public static String getCustomPageRedirectUrl(String customPageConfigValue, String queryString) {

        String redirectUrl = customPageConfigValue;
        if (customPageConfigValue != null && queryString != null && !queryString.isEmpty()) {
            if (customPageConfigValue.indexOf(QUERY_STRING_INITIATOR) > 0) {
                redirectUrl = customPageConfigValue + QUERY_STRING_APPENDER + queryString;
            } else {
                redirectUrl = customPageConfigValue + QUERY_STRING_INITIATOR + queryString;
            }
        }
        return redirectUrl;
    }

    /**
     * Cleaning the queryString.
     *
     * @param queryString           query string of the incoming request
     * @return redirect url of the custom page configuration
     */
    public static String cleanErrorMessages(String queryString) {
        StringBuilder cleanedQueryString = new StringBuilder();
        if(queryString != null){
            String[] split = queryString.split("&");
            for (int i = 0; i < split.length ; i++) {
                String query = split[i];
                if(!query.startsWith(Constants.AUTH_FAILURE) && !query.startsWith(Constants.ERROR_CODE)){
                    cleanedQueryString.append(query);
                    cleanedQueryString.append("&");
                }
            }
            if(cleanedQueryString.length()>0 && cleanedQueryString.charAt(cleanedQueryString.length() - 1)=='&'){
               return cleanedQueryString.substring(0,cleanedQueryString.length()-1);
            }
        }
        return cleanedQueryString.toString();
    }


    /**
     * Build user object from complete username
     * @param userName
     * @return
     */
    public static UserDTO getUser(String userName) {

        if (userName == null) {
            return null;
        }

        String userStoreDomain = extractDomainFromName(userName);
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        String userNameWithoutTenantDomainAndUserStoreDomain = MultitenantUtils
                .getTenantAwareUsername(UserCoreUtil.removeDomainFromName(userName));

        UserDTO user = new UserDTO();
        user.setUsername(userNameWithoutTenantDomainAndUserStoreDomain);
        user.setRealm(userStoreDomain);
        user.setTenantDomain(tenantDomain);

        return user;
    }

    /**
     * This method will extract the userstore domain from the username
     * @param nameWithDomain username (ex: Secondary/alex)
     * @return user-store-domain (ex: Secondary) or null if domain is not present in the username
     */
    public static String extractDomainFromName(String nameWithDomain) {
        if (nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
            String domain = nameWithDomain.substring(0, nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR));
            return domain.toUpperCase();
        } else {
            return null;
        }
    }
    /**
     * To get the property value for the given key from the ResourceBundle
     * Retrieve the value of property entry for key, return key if a value is not found for key
     * @param resourceBundle
     * @param key
     * @return
     */
    public static String i18n(ResourceBundle resourceBundle, String key) {
        try {
            return Encode.forHtml((StringUtils.isNotBlank(resourceBundle.getString(key)) ?
                    resourceBundle.getString(key) : key));
        } catch (Exception e) {
            // Intentionally catching Exception and if something goes wrong while finding the value for key, return
            // default, not to break the UI
            return Encode.forHtml(key);
        }
    }

    /**
     * To get the property value for the base64 encoded value of the key from the ResourceBundle
     * Retrieve the value of property entry for where key is obtained after replacing "=" with "_" of base64 encoded
     * value of the given key,
     * return key if a value is not found for above calculated
     * @param resourceBundle
     * @param key
     * @return
     */
    public static String i18nBase64(ResourceBundle resourceBundle, String key) {
        String base64Key = Base64.encode(key.getBytes(StandardCharsets.UTF_8)).replaceAll(PADDING_CHAR, UNDERSCORE);
        try {
            return Encode.forHtml((StringUtils.isNotBlank(resourceBundle.getString(base64Key)) ?
                    resourceBundle.getString(base64Key) : key));
        } catch (Exception e) {
            // Intentionally catching Exception and if something goes wrong while finding the value for key, return
            // default, not to break the UI
            return Encode.forHtml(key);
        }
    }

    /**
     * Read the value for the key from resources.properties. If there are no matching key call i18nBase64(), which
     * was the previous implementation.
     *
     * @param resourceBundle Resource bundle
     * @param key            key
     * @return value of the key
     */
    public static String customi18n(ResourceBundle resourceBundle, String key) {

        try {
            return Encode.forHtml((StringUtils.isNotBlank(resourceBundle.getString(key)) ?
                    resourceBundle.getString(key) : key));
        } catch (Exception e) {
            return i18nBase64(resourceBundle, key);
        }
    }

    /**
     * Retrieve the key mapped to the corresponding error code and sub error code combination.
     *
     * @param errorCode error code.
     * @param subErrorCode sub error code or error message context.
     * @return mapped key for the error code and sub error code combination.
     */
    public static String getErrorCodeToi18nMapping(String errorCode, String subErrorCode) {

        String errorKey = errorCode + "_" + subErrorCode;
        switch (errorKey) {
            case Constants.ErrorToi18nMappingConstants.INVALID_CALLBACK_CALLBACK_NOT_MATCH:
                return Constants.ErrorToi18nMappingConstants.INVALID_CALLBACK_CALLBACK_NOT_MATCH_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.INVALID_CLIENT_APP_NOT_FOUND:
                return Constants.ErrorToi18nMappingConstants.INVALID_CLIENT_APP_NOT_FOUND_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.INVALID_REQUEST_INVALID_REDIRECT_URI:
                return Constants.ErrorToi18nMappingConstants.INVALID_REQUEST_INVALID_REDIRECT_URI_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_AUTHORIZATION_FAILED:
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_CLAIM_REQUEST_MISSING:
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_JIT_PROVISIONING_VERIFY_USERNAME_FAILED:
                return Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_AUTHORIZATION_FAILED_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.MISCONFIGURATION_ERROR_SOMETHING_WENT_WRONG_CONTACT_ADMIN:
                return Constants.ErrorToi18nMappingConstants.MISCONFIGURATION_ERROR_SOMETHING_WENT_WRONG_CONTACT_ADMIN_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_USERNAME_EXISTS:
                return Constants.ErrorToi18nMappingConstants.USERNAME_EXISTS_ERROR_I19N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_USER_STORE_DOMAIN_ERROR:
                return Constants.ErrorToi18nMappingConstants.USER_STORE_DOMAIN_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_ERROR_INVALID_USER_STORE_DOMAIN:
                return Constants.ErrorToi18nMappingConstants.ERROR_INVALID_USER_STORE_DOMAIN_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_USER_STORE_MAN_ERROR:
                return Constants.ErrorToi18nMappingConstants.USER_STORE_MAN_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_INVALID_USER_STORE:
                return Constants.ErrorToi18nMappingConstants.INVALID_USER_STORE_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_USERNAME_EXISTENCE_ERROR:
                return Constants.ErrorToi18nMappingConstants.USERNAME_EXISTENCE_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_CLAIM_MAP_HANDLING_ERROR:
                return Constants.ErrorToi18nMappingConstants.CLAIM_MAP_HANDLING_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_RESIDENT_IDP_NULL_ERROR:
                return Constants.ErrorToi18nMappingConstants.RESIDENT_IDP_NULL_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_CLAIM_MAP_GET_ERROR:
                return Constants.ErrorToi18nMappingConstants.CLAIM_MAP_GET_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_ASSOCIATED_LOCAL_USER_ID_ERROR:
                return Constants.ErrorToi18nMappingConstants.ASSOCIATED_LOCAL_USER_ID_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_IDP_ERROR_FOR_TENANT:
                return Constants.ErrorToi18nMappingConstants.IDP_ERROR_FOR_TENANT_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_REALM_ERROR_FOR_TENANT:
                return Constants.ErrorToi18nMappingConstants.REALM_ERROR_FOR_TENANT_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_CLAIM_ERROR_PASSWORD_PROVISION:
                return Constants.ErrorToi18nMappingConstants.CLAIM_ERROR_PASSWORD_PROVISION_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_USERNAME_FOR_ASSOCIATED_IDP_ERROR:
                return Constants.ErrorToi18nMappingConstants.USERNAME_FOR_ASSOCIATED_IDP_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_SIGNUP_EP_ERROR_FOR_PROVISION:
                return Constants.ErrorToi18nMappingConstants.SIGNUP_EP_ERROR_FOR_PROVISION_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_CONSENT_ADD_FOR_TENANT_ERROR:
                return Constants.ErrorToi18nMappingConstants.CONSENT_ADD_FOR_TENANT_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_SET_IDP_FOR_TENANT_ERROR:
                return Constants.ErrorToi18nMappingConstants.SET_IDP_FOR_TENANT_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_GET_CONSENT_FOR_USER_ERROR:
                return Constants.ErrorToi18nMappingConstants.GET_CONSENT_FOR_USER_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_CONSENT_DISABLED_FOR_SSO_ERROR:
                return Constants.ErrorToi18nMappingConstants.CONSENT_DISABLED_FOR_SSO_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_INPUT_CONSENT_FOR_USER_ERROR:
                return Constants.ErrorToi18nMappingConstants.INPUT_CONSENT_FOR_USER_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_USER_DENIED_CONSENT_ERROR:
                return Constants.ErrorToi18nMappingConstants.USER_DENIED_CONSENT_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_USER_DENIED_MANDATORY_CONSENT_ERROR:
                return Constants.ErrorToi18nMappingConstants.USER_DENIED_MANDATORY_CONSENT_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_CONSENT_PAGE_ERROR:
                return Constants.ErrorToi18nMappingConstants.CONSENT_PAGE_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_APPLICATION_CONFIG_NULL_ERROR:
                return Constants.ErrorToi18nMappingConstants.APPLICATION_CONFIG_NULL_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_REQUEST_CLAIMS_PAGE_ERROR:
                return Constants.ErrorToi18nMappingConstants.REQUEST_CLAIMS_PAGE_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_REQUEST_CLAIMS_PAGE_URI_ERROR:
                return Constants.ErrorToi18nMappingConstants.REQUEST_CLAIMS_PAGE_URI_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_RETRIEVE_CLAIM_ERROR:
                return Constants.ErrorToi18nMappingConstants.RETRIEVE_CLAIM_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_GET_USER_ASSOCIATION_ERROR:
                return Constants.ErrorToi18nMappingConstants.GET_USER_ASSOCIATION_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_UPDATE_LOCAL_USER_CLAIMS_ERROR:
                return Constants.ErrorToi18nMappingConstants.UPDATE_LOCAL_USER_CLAIMS_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_RETRIEVING_REALM_TO_HANDLE_CLAIMS_ERROR:
                return Constants.ErrorToi18nMappingConstants.RETRIEVING_REALM_TO_HANDLE_CLAIMS_ERROR_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.AUTHENTICATION_ATTEMPT_FAILED_POST_AUTH_COOKIE_NOT_FOUND:
                return Constants.ErrorToi18nMappingConstants.POST_AUTH_COOKIE_NOT_FOUND_I18N_KEY;
            case Constants.ErrorToi18nMappingConstants.SUSPICIOUS_AUTHENTICATION_ATTEMPTS_SUSPICIOUS_AUTHENTICATION_ATTEMPTS_DESCRIPTION:
                return Constants.ErrorToi18nMappingConstants.SUSPICIOUS_AUTHENTICATION_ATTEMPTS_SUSPICIOUS_AUTHENTICATION_ATTEMPTS_DESCRIPTION_I18N_KEY;
            default:
                return Constants.ErrorToi18nMappingConstants.INCORRECT_ERROR_MAPPING_KEY;
        }
    }

    /**
     * This method is to validate a URL. This method validate both absolute & relative URLs.
     *
     * @param urlString URL String.
     * @return true if valid URL, false otherwise.
     */
    public static boolean isValidURL(String urlString) {

        if (StringUtils.isBlank(urlString)) {
            String errorMsg = "Invalid URL.";
            if (log.isDebugEnabled()) {
                log.debug(errorMsg);
            }
            return false;
        }

        try {
            if (isURLRelative(urlString)) {
                // Build Absolute URL using the relative url path.
                urlString = buildAbsoluteURL(urlString);
            }
            /*
              Validate URL string using the  java.net.URL class.
              Create a URL object from the URL string representation. Throw MalformedURLException if not a valid URL.
             */
            new URL(urlString);
        } catch (MalformedURLException | URISyntaxException | URLBuilderException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return false;
        }
        return true;
    }

    private static boolean isURLRelative(String uriString) throws URISyntaxException {

        return !new URI(uriString).isAbsolute();
    }

    private static String buildAbsoluteURL(String contextPath) throws URLBuilderException {

        return ServiceURLBuilder.create().addPath(contextPath).build().getAbsolutePublicURL();
    }
}
