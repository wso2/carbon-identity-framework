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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.internal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
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
import org.wso2.carbon.identity.application.authentication.framework.UserSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.internal.impl.ServerSessionManagementServiceImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.impl.UserSessionManagementServiceImpl;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.CacheBackedLongWaitStatusDAO;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.LongWaitStatusDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimFilter;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimFilter;
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
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityServlet;
import org.wso2.carbon.identity.application.authentication.framework.internal.impl.AuthenticationMethodNameTranslatorImpl;
import org.wso2.carbon.identity.application.authentication.framework.listener.AuthenticationEndpointTenantActivityListener;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtService;
import org.wso2.carbon.identity.application.authentication.framework.servlet.CommonAuthenticationServlet;
import org.wso2.carbon.identity.application.authentication.framework.servlet.LoginContextServlet;
import org.wso2.carbon.identity.application.authentication.framework.servlet.LongWaitStatusServlet;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.processor.SessionExtenderProcessor;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.request.SessionExtenderRequestFactory;
import org.wso2.carbon.identity.application.authentication.framework.session.extender.response.SessionExtenderResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.core.handler.HandlerComparator;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryManagementService;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.servlet.Servlet;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.promptOnLongWait;
import static org.wso2.carbon.identity.base.IdentityConstants.TRUE;

/**
 * OSGi declarative services component which handled registration and unregistration of FrameworkServiceComponent.
 */

@Component(
        name = "identity.application.authentication.framework.component",
        immediate = true
)
public class FrameworkServiceComponent {

    public static final String COMMON_SERVLET_URL = "/commonauth";
    public static final String IS_HANDLER = "IS_HANDLER";
    private static final String IDENTITY_SERVLET_URL = "/identity";
    private static final String LOGIN_CONTEXT_SERVLET_URL = "/logincontext";
    private static final String LONGWAITSTATUS_SERVLET_URL = "/longwaitstatus";
    private static final Log log = LogFactory.getLog(FrameworkServiceComponent.class);

    private HttpService httpService;
    private ConsentMgtPostAuthnHandler consentMgtPostAuthnHandler = new ConsentMgtPostAuthnHandler();
    private String requireCode;

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

    public static RegistryService getRegistryService() {

        return FrameworkServiceDataHolder.getInstance().getRegistryService();
    }

    @Reference(
            name = "registry.service",
            service = RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService"
    )
    protected void setRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService is set in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRegistryService(registryService);
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

    public static List<ApplicationAuthenticator> getAuthenticators() {

        return FrameworkServiceDataHolder.getInstance().getAuthenticators();
    }

