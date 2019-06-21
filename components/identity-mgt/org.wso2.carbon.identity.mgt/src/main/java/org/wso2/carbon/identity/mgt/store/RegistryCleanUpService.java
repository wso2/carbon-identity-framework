/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RegistryCleanUpService {

    private static final int NUM_THREADS = 1;
    private static final Log log = LogFactory.getLog(RegistryCleanUpService.class);
    private final ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long delayBetweenRuns;

    /**
     * @param initialDelay
     * @param delayBetweenRuns
     */
    public RegistryCleanUpService(long initialDelay, long delayBetweenRuns) {
        this.initialDelay = initialDelay;
        this.delayBetweenRuns = delayBetweenRuns;
        this.scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
    }

    /**
     * Activate clean up task.
     */
    public void activateCleanUp() {
        if(delayBetweenRuns == 0){
            return;
        }
        Runnable registryCleanUpTask = new RegistryCleanUpTask();
        scheduler.scheduleWithFixedDelay(registryCleanUpTask, initialDelay, delayBetweenRuns,
                TimeUnit.MINUTES);

    }

    private static final class RegistryCleanUpTask implements Runnable {

        private static final String CONFIRMATION_REGISTRY_RESOURCE_PATH = "/repository/components/org.wso2.carbon" +
                ".identity.mgt/data";
        private static final String EXPIRE_TIME_PROPERTY = "expireTime";

        @Override
        public void run() {

            if (log.isDebugEnabled()) {
                log.debug("Start running the Identity-Management registry Data cleanup task.");
            }
            Registry registry;
            Collection identityDataResource;
            try {
                Tenant[] tenants = IdentityMgtServiceComponent.getRealmService().getTenantManager().getAllTenants();
                for (int i = 0; i < tenants.length + 1; i++) {
                    Tenant tenant;
                    if ( i == tenants.length) {
                        tenant = new Tenant();
                        tenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                        tenant.setId(MultitenantConstants.SUPER_TENANT_ID);
                    } else {
                        tenant = tenants[i];
                    }
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant.getDomain());
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenant.getId());
                    try {
                        registry = IdentityMgtServiceComponent.getRegistryService().
                            getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                        String[] identityResourcesPaths = new String[0];
                        if (registry.resourceExists(CONFIRMATION_REGISTRY_RESOURCE_PATH)) {
                            identityDataResource = (Collection) registry.get(CONFIRMATION_REGISTRY_RESOURCE_PATH);
                            identityResourcesPaths = identityDataResource.getChildren();
                        }
                        for (int j = 0; j < identityResourcesPaths.length; j++) {
                            try {
                                Resource currentResource = registry.get(identityResourcesPaths[j]);
                                if (currentResource instanceof Collection) {
                                    Collection secondaryStoreCollection = (Collection) currentResource;
                                    String[] secondaryStoreResourcePaths = secondaryStoreCollection.getChildren();
                                    for (int k = 0; k < secondaryStoreResourcePaths.length; k++) {
                                        checkAndDeleteRegistryResource(registry, secondaryStoreResourcePaths[k]);
                                    }
                                } else {
                                    checkAndDeleteRegistryResource(registry, identityResourcesPaths[j]);
                                }
                            } catch (RegistryException e) {
                                log.error("Error while retrieving resource at " + identityResourcesPaths[j], e);
                            }
                        }
                    } catch (ResourceNotFoundException e) {
                        if(log.isDebugEnabled()){
                            log.debug("No resource found for tenant " + tenant.getDomain(), e);
                        }
                    } catch (RegistryException e) {
                        if(log.isDebugEnabled()){
                            log.debug("Error while deleting the expired confirmation code.", e);
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            } catch (UserStoreException e) {
                if(log.isDebugEnabled()){
                    log.debug("Error while getting the tenant manager.", e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Finished running the Identity-Management registry Data cleanup task.");
            }
        }

        /**
         * Check if resource has expired and delete.
         *
         * @param registry Registry instance to use.
         * @param resourcePath Path of resource to be deleted.
         * @throws RegistryException
         */
        private static void checkAndDeleteRegistryResource (Registry registry, String resourcePath) throws
                RegistryException {

            Resource resource = registry.get(resourcePath);
            long currentEpochTime = System.currentTimeMillis();
            long resourceExpireTime = Long.parseLong(resource.getProperty(EXPIRE_TIME_PROPERTY));
            if (currentEpochTime > resourceExpireTime) {

                registry.delete(resource.getId());
            }
        }

    }


}
