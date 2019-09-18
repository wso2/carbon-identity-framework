/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.encoder.Encode;
import org.wso2.carbon.identity.mgt.endpoint.client.model.Claim;
import org.wso2.carbon.identity.mgt.endpoint.client.model.Error;
import org.wso2.carbon.identity.mgt.endpoint.client.model.RetryError;
import org.wso2.carbon.identity.mgt.endpoint.client.model.User;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

/**
 * This class defines utility methods used within this web application.
 */
public class IdentityManagementEndpointUtil {

    public static final String PADDING_CHAR = "=";
    public static final String UNDERSCORE = "_";
    public static final String SPLITTING_CHAR = "&";
    public static final String PII_CATEGORIES = "piiCategories";
    public static final String PII_CATEGORY = "piiCategory";
    public static final String PURPOSES = "purposes";
    public static final String MANDATORY = "mandatory";
    public static final String DISPLAY_NAME = "displayName";

    private static final Log log = LogFactory.getLog(IdentityManagementEndpointUtil.class);
    private static final String CODE = "51007";
    private static final String UNEXPECTED_ERROR = "Unexpected Error.";

    private IdentityManagementEndpointUtil() {

    }

    /**
     * Reruns the full qualified username of the user in below format.
     * <user_store_domain>/<username>@<tenant_domain>
     *
     * @param username        username of the user
     * @param tenantDomain    tenant domain the user belongs to
     * @param userStoreDomain user store domain usee belongs to
     * @return full qualified username
     */
    public static final String getFullQualifiedUsername(String username, String tenantDomain, String userStoreDomain) {

        String fullQualifiedUsername = username;
        if (StringUtils.isNotBlank(userStoreDomain) && !IdentityManagementEndpointConstants.PRIMARY_USER_STORE_DOMAIN
                .equals(userStoreDomain)) {
            fullQualifiedUsername = userStoreDomain + IdentityManagementEndpointConstants.USER_STORE_DOMAIN_SEPARATOR
                    + fullQualifiedUsername;
        }

        if (StringUtils.isNotBlank(tenantDomain) && !IdentityManagementEndpointConstants.SUPER_TENANT.equals
                (tenantDomain)) {
            fullQualifiedUsername = fullQualifiedUsername + IdentityManagementEndpointConstants
                    .TENANT_DOMAIN_SEPARATOR + tenantDomain;
        }

        return fullQualifiedUsername;
    }

    /**
     * Returns the error to be viewed for end user.
     *
     * @param errorMsgSummary  required error message to be viewed
     * @param optionalErrorMsg optional content to be viewed
     * @param verificationBean info recovery confirmation bean
     * @return error message to be viewed
     */
    public static String getPrintableError(String errorMsgSummary, String optionalErrorMsg, VerificationBean
            verificationBean) {

        StringBuilder errorMsg = new StringBuilder(errorMsgSummary);

        if (verificationBean != null && StringUtils.isNotBlank(verificationBean.getError())) {
            String[] error = verificationBean.getError().split(" ", 2);
            errorMsg.append(" ").append(error[1]);
        } else if (StringUtils.isNotBlank(optionalErrorMsg)) {
            errorMsg.append(" ").append(optionalErrorMsg);
        }

        return errorMsg.toString();
    }

    /**
     * Returns the end user portal url.
     *
     * @param userPortalUrl configured user portal url
     * @return configured url or the default url if configured url is empty
     */
    public static final String getUserPortalUrl(String userPortalUrl) {

        if (StringUtils.isNotBlank(userPortalUrl)) {
            return userPortalUrl;
        }
        return IdentityManagementEndpointConstants.DEFAULT_USER_PORTAL_URL;
    }

    /**
     * Cast the provided Object to a Boolean
     *
     * @param value Object
     * @return Boolean
     */
    public static boolean getBooleanValue(Object value) {

        if (value != null && value instanceof Boolean) {
            return (Boolean) value;
        }

        return false;
    }

    /**
     * Cast the provided Object to a String
     *
     * @param value Object
     * @return String
     */
    public static String getStringValue(Object value) {

        if (value != null && value instanceof String) {
            return (String) value;
        }

        return "";
    }

    /**
     * Cast provided Object to an Integer
     *
     * @param value Object
     * @return Integer
     */
    public static int getIntValue(Object value) {

        if (value != null && value instanceof Integer) {
            return (Integer) value;
        }

        return 0;
    }

