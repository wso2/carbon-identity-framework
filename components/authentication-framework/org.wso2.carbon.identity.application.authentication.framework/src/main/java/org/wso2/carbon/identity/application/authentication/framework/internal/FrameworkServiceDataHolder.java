/*
 * Copyright (c) 2015-2023, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationMethodNameTranslator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.ServerSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.SequenceLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JSExecutionSupervisor;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.ApplicationRolesResolver;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimFilter;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimFilter;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.SSOConsentService;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.AsyncSequenceExecutor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.listener.SessionContextMgtListener;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtService;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionSerializer;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.core.handler.HandlerComparator;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryManagementService;
import org.wso2.carbon.identity.multi.attribute.login.mgt.MultiAttributeLoginService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManagementInitialize;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Authentication framework data holder.
 */
public class FrameworkServiceDataHolder {

    private static final Log log = LogFactory.getLog(FrameworkServiceDataHolder.class);
    private static FrameworkServiceDataHolder instance = new FrameworkServiceDataHolder();
    private BundleContext bundleContext = null;
    private RealmService realmService = null;
    private List<ApplicationAuthenticator> authenticators = new ArrayList<>();
    private List<ApplicationRolesResolver> applicationRolesResolvers = new ArrayList<>();
    private long nanoTimeReference = 0;
    private long unixTimeReference = 0;
    private List<IdentityProcessor> identityProcessors = new ArrayList<>();
    private List<HttpIdentityRequestFactory> httpIdentityRequestFactories = new ArrayList<>();
    private List<HttpIdentityResponseFactory> httpIdentityResponseFactories = new ArrayList<>();
    private AuthenticationDataPublisher authnDataPublisherProxy = null;
    private SequenceLoader sequenceLoader = null;
    private JsBaseGraphBuilderFactory jsGraphBuilderFactory;
    private AuthenticationMethodNameTranslator authenticationMethodNameTranslator;
    private List<PostAuthenticationHandler> postAuthenticationHandlers = new ArrayList<>();
    private PostAuthenticationMgtService postAuthenticationMgtService = null;
    private ConsentManager consentManager = null;
    private ClaimMetadataManagementService claimMetadataManagementService = null;
    private SSOConsentService ssoConsentService;
    private JsFunctionRegistry jsFunctionRegistry;
    private List<ClaimFilter> claimFilters = new ArrayList<>();
    private AsyncSequenceExecutor asyncSequenceExecutor;
    private LongWaitStatusStoreService longWaitStatusStoreService;
    private IdentityEventService identityEventService;
    private FunctionLibraryManagementService functionLibraryManagementService = null;
    private String requireCode = "";
    private boolean userSessionMappingEnabled;

    /*
    This property restricts unnecessary user local search during federated authentication flow for authentication
    flow handlers like identifier first.
     */
    private boolean skipLocalUserSearchForAuthenticationFlowHandlersEnabled;
    private FederatedAssociationManager federatedAssociationManager;
    private ServerSessionManagementService serverSessionManagementService;
    private MultiAttributeLoginService multiAttributeLoginService;
    private Map<String, SessionContextMgtListener> sessionContextMgtListeners = new HashMap<>();
    private SessionSerializer sessionSerializer;

    private JSExecutionSupervisor jsExecutionSupervisor;
    private IdpManager identityProviderManager = null;
    private IdpManager idPManager;
    private ApplicationManagementService applicationManagementService;
    private ConfigurationManager configurationManager = null;

    private boolean isAdaptiveAuthenticationAvailable = false;
    private boolean isOrganizationManagementEnable = false;
    private OrganizationManager organizationManager;
    private RoleManagementService roleManagementServiceV2;

    private FrameworkServiceDataHolder() {

        setNanoTimeReference(System.nanoTime());
        setUnixTimeReference(System.currentTimeMillis());
    }

