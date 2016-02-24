/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.builder;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.ServerConstants;

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
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Application Authenticators Framework configuration reader.
 */
public class FileBasedConfigurationBuilder {

    private static final Log log = LogFactory.getLog(FileBasedConfigurationBuilder.class);
    private static volatile FileBasedConfigurationBuilder instance;
    private static String configFilePath;
    private static OMElement rootElement;
    private static Map<String, Object> configuration = new HashMap<String, Object>();

    private String authenticationEndpointURL;
    private String authenticationEndpointRetryURL;

    /**
     * List of URLs that receive the tenant list
     */
    private List<String> tenantDataEndpointURLs = new ArrayList<String>();

    /**
     * Tenant list dropdown enabled or disabled value
     */
    private boolean isTenantDomainDropdownEnabled;

    private boolean isDumbMode;
    private List<ExternalIdPConfig> idpList = new ArrayList<>();
    private List<SequenceConfig> sequenceList = new ArrayList<>();
    private List<String> authEndpointQueryParams = new ArrayList<>();
    private Map<String, AuthenticatorConfig> authenticatorConfigMap = new HashMap<>();
    private Map<String, Object> extensions = new HashMap<>();
    private int maxLoginAttemptCount = 5;
    private Map<String, String> authenticatorNameMappings = new HashMap<>();
    private Map<String, Integer> cacheTimeouts = new HashMap<>();
    private String authEndpointQueryParamsAction;
    private boolean authEndpointQueryParamsConfigAvailable;

    public static FileBasedConfigurationBuilder getInstance() {
        if (instance == null) {
            synchronized (FileBasedConfigurationBuilder.class){
                if(instance == null) {
                    instance = new FileBasedConfigurationBuilder();
                }
            }
        }
        return instance;
    }

    public static FileBasedConfigurationBuilder getInstance(String filePath) {
        configFilePath = filePath;
        return getInstance();
    }

    /**
     * Returns the element with the provided local part
     *
     * @param localPart local part name
     * @return Corresponding OMElement
     */
    public OMElement getConfigElement(String localPart) {
        return rootElement.getFirstChildWithName(
                IdentityApplicationManagementUtil.getQNameWithIdentityApplicationNS(localPart));
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    private FileBasedConfigurationBuilder(){
        buildConfiguration();
    }

    /**
     * Read the authenticator info from the file and populate the in-memory model
     */
    private void buildConfiguration() {

        InputStream inStream = null;
        File configFile = null;
        try {
            if (configFilePath != null) {
                configFile = new File(configFilePath);
            } else {
                configFile = new File(IdentityUtil.getIdentityConfigDirPath(),
                        IdentityApplicationConstants.APPLICATION_AUTHENTICATION_CONGIG);
            }
            if (configFile.exists()) {
                inStream = new FileInputStream(configFile);
            }
            if (inStream == null) {
                String message = "Identity Application Authentication Framework configuration not found";
                log.error(message);
                throw new FileNotFoundException(message);
            }
            StAXOMBuilder builder = new StAXOMBuilder(inStream);
            rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<String>();
            readChildElements(rootElement, nameStack);

            //########### Read Authentication Endpoint URL ###########
            readAuthenticationEndpointURL(rootElement);
            readAuthenticationEndpointRetryURL(rootElement);

            //########### Read tenant data listener URLs ###########
            readTenantDataListenerURLs(rootElement);

            //########### Read tenant domain dropdown enabled value ###########
            readTenantDomainDropdownEnabledValue(rootElement);

            //########### Read Proxy Mode ###########
            readProxyModes(rootElement);

            //########### Read Maximum Login Attempt Count ###########
            readMaximumLoginAttemptCount(rootElement);

            // ########### Read Authentication Endpoint Query Params ###########
            readAuthenticationEndpointQueryParams(rootElement);

            //########### Read Extension Points ###########
            readExtensionPoints(rootElement);

            //########### Read Cache Timeouts ###########
            readCacheTimeouts(rootElement);

            //########### Read Authenticator Name Mappings ###########
            readAuthenticatorNameMappings(rootElement);

            //########### Read Authenticator Configs ###########
            readAuthenticatorConfigs(rootElement);

            //########### Read IdP Configs ###########
            readIdpConfigs(rootElement);

            //########### Read Sequence Configs ###########
            readSequenceConfigs(rootElement);

        } catch (FileNotFoundException e) {
            log.error(IdentityApplicationConstants.APPLICATION_AUTHENTICATION_CONGIG + " file is not available", e);
        } catch (XMLStreamException e) {
            log.error("Error reading the " + IdentityApplicationConstants.APPLICATION_AUTHENTICATION_CONGIG, e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error occurred while closing the FileInputStream after reading " +
                        "Identity Application Authentication Framework configuration", e);
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
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    List<String> list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        List arrayList = new ArrayList(2);
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

        StringBuilder key = new StringBuilder();
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
        String tmpText = null;
        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
                && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String sysProp = text.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                tmpText = text.substring(0, indexOfStartingChars) + propValue
                        + text.substring(indexOfClosingBrace + 1);
            }

            if ((ServerConstants.CARBON_HOME).equals(sysProp) &&
                    (".").equals(System.getProperty(ServerConstants.CARBON_HOME))) {
                tmpText = new File(".").getAbsolutePath() + File.separator + text;

            }
        }
        return tmpText;
    }

