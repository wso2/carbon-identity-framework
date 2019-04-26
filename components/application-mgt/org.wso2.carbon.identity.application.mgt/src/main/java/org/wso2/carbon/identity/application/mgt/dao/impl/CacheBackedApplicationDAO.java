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

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheKey;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderByIDCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderByInboundAuthCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderCacheInboundAuthEntry;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderCacheInboundAuthKey;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderIDCacheEntry;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderIDCacheKey;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cached DAO layer for the application management. All the DAO access has to be happen through this layer to ensure
 * single point of caching.
 */
public class CacheBackedApplicationDAO extends AbstractApplicationDAOImpl {

    private static final Log log = LogFactory.getLog(CacheBackedApplicationDAO.class);

    private ApplicationDAO appDAO;

    private static IdentityServiceProviderCache appCacheByName = null;
    private static ServiceProviderByInboundAuthCache appCacheByInboundAuth = null;
    private static ServiceProviderByIDCache appCacheByID = null;

    public CacheBackedApplicationDAO(ApplicationDAO appDAO) {

        this.appDAO = appDAO;
        appCacheByName = IdentityServiceProviderCache.getInstance();
        appCacheByInboundAuth = ServiceProviderByInboundAuthCache.getInstance();
        appCacheByID = ServiceProviderByIDCache.getInstance();
    }

    public ServiceProvider getApplication(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        ServiceProvider serviceProvider = getApplicationFromCache(applicationName, tenantDomain);
        if (serviceProvider == null) {
            try {
                serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
                if (serviceProvider != null) {
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
            if (serviceProvider == null) {
                throw new IdentityApplicationManagementException(
                        "Error while getting the service provider for appId: " + appId);
            }
            addToCache(serviceProvider, serviceProvider.getOwner().getTenantDomain());
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

        if (StringUtils.isEmpty(clientId)) {
            return null;
        }

        String appName = null;
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            if (tenantDomain != null) {
                ServiceProviderCacheInboundAuthKey cacheKey = new ServiceProviderCacheInboundAuthKey(clientId, type,
                        tenantDomain);
                ServiceProviderCacheInboundAuthEntry entry = appCacheByInboundAuth.getValueFromCache(cacheKey);
                if (entry != null) {
                    appName = entry.getServiceProviderName();
                }
            }
            if (appName == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Inbound Auth Key Cache is missing for " + clientId);
                }
                appName = appDAO.getServiceProviderNameByClientId(clientId, type, tenantDomain);
                if (tenantDomain != null) {
                    ServiceProviderCacheInboundAuthKey clientKey = new ServiceProviderCacheInboundAuthKey(clientId,
                            type, tenantDomain);
                    ServiceProviderCacheInboundAuthEntry clientEntry = new ServiceProviderCacheInboundAuthEntry(appName,
                            tenantDomain);
                    appCacheByInboundAuth.addToCache(clientKey, clientEntry);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Inbound Auth Key Cache is present for " + clientId);
                }
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
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

        String storedAppName = getApplicationName(serviceProvider.getApplicationID());
        clearAllAppCache(serviceProvider, storedAppName, tenantDomain);
        appDAO.updateApplication(serviceProvider, tenantDomain);

    }

    public void deleteApplication(String applicationName) throws IdentityApplicationManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        ServiceProvider serviceProvider = getApplication(applicationName, tenantDomain);
        clearAllAppCache(serviceProvider, tenantDomain);
        appDAO.deleteApplication(applicationName);
    }

    public ApplicationBasicInfo[] getAllApplicationBasicInfo() throws IdentityApplicationManagementException {

        return getApplicationBasicInfo("*");
    }

    public ApplicationBasicInfo[] getApplicationBasicInfo(String filter) throws IdentityApplicationManagementException {

        // No need to cache the returned list.
        return ((AbstractApplicationDAOImpl) appDAO).getApplicationBasicInfo(filter);
    }

    public Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName, String
            tenantDomain) throws IdentityApplicationManagementException {

        ServiceProvider applicationFromCache = getApplicationFromCache(serviceProviderName, tenantDomain);
        if (applicationFromCache != null) {
            Map<String, String> localIdPToSPClaimMapping = new HashMap<>();
            ClaimConfig claimConfig = applicationFromCache.getClaimConfig();
            ClaimMapping[] claimMappings = claimConfig.getClaimMappings();
            for (ClaimMapping claimMapping : claimMappings) {
                localIdPToSPClaimMapping.put(claimMapping.getRemoteClaim().getClaimUri(),
                        claimMapping.getLocalClaim().getClaimUri());
            }
            return localIdPToSPClaimMapping;
        }
        return appDAO.getServiceProviderToLocalIdPClaimMapping(serviceProviderName, tenantDomain);
    }

    public Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName, String
            tenantDomain) throws IdentityApplicationManagementException {

        ServiceProvider applicationFromCache = getApplicationFromCache(serviceProviderName, tenantDomain);
        if (applicationFromCache != null) {
            Map<String, String> localIdPToSPClaimMapping = new HashMap<>();
            ClaimConfig claimConfig = applicationFromCache.getClaimConfig();
            ClaimMapping[] claimMappings = claimConfig.getClaimMappings();
            for (ClaimMapping claimMapping : claimMappings) {
                localIdPToSPClaimMapping.put(claimMapping.getLocalClaim().getClaimUri(),
                        claimMapping.getRemoteClaim().getClaimUri());
            }
            return localIdPToSPClaimMapping;
        }
        return appDAO.getLocalIdPToServiceProviderClaimMapping(serviceProviderName, tenantDomain);
    }

