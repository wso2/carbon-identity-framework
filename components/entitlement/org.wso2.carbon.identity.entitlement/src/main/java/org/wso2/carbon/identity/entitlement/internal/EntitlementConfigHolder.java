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

package org.wso2.carbon.identity.entitlement.internal;

import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.pap.EntitlementDataFinderModule;
import org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPExtension;
import org.wso2.carbon.identity.entitlement.pip.PIPResourceFinder;
import org.wso2.carbon.identity.entitlement.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisherModule;
import org.wso2.carbon.identity.entitlement.policy.publisher.PostPublisherModule;
import org.wso2.carbon.identity.entitlement.policy.publisher.PublisherVerificationModule;
import org.wso2.carbon.identity.entitlement.policy.store.PolicyDataStore;
import org.wso2.carbon.identity.entitlement.policy.store.PolicyStoreManageModule;
import org.wso2.carbon.identity.entitlement.policy.version.PolicyVersionManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.validation.Schema;

/**
 * keeps track of the configuration found in entitlement-config.xml
 */
public class EntitlementConfigHolder {

    /**
     * PIPExtensions will be fired for each and every XACML request - which will give a handle to
     * the incoming request.
     */
    private Map<PIPExtension, Properties> extensions = new HashMap<PIPExtension, Properties>();

    /**
     * This will be fired by CarbonAttributeFinder whenever it finds an attribute supported by this
     * module and missing in the XACML request.
     */
    private Map<PIPAttributeFinder, Properties> designators = new HashMap<PIPAttributeFinder, Properties>();

    /**
     * This will be fired by CarbonResourceFinder whenever it wants to find a descendant or child resource
     * of a given resource
     */
    private Map<PIPResourceFinder, Properties> resourceFinders = new HashMap<PIPResourceFinder, Properties>();

    /**
     * This will be fired by EntitlementDataFinder, whenever it wants to retrieve an attribute values to build the
     * XACML policy
     */
    private Map<EntitlementDataFinderModule, Properties> policyEntitlementDataFinders =
            new HashMap<EntitlementDataFinderModule, Properties>();

    /**
     * Will be fired by PolicyPublisher, whenever it wants to publish a policy
     */
    private Map<PolicyPublisherModule, Properties> policyPublisherModules =
            new HashMap<PolicyPublisherModule, Properties>();

    /**
     * Will be fired by PolicyPublisher, after a policy is published
     */
    private Map<PostPublisherModule, Properties> policyPostPublisherModules =
            new HashMap<PostPublisherModule, Properties>();

    /**
     * Will be fired by PolicyPublisher, before a policy is published
     */
    private Map<PublisherVerificationModule, Properties> publisherVerificationModule =
            new HashMap<PublisherVerificationModule, Properties>();

    /**
     * Will be fired by CarbonPolicyFinder, whenever it wants to find policies
     */
    private Map<PolicyFinderModule, Properties> policyFinderModules =
            new HashMap<PolicyFinderModule, Properties>();

    /**
     * This holds all the policies of entitlement engine
     */
    private Map<PolicyCollection, Properties> policyCollections =
            new HashMap<PolicyCollection, Properties>();

    /**
     * Will be fired by admin services, whenever it wants send notifications
     */
    private Map<PAPStatusDataHandler, Properties> papStatusDataHandlers =
            new HashMap<PAPStatusDataHandler, Properties>();

    /**
     * This holds all the policy storing logic of entitlement engine
     */
    private Map<PolicyStoreManageModule, Properties> policyStore =
            new HashMap<PolicyStoreManageModule, Properties>();

    /**
     * This holds all the policy versioning of PAP
     */
    private Map<PolicyDataStore, Properties> policyDataStore =
            new HashMap<PolicyDataStore, Properties>();


    /**
     * This holds all the policy storing logic of entitlement engine
     */
    private Map<PolicyVersionManager, Properties> policyVersionModule =
            new HashMap<PolicyVersionManager, Properties>();


    /**
     * This holds the policy schema against its version
     */
    private Map<String, Schema> policySchemaMap = new HashMap<String, Schema>();

    /**
     * Holds all caching related configurations
     */
    private Properties engineProperties;

    /**
     * Holds the properties of all modules.
     */
    private Map<String, List<PublisherDataHolder>> modulePropertyHolderMap =
            new HashMap<String, List<PublisherDataHolder>>();

    private ConfigurationContextService configurationContextService;
    private ApplicationManagementService applicationManagementService;
    private static EntitlementConfigHolder instance = new EntitlementConfigHolder();

    private EntitlementConfigHolder() {
    }

    public static EntitlementConfigHolder getInstance() {
        return instance;
    }