    public static FrameworkServiceDataHolder getInstance() {

        return instance;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * @return
     * @throws FrameworkException
     * @Deprecated The usage of bundle context outside of the component should never be needed. Component should
     * provide necessary wiring for any place which require the BundleContext.
     */
    @Deprecated
    public BundleContext getBundleContext() {

        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {

        this.bundleContext = bundleContext;
    }

    public List<ApplicationAuthenticator> getAuthenticators() {

        return authenticators;
    }

    /**
     * Add an application role resolver to the list of application role resolvers.
     *
     * @param applicationRolesResolver Application roles resolver implementation.
     */
    public void addApplicationRolesResolver(ApplicationRolesResolver applicationRolesResolver) {

        applicationRolesResolvers.add(applicationRolesResolver);
        applicationRolesResolvers.sort(getApplicationRolesResolverComparator());
    }

    /**
     * Remove an application role resolver from the list of application role resolvers.
     *
     * @param applicationRolesResolver Application roles resolver implementation.
     */
    public void removeApplicationRolesResolver(ApplicationRolesResolver applicationRolesResolver) {

        applicationRolesResolvers.removeIf(applicationRolesResolver1 -> applicationRolesResolver1.getClass().getName()
                .equals(applicationRolesResolver.getClass().getName()));
    }

    /**
     * Get the list of application roles resolvers.
     *
     * @return List of application roles resolvers.
     */
    public List<ApplicationRolesResolver> getApplicationRolesResolvers() {

        return applicationRolesResolvers;
    }

    /**
     * Get the highest priority application roles resolver.
     *
     * @return the highest priority application roles resolver.
     */
    public ApplicationRolesResolver getHighestPriorityApplicationRolesResolver() {

        if (applicationRolesResolvers.isEmpty()) {
            log.info("No Registered Application roles resolvers available");
            return null;
        }
        return applicationRolesResolvers.get(0);
    }

    private Comparator<ApplicationRolesResolver> getApplicationRolesResolverComparator() {

        // Sort based on priority in descending order, ie. the highest priority comes to the first element of the list.
        return Comparator.comparingInt(ApplicationRolesResolver::getPriority).reversed();
    }

    public long getNanoTimeReference() {

        return nanoTimeReference;
    }

    private void setNanoTimeReference(long nanoTimeReference) {

        this.nanoTimeReference = nanoTimeReference;
    }

    public long getUnixTimeReference() {

        return unixTimeReference;
    }

    private void setUnixTimeReference(long unixTimeReference) {

        this.unixTimeReference = unixTimeReference;
    }

    public List<HttpIdentityRequestFactory> getHttpIdentityRequestFactories() {

        return httpIdentityRequestFactories;
    }

    public List<IdentityProcessor> getIdentityProcessors() {

        return identityProcessors;
    }

    public List<HttpIdentityResponseFactory> getHttpIdentityResponseFactories() {

        return httpIdentityResponseFactories;
    }

    public AuthenticationDataPublisher getAuthnDataPublisherProxy() {

        return authnDataPublisherProxy;
    }

    public void setAuthnDataPublisherProxy(AuthenticationDataPublisher authnDataPublisherProxy) {

        this.authnDataPublisherProxy = authnDataPublisherProxy;
    }

    public SequenceLoader getSequenceLoader() {

        return sequenceLoader;
    }

    public void setSequenceLoader(SequenceLoader sequenceLoader) {

        this.sequenceLoader = sequenceLoader;
    }

    public AuthenticationMethodNameTranslator getAuthenticationMethodNameTranslator() {

        return authenticationMethodNameTranslator;
    }

    public void setAuthenticationMethodNameTranslator(
            AuthenticationMethodNameTranslator authenticationMethodNameTranslator) {

        this.authenticationMethodNameTranslator = authenticationMethodNameTranslator;
    }

    public JsBaseGraphBuilderFactory getJsGraphBuilderFactory() {

        return jsGraphBuilderFactory;
    }

    public void setJsGraphBuilderFactory(JsBaseGraphBuilderFactory jsGraphBuilderFactory) {

        this.jsGraphBuilderFactory = jsGraphBuilderFactory;
    }

    public MultiAttributeLoginService getMultiAttributeLoginService() {

        return multiAttributeLoginService;
    }

    public void setMultiAttributeLoginService(MultiAttributeLoginService multiAttributeLoginService) {

        this.multiAttributeLoginService = multiAttributeLoginService;
    }

    /**
     * Adds a post authentication handler.
     *
     * @param postAuthenticationHandler Post authentication handler implementation.
     */
    public void addPostAuthenticationHandler(PostAuthenticationHandler postAuthenticationHandler) {

        synchronized (postAuthenticationHandlers) {
            this.postAuthenticationHandlers.add(postAuthenticationHandler);
            postAuthenticationHandlers.sort(new HandlerComparator());
        }
    }

    /**
     * Get set of post authentication handlers registered via OSGI services.
     *
     * @return List of Post Authentication handlers.
     */
    public List<PostAuthenticationHandler> getPostAuthenticationHandlers() {

        return this.postAuthenticationHandlers;
    }

    /**
     * Get post authentication management service.
     *
     * @return Post authentication management service.
     */
    public PostAuthenticationMgtService getPostAuthenticationMgtService() {

        return this.postAuthenticationMgtService;
    }

    /**
     * Set post authentication management service.
     *
     * @param postAuthenticationMgtService Post authentication management service.
     */
    public void setPostAuthenticationMgtService(PostAuthenticationMgtService postAuthenticationMgtService) {

        this.postAuthenticationMgtService = postAuthenticationMgtService;
    }

    /**
     * Get {@link ConsentManager} service.
     *
     * @return Consent manager service
     */
    public ConsentManager getConsentManager() {

        return consentManager;
    }

    /**
     * Set {@link ConsentManager} service.
     *
     * @param consentManager Instance of {@link ConsentManager} service.
     */
    public void setConsentManager(ConsentManager consentManager) {

        this.consentManager = consentManager;
    }

    /**
     * Get {@link ClaimMetadataManagementService}.
     *
     * @return ClaimMetadataManagementService.
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    /**
     * Set {@link ClaimMetadataManagementService}.
     *
     * @param claimMetadataManagementService Instance of {@link ClaimMetadataManagementService}.
     */
    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }

    /**
     * Get {@link SSOConsentService}.
     *
     * @return SSOConsentService.
     */
    public SSOConsentService getSSOConsentService() {

        return ssoConsentService;
    }

    /**
     * Set {@link SSOConsentService}.
     *
     * @param ssoConsentService Instance of {@link SSOConsentService}.
     */
    public void setSSOConsentService(SSOConsentService ssoConsentService) {

        this.ssoConsentService = ssoConsentService;
    }

    /**
     * Get the {@link JsFunctionRegistry}
     *
     * @return JsFunctionRegistry which hold the native functions
     */
    public JsFunctionRegistry getJsFunctionRegistry() {

        return jsFunctionRegistry;
    }

    /**
     * Set the {@link JsFunctionRegistry}
     *
     * @param jsFunctionRegistry JsFunctionRegistry which hold the native functions
     */
    public void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        this.jsFunctionRegistry = jsFunctionRegistry;
    }

