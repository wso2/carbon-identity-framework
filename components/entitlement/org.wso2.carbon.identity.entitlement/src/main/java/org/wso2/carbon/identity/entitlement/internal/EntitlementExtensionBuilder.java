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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.dao.PAPStatusDataHandlerModule;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dao.PolicyDataStoreModule;
import org.wso2.carbon.identity.entitlement.pap.EntitlementDataFinderModule;
import org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPExtension;
import org.wso2.carbon.identity.entitlement.pip.PIPResourceFinder;
import org.wso2.carbon.identity.entitlement.policy.collection.PolicyCollection;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.publisher.PolicyPublisherModule;
import org.wso2.carbon.identity.entitlement.policy.publisher.PostPublisherModule;
import org.wso2.carbon.identity.entitlement.policy.publisher.PublisherVerificationModule;
import org.wso2.carbon.identity.entitlement.dao.PDPPolicyStoreModule;
import org.wso2.carbon.identity.entitlement.dao.PolicyVersionManagerModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Build Entitlement configuration from entitlement.properties. First this will try to find the
 * configuration file from [CARBON_HOME]\repository\conf - failing to do so will load the file from
 * this bundle it self.The default file ships with the bundle only includes
 * org.wso2.carbon.identity.entitlement.pip.DefaultAttributeFinder as an AttributeDesignator and
 * default caching configurations.
 * <p/>
 * <p/>
 * PDP.OnDemangPolicyLoading.Enable=false
 * PDP.OnDemangPolicyLoading.MaxInMemoryPolicies=1000
 * PDP.DecisionCaching.Enable=true
 * PDP.DecisionCaching.CachingInterval=30000
 * PDP.AttributeCaching.Enable=true
 * PDP.DecisionCaching.CachingInterval=30000
 * PDP.ResourceCaching.Enable=true
 * PDP.ResourceCaching.CachingInterval=30000
 * JSON.Shorten.Form.Enabled.ForDefault=false
 * <p/>
 * PDP.Extensions.Extension.1=org.wso2.carbon.identity.entitlement.pdp.DefaultExtension
 * <p/>
 * PIP.AttributeDesignators.Designator.1=org.wso2.carbon.identity.entitlement.pip.DefaultAttributeFinder
 * PIP.ResourceFinders.Finder.1="org.wso2.carbon.identity.entitlement.pip.DefaultResourceFinder
 * <p/>
 * PAP.MetaDataFinders.Finder.1=org.wso2.carbon.identity.entitlement.pap.CarbonEntitlementDataFinder
 * PAP.PolicyPublishers.Publisher.1=org.wso2.carbon.identity.entitlement.policy.publisher
 * .CarbonBasicPolicyPublisherModule
 * <p/>
 * # Properties needed for each extension. #
 * org.wso2.carbon.identity.entitlement.pip.DefaultAttributeFinder.1=name,value #
 * org.wso2.carbon.identity.entitlement.pip.DefaultAttributeFinder.2=name,value #
 * org.wso2.carbon.identity.entitlement.pip.DefaultResourceFinder.1=name.value #
 * org.wso2.carbon.identity.entitlement.pip.DefaultResourceFinder.2=name,value #
 * org.wso2.carbon.identity.entitlement.pap.CarbonEntitlementDataFinder.1=name,value #
 * org.wso2.carbon.identity.entitlement.pap.CarbonEntitlementDataFinder.2=name,value
 */
public class EntitlementExtensionBuilder {


    public static final String PDP_SCHEMA_VALIDATION = "PDP.SchemaValidation.Enable";

    private static final String ENTITLEMENT_CONFIG = "entitlement.properties";

    private static final Log log = LogFactory.getLog(EntitlementExtensionBuilder.class);

