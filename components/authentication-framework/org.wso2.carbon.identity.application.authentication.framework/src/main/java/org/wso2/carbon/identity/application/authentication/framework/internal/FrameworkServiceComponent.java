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
import org.osgi.service.http.HttpService;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticationService;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.CommonInboundAuthenticationServlet;
import org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationRequestBuilder;
import org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationRequestProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationResponseProcessor;
import org.wso2.carbon.identity.application.authentication.framework.listener.AuthenticationEndpointTenantActivityListener;
import org.wso2.carbon.identity.application.authentication.framework.servlet.CommonAuthenticationServlet;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.Servlet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @scr.component name="identity.application.authentication.framework.component"
 * immediate="true"
 * @scr.reference name="osgi.httpservice"
 * interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService"
 * unbind="unsetHttpService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="application.authenticator"
 * interface="org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator"
 * cardinality="1..n" policy="dynamic" bind="setAuthenticator"
 * unbind="unsetAuthenticator"
 * @scr.reference name="identityCoreInitializedEventService"
 * interface="org.wso2.carbon.identity.core.util.IdentityCoreInitializedEvent" cardinality="1..1"
 * policy="dynamic" bind="setIdentityCoreInitializedEventService" unbind="unsetIdentityCoreInitializedEventService"
 * @scr.reference name="application.requestprocessor"
 * interface="org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationRequestProcessor"
 * cardinality="0..n" policy="dynamic" bind="setInboundRequestProcessor"
 * unbind="unsetInboundRequestProcessor"
 * @scr.reference name="application.responseprocessor"
 * interface="org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationResponseProcessor"
 * cardinality="0..n" policy="dynamic" bind="setInboundResponseProcessor"
 * unbind="unsetInboundResponseProcessor"
 * @scr.reference name="application.requestbuilder"
 * interface="org.wso2.carbon.identity.application.authentication.framework.inbound.InboundAuthenticationRequestBuilder"
 * cardinality="0..n" policy="dynamic" bind="setInboundRequestBuilder"
 * unbind="unsetInboundRequestBuilder"
 */



public class FrameworkServiceComponent {

    public static final String COMMON_SERVLET_URL = "/commonauth";
    private static final String COMMON_INBOUND_SERVLET_URL = "/authentication";
    private static final Log log = LogFactory.getLog(FrameworkServiceComponent.class);

    private HttpService httpService;

    public static RealmService getRealmService() {
        return FrameworkServiceDataHolder.getInstance().getRealmService();
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRealmService(realmService);
    }

    public static RegistryService getRegistryService() {
        return FrameworkServiceDataHolder.getInstance().getRegistryService();
    }

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
        Servlet commonServlet = new ContextPathServletAdaptor(
                new CommonAuthenticationServlet(),
                COMMON_SERVLET_URL);

        Servlet commonInboundServlet = new ContextPathServletAdaptor(
                new CommonInboundAuthenticationServlet(),
                COMMON_INBOUND_SERVLET_URL);
        try {
            httpService.registerServlet(COMMON_SERVLET_URL, commonServlet,
                                        null, null);
            httpService.registerServlet(COMMON_INBOUND_SERVLET_URL, commonInboundServlet,
                    null, null);
        } catch (Exception e) {
            String errMsg = "Error when registering Common Servlet via the HttpService.";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        FrameworkServiceDataHolder.getInstance().setBundleContext(bundleContext);

        //this is done to load SessionDataStore class and start the cleanup tasks.
        SessionDataStore.getInstance();

        if (log.isDebugEnabled()) {
            log.info("Application Authentication Framework bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Application Authentication Framework bundle is deactivated");
        }

        FrameworkServiceDataHolder.getInstance().setBundleContext(null);
    }

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

    protected void setInboundRequestProcessor(InboundAuthenticationRequestProcessor requestProcessor) {

        FrameworkServiceDataHolder.getInstance().getInboundAuthenticationRequestProcessors().add(requestProcessor);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getInboundAuthenticationRequestProcessors(),
                inboundRequestProcessor);

