/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.system.config.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.admin.advisory.mgt.dao.AdminAdvisoryBannerDAO;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.system.config.mgt.advisory.DBBasedAdminBannerDAO;

/**
 * Service component class for system config mgt Service.
 */
@Component(
        name = "identity.system.config.mgt.component",
        immediate = true
)
public class SystemConfigMgtServiceComponent {

    private static final Log log = LogFactory.getLog(SystemConfigMgtServiceComponent.class);

    /**
     * Activate the SystemConfigMgtServiceComponent.
     *
     * @param context Component Context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            // Register the Database based Admin Advisory Banner DAO.
            bundleContext.registerService(AdminAdvisoryBannerDAO.class, new DBBasedAdminBannerDAO(), null);
            log.debug("System Config Mgt Service Component is activated.");

        } catch (Throwable e) {
            log.error("Error while activating System Config management service.", e);
        }
    }

    /**
     * Deactivate the System Config Mgt Service Component.
     *
     * @param context Component Context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("System Config Mgt Service Component is deactivated.");
    }

    /**
     * Set the ConfigurationManager.
     *
     * @param configurationManager The {@code ConfigurationManager} instance.
     */
    @Reference(
            name = "resource.configuration.manager",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigurationManager"
    )
    protected void registerConfigurationManager(ConfigurationManager configurationManager) {

        log.debug("Registering the ConfigurationManager in System Config Mgt Service.");
        SystemConfigMgtServiceHolder.getInstance().setConfigurationManager(configurationManager);
    }

    /**
     * Unset the ConfigurationManager.
     *
     * @param configurationManager The {@code ConfigurationManager} instance.
     */
    protected void unregisterConfigurationManager(ConfigurationManager configurationManager) {

        log.debug("Unregistering the ConfigurationManager in System Config Mgt Service.");
        SystemConfigMgtServiceHolder.getInstance().setConfigurationManager(null);
    }

}