    /**
     * Cast provided Object to a String[]
     *
     * @param value Object
     * @return String[]
     */
    public static String[] getStringArray(Object value) {

        if (value != null && value instanceof String[]) {
            return (String[]) value;
        }

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public static <T> T create(String baseAddress, Class<T> cls, List<?> providers, String configLocation,
                               Map<String, String> headers) {

        JAXRSClientFactoryBean bean = getBean(baseAddress, cls, configLocation, headers);
        bean.setProviders(providers);
        return bean.create(cls, new Object[0]);
    }

    private static JAXRSClientFactoryBean getBean(String baseAddress, Class<?> cls, String configLocation,
                                                  Map<String, String> headers) {

        JAXRSClientFactoryBean bean = getBean(baseAddress, configLocation, headers);
        bean.setServiceClass(cls);
        return bean;
    }

    static JAXRSClientFactoryBean getBean(String baseAddress, String configLocation, Map<String, String> headers) {

        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        if (configLocation != null) {
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus(configLocation);
            bean.setBus(bus);
        }
        bean.setAddress(baseAddress);
        if (headers != null && !headers.isEmpty()) {
            bean.setHeaders(headers);
        }
        return bean;
    }

    public static void addReCaptchaHeaders(HttpServletRequest request, Map<String, List<String>> headers) {

        if (headers != null && headers.get("reCaptcha") != null) {
            request.setAttribute("reCaptcha", Boolean.TRUE.toString());
            request.setAttribute("reCaptchaAPI", headers.get("reCaptchaAPI").get(0));
            request.setAttribute("reCaptchaKey", headers.get("reCaptchaKey").get(0));
        }
    }

    /**
     * Builds consent string according to consent API. This string can be used as the body of add receipt API
     *
     * @param username               Username of the user.
     * @param consent                Consent String which contains services.
     * @param jurisdiction           Jurisdiction.
     * @param collectionMethod       Collection Method.
     * @param language               Language.
     * @param policyURL              Policy URL.
     * @param consentType            Consent Type.
     * @param isPrimaryPurpose       Whether this this receipt is for primary purpose.
     * @param isThirdPartyDisclosure Whether this receipt can be disclosed to thrid parties.
     * @param termination            Termination date.
     * @return Consent string which contains above facts.
     */
    public static String buildConsentForResidentIDP(String username, String consent, String jurisdiction,
                                                    String collectionMethod, String language, String policyURL,
                                                    String consentType, boolean isPrimaryPurpose, boolean
                                                            isThirdPartyDisclosure, String termination) {

        if (StringUtils.isEmpty(consent)) {
            if (log.isDebugEnabled()) {
                log.debug("Empty consent string. Hence returning without building consent from endpoint");
            }
            return consent;
        }
        String piiPrincipalId = getPiiPrincipalID(username);
        JSONObject receipt = new JSONObject(consent);
        receipt.put(IdentityManagementEndpointConstants.Consent.JURISDICTION_KEY, jurisdiction);
        receipt.put(IdentityManagementEndpointConstants.Consent.COLLECTION_METHOD_KEY, collectionMethod);
        receipt.put(IdentityManagementEndpointConstants.Consent.LANGUAGE_KEY, language);
        receipt.put(IdentityManagementEndpointConstants.Consent.PII_PRINCIPAL_ID_KEY, piiPrincipalId);
        receipt.put(IdentityManagementEndpointConstants.Consent.POLICY_URL_KEY, policyURL);
        buildServices(receipt, consentType, isPrimaryPurpose,
                isThirdPartyDisclosure, termination);
        if (log.isDebugEnabled()) {
            log.debug("Built consent from endpoint util : " + consent);
        }

        return receipt.toString();
    }

    private static String getPiiPrincipalID(String username) {

        User user = IdentityManagementServiceUtil.getInstance().getUser(username);
        String piiPrincipalId;

        if (StringUtils.isNotBlank(user.getRealm()) && !IdentityManagementEndpointConstants.PRIMARY_USER_STORE_DOMAIN
                .equals(user.getRealm())) {
            piiPrincipalId = user.getRealm() + IdentityManagementEndpointConstants.USER_STORE_DOMAIN_SEPARATOR
                    + user.getUsername();
        } else {
            piiPrincipalId = user.getUsername();
        }
        return piiPrincipalId;
    }

    private static void buildServices(JSONObject receipt, String consentType, boolean isPrimaryPurpose, boolean
            isThirdPartyDisclosure, String termination) {

        JSONArray services = (JSONArray) receipt.get(IdentityManagementEndpointConstants.Consent.SERVICES);
        for (int serviceIndex = 0; serviceIndex < services.length(); serviceIndex++) {
            buildService((JSONObject) services.get(serviceIndex), consentType, isPrimaryPurpose,
                    isThirdPartyDisclosure, termination);
        }
    }

    private static void buildService(JSONObject service, String consentType, boolean isPrimaryPurpose, boolean
            isThirdPartyDisclosure, String termination) {

        JSONArray purposes = (JSONArray) service.get(IdentityManagementEndpointConstants.Consent.PURPOSES);

        for (int purposeIndex = 0; purposeIndex < purposes.length(); purposeIndex++) {
            buildPurpose((JSONObject) purposes.get(purposeIndex), consentType, isPrimaryPurpose,
                    isThirdPartyDisclosure, termination);
        }
    }

    private static void buildPurpose(JSONObject purpose, String consentType, boolean isPrimaryPurpose, boolean
            isThirdPartyDisclosure, String termination) {

        purpose.put(IdentityManagementEndpointConstants.Consent.CONSENT_TYPE_KEY, consentType);
        purpose.put(IdentityManagementEndpointConstants.Consent.PRIMARY_PURPOSE_KEY, isPrimaryPurpose);
        purpose.put(IdentityManagementEndpointConstants.Consent.THRID_PARTY_DISCLOSURE_KEY, isThirdPartyDisclosure);
        purpose.put(IdentityManagementEndpointConstants.Consent.TERMINATION_KEY, termination);
        JSONArray piiCategories = (JSONArray) purpose.get(IdentityManagementEndpointConstants.Consent.PII_CATEGORY);
        for (int categoryIndex = 0; categoryIndex < piiCategories.length(); categoryIndex++) {
            buildCategory((JSONObject) piiCategories.get(categoryIndex), termination);
        }
    }

    private static void buildCategory(JSONObject piiCategory, String validity) {

        piiCategory.put(IdentityManagementEndpointConstants.Consent.VALIDITY_KEY, validity);
    }

    /**
     * To get the property value for the given key from the ResourceBundle
     * Retrieve the value of property entry for key, return key if a value is not found for key
     *
     * @param resourceBundle name of the resourcebundle object
     * @param key            name of the key
     * @return property value entry of the key or key value itself
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
     *
     * @param resourceBundle name of the resourcebundle object
     * @param key            name of the key
     * @return property value entry of the base64 encoded key value or key value itself
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
     * Get unique PIIs out of given purposes.
     *
     * @param purposesResponseString Purposes response JSON received from Consent Mgt API.
     * @return Unique PIIs out of given purposes in the purposes JSON String.
     */
    public static Map<String, Claim> getUniquePIIs(String purposesResponseString) {

        Map<String, Claim> claimsMap = new HashMap<>();
        JSONObject purposes = new JSONObject(purposesResponseString);
        JSONArray purposesArray = purposes.getJSONArray(PURPOSES);
        for (int i = 0; i < purposesArray.length(); i++) {
            JSONObject purpose = purposesArray.getJSONObject(i);
            JSONArray piis = (JSONArray) purpose.get(PII_CATEGORIES);
            for (int j = 0; j < piis.length(); j++) {
                JSONObject pii = piis.getJSONObject(j);
                if (claimsMap.get(pii.getString(PII_CATEGORY)) == null) {
                    Claim claim = new Claim();
                    claim.displayName(getStringValue(pii.get(DISPLAY_NAME)));
                    claim.setUri(getStringValue(pii.get(PII_CATEGORY)));
                    claim.required(pii.getBoolean(MANDATORY));
                    claimsMap.put(getStringValue(pii.get(PII_CATEGORY)), claim);
                } else {
                    Claim claim = claimsMap.get(pii.getString(PII_CATEGORY));
                    if (pii.getBoolean(MANDATORY)) {
                        claim.required(true);
                    }
                }
            }
        }
        return claimsMap;
    }

    public static Map<String, Claim> fillPiisWithClaimInfo(Map<String, Claim> piis, List<Claim> defaultClaims) {

        if (piis != null) {
            for (Claim defaultClaim : defaultClaims) {
                if (defaultClaim != null && defaultClaim.getUri() != null && piis.get(defaultClaim.getUri()) != null) {
                    piis.get(defaultClaim.getUri()).setValidationRegex(defaultClaim.getValidationRegex());
                    piis.get(defaultClaim.getUri()).setReadOnly(defaultClaim.getReadOnly());
                }
            }
        } else {
            piis = new HashMap<>();
            for (Claim defaultClaim : defaultClaims) {
                if (defaultClaim != null && defaultClaim.getUri() != null) {
                    piis.put(defaultClaim.getUri(), defaultClaim);
                }
            }
        }
        return piis;
    }

    /**
     * Encode query params of the call back url. Method supports all URL formats supported in
     * {@link #getURLEncodedCallback(String)} and URLs containing spaces
     *
     * @param callbackUrl callback url from the request.
     * @return encoded callback url.
     * @throws MalformedURLException Malformed URL Exception.
     */
    public static String encodeURL(String callbackUrl) throws MalformedURLException {

        URL url = new URL(callbackUrl);
        StringBuilder encodedCallbackUrl = new StringBuilder(
                new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath(), null).toString());

        Map<String, String> encodedQueryMap = getEncodedQueryParamMap(url.getQuery());

        if (MapUtils.isNotEmpty(encodedQueryMap)) {
            encodedCallbackUrl.append("?");
            encodedCallbackUrl.append(encodedQueryMap.keySet().stream().map(key -> key + PADDING_CHAR
                    + encodedQueryMap.get(key)).collect(Collectors.joining(SPLITTING_CHAR)));
        }

        return encodedCallbackUrl.toString();
    }

