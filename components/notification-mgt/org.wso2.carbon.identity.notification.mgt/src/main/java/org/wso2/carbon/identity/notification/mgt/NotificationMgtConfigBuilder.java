/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.identity.notification.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.notification.mgt.bean.ModuleConfiguration;
import org.wso2.carbon.identity.notification.mgt.bean.Subscription;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Configuration builder class for Message Management component. Responsible for reading msg-mgt
 * .properties file and extract properties and distribute them to relevant message sending
 * components.
 */
@SuppressWarnings("unused")
public class NotificationMgtConfigBuilder {

    private static final Log log = LogFactory.getLog(NotificationMgtConfigBuilder.class);
    /**
     * All properties configured in msg-mgt.properties file
     */
    private Properties notificationMgtConfigProperties;
    /**
     * Map of configurations which are specific to notification sending modules
     */
    private Map<String, ModuleConfiguration> moduleConfiguration;
    /**
     * Thread pool size for message sending task
     */
    private String threadPoolSize;

    /**
     * Load properties file and set Module properties
     *
     * @param bundleContext Bundle context
     * @throws NotificationManagementException
     */
    public NotificationMgtConfigBuilder(BundleContext bundleContext) throws NotificationManagementException {
        notificationMgtConfigProperties = loadProperties(bundleContext);
        setThreadPoolSize();
        resolveSecrets();
        moduleConfiguration = new HashMap<String, ModuleConfiguration>();
        build();
    }

    /**
     * Sets the thread pool size read from configurations
     */
    private void setThreadPoolSize() {
        threadPoolSize = (String) notificationMgtConfigProperties.remove(NotificationMgtConstants.Configs.
                THREAD_POOL_SIZE);
    }

    /**
     * Load properties which are defined in msg-mgt.properties file
     *
     * @param bundleContext Bundle context
     * @return Set of properties which are defined in msg-mgt.properties file
     * @throws NotificationManagementException
     */
    private Properties loadProperties(BundleContext bundleContext) throws NotificationManagementException {
        Properties properties = new Properties();
        InputStream inStream = null;

        // Open the default configuration file in carbon conf directory path .
        File MessageMgtPropertyFile = new File(CarbonUtils.getCarbonConfigDirPath() + File.separator + "identity" +
                File.separator, NotificationMgtConstants.MODULE_CONFIG_FILE);

        try {
            // If the configuration exists in the carbon conf directory, read properties from there
            if (MessageMgtPropertyFile.exists()) {
                inStream = new FileInputStream(MessageMgtPropertyFile);
                // Else read properties form either bundle context path or class path
            } else {
                URL url;
                if (bundleContext != null) {
                    if ((url = bundleContext.getBundle().getResource(NotificationMgtConstants.MODULE_CONFIG_FILE))
                            != null) {
                        inStream = url.openStream();
                    } else {
                        log.warn("Bundle context could not find resource " + NotificationMgtConstants
                                .MODULE_CONFIG_FILE);
                    }
                    // If bundle context is not found, read properties file from class path
                } else {

                    if ((url = this.getClass().getClassLoader().getResource(NotificationMgtConstants.
                            MODULE_CONFIG_FILE)) != null) {
                        inStream = url.openStream();
                    } else {
                        log.warn("Class resource loader could not find resource " + NotificationMgtConstants.
                                MODULE_CONFIG_FILE);
                    }
                }
            }

            if (inStream != null) {
                properties.load(inStream);
            }
            //Even if the configurations are not found, individual modules can behave themselves without configuration
        } catch (FileNotFoundException e) {
            log.warn("Could not find configuration file for Message Sending module", e);
        } catch (IOException e) {
            log.warn("Error while opening input stream for property file", e);
            // Finally close input stream
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error while closing input stream ", e);
            }
        }

        return properties;
    }

    /**
     * Build and store per module configuration objects
     */
    private void build() {
        Properties moduleNames = NotificationManagementUtils.getSubProperties(NotificationMgtConstants.Configs.
                MODULE_NAME, notificationMgtConfigProperties);
        Enumeration propertyNames = moduleNames.propertyNames();
        // Iterate through events and build event objects
        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String moduleName = (String) moduleNames.remove(key);
            moduleConfiguration.put(moduleName, buildModuleConfigurations(moduleName));
        }
    }

    /**
     * Building per module configuration objects
     *
     * @param moduleName Name of the module
     * @return ModuleConfiguration object which has configurations for the given module name
     */
    private ModuleConfiguration buildModuleConfigurations(String moduleName) {
        Properties moduleProperties = getModuleProperties(moduleName);
        List<Subscription> subscriptionList = buildSubscriptionList(moduleName, moduleProperties);

        return new ModuleConfiguration(getModuleProperties(moduleName), subscriptionList);
    }

    /**
     * Build a list of subscription by a particular module
     *
     * @param moduleName       Name of the module
     * @param moduleProperties Set of properties which
     * @return A list of subscriptions by the module
     */
    private List<Subscription> buildSubscriptionList(String moduleName, Properties moduleProperties) {
        // Get subscribed events
        Properties subscriptions = NotificationManagementUtils.getSubProperties(moduleName + "." +
                NotificationMgtConstants.Configs.SUBSCRIPTION, moduleProperties);

        List<Subscription> subscriptionList = new ArrayList<Subscription>();
        Enumeration propertyNames = subscriptions.propertyNames();
        // Iterate through events and build event objects
        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String subscriptionName = (String) subscriptions.remove(key);
            // Read all the event properties starting from the event prefix
            Properties subscriptionProperties = NotificationManagementUtils.getPropertiesWithPrefix
                    (moduleName + "." + NotificationMgtConstants.Configs.SUBSCRIPTION + "." + subscriptionName,
                            moduleProperties);
            Subscription subscription = new Subscription(subscriptionName, subscriptionProperties);
            subscriptionList.add(subscription);
        }
        return subscriptionList;
    }

    /**
     * Retrieve all properties defined for a particular module
     *
     * @param moduleName Name of the module
     * @return A set of properties which are defined for a particular module
     */
    private Properties getModuleProperties(String moduleName) {
        return NotificationManagementUtils.getPropertiesWithPrefix(moduleName, notificationMgtConfigProperties);
    }

    /**
     * Returns a module configuration object for the passed mdoule name
     *
     * @param moduleName Name of the module
     * @return Module configuration object which is relevant to the given name.
     */
    public ModuleConfiguration getModuleConfigurations(String moduleName) {
        return this.moduleConfiguration.get(moduleName);
    }

    public String getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * There can be sensitive information like passwords in configuration file. If they are encrypted using secure
     * vault, this method will resolve them and replace with original values.
     */
    private void resolveSecrets() {

        SecretResolver secretResolver = SecretResolverFactory.create(notificationMgtConfigProperties);
        Enumeration propertyNames = notificationMgtConfigProperties.propertyNames();
        if (secretResolver != null && secretResolver.isInitialized()) {
            // Iterate through whole config file and find encrypted properties and resolve them
            while (propertyNames.hasMoreElements()) {
                String key = (String) propertyNames.nextElement();
                if (secretResolver.isTokenProtected(key)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resolving and replacing secret for " + key);
                    }
                    // Resolving the secret password.
                    String value = secretResolver.resolve(key);
                    // Replaces the original encrypted property with resolved property
                    notificationMgtConfigProperties.put(key, value);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No encryption done for value with key :" + key);
                    }
                }
            }
        } else {
            if(log.isDebugEnabled()){
                log.debug("Secret Resolver is not present. Will not resolve encryptions in config file");
            }
        }
    }
}