    public Map<PIPExtension, Properties> getExtensions() {
        return extensions;
    }

    public void addExtensions(PIPExtension extension, Properties properties) {
        this.extensions.put(extension, properties);
    }

    public Map<PIPAttributeFinder, Properties> getDesignators() {
        return designators;
    }

    public void addDesignators(PIPAttributeFinder attributeFinder, Properties properties) {
        this.designators.put(attributeFinder, properties);
    }

    public Map<PIPResourceFinder, Properties> getResourceFinders() {
        return resourceFinders;
    }

    public void addResourceFinders(PIPResourceFinder resourceFinder, Properties properties) {
        this.resourceFinders.put(resourceFinder, properties);
    }

    public Map<EntitlementDataFinderModule, Properties> getPolicyEntitlementDataFinders() {
        return policyEntitlementDataFinders;
    }

    public void addPolicyEntitlementDataFinder(EntitlementDataFinderModule metaDataFinderModule,
                                               Properties properties) {
        this.policyEntitlementDataFinders.put(metaDataFinderModule, properties);
    }

    public Properties getEngineProperties() {
        return engineProperties;
    }

    public void setEngineProperties(Properties engineProperties) {
        this.engineProperties = engineProperties;
    }

    public Map<String, Schema> getPolicySchemaMap() {
        return policySchemaMap;
    }

    public void setPolicySchema(String schemaNS, Schema schema) {
        this.policySchemaMap.put(schemaNS, schema);
    }

    public Map<PolicyPublisherModule, Properties> getPolicyPublisherModules() {
        return policyPublisherModules;
    }

    public void addPolicyPublisherModule(PolicyPublisherModule policyPublisherModules,
                                         Properties properties) {
        this.policyPublisherModules.put(policyPublisherModules, properties);
    }

    public List<PublisherDataHolder> getModulePropertyHolders(String type) {
        return modulePropertyHolderMap.get(type);
    }

    public void addModulePropertyHolder(String type, PublisherDataHolder holder) {
        if (this.modulePropertyHolderMap.get(type) == null) {
            List<PublisherDataHolder> holders = new ArrayList<PublisherDataHolder>();
            holders.add(holder);
            this.modulePropertyHolderMap.put(type, holders);
        } else {
            this.modulePropertyHolderMap.get(type).add(holder);
        }
    }

    public Map<PolicyFinderModule, Properties> getPolicyFinderModules() {
        return policyFinderModules;
    }

    public void addPolicyFinderModule(PolicyFinderModule policyFinderModule,
                                      Properties properties) {
        this.policyFinderModules.put(policyFinderModule, properties);
    }

    public Map<PolicyCollection, Properties> getPolicyCollections() {
        return policyCollections;
    }

    public void addPolicyCollection(PolicyCollection collection, Properties properties) {
        this.policyCollections.put(collection, properties);
    }

    public Map<PolicyStoreManageModule, Properties> getPolicyStore() {
        return policyStore;
    }

    public void addPolicyStore(PolicyStoreManageModule policyStoreStore, Properties properties) {
        this.policyStore.put(policyStoreStore, properties);
    }

    public Map<PostPublisherModule, Properties> getPolicyPostPublisherModules() {
        return policyPostPublisherModules;
    }

    public void addPolicyPostPublisherModule(PostPublisherModule postPublisherModule, Properties properties) {
        this.policyPostPublisherModules.put(postPublisherModule, properties);
    }

    public Map<PublisherVerificationModule, Properties> getPublisherVerificationModule() {
        return publisherVerificationModule;
    }

    public void addPublisherVerificationModule(PublisherVerificationModule publisherVerificationModule,
                                               Properties properties) {
        this.publisherVerificationModule.put(publisherVerificationModule, properties);
    }

    public Map<PAPStatusDataHandler, Properties> getPapStatusDataHandlers() {
        return papStatusDataHandlers;
    }

    public void addNotificationHandler(PAPStatusDataHandler notificationHandler,
                                       Properties properties) {
        this.papStatusDataHandlers.put(notificationHandler, properties);
    }

    public Map<PolicyVersionManager, Properties> getPolicyVersionModule() {
        return policyVersionModule;
    }

    public void addPolicyVersionModule(PolicyVersionManager policyVersionModule, Properties properties) {
        this.policyVersionModule.put(policyVersionModule, properties);
    }

    public Map<PolicyDataStore, Properties> getPolicyDataStore() {
        return policyDataStore;
    }

    public void addPolicyDataStore(PolicyDataStore policyDataStore, Properties properties) {
        this.policyDataStore.put(policyDataStore, properties);
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }
}
