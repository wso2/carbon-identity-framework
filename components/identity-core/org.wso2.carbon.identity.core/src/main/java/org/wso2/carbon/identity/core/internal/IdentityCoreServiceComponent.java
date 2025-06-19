/*
 * Copyright (c) 2014-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.KeyProviderService;
import org.wso2.carbon.identity.core.KeyStoreManagerExtension;
import org.wso2.carbon.identity.core.SAMLSSOServiceProviderManager;
import org.wso2.carbon.identity.core.ServiceURLBuilderFactory;
import org.wso2.carbon.identity.core.migrate.MigrationClient;
import org.wso2.carbon.identity.core.migrate.MigrationClientException;
import org.wso2.carbon.identity.core.migrate.MigrationClientStartupObserver;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.persistence.UmPersistenceManager;
import org.wso2.carbon.identity.core.persistence.registry.RegistryResourceMgtService;
import org.wso2.carbon.identity.core.persistence.registry.RegistryResourceMgtServiceImpl;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEventImpl;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationUserResidentResolverService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

@Component(
        name = "identity.core.component",
        immediate = true
)
public class IdentityCoreServiceComponent {
    private static Log log = LogFactory.getLog(IdentityCoreServiceComponent.class);
    private static ServerConfigurationService serverConfigurationService = null;
    private static MigrationClient migrationClient = null;

    private static BundleContext bundleContext = null;
    private static ConfigurationContextService configurationContextService = null;
    private static ServiceURLBuilderFactory serviceURLBuilderFactory = new ServiceURLBuilderFactory();
    private ServiceRegistration<KeyProviderService> defaultKeystoreManagerServiceRef;
    private DefaultKeystoreManagerExtension defaultKeystoreManagerExtension = new DefaultKeystoreManagerExtension();
    private DefaultKeyProviderService defaultKeyProviderService;

    public IdentityCoreServiceComponent() {
        defaultKeyProviderService = new DefaultKeyProviderService(defaultKeystoreManagerExtension);
    }

    public static ServerConfigurationService getServerConfigurationService() {
        return IdentityCoreServiceComponent.serverConfigurationService;
    }

    @Reference(
            name = "server.configuration.service",
            service = ServerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService"
    )
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
    @Activate
    protected void activate(ComponentContext ctxt) throws MigrationClientException {
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
            if (Boolean.parseBoolean(migrate) && component != null && component.contains("identity")) {
                if (migrationClient == null) {
                    log.warn("Waiting for migration client.");
                    throw new MigrationClientException("Migration client not found");
                } else {
                    log.info("Executing Migration client : " + migrationClient.getClass().getName());
                    migrationClient.execute();
                    ctxt.getBundleContext().registerService(ServerStartupObserver.class.getName(),
                            new MigrationClientStartupObserver(migrationClient), null) ;
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

            // Registering the SAML SSO Service Provider configuration manager.
            ctxt.getBundleContext().registerService(SAMLSSOServiceProviderManager.class.getName(),
                    new SAMLSSOServiceProviderManager(), null);

            defaultKeystoreManagerServiceRef = ctxt.getBundleContext().registerService(KeyProviderService.class,
                    defaultKeyProviderService, null);

            IdentityCoreServiceDataHolder.getInstance()
                    .setTenantQualifiedUrlsEnabled(Boolean.parseBoolean(IdentityUtil.getProperty(
                            IdentityCoreConstants.ENABLE_TENANT_QUALIFIED_URLS)));
            IdentityCoreServiceDataHolder.getInstance()
                    .setTenantedSessionsEnabled(Boolean.parseBoolean(IdentityUtil.getProperty(
                            IdentityCoreConstants.ENABLE_TENANTED_SESSIONS)));

            // Register initialize service To guarantee the activation order. Component which is referring this
            // service will wait until this component activated.
            ctxt.getBundleContext().registerService(IdentityCoreInitializedEvent.class.getName(),
                    new IdentityCoreInitializedEventImpl(), null);
            // Note : DO NOT add any activation related code below this point (after core initialized event registration),
            // to make sure the server doesn't start up if any activation failures
        } catch (MigrationClientException e) {
            // Throwing migration client exception to wait till migration client implementation bundle starts if
            // -Dmigrate option is used.
            throw e;
        } catch (Throwable e) {
            log.error("Error occurred while populating identity configuration properties", e);
        }
    }

    /**
     * @param ctxt
     */
    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        defaultKeystoreManagerServiceRef.unregister();
        IdentityTenantUtil.setBundleContext(null);
        if (log.isDebugEnabled()) {
            log.debug("Identity Core bundle is deactivated");
        }
    }

    @Reference(
            name = "registry.service",
            service = RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService"
    )
    protected void setRegistryService(RegistryService registryService) {
        IdentityTenantUtil.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        IdentityTenantUtil.setRegistryService(null);
    }

    /**
     * @param realmService
     */
    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {
        IdentityTenantUtil.setRealmService(realmService);
        defaultKeystoreManagerExtension.setRealmService(realmService);
        IdentityCoreServiceDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * @param realmService
     */
    protected void unsetRealmService(RealmService realmService) {
        defaultKeystoreManagerExtension.setRealmService(null);
        IdentityTenantUtil.setRealmService(null);
        IdentityCoreServiceDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "registry.loader.default",
            service = TenantRegistryLoader.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTenantRegistryLoader"
    )
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
    @Reference(
            name = "config.context.service",
            service = ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService"
    )
    protected void setConfigurationContextService(ConfigurationContextService service) {
        configurationContextService = service;
    }

    /**
     * @param service
     */
    protected void unsetConfigurationContextService(ConfigurationContextService service) {
        configurationContextService = null;
    }

    /**
     *
     * @param client
     */
    @Reference(
            name = "is.migration.client",
            service = MigrationClient.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMigrationClient"
    )
    protected void setMigrationClient(MigrationClient client) {
        migrationClient = client;
    }

    /**
     * @param client
     */
    protected void unsetMigrationClient(MigrationClient client) {
        migrationClient = null;
    }

    protected void setKeyStoreManagerExtension(KeyStoreManagerExtension keyStoreManagerExtension) {
        if (log.isDebugEnabled()) {
            log.debug("KeyStoreManagerExtension is being set by an OSGI component. The extension class is: "
                    + keyStoreManagerExtension);
        }
        defaultKeyProviderService.setKeyStoreManagerExtension(keyStoreManagerExtension);
    }

    protected void unsetKeyStoreManagerExtension(KeyStoreManagerExtension keyStoreManagerExtension) {
        defaultKeyProviderService.setKeyStoreManagerExtension(defaultKeystoreManagerExtension);
    }

    @Reference(
            name = "url.builder.factory",
            service = ServiceURLBuilderFactory.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServiceURLBuilderFactory"
    )
    protected void setServiceURLBuilderFactory(ServiceURLBuilderFactory serviceURLBuilderFactory) {

        IdentityCoreServiceComponent.serviceURLBuilderFactory = serviceURLBuilderFactory;
        if (log.isDebugEnabled()) {
            log.debug("ServiceURLBuilderFactory service set to: " + IdentityCoreServiceComponent.serviceURLBuilderFactory
                    .getClass().getName());
        }
    }

    protected void unsetServiceURLBuilderFactory(ServiceURLBuilderFactory serviceURLBuilderFactory) {

        IdentityCoreServiceComponent.serviceURLBuilderFactory = new ServiceURLBuilderFactory();
        if (log.isDebugEnabled()) {
            log.debug("ServiceURLBuilderFactory service reverted to: " + IdentityCoreServiceComponent.serviceURLBuilderFactory
                    .getClass().getName());
        }
    }

    public static ServiceURLBuilderFactory getServiceURLBuilderFactory() {

        return serviceURLBuilderFactory;
    }
    
    @Reference(
            name = "identity.organization.management.component.resident.resolver",
            service = OrganizationUserResidentResolverService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationUserResidentResolverService"
    )
    protected void setOrganizationUserResidentResolverService(
            OrganizationUserResidentResolverService organizationUserResidentResolverService) {
        
        log.debug("Setting the organization management service.");
        IdentityCoreServiceDataHolder.getInstance()
                .setOrganizationUserResidentResolverService(organizationUserResidentResolverService);
    }

    protected void unsetOrganizationUserResidentResolverService(
            OrganizationUserResidentResolverService organizationUserResidentResolverService) {
        
        log.debug("Unset organization management service.");
        IdentityCoreServiceDataHolder.getInstance().setOrganizationUserResidentResolverService(null);
    }
}
