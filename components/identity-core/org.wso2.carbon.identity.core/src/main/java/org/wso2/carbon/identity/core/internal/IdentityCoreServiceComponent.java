/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.DefaultBootstrap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.persistence.UmPersistenceManager;
import org.wso2.carbon.identity.core.persistence.registry.RegistryResourceMgtService;
import org.wso2.carbon.identity.core.persistence.registry.RegistryResourceMgtServiceImpl;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEventImpl;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="identity.core.component" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration.service"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService" cardinality="1..1"
 * policy="dynamic"  bind="setServerConfigurationService"
 * unbind="unsetServerConfigurationService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="registry.loader.default"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic" bind="setTenantRegistryLoader" unbind="unsetTenantRegistryLoader"
 */

public class IdentityCoreServiceComponent {
    private static final String MIGRATION_CLIENT_CLASS_NAME = "org.wso2.carbon.is.migration.client.MigrateFrom520to530";
    private static Log log = LogFactory.getLog(IdentityCoreServiceComponent.class);
    private static ServerConfigurationService serverConfigurationService = null;

    private static BundleContext bundleContext = null;
    private static ConfigurationContextService configurationContextService = null;

    public IdentityCoreServiceComponent() {
    }

    public static ServerConfigurationService getServerConfigurationService() {
        return IdentityCoreServiceComponent.serverConfigurationService;
    }

    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Set the ServerConfiguration Service");
        }
        IdentityCoreServiceComponent.serverConfigurationService = serverConfigurationService;

    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Unset the ServerConfiguration Service");
        }
        IdentityCoreServiceComponent.serverConfigurationService = null;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * @param ctxt
     */
    protected void activate(ComponentContext ctxt) {
        IdentityTenantUtil.setBundleContext(ctxt.getBundleContext());
        if (log.isDebugEnabled()) {
            log.debug("Identity Core bundle is activated");
        }
        try {
            IdentityUtil.populateProperties();
            bundleContext = ctxt.getBundleContext();

            // Identity database schema creation can be avoided by setting
            // JDBCPersistenceManager.SkipDBSchemaCreation property to "true".
            String skipSchemaCreation = IdentityUtil.getProperty(
                    IdentityConstants.ServerConfig.SKIP_DB_SCHEMA_CREATION);

            // initialize the identity persistence manager, if it is not already initialized.
            JDBCPersistenceManager jdbcPersistenceManager = JDBCPersistenceManager.getInstance();
            if (("true".equals(skipSchemaCreation))) {
                // This ideally should be an info log but in API Manager it could be confusing to say
                // DB initialization was skipped, because DB initialization is done by apimgt components
                if (log.isDebugEnabled()) {
                    log.debug("Identity Provider Database initialization attempt was skipped since '" +
                            IdentityConstants.ServerConfig.SKIP_DB_SCHEMA_CREATION + "' property has been set to \'true\'");
                }
            } else if (System.getProperty("setup") == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Identity Database schema initialization check was skipped since " +
                            "\'setup\' variable was not given during startup");
                }
            } else {
                jdbcPersistenceManager.initializeDatabase();
            }

            // initialize um persistence manager and retrieve the user management datasource.
            UmPersistenceManager.getInstance();

            String migrate = System.getProperty("migrate");
            String component = System.getProperty("component");
            try {
                if (component != null && component.contains("identity") && Boolean.parseBoolean(migrate)) {
                    //Directly call migration client here and selectively check for component migrations at client
                    Class<?> c = Class.forName(MIGRATION_CLIENT_CLASS_NAME);
                    c.getMethod("databaseMigration").invoke(c.newInstance());
                    log.info("Migrated the identity and user management databases");
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Migration client is not available");
                }
            }

            //this is done to initialize primary key store
            try {
                KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID).getPrimaryKeyStore();
            } catch (Exception e) {
                log.error("Error while initializing primary key store.", e);
            }

            // register identity registry resource management service
            ServiceRegistration registryServiceSR =
                    ctxt.getBundleContext().registerService(RegistryResourceMgtService.class.getName(),
                            new RegistryResourceMgtServiceImpl(), null);

            if (registryServiceSR != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Identity Registry Management Service registered successfully.");
                }
            }

            // Register initialize service To guarantee the activation order. Component which is referring this
            // service will wait until this component activated.
            ctxt.getBundleContext().registerService(IdentityCoreInitializedEvent.class.getName(),
                    new IdentityCoreInitializedEventImpl(), null);


        } catch (Throwable e) {
            log.error("Error occurred while populating identity configuration properties", e);
        }
    }

    /**
     * @param ctxt
     */
    protected void deactivate(ComponentContext ctxt) {
        IdentityTenantUtil.setBundleContext(null);
        if (log.isDebugEnabled()) {
            log.debug("Identity Core bundle is deactivated");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        IdentityTenantUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        IdentityTenantUtil.setRegistryService(null);
    }

    /**
     * @param realmService
     */
    protected void setRealmService(RealmService realmService) {
        IdentityTenantUtil.setRealmService(realmService);
    }

    /**
     * @param realmService
     */
    protected void unsetRealmService(RealmService realmService) {
        IdentityTenantUtil.setRealmService(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Tenant Registry Loader is set in the SAML SSO bundle");
        }
        IdentityTenantUtil.setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Tenant Registry Loader is unset in the SAML SSO bundle");
        }
        IdentityTenantUtil.setTenantRegistryLoader(null);
    }

    /**
     * @return
     */
    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    /**
     * @param service
     */
    protected void setConfigurationContextService(ConfigurationContextService service) {
        configurationContextService = service;
    }

    /**
     * @param service
     */
    protected void unsetConfigurationContextService(ConfigurationContextService service) {
        configurationContextService = null;
    }

}
