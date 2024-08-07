/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.pap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.persistence.ConfigPersistenceManager;
import org.wso2.carbon.identity.entitlement.persistence.PersistenceManagerFactory;
import org.wso2.carbon.identity.entitlement.persistence.PolicyPersistenceManager;
import org.wso2.carbon.identity.entitlement.persistence.SubscriberPersistenceManager;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisher;
import org.wso2.carbon.identity.entitlement.policy.store.DefaultPolicyDataStore;
import org.wso2.carbon.identity.entitlement.policy.store.PolicyDataStore;
import org.wso2.carbon.identity.entitlement.policy.store.PolicyStoreManageModule;
import org.wso2.carbon.identity.entitlement.policy.store.PolicyStoreManager;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class EntitlementAdminEngine {

    private static final Object lock = new Object();
    private static ConcurrentHashMap<String, EntitlementAdminEngine> entitlementAdminEngines =
            new ConcurrentHashMap<String, EntitlementAdminEngine>();
    private static Log log = LogFactory.getLog(EntitlementAdminEngine.class);
    private PolicyPublisher policyPublisher;
    private EntitlementDataFinder entitlementDataFinder;
    private PolicyDataStore policyDataStore;
    private PolicyStoreManager policyStoreManager;
    private PAPPolicyStoreManager papPolicyStoreManager;
    private Set<PAPStatusDataHandler> papStatusDataHandlers;
    private ConfigPersistenceManager configPersistenceManager;
    private PolicyPersistenceManager policyPersistenceManager;
    private SubscriberPersistenceManager subscriberPersistenceManager;

    public EntitlementAdminEngine() {

        this.entitlementDataFinder = new EntitlementDataFinder();
        this.policyPublisher = new PolicyPublisher();
        this.papPolicyStoreManager = new PAPPolicyStoreManager();

        Map<PolicyStoreManageModule, Properties> policyCollections = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyStore();
        Properties policyStoreProperties = new Properties();
        if (MapUtils.isNotEmpty(policyCollections)) {
            policyStoreProperties = policyCollections.entrySet().iterator().next().getValue();
        }
        Map<PolicyDataStore, Properties> dataStoreModules = EntitlementServiceComponent.
                getEntitlementConfig().getPolicyDataStore();
        if (MapUtils.isNotEmpty(dataStoreModules)) {
            this.policyDataStore = dataStoreModules.entrySet().iterator().next().getKey();
        } else {
            this.policyDataStore = new DefaultPolicyDataStore();
        }
        try {
            this.policyDataStore.init(policyStoreProperties);
        } catch (EntitlementException e) {
            log.warn("Error occurred while initializing the policy data store", e);
        }

        Map<PAPStatusDataHandler, Properties> statusDataHandlers = EntitlementServiceComponent.
                getEntitlementConfig().getPapStatusDataHandlers();
        papStatusDataHandlers = statusDataHandlers.keySet();
        this.policyPublisher.setPapStatusDataHandlers(papStatusDataHandlers);
        this.policyStoreManager = new PolicyStoreManager(policyDataStore);
        this.configPersistenceManager = PersistenceManagerFactory.getConfigPersistenceManager();
        this.policyPersistenceManager = PersistenceManagerFactory.getPolicyPersistenceManager();
        this.subscriberPersistenceManager = PersistenceManagerFactory.getSubscriberPersistenceManager();

    }

    /**
     * Get a EntitlementEngine instance for that tenant. This method will return an
     * EntitlementEngine instance if exists, or creates a new one
     *
     * @return EntitlementEngine instance for that tenant
     */
    public static EntitlementAdminEngine getInstance() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!entitlementAdminEngines.containsKey(Integer.toString(tenantId))) {
            synchronized (lock) {
                if (!entitlementAdminEngines.containsKey(Integer.toString(tenantId))) {
                    entitlementAdminEngines.put(Integer.toString(tenantId), new EntitlementAdminEngine());
                }
            }
        }
        return entitlementAdminEngines.get(Integer.toString(tenantId));
    }

    /**
     * This method returns policy publisher
     *
     * @return PolicyPublisher
     */
    public PolicyPublisher getPolicyPublisher() {
        return policyPublisher;
    }

    /**
     * This method returns the entitlement data finder
     *
     * @return EntitlementDataFinder
     */
    public EntitlementDataFinder getEntitlementDataFinder() {
        return entitlementDataFinder;
    }

    /**
     * @return PolicyDataStore.
     */
    public PolicyDataStore getPolicyDataStore() {

        return policyDataStore;
    }

    /**
     * This returns policy store manager
     *
     * @return
     */
    public PolicyStoreManager getPolicyStoreManager() {
        return policyStoreManager;
    }

    /**
     * @return
     */
    public PAPPolicyStoreManager getPapPolicyStoreManager() {
        return papPolicyStoreManager;
    }

    public Set<PAPStatusDataHandler> getPapStatusDataHandlers() {
        return papStatusDataHandlers;
    }

    public ConfigPersistenceManager getConfigPersistenceManager() { return configPersistenceManager; }

    public PolicyPersistenceManager getPolicyPersistenceManager() { return policyPersistenceManager; }

    public SubscriberPersistenceManager getSubscriberPersistenceManager() { return subscriberPersistenceManager; }
}
