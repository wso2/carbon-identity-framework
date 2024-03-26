/*
 * Copyright (c) 2014, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.owasp.encoder.Encode;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.endpoint.util.bean.UserDTO;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.HTTPClientUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static org.wso2.carbon.identity.application.authentication.endpoint.util.Constants.ErrorToi18nMappingConstants.ERROR_TO_I18N_MAP;

/**
 * AuthenticationEndpointUtil defines utility methods used across the authenticationendpoint web application.
 */
public class AuthenticationEndpointUtil {

    private static final Log log = LogFactory.getLog(AuthenticationEndpointUtil.class);
    public static final String CLIENT_AUTH_TYPE = "Client";
    private static final String CLIENT = CLIENT_AUTH_TYPE + " ";
    private static final String COLON = ":";
    private static final String CUSTOM_PAGE_APP_SPECIFIC_CONFIG_KEY_SEPARATOR = "-";
    private static final String HTTP_METHOD_GET = "GET";
    private static final String QUERY_STRING_APPENDER = "&";
    private static final String QUERY_STRING_INITIATOR = "?";
    private static final String PADDING_CHAR = "=";
    private static final String UNDERSCORE = "_";
    private static final String TENANT_DOMAIN_PLACEHOLDER = "${tenantDomain}";
    private static final String SUPER_TENANT = "carbon.super";

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
        return ERROR_TO_I18N_MAP.getOrDefault(errorKey,
                Constants.ErrorToi18nMappingConstants.INCORRECT_ERROR_MAPPING_KEY);
    }

    /**
     * This method is to validate a URL. This method validate both absolute & relative URLs.
     *
     * @param urlString URL String.
     * @return true if valid URL, false otherwise.
     */
    public static boolean isValidURL(String urlString) {

        return validateURL(urlString);
    }

    /**
     * This method is to validate a multiOptionURI. This will check whether the URL is a proper relative URL and
     * if it is an absolute URL will validate the host of the URL.
     * @param urlString URL String.
     * @return true if valid URL, false otherwise.
     */
    public static boolean isValidMultiOptionURI(String urlString) {

        if (validateURL(urlString)) {
            return validateCallbackURL(urlString);
        }
        return false;
    }

    private static boolean validateURL(String urlString) {

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

    private static boolean validateCallbackURL(String callbackURL) {

        String authenticationEndpointURL = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        try {
            if (isURLRelative(callbackURL)) {
                /*
                  If the multiOptionURI is a relative URL, then the URL should start with an alphanumeric character
                  or a slash followed by an alphanumeric character.
                 */
                if (callbackURL.matches("^/?[a-zA-Z0-9].*")) {
                    return true;
                } else {
                    log.error("No valid URL found for the multiOptionURI. URL: " + callbackURL + " is not allowed.");
                    return false;
                }
            } else {
                String multiOptionURIHostAndPort = getHostAndPort(callbackURL);
                /*
                  If the multiOptionURI is an absolute URL, then the host of the multiOptionURI should be
                  either host of the server or host of the externalized authenticationEndpointURL.
                 */
                if (multiOptionURIHostAndPort.equals(getHostAndPort(buildAbsoluteURL("/"))) ||
                        (!isURLRelative(authenticationEndpointURL) && multiOptionURIHostAndPort
                                .equals(getHostAndPort(authenticationEndpointURL)))) {
                    return true;
                } else {
                    log.error("No valid host found for the multiOptionURI. URL: " + multiOptionURIHostAndPort +
                            " is not allowed.");
                    return false;
                }
            }
        } catch (MalformedURLException | URISyntaxException | URLBuilderException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * Extracts the host and port from a given URL string.
     * If the URL does not specify a port, only the host is returned.
     *
     * @param urlString The URL from which to extract the host and port.
     * @return A string containing the host and, if specified, the port.
     * @throws MalformedURLException If the given string does not represent a valid URL.
     */
    private static String getHostAndPort(String urlString) throws MalformedURLException {

        URL url = new URL(urlString);
        String host = url.getHost();
        int port = url.getPort(); // Returns -1 if the port is not explicitly specified in the URL.

        return port == -1 ? host : host + ":" + port;
    }

    private static boolean isURLRelative(String uriString) throws URISyntaxException {

        return !new URI(uriString).isAbsolute();
    }

    private static String buildAbsoluteURL(String contextPath) throws URLBuilderException {

        return ServiceURLBuilder.create().addPath(contextPath).build().getAbsolutePublicURL();
    }

    /**
     * Send GET request with client authentication and return data.
     *
     * @param backendURL The URL of the backend service.
     * @return Data which was received from the backend service.
     */
    public static String sendGetRequest(String backendURL) {

        StringBuilder responseString = new StringBuilder();
        try (CloseableHttpClient httpclient = HTTPClientUtils.createClientWithCustomVerifier().build()) {

            HttpGet httpGet = new HttpGet(backendURL);
            setAuthorizationHeader(httpGet);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                if (log.isDebugEnabled()) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode() +
                            " when invoking " + HTTP_METHOD_GET + " for URL: " + backendURL);
                }
                responseString = handleHttpResponse(response, backendURL);
            } finally {
                httpGet.releaseConnection();
            }
        } catch (IOException e) {
            log.error("Sending " + HTTP_METHOD_GET + " request to URL : " + backendURL + ", failed.", e);
        }
        return responseString.toString();
    }

    /**
     * Extracts the response content from the http response provided to the method.
     *
     * @param response The response obtained from the backend service.
     *                 backendURL The URL of the backend service.
     * @return Extracted http response content.
     * @throws IOException if there is an error while extracting the response content.
     */
    private static StringBuilder handleHttpResponse(CloseableHttpResponse response, String backendURL) throws IOException {

        StringBuilder responseString = new StringBuilder();

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()))) {
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    responseString.append(inputLine);
                }
            }
        } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            if (log.isDebugEnabled()) {
                log.debug("Response received from the backendURL " + backendURL + " with status " +
                        response.getStatusLine() + ".");
            }
        } else {
            log.error("Response received from the backendURL " + backendURL +" failed with status " +
                    response.getStatusLine() + ".");
        }

        return responseString;
    }

    /**
     * Add OAuth authorization header to the httpMethod.
     *
     * @param httpMethod The HttpMethod which needs the authorization header.
     */
    private static void setAuthorizationHeader(HttpRequestBase httpMethod) {

        String name = EndpointConfigManager.getAppName();
        String password = String.valueOf(EndpointConfigManager.getAppPassword());

        String toEncode = name + COLON + password;
        byte[] encoding = org.apache.commons.codec.binary.Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());

        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION, CLIENT + authHeader);
    }

    /**
     * Resolve "${tenantDomain}" in the URL.
     *
     * @param   url URL to be tenant resolved.
     * @return  Tenant resolved URL.
     */
    public static String resolveTenantDomain(String url) {

        if (url.contains(TENANT_DOMAIN_PLACEHOLDER)) {
            String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
            if (StringUtils.isBlank(tenantDomain)) {
                tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }
            if (SUPER_TENANT.equals(tenantDomain)) {
                url = url.replace("t/" + TENANT_DOMAIN_PLACEHOLDER + "/", "");
            } else {
                url = url.replace(TENANT_DOMAIN_PLACEHOLDER, tenantDomain);
            }
        }
        return url;
    }

    /**
     * Resolve the query string of the request using the request parameter map.
     *
     * @param queryParamMap - Parameter map of the request.
     * @return - StringBuilder with query parameters appended.
     */
    public static String resolveQueryString(Map<String, String[]> queryParamMap) {

        StringBuilder queryParamString = new StringBuilder();
        if (queryParamMap != null && !queryParamMap.isEmpty()) {
            queryParamString.append("?");
            List<NameValuePair> paramNameValuePairs = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : queryParamMap.entrySet()) {
                if (ArrayUtils.isNotEmpty(entry.getValue())) {
                    paramNameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()[0]));
                }
            }
            queryParamString.append(URLEncodedUtils.format(paramNameValuePairs, StandardCharsets.UTF_8));
        }
        return queryParamString.toString();
    }
}