    public List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider applicationFromCache = getApplicationFromCache(serviceProviderName, tenantDomain);
        if (applicationFromCache != null) {
            List<String> requestedLocalClaims = new ArrayList<>();
            ClaimConfig claimConfig = applicationFromCache.getClaimConfig();
            ClaimMapping[] claimMappings = claimConfig.getClaimMappings();
            for (ClaimMapping claimMapping : claimMappings) {
                if (claimMapping.isRequested()) {
                    requestedLocalClaims.add(claimMapping.getLocalClaim().getClaimUri());
                }
            }
            return requestedLocalClaims;
        }
        return appDAO.getAllRequestedClaimsByServiceProvider(serviceProviderName, tenantDomain);
    }

    private void addToCache(ServiceProvider serviceProvider, String tenantDomain) throws
            IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Add cache for the application " + serviceProvider.getApplicationName() + "@" + tenantDomain);
        }
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            IdentityServiceProviderCacheKey nameKey = new IdentityServiceProviderCacheKey(serviceProvider
                    .getApplicationName(), tenantDomain);
            IdentityServiceProviderCacheEntry nameEntry = new IdentityServiceProviderCacheEntry(serviceProvider);
            appCacheByName.addToCache(nameKey, nameEntry);

            ServiceProviderIDCacheKey idKey = new ServiceProviderIDCacheKey(serviceProvider.getApplicationID());
            ServiceProviderIDCacheEntry idEntry = new ServiceProviderIDCacheEntry(serviceProvider);
            appCacheByID.addToCache(idKey, idEntry);

            if (serviceProvider.getInboundAuthenticationConfig() != null && serviceProvider
                    .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {
                InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                        .getInboundAuthenticationRequestConfigs();
                for (InboundAuthenticationRequestConfig config : configs) {
                    ServiceProviderCacheInboundAuthKey clientKey = new ServiceProviderCacheInboundAuthKey(
                            config.getInboundAuthKey(), config.getInboundAuthType(), tenantDomain);
                    ServiceProviderCacheInboundAuthEntry clientEntry = new ServiceProviderCacheInboundAuthEntry(
                            serviceProvider.getApplicationName(), tenantDomain);
                    appCacheByInboundAuth.addToCache(clientKey, clientEntry);
                }
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }
    }

    private ServiceProvider getApplicationFromCache(int appId) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ServiceProviderIDCacheKey cacheKey = new ServiceProviderIDCacheKey(appId);
            ServiceProviderIDCacheEntry entry = appCacheByID.getValueFromCache(cacheKey);

            if (entry != null) {
                serviceProvider = entry.getServiceProvider();
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }
        if (serviceProvider == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache missing for the application with id " + appId);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache present for the application with id " + appId);
            }
        }
        return serviceProvider;
    }

    private ServiceProvider getApplicationFromCache(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    applicationName, tenantDomain);
            IdentityServiceProviderCacheEntry entry = appCacheByName.getValueFromCache(cacheKey);

            if (entry != null) {
                serviceProvider = entry.getServiceProvider();
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }
        if (serviceProvider == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache missing for the application " + applicationName + "@" + tenantDomain);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache is present for the application " + applicationName + "@" + tenantDomain);
            }
        }
        return serviceProvider;
    }

    private void clearAllAppCache(ServiceProvider serviceProvider, String tenantDomain) throws
            IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Clearing all the Service Provider Caches for " + serviceProvider.getApplicationName() + "@" +
                    tenantDomain);
        }
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    serviceProvider.getApplicationName(), tenantDomain);
            appCacheByName.clearCacheEntry(cacheKey);

            ServiceProviderIDCacheKey idKey = new ServiceProviderIDCacheKey(serviceProvider.getApplicationID());
            appCacheByID.clearCacheEntry(idKey);

            clearAppCacheByInboundKey(serviceProvider, tenantDomain);
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }
    }

    private void clearAllAppCache(ServiceProvider serviceProvider, String storedAppName, String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(storedAppName, tenantDomain);
            appCacheByName.clearCacheEntry(cacheKey);

            cacheKey = new IdentityServiceProviderCacheKey(serviceProvider.getApplicationName(), tenantDomain);
            appCacheByName.clearCacheEntry(cacheKey);

            ServiceProviderIDCacheKey idKey = new ServiceProviderIDCacheKey(serviceProvider.getApplicationID());
            appCacheByID.clearCacheEntry(idKey);

            clearAppCacheByInboundKey(serviceProvider, tenantDomain);
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }

    }

    private void clearAppCacheByInboundKey(ServiceProvider serviceProvider, String tenantDomain) {

        if (serviceProvider.getInboundAuthenticationConfig() != null && serviceProvider
                .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {
            InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                    .getInboundAuthenticationRequestConfigs();
            for (InboundAuthenticationRequestConfig config : configs) {
                ServiceProviderCacheInboundAuthKey clientKey = new ServiceProviderCacheInboundAuthKey(
                        config.getInboundAuthKey(), config.getInboundAuthType(), tenantDomain);
                appCacheByInboundAuth.clearCacheEntry(clientKey);
            }
        }
    }
}