    private BundleContext bundleContext;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void buildEntitlementConfig(EntitlementConfigHolder holder) throws Exception {

        Properties properties;

        if ((properties = loadProperties()) != null) {
            populateEntitlementAttributes(properties, holder);
            populatePDPExtensions(properties, holder);
            populateAttributeFinders(properties, holder);
            populateEntitlementDataFinders(properties, holder);
            populateResourceFinders(properties, holder);
            populatePolicyPublishers(properties, holder);
            populatePolicyFinders(properties, holder);
            populatePolicyCollection(properties, holder);
            populatePolicyStoreModule(properties, holder);
            populatePolicyDataStore(properties, holder);
            populatePolicyVersionModule(properties, holder);
            populatePolicyPostPublishers(properties, holder);
            populateAdminNotificationHandlers(properties, holder);
            populatePublisherVerificationHandler(properties, holder);
        }
    }

    /**
     * @return properties
     * @throws IOException
     */
    private Properties loadProperties() throws IOException {

        Properties properties = new Properties();
        InputStream inStream = null;
        String warningMessage = null;

        File pipConfigXml = new File(IdentityUtil.getIdentityConfigDirPath(), ENTITLEMENT_CONFIG);

        try {
            if (pipConfigXml.exists()) {
                inStream = new FileInputStream(pipConfigXml);
            } else {
                URL url;
                if (bundleContext != null) {
                    if ((url = bundleContext.getBundle().getResource(ENTITLEMENT_CONFIG)) != null) {
                        inStream = url.openStream();
                    } else {
                        warningMessage = "Bundle context could not find resource "
                                + ENTITLEMENT_CONFIG
                                + " or user does not have sufficient permission to access the resource.";
                    }

                } else {

                    if ((url = this.getClass().getClassLoader().getResource(ENTITLEMENT_CONFIG)) != null) {
                        inStream = url.openStream();
                    } else {
                        warningMessage = "PIP Config Builder could not find resource "
                                + ENTITLEMENT_CONFIG
                                + " or user does not have sufficient permission to access the resource.";
                    }
                }
            }

            if (inStream == null) {
                log.warn(warningMessage);
                return null;
            }

            properties.load(inStream);

        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
            throw e;
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
            throw e;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (Exception e) {
                log.error("Error while closing input stream ", e);
            }
        }

        return properties;
    }

    /**
     * @param properties which are used to populate pdp properties
     * @param holder     holder of properties
     */
    private void populateEntitlementAttributes(Properties properties, EntitlementConfigHolder holder) {

        Properties pdpProperties = new Properties();

        setProperty(properties, pdpProperties, PDPConstants.ON_DEMAND_POLICY_LOADING);
        setProperty(properties, pdpProperties, PDPConstants.ON_DEMAND_POLICY_MAX_POLICY_ENTRIES);
        setProperty(properties, pdpProperties, PDPConstants.DECISION_CACHING);
        setProperty(properties, pdpProperties, PDPConstants.DECISION_CACHING_INTERVAL);
        setProperty(properties, pdpProperties, PDPConstants.ATTRIBUTE_CACHING);
        setProperty(properties, pdpProperties, PDPConstants.ATTRIBUTE_CACHING_INTERVAL);
        setProperty(properties, pdpProperties, PDPConstants.RESOURCE_CACHING);
        setProperty(properties, pdpProperties, PDPConstants.RESOURCE_CACHING_INTERVAL);
        setProperty(properties, pdpProperties, PDPConstants.PDP_ENABLE);
        setProperty(properties, pdpProperties, PDPConstants.PAP_ENABLE);
        setProperty(properties, pdpProperties, PDPConstants.BALANA_CONFIG_ENABLE);
        setProperty(properties, pdpProperties, PDPConstants.MULTIPLE_DECISION_PROFILE_ENABLE);
        setProperty(properties, pdpProperties, PDPConstants.MAX_POLICY_REFERENCE_ENTRIES);
        setProperty(properties, pdpProperties, PDPConstants.FILESYSTEM_POLICY_PATH);
        setProperty(properties, pdpProperties, PDPConstants.POLICY_ID_REGEXP_PATTERN);
        setProperty(properties, pdpProperties, PDPConstants.PDP_GLOBAL_COMBINING_ALGORITHM);
        setProperty(properties, pdpProperties, PDPConstants.ENTITLEMENT_ITEMS_PER_PAGE);
        setProperty(properties, pdpProperties, PDPConstants.START_UP_POLICY_ADDING);
        setProperty(properties, pdpProperties, PDP_SCHEMA_VALIDATION);
        setProperty(properties, pdpProperties, PDPConstants.ENTITLEMENT_ENGINE_CACHING_INTERVAL);
        setProperty(properties, pdpProperties, PDPConstants.PDP_REGISTRY_LEVEL_POLICY_CACHE_CLEAR);
        setProperty(properties, pdpProperties, PDPConstants.POLICY_CACHING_INTERVAL);
        setProperty(properties, pdpProperties, PDPConstants.XACML_JSON_SHORT_FORM_ENABLED);

        holder.setEngineProperties(pdpProperties);
    }


