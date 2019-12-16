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
import org.wso2.carbon.identity.application.mgt.dao.PaginatableFilterableApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.cache.ApplicationBasicInfoByResourceIdCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ApplicationBasicInfoResourceIdCacheEntry;
import org.wso2.carbon.identity.application.mgt.internal.cache.ApplicationBasicInfoResourceIdCacheKey;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderByIDCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderByInboundAuthCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderByResourceIdCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderCacheInboundAuthEntry;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderCacheInboundAuthKey;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderIDCacheEntry;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderIDCacheKey;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderResourceIdCacheEntry;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderResourceIdCacheKey;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cached DAO layer for the application management. All the DAO access has to be happen through this layer to ensure
 * single point of caching.
 */
public class CacheBackedApplicationDAO extends ApplicationDAOImpl {

    private static final Log log = LogFactory.getLog(CacheBackedApplicationDAO.class);

    private ApplicationDAO appDAO;

    private static IdentityServiceProviderCache appCacheByName = null;
    private static ServiceProviderByInboundAuthCache appCacheByInboundAuth = null;
    private static ServiceProviderByIDCache appCacheByID = null;
    private static ServiceProviderByResourceIdCache appCacheByResourceId = null;
    private static ApplicationBasicInfoByResourceIdCache appBasicInfoCacheByResourceId = null;

    public CacheBackedApplicationDAO(ApplicationDAO appDAO) {

        this.appDAO = appDAO;
        appCacheByName = IdentityServiceProviderCache.getInstance();
        appCacheByInboundAuth = ServiceProviderByInboundAuthCache.getInstance();
        appCacheByID = ServiceProviderByIDCache.getInstance();
        appCacheByResourceId = ServiceProviderByResourceIdCache.getInstance();
        appBasicInfoCacheByResourceId = ApplicationBasicInfoByResourceIdCache.getInstance();
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

    public int createApplication(ServiceProvider application, String tenantDomain) throws
            IdentityApplicationManagementException {

        return appDAO.createApplication(application, tenantDomain);
    }

    public void updateApplication(ServiceProvider serviceProvider, String tenantDomain) throws
            IdentityApplicationManagementException {

        ServiceProvider storedApp = getApplication(serviceProvider.getApplicationID());
        clearAllAppCache(storedApp, tenantDomain);
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

        if (appDAO instanceof PaginatableFilterableApplicationDAO) {
            // No need to cache the returned list.
            return ((PaginatableFilterableApplicationDAO) appDAO).getApplicationBasicInfo(filter);
        } else {
            throw new UnsupportedOperationException("Get application basic info with filter not supported.");
        }
    }

    public ApplicationBasicInfo[] getAllPaginatedApplicationBasicInfo(int pageNumber)
            throws IdentityApplicationManagementException {

        if (appDAO instanceof PaginatableFilterableApplicationDAO) {
            // No need to cache the returned list.
            return ((PaginatableFilterableApplicationDAO) appDAO).getAllPaginatedApplicationBasicInfo(pageNumber);
        } else {
            throw new UnsupportedOperationException("This operation only supported in" +
                    " PaginatableFilterableApplicationDAO only.");
        }
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(int offset, int limit) throws IdentityApplicationManagementException {

        if (appDAO instanceof PaginatableFilterableApplicationDAO) {
            // No need to cache the returned list.
            return ((PaginatableFilterableApplicationDAO) appDAO).getApplicationBasicInfo(offset, limit);
        } else {
            throw new UnsupportedOperationException("This operation only supported in" +
                    " PaginatableFilterableApplicationDAO only.");
        }
    }

    public ApplicationBasicInfo[] getPaginatedApplicationBasicInfo(int pageNumber, String filter)
            throws IdentityApplicationManagementException {

        if (appDAO instanceof PaginatableFilterableApplicationDAO) {
            // No need to cache the returned list.
            return ((PaginatableFilterableApplicationDAO) appDAO).getPaginatedApplicationBasicInfo(pageNumber, filter);
        } else {
            throw new UnsupportedOperationException("This operation only supported in" +
                    " PaginatableFilterableApplicationDAO only.");
        }
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String filter, int offset, int limit)
            throws IdentityApplicationManagementException {

        if (appDAO instanceof PaginatableFilterableApplicationDAO) {
            // No need to cache the returned list.
            return ((PaginatableFilterableApplicationDAO) appDAO).getApplicationBasicInfo(filter, offset, limit);
        } else {
            throw new UnsupportedOperationException("This operation only supported in" +
                    " PaginatableFilterableApplicationDAO only.");
        }
    }

    public int getCountOfAllApplications() throws IdentityApplicationManagementException {

        if (appDAO instanceof PaginatableFilterableApplicationDAO) {
            return ((PaginatableFilterableApplicationDAO) appDAO).getCountOfAllApplications();
        } else {
            throw new UnsupportedOperationException("This operation only supported in" +
                    " PaginatableFilterableApplicationDAO only.");
        }
    }

    public int getCountOfApplications(String filter) throws IdentityApplicationManagementException {

        if (appDAO instanceof PaginatableFilterableApplicationDAO) {
            return ((PaginatableFilterableApplicationDAO) appDAO).getCountOfApplications(filter);
        } else {
            throw new UnsupportedOperationException("This operation only supported in" +
                    " PaginatableFilterableApplicationDAO only.");
        }
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

    @Override
    public ApplicationBasicInfo getApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationBasicInfo appBasicInfo = getApplicationBasicInfoFromCacheByResourceId(resourceId);
        if (appBasicInfo == null) {
            // Cache miss, fetch from DB.
            appBasicInfo = appDAO.getApplicationBasicInfoByResourceId(resourceId, tenantDomain);
            if (appBasicInfo != null) {
                addAppBasicInfoToCache(appBasicInfo, tenantDomain);
            }
        }
        return appBasicInfo;
    }

    @Override
    public ServiceProvider getApplicationByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider application = getApplicationFromCacheByResourceId(resourceId);
        if (application == null) {
            // Cache miss, fetch from DB.
            application = appDAO.getApplicationByResourceId(resourceId, tenantDomain);
            if (application != null) {
                addToCache(application, tenantDomain);
            }
        }
        return application;
    }

    @Override
    public String addApplication(ServiceProvider application,
                                      String tenantDomain) throws IdentityApplicationManagementException {

        return appDAO.addApplication(application, tenantDomain);
    }

    @Override
    public void updateApplicationByResourceId(String resourceId,
                                              String tenantDomain,
                                              ServiceProvider updatedApp) throws IdentityApplicationManagementException {

        ServiceProvider storedApp = getApplicationByResourceId(resourceId, tenantDomain);
        clearAllAppCache(storedApp, tenantDomain);

        appDAO.updateApplicationByResourceId(resourceId, tenantDomain, updatedApp);
    }

    @Override
    public void deleteApplicationByResourceId(String resourceId,
                                              String tenantDomain) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = getApplicationByResourceId(resourceId, tenantDomain);
        clearAllAppCache(serviceProvider, tenantDomain);

        appDAO.deleteApplicationByResourceId(resourceId, tenantDomain);
    }

