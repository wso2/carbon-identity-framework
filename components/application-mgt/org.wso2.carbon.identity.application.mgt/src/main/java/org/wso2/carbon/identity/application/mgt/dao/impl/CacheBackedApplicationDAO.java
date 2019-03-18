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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheKey;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderIDCache;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderIDCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderIDCacheKey;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderClientIDCache;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderClientCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderClientIDCacheKey;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;

public class CacheBackedApplicationDAO {

    private static final Log log = LogFactory.getLog(CacheBackedApplicationDAO.class);

    private ApplicationDAO appDAO = null;

    private IdentityServiceProviderCache appCacheByName = null;
    private ServiceProviderClientIDCache appCacheByClientId = null;
    private ServiceProviderIDCache appCacheByID = null;

    public CacheBackedApplicationDAO() {

        this.appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        appCacheByName = IdentityServiceProviderCache.getInstance();
        appCacheByClientId = ServiceProviderClientIDCache.getInstance();
        appCacheByID = ServiceProviderIDCache.getInstance();
    }

    public ServiceProvider getServiceProvider(String applicationName, String tenantDomain) throws
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

            if (serviceProvider == null) {
                ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
                serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
                if (serviceProvider != null) {
                    loadApplicationPermissions(applicationName, serviceProvider);
                    IdentityServiceProviderCacheEntry spEntry = new IdentityServiceProviderCacheEntry();
                    spEntry.setServiceProvider(serviceProvider);
                    appCacheByName.addToCache(cacheKey, spEntry);
                }
            }
        } catch (Exception e) {
            String error = "Error occurred while retrieving the application, " + applicationName;
            log.error(error, e);
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
        return serviceProvider;
    }

    public ApplicationBasicInfo[] getAllApplicationBasicInfo(String tenantDomain, String username, String filter)
            throws IdentityApplicationManagementException {

        ApplicationBasicInfo[] allApplicationBasicInfo = null;
        try {
            startTenantFlow(tenantDomain, username);
            if (!(appDAO instanceof AbstractApplicationDAOImpl)) {
                log.error("Get application basic info service is not supported.");
                throw new IdentityApplicationManagementException("This service is not supported.");
            }
            allApplicationBasicInfo = ((AbstractApplicationDAOImpl) appDAO).getApplicationBasicInfo(filter);
            // TODO: 15/03/19 add cache layer for appInfo
        } catch (Exception e) {
            String error = "Error occurred while retrieving all the applications";
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
        return allApplicationBasicInfo;
    }

    public boolean isApplicationExists(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                applicationName, tenantDomain);
        IdentityServiceProviderCacheEntry entry = appCacheByName.getValueFromCache(cacheKey);
        if (entry != null) {
            return true;
        } else {
            return appDAO.isApplicationExists(applicationName, tenantDomain);
        }
    }

    public int createApplication(ServiceProvider serviceProvider, String tenantDomain, String username) throws
            IdentityApplicationManagementException {

        int applicationId;
        try {
            startTenantFlow(tenantDomain, username);
            String applicationName = serviceProvider.getApplicationName();
            // First we need to create a role with the application name. Only the users in this role will be able to
            // edit/update the application.
            ApplicationMgtUtil.createAppRole(applicationName, username);
            try {
                ApplicationMgtUtil.storePermissions(applicationName, username,
                        serviceProvider.getPermissionAndRoleConfig());
            } catch (IdentityApplicationManagementException e) {
                deleteApplicationRole(applicationName);
                throw e;
            }
            try {
                applicationId = appDAO.createApplication(serviceProvider, tenantDomain);
            } catch (IdentityApplicationManagementException e) {
                deleteApplicationRole(applicationName);
                deleteApplicationPermission(applicationName);
                throw e;
            }
        } finally {
            endTenantFlow();
        }
        return applicationId;
    }

    public void deleteApplication(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        appDAO.deleteApplication(applicationName);
        clearAllAppCache(applicationName, tenantDomain);
    }

    public String getServiceProviderNameByClientIdExcludingFileBasedSPs(String clientId, String type, String
            tenantDomain) throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain);
            ServiceProviderClientIDCacheKey cacheKey = new ServiceProviderClientIDCacheKey(clientId);
            ServiceProviderClientCacheEntry entry = appCacheByClientId.getValueFromCache(cacheKey);

            if (entry != null) {
                return entry.getServiceProviderName();
            }

            String serviceProviderName = appDAO.getServiceProviderNameByClientId(clientId, type, tenantDomain);
            ServiceProviderClientCacheEntry spNameEntry = new ServiceProviderClientCacheEntry();
            spNameEntry.setServiceProviderName(serviceProviderName);
            appCacheByClientId.addToCache(cacheKey, spNameEntry);
            return serviceProviderName;
        } finally {
            endTenantFlow();
        }
    }

    public String getServiceProviderNameByClientId(String clientId, String clientType, String tenantDomain) throws
            IdentityApplicationManagementException {

        String name = getServiceProviderNameByClientIdExcludingFileBasedSPs(clientId, clientType, tenantDomain);
        if (name == null) {
            name = new FileBasedApplicationDAO().getServiceProviderNameByClientId(clientId, clientType, tenantDomain);
        }
        return name;
    }

    public ServiceProvider getServiceProvider(int appId) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ServiceProviderIDCacheKey cacheKey = new ServiceProviderIDCacheKey(appId);
            ServiceProviderIDCacheEntry entry = appCacheByID.getValueFromCache(cacheKey);

            if (entry != null) {
                serviceProvider = entry.getServiceProvider();
            }

            if (serviceProvider == null){
                serviceProvider = appDAO.getApplication(appId);
                ServiceProviderIDCacheEntry spEntry = new ServiceProviderIDCacheEntry();
                spEntry.setServiceProvider(serviceProvider);
                appCacheByID.addToCache(cacheKey, spEntry);
            }
        } finally {
            endTenantFlow();
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

        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ServiceProviderIDCacheKey cacheKey = new ServiceProviderIDCacheKey(applicationID);
            ServiceProviderIDCacheEntry entry = appCacheByID.getValueFromCache(cacheKey);
            if (entry != null && entry.getServiceProvider() != null) {
                return entry.getServiceProvider().getApplicationName();
            }
        } finally {
            endTenantFlow();
        }
        return appDAO.getApplicationName(applicationID);
    }

    public void updateApplication(ServiceProvider serviceProvider, String tenantDomain) throws
            IdentityApplicationManagementException {

        clearAllAppCache(serviceProvider.getApplicationName(), tenantDomain);
        appDAO.updateApplication(serviceProvider, tenantDomain);

    }

    private void deleteApplicationPermission(String applicationName) {

        try {
            ApplicationMgtUtil.deletePermissions(applicationName);
        } catch (IdentityApplicationManagementException e) {
            log.error("Failed to delete the permissions for: " + applicationName, e);
        }
    }

    private void deleteApplicationRole(String applicationName) {

        try {
            ApplicationMgtUtil.deleteAppRole(applicationName);
        } catch (IdentityApplicationManagementException e) {
            log.error("Failed to delete the application role for: " + applicationName, e);
        }
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

    private void startTenantFlow(String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

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
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
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

    private void clearAllAppCache(String applicationName, String tenantDomain) throws
            IdentityApplicationManagementException {

        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    applicationName, tenantDomain);
            appCacheByName.clearCacheEntry(cacheKey);
            appCacheByClientId.clear();
            appCacheByID.clear();
        } finally {
            endTenantFlow();
        }

    }
}