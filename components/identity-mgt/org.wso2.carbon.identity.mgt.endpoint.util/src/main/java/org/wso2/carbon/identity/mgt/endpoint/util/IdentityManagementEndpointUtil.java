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

package org.wso2.carbon.identity.mgt.endpoint.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.client.ServiceClient;
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
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.SameSiteCookie;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.model.CookieBuilder;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApplicationDataRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApplicationDataRetrievalClientException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Claim;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Error;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.RetryError;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.User;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants.My_ACCOUNT_APPLICATION_NAME;
import static org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants.SUPER_TENANT;
import static org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants.USER_TENANT_HINT_PLACE_HOLDER;

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
    public static final String ROOT_DOMAIN = "/";
    private static final String PROTECTED_TOKENS = "protectedTokens";
    private static final String DEFAULT_CALLBACK_HANDLER = "org.wso2.carbon.securevault.DefaultSecretCallbackHandler";
    private static final String SECRET_PROVIDER = "secretProvider";

    private static final Log log = LogFactory.getLog(IdentityManagementEndpointUtil.class);
    private static final String CODE = "51007";
    private static final String UNEXPECTED_ERROR = "Unexpected Error.";

    private static String accessUsername = null;
    private static String accessPassword = null;

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
    @Deprecated
    public static final String getUserPortalUrl(String userPortalUrl) {

        return getUserPortalUrl(userPortalUrl, null);
    }

    /**
     * Returns the My Account access url for the specific tenant.
     *
     * @param userPortalUrl configured user portal url
     * @param tenantDomain tenant domain of the user
     * @return configured url or the default url if configured url is empty
     */
    public static final String getUserPortalUrl(String userPortalUrl, String tenantDomain) {

        if (StringUtils.isNotBlank(userPortalUrl)) {
            return userPortalUrl;
        }
        try {
            if (StringUtils.isNotEmpty(tenantDomain)) {
                ApplicationDataRetrievalClient applicationDataRetrievalClient = new ApplicationDataRetrievalClient();
                try {
                    String myAccountAccessUrl = applicationDataRetrievalClient.getApplicationAccessURL(SUPER_TENANT,
                            My_ACCOUNT_APPLICATION_NAME);
                    if (StringUtils.isNotEmpty(myAccountAccessUrl)) {
                        return replaceUserTenantHintPlaceholder(myAccountAccessUrl, tenantDomain);
                    }
                } catch (ApplicationDataRetrievalClientException e) {
                    // Falling back to building the url.
                }
            }
            return ServiceURLBuilder.create().addPath(IdentityManagementEndpointConstants.USER_PORTAL_URL).build()
                    .getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            throw new IdentityRuntimeException(
                    "Error while building url for context: " + IdentityManagementEndpointConstants.USER_PORTAL_URL);
        }
    }

    /**
     * Replace the ${UserTenantHint} placeholder in the url with the tenant domain.
     *
     * @param url           Url with the placeholder.
     * @param tenantDomain  Tenant Domain.
     * @return              Processed url.
     */
    public static String replaceUserTenantHintPlaceholder(String url, String tenantDomain) {

        if (StringUtils.isBlank(url)) {
            return url;
        }
        if (!url.contains(USER_TENANT_HINT_PLACE_HOLDER)) {
            return url;
        }
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = SUPER_TENANT;
        }
        return url.replaceAll(Pattern.quote(USER_TENANT_HINT_PLACE_HOLDER), tenantDomain)
                .replaceAll(Pattern.quote("/t/" + SUPER_TENANT), "");
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
     * Encode query params of the call back url.
     *
     * @param callbackUrl Callback url from the request.
     * @return Encoded callback url.
     * @throws URISyntaxException URI Syntax Exception.
     * @deprecated
     * This method is no longer acceptable to encode query params of the call back url. Because this method
     * doesn't support URLs with spaces.
     * Use the {@link #encodeURL(String)} method instead.
     */
    @Deprecated
    public static String getURLEncodedCallback(String callbackUrl) throws URISyntaxException {

        if (StringUtils.isBlank(callbackUrl)) {
            return callbackUrl;
        }

        callbackUrl = callbackUrl.trim().replace(" ", "%20");
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
    public static Map<String, String> getEncodedQueryParamMap(String queryParams) {

        Map<String, String> encodedQueryMap = new HashMap<>();
        if (StringUtils.isBlank(queryParams)) {
            return encodedQueryMap;
        }
        String[] params = queryParams.split(SPLITTING_CHAR);
        if (ArrayUtils.isEmpty(params)) {
            return encodedQueryMap;
        }
        for (String param : params) {
            String[] queryParamWithValue = param.split(PADDING_CHAR);
            if (queryParamWithValue.length > 1) {
                encodedQueryMap.put(Encode.forUriComponent(queryParamWithValue[0]),
                        Encode.forUriComponent(queryParamWithValue[1]));
            }
        }
        return encodedQueryMap;
    }

    /**
     * Construct the URL depending on the path and the resource name.
     *
     * @param path path of the url
     * @return endpoint url
     */
    @Deprecated
    public static String buildEndpointUrl(String path) {

        String serviceContextURL = IdentityManagementServiceUtil.getInstance().getServiceContextURL();
        String endpointUrl = replaceLastOccurrence(serviceContextURL,
                IdentityManagementEndpointConstants.UserInfoRecovery.SERVICE_CONTEXT_URL, "");

        if (path.startsWith("/")) {
            return endpointUrl + path;
        } else {
            return endpointUrl + "/" + path;
        }
    }

    /**
     * Method to replace the last occurrence of the provided string in the base string.
     *
     * issue: https://services.mnm.local:9443/services => https://am/identity/concent-mgt/v1.mnm.local:9443/services/
     * Fix: https://services.mnm.local:9443/services => https://services.mnm.local:9443/am/identity/concent-mgt/v1/
     *
     * @param base The base string
     * @param toReplace The string which needs to be find and replaced
     * @param replaceWith The replacement string.
     * @return String with the replaced value.
     * */
    private static String replaceLastOccurrence(String base, String toReplace, String replaceWith) {
        int lastIndex = base.lastIndexOf(toReplace);

        if (lastIndex == -1) {
            return base;
        }

        String begin = base.substring(0, lastIndex);
        String end = base.substring(lastIndex + toReplace.length());
        return begin + replaceWith + end;
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

    public static void authenticate(ServiceClient client) throws Exception {

        if (accessPassword == null || accessUsername == null) {
            loadCredentials();
        }

        if (accessUsername != null && accessPassword != null) {
            setOptions(client, accessUsername, accessPassword);
        } else {
            throw new Exception("Authentication username or password not set");
        }
    }

    private static void loadCredentials() throws IOException {

        Properties properties = new Properties();
        File currentDirectory =
                new File(new File(IdentityManagementEndpointConstants.RELATIVE_PATH_START_CHAR).getAbsolutePath());
        String configFilePath = currentDirectory.getCanonicalPath() + File.separator +
                IdentityManagementEndpointConstants.SERVICE_CONFIG_RELATIVE_PATH;
        File configFile = new File(configFilePath);

        try (InputStream inputStream = configFile.exists() ? new FileInputStream(configFile) :
                IdentityManagementServiceUtil.class.getClassLoader().getResourceAsStream
                        (IdentityManagementEndpointConstants.SERVICE_CONFIG_FILE_NAME)) {
            properties.load(inputStream);

            // Resolve encrypted properties with secure vault.
            resolveSecrets(properties);
        }

        accessUsername = properties.getProperty(IdentityManagementEndpointConstants.ServiceConfigConstants
                .SERVICE_ACCESS_USERNAME);
        accessPassword = properties.getProperty(IdentityManagementEndpointConstants.ServiceConfigConstants
                .SERVICE_ACCESS_PASSWORD);
    }

    public static void setOptions(ServiceClient client, String accessUsername, String accessPassword) {

        IdentityManagementServiceUtil.setAutheticationOptions(client, accessUsername, accessPassword);
    }

    /**
     * Get base path URL for API clients.
     *
     * @param tenantDomain tenant Domain.
     * @param context      URL context.
     * @return base path.
     * @throws ApiException ApiException.
     */
    public static String getBasePath(String tenantDomain, String context) throws ApiException {

        return getBasePath(tenantDomain, context, true);
    }

    /**
     * Get base path URL for API clients.
     *
     * @param tenantDomain          Tenant Domain.
     * @param context               URL context.
     * @param isEndpointTenantAware Whether the endpoint is tenant aware.
     * @return Base path.
     * @throws ApiException ApiException.
     */
    public static String getBasePath(String tenantDomain, String context, boolean isEndpointTenantAware)
            throws ApiException {

        String basePath;
        String serverUrl = IdentityManagementServiceUtil.getInstance().getContextURLFromFile();
        try {
            if (StringUtils.isBlank(serverUrl)) {
                if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
                    basePath = ServiceURLBuilder.create().addPath(context).setTenant(tenantDomain).build()
                            .getAbsoluteInternalURL();
                } else {
                    serverUrl = ServiceURLBuilder.create().build().getAbsoluteInternalURL();
                    if (StringUtils.isNotBlank(tenantDomain) && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                            .equalsIgnoreCase(tenantDomain) && isEndpointTenantAware) {
                        basePath = serverUrl + "/t/" + tenantDomain + context;
                    } else {
                        basePath = serverUrl + context;
                    }
                }
            } else {
                if (StringUtils.isNotBlank(tenantDomain) && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                        .equalsIgnoreCase(tenantDomain) && isEndpointTenantAware) {
                    basePath = serverUrl + "/t/" + tenantDomain + context;
                } else {
                    basePath = serverUrl + context;
                }
            }
        } catch (URLBuilderException e) {
            throw new ApiException("Error while building url for context: " + context);
        }
        return basePath;
    }

    /**
     * Get status of the availability of secured (with secure vault) properties
     *
     * @return availability of secured properties
     */
    private static boolean isSecuredPropertyAvailable(Properties properties) {

        Enumeration propertyNames = properties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            if (PROTECTED_TOKENS.equals(key) && StringUtils.isNotBlank(properties.getProperty(key))) {
                return true;
            }
        }
        return false;
    }

    /**
     * There can be sensitive information like passwords in configuration file. If they are encrypted using secure
     * vault, this method will resolve them and replace with original values.
     */
    private static void resolveSecrets(Properties properties) {

        String secretProvider = (String) properties.get(SECRET_PROVIDER);
        if (StringUtils.isBlank(secretProvider)) {
            properties.put(SECRET_PROVIDER, DEFAULT_CALLBACK_HANDLER);
        }
        SecretResolver secretResolver = SecretResolverFactory.create(properties);
        if (secretResolver != null && secretResolver.isInitialized()) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                if (value != null) {
                    value = MiscellaneousUtil.resolve(value, secretResolver);
                }
                properties.put(key, value);
            }
        }
        // Support the protectedToken alias used for encryption. ProtectedToken alias is deprecated.
        if (isSecuredPropertyAvailable(properties)) {
            SecretResolver resolver = SecretResolverFactory.create(properties, "");
            String protectedTokens = (String) properties.get(PROTECTED_TOKENS);
            StringTokenizer st = new StringTokenizer(protectedTokens, ",");
            while (st.hasMoreElements()) {
                String element = st.nextElement().toString().trim();

                if (resolver.isTokenProtected(element)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resolving and replacing secret for " + element);
                    }
                    // Replaces the original encrypted property with resolved property
                    properties.put(element, resolver.resolve(element));
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No encryption done for value with key :" + element);
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Secure vault encryption ignored since no protected tokens available.");
            }
        }
    }

    /**
     * Get a query parameter value from a URL.
     *
     * @param url               URL.
     * @param queryParameter    Required query parameter name.
     * @return Query parameter value.
     * @throws URISyntaxException If url is not in valid syntax.
     */
    public static String getQueryParameter(String url, String queryParameter) throws URISyntaxException {

        String queryParams = new URI(url).getQuery();
        Map<String, String> queryParamMap = new HashMap<>();
        if (StringUtils.isNotBlank(queryParams)) {
            queryParamMap = Arrays.stream(queryParams.split(SPLITTING_CHAR))
                    .map(entry -> entry.split(PADDING_CHAR))
                    .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
        }
        return queryParamMap.get(queryParameter);
    }

    /**
     * Stores a cookie to the response.
     *
     * @param req         Incoming HttpServletRequest.
     * @param resp        Outgoing HttpServletResponse.
     * @param cookieName  Name of the cookie to be stored.
     * @param value       Value of the cookie.
     * @param age         Max age of the cookie.
     * @param sameSite    SameSite attribute value for the cookie.
     * @param domain      Domain of the cookie.
     */
    public static void setCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName, String value,
                          Integer age, SameSiteCookie sameSite, String path, String domain) {

        CookieBuilder cookieBuilder = new CookieBuilder(cookieName, value);
        IdentityCookieConfig cookieConfig = IdentityUtil.getIdentityCookieConfig(cookieName);
        if (cookieConfig != null) {
            updateCookieConfig(cookieBuilder, cookieConfig, age, path, sameSite, domain);
        } else {
            cookieBuilder.setSecure(true)
                    .setHttpOnly(true)
                    .setPath(StringUtils.isNotBlank(path) ? path : ROOT_DOMAIN)
                    .setDomain(domain)
                    .setSameSite(sameSite);
            if (age != null && age > 0) {
                cookieBuilder.setMaxAge(age);
            }
        }
        resp.addCookie(cookieBuilder.build());
    }

    private static void updateCookieConfig(CookieBuilder cookieBuilder, IdentityCookieConfig
            cookieConfig, Integer age, String path, SameSiteCookie sameSite, String domain) {

        if (cookieConfig.getDomain() != null) {
            cookieBuilder.setDomain(cookieConfig.getDomain());
        } else if (StringUtils.isNotBlank(domain)) {
            cookieBuilder.setDomain(domain);
        }

        if (cookieConfig.getPath() != null) {
            cookieBuilder.setPath(cookieConfig.getPath());
        } else if (StringUtils.isNotBlank(path)) {
            cookieBuilder.setPath(path);
        }

        if (cookieConfig.getComment() != null) {
            cookieBuilder.setComment(cookieConfig.getComment());
        }

        if (cookieConfig.getMaxAge() > 0) {
            cookieBuilder.setMaxAge(cookieConfig.getMaxAge());
        } else if (age != null && age > 0) {
            cookieBuilder.setMaxAge(age);
        }

        if (cookieConfig.getVersion() > 0) {
            cookieBuilder.setVersion(cookieConfig.getVersion());
        }

        if (cookieConfig.getSameSite() != null) {
            cookieBuilder.setSameSite(cookieConfig.getSameSite());
        } else if (sameSite != null) {
            cookieBuilder.setSameSite(sameSite);
        }

        cookieBuilder.setHttpOnly(cookieConfig.isHttpOnly());

        cookieBuilder.setSecure(cookieConfig.isSecure());
    }
}
