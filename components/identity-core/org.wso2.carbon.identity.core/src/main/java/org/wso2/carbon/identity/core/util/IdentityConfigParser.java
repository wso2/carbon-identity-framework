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
package org.wso2.carbon.identity.core.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.model.IdentityCacheConfigKey;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfigKey;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class IdentityConfigParser {

    private static Map<String, Object> configuration = new HashMap<String, Object>();
    private static Map<IdentityEventListenerConfigKey, IdentityEventListenerConfig> eventListenerConfiguration = new HashMap();
    private static Map<IdentityCacheConfigKey, IdentityCacheConfig> identityCacheConfigurationHolder = new HashMap();
    private static IdentityConfigParser parser;
    private static SecretResolver secretResolver;
    // To enable attempted thread-safety using double-check locking
    private static Object lock = new Object();
    private static Log log = LogFactory.getLog(IdentityConfigParser.class);
    private static String configFilePath;

    private OMElement rootElement;

    private IdentityConfigParser() {
        buildConfiguration();
    }

    public static IdentityConfigParser getInstance() {
        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    parser = new IdentityConfigParser();
                }
            }
        }
        return parser;
    }

    public static IdentityConfigParser getInstance(String filePath) {
        configFilePath = filePath;
        return getInstance();
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public static Map<IdentityEventListenerConfigKey, IdentityEventListenerConfig> getEventListenerConfiguration() {
        return eventListenerConfiguration;
    }

    public static Map<IdentityCacheConfigKey, IdentityCacheConfig> getIdentityCacheConfigurationHolder() {
        return identityCacheConfigurationHolder;
    }

    /**
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    private void buildConfiguration() {
        InputStream inStream = null;
        StAXOMBuilder builder = null;

        String warningMessage = "";
        try {
            if (configFilePath != null) {
                File identityConfigXml = new File(configFilePath);
                if (identityConfigXml.exists()) {
                    inStream = new FileInputStream(identityConfigXml);
                }
            } else {

                File identityConfigXml = new File(IdentityUtil.getIdentityConfigDirPath(),
                        IdentityCoreConstants.IDENTITY_CONFIG);
                if (identityConfigXml.exists()) {
                    inStream = new FileInputStream(identityConfigXml);
                }
                /*Following seems a wrong use of a class inside internal package (IdentityCoreServiceComponent),
                outside that package which causes hard to troubleshoot CNF errors in certain occasions.
                Besides, identity.xml is not present in the */
                /*if (inStream == null) {
                    URL url;
                    BundleContext bundleContext = IdentityCoreServiceComponent.getBundleContext();
                    if (bundleContext != null) {
                        if ((url = bundleContext.getBundle().getResource(IDENTITY_CONFIG)) != null) {
                            inStream = url.openStream();
                        } else {
                            warningMessage = "Bundle context could not find resource " + IDENTITY_CONFIG +
                                    " or user does not have sufficient permission to access the resource.";
                        }

                    } else {

                        if ((url = this.getClass().getClassLoader().getResource(IDENTITY_CONFIG)) != null) {
                            inStream = url.openStream();
                        } else {
                            warningMessage = "Identity core could not find resource " + IDENTITY_CONFIG +
                                    " or user does not have sufficient permission to access the resource.";
                        }
                    }
                }*/
            }

            if (inStream == null) {
                String message = "Identity configuration not found. Cause - " + warningMessage;
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new FileNotFoundException(message);
            }

            builder = new StAXOMBuilder(inStream);
            rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<String>();
            secretResolver = SecretResolverFactory.create(rootElement, true);
            readChildElements(rootElement, nameStack);
            buildEventListenerData();
            buildCacheConfig();

        } catch (IOException|XMLStreamException e) {
            throw IdentityRuntimeException.error("Error occurred while building configuration from identity.xml", e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the input stream for identity.xml", e);
            }
        }
    }

    private void buildEventListenerData() {
        OMElement eventListeners = this.getConfigElement(IdentityConstants.EVENT_LISTENERS);
        if (eventListeners != null) {
            Iterator<OMElement> eventListener = eventListeners.getChildrenWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, IdentityConstants.EVENT_LISTENER));

            if (eventListener != null) {
                while (eventListener.hasNext()) {
                    OMElement eventListenerElement = eventListener.next();
                    String eventListenerType = eventListenerElement.getAttributeValue(new QName(
                            IdentityConstants.EVENT_LISTENER_TYPE));
                    String eventListenerName = eventListenerElement.getAttributeValue(new QName(
                            IdentityConstants.EVENT_LISTENER_NAME));
                    int order = Integer.parseInt(eventListenerElement.getAttributeValue(new QName(
                            IdentityConstants.EVENT_LISTENER_ORDER)));
                    String enable = eventListenerElement.getAttributeValue(new QName(
                            IdentityConstants.EVENT_LISTENER_ENABLE));

                    if (StringUtils.isBlank(eventListenerType) || StringUtils.isBlank(eventListenerName)) {
                        throw IdentityRuntimeException.error("eventListenerType or eventListenerName is not defined " +
                                "correctly");
                    }
                    IdentityEventListenerConfigKey configKey = new IdentityEventListenerConfigKey(eventListenerType, eventListenerName);
                    IdentityEventListenerConfig identityEventListenerConfig = new IdentityEventListenerConfig(enable, order, configKey);
                    eventListenerConfiguration.put(configKey, identityEventListenerConfig);

                }
            }

        }
    }

    private void buildCacheConfig() {
        OMElement cacheConfig = this.getConfigElement(IdentityConstants.CACHE_CONFIG);
        if (cacheConfig != null) {
            Iterator<OMElement> cacheManagers = cacheConfig.getChildrenWithName(
                    new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, IdentityConstants.CACHE_MANAGER));

            if (cacheManagers != null) {
                while (cacheManagers.hasNext()) {
                    OMElement cacheManager = cacheManagers.next();

                    String cacheManagerName = cacheManager.getAttributeValue(new QName(
                            IdentityConstants.CACHE_MANAGER_NAME));

                    if (StringUtils.isBlank(cacheManagerName)) {
                        throw IdentityRuntimeException.error("CacheManager name not defined correctly");
                    }

                    Iterator<OMElement> caches = cacheManager.getChildrenWithName(
                            new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, IdentityConstants.CACHE));

                    if (caches != null) {
                        while (caches.hasNext()) {
                            OMElement cache = caches.next();

                            String cacheName = cache.getAttributeValue(new QName(IdentityConstants.CACHE_NAME));

                            if (StringUtils.isBlank(cacheName)) {
                                throw IdentityRuntimeException.error("Cache name not defined correctly");
                            }

                            IdentityCacheConfigKey identityCacheConfigKey = new IdentityCacheConfigKey(cacheManagerName,
                                    cacheName);
                            IdentityCacheConfig identityCacheConfig = new IdentityCacheConfig(identityCacheConfigKey);

                            String enable = cache.getAttributeValue(new QName(IdentityConstants.CACHE_ENABLE));
                            if (StringUtils.isNotBlank(enable)) {
                                identityCacheConfig.setEnabled(Boolean.parseBoolean(enable));
                            }

                            String timeout = cache.getAttributeValue(new QName(IdentityConstants.CACHE_TIMEOUT));
                            if (StringUtils.isNotBlank(timeout)) {
                                identityCacheConfig.setTimeout(Integer.parseInt(timeout));
                            }

                            String capacity = cache.getAttributeValue(new QName(IdentityConstants.CACHE_CAPACITY));
                            if (StringUtils.isNotBlank(capacity)) {
                                identityCacheConfig.setCapacity(Integer.parseInt(capacity));
                            }

                            // Add the config to container
                            identityCacheConfigurationHolder.put(identityCacheConfigKey, identityCacheConfig);
                        }
                    }
                }
            }

        }
    }

    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {
        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (secretResolver != null && secretResolver.isInitialized() &&
                        secretResolver.isTokenProtected(key)) {
                    value = secretResolver.resolve(key);
                }
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    ArrayList list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        ArrayList arrayList = new ArrayList(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        configuration.put(key, arrayList);
                    }
                }
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    private String getKey(Stack<String> nameStack) {
        StringBuffer key = new StringBuffer();
        for (int i = 0; i < nameStack.size(); i++) {
            String name = nameStack.elementAt(i);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));

        return key.toString();
    }

    private boolean elementHasText(OMElement element) {
        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    private String replaceSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
                && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String sysProp = text.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue
                        + text.substring(indexOfClosingBrace + 1);
            }
            if (sysProp.equals(ServerConstants.CARBON_HOME)) {
                if (System.getProperty(ServerConstants.CARBON_HOME).equals(".")) {
                    text = new File(".").getAbsolutePath() + File.separator + text;
                }
            }
        }
        return text;
    }

    /**
     * Returns the element with the provided local part
     *
     * @param localPart local part name
     * @return Corresponding OMElement
     */
    public OMElement getConfigElement(String localPart) {
        return rootElement.getFirstChildWithName(new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE,localPart));
    }

}
