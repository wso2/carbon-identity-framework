/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.mgt.EventDistributionTask;
import org.wso2.carbon.identity.mgt.EventMgtConstants;
import org.wso2.carbon.identity.mgt.EventMgtException;
import org.wso2.carbon.identity.mgt.EventMgtConfigBuilder;
import org.wso2.carbon.identity.mgt.bean.ModuleConfiguration;
import org.wso2.carbon.identity.mgt.event.Event;
import org.wso2.carbon.identity.mgt.handler.EventHandler;
import org.wso2.carbon.identity.mgt.internal.EventMgtServiceComponent;
import org.wso2.carbon.identity.mgt.internal.EventMgtServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdpManager;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;

public class EventMgtServiceImpl implements EventMgtService {

    private static final Log log = LogFactory.getLog(EventMgtServiceImpl.class);
    private EventDistributionTask eventDistributionTask;

    public EventMgtServiceImpl (List<EventHandler> handlerList, int threadPoolSize) {
        this.eventDistributionTask = new EventDistributionTask(handlerList, threadPoolSize);
        if (log.isDebugEnabled()) {
            log.debug("Starting event distribution task from Notification Management component");
        }
        new Thread(eventDistributionTask).start();
    }

    /**
     * Add configurations in identity-mgt.properties file into database and cache
     */
    @Override
    public Properties addConfiguration(int tenantId) throws EventMgtException {


        Properties properties = new Properties();
        Map<String, ModuleConfiguration> moduleConfiguration = EventMgtConfigBuilder.getInstance()
                .getModuleConfiguration();
        for (Map.Entry<String, ModuleConfiguration> module : moduleConfiguration.entrySet()) {
            Properties moduleProperties = module.getValue().getModuleProperties();
            for (Map.Entry <Object, Object> prop : moduleProperties.entrySet()) {
                properties.put(prop.getKey(), prop.getValue());
            }
        }

        InputStream inStream = null;
        IdpManager identityProviderManager = EventMgtServiceDataHolder.getInstance().getIdpManager();

        try {
            Enumeration enuKeys = properties.keys();
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            IdentityProvider residentIdp = identityProviderManager.getResidentIdP(tenantDomain);
            IdentityProviderProperty[] idpProperties = residentIdp.getIdpProperties();
            List<String> idpPropertyKeys = new ArrayList<>();
            List<IdentityProviderProperty> propertyList = new ArrayList<>();
            for (IdentityProviderProperty idpProperty : idpProperties) {
                String propertyName = idpProperty.getName();
                if (EventMgtConstants.PropertyConfig.ALREADY_WRITTEN_PROPERTY_KEY.equals(propertyName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Identity management property saving skipped for tenant : " + tenantDomain);
                    }
                    return properties;
                }
                idpPropertyKeys.add(idpProperty.getName());
                propertyList.add(idpProperty);
            }
            while (enuKeys.hasMoreElements()) {
                String key = (String) enuKeys.nextElement();
                String value = properties.getProperty(key);
                IdentityProviderProperty property = new IdentityProviderProperty();
                property.setName(key);
                property.setValue(value);
                propertyList.add(property);
            }
            IdentityProviderProperty property = new IdentityProviderProperty();
            property.setName(EventMgtConstants.PropertyConfig.ALREADY_WRITTEN_PROPERTY_KEY);
            property.setValue(EventMgtConstants.PropertyConfig.ALREADY_WRITTEN_PROPERTY_VALUE);
            propertyList.add(property);
            residentIdp.setIdpProperties(propertyList.toArray(new IdentityProviderProperty[propertyList.size()]));
            FederatedAuthenticatorConfig[] authenticatorConfigs = residentIdp.getFederatedAuthenticatorConfigs();
            List<FederatedAuthenticatorConfig> configsToSave = new ArrayList<>();
            for (FederatedAuthenticatorConfig authenticatorConfig : authenticatorConfigs) {
                if (IdentityApplicationConstants.Authenticator.PassiveSTS.NAME.equals(authenticatorConfig.getName
                        ()) || IdentityApplicationConstants.NAME.equals(authenticatorConfig.getName()) ||
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(authenticatorConfig
                                .getName())) {
                    configsToSave.add(authenticatorConfig);
                }
            }
            residentIdp.setFederatedAuthenticatorConfigs(configsToSave.toArray(new
                    FederatedAuthenticatorConfig[configsToSave.size()]));

            identityProviderManager.updateResidentIdP(residentIdp, tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("New resident IDP properties for tenant : " + tenantDomain + " written to database");
            }

        } catch (IdentityProviderManagementException e) {
            log.error("Error while adding identity management properties to resident Idp.", e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.error("Error while closing stream ", e);
                }
            }
        }

