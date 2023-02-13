/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import com.google.gson.JsonObject;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Application.CONSOLE_APP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Application.MY_ACCOUNT_APP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ContentTypes.TYPE_APPLICATION_JSON;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.StandardInboundProtocols.OAUTH2;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.StandardInboundProtocols.SAML2;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.REQUEST_PARAM_APPLICATION;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.getConsoleURL;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.getMyAccountURL;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.getNonceCookieName;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.isNonceCookieEnabled;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.DEFAULT_SP_CONFIG;

/**
 * This class handles the redirect URL retrieval for invalid session index.
 */
public class LoginContextManagementUtil {

    private static final Log log = LogFactory.getLog(LoginContextManagementUtil.class);

    public static void handleLoginContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String sessionDataKey = request.getParameter("sessionDataKey");
        String relyingParty = request.getParameter("relyingParty");
        String applicationName = request.getParameter(REQUEST_PARAM_APPLICATION);
        String authenticators = request.getParameter("authenticators");
        String tenantDomain = getTenantDomain(request);

        JsonObject result = new JsonObject();
        response.setContentType(TYPE_APPLICATION_JSON);
        if (StringUtils.isBlank(sessionDataKey) || (StringUtils.isBlank (applicationName) && StringUtils.isBlank
                (relyingParty))) {
            if (log.isDebugEnabled()) {
                log.debug("Required data to proceed is not available in the request.");
            }
            //cannot handle the request without sessionDataKey and application name or relying party
            // (for backward compatibility of the api)
            result.addProperty("status", "success");
            response.getWriter().write(result.toString());
            return;
        }