    private void readSequenceConfigs(OMElement documentElement) {
        OMElement sequencesElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_SEQUENCES));

        if (sequencesElem != null) {
            // for each every application defined, create a ApplicationBean instance
            for (Iterator sequenceElements = sequencesElem.getChildrenWithLocalName(FrameworkConstants.Config.ELEM_SEQUENCE);
                 sequenceElements.hasNext(); ) {
                SequenceConfig sequenceConfig = processSequenceElement((OMElement) sequenceElements.next());

                if (sequenceConfig != null) {
                    this.sequenceList.add(sequenceConfig);
                }
            }
        }
    }

    private void readIdpConfigs(OMElement documentElement) {
        OMElement idpConfigsElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_IDP_CONFIGS));

        if (idpConfigsElem != null) {
            // for each and every external idp defined, create an ExternalIdPConfig instance
            for (Iterator idpConfigElements = idpConfigsElem.getChildrenWithLocalName(FrameworkConstants.Config.ELEM_IDP_CONFIG);
                 idpConfigElements.hasNext(); ) {

                ExternalIdPConfig idpConfig = processIdPConfigElement((OMElement) idpConfigElements.next());

                if (idpConfig != null) {
                    idpList.add(idpConfig);
                }
            }
        }
    }

    private void readAuthenticatorConfigs(OMElement documentElement) {
        OMElement authenticatorConfigsElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_AUTHENTICATOR_CONFIGS));

        if (authenticatorConfigsElem != null) {
            // for each and every authenticator defined, create an AuthenticatorConfig instance
            for (Iterator authenticatorConfigElements = authenticatorConfigsElem.getChildrenWithLocalName(FrameworkConstants.Config.ELEM_AUTHENTICATOR_CONFIG);
                 authenticatorConfigElements.hasNext(); ) {
                AuthenticatorConfig authenticatorConfig = processAuthenticatorConfigElement((OMElement) authenticatorConfigElements.next());

                if (authenticatorConfig != null) {
                    this.authenticatorConfigMap.put(authenticatorConfig.getName(), authenticatorConfig);
                }
            }
        }
    }

    private void readAuthenticatorNameMappings(OMElement documentElement) {
        OMElement authenticatorNameMappingsElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_AUTHENTICATOR_NAME_MAPPINGS));

        if (authenticatorNameMappingsElem != null) {
            for (Iterator authenticatorNameMappingElems = authenticatorNameMappingsElem.getChildrenWithLocalName(FrameworkConstants.Config.ELEM_AUTHENTICATOR_NAME_MAPPING);
                 authenticatorNameMappingElems.hasNext(); ) {
                processAuthenticatorNameMappingElement((OMElement) authenticatorNameMappingElems.next());
            }
        }
    }

    private void readCacheTimeouts(OMElement documentElement) {
        OMElement cacheTimeoutsElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_CACHE_TIMEOUTS));

        if (cacheTimeoutsElem != null) {
            for (Iterator cacheChildElems = cacheTimeoutsElem.getChildElements(); cacheChildElems.hasNext(); ) {
                OMElement cacheTimeoutElem = (OMElement) cacheChildElems.next();
                String value = cacheTimeoutElem.getText();

                if (value != null && value.trim().length() > 0) {
                    readCacheTimeOut(cacheTimeoutElem, value);
                }
            }
        }
    }

    private void readExtensionPoints(OMElement documentElement) {
        OMElement extensionsElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_EXTENSIONS));

        if (extensionsElem != null) {
            for (Iterator extChildElems = extensionsElem.getChildElements(); extChildElems.hasNext(); ) {
                OMElement extensionElem = (OMElement) extChildElems.next();
                instantiateClass(extensionElem);
            }
        }
    }

    private void readAuthenticationEndpointQueryParams(OMElement documentElement) {
        OMElement authEndpointQueryParamsElem = documentElement
                .getFirstChildWithName(IdentityApplicationManagementUtil
                                               .getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_AUTH_ENDPOINT_QUERY_PARAMS));

        if (authEndpointQueryParamsElem != null) {

            authEndpointQueryParamsConfigAvailable = true;
            OMAttribute actionAttr = authEndpointQueryParamsElem.getAttribute(new QName(
                    FrameworkConstants.Config.ATTR_AUTH_ENDPOINT_QUERY_PARAM_ACTION));
            authEndpointQueryParamsAction = FrameworkConstants.AUTH_ENDPOINT_QUERY_PARAMS_ACTION_EXCLUDE;

            if (actionAttr != null) {
                String actionValue = actionAttr.getAttributeValue();

                if (actionValue != null && !actionValue.isEmpty()) {
                    authEndpointQueryParamsAction = actionValue;
                }
            }


            for (Iterator authEndpointQueryParamElems = authEndpointQueryParamsElem
                    .getChildrenWithLocalName(FrameworkConstants.Config.ELEM_AUTH_ENDPOINT_QUERY_PARAM); authEndpointQueryParamElems
                         .hasNext(); ) {
                String queryParamName = processAuthEndpointQueryParamElem((OMElement) authEndpointQueryParamElems
                        .next());

                if (queryParamName != null) {
                    this.authEndpointQueryParams.add(queryParamName);
                }
            }
        }
    }

    private void readMaximumLoginAttemptCount(OMElement documentElement) {
        OMElement maxLoginAttemptCountElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_MAX_LOGIN_ATTEMPT_COUNT));

        if (maxLoginAttemptCountElem != null) {
            String maxLoginAttemptCountStr = maxLoginAttemptCountElem.getText();

            if (maxLoginAttemptCountStr != null && !maxLoginAttemptCountStr.isEmpty()) {
                try {
                    maxLoginAttemptCount = Integer.parseInt(maxLoginAttemptCountElem.getText());
                } catch (NumberFormatException e) {
                    log.error("MaxLoginAttemptCount must be a number");
                    maxLoginAttemptCount = 5;
                }
            }
        }
    }

    private void readProxyModes(OMElement documentElement) {
        //TODO:get proxy modes from an enum?
        OMElement proxyModeElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_PROXY_MODE));

        if (proxyModeElem != null && proxyModeElem.getText() != null && !proxyModeElem.getText().isEmpty() &&
            "dumb".equalsIgnoreCase(proxyModeElem.getText())) {
            isDumbMode = true;
        }
    }

    private void readTenantDomainDropdownEnabledValue(OMElement documentElement) {
        OMElement tenantDomainDropdownElem =
                documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                        getQNameWithIdentityApplicationNS(
                                FrameworkConstants.Config.QNAME_TENANT_DOMAIN_DROPDOWN_ENABLED));

        if (tenantDomainDropdownElem != null) {
            isTenantDomainDropdownEnabled = Boolean.parseBoolean(tenantDomainDropdownElem.getText());
        }
    }

    private void readTenantDataListenerURLs(OMElement documentElement) {
        OMElement tenantDataURLsElem =
                documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                        getQNameWithIdentityApplicationNS(
                                FrameworkConstants.Config.QNAME_TENANT_DATA_LISTENER_URLS));

        if (tenantDataURLsElem != null) {
            for (Iterator tenantDataURLElems = tenantDataURLsElem.getChildrenWithLocalName(
                    FrameworkConstants.Config.ELEM_TENANT_DATA_LISTENER_URL);
                 tenantDataURLElems.hasNext(); ) {

                OMElement tenantDataListenerURLElem = (OMElement) tenantDataURLElems.next();
                if (tenantDataListenerURLElem != null &&
                    StringUtils.isNotEmpty(tenantDataListenerURLElem.getText())) {
                    tenantDataEndpointURLs.add(IdentityUtil.fillURLPlaceholders(tenantDataListenerURLElem.getText()));
                }
            }
        }
    }

    private void readAuthenticationEndpointURL(OMElement documentElement) {
        OMElement authEndpointURLElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_AUTHENTICATION_ENDPOINT_URL));

        if (authEndpointURLElem != null) {
            authenticationEndpointURL = IdentityUtil.fillURLPlaceholders(authEndpointURLElem.getText());
        }
    }

    private void readAuthenticationEndpointRetryURL(OMElement documentElement) {
        OMElement authEndpointRetryURLElem = documentElement.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.QNAME_AUTHENTICATION_ENDPOINT_RETRY_URL));

        if (authEndpointRetryURLElem != null) {
            authenticationEndpointRetryURL = IdentityUtil.fillURLPlaceholders(authEndpointRetryURLElem.getText());
        }
    }

    private void readCacheTimeOut(OMElement cacheTimeoutElem, String value) {
        Integer timeout;

        try {
            timeout = Integer.valueOf(value);
            cacheTimeouts.put(cacheTimeoutElem.getLocalName(), timeout);
        } catch (NumberFormatException e) {
            log.warn(cacheTimeoutElem.getLocalName() + "doesn't have a numeric value specified." +
                     "Entry is ignored");
        }
    }

    private void instantiateClass(OMElement extensionElem) {
        Class<?> clazz;
        Object obj;
        try {
            clazz = Class.forName(extensionElem.getText());
            obj = clazz.newInstance();
            extensions.put(extensionElem.getLocalName(), obj);
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Stack trace skipped in Error log  : ", e);
            }
            log.error("ClassNotFoundException while trying to find class " + extensionElem.getText());
        } catch (InstantiationException e) {
            if (log.isDebugEnabled()) {
                log.debug("Stack trace skipped in Error log   : ", e);
            }
            log.error("InstantiationException while trying to instantiate class " + extensionElem.getText());
        } catch (IllegalAccessException e) {
            if (log.isDebugEnabled()) {
                log.debug("Stack trace skipped in Error log  : ", e);
            }
            log.error("IllegalAccessException while trying to instantiate class " + extensionElem.getText());
        }
    }

    private String processAuthEndpointQueryParamElem(OMElement authEndpointQueryParamElem) {

        OMAttribute nameAttr = authEndpointQueryParamElem.getAttribute(new QName(
                FrameworkConstants.Config.ATTR_AUTH_ENDPOINT_QUERY_PARAM_NAME));

        if (nameAttr == null) {
            log.warn("Each Authentication Endpoint Query Param should have a unique name attribute. This Query Param will skipped.");
            return null;
        }

        return nameAttr.getAttributeValue();
    }

    private void processAuthenticatorNameMappingElement(OMElement authenticatorNameMappingElem) {

        OMAttribute nameAttr = authenticatorNameMappingElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_AUTHENTICATOR_NAME_MAPPING_NAME));
        OMAttribute aliasAttr = authenticatorNameMappingElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_AUTHENTICATOR_NAME_MAPPING_ALIAS));

        if (nameAttr == null || aliasAttr == null) {
            log.warn("An AuthenticatorNameMapping must contain \'name\' and \'alias\' attributes. Skipping the element.");
            return;
        }

        authenticatorNameMappings.put(aliasAttr.getAttributeValue(), nameAttr.getAttributeValue());
    }

    /**
     * Create SequenceDOs for each sequence entry
     *
     * @param sequenceElem
     * @return
     */
    private SequenceConfig processSequenceElement(OMElement sequenceElem) {
        SequenceConfig sequenceConfig = new SequenceConfig();

        String applicationId = "default";
        OMAttribute appIdAttr = sequenceElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_APPLICATION_ID));

        if (appIdAttr != null) {
            applicationId = appIdAttr.getAttributeValue();
        }

        sequenceConfig.setApplicationId(applicationId);

        OMAttribute forceAuthnAttr = sequenceElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_FORCE_AUTHENTICATE));

        if (forceAuthnAttr != null) {
            sequenceConfig.setForceAuthn(Boolean.valueOf(forceAuthnAttr.getAttributeValue()));
        }

        OMAttribute checkAuthnAttr = sequenceElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_CHECK_AUTHENTICATE));

        if (checkAuthnAttr != null) {
            sequenceConfig.setCheckAuthn(Boolean.valueOf(checkAuthnAttr.getAttributeValue()));
        }

        //RequestPathAuthenticators
        OMElement reqPathAuthenticatorsElem = sequenceElem.getFirstChildWithName(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.ELEM_REQ_PATH_AUTHENTICATOR));

        if (reqPathAuthenticatorsElem != null) {

            for (Iterator reqPathAuthenticatorElems = reqPathAuthenticatorsElem.getChildElements(); reqPathAuthenticatorElems.hasNext(); ) {
                OMElement reqPathAuthenticatorElem = (OMElement) reqPathAuthenticatorElems.next();

                String authenticatorName = reqPathAuthenticatorElem.getAttributeValue(IdentityApplicationManagementUtil.
                        getQNameWithIdentityApplicationNS(FrameworkConstants.Config.ATTR_AUTHENTICATOR_NAME));
                AuthenticatorConfig authenticatorConfig = authenticatorConfigMap.get(authenticatorName);
                sequenceConfig.getReqPathAuthenticators().add(authenticatorConfig);
            }
        }

        // for each step defined, create a StepDO instance
        for (Iterator stepElements = sequenceElem.getChildrenWithLocalName(FrameworkConstants.Config.ELEM_STEP);
             stepElements.hasNext(); ) {
            StepConfig stepConfig = processStepElement((OMElement) stepElements.next());

            if (stepConfig != null) {
                sequenceConfig.getStepMap().put(stepConfig.getOrder(), stepConfig);
            }
        }

        return sequenceConfig;
    }

    /**
     * Create StepDOs for each step entry
     *
     * @param stepElem
     * @return
     */
    private StepConfig processStepElement(OMElement stepElem) {

        StepConfig stepConfig = new StepConfig();
        OMAttribute loginPageAttr = stepElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_STEP_LOGIN_PAGE));

        if (loginPageAttr != null) {
            stepConfig.setLoginPage(loginPageAttr.getAttributeValue());
        }

        OMAttribute orderAttr = stepElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_STEP_ORDER));

        if (orderAttr == null) {
            log.warn("Each Step Configuration should have an order. +"
                     + "Authenticators under this Step will not be registered.");
            return null;
        }

        stepConfig.setOrder(Integer.parseInt(orderAttr.getAttributeValue()));

        for (Iterator authenticatorElements = stepElem.getChildrenWithLocalName(FrameworkConstants.Config.ELEM_AUTHENTICATOR);
             authenticatorElements.hasNext(); ) {
            OMElement authenticatorElem = (OMElement) authenticatorElements.next();

            String authenticatorName = authenticatorElem.getAttributeValue(new QName(FrameworkConstants.Config.ATTR_AUTHENTICATOR_NAME));
            AuthenticatorConfig authenticatorConfig = authenticatorConfigMap.get(authenticatorName);
            String idps = authenticatorElem.getAttributeValue(new QName(FrameworkConstants.Config.ATTR_AUTHENTICATOR_IDPS));

            //if idps defined
            if (idps != null && !idps.isEmpty()) {
                String[] idpArr = idps.split(",");

                for (String idp : idpArr) {
                    authenticatorConfig.getIdpNames().add(idp);
                }
            } else {
                authenticatorConfig.getIdpNames().add(FrameworkConstants.LOCAL_IDP_NAME);
            }

            stepConfig.getAuthenticatorList().add(authenticatorConfig);
        }

        return stepConfig;
    }

    /**
     * Create AuthenticatorBean elements for each authenticator entry
     *
     * @param authenticatorConfigElem OMElement for Authenticator
     * @return AuthenticatorBean object
     */
    private AuthenticatorConfig processAuthenticatorConfigElement(OMElement authenticatorConfigElem) {

        // read the name of the authenticator. this is a mandatory attribute.
        OMAttribute nameAttr = authenticatorConfigElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_AUTHENTICATOR_CONFIG_NAME));
        // if the name is not given, do not register this authenticator
        if (nameAttr == null) {
            log.warn("Each Authenticator Configuration should have a unique name attribute. +" +
                     "This Authenticator will not be registered.");
            return null;
        }

        String authenticatorName = nameAttr.getAttributeValue();

        // check whether the disabled attribute is set
        boolean enabled = false;

        if (authenticatorConfigElem.getAttribute(IdentityApplicationManagementUtil.
                getQNameWithIdentityApplicationNS(FrameworkConstants.Config.ATTR_AUTHENTICATOR_ENABLED)) != null) {
            enabled = Boolean.parseBoolean(authenticatorConfigElem.getAttribute(IdentityApplicationManagementUtil.
                    getQNameWithIdentityApplicationNS(FrameworkConstants.Config.ATTR_AUTHENTICATOR_ENABLED)).getAttributeValue());
        }

        // read the config parameters
        Map<String, String> parameterMap = new HashMap<>();

        for (Iterator paramIterator = authenticatorConfigElem.getChildrenWithLocalName(FrameworkConstants.Config.ELEM_PARAMETER);
             paramIterator.hasNext(); ) {
            OMElement paramElem = (OMElement) paramIterator.next();
            OMAttribute paramNameAttr = paramElem.getAttribute(new QName(FrameworkConstants.Config.ATTR_PARAMETER_NAME));

            if (paramNameAttr == null) {
                log.warn("An Authenticator Parameter should have a name attribute. Skipping the parameter.");
                continue;
            }

            parameterMap.put(paramNameAttr.getAttributeValue(), paramElem.getText());
        }

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig(authenticatorName, enabled, parameterMap);
        authenticatorConfig.setApplicationAuthenticator(FrameworkUtils.getAppAuthenticatorByName(authenticatorName));

        return authenticatorConfig;
    }

    private ExternalIdPConfig processIdPConfigElement(OMElement idpConfigElem) {

        OMAttribute nameAttr = idpConfigElem.getAttribute(new QName("name"));

        // if the name is not given, do not register this config
        if (nameAttr == null) {
            log.warn("Each IDP configuration should have a unique name attribute");
            return null;
        }

        // read the config parameters
        Map<String, String> parameterMap = new HashMap<>();

        for (Iterator paramIterator = idpConfigElem.getChildrenWithLocalName("Parameter");
             paramIterator.hasNext(); ) {
            OMElement paramElem = (OMElement) paramIterator.next();
            OMAttribute paramNameAttr = paramElem.getAttribute(new QName("name"));

            if (paramNameAttr == null) {
                log.warn("A Parameter should have a name attribute. Skipping the parameter.");
                continue;
            }

            parameterMap.put(paramNameAttr.getAttributeValue(), paramElem.getText());
        }

        IdentityProvider fedIdp = new IdentityProvider();
        fedIdp.setIdentityProviderName(nameAttr.getAttributeValue());
        ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(fedIdp);
        externalIdPConfig.setParameterMap(parameterMap);

        return externalIdPConfig;

    }

    public AuthenticatorConfig getAuthenticatorBean(String authenticatorName) {
        return authenticatorConfigMap.get(authenticatorName);
    }

    public Map<String, AuthenticatorConfig> getAuthenticatorConfigMap() {
        return authenticatorConfigMap;
    }

    public SequenceConfig findSequenceByApplicationId(String appId) {

        for (SequenceConfig seq : sequenceList) {

            if (seq.getApplicationId() != null && seq.getApplicationId().equalsIgnoreCase(appId)) {
                return seq;
            }
        }

        return null;
    }

    public List<SequenceConfig> getSequenceList() {
        return sequenceList;
    }


    public List<ExternalIdPConfig> getIdpList() {
        return idpList;
    }

    public ExternalIdPConfig getIdPConfigs(String name) {
        for (ExternalIdPConfig externalIdPConfig : idpList) {

            if (externalIdPConfig.getName().equals(name)) {
                return externalIdPConfig;
            }
        }

        return null;
    }

    public List<String> getAuthEndpointQueryParams() {
        return authEndpointQueryParams;
    }

    public String getAuthEndpointQueryParamsAction() {
        return authEndpointQueryParamsAction;
    }

    public boolean isAuthEndpointQueryParamsConfigAvailable() {
        return authEndpointQueryParamsConfigAvailable;
    }

    public String getAuthenticationEndpointURL() {
        return authenticationEndpointURL;
    }

    public void setAuthenticationEndpointURL(String authenticationEndpointURL) {
        this.authenticationEndpointURL = authenticationEndpointURL;
    }

    public String getAuthenticationEndpointRetryURL() {
        return authenticationEndpointRetryURL;
    }

    public void setAuthenticationEndpointRetryURL(String authenticationEndpointRetryURL) {
        this.authenticationEndpointRetryURL = authenticationEndpointRetryURL;
    }

    /**
     * Get the tenant list receiving urls
     *
     * @return Tenant list receiving urls
     */
    public List<String> getTenantDataEndpointURLs() {
        return tenantDataEndpointURLs;
    }

    /**
     * Get the value of tenant list dropdown enabled or disabled
     *
     * @return Tenant list dropdown enabled or disabled
     */
    public boolean isTenantDomainDropdownEnabled() {
        return isTenantDomainDropdownEnabled;
    }

    public boolean isDumbMode() {
        return isDumbMode;
    }

    public int getMaxLoginAttemptCount() {
        return maxLoginAttemptCount;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public Map<String, String> getAuthenticatorNameMappings() {
        return authenticatorNameMappings;
    }

    public Map<String, Integer> getCacheTimeouts() {
        return cacheTimeouts;
    }

    public boolean isForceAuthnEnabled(String appId) {

        for (SequenceConfig seqConfig : sequenceList) {

            if (seqConfig.getApplicationId().equalsIgnoreCase(appId)) {
                return seqConfig.isForceAuthn();
            }
        }

        return false;
    }

    public boolean isCheckAuthnEnabled(String appId) {

        for (SequenceConfig seqConfig : sequenceList) {

            if (seqConfig.getApplicationId().equalsIgnoreCase(appId)) {
                return seqConfig.isCheckAuthn();
            }
        }

        return false;
    }
} 