    @Override
    public List<ApplicationBasicInfo> getDiscoverableApplicationBasicInfo(int limit, int offset, String filter,
                                                                          String sortOrder, String sortBy, String
                                                                                  tenantDomain) throws
            IdentityApplicationManagementException {

        return appDAO.getDiscoverableApplicationBasicInfo(limit, offset, filter, sortOrder, sortBy, tenantDomain);
    }

    @Override
    public int getCountOfDiscoverableApplications(String filter, String tenantDomain) throws
            IdentityApplicationManagementException {

        return appDAO.getCountOfDiscoverableApplications(filter, tenantDomain);
    }

    @Override
    public ApplicationBasicInfo getDiscoverableApplicationBasicInfoByResourceId(String resourceId, String
            tenantDomain) throws IdentityApplicationManagementException {

        return appDAO.getDiscoverableApplicationBasicInfoByResourceId(resourceId, tenantDomain);
    }

    @Override
    public boolean isApplicationDiscoverable(String resourceId, String tenantDomain) throws
            IdentityApplicationManagementException {

        return appDAO.isApplicationDiscoverable(resourceId, tenantDomain);
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

            ServiceProviderResourceIdCacheKey resourceIdCacheKey =
                    new ServiceProviderResourceIdCacheKey(serviceProvider.getApplicationResourceId());
            ServiceProviderResourceIdCacheEntry entry = new ServiceProviderResourceIdCacheEntry(serviceProvider);
            appCacheByResourceId.addToCache(resourceIdCacheKey, entry);

            if (serviceProvider.getInboundAuthenticationConfig() != null && serviceProvider
                    .getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {
                InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                        .getInboundAuthenticationRequestConfigs();
                for (InboundAuthenticationRequestConfig config : configs) {
                    if (config.getInboundAuthKey() != null) {
                        ServiceProviderCacheInboundAuthKey clientKey = new ServiceProviderCacheInboundAuthKey(
                                config.getInboundAuthKey(), config.getInboundAuthType(), tenantDomain);
                        ServiceProviderCacheInboundAuthEntry clientEntry = new ServiceProviderCacheInboundAuthEntry(
                                serviceProvider.getApplicationName(), tenantDomain);
                        appCacheByInboundAuth.addToCache(clientKey, clientEntry);
                    }
                }
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }
    }

    private void addAppBasicInfoToCache(ApplicationBasicInfo appBasicInfo, String tenantDomain) throws
            IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Add cache for the application " + appBasicInfo.getApplicationName() + "@" + tenantDomain);
        }
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            ApplicationBasicInfoResourceIdCacheKey key =
                    new ApplicationBasicInfoResourceIdCacheKey(appBasicInfo.getApplicationResourceId());
            ApplicationBasicInfoResourceIdCacheEntry entry = new ApplicationBasicInfoResourceIdCacheEntry(appBasicInfo);
            appBasicInfoCacheByResourceId.addToCache(key, entry);
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

