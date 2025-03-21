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
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.system.config.mgt.advisory.DBBasedAdminBannerDAO;
import org.wso2.carbon.identity.system.config.mgt.remotelogging.DBBasedRemoteLoggingConfigDAO;
import org.wso2.carbon.logging.service.dao.RemoteLoggingConfigDAO;

/**
 * Service component class for system config mgt Service.
 */
@Component(
        name = "identity.system.config.mgt.component",
        immediate = true
)
public class SystemConfigMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(SystemConfigMgtServiceComponent.class);

    /**
     * Activate the SystemConfigMgtServiceComponent.
     *
     * @param context Component Context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            ConfigurationManager configManager = SystemConfigMgtServiceHolder.getInstance().getConfigurationManager();
            SecretManager secretManager = SystemConfigMgtServiceHolder.getInstance().getSecretManager();
            SecretResolveManager secretResolveManager =
                    SystemConfigMgtServiceHolder.getInstance().getSecretResolveManager();

            if (isDBBasedConfigMgtEnabled("AdminAdvisoryBanner")) {
                bundleContext.registerService(AdminAdvisoryBannerDAO.class, new DBBasedAdminBannerDAO(configManager),
                        null);
                LOG.debug("DB based Admin Banner DAO registered.");
            }
            if (isDBBasedConfigMgtEnabled("RemoteLoggingConfig")) {
                DBBasedRemoteLoggingConfigDAO dbBasedLoggingConfigDAO =
                        new DBBasedRemoteLoggingConfigDAO(configManager, secretManager, secretResolveManager);
                bundleContext.registerService(RemoteLoggingConfigDAO.class, dbBasedLoggingConfigDAO, null);
                LOG.debug("DB based Remote Logging Config DAO registered.");
            }
            LOG.debug("System Config Mgt Service Component is activated.");
        } catch (Throwable e) {
            LOG.error("Error while activating System Config management service.", e);
        }
    }

    private boolean isDBBasedConfigMgtEnabled(String configType) {

        String storageType = IdentityUtil.getProperty("DataStorageType." + configType);
        boolean isRegistry = "registry".equals(storageType) || "hybrid".equals(storageType);
        return !isRegistry;
    }

    /**
     * Deactivate the System Config Mgt Service Component.
     *
     * @param context Component Context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("System Config Mgt Service Component is deactivated.");
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

        LOG.debug("Registering the ConfigurationManager in System Config Mgt Service.");
        SystemConfigMgtServiceHolder.getInstance().setConfigurationManager(configurationManager);
    }

    /**
     * Unset the ConfigurationManager.
     *
     * @param configurationManager The {@code ConfigurationManager} instance.
     */
    protected void unregisterConfigurationManager(ConfigurationManager configurationManager) {

        LOG.debug("Unregistering the ConfigurationManager in System Config Mgt Service.");
        SystemConfigMgtServiceHolder.getInstance().setConfigurationManager(null);
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService")
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
    /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretManager",
            service = SecretManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretManager"
    )
    private void setSecretManager(SecretManager secretManager) {

        SystemConfigMgtServiceHolder.getInstance().setSecretManager(secretManager);
        LOG.debug("SecretManager set in System Config Mgt Service.");
    }

    private void unsetSecretManager(SecretManager secretManager) {

        SystemConfigMgtServiceHolder.getInstance().setSecretManager(null);
        LOG.debug("SecretManager unset in System Config Mgt Service.");
    }

    @Reference(
            name = "org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager",
            service = SecretResolveManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretResolveManager"
    )
    private void setSecretResolveManager(SecretResolveManager secretResolveManager) {

        SystemConfigMgtServiceHolder.getInstance().setSecretResolveManager(secretResolveManager);
        LOG.debug("SecretResolveManager set in System Config Mgt Service.");
    }

    private void unsetSecretResolveManager(SecretResolveManager secretResolveManager) {

        SystemConfigMgtServiceHolder.getInstance().setSecretResolveManager(null);
        LOG.debug("SecretResolveManager unset in System Config Mgt Service.");
    }
}
