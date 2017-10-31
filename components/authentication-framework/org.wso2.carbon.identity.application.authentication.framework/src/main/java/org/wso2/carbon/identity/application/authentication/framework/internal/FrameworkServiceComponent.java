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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.internal;

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
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticationService;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkLoginResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkLogoutResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityServlet;
import org.wso2.carbon.identity.application.authentication.framework.listener.AuthenticationEndpointTenantActivityListener;
import org.wso2.carbon.identity.application.authentication.framework.servlet.CommonAuthenticationServlet;
import org.wso2.carbon.identity.application.authentication.framework.servlet.LoginContextServlet;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.core.handler.HandlerComparator;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Collections;
import java.util.List;
import javax.servlet.Servlet;

/**
 * OSGi declarative services component which handled registration and unregistration of FrameworkServiceComponent.
 */

@Component(
        name = "identity.application.authentication.framework.component",
        immediate = true
)
public class FrameworkServiceComponent {

    public static final String COMMON_SERVLET_URL = "/commonauth";
    private static final String IDENTITY_SERVLET_URL = "/identity";
    private static final String LOGIN_CONTEXT_SERVLET_URL = "/logincontext";

    private static final Log log = LogFactory.getLog(FrameworkServiceComponent.class);

    private HttpService httpService;

    public static RealmService getRealmService() {
        return FrameworkServiceDataHolder.getInstance().getRealmService();
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
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
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
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
        BundleContext bundleContext = ctxt.getBundleContext();
        bundleContext.registerService(ApplicationAuthenticationService.class.getName(), new
                ApplicationAuthenticationService(), null);
        ;
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

        FrameworkServiceDataHolder.getInstance().setBundleContext(bundleContext);
        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().add(new HttpIdentityRequestFactory());
        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().add(new
                FrameworkLoginResponseFactory());
        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().add(new
                FrameworkLogoutResponseFactory());

        //this is done to load SessionDataStore class and start the cleanup tasks.
        SessionDataStore.getInstance();

        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is deactivated");
        }

        FrameworkServiceDataHolder.getInstance().setBundleContext(null);
    }

    @Reference(
            name = "osgi.httpservice",
            service = org.osgi.service.http.HttpService.class,
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
            service = org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAuthenticator"
    )
    protected void setAuthenticator(ApplicationAuthenticator authenticator) {

        FrameworkServiceDataHolder.getInstance().getAuthenticators().add(authenticator);

        Property[] configProperties = null;

        if (authenticator.getConfigurationProperties() != null
            && !authenticator.getConfigurationProperties().isEmpty()) {
            configProperties = authenticator.getConfigurationProperties().toArray(new Property[0]);
        }

        if (authenticator instanceof LocalApplicationAuthenticator) {
            LocalAuthenticatorConfig localAuthenticatorConfig = new LocalAuthenticatorConfig();
            localAuthenticatorConfig.setName(authenticator.getName());
            localAuthenticatorConfig.setProperties(configProperties);
            localAuthenticatorConfig.setDisplayName(authenticator.getFriendlyName());
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
            service = org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor.class,
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
            service = org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityRequestFactory.class,
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
            service = org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponseFactory.class,
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
            service = org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent.class,
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
            service = org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher.class,
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
}
