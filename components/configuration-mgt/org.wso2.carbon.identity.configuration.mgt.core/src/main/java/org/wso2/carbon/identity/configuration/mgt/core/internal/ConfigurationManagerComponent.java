/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManagerImpl;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.dao.impl.ConfigurationDAOImpl;
import org.wso2.carbon.identity.configuration.mgt.core.model.ConfigurationManagerConfigurationHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * OSGi declarative services component which handles registration and un-registration of configuration management service.
 */
@Component(
        name = "carbon.configuration.mgt.component",
        immediate = true
)
public class ConfigurationManagerComponent {

    private static final Log log = LogFactory.getLog(ConfigurationManagerComponent.class);
    private List<ConfigurationDAO> configurationDAOs = new ArrayList<>();

    /**
     * Register ConfigurationManager as an OSGI service.
     *
     * @param componentContext OSGI service component context.
     */
    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            BundleContext bundleContext = componentContext.getBundleContext();

            bundleContext.registerService(ConfigurationDAO.class.getName(), new ConfigurationDAOImpl(),
                    null);

            ConfigurationManagerConfigurationHolder configurationManagerConfigurationHolder =
                    new ConfigurationManagerConfigurationHolder();
            configurationManagerConfigurationHolder.setConfigurationDAOS(configurationDAOs);

            bundleContext.registerService(ConfigurationManager.class.getName(),
                    new ConfigurationManagerImpl(configurationManagerConfigurationHolder), null);

        } catch (Throwable e) {
            log.error("Error while activating ConfigurationManagerComponent.", e);
        }
    }

    @Reference(
            name = "configuration.dao",
            service = org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfiguration"
    )
    protected void setConfiguration(ConfigurationDAO configurationDAO) {

        if (configurationDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource DAO is registered in ConfigurationManager service.");
            }

            this.configurationDAOs.add(configurationDAO);
            this.configurationDAOs.sort(Comparator.comparingInt(ConfigurationDAO::getPriority));
        }
    }

    protected void unsetConfiguration(ConfigurationDAO configurationDAO) {

        if (log.isDebugEnabled()) {
            log.debug("Purpose DAO is unregistered in ConfigurationManager service.");
        }
        this.configurationDAOs.remove(configurationDAO);
    }
}