    /**
     * @return The Claim Filter with the highest priority.
     */
    public ClaimFilter getHighestPriorityClaimFilter() {

        if (claimFilters.isEmpty()) {
            log.info("No Registered Claim Filters available. Using the default claim filter.");
            return new DefaultClaimFilter();
        }
        return claimFilters.get(0);
    }

    /**
     * Get all the registered claim filters.
     *
     * @return list of claim filters
     */
    public List<ClaimFilter> getClaimFilters() {

        return claimFilters;
    }

    /**
     * Add claim filters.
     *
     * @param claimFilter a claim filter
     */
    public void addClaimFilter(ClaimFilter claimFilter) {

        claimFilters.add(claimFilter);
        claimFilters.sort(getClaimFilterComparator());

    }

    public void removeClaimFilter(ClaimFilter claimFilter) {

        Iterator<ClaimFilter> claimFilterIterator = claimFilters.iterator();
        while (claimFilterIterator.hasNext()) {
            if (claimFilterIterator.next().getClass().getName().equals(claimFilter.getClass().getName())) {
                claimFilterIterator.remove();
            }
        }
    }

    public AsyncSequenceExecutor getAsyncSequenceExecutor() {

        return asyncSequenceExecutor;
    }

    public void setAsyncSequenceExecutor(AsyncSequenceExecutor asyncSequenceExecutor) {

        this.asyncSequenceExecutor = asyncSequenceExecutor;
    }

