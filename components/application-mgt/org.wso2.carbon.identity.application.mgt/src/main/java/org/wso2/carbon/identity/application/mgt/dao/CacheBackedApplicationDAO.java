/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.mgt.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheKey;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderClientCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderClientIDCache;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderClientIDCacheKey;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderIDCache;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderIDCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderIDCacheKey;
import org.wso2.carbon.identity.application.mgt.dao.impl.AbstractApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.Map;

/**
 * Cached DAO layer for the application management. All the DAO access has to be happen through this layer to ensure
 * single point of caching.
 */
public class CacheBackedApplicationDAO {

    private static final Log log = LogFactory.getLog(CacheBackedApplicationDAO.class);
    private static final String OAUTH2 = "oauth2";

    private ApplicationDAO appDAO;

    private static IdentityServiceProviderCache appCacheByName = null;
    private static ServiceProviderClientIDCache appCacheByClientId = null;
    private static ServiceProviderIDCache appCacheByID = null;

    public CacheBackedApplicationDAO(ApplicationDAO appDAO) {

        this.appDAO = appDAO;
        appCacheByName = IdentityServiceProviderCache.getInstance();
        appCacheByClientId = ServiceProviderClientIDCache.getInstance();
        appCacheByID = ServiceProviderIDCache.getInstance();
    }

    public ApplicationDAO getAppDAO() {

        return appDAO;
    }

    public ServiceProvider getApplication(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        ServiceProvider serviceProvider = getApplicationFromCache(applicationName, tenantDomain);
        if (serviceProvider == null) {
            try {
                serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
                if (serviceProvider != null) {
                    loadApplicationPermissions(applicationName, serviceProvider);
                    addToCache(serviceProvider, tenantDomain);
                }
            } catch (Exception e) {
                String error = "Error occurred while retrieving the application, " + applicationName;
                log.error(error, e);
                throw new IdentityApplicationManagementException(error, e);
            }
        }
        return serviceProvider;
    }

    public ServiceProvider getApplication(int appId) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = getApplicationFromCache(appId);
        if (serviceProvider == null) {
            serviceProvider = appDAO.getApplication(appId);
            addToCache(serviceProvider, null);
        }
        if (serviceProvider == null) {
            throw new IdentityApplicationManagementException(
                    "Error while getting the service provider for appId: " + appId);
        }
        String serviceProviderName = serviceProvider.getApplicationName();
        String tenantDomain = serviceProvider.getOwner().getTenantDomain();

