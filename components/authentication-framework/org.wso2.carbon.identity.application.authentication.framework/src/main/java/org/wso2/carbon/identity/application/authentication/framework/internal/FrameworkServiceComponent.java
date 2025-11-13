/*
 * Copyright (c) 2013-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.internal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticationService;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationFlowHandler;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationMethodNameTranslator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.ServerSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.UserDefinedAuthenticatorService;
import org.wso2.carbon.identity.application.authentication.framework.UserSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JSExecutionSupervisor;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGenericGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.CacheBackedLongWaitStatusDAO;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.LongWaitStatusDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.ApplicationRolesResolver;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.impl.AppAssociatedRolesResolverImpl;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimFilter;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimFilter;
import org.wso2.carbon.identity.application.authentication.framework.handler.orgdiscovery.OrganizationDiscoveryHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.listener.JITProvisioningIdentityProviderMgtListener;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.JITProvisioningPostAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.PostAuthAssociationHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.PostAuthenticatedSubjectIdentifierHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.PostAuthnMissingClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.ConsentMgtPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.SSOConsentService;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.SSOConsentServiceImpl;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.AsyncSequenceExecutor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkLoginResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkLogoutResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.internal.impl.AuthenticationMethodNameTranslatorImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.impl.ServerSessionManagementServiceImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.impl.UserSessionManagementServiceImpl;
import org.wso2.carbon.identity.application.authentication.framework.listener.AuthenticationEndpointTenantActivityListener;
import org.wso2.carbon.identity.application.authentication.framework.listener.SessionContextMgtListener;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtService;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.processor.SessionExtenderProcessor;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.request.SessionExtenderRequestFactory;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.response.SessionExtenderResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.store.JavaSessionSerializer;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.authentication.framework.store.PushedAuthDataStore;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionSerializer;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants.DefinedByType;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.core.handler.HandlerComparator;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryManagementService;
import org.wso2.carbon.identity.multi.attribute.login.mgt.MultiAttributeLoginService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManagementInitialize;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.CUSTOM_AUTHENTICATOR_PREFIX;
import static org.wso2.carbon.identity.base.IdentityConstants.TRUE;

/**
 * OSGi declarative services component which handled registration and unregistration of FrameworkServiceComponent.
 */

@Component(
        name = "identity.application.authentication.framework.component",
        immediate = true
)
public class FrameworkServiceComponent {

    public static final String IS_HANDLER = "IS_HANDLER";
    private static final Log log = LogFactory.getLog(FrameworkServiceComponent.class);
    private static final String API_AUTH = "APIAuth";

    private ConsentMgtPostAuthnHandler consentMgtPostAuthnHandler = new ConsentMgtPostAuthnHandler();
    private String requireCode;
    private String secretsCode;

