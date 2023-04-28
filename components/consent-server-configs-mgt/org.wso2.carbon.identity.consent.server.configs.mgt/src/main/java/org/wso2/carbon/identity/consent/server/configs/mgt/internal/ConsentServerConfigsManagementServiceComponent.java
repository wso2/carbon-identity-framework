/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.consent.server.configs.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.consent.server.configs.mgt.services.ConsentServerConfigsManagementService;
import org.wso2.carbon.identity.consent.server.configs.mgt.services.ConsentServerConfigsManagementServiceImpl;

/**
 * OSGi declarative services component which handled registration and un-registration of
 * ConsentServerConfigsManagementServiceComponent.
 */

@Component(
        name = "identity.consent.server.configs.mgt.component",
        immediate = true
)
public class ConsentServerConfigsManagementServiceComponent {

    private static final Log log = LogFactory.getLog(ConsentServerConfigsManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            context.getBundleContext().registerService(ConsentServerConfigsManagementService.class.getName(),
                    new ConsentServerConfigsManagementServiceImpl(), null);

        } catch (Throwable throwable) {
            log.error("Error while activating Consent Server Configs Management Service Component.", throwable);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Consent Server Configs Management service component deactivated.");
    }

    @Reference(
            name = "resource.configuration.manager",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationManager"
    )

    /**
     * This method is used to set the Configuration Manager Service.
     *
     * @param configurationManager  The Configuration Manager which needs to be set.
     */
    protected void setConfigurationManager(ConfigurationManager configurationManager) {

        ConsentServerConfigsManagementDataHolder.setConfigurationManager(configurationManager);
        log.debug("Setting the ConfigurationManager.");
    }

    /**
     * This method is used to unset the Configuration Manager Service.
     *
     * @param configurationManager The Configuration Manager Service which needs to unset.
     */
    protected void unsetConfigurationManager(ConfigurationManager configurationManager) {

        ConsentServerConfigsManagementDataHolder.setConfigurationManager(null);
        log.debug("Unsetting the ConfigurationManager.");
    }

}