        return properties;

    }

    /**
     * Store the configurations of a tenant in cache and database
     *
     * @param tenantId             Id of the tenant
     * @param configurationDetails Configurations belong to the tenant
     */
    public void updateConfiguration(int tenantId, Map<String, String> configurationDetails) throws EventMgtException {

        try {
            IdpManager identityProviderManager = EventMgtServiceDataHolder.getInstance().getIdpManager();
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            IdentityProvider residentIdp = identityProviderManager.getResidentIdP(tenantDomain);

            IdentityProviderProperty[] identityMgtProperties = residentIdp.getIdpProperties();
            List<IdentityProviderProperty> newProperties = new ArrayList<>();
            for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
                IdentityProviderProperty prop = new IdentityProviderProperty();
                String key = identityMgtProperty.getName();
                prop.setName(key);
                if (configurationDetails.containsKey(key)) {
                    prop.setValue(configurationDetails.get(key));
                } else {
                    prop.setValue(identityMgtProperty.getValue());
                }
                newProperties.add(prop);
                configurationDetails.remove(key);
            }
            for (Map.Entry<String, String> entry : configurationDetails.entrySet()) {
                IdentityProviderProperty prop = new IdentityProviderProperty();
                prop.setName(entry.getKey());
                prop.setValue(entry.getValue());
                newProperties.add(prop);
            }

            residentIdp.setIdpProperties(newProperties.toArray(new IdentityProviderProperty[newProperties.size()]));
            FederatedAuthenticatorConfig[] authenticatorConfigs = residentIdp.getFederatedAuthenticatorConfigs();
            List<FederatedAuthenticatorConfig> configsToSave = new ArrayList<>();
            for (FederatedAuthenticatorConfig authenticatorConfig : authenticatorConfigs) {
                if (IdentityApplicationConstants.Authenticator.PassiveSTS.NAME.equals(authenticatorConfig.getName
                        ()) || IdentityApplicationConstants.NAME.equals(authenticatorConfig.getName()) ||
                        IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(authenticatorConfig
                                .getName())) {
                    configsToSave.add(authenticatorConfig);
                }
            }
            residentIdp.setFederatedAuthenticatorConfigs(configsToSave.toArray(new
                    FederatedAuthenticatorConfig[configsToSave.size()]));
            identityProviderManager.updateResidentIdP(residentIdp, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            log.error("Error while updating identityManagement Properties of Resident Idp.", e);
        }

    }


    /**
     * Get the configurations of a tenant from cache or database
     *
     * @param tenantId Id of the tenant
     * @return Configurations belong to the tenant
     */
    @Override
    public Map<String, String> getConfiguration(int tenantId) throws EventMgtException {

        IdpManager identityProviderManager = EventMgtServiceDataHolder.getInstance().getIdpManager();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        IdentityProvider residentIdp = null;
        try {
            residentIdp = identityProviderManager.getResidentIdP(tenantDomain);
        } catch (IdentityProviderManagementException e) {
            log.error("Error while retrieving resident Idp with identity mgt properties.");
        }
        IdentityProviderProperty[] identityMgtProperties = residentIdp.getIdpProperties();
        Map<String, String> configMap = new HashMap<>();
        for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
            if (EventMgtConstants.PropertyConfig.ALREADY_WRITTEN_PROPERTY_KEY.equals(identityMgtProperty.getName())) {
                continue;
            }
            configMap.put(identityMgtProperty.getName(), identityMgtProperty.getValue());
        }
        return configMap;
    }

    /**
     * Handle event by relevant event handlers based on the event name
     */
    @Override
    public boolean handleEvent(Event event) throws EventMgtException {

        List<EventHandler> eventHandlerList = EventMgtServiceComponent.eventHandlerList;
        boolean returnValue = true;
        for (final EventHandler handler : eventHandlerList) {
            if (handler.isRegistered(event)) {
                if (handler.isAssociationAsync(event.getEventName())) {
                    eventDistributionTask.addEventToQueue(event);
                } else {
                    returnValue = handler.handleEvent(event);
                }
            }
        }
        return returnValue;
    }
}
