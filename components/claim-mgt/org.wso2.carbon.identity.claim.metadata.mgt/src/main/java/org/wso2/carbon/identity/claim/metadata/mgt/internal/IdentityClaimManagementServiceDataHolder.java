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
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimConfigInitDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.listener.ClaimMetadataMgtListener;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;
import org.wso2.carbon.user.core.listener.ClaimManagerListener;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
    private IdentityEventService identityEventService;
    private static Map<Integer, ClaimManagerListener> claimManagerListeners = new TreeMap<Integer,
            ClaimManagerListener>();
    private static List<ClaimMetadataMgtListener> claimMetadataMgtListeners = new ArrayList<>();
    private ClaimConfigInitDAO claimConfigInitDAO;
    private ClaimConfig claimConfig;

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

    /**
     * Get {@link IdentityEventService}.
     *
     * @return IdentityEventService.
     */
    public IdentityEventService getIdentityEventService() {

        return identityEventService;
    }

    /**
     * Set {@link IdentityEventService}.
     *
     * @param identityEventService Instance of {@link IdentityEventService}.
     */
    public void setIdentityEventService(IdentityEventService identityEventService) {

        this.identityEventService = identityEventService;
    }

    public static synchronized List<ClaimMetadataMgtListener> getClaimMetadataMgtListeners() {

        return claimMetadataMgtListeners;
    }

    public synchronized void addClaimMetadataMgtListener(ClaimMetadataMgtListener claimMetadataMgtListener) {

        claimMetadataMgtListeners.add(claimMetadataMgtListener);
        claimMetadataMgtListeners.sort(claimMetadataMgtListenerComparator);
    }

    public synchronized void removeClaimMetadataMgtListener(ClaimMetadataMgtListener claimMetadataMgtListener) {

        if (claimMetadataMgtListener != null) {
            claimMetadataMgtListeners.remove(claimMetadataMgtListener);
        }
    }

    private static Comparator<ClaimMetadataMgtListener> claimMetadataMgtListenerComparator =
            Comparator.comparingInt(ClaimMetadataMgtListener::getExecutionOrderId);

    public void setClaimConfigInitDAO(ClaimConfigInitDAO claimConfigInitDAO) {

        this.claimConfigInitDAO = claimConfigInitDAO;
    }

    public ClaimConfigInitDAO getClaimConfigInitDAO() {

        return claimConfigInitDAO;
    }

    public ClaimConfig getClaimConfig() {

        return claimConfig;
    }

    public void setClaimConfig(ClaimConfig claimConfig) {

        this.claimConfig = claimConfig;
    }
}