    private void setProperty(Properties inProp, Properties outProp, String name) {
        String value;
        if ((value = inProp.getProperty(name)) != null) {
            outProp.setProperty(name, value.trim());
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populateAttributeFinders(Properties properties, EntitlementConfigHolder holder)
            throws Exception {
        int i = 1;
        PIPAttributeFinder designator = null;

        while (properties.getProperty("PIP.AttributeDesignators.Designator." + i) != null) {
            String className = properties.getProperty("PIP.AttributeDesignators.Designator." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            designator = (PIPAttributeFinder) clazz.newInstance();

            int j = 1;
            Properties designatorProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                designatorProps.put(props[0], props[1]);
            }

            designator.init(designatorProps);
            holder.addDesignators(designator, designatorProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populateResourceFinders(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        int i = 1;
        PIPResourceFinder resource = null;

        while (properties.getProperty("PIP.ResourceFinders.Finder." + i) != null) {
            String className = properties.getProperty("PIP.ResourceFinders.Finder." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            resource = (PIPResourceFinder) clazz.newInstance();

            int j = 1;
            Properties resourceProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                resourceProps.put(props[0], props[1]);
            }

            resource.init(resourceProps);
            holder.addResourceFinders(resource, resourceProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePDPExtensions(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        int i = 1;
        PIPExtension extension = null;

        while (properties.getProperty("PDP.Extensions.Extension." + i) != null) {
            String className = properties.getProperty("PDP.Extensions.Extension." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            extension = (PIPExtension) clazz.newInstance();

            int j = 1;
            Properties extensionProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                extensionProps.put(props[0], props[1]);
            }

            extension.init(extensionProps);
            holder.addExtensions(extension, extensionProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePolicyFinders(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        int i = 1;
        PolicyFinderModule finderModule = null;

        while (properties.getProperty("PDP.Policy.Finder." + i) != null) {
            String className = properties.getProperty("PDP.Policy.Finder." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            finderModule = (PolicyFinderModule) clazz.newInstance();

            int j = 1;
            Properties finderModuleProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                finderModuleProps.put(props[0], props[1]);
            }

            finderModule.init(finderModuleProps);
            if (finderModule instanceof PDPPolicyStoreModule) {
                holder.addPolicyStore((PDPPolicyStoreModule) finderModule, finderModuleProps);
            }
            holder.addPolicyFinderModule(finderModule, finderModuleProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePolicyCollection(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        PolicyCollection collection = null;

        //only one policy collection can be there
        if (properties.getProperty("PDP.Policy.Collection") != null) {
            String className = properties.getProperty("PDP.Policy.Collection");
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            collection = (PolicyCollection) clazz.newInstance();

            int j = 1;
            Properties collectionProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                collectionProps.put(props[0], props[1]);
            }

            collection.init(collectionProps);
            holder.addPolicyCollection(collection, collectionProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePolicyStoreModule(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        PDPPolicyStoreModule policyStoreStore = null;

        if (properties.getProperty("PDP.Policy.Store.Module") != null) {
            String className = properties.getProperty("PDP.Policy.Store.Module");
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            policyStoreStore = (PDPPolicyStoreModule) clazz.newInstance();

            int j = 1;
            Properties storeProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                storeProps.put(props[0], props[1]);
            }

            policyStoreStore.init(storeProps);
            holder.addPolicyStore(policyStoreStore, storeProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePolicyDataStore(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        PolicyDataStoreModule policyDataStore = null;

        if (properties.getProperty("PDP.Policy.Data.Store.Module") != null) {
            String className = properties.getProperty("PDP.Policy.Data.Store.Module");
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            policyDataStore = (PolicyDataStoreModule) clazz.newInstance();

            int j = 1;
            Properties storeProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                storeProps.put(props[0], props[1]);
            }

            policyDataStore.init(storeProps);
            holder.addPolicyDataStore(policyDataStore, storeProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populateEntitlementDataFinders(Properties properties, EntitlementConfigHolder holder)
            throws Exception {
        int i = 1;
        EntitlementDataFinderModule metadata = null;

        while (properties.getProperty("PAP.Entitlement.Data.Finder." + i) != null) {
            String className = properties.getProperty("PAP.Entitlement.Data.Finder." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            metadata = (EntitlementDataFinderModule) clazz.newInstance();

            int j = 1;
            Properties metadataProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String value = properties.getProperty(className + "." + j++);
                metadataProps.put(value.substring(0, value.indexOf(",")),
                        value.substring(value.indexOf(",") + 1));
            }

            metadata.init(metadataProps);
            holder.addPolicyEntitlementDataFinder(metadata, metadataProps);
        }
    }


    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePolicyPublishers(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        int i = 1;
        PolicyPublisherModule publisher = null;

        while (properties.getProperty("PAP.Policy.Publisher.Module." + i) != null) {
            String className = properties.getProperty("PAP.Policy.Publisher.Module." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            publisher = (PolicyPublisherModule) clazz.newInstance();

            int j = 1;
            Properties publisherProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                publisherProps.put(props[0], props[1]);
            }

            publisher.init(publisherProps);
            holder.addPolicyPublisherModule(publisher, publisherProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePolicyVersionModule(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        PolicyVersionManagerModule versionManager = null;

        if (properties.getProperty("PAP.Policy.Version.Module") != null) {
            String className = properties.getProperty("PAP.Policy.Version.Module");
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            versionManager = (PolicyVersionManagerModule) clazz.newInstance();

            int j = 1;
            Properties storeProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                storeProps.put(props[0], props[1]);
            }

            versionManager.init(storeProps);
            holder.addPolicyVersionModule(versionManager, storeProps);
        }

    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePolicyPostPublishers(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        int i = 1;
        PostPublisherModule postPublisherModule = null;

        while (properties.getProperty("PAP.Policy.Post.Publisher.Module." + i) != null) {
            String className = properties.getProperty("PAP.Policy.Post.Publisher.Module." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            postPublisherModule = (PostPublisherModule) clazz.newInstance();

            int j = 1;
            Properties publisherProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                publisherProps.put(props[0], props[1]);
            }

            postPublisherModule.init(publisherProps);
            holder.addPolicyPostPublisherModule(postPublisherModule, publisherProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populatePublisherVerificationHandler(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        PublisherVerificationModule verificationModule = null;

        if (properties.getProperty("PAP.Policy.Publisher.Verification.Handler") != null) {
            String className = properties.getProperty("PAP.Policy.Publisher.Verification.Handler");
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            verificationModule = (PublisherVerificationModule) clazz.newInstance();

            int j = 1;
            Properties storeProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                storeProps.put(props[0], props[1]);
            }

            verificationModule.init(storeProps);
            holder.addPublisherVerificationModule(verificationModule, storeProps);
        }
    }

    /**
     * @param properties
     * @param holder
     * @throws Exception
     */
    private void populateAdminNotificationHandlers(Properties properties, EntitlementConfigHolder holder)
            throws Exception {

        int i = 1;
        PAPStatusDataHandlerModule handler = null;

        while (properties.getProperty("PAP.Status.Data.Handler." + i) != null) {
            String className = properties.getProperty("PAP.Status.Data.Handler." + i++);
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            handler = (PAPStatusDataHandlerModule) clazz.newInstance();

            int j = 1;
            Properties publisherProps = new Properties();
            while (properties.getProperty(className + "." + j) != null) {
                String[] props = properties.getProperty(className + "." + j++).split(",");
                publisherProps.put(props[0], props[1]);
            }

            handler.init(publisherProps);
            holder.addNotificationHandler(handler, publisherProps);
        }
    }
}
