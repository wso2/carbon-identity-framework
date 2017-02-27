/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * This singleton data holder contains all the data required by the identity claim metadata OSGi bundle
 */
public class IdentityClaimManagementServiceDataHolder {

    private static IdentityClaimManagementServiceDataHolder instance = new IdentityClaimManagementServiceDataHolder();

    private ClaimMetadataManagementService claimManagementService;
    private BundleContext bundleContext;
    private RealmService realmService;
    private RegistryService registryService;
    private static Map<Integer, ClaimManagerListener> claimManagerListeners = new TreeMap<Integer,
            ClaimManagerListener>();

    private IdentityClaimManagementServiceDataHolder() {

    }

    public static IdentityClaimManagementServiceDataHolder getInstance() {
        return instance;
    }

    public void setClaimManagementService(ClaimMetadataManagementService identityClaimManagementService) {
        this.claimManagementService = identityClaimManagementService;
    }

    public ClaimMetadataManagementService getClaimManagementService() {
        return claimManagementService;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public static synchronized Collection<ClaimManagerListener> getClaimManagerListeners() {

        return claimManagerListeners.values();
    }

    public synchronized void setClaimManagerListener(ClaimManagerListener claimManagerListener) {

        claimManagerListeners.put(claimManagerListener.getExecutionOrderId(), claimManagerListener);
    }

    public synchronized void unsetClaimManagerListener(ClaimManagerListener claimManagerListener) {

        if (claimManagerListener != null) {
            claimManagerListeners.remove(claimManagerListener.getExecutionOrderId());
        }
    }
}