    public static RealmService getRealmService() {

        return FrameworkServiceDataHolder.getInstance().getRealmService();
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * @return
     * @throws FrameworkException
     * @Deprecated The usage of bundle context outside of the component should never be needed. Component should
     * provide necessary wiring for any place which require the BundleContext.
     */
    @Deprecated
    public static BundleContext getBundleContext() throws FrameworkException {

        BundleContext bundleContext = FrameworkServiceDataHolder.getInstance().getBundleContext();
        if (bundleContext == null) {
            String msg = "System has not been started properly. Bundle Context is null.";
            log.error(msg);
            throw new FrameworkException(msg);
        }

        return bundleContext;
    }

    @SuppressWarnings("unchecked")
    @Activate
    protected void activate(ComponentContext ctxt) {

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        dataHolder.setJsFunctionRegistry(new JsFunctionRegistryImpl());
        BundleContext bundleContext = ctxt.getBundleContext();

        bundleContext.registerService(UserSessionManagementService.class.getName(),
                new UserSessionManagementServiceImpl(), null);
        bundleContext.registerService(HttpIdentityRequestFactory.class.getName(),
                new SessionExtenderRequestFactory(), null);
        bundleContext.registerService(HttpIdentityResponseFactory.class.getName(),
                new SessionExtenderResponseFactory(), null);
        bundleContext.registerService(IdentityProcessor.class.getName(), new SessionExtenderProcessor(), null);

        bundleContext.registerService(ApplicationRolesResolver.class.getName(), new AppAssociatedRolesResolverImpl(),
                null);

        ServerSessionManagementService serverSessionManagementService = new ServerSessionManagementServiceImpl();
        bundleContext.registerService(ServerSessionManagementService.class.getName(),
                serverSessionManagementService, null);
        dataHolder.setServerSessionManagementService(serverSessionManagementService);
        setAdaptiveAuthExecutionSupervisor();

        boolean tenantDropdownEnabled = ConfigurationFacade.getInstance().getTenantDropdownEnabled();

        if (tenantDropdownEnabled) {
            // Register the tenant management listener for tracking changes to tenants
            bundleContext.registerService(TenantMgtListener.class.getName(),
                    new AuthenticationEndpointTenantActivityListener(), null);

            if (log.isDebugEnabled()) {
                log.debug("AuthenticationEndpointTenantActivityListener is registered. Tenant Domains Dropdown is " +
                        "enabled.");
            }
        }
        AuthenticationMethodNameTranslatorImpl authenticationMethodNameTranslator
                = new AuthenticationMethodNameTranslatorImpl();
        authenticationMethodNameTranslator.initializeConfigsWithServerConfig();
        bundleContext
                .registerService(AuthenticationMethodNameTranslator.class, authenticationMethodNameTranslator, null);
        dataHolder.setAuthenticationMethodNameTranslator(authenticationMethodNameTranslator);

        dataHolder.setBundleContext(bundleContext);
        dataHolder.getHttpIdentityRequestFactories().add(new HttpIdentityRequestFactory());
        dataHolder.getHttpIdentityResponseFactories().add(new FrameworkLoginResponseFactory());
        dataHolder.getHttpIdentityResponseFactories().add(new FrameworkLogoutResponseFactory());
        UIBasedConfigurationLoader uiBasedConfigurationLoader = new UIBasedConfigurationLoader();
        dataHolder.setSequenceLoader(uiBasedConfigurationLoader);

        JsGenericGraphBuilderFactory jsGraphBuilderFactory =
                FrameworkUtils.createJsGenericGraphBuilderFactoryFromConfig();
        if (jsGraphBuilderFactory != null) {
            bundleContext.registerService(JsFunctionRegistry.class, dataHolder.getJsFunctionRegistry(), null);
            dataHolder.setAdaptiveAuthenticationAvailable(true);
            jsGraphBuilderFactory.init();
            dataHolder.setJsGenericGraphBuilderFactory(jsGraphBuilderFactory);
        } else {
            dataHolder.setAdaptiveAuthenticationAvailable(false);
            log.warn("Adaptive authentication is disabled.");
        }

        PostAuthenticationMgtService postAuthenticationMgtService = new PostAuthenticationMgtService();
        bundleContext.registerService(PostAuthenticationMgtService.class.getName(), postAuthenticationMgtService, null);
        dataHolder.setPostAuthenticationMgtService(postAuthenticationMgtService);
        // Registering missing mandatory claim handler as a post authn handler
        PostAuthenticationHandler postAuthnMissingClaimHandler = new PostAuthnMissingClaimHandler();
        bundleContext.registerService(PostAuthenticationHandler.class.getName(), postAuthnMissingClaimHandler, null);

        SSOConsentService ssoConsentService = new SSOConsentServiceImpl();
        bundleContext.registerService(SSOConsentService.class.getName(), ssoConsentService, null);
        dataHolder.setSSOConsentService(ssoConsentService);
        bundleContext.registerService(PostAuthenticationHandler.class.getName(), consentMgtPostAuthnHandler, null);
        JITProvisioningIdentityProviderMgtListener jitProvisioningIDPMgtListener =
                new JITProvisioningIdentityProviderMgtListener();
        bundleContext.registerService(IdentityProviderMgtListener.class.getName(),
                jitProvisioningIDPMgtListener, null);
        bundleContext.registerService(ClaimFilter.class.getName(), new DefaultClaimFilter(), null);

        // This is done to load SessionDataStore and PushedAuthDataStore classes and start the cleanup tasks.
        SessionDataStore.getInstance();
        PushedAuthDataStore.getInstance();

        AsyncSequenceExecutor asyncSequenceExecutor = new AsyncSequenceExecutor();
        asyncSequenceExecutor.init();
        dataHolder.setAsyncSequenceExecutor(asyncSequenceExecutor);

        LongWaitStatusDAOImpl daoImpl = new LongWaitStatusDAOImpl();
        CacheBackedLongWaitStatusDAO cacheBackedDao = new CacheBackedLongWaitStatusDAO(daoImpl);

        String connectionTimeoutString = IdentityUtil.getProperty("AdaptiveAuth.HTTPConnectionTimeout");
        int connectionTimeout = 5000;
        if (connectionTimeoutString != null) {
            try {
                connectionTimeout = Integer.parseInt(connectionTimeoutString);
            } catch (NumberFormatException e) {
                log.error("Error while parsing connection timeout : " + connectionTimeoutString, e);
            }
        }

        LongWaitStatusStoreService longWaitStatusStoreService =
                new LongWaitStatusStoreService(cacheBackedDao, connectionTimeout);
        dataHolder.setLongWaitStatusStoreService(longWaitStatusStoreService);

        // Registering JIT, association and domain handler as post authentication handler
        PostAuthenticationHandler postJITProvisioningHandler = JITProvisioningPostAuthenticationHandler.getInstance();
        bundleContext.registerService(PostAuthenticationHandler.class.getName(), postJITProvisioningHandler, null);
        PostAuthenticationHandler postAuthAssociationHandler = PostAuthAssociationHandler.getInstance();
        bundleContext.registerService(PostAuthenticationHandler.class.getName(), postAuthAssociationHandler, null);
        PostAuthenticationHandler postAuthenticatedUserDomainHandler = PostAuthenticatedSubjectIdentifierHandler
                .getInstance();
        bundleContext
                .registerService(PostAuthenticationHandler.class.getName(), postAuthenticatedUserDomainHandler, null);

        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }

        /**
         * Load and reade the require.js file in resources.
         */
        this.loadCodeForRequire();
        this.loadCodeForSecrets();

        // Check whether the TENANT_ID column is available in the IDN_FED_AUTH_SESSION_MAPPING table.
        FrameworkUtils.checkIfTenantIdColumnIsAvailableInFedAuthTable();
        // Check whether the IDP_ID column is available in the IDN_FED_AUTH_SESSION_MAPPING table.
        FrameworkUtils.checkIfIdpIdColumnIsAvailableInFedAuthTable();

        // Set user session mapping enabled.
        FrameworkServiceDataHolder.getInstance().setUserSessionMappingEnabled(FrameworkUtils
                .isUserSessionMappingEnabled());
        if (FrameworkServiceDataHolder.getInstance().getSessionSerializer() == null) {
            FrameworkServiceDataHolder.getInstance().setSessionSerializer(new JavaSessionSerializer());
        }

        // Set skip local user search for authentication flow handlers enabled.
        FrameworkServiceDataHolder.getInstance().setSkipLocalUserSearchForAuthenticationFlowHandlersEnabled
                (FrameworkUtils.isSkipLocalUserSearchForAuthenticationFlowHandlersEnabled());

        bundleContext.registerService(ApplicationAuthenticationService.class.getName(), new
                ApplicationAuthenticationService(), null);
        // Note : DO NOT add any activation related code below this point,
        // to make sure the server doesn't start up if any activation failures
    }

