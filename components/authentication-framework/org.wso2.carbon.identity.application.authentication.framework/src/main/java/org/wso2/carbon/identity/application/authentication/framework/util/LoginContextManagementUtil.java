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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.getConsoleURL;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.getMyAccountURL;

/**
 * This class handles the redirect URL retrieval for invalid session index.
 */
public class LoginContextManagementUtil {

    private static final String SP_REDIRECT_URL_RESOURCE_PATH = "/identity/config/relyingPartyRedirectUrls";
    private static final Log log = LogFactory.getLog(LoginContextManagementUtil.class);

    public static void handleLoginContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String sessionDataKey = request.getParameter("sessionDataKey");
        String relyingParty = request.getParameter("relyingParty");
        String applicationName = request.getParameter("application");

        if (StringUtils.isEmpty(applicationName)) {
            // Try retrieving the application name from the referer
            applicationName = FrameworkUtils.getServiceProviderNameByReferer(request);
        }
        String tenantDomain = getTenantDomain(request);

        JsonObject result = new JsonObject();
        response.setContentType("application/json");
        if (StringUtils.isBlank(sessionDataKey) || (StringUtils.isBlank (applicationName) && StringUtils.isBlank
                (relyingParty))) {
            if (log.isDebugEnabled()) {
                log.debug("Required data to proceed is not available in the request.");
            }
            //cannot handle the request without sessionDataKey
            result.addProperty("status", "success");
            response.getWriter().write(result.toString());
            return;
        }

        AuthenticationContext context = FrameworkUtils.getAuthenticationContextFromCache(sessionDataKey);
        // Valid Request
        if (context != null) {
            if (isStepHasMultiOption(context)) {
                context.setCurrentAuthenticator(null);
            }
            result.addProperty("status", "success");
            response.getWriter().write(result.toString());
        } else {

            String redirectUrl = getRedirectURL(applicationName, relyingParty, tenantDomain);

            if (StringUtils.isBlank(redirectUrl)) {
                if (log.isDebugEnabled()) {
                    log.debug("Redirect URL is not available for the relying party - " + relyingParty + " for " +
                            "sessionDataKey: " + sessionDataKey);
                }
                // Can't handle
                result.addProperty("status", "success");
                response.getWriter().write(result.toString());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Redirect URL is: " + redirectUrl + " for the relaying party - " + relyingParty +
                            " for " + "sessionDataKey: " + sessionDataKey);
                }
                result.addProperty("status", "redirect");
                result.addProperty("redirectUrl", redirectUrl);
                response.getWriter().write(result.toString());
            }
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
     * @param appName
     * @param tenantDomain
     * @return
     */
    private static String getRedirectURL(String appName, String relyingParty, String tenantDomain) {

        ServiceProvider sp = null;
        String redirectUrl = null;
        if (StringUtils.isNotEmpty(appName)) {
            try {
                appName = URLDecoder.decode(appName, "UTF-8");
                sp = ApplicationManagementService.getInstance().getServiceProvider(appName, tenantDomain);
                if (sp != null) {
                    redirectUrl = sp.getAccessUrl();
                }
            } catch (IdentityApplicationManagementException e) {
                log.error("Unable to retrieve an application with name: " + appName, e);
            } catch (UnsupportedEncodingException e) {
                log.error("Error while decoding application name: " + appName, e);
            }
        }

        if ("My Account".equals(appName)) {
            redirectUrl = getMyAccountURL(redirectUrl);
        } else if ("Console".equals(appName)) {
            redirectUrl = getConsoleURL(redirectUrl);
        } else if (StringUtils.isEmpty(redirectUrl) && StringUtils.isNotEmpty(relyingParty)) {
            // If access URL is not configured for the application, retrieve the URL from SP_REDIRECT_URL_RESOURCE_PATH.
            // Eventually, this step need to be retried.
            if (log.isDebugEnabled()) {
                log.debug("Unable to find access URL for the application: " + appName + ". Fallback to " +
                        "searching the registry for the redirect url for relyingParty: " + relyingParty);
            }
            redirectUrl = getRelyingPartyRedirectUrl(relyingParty, tenantDomain);

            // Migrate the redirect URL from SP_REDIRECT_URL_RESOURCE_PATH to the application as access URL.
            migrateRedirectURLFromRegistryToApplication(relyingParty, tenantDomain, sp, redirectUrl);
        }
        return redirectUrl;
    }

    /**
     * If the relying party is a valid inbound authenticator configured in the application, then update the
     * applications access URL with the redirect URL defined in the registry (SP_REDIRECT_URL_RESOURCE_PATH) for
     * relying party
     * @param relyingParty
     * @param tenantDomain
     * @param sp
     * @param redirectUrl
     */
    private static void migrateRedirectURLFromRegistryToApplication(String relyingParty, String tenantDomain,
                                                                    ServiceProvider sp, String redirectUrl) {

        if (sp != null && StringUtils.isNotEmpty(redirectUrl)) {
            InboundAuthenticationConfig inboundAuthenticationConfig = sp.getInboundAuthenticationConfig();
            InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfig = inboundAuthenticationConfig
                    .getInboundAuthenticationRequestConfigs();
            for (InboundAuthenticationRequestConfig inboundAuth : inboundAuthenticationRequestConfig) {
                if (relyingParty.equals(inboundAuth.getInboundAuthKey())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Updating the application: " + sp.getApplicationName() + " access URL with " +
                                "redirect URL: " + redirectUrl + " configured for relyingParty: " + relyingParty);
                    }
                    try {
                        sp.setAccessUrl(redirectUrl);
                        ApplicationManagementService.getInstance().updateApplication(sp, tenantDomain, sp.getOwner()
                                .getUserName());
                    } catch (IdentityApplicationManagementException e) {
                        log.error("Unable to update the application: " + sp.getApplicationName() + " with " +
                                "accessURL:"+ relyingParty, e);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Returns the access URL of the application as the redirect url.
     * @param appName
     * @param tenantDomain
     * @return
     */
    public static String getAccessURLFromApplication(String appName, String tenantDomain) {

        ServiceProvider sp;
        String redirectUrl = null;
        if (StringUtils.isNotEmpty(appName)) try {
            sp = ApplicationManagementService.getInstance().getServiceProvider(appName, tenantDomain);
            if (sp != null) {
                redirectUrl = sp.getAccessUrl();
            }
        } catch (IdentityApplicationManagementException e) {
            log.error("Unable to retrieve an application with name: " + appName, e);
        }
        return redirectUrl;
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
            log.debug("retrieving configured url against relying party : " + relyingParty + "for tenant domain : " +
                    tenantDomain);
        }

        int tenantId;
        if (StringUtils.isEmpty(tenantDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain is not available. Hence using super tenant domain");
            }
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        } else {
            tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        }
        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
            Registry registry = IdentityTenantUtil.getConfigRegistry(tenantId);
            if (registry.resourceExists(SP_REDIRECT_URL_RESOURCE_PATH)) {
                Resource resource = registry.get(SP_REDIRECT_URL_RESOURCE_PATH);
                if (resource != null) {
                    String redirectUrl = resource.getProperty(relyingParty);
                    if (StringUtils.isNotEmpty(redirectUrl)) {
                        return redirectUrl;
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while getting data from the registry.", e);
        } catch (IdentityException e) {
            log.error("Error while getting the tenant domain from tenant id : " + tenantId, e);
        }
        return null;
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
}