    @SuppressWarnings("unchecked")
    @Activate
    protected void activate(ComponentContext ctxt) {

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        dataHolder.setJsFunctionRegistry(new JsFunctionRegistryImpl());
        BundleContext bundleContext = ctxt.getBundleContext();
        bundleContext.registerService(ApplicationAuthenticationService.class.getName(), new
                ApplicationAuthenticationService(), null);
        bundleContext.registerService(JsFunctionRegistry.class, dataHolder.getJsFunctionRegistry(), null);
        bundleContext.registerService(UserSessionManagementService.class.getName(),
                new UserSessionManagementServiceImpl(), null);
        bundleContext.registerService(HttpIdentityRequestFactory.class.getName(),
                new SessionExtenderRequestFactory(), null);
        bundleContext.registerService(HttpIdentityResponseFactory.class.getName(),
                new SessionExtenderResponseFactory(), null);
        bundleContext.registerService(IdentityProcessor.class.getName(), new SessionExtenderProcessor(), null);

        ServerSessionManagementService serverSessionManagementService = new ServerSessionManagementServiceImpl();
        bundleContext.registerService(ServerSessionManagementService.class.getName(),
                serverSessionManagementService, null);
        dataHolder.setServerSessionManagementService(serverSessionManagementService);

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
        AuthenticationMethodNameTranslatorImpl authenticationMethodNameTranslator = new AuthenticationMethodNameTranslatorImpl();
        authenticationMethodNameTranslator.initializeConfigsWithServerConfig();
        bundleContext
                .registerService(AuthenticationMethodNameTranslator.class, authenticationMethodNameTranslator, null);
        dataHolder.setAuthenticationMethodNameTranslator(authenticationMethodNameTranslator);

        // Register Common servlet
        Servlet commonAuthServlet = new ContextPathServletAdaptor(new CommonAuthenticationServlet(),
                COMMON_SERVLET_URL);

        Servlet identityServlet = new ContextPathServletAdaptor(new IdentityServlet(),
                IDENTITY_SERVLET_URL);

        Servlet loginContextServlet = new ContextPathServletAdaptor(new LoginContextServlet(),
                LOGIN_CONTEXT_SERVLET_URL);
        try {
            httpService.registerServlet(COMMON_SERVLET_URL, commonAuthServlet, null, null);
            httpService.registerServlet(IDENTITY_SERVLET_URL, identityServlet, null, null);
            httpService.registerServlet(LOGIN_CONTEXT_SERVLET_URL, loginContextServlet, null, null);
        } catch (Exception e) {
            String errMsg = "Error when registering servlets via the HttpService.";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        if (promptOnLongWait()) {
            Servlet longWaitStatusServlet = new ContextPathServletAdaptor(new LongWaitStatusServlet(),
                    LONGWAITSTATUS_SERVLET_URL);
            try {
                httpService.registerServlet(LONGWAITSTATUS_SERVLET_URL, longWaitStatusServlet, null, null);
            } catch (Exception e) {
                String errMsg = "Error when registering longwaitstatus servlet via the HttpService.";
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }

        dataHolder.setBundleContext(bundleContext);
        dataHolder.getHttpIdentityRequestFactories().add(new HttpIdentityRequestFactory());
        dataHolder.getHttpIdentityResponseFactories().add(new FrameworkLoginResponseFactory());
        dataHolder.getHttpIdentityResponseFactories().add(new FrameworkLogoutResponseFactory());
        JsGraphBuilderFactory jsGraphBuilderFactory = new JsGraphBuilderFactory();
        jsGraphBuilderFactory.init();
        UIBasedConfigurationLoader uiBasedConfigurationLoader = new UIBasedConfigurationLoader();
        dataHolder.setSequenceLoader(uiBasedConfigurationLoader);
        dataHolder.setJsGraphBuilderFactory(jsGraphBuilderFactory);

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

        bundleContext.registerService(ClaimFilter.class.getName(), new DefaultClaimFilter(), null);

        //this is done to load SessionDataStore class and start the cleanup tasks.
        SessionDataStore.getInstance();

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

        // Set user session mapping enabled.
        FrameworkServiceDataHolder.getInstance().setUserSessionMappingEnabled(FrameworkUtils
                .isUserSessionMappingEnabled());
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is deactivated");
        }

        FrameworkServiceDataHolder.getInstance().setBundleContext(null);
        SessionDataStore.getInstance().stopService();
    }

    @Reference(
            name = "osgi.httpservice",
            service = HttpService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHttpService"
    )
    protected void setHttpService(HttpService httpService) {

        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is set in the Application Authentication Framework bundle");
        }

        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {

        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is unset in the Application Authentication Framework bundle");
        }

        this.httpService = null;
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("RealmService is unset in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRealmService(null);
    }

    protected void unsetRegistryService(RegistryService registryService) {

        if (log.isDebugEnabled()) {
            log.debug("RegistryService is unset in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRegistryService(null);
    }

    @Reference(
            name = "application.authenticator",
            service = ApplicationAuthenticator.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAuthenticator"
    )
    protected void setAuthenticator(ApplicationAuthenticator authenticator) {

        FrameworkServiceDataHolder.getInstance().getAuthenticators().add(authenticator);

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
            AuthenticatorConfig fileBasedConfig = getAuthenticatorConfig(authenticator.getName());
            localAuthenticatorConfig.setEnabled(fileBasedConfig.isEnabled());
            ApplicationAuthenticatorService.getInstance().addLocalAuthenticator(localAuthenticatorConfig);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
            federatedAuthenticatorConfig.setName(authenticator.getName());
            federatedAuthenticatorConfig.setProperties(configProperties);
            federatedAuthenticatorConfig.setDisplayName(authenticator.getFriendlyName());
            ApplicationAuthenticatorService.getInstance().addFederatedAuthenticator(federatedAuthenticatorConfig);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            RequestPathAuthenticatorConfig reqPathAuthenticatorConfig = new RequestPathAuthenticatorConfig();
            reqPathAuthenticatorConfig.setName(authenticator.getName());
            reqPathAuthenticatorConfig.setProperties(configProperties);
            reqPathAuthenticatorConfig.setDisplayName(authenticator.getFriendlyName());
            AuthenticatorConfig fileBasedConfig = getAuthenticatorConfig(authenticator.getName());
            reqPathAuthenticatorConfig.setEnabled(fileBasedConfig.isEnabled());
            ApplicationAuthenticatorService.getInstance().addRequestPathAuthenticator(reqPathAuthenticatorConfig);
        }

        if (log.isDebugEnabled()) {
            log.debug("Added application authenticator : " + authenticator.getName());
        }
    }

    protected void unsetAuthenticator(ApplicationAuthenticator authenticator) {

        FrameworkServiceDataHolder.getInstance().getAuthenticators().remove(authenticator);
        String authenticatorName = authenticator.getName();
        ApplicationAuthenticatorService appAuthenticatorService = ApplicationAuthenticatorService.getInstance();

        if (authenticator instanceof LocalApplicationAuthenticator) {
            LocalAuthenticatorConfig localAuthenticatorConfig = appAuthenticatorService.getLocalAuthenticatorByName
                    (authenticatorName);
            appAuthenticatorService.removeLocalAuthenticator(localAuthenticatorConfig);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FederatedAuthenticatorConfig federatedAuthenticatorConfig = appAuthenticatorService
                    .getFederatedAuthenticatorByName(authenticatorName);
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

    /**
     * Load and read the JS function in require.js file.
     */
    private void loadCodeForRequire() {

        try {
            ClassLoader loader = FrameworkServiceComponent.class.getClassLoader();
            InputStream resourceStream = loader.getResourceAsStream("js/require.js");
            requireCode = IOUtils.toString(resourceStream);
            FrameworkServiceDataHolder.getInstance().setCodeForRequireFunction(requireCode);
        } catch (IOException e) {
            log.error("Failed to read require.js file. Therefore, require() function doesn't support in" +
                    "adaptive authentication scripts.", e);
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

    private AuthenticatorConfig getAuthenticatorConfig(String name) {

        AuthenticatorConfig authConfig = FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean(name);
        if (authConfig == null) {
            authConfig = new AuthenticatorConfig();
            authConfig.setParameterMap(new HashMap<String, String>());
        }
        return authConfig;
    }
}