    private void setAdaptiveAuthExecutionSupervisor() {

        String isEnabled = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_ENABLE);
        if (!Boolean.parseBoolean(isEnabled)) {
            if (log.isDebugEnabled()) {
                log.debug("Adaptive auth script execution supervisor is turned off.");
            }
            return;
        }

        String threadCountString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_THREAD_COUNT);
        int threadCount = FrameworkConstants.AdaptiveAuthentication.DEFAULT_EXECUTION_SUPERVISOR_THREAD_COUNT;
        if (StringUtils.isNotBlank(threadCountString)) {
            try {
                threadCount = Integer.parseInt(threadCountString);
            } catch (NumberFormatException e) {
                log.error("Error while parsing adaptive authentication execution supervisor thread count config: "
                        + threadCountString + ", setting thread count to default value: " + threadCount, e);
            }
        }

        String timeOutEnabledString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_TIMEOUT_ENABLE);
        boolean timeOutEnabled = FrameworkConstants.AdaptiveAuthentication
                .DEFAULT_EXECUTION_SUPERVISOR_TIMEOUT_ENABLE;
        if (StringUtils.isNotBlank(timeOutEnabledString)) {
            try {
                timeOutEnabled = BooleanUtils.toBoolean(timeOutEnabledString.toLowerCase(Locale.ROOT),
                        Boolean.TRUE.toString(), Boolean.FALSE.toString());
            } catch (IllegalArgumentException e) {
                log.error("Error while parsing adaptive authentication execution supervisor timeout enable config: "
                        + timeOutEnabledString + ", setting timeout enable to default value: " + timeOutEnabled, e);
            }
        }
        String timeoutString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_TIMEOUT);
        long timeoutInMillis = FrameworkConstants.AdaptiveAuthentication.DEFAULT_EXECUTION_SUPERVISOR_TIMEOUT;
        if (StringUtils.isNotBlank(timeoutString)) {
            try {
                timeoutInMillis = Long.parseLong(timeoutString);
            } catch (NumberFormatException e) {
                log.error("Error while parsing adaptive authentication execution supervisor timeout config: "
                        + timeoutString + ", setting timeout to default value: " + timeoutInMillis, e);
            }
        }

        String memoryLimitString = IdentityUtil.getProperty(
                FrameworkConstants.AdaptiveAuthentication.CONF_EXECUTION_SUPERVISOR_MEMORY_LIMIT);
        long memoryLimitInBytes = FrameworkConstants.AdaptiveAuthentication.DEFAULT_EXECUTION_SUPERVISOR_MEMORY_LIMIT;
        if (StringUtils.isNotBlank(memoryLimitString)) {
            try {
                memoryLimitInBytes = Long.parseLong(memoryLimitString);
            } catch (NumberFormatException e) {
                log.error("Error while parsing adaptive authentication execution supervisor memory limit config: "
                        + memoryLimitString + ", memory consumption will not be monitored.", e);
            }
        }

        FrameworkServiceDataHolder.getInstance()
                .setJsExecutionSupervisor(new JSExecutionSupervisor(threadCount, timeOutEnabled, timeoutInMillis,
                        memoryLimitInBytes));
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is deactivated");
        }

        FrameworkServiceDataHolder.getInstance().setBundleContext(null);
        SessionDataStore.getInstance().stopService();
        if (FrameworkServiceDataHolder.getInstance().getJsExecutionSupervisor() != null) {
            FrameworkServiceDataHolder.getInstance().getJsExecutionSupervisor().shutdown();
        }
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is unset in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "application.authenticator",
            service = ApplicationAuthenticator.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAuthenticator"
    )
    protected void setAuthenticator(ApplicationAuthenticator authenticator)
            throws IdentityProviderManagementServerException {

        /* All custom authenticator names must start with the `custom-` prefix. If a system-defined authenticator is
         attempted to be registered at server startup with a name starting with this prefix, an error will be thrown. */
        if (authenticator.getName().startsWith(CUSTOM_AUTHENTICATOR_PREFIX)) {
            throw new IdentityProviderManagementServerException(String.format("System-defined authenticator names " +
                    "are not allowed to have the %s prefix. Therefore, %s cannot be registered.",
                    CUSTOM_AUTHENTICATOR_PREFIX, authenticator.getName()));
        }
        ApplicationAuthenticatorManager.getInstance().addSystemDefinedAuthenticator(authenticator);

        Property[] configProperties = null;

        List<Property> configurationProperties = authenticator.getConfigurationProperties();
        if (configurationProperties == null) {
            configurationProperties = new ArrayList<>();
        }
        if (authenticator instanceof AuthenticationFlowHandler) {
            Property handlerProperty = new Property();
            handlerProperty.setName(IS_HANDLER);
            handlerProperty.setValue(TRUE);
            configurationProperties.add(handlerProperty);
        }
        if (!configurationProperties.isEmpty()) {
            configProperties = configurationProperties.toArray(new Property[0]);
        }

        if ((authenticator instanceof LocalApplicationAuthenticator) ||
                (authenticator instanceof AuthenticationFlowHandler)) {
            LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();
            localAuthenticatorConfig.setName(authenticator.getName());
            localAuthenticatorConfig.setProperties(configProperties);
            localAuthenticatorConfig.setDisplayName(authenticator.getFriendlyName());
            localAuthenticatorConfig.setTags(getTags(authenticator));
            AuthenticatorConfig fileBasedConfig = getAuthenticatorConfig(authenticator.getName());
            localAuthenticatorConfig.setEnabled(fileBasedConfig.isEnabled());
            localAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
            ApplicationAuthenticatorService.getInstance().addLocalAuthenticator(localAuthenticatorConfig);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
            federatedAuthenticatorConfig.setName(authenticator.getName());
            federatedAuthenticatorConfig.setProperties(configProperties);
            federatedAuthenticatorConfig.setDisplayName(authenticator.getFriendlyName());
            federatedAuthenticatorConfig.setTags(getTags(authenticator));
            federatedAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
            ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            RequestPathAuthenticatorConfig reqPathAuthenticatorConfig = new RequestPathAuthenticatorConfig();
            reqPathAuthenticatorConfig.setName(authenticator.getName());
            reqPathAuthenticatorConfig.setProperties(configProperties);
            reqPathAuthenticatorConfig.setDisplayName(authenticator.getFriendlyName());
            reqPathAuthenticatorConfig.setTags(getTags(authenticator));
            AuthenticatorConfig fileBasedConfig = getAuthenticatorConfig(authenticator.getName());
            reqPathAuthenticatorConfig.setEnabled(fileBasedConfig.isEnabled());
            reqPathAuthenticatorConfig.setDefinedByType(DefinedByType.SYSTEM);
            ApplicationAuthenticatorService.getInstance().addRequestPathAuthenticator(reqPathAuthenticatorConfig);
        }

        if (log.isDebugEnabled()) {
            log.debug("Added application authenticator : " + authenticator.getName());
        }
    }

    private static String[] getTags(ApplicationAuthenticator authenticator) {

        if (!authenticator.isAPIBasedAuthenticationSupported()) {
            return authenticator.getTags();
        }

        List<String> tagList = new ArrayList<>();
        if (authenticator.getTags() != null) {
            Collections.addAll(tagList, authenticator.getTags());
        }
        tagList.add(API_AUTH);
        return tagList.toArray(new String[0]);
    }

    @Reference(
            name = "session.serializer",
            service = SessionSerializer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSessionSerializer"
    )
    protected void setSessionSerializer(SessionSerializer sessionSerializer) {

        SessionSerializer existingSessionSerializer = FrameworkServiceDataHolder.getInstance().getSessionSerializer();

        if (existingSessionSerializer != null) {
            log.warn("Multiple Session Serializers are registered. Serializer:"
                    + existingSessionSerializer.getClass().getName() + " will be replaced with "
                    + sessionSerializer.getClass().getName());
        }
        FrameworkServiceDataHolder.getInstance().setSessionSerializer(sessionSerializer);
        log.info("Session serializer got registered: " + sessionSerializer.getClass().getName());
    }

    protected void unsetSessionSerializer(SessionSerializer sessionSerializer) {

        FrameworkServiceDataHolder.getInstance().setSessionSerializer(new JavaSessionSerializer());

        if (log.isDebugEnabled()) {
            log.debug("Removed session serializer.");
        }

    }

    protected void unsetAuthenticator(ApplicationAuthenticator authenticator) {

        ApplicationAuthenticatorManager.getInstance().removeSystemDefinedAuthenticator(authenticator);
        String authenticatorName = authenticator.getName();
        ApplicationAuthenticatorService appAuthenticatorService = ApplicationAuthenticatorService.getInstance();

        if (authenticator instanceof LocalApplicationAuthenticator) {
            LocalAuthenticatorConfig localAuthenticatorConfig = appAuthenticatorService.getLocalAuthenticatorByName
                    (authenticatorName);
            appAuthenticatorService.removeLocalAuthenticator(localAuthenticatorConfig);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FederatedAuthenticatorConfig federatedAuthenticatorConfig = appAuthenticatorService
                    .getSystemDefinedFederatedAuthenticatorByName(authenticatorName);
            appAuthenticatorService.removeFederatedAuthenticator(federatedAuthenticatorConfig);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            RequestPathAuthenticatorConfig reqPathAuthenticatorConfig = appAuthenticatorService
                    .getRequestPathAuthenticatorByName(authenticatorName);
            appAuthenticatorService.removeRequestPathAuthenticator(reqPathAuthenticatorConfig);
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed application authenticator : " + authenticator.getName());
        }

    }

    @Reference(
            name = "identity.processor",
            service = IdentityProcessor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeIdentityProcessor"
    )
    protected void addIdentityProcessor(IdentityProcessor requestProcessor) {

        FrameworkServiceDataHolder.getInstance().getIdentityProcessors().add(requestProcessor);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getIdentityProcessors(),
                new HandlerComparator());
        Collections.reverse(FrameworkServiceDataHolder.getInstance().getIdentityProcessors());
        if (log.isDebugEnabled()) {
            log.debug("Added IdentityProcessor : " + requestProcessor.getName());
        }
    }

    protected void removeIdentityProcessor(IdentityProcessor requestProcessor) {

        FrameworkServiceDataHolder.getInstance().getIdentityProcessors().remove(requestProcessor);

        if (log.isDebugEnabled()) {
            log.debug("Removed IdentityProcessor : " + requestProcessor.getName());
        }
    }

    @Reference(
            name = "identity.request.factory",
            service = HttpIdentityRequestFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeHttpIdentityRequestFactory"
    )
    protected void addHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories(),
                new HandlerComparator());
        Collections.reverse(FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories());
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void removeHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    @Reference(
            name = "identity.response.factory",
            service = HttpIdentityResponseFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeHttpIdentityResponseFactory"
    )
    protected void addHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories(),
                new HandlerComparator());
        Collections.reverse(FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories());
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void removeHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
            name = "identityCoreInitializedEventService",
            service = IdentityCoreInitializedEvent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityCoreInitializedEventService"
    )
    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    @Reference(
            name = "identity.authentication.data.publisher",
            service = AuthenticationDataPublisher.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAuthenticationDataPublisher"
    )
    protected void setAuthenticationDataPublisher(AuthenticationDataPublisher publisher) {

        if (FrameworkConstants.AnalyticsAttributes.AUTHN_DATA_PUBLISHER_PROXY.equalsIgnoreCase(publisher.getName())
                && publisher.isEnabled(null)) {
            FrameworkServiceDataHolder.getInstance().setAuthnDataPublisherProxy(publisher);
        }
    }

    protected void unsetAuthenticationDataPublisher(AuthenticationDataPublisher publisher) {

        if (FrameworkConstants.AnalyticsAttributes.AUTHN_DATA_PUBLISHER_PROXY.equalsIgnoreCase(publisher.getName())
                && publisher.isEnabled(null)) {
            FrameworkServiceDataHolder.getInstance().setAuthnDataPublisherProxy(null);
        }
    }

    @Reference(
            name = "identity.post.authn.handler",
            service = PostAuthenticationHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPostAuthenticationHandler"
    )
    protected void setPostAuthenticationHandler(PostAuthenticationHandler postAuthenticationHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Post Authenticaion Handler : " + postAuthenticationHandler.getName() + " registered");
        }
        FrameworkServiceDataHolder.getInstance().addPostAuthenticationHandler(postAuthenticationHandler);
    }

    protected void unsetPostAuthenticationHandler(PostAuthenticationHandler postAuthenticationHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Post Authenticaion Handler : " + postAuthenticationHandler.getName() + " unregistered");
        }
        FrameworkServiceDataHolder.getInstance().getPostAuthenticationHandlers().remove(postAuthenticationHandler);
    }

    @Reference(
            name = "consent.mgt.service",
            service = ConsentManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConsentMgtService"
    )
    protected void setConsentMgtService(ConsentManager consentManager) {

        if (log.isDebugEnabled()) {
            log.debug("Consent Manger is set in the Application Authentication Framework bundle.");
        }
        FrameworkServiceDataHolder.getInstance().setConsentManager(consentManager);
    }

    protected void unsetConsentMgtService(ConsentManager consentManager) {

        FrameworkServiceDataHolder.getInstance().setConsentManager(null);
    }

    @Reference(
            name = "claim.meta.mgt.service",
            service = ClaimMetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClaimMetaMgtService"
    )
    protected void setClaimMetaMgtService(ClaimMetadataManagementService claimMetaMgtService) {

        FrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(claimMetaMgtService);
    }

    protected void unsetClaimMetaMgtService(ClaimMetadataManagementService claimMetaMgtService) {

        FrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(null);
    }

    @Reference(
            name = "claim.filter.service",
            service = ClaimFilter.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClaimFilter"
    )
    protected void setClaimFilter(ClaimFilter claimFilter) {

        if (log.isDebugEnabled()) {
            log.debug("DefaultClaimFilter: " + claimFilter.getClass().getName() + " set in " +
                    "FrameworkServiceComponent.");
        }
        FrameworkServiceDataHolder.getInstance().addClaimFilter(claimFilter);
    }

    protected void unsetClaimFilter(ClaimFilter claimFilter) {

        FrameworkServiceDataHolder.getInstance().removeClaimFilter(claimFilter);
    }

    @Reference(
            name = "approles.resolver.service",
            service = ApplicationRolesResolver.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAppRolesResolverService"
    )
    protected void setAppRolesResolverService(ApplicationRolesResolver applicationRolesResolver) {

        FrameworkServiceDataHolder.getInstance().addApplicationRolesResolver(applicationRolesResolver);
        log.debug("Application Roles Resolver is set in the Application Authentication Framework bundle.");
    }

    protected void unsetAppRolesResolverService(ApplicationRolesResolver applicationRolesResolver) {

        FrameworkServiceDataHolder.getInstance().removeApplicationRolesResolver(applicationRolesResolver);
    }

    @Reference(
            name = "identity.event.service",
            service = IdentityEventService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityEventService"
    )
    protected void setIdentityEventService(IdentityEventService identityEventService) {

        FrameworkServiceDataHolder.getInstance().setIdentityEventService(identityEventService);
    }

    protected void unsetIdentityEventService(IdentityEventService identityEventService) {

        FrameworkServiceDataHolder.getInstance().setIdentityEventService(null);
    }

    @Reference(
            name = "function.library.management.service",
            service = FunctionLibraryManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetFunctionLibraryManagementService"
    )
    protected void setFunctionLibraryManagementService
            (FunctionLibraryManagementService functionLibraryManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("FunctionLibraryManagementService is set in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setFunctionLibraryManagementService(functionLibraryManagementService);
    }

    @Reference(
            name = "secret.config.manager",
            service = SecretResolveManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretConfigManager"
    )
    protected void setSecretConfigManager(SecretResolveManager secretConfigManager) {

        if (log.isDebugEnabled()) {
            log.debug("Secret Config Manager is set from functions");
        }
        FrameworkServiceDataHolder.getInstance().setSecretConfigManager(secretConfigManager);
    }

    protected void unsetSecretConfigManager(SecretResolveManager secretConfigManager) {

        if (log.isDebugEnabled()) {
            log.debug("Secret Config Manager is unset from functions");
        }
        FrameworkServiceDataHolder.getInstance().setSecretConfigManager(null);
    }

    protected void unsetFunctionLibraryManagementService
            (FunctionLibraryManagementService functionLibraryManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("FunctionLibraryManagementService is unset in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setFunctionLibraryManagementService(null);
    }

    public static FunctionLibraryManagementService getFunctionLibraryManagementService() {

        return FrameworkServiceDataHolder.getInstance().getFunctionLibraryManagementService();
    }

    public static SecretResolveManager getSecretConfigManager() {

        return FrameworkServiceDataHolder.getInstance().getSecretConfigManager();
    }

    /**
     * Load and read the JS function in require.js file.
     */
    private void loadCodeForRequire() {

        ClassLoader loader = FrameworkServiceComponent.class.getClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream("js/require.js")) {
            requireCode = IOUtils.toString(resourceStream);
            FrameworkServiceDataHolder.getInstance().setCodeForRequireFunction(requireCode);
        } catch (IOException e) {
            log.error("Failed to read require.js file. Therefore, require() function doesn't support in" +
                    "adaptive authentication scripts.", e);
        }
    }

    /**
     * Load and read the JS function in secrets.js file.
     */
    private void loadCodeForSecrets() {

        try {
            ClassLoader loader = FrameworkServiceComponent.class.getClassLoader();
            InputStream resourceStream = loader.getResourceAsStream("js/secrets.js");
            secretsCode = IOUtils.toString(resourceStream);
            FrameworkServiceDataHolder.getInstance().setCodeForSecretsFunction(secretsCode);
        } catch (IOException e) {
            log.error("Secrets object for secret resolution will not be available for adaptive auth scripts.", e);
        }
    }

    @Reference(
            name = "identity.user.profile.mgt.component",
            service = FederatedAssociationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetFederatedAssociationManagerService"
    )
    protected void setFederatedAssociationManagerService(FederatedAssociationManager
                                                                     federatedAssociationManagerService) {

        if (log.isDebugEnabled()) {
            log.debug("Federated Association Manager Service is set in the Application Authentication Framework " +
                    "bundle");
        }
        FrameworkServiceDataHolder.getInstance().setFederatedAssociationManager(federatedAssociationManagerService);
    }

    protected void unsetFederatedAssociationManagerService(FederatedAssociationManager
                                                                   federatedAssociationManagerService) {

        if (log.isDebugEnabled()) {
            log.debug("Federated Association Manager Service is unset in the Application Authentication Framework " +
                    "bundle");
        }
        FrameworkServiceDataHolder.getInstance().setFederatedAssociationManager(null);
    }

    @Reference(
            name = "MultiAttributeLoginService",
            service = MultiAttributeLoginService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMultiAttributeLoginService")
    protected void setMultiAttributeLoginService(MultiAttributeLoginService multiAttributeLogin) {

        FrameworkServiceDataHolder.getInstance().setMultiAttributeLoginService(multiAttributeLogin);
    }

    protected void unsetMultiAttributeLoginService(MultiAttributeLoginService multiAttributeLogin) {

        FrameworkServiceDataHolder.getInstance().setMultiAttributeLoginService(null);
    }

    private AuthenticatorConfig getAuthenticatorConfig(String name) {

        AuthenticatorConfig authConfig = FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean(name);
        if (authConfig == null) {
            authConfig = new AuthenticatorConfig();
            authConfig.setParameterMap(new HashMap<String, String>());
        }
        return authConfig;
    }

    @Reference(
            name = "session.context.listener",
            service = SessionContextMgtListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSessionContextListener"
    )
    protected void setSessionContextListener(SessionContextMgtListener sessionListener) {

        FrameworkServiceDataHolder.getInstance().setSessionContextMgtListener(sessionListener.getInboundType(),
                sessionListener);
    }

    protected void unsetSessionContextListener(SessionContextMgtListener sessionListener) {

        FrameworkServiceDataHolder.getInstance().removeSessionContextMgtListener(sessionListener.getInboundType());
    }

    @Reference(
            name = "organization.mgt.initialize.service",
            service = OrganizationManagementInitialize.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManagementEnablingService"
    )
    protected void setOrganizationManagementEnablingService(
            OrganizationManagementInitialize organizationManagementInitializeService) {

        FrameworkServiceDataHolder.getInstance()
                .setOrganizationManagementEnable(organizationManagementInitializeService);
    }

    protected void unsetOrganizationManagementEnablingService(
            OrganizationManagementInitialize organizationManagementInitializeInstance) {

        FrameworkServiceDataHolder.getInstance().setOrganizationManagementEnable(null);
    }

    @Reference(
            name = "idp.mgt.dscomponent",
            service = IdpManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityProviderManager"
    )
    protected void setIdentityProviderManager(IdpManager idpMgtService) {

        FrameworkServiceDataHolder.getInstance().setIdentityProviderManager(idpMgtService);
    }

    protected void unsetIdentityProviderManager(IdpManager idpMgtService) {

        FrameworkServiceDataHolder.getInstance().setIdentityProviderManager(null);
    }

    @Reference(
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagement"
    )
    public void setApplicationManagement(ApplicationManagementService applicationManagement) {

        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagement);
    }

    public void unsetApplicationManagement(ApplicationManagementService applicationManagementService) {

        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(null);
    }

    @Reference(name = "identity.organization.management.component",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager")
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        FrameworkServiceDataHolder.getInstance().setOrganizationManager(organizationManager);
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        FrameworkServiceDataHolder.getInstance().setOrganizationManager(null);
    }

    @Reference(
            name = "resource.configuration.manager",
            service = ConfigurationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigurationManager"
    )
    protected void registerConfigurationManager(ConfigurationManager configurationManager) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the configuration manager in Application Authentication Framework bundle.");
        }
        FrameworkServiceDataHolder.getInstance().setConfigurationManager(configurationManager);
    }

    protected void unregisterConfigurationManager(ConfigurationManager configurationManager) {

        if (log.isDebugEnabled()) {
            log.debug("Unsetting the configuration manager in Application Authentication Framework bundle.");
        }
        FrameworkServiceDataHolder.getInstance().setConfigurationManager(null);
    }

    @Reference(
            name = "org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService",
            service = org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRoleManagementServiceV2")
    protected void setRoleManagementServiceV2(RoleManagementService roleManagementService) {

        FrameworkServiceDataHolder.getInstance().setRoleManagementServiceV2(roleManagementService);
        log.debug("RoleManagementServiceV2 set in FrameworkServiceComponent bundle.");
    }

    protected void unsetRoleManagementServiceV2(RoleManagementService roleManagementService) {

        FrameworkServiceDataHolder.getInstance().setRoleManagementServiceV2(null);
        log.debug("RoleManagementServiceV2 unset in FrameworkServiceComponent bundle.");
    }

    @Reference(
            name = "org.wso2.carbon.identity.application.authentication.framework.UserDefinedAuthenticatorService",
            service =
                    org.wso2.carbon.identity.application.authentication.framework.UserDefinedAuthenticatorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserDefinedAuthenticatorService")
    protected void setUserDefinedAuthenticatorService(UserDefinedAuthenticatorService authenticatorService) {

        FrameworkServiceDataHolder.getInstance().setUserDefinedAuthenticatorService(authenticatorService);
        log.debug("UserDefinedAuthenticatorService set in FrameworkServiceComponent bundle.");
    }

    protected void unsetUserDefinedAuthenticatorService(UserDefinedAuthenticatorService authenticatorService) {

        FrameworkServiceDataHolder.getInstance().setUserDefinedAuthenticatorService(authenticatorService);
        log.debug("UserDefinedAuthenticatorService unset in FrameworkServiceComponent bundle.");
    }

    @Reference(
            name = "org.discovery.handler",
            service = OrganizationDiscoveryHandler.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationDiscoveryHandler"
    )
    protected void setOrganizationDiscoveryHandler(OrganizationDiscoveryHandler organizationDiscoveryHandler) {

        FrameworkServiceDataHolder.getInstance().setOrganizationDiscoveryHandler(organizationDiscoveryHandler);
        log.debug("OrganizationDiscoveryHandler set in FrameworkServiceComponent bundle.");
    }

    protected void unsetOrganizationDiscoveryHandler(OrganizationDiscoveryHandler organizationDiscoveryHandler) {

        FrameworkServiceDataHolder.getInstance().setOrganizationDiscoveryHandler(null);
        log.debug("OrganizationDiscoveryHandler unset in FrameworkServiceComponent bundle.");
    }
}