    public LongWaitStatusStoreService getLongWaitStatusStoreService() {

        return longWaitStatusStoreService;
    }

    public void setLongWaitStatusStoreService(LongWaitStatusStoreService longWaitStatusStoreService) {

        this.longWaitStatusStoreService = longWaitStatusStoreService;
    }

    private Comparator<ClaimFilter> getClaimFilterComparator() {

        // Sort based on priority in descending order, ie. highest priority comes to the first element of the list.
        return Comparator.comparingInt(ClaimFilter::getPriority).reversed();
    }

    /**
     * Get {@link IdentityEventService}.
     *
     * @return IdentityEventService.
     */
    public IdentityEventService getIdentityEventService() {

        return identityEventService;
    }

    /**
     * Set {@link IdentityEventService}.
     *
     * @param identityEventService Instance of {@link IdentityEventService}.
     */
    public void setIdentityEventService(IdentityEventService identityEventService) {

        this.identityEventService = identityEventService;
    }

    /**
     * Get function library management service.
     *
     * @return functionLibraryManagementService
     */
    public FunctionLibraryManagementService getFunctionLibraryManagementService() {

        return functionLibraryManagementService;
    }

    /**
     * Set function library management service.
     *
     * @param functionLibraryManagementService functionLibraryManagementService
     */
    public void setFunctionLibraryManagementService(FunctionLibraryManagementService functionLibraryManagementService) {

        this.functionLibraryManagementService = functionLibraryManagementService;
    }

    /**
     * Get require() function's code.
     *
     * @return code snippet of require()
     */
    public String getCodeForRequireFunction() {

        return requireCode;
    }

    /**
     * Set require() function's code.
     *
     * @param requireCode code snippet of require() function
     */
    public void setCodeForRequireFunction(String requireCode) {

        this.requireCode = requireCode;
    }

    /**
     * Is user session mapping enabled.
     *
     * @return return true if user session mapping enabled.
     */
    public boolean isUserSessionMappingEnabled() {

        return this.userSessionMappingEnabled;
    }

    /**
     * Is skip local user search for authentication flow handlers enabled.
     *
     * @return return true if skip local user search for authentication flow handlers enabled.
     */
    public boolean isSkipLocalUserSearchForAuthenticationFlowHandlersEnabled() {

        return this.skipLocalUserSearchForAuthenticationFlowHandlersEnabled;
    }

    /**
     * Set user session mapping enabled.
     *
     * @param userSessionMappingEnabled
     */
    public void setUserSessionMappingEnabled(boolean userSessionMappingEnabled) {

        if (log.isDebugEnabled()) {
            if (userSessionMappingEnabled) {
                log.debug("User session mapping enabled for server.");
            } else {
                log.debug("User session mapping not enabled for server.");
            }
        }

        this.userSessionMappingEnabled = userSessionMappingEnabled;
    }

    /**
     * Get the server config for skip user local search during federated authentication flow
     *
     * @param skipLocalUserSearchForAuthenticationFlowHandlersEnabled
     */
    public void setSkipLocalUserSearchForAuthenticationFlowHandlersEnabled
    (boolean skipLocalUserSearchForAuthenticationFlowHandlersEnabled) {

        if (log.isDebugEnabled()) {
            if (skipLocalUserSearchForAuthenticationFlowHandlersEnabled) {
                log.debug("Skip Local User Search For Authentication Flow Handlers enabled for server.");
            } else {
                log.debug("Skip Local User Search For Authentication Flow Handlers not enabled for server.");
            }
        }

        this.skipLocalUserSearchForAuthenticationFlowHandlersEnabled =
                skipLocalUserSearchForAuthenticationFlowHandlersEnabled;
    }