    /**
     * Encode query params of the call back url. However this method doesn't support URLs with
     * spaces. Use {@link #encodeURL(String)} to encode URLs which contain spaces.
     *
     * @param callbackUrl callback url from the request.
     * @return encoded callback url.
     * @throws URISyntaxException URI Syntax Exception.
     */
    public static String getURLEncodedCallback(String callbackUrl) throws URISyntaxException {

        URI uri = new URI(callbackUrl);
        StringBuilder encodedCallbackUrl = new StringBuilder(
                new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString());

        Map<String, String> encodedQueryMap = getEncodedQueryParamMap(uri.getQuery());

        if (MapUtils.isNotEmpty(encodedQueryMap)) {
            encodedCallbackUrl.append("?");
            encodedCallbackUrl.append(encodedQueryMap.keySet().stream().map(key -> key + PADDING_CHAR + encodedQueryMap.get(key))
                    .collect(Collectors.joining(SPLITTING_CHAR)));
        }

        return encodedCallbackUrl.toString();
    }

    /**
     * Encode query params of the call back url one by one.
     *
     * @param queryParams String of query params.
     * @return map of the encoded query params.
     */
    public static Map<String,String> getEncodedQueryParamMap(String queryParams) {

        Map<String, String> encodedQueryMap = new HashMap<>();
        if (StringUtils.isNotBlank(queryParams)) {
            encodedQueryMap = Arrays.stream(queryParams.split(SPLITTING_CHAR))
                    .map(entry -> entry.split(PADDING_CHAR))
                    .collect(Collectors.toMap(entry -> Encode.forUriComponent(entry[0]),
                            entry -> Encode.forUriComponent(entry[1])));
        }

        return encodedQueryMap;
    }

