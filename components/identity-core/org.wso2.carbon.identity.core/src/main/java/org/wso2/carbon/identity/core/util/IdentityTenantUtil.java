/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.core.util;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.AuthenticationObserver;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;

public class IdentityTenantUtil {

    private static RealmService realmService;
    private static RegistryService registryService;
    private static Log log = LogFactory.getLog(IdentityTenantUtil.class);
    private static TenantRegistryLoader tenantRegistryLoader;
    private static BundleContext bundleContext;
    protected static ConcurrentHashMap<Integer,Boolean> tenantIdMap = new ConcurrentHashMap<Integer,Boolean>();

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    public static void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        IdentityTenantUtil.tenantRegistryLoader = tenantRegistryLoader;
    }

    public static Registry getConfigRegistry(int tenantId) throws RegistryException {
        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static Registry getRegistry(String domainName, String username) throws IdentityException {
        HttpSession httpSess = getHttpSession();

        if (httpSess != null) {
            if (httpSess.getAttribute(ServerConstants.USER_LOGGED_IN) != null) {
                try {
                    return AdminServicesUtil.getSystemRegistry();
                } catch (CarbonException e) {
                    log.error("Error obtaining a registry instance", e);
                    throw IdentityException.error(
                            "Error obtaining a registry instance", e);
                }
            }
        }
        return getRegistryForAnonymousSession(domainName, username);
    }

    @SuppressWarnings("deprecation")
    public static Registry getRegistry() throws IdentityException {
        try {
            return AdminServicesUtil.getSystemRegistry();
        } catch (CarbonException e) {
            log.error("Error obtaining a registry instance", e);
            throw IdentityException.error("Error obtaining a registry instance", e);
        }
    }

    public static UserRealm getRealm(String domainName, String username) throws IdentityException {
        return getRealmForAnonymousSession(domainName, username);
    }

    @SuppressWarnings("deprecation")
    private static Registry getRegistryForAnonymousSession(String domainName, String username)
            throws IdentityException {
        try {
            if (domainName == null && username == null) {
                domainName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            if (username == null) {
                return AnonymousSessionUtil.getSystemRegistryByDomainName(registryService,
                        realmService, domainName);
            } else {
                return AnonymousSessionUtil.getSystemRegistryByUserName(registryService,
                        realmService, username);
            }
        } catch (CarbonException e) {
            log.error("Error obtaining a registry instance", e);
            throw IdentityException.error("Error obtaining a registry instance", e);
        }
    }

    private static UserRealm getRealmForAnonymousSession(String domainName, String username)
            throws IdentityException {

        try {
            if (username != null) {
                return AnonymousSessionUtil.getRealmByUserName(registryService, realmService,
                        username);
            }

            if (domainName == null) {
                domainName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }

            return AnonymousSessionUtil.getRealmByTenantDomain(registryService, realmService,
                    domainName);

        } catch (CarbonException e) {
            throw IdentityException.error("Error Obtaining a realm for user name: " + username + " and " +
                    "domain:" + domainName, e);
        }
    }

    public static String getGlobalUserName(String userName) {

        if (userName != null && userName.indexOf("@") > 0) {
            return userName;
        }

        String domain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (domain != null) {
            return userName + "@" + domain;
        }
        return userName;
    }

    private static HttpSession getHttpSession() {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        HttpSession httpSess = null;
        if (msgCtx != null) {
            HttpServletRequest request = (HttpServletRequest) msgCtx
                    .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
            httpSess = request.getSession();
        }
        return httpSess;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        IdentityTenantUtil.registryService = registryService;
    }

    public static BundleContext getBundleContext() {
        return IdentityTenantUtil.bundleContext;
    }

    public static void setBundleContext(BundleContext bundleContext) {
        IdentityTenantUtil.bundleContext = bundleContext;
    }

    /**
     *
     * @return
     * @deprecated Please use OSGI wiring to get the realm service for your component.
     */
    @Deprecated
    public static RealmService getRealmService() {
        return realmService;
    }

    @Deprecated
    public static void setRealmService(RealmService realmService) {
        IdentityTenantUtil.realmService = realmService;
    }

    /**
     * @deprecated
     *
     * This method will be removed in the upcoming major release.
     * Because, tenant domain can be retrieved using tenantId.
     * Use {@link #initializeRegistry(int)} instead.
     *
     */
    @Deprecated
    public static void initializeRegistry(int tenantId, String tenantDomain) throws IdentityException {
        initializeRegistry(tenantId);
    }

    public static void initializeRegistry(int tenantId) throws IdentityException {
        String tenantDomain = getTenantDomain(tenantId);

        if (tenantIdMap.get(tenantId) == null || !tenantIdMap.get(tenantId)) {

            if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    carbonContext.setTenantDomain(tenantDomain, true);
                    BundleContext bundleContext = IdentityTenantUtil.getBundleContext();
                    if (bundleContext != null) {
                        ServiceTracker tracker = new ServiceTracker(bundleContext, AuthenticationObserver.class.getName(), null);
                        tracker.open();
                        Object[] services = tracker.getServices();
                        if (services != null) {
                            for (Object service : services) {
                                ((AuthenticationObserver) service).startedAuthentication(tenantId);
                            }
                        }
                        tracker.close();
                        try {
                            IdentityTenantUtil.getTenantRegistryLoader().loadTenantRegistry(tenantId);
                        } catch (RegistryException e) {
                            throw IdentityException.error("Error loading tenant registry for tenant domain " + tenantDomain, e);
                        }
                        try {
                            registryService.getGovernanceSystemRegistry(tenantId);
                            tenantIdMap.put(tenantId, true);
                        } catch (RegistryException e) {
                            throw IdentityException.error("Error obtaining governance system registry for tenant domain " +
                                    tenantDomain, e);
                        }
                    }
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        }
    }

    public static int getTenantId(String tenantDomain) throws IdentityRuntimeException {

        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        try {
            if (realmService != null) {
                tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            }
        } catch (UserStoreException e) {
            // Ideally user.core should be throwing an unchecked exception, in which case no need to wrap at this
            // level once more without adding any valuable contextual information. Because we don't have exception
            // enrichment properly implemented, we are appending the error message from the UserStoreException to the
            // new message
            throw IdentityRuntimeException.error("Error occurred while retrieving tenantId for tenantDomain: " +
                    tenantDomain + e.getMessage(), e);
        }
        if(tenantId == MultitenantConstants.INVALID_TENANT_ID){
            throw IdentityRuntimeException.error("Invalid tenant domain " + tenantDomain);
        } else {
            return tenantId;
        }

    }

    public static String getTenantDomain(int tenantId) throws IdentityRuntimeException {

        String tenantDomain = null;
        try {
            tenantDomain = realmService.getTenantManager().getDomain(tenantId);
        } catch (UserStoreException e) {
            // Ideally user.core should be throwing an unchecked exception, in which case no need to wrap at this
            // level once more without adding any valuable contextual information. Because we don't have exception
            // enrichment properly implemented, we are appending the error message from the UserStoreException to the
            // new message
            throw IdentityRuntimeException.error("Error occurred while retrieving tenantDomain for tenantId: " +
                    tenantId + e.getMessage(), e);
        }
        if (tenantDomain == null) {
            throw IdentityRuntimeException.error("Can not find the tenant domain for the tenant id " + tenantId);
        } else {
            return tenantDomain;
        }

    }

    /**
     * Get the tenant id of the given user.
     *
     * @param username Username
     * @return Tenant Id of domain user belongs to.
     * @throws IdentityRuntimeException Error when getting the tenant Id from tenant domain
     */
    public static int getTenantIdOfUser(String username) throws IdentityRuntimeException {

        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        String domainName = MultitenantUtils.getTenantDomain(username);
        if (domainName != null) {
            try {
                TenantManager tenantManager = IdentityTenantUtil.getRealmService().getTenantManager();
                tenantId = tenantManager.getTenantId(domainName);
            } catch (UserStoreException e) {
                String errorMsg = "Error when getting the tenant id from the tenant domain : " + domainName;
                throw IdentityRuntimeException.error(errorMsg, e);
            }
        }
        if(tenantId == MultitenantConstants.INVALID_TENANT_ID){
            throw IdentityRuntimeException.error("Invalid tenant domain of user " + username);
        } else {
            return tenantId;
        }
    }

    /**
     * Get the tenant name from the thread local properties.
     *
     * @return Tenant name from the context.
     */
    public static String getTenantDomainFromContext() {

        return (String) IdentityUtil.threadLocalProperties.get().get(IdentityCoreConstants.TENANT_NAME_FROM_CONTEXT);
    }

    /**
     * Checks whether the tenant URL support is enabled.
     *
     * @return true if the config is set to true, false otherwise.
     */
    public static boolean isTenantQualifiedUrlsEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(IdentityCoreConstants.ENABLE_TENANT_QUALIFIED_URLS));
    }


    /**
     * Checks if the tenanted session support is enabled.
     * @return true if tenanted session support is enabled, false otherwise
     */
    public static boolean isTenantedSessionsEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(IdentityCoreConstants.ENABLE_TENANTED_SESSIONS));
    }

    /**
     *
     * Checks whether legacy SaaS authentication is enabled.
     *
     * If enabled and if the username provided during the SaaS application authentication does not have a tenant
     * domain appended, the user will be treated as a super tenant user and will be authenticated against the super
     * tenant domain.
     *
     * If disabled and if the username provided during the SaaS application authentication does not have a tenant
     * domain appended, the user will be treated as a application tenant domain user and will be authenticated
     * against the application tenant domain.
     *
     * @return true if legacy SaaS authentication is enabled, false otherwise.
     */
    public static boolean isLegacySaaSAuthenticationEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(IdentityCoreConstants.ENABLE_LEGACY_SAAS_AUTHENTICATION));
    }
}