    private ServiceProvider getApplicationFromCacheByResourceId(String resourceId)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            if (resourceId != null) {
                ServiceProviderResourceIdCacheKey cacheKey = new ServiceProviderResourceIdCacheKey(resourceId);
                ServiceProviderResourceIdCacheEntry entry = appCacheByResourceId.getValueFromCache(cacheKey);

                if (entry != null) {
                    serviceProvider = entry.getServiceProvider();
                }
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }

        if (serviceProvider == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for the application with resourceId: " + resourceId);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for the application with resourceId: " + resourceId);
            }
        }
        return serviceProvider;
    }

    private ApplicationBasicInfo getApplicationBasicInfoFromCacheByResourceId(String resourceId)
            throws IdentityApplicationManagementException {

        ApplicationBasicInfo applicationBasicInfo = null;
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            if (resourceId != null) {
                ApplicationBasicInfoResourceIdCacheKey cacheKey =
                        new ApplicationBasicInfoResourceIdCacheKey(resourceId);
                ApplicationBasicInfoResourceIdCacheEntry entry =
                        appBasicInfoCacheByResourceId.getValueFromCache(cacheKey);

                if (entry != null) {
                    applicationBasicInfo = entry.getApplicationBasicInfo();
                }
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }

        if (applicationBasicInfo == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for the application with resourceId: " + resourceId);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for the application with resourceId: " + resourceId);
            }
        }
        return applicationBasicInfo;
    }

    private ServiceProvider getApplicationFromCache(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            if (StringUtils.isNotBlank(applicationName)) {
                IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                        applicationName, tenantDomain);
                IdentityServiceProviderCacheEntry entry = appCacheByName.getValueFromCache(cacheKey);

                if (entry != null) {
                    serviceProvider = entry.getServiceProvider();
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Provided application name is empty");
                }
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

            ServiceProviderResourceIdCacheKey resourceIdKey =
                    new ServiceProviderResourceIdCacheKey(serviceProvider.getApplicationResourceId());
            appCacheByResourceId.clearCacheEntry(resourceIdKey);

            ApplicationBasicInfoResourceIdCacheKey basicInfoKey =
                    new ApplicationBasicInfoResourceIdCacheKey(serviceProvider.getApplicationResourceId());
            appBasicInfoCacheByResourceId.clearCacheEntry(basicInfoKey);

            clearAppCacheByInboundKey(serviceProvider, tenantDomain);
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }
    }

    private void clearAllAppCache(ServiceProvider serviceProvider, String updatedName, String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            ApplicationMgtUtil.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(updatedName, tenantDomain);
            appCacheByName.clearCacheEntry(cacheKey);

            clearAllAppCache(serviceProvider, tenantDomain);
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
                if (config.getInboundAuthKey() != null) {
                    ServiceProviderCacheInboundAuthKey clientKey = new ServiceProviderCacheInboundAuthKey(
                            config.getInboundAuthKey(), config.getInboundAuthType(), tenantDomain);
                    appCacheByInboundAuth.clearCacheEntry(clientKey);
                }
            }
        }
    }
}