        if (log.isDebugEnabled()) {
            log.debug("Added application inbound request processor : " + requestProcessor.getName());
        }
    }

    protected void unsetInboundRequestProcessor(InboundAuthenticationRequestProcessor requestProcessor) {

        FrameworkServiceDataHolder.getInstance().getInboundAuthenticationRequestProcessors().remove(requestProcessor);


        if (log.isDebugEnabled()) {
            log.debug("Removed application inbound request processor : " + requestProcessor.getName());
        }
    }

    protected void setInboundResponseProcessor(InboundAuthenticationResponseProcessor responseProcessor) {

        FrameworkServiceDataHolder.getInstance().getInboundAuthenticationResponseProcessors().add(responseProcessor);
        Collections
                .sort(FrameworkServiceDataHolder.getInstance().getInboundAuthenticationResponseProcessors(),
                        inboundResponseBuilder);

        if (log.isDebugEnabled()) {
            log.debug("Added application inbound response builder : " + responseProcessor.getName());
        }
    }

    protected void unsetInboundResponseProcessor(InboundAuthenticationResponseProcessor responseProcessor) {

        FrameworkServiceDataHolder.getInstance().getInboundAuthenticationResponseProcessors().remove(responseProcessor);

        if (log.isDebugEnabled()) {
            log.debug("Removed application inbound response builder : " + responseProcessor.getName());
        }

    }

    protected void setInboundRequestBuilder(InboundAuthenticationRequestBuilder requestBuilder) {

        FrameworkServiceDataHolder.getInstance().getInboundAuthenticationRequestBuilders().add(requestBuilder);
        Collections
                .sort(FrameworkServiceDataHolder.getInstance().getInboundAuthenticationRequestBuilders(), inboundRequestBuilder);

        if (log.isDebugEnabled()) {
            log.debug("Added application inbound request builder : " + requestBuilder.getName());
        }
    }

    protected void unsetInboundRequestBuilder(InboundAuthenticationRequestBuilder requestBuilder) {

        FrameworkServiceDataHolder.getInstance().getInboundAuthenticationRequestBuilders().remove(requestBuilder);

        if (log.isDebugEnabled()) {
            log.debug("Removed application inbound request builder : " + requestBuilder.getName());
        }

    }



    protected void unsetIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    protected void setIdentityCoreInitializedEventService(IdentityCoreInitializedEvent identityCoreInitializedEvent) {
        /* reference IdentityCoreInitializedEvent service to guarantee that this component will wait until identity core
         is started */
    }

    private static Comparator<InboundAuthenticationRequestProcessor> inboundRequestProcessor =
            new Comparator<InboundAuthenticationRequestProcessor>() {

                @Override
                public int compare(InboundAuthenticationRequestProcessor inboundRequestProcessor1,
                        InboundAuthenticationRequestProcessor inboundRequestProcessor2) {

                    if (inboundRequestProcessor1.getPriority() > inboundRequestProcessor2.getPriority()) {
                        return 1;
                    } else if (inboundRequestProcessor1.getPriority() < inboundRequestProcessor2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
    private static Comparator<InboundAuthenticationResponseProcessor> inboundResponseBuilder =
            new Comparator<InboundAuthenticationResponseProcessor>() {

                @Override
                public int compare(InboundAuthenticationResponseProcessor inboundResponseBuilder1,
                        InboundAuthenticationResponseProcessor inboundResponseBuilder2) {

                    if (inboundResponseBuilder1.getPriority() > inboundResponseBuilder2.getPriority()) {
                        return 1;
                    } else if (inboundResponseBuilder1.getPriority() < inboundResponseBuilder2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

    private static Comparator<InboundAuthenticationRequestBuilder> inboundRequestBuilder =
            new Comparator<InboundAuthenticationRequestBuilder>() {

                @Override
                public int compare(InboundAuthenticationRequestBuilder inboundRequestBuilder1,
                        InboundAuthenticationRequestBuilder inboundRequestBuilder2) {

                    if (inboundRequestBuilder1.getPriority() > inboundRequestBuilder2.getPriority()) {
                        return 1;
                    } else if (inboundRequestBuilder1.getPriority() < inboundRequestBuilder2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
}