    /**
     * Construct the URL depending on the path and the resource name.
     *
     * @param path path of the url
     * @return endpoint url
     */
    public static String buildEndpointUrl(String path) {

        String endpointUrl = IdentityManagementServiceUtil.getInstance().getServiceContextURL()
                .replace(IdentityManagementEndpointConstants.UserInfoRecovery.SERVICE_CONTEXT_URL, "");

        if (path.startsWith("/")) {
            return endpointUrl + path;
        } else {
            return endpointUrl + "/" + path;
        }
    }

    public static void addErrorInformation(HttpServletRequest request, Exception e) {
        Error error = buildError(e);
        addErrorInformation(request, error);
    }

    public static void addErrorInformation(HttpServletRequest request, Error errorD) {
        request.setAttribute("error", true);
        if (errorD != null) {
            request.setAttribute("errorMsg", errorD.getDescription());
            request.setAttribute("errorCode", errorD.getCode());
        }
    }

    public static Error buildError(Exception e) {

        try {
            return new Gson().fromJson(e.getMessage(), Error.class);
        } catch (JsonSyntaxException ex) {
            // We cannot build proper error code and error messages from the exception.
            log.error("Exception while retrieving error details from original exception. Original exception:", e);
            return buildUnexpectedError();

        }
    }

    private static Error buildUnexpectedError() {

        Error error = new Error();
        error.setCode(CODE);
        error.setMessage(UNEXPECTED_ERROR);
        error.setDescription(UNEXPECTED_ERROR);
        return error;
    }

    private static RetryError buildUnexpectedRetryError() {

        RetryError error = new RetryError();
        error.setCode(CODE);
        error.setMessage(UNEXPECTED_ERROR);
        error.setDescription(UNEXPECTED_ERROR);
        return error;
    }

    public static RetryError buildRetryError(Exception e) {

        try {
            return new Gson().fromJson(e.getMessage(), RetryError.class);
        } catch (JsonSyntaxException ex) {
            // We cannot build proper error code and error messages from the exception.
            log.error("Exception while retrieving error details from original exception. Original exception:", e);
            return buildUnexpectedRetryError();
        }
    }
}