    public FederatedAssociationManager getFederatedAssociationManager() {

        return federatedAssociationManager;
    }

    public void setFederatedAssociationManager(FederatedAssociationManager federatedAssociationManager) {

        this.federatedAssociationManager = federatedAssociationManager;
    }

    public ServerSessionManagementService getServerSessionManagementService() {

        return serverSessionManagementService;
    }

    public void setServerSessionManagementService(
            ServerSessionManagementService sessionManagementService) {

        this.serverSessionManagementService = sessionManagementService;
    }

    public JSExecutionSupervisor getJsExecutionSupervisor() {

        return jsExecutionSupervisor;
    }

    public void setJsExecutionSupervisor(JSExecutionSupervisor jsExecutionSupervisor) {

        this.jsExecutionSupervisor = jsExecutionSupervisor;
    }

    public SessionContextMgtListener getSessionContextMgtListener(String inboundType) {

        return sessionContextMgtListeners.get(inboundType);
    }

    public void setSessionContextMgtListener(String inboundType, SessionContextMgtListener sessionContextMgtListener) {

        sessionContextMgtListeners.put(inboundType, sessionContextMgtListener);
    }

    public void removeSessionContextMgtListener(String inboundType) {

        sessionContextMgtListeners.remove(inboundType);
    }

    public SessionSerializer getSessionSerializer() {
        return sessionSerializer;
    }

    public void setSessionSerializer(SessionSerializer sessionSerializer) {
        this.sessionSerializer = sessionSerializer;
    }

    /**
     * Get adaptive authentication available or not.
     *
     * @return isAdaptiveAuthenticationAvailable
     */
    public boolean isAdaptiveAuthenticationAvailable() {

        return isAdaptiveAuthenticationAvailable;
    }

    /**
     * Set adaptive authentication availability.
     *
     * @param adaptiveAuthenticationAvailable adaptiveAuthenticationAvailable
     */
    public void setAdaptiveAuthenticationAvailable(boolean adaptiveAuthenticationAvailable) {

        isAdaptiveAuthenticationAvailable = adaptiveAuthenticationAvailable;
    }

    /**
     * Get is organization management enabled.
     *
     * @return True if organization management is enabled.
     */
    public boolean isOrganizationManagementEnabled() {

        return isOrganizationManagementEnable;
    }

    /**
     * Set organization management enable/disable state.
     *
     * @param organizationManagementInitializeService OrganizationManagementInitializeInstance.
     */
    public void setOrganizationManagementEnable(
            OrganizationManagementInitialize organizationManagementInitializeService) {

        if (organizationManagementInitializeService != null) {
            isOrganizationManagementEnable = organizationManagementInitializeService.isOrganizationManagementEnabled();
        }
    }

    /**
     * Get {@link OrganizationManager}.
     *
     * @return organization manager instance {@link OrganizationManager}.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    /**
     * Set {@link OrganizationManager}.
     *
     * @param organizationManager Instance of {@link OrganizationManager}.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    public IdpManager getIdentityProviderManager() {

        return identityProviderManager;
    }

    public void setIdentityProviderManager(IdpManager identityProviderManager) {

        this.identityProviderManager = identityProviderManager;
    }

    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }

    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    /**
     * Get {@link RoleManagementService}.
     *
     * @return Instance of {@link RoleManagementService}.
     */
    public RoleManagementService getRoleManagementServiceV2() {

        return roleManagementServiceV2;
    }

    /**
     * Set {@link RoleManagementService}.
     *
     * @param roleManagementServiceV2 Instance of {@link RoleManagementService}.
     */
    public void setRoleManagementServiceV2(RoleManagementService roleManagementServiceV2) {

        this.roleManagementServiceV2 = roleManagementServiceV2;
    }
}