        AuthenticationContext context = FrameworkUtils.getAuthenticationContextFromCache(sessionDataKey);
        // Valid Request
        if (isValidRequest(request, context, authenticators)) {
            if (log.isDebugEnabled()) {
                log.debug("Setting response for the valid request.");
            }
            // If the context is valid and at the first step.
            if (isStepHasMultiOption(context)) {
                context.setCurrentAuthenticator(null);
            }
            result.addProperty("status", "success");
            response.getWriter().write(result.toString());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving redirect url for the requested application:" + applicationName
                        + " relying party: " + relyingParty +  " for sessionDataKey: " +  sessionDataKey);
            }
            String redirectUrl = getRedirectURL(applicationName, relyingParty, tenantDomain, request);
            if (StringUtils.isBlank(redirectUrl)) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to obtain a redirect URL for the application: " + applicationName + "or " +
                            "relyingParty: " + relyingParty + " for sessionDataKey: " + sessionDataKey);
                }
                // Can't handle
                result.addProperty("status", "success");
            } else {
                result.addProperty("status", "redirect");
                result.addProperty("redirectUrl", redirectUrl);
            }
            response.getWriter().write(result.toString());
        }
    }

    private static String getTenantDomain(HttpServletRequest request) {

        String tenantDomain;
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant Qualified URL mode enabled. Retrieving tenantDomain from thread local context.");
            }
            tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        } else {
            tenantDomain = request.getParameter("tenantDomain");
        }

        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        if (log.isDebugEnabled()) {
            log.debug("Service Provider tenant domain: " + tenantDomain);
        }
        return tenantDomain;
    }

    /**
     * Get the redirect URL for the application.
     * @param appName
     * @param tenantDomain
     * @return
     */
    private static String getRedirectURL(String appName, String relyingParty, String tenantDomain,
                                         HttpServletRequest request) {

        String redirectUrl = null;
        if (StringUtils.isNotEmpty(appName)) {
            String accessURLFromApplication = getAccessURLFromApplication(appName, tenantDomain);
            return replaceURLPlaceholders(accessURLFromApplication, request);
        } else if (StringUtils.isNotEmpty(relyingParty)) {
            // If application is not sent in the request, retrieve the URL via the relying party configuration
            // for the backward compatibility
            if (log.isDebugEnabled()) {
                log.debug("Trying to retrieve the access url using relyingParty: " + relyingParty + " as the " +
                        "application name is not sent in the request.");
            }
            redirectUrl = getRelyingPartyRedirectUrl(relyingParty, tenantDomain);
        }
        return redirectUrl;
    }

    /**
     * Returns the access URL of the application as the redirect url.
     * @param appName
     * @param tenantDomain
     * @return
     */
    public static String getAccessURLFromApplication(String appName, String tenantDomain) {

        ServiceProvider sp;
        String accessURL = null;
        try {
            appName = URLDecoder.decode(appName, "UTF-8");
            if (MY_ACCOUNT_APP.equals(appName) || CONSOLE_APP.equals(appName)) {
                sp = ApplicationManagementService.getInstance().getServiceProvider(appName,
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            } else {
                sp = ApplicationManagementService.getInstance().getServiceProvider(appName, tenantDomain);
            }
            if (sp != null) {
                accessURL = sp.getAccessUrl();
            }
        } catch (IdentityApplicationManagementException e) {
            log.error("Unable to retrieve an application with name: " + appName, e);
        } catch (UnsupportedEncodingException e) {
            log.error("Error while decoding application name: " + appName, e);
        }

        if (MY_ACCOUNT_APP.equals(appName)) {
            accessURL = getMyAccountURL(accessURL);
        } else if (CONSOLE_APP.equals(appName)) {
            accessURL = getConsoleURL(accessURL);
        }

        if (log.isDebugEnabled() && StringUtils.isNotEmpty(accessURL)) {
            log.debug("Access URL is: " + accessURL + " for the the application: " + appName + " in tenant: "
                    + tenantDomain);
        }
        return accessURL;
    }

    /**
     * Resolve the placeholders in the access URL
     *
     * @param accessURL Access URL.
     * @param request   Servlet Request.
     * @return Resolved access URL.
     */
    private static String replaceURLPlaceholders(String accessURL, HttpServletRequest request) {
        if (StringUtils.isBlank(accessURL)) {
            return accessURL;
        }
        if (!accessURL.contains("${UserTenantHint}")) {
            return accessURL;
        }
        String userTenantHint = request.getParameter("ut");
        if (StringUtils.isBlank(userTenantHint)) {
            userTenantHint = request.getParameter("t");
        }
        if (StringUtils.isBlank(userTenantHint)) {
            userTenantHint = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return accessURL.replaceAll(Pattern.quote("${UserTenantHint}"), userTenantHint)
                .replaceAll(Pattern.quote("/t/carbon.super"), "");
    }

    /**
     * Returns the redirect url configured in the registry against relying party.
     * This is a deprecated functionality. Use the getAccessURLFromApplication method instead of this method
     * @param relyingParty Name of the relying party
     * @param tenantDomain Tenant Domain.
     * @return Redirect URL.
     */
    @Deprecated
    public static String getRelyingPartyRedirectUrl(String relyingParty, String tenantDomain) {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving configured url against relying party : " + relyingParty + "for tenant domain : " +
                    tenantDomain);
        }

        ServiceProvider sp;
        String redirectUrl = null;

        // Retrieve an application of which oauth2 is configured as the inbound auth config.
        sp = getServiceProviderByRelyingParty(relyingParty, tenantDomain, OAUTH2);

        if (sp == null) {
            // Retrieve an application of which saml2 is configured as the inbound auth config.
            sp = getServiceProviderByRelyingParty(relyingParty, tenantDomain, SAML2);
        }

        if (sp != null) {
            redirectUrl = sp.getAccessUrl();
        }

        if (log.isDebugEnabled() && StringUtils.isNotEmpty(redirectUrl)) {
            log.debug("Redirect URL is: " + redirectUrl + " for the the relyingParty: " + relyingParty
                    + " in tenant: " + tenantDomain);
        }
        return redirectUrl;
    }

    private static ServiceProvider getServiceProviderByRelyingParty(String relyingParty, String tenantDomain, String
            type) {
        ServiceProvider sp = null;
        try {
            sp = ApplicationManagementService.getInstance().getServiceProviderByClientId(relyingParty,
                    type, tenantDomain);
            if (sp != null && DEFAULT_SP_CONFIG.equals(sp.getApplicationName())) {
                return null;
            }
        } catch (IdentityApplicationManagementException e) {
            log.warn("Unable to retrieve an application for the relying party: " + relyingParty + " of type: " +
                    type + " in the tenant: " + tenantDomain);
        }
        return sp;
    }

    /**
     * Returns whether post authentication handler execution is ended or not.
     *
     * @param authenticationContext Authentication context.
     * @return True if post authentication handlers have finished execution on this context. else false.
     */
    public static boolean isPostAuthenticationExtensionCompleted(AuthenticationContext authenticationContext) {

        Object object = authenticationContext.getProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED);
        if (object != null && object instanceof Boolean) {
            return (Boolean) object;
        } else {
            return false;
        }
    }

    /**
     * Mark post authentication handler execution completion on authentication context.
     *
     * @param authenticationContext Authentication context.
     */
    public static void markPostAuthenticationCompleted(AuthenticationContext authenticationContext) {

        authenticationContext.setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED,
                true);
    }

    private static boolean isStepHasMultiOption(AuthenticationContext context) {

        Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
        boolean stepHasMultiOption = false;

        if (stepMap != null && !stepMap.isEmpty()) {
            StepConfig stepConfig = stepMap.get(context.getCurrentStep());

            if (stepConfig != null) {
                stepHasMultiOption = stepConfig.isMultiOption();
            }
        }
        return stepHasMultiOption;
    }

    private static boolean canHandleAuthenticator(AuthenticationContext context, String authenticators) {

        List<String> authenticatorsList = new ArrayList();
        if (authenticators != null) {
            String[] authenticatorIdPMappings = authenticators.split(";");
            for (String authenticatorIdPMapping : authenticatorIdPMappings) {
                String[] authenticatorIdPMapArr = authenticatorIdPMapping.split(":");
                authenticatorsList.add(authenticatorIdPMapArr[0]);
            }
        }

        Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
        if (MapUtils.isNotEmpty(stepMap)) {
            StepConfig stepConfig = stepMap.get(context.getCurrentStep());

            for (AuthenticatorConfig authenticatorConfig : stepConfig.getAuthenticatorList()) {
                if (!authenticatorsList.contains(authenticatorConfig.getName())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isValidRequest(HttpServletRequest request, AuthenticationContext context,
                                          String authenticators) {

        if (context == null) {
            return false;
        }

        boolean isValidRequest = context.getProperty(FrameworkConstants.CURRENT_POST_AUTHENTICATION_HANDLER) == null;
        if (FrameworkUtils.isAuthenticationContextExpiryEnabled() && isValidRequest) {
            isValidRequest = FrameworkUtils.getCurrentStandardNano() <= context.getExpiryTime();
        }
        if (isNonceCookieEnabled() && isValidRequest) {
            Cookie nonceCookie = FrameworkUtils.getCookie(request, getNonceCookieName(context));
            isValidRequest = nonceCookie != null && StringUtils.isNotBlank(nonceCookie.getValue());
        }
        if (StringUtils.isNotEmpty(authenticators) && isValidRequest) {
            isValidRequest = canHandleAuthenticator(context, authenticators);
        }
        return isValidRequest;
    }
}