        try {
            startTenantFlow(tenantDomain);
            loadApplicationPermissions(serviceProviderName, serviceProvider);
        } finally {
            endTenantFlow();
        }
        return serviceProvider;
    }

    public String getApplicationName(int applicationID) throws IdentityApplicationManagementException {

        ServiceProvider applicationFromCache = getApplicationFromCache(applicationID);
        if (applicationFromCache != null) {
            return applicationFromCache.getApplicationName();
        }
        return appDAO.getApplicationName(applicationID);
    }

    public String getServiceProviderNameByClientId(String clientId, String type, String
            tenantDomain) throws IdentityApplicationManagementException {

        String appName = null;
        try {
            startTenantFlow(tenantDomain);
            ServiceProviderClientIDCacheKey cacheKey = new ServiceProviderClientIDCacheKey(clientId);
            ServiceProviderClientCacheEntry entry = appCacheByClientId.getValueFromCache(cacheKey);
            if (entry != null) {
                appName = entry.getServiceProviderName();
            }
        } finally {
            endTenantFlow();
        }
        if (appName == null) {
            appName = appDAO.getServiceProviderNameByClientId(clientId, type, tenantDomain);
            ServiceProviderClientIDCacheKey clientKey = new ServiceProviderClientIDCacheKey(clientId);
            ServiceProviderClientCacheEntry clientEntry = new ServiceProviderClientCacheEntry(appName, tenantDomain);
            appCacheByClientId.addToCache(clientKey, clientEntry);
        }
        return appName;
    }

    public boolean isApplicationExists(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        ServiceProvider applicationFromCache = getApplicationFromCache(applicationName, tenantDomain);
        if (applicationFromCache != null) {
            return true;
        } else {
            return appDAO.isApplicationExists(applicationName, tenantDomain);
        }
    }

    public int createApplication(ServiceProvider serviceProvider, String tenantDomain) throws
            IdentityApplicationManagementException {

        return appDAO.createApplication(serviceProvider, tenantDomain);
    }

    public void updateApplication(ServiceProvider serviceProvider, String tenantDomain) throws
            IdentityApplicationManagementException {

        clearAllAppCache(serviceProvider, tenantDomain);
        appDAO.updateApplication(serviceProvider, tenantDomain);

    }

    public void deleteApplication(String applicationName) throws IdentityApplicationManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        ServiceProvider serviceProvider = getApplication(applicationName, tenantDomain);
        appDAO.deleteApplication(applicationName);
        clearAllAppCache(serviceProvider, tenantDomain);
    }

    public ApplicationBasicInfo[] getAllApplicationBasicInfo() throws IdentityApplicationManagementException {

        return getApplicationBasicInfo("*");
    }

    public ApplicationBasicInfo[] getApplicationBasicInfo(String filter) throws IdentityApplicationManagementException {

        if (!(appDAO instanceof AbstractApplicationDAOImpl)) {
            log.error("Get application basic info service is not supported.");
            throw new IdentityApplicationManagementException("This service is not supported.");
        }
        return ((AbstractApplicationDAOImpl) appDAO).getApplicationBasicInfo(filter);
    }

    public Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName, String
            tenantDomain) throws IdentityApplicationManagementException {

        return appDAO.getServiceProviderToLocalIdPClaimMapping(serviceProviderName, tenantDomain);
    }

    public Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName, String
            tenantDomain) throws IdentityApplicationManagementException {

        return appDAO.getLocalIdPToServiceProviderClaimMapping(serviceProviderName, tenantDomain);
    }

    public List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName, String tenantDomain)
            throws IdentityApplicationManagementException {

        return appDAO.getAllRequestedClaimsByServiceProvider(serviceProviderName, tenantDomain);
    }

    private void startTenantFlow(String tenantDomain) throws IdentityApplicationManagementException {

        int tenantId;
        try {
            tenantId = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error when setting tenant domain. ", e);
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
    }

    private void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    private void loadApplicationPermissions(String serviceProviderName, ServiceProvider serviceProvider)
            throws IdentityApplicationManagementException {

        List<ApplicationPermission> permissionList = ApplicationMgtUtil.loadPermissions(serviceProviderName);

        if (permissionList != null) {
            PermissionsAndRoleConfig permissionAndRoleConfig;
            if (serviceProvider.getPermissionAndRoleConfig() == null) {
                permissionAndRoleConfig = new PermissionsAndRoleConfig();
            } else {
                permissionAndRoleConfig = serviceProvider.getPermissionAndRoleConfig();
            }
            permissionAndRoleConfig.setPermissions(permissionList.toArray(new ApplicationPermission[0]));
            serviceProvider.setPermissionAndRoleConfig(permissionAndRoleConfig);
        }
    }

    private void addToCache(ServiceProvider serviceProvider, String tenantDomain) {

        IdentityServiceProviderCacheKey nameKey = new IdentityServiceProviderCacheKey(serviceProvider
                .getApplicationName(), tenantDomain);
        IdentityServiceProviderCacheEntry nameEntry = new IdentityServiceProviderCacheEntry(serviceProvider);
        appCacheByName.addToCache(nameKey, nameEntry);

        ServiceProviderIDCacheKey idKey = new ServiceProviderIDCacheKey(serviceProvider.getApplicationID());
        ServiceProviderIDCacheEntry idEntry = new ServiceProviderIDCacheEntry(serviceProvider.getApplicationName(),
                tenantDomain);
        appCacheByID.addToCache(idKey, idEntry);

        String clientID = getClientID(serviceProvider);
        if (clientID != null) {
            ServiceProviderClientIDCacheKey clientKey = new ServiceProviderClientIDCacheKey(clientID);
            ServiceProviderClientCacheEntry clientEntry = new ServiceProviderClientCacheEntry(serviceProvider
                    .getApplicationName(), tenantDomain);
            appCacheByClientId.addToCache(clientKey, clientEntry);
        }
    }

    private String getClientID(ServiceProvider serviceProvider) {

        if (serviceProvider != null &&
                serviceProvider.getInboundAuthenticationConfig() != null &&
                serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {
            InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();
            for (InboundAuthenticationRequestConfig config : configs) {
                if (OAUTH2.equalsIgnoreCase(config.getInboundAuthType()) && config.getInboundAuthKey() != null) {
                    return config.getInboundAuthKey();
                }
            }
        }
        return null;
    }

    private ServiceProvider getApplicationFromCache(int appId) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ServiceProviderIDCacheKey cacheKey = new ServiceProviderIDCacheKey(appId);
            ServiceProviderIDCacheEntry entry = appCacheByID.getValueFromCache(cacheKey);

            if (entry != null) {
                serviceProvider = getApplicationFromCache(entry.getServiceProviderName(), entry.getTenantName());
            }
        } finally {
            endTenantFlow();
        }
        return serviceProvider;
    }

    private ServiceProvider getApplicationFromCache(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        try {
            startTenantFlow(tenantDomain);
            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    applicationName, tenantDomain);
            IdentityServiceProviderCacheEntry entry = appCacheByName.getValueFromCache(cacheKey);

            if (entry != null) {
                serviceProvider = entry.getServiceProvider();
            }
        } finally {
            endTenantFlow();
        }
        return serviceProvider;
    }

    private void clearAllAppCache(ServiceProvider application, String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    application.getApplicationName(), tenantDomain);
            appCacheByName.clearCacheEntry(cacheKey);

            ServiceProviderIDCacheKey idKey = new ServiceProviderIDCacheKey(application.getApplicationID());
            appCacheByID.clearCacheEntry(idKey);

            String clientID = getClientID(application);
            if (clientID != null) {
                ServiceProviderClientIDCacheKey clientKey = new ServiceProviderClientIDCacheKey(clientID);
                appCacheByClientId.clearCacheEntry(clientKey);
            }
        } finally {
            endTenantFlow();
        }
    }
}
