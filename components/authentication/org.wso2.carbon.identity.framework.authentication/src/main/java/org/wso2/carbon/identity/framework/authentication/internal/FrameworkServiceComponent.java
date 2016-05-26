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

package org.wso2.carbon.identity.framework.authentication.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.identity.framework.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.framework.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.framework.IdentityProcessor;
import org.wso2.carbon.identity.framework.authentication.demo.DemoSequenceBuildFactory;
import org.wso2.carbon.identity.framework.authentication.processor.AuthenticationProcessor;
import org.wso2.carbon.identity.framework.authentication.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.framework.authentication.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.framework.authentication.processor.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.framework.authentication.processor.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl
        .AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authorization.AbstractAuthorizationHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.claim.ClaimHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.extension.AbstractPostHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.extension.AbstractPreHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.extension.ExtensionHandlerPoints;
import org.wso2.carbon.identity.framework.authentication.processor.handler.jit.JITHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.framework.authentication.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @scr.component name="identity.framework.authentication.component"
 * immediate="true"
 * @scr.reference name="osgi.httpservice"
 * interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService"
 * unbind="unSetHttpService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unSetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unSetRegistryService"
 * @scr.reference name="application.authenticator"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.authenticator.ApplicationAuthenticator"
 * cardinality="1..n" policy="dynamic" bind="setAuthenticator"
 * unbind="unSetAuthenticator"
 * @scr.reference name="identity.handlers.request"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.request.AbstractRequestHandler"
 * cardinality="0..n" policy="dynamic" bind="addRequestHandler"
 * unbind="unSetRequestHandler"
 * @scr.reference name="identity.handlers.authentication"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.AuthenticationHandler"
 * cardinality="0..n" policy="dynamic" bind="addAuthenticationHandler"
 * unbind="unSetAuthenticationHandler"
 * @scr.reference name="identity.handlers.authorization"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.authorization.AbstractAuthorizationHandler"
 * cardinality="0..n" policy="dynamic" bind="addAuthorizationHandler"
 * unbind="unSetAuthorizationHandler"
 * @scr.reference name="identity.handlers.jit"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.jit.JITHandler"
 * cardinality="0..n" policy="dynamic" bind="addJITHandler"
 * unbind="unSetJITHandler"
 * @scr.reference name="identity.handlers.claim"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.claim.ClaimHandler"
 * cardinality="0..n" policy="dynamic" bind="addClaimHandler"
 * unbind="unSetClaimHandler"
 * @scr.reference name="identity.handlers.response"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.response.AbstractResponseHandler"
 * cardinality="0..n" policy="dynamic" bind="addResponseHandler"
 * unbind="unSetResponseHandler"
 * @scr.reference name="identity.handlers.extension.pre"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.extension.AbstractPreHandler"
 * cardinality="0..n" policy="dynamic" bind="addPreHandler"
 * unbind="unSetPreHandler"
 * @scr.reference name="identity.handlers.extension.post"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.extension.AbstractPostHandler"
 * cardinality="0..n" policy="dynamic" bind="addPostHandler"
 * unbind="unSetPostHandler"
 * @scr.reference name="identity.handlers.sequence.factory"
 * interface="org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.AbstractSequenceBuildFactory"
 * cardinality="0..n" policy="dynamic" bind="addSequenceBuildFactory"
 * unbind="unSetSequenceBuildFactory"
 */


public class FrameworkServiceComponent {

    public static final String COMMON_SERVLET_URL = "/commonauth";
    private static final String IDENTITY_SERVLET_URL = "/identitynew";
    private static final Log log = LogFactory.getLog(FrameworkServiceComponent.class);
    private static Comparator<IdentityProcessor> identityProcessor =
            new Comparator<IdentityProcessor>() {

                @Override
                public int compare(IdentityProcessor identityProcessor1,
                                   IdentityProcessor identityProcessor2) {

                    if (identityProcessor1.getPriority() > identityProcessor2.getPriority()) {
                        return 1;
                    } else if (identityProcessor1.getPriority() < identityProcessor2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
    private static Comparator<HttpIdentityRequestFactory> httpIdentityRequestFactory =
            new Comparator<HttpIdentityRequestFactory>() {

                @Override
                public int compare(HttpIdentityRequestFactory factory1,
                                   HttpIdentityRequestFactory factory2) {

                    if (factory1.getPriority() > factory2.getPriority()) {
                        return 1;
                    } else if (factory1.getPriority() < factory2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
    private static Comparator<HttpIdentityResponseFactory> httpIdentityResponseFactory =
            new Comparator<HttpIdentityResponseFactory>() {

                @Override
                public int compare(HttpIdentityResponseFactory factory1,
                                   HttpIdentityResponseFactory factory2) {

                    if (factory1.getPriority() > factory2.getPriority()) {
                        return 1;
                    } else if (factory1.getPriority() < factory2.getPriority()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
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

    public static BundleContext getBundleContext() {
        BundleContext bundleContext = FrameworkServiceDataHolder.getInstance().getBundleContext();
        if (bundleContext == null) {
            String msg = "System has not been started properly. Bundle Context is null.";
            log.error(msg);
        }

        return bundleContext;
    }

    @SuppressWarnings("unchecked")
    protected void activate(ComponentContext ctxt) {
        BundleContext bundleContext = ctxt.getBundleContext();

        //Registering processor
        AuthenticationProcessor authenticationProcessor = new AuthenticationProcessor();
        bundleContext.registerService(IdentityProcessor.class, authenticationProcessor, null);


        //Registering this for demo perposes only
        DemoSequenceBuildFactory demoSequenceBuildFactory = new DemoSequenceBuildFactory();
        bundleContext.registerService(DemoSequenceBuildFactory.class, demoSequenceBuildFactory, null);

        FrameworkServiceDataHolder.getInstance().setBundleContext(bundleContext);


        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is deactivated");
        }

        FrameworkServiceDataHolder.getInstance().setBundleContext(null);
    }

    protected void setHttpService(HttpService httpService) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is set in the Application Authentication Framework bundle");
        }

        this.httpService = httpService;
    }

    protected void unSetHttpService(HttpService httpService) {
        if (log.isDebugEnabled()) {
            log.debug("HTTP Service is unSet in the Application Authentication Framework bundle");
        }

        this.httpService = null;
    }

    protected void unSetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unSet in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRealmService(null);
    }

    protected void unSetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is unSet in the Application Authentication Framework bundle");
        }
        FrameworkServiceDataHolder.getInstance().setRegistryService(null);
    }

    protected void setAuthenticator(ApplicationAuthenticator authenticator) {

        if (authenticator instanceof LocalApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators()
                    .add((LocalApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators()
                    .add((FederatedApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators()
                    .add((RequestPathApplicationAuthenticator) authenticator);
        } else {
            log.error("Unsupported Authenticator found : " + authenticator.getName());
        }

        if (log.isDebugEnabled()) {
            log.debug("Added application authenticator : " + authenticator.getName());
        }
    }

    protected void unSetAuthenticator(ApplicationAuthenticator authenticator) {

        if (authenticator instanceof LocalApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators().remove(authenticator);
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed application authenticator : " + authenticator.getName());
        }

    }


    protected void addHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories(),
                         httpIdentityRequestFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void addHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories(),
                         httpIdentityResponseFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void addRequestHandler(AbstractRequestHandler abstractRequestHandler) {

        FrameworkServiceDataHolder.getInstance().getRequestHandlers().add(abstractRequestHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthenticationHandler : " + abstractRequestHandler.getName());
        }
    }

    protected void unSetRequestHandler(AbstractRequestHandler abstractRequestHandler) {

        FrameworkServiceDataHolder.getInstance().getRequestHandlers().remove(abstractRequestHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + abstractRequestHandler.getName());
        }
    }

    protected void addAuthenticationHandler(AuthenticationHandler authenticationHandler) {

        FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers().add(authenticationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthenticationHandler : " + authenticationHandler.getName());
        }
    }

    protected void unSetAuthenticationHandler(AuthenticationHandler authenticationHandler) {

        FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers().remove(authenticationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + authenticationHandler.getName());
        }
    }

    protected void addAuthorizationHandler(AbstractAuthorizationHandler authorizationHandler) {

        FrameworkServiceDataHolder.getInstance().getAuthorizationHandlers().add(authorizationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthorizationHandler : " + authorizationHandler.getName());
        }
    }

    protected void unSetAuthorizationHandler(AbstractAuthorizationHandler authorizationHandler) {

        FrameworkServiceDataHolder.getInstance().getAuthorizationHandlers().remove(authorizationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + authorizationHandler.getName());
        }
    }

    protected void addJITHandler(JITHandler jitHandler) {

        FrameworkServiceDataHolder.getInstance().getJitHandlers().add(jitHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added JITHandler : " + jitHandler.getName());
        }
    }

    protected void unSetJITHandler(JITHandler jitHandler) {

        FrameworkServiceDataHolder.getInstance().getJitHandlers().remove(jitHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed JITHandler : " + jitHandler.getName());
        }
    }

    protected void addClaimHandler(ClaimHandler claimHandler) {

        FrameworkServiceDataHolder.getInstance().getClaimHandlers().add(claimHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added ClaimHandler : " + claimHandler.getName());
        }
    }

    protected void unSetClaimHandler(ClaimHandler claimHandler) {

        FrameworkServiceDataHolder.getInstance().getClaimHandlers().remove(claimHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed ClaimHandler : " + claimHandler.getName());
        }
    }

    protected void addResponseHandler(AbstractResponseHandler responseHandler) {

        FrameworkServiceDataHolder.getInstance().getResponseHandlers().add(responseHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractResponseHandler : " + responseHandler.getName());
        }
    }

    protected void unSetResponseHandler(AbstractResponseHandler responseHandler) {

        FrameworkServiceDataHolder.getInstance().getResponseHandlers().remove(responseHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractResponseHandler : " + responseHandler.getName());
        }
    }

    protected void addPreHandler(AbstractPreHandler preHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandlerMap =
                FrameworkServiceDataHolder.getInstance().getPreHandler();

        List<AbstractPreHandler> abstractPreHandlers = preHandlerMap.get(preHandler.getExtensionHandlerPoints());
        if (abstractPreHandlers == null) {
            abstractPreHandlers = new ArrayList<>();
            preHandlerMap.put(preHandler.getExtensionHandlerPoints(), abstractPreHandlers);
        }
        abstractPreHandlers.add(preHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractPreHandler : " + preHandler.getName());
        }
    }

    protected void unSetPreHandler(AbstractPreHandler preHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandlerMap =
                FrameworkServiceDataHolder.getInstance().getPreHandler();

        List<AbstractPreHandler> abstractPreHandlers = preHandlerMap.get(preHandler.getExtensionHandlerPoints());
        if (abstractPreHandlers != null) {
            abstractPreHandlers.remove(preHandler);
        }
        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractPreHandler : " + preHandler.getName());
        }
    }

    protected void addPostHandler(AbstractPostHandler postHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPostHandler>> postHandlerMap =
                FrameworkServiceDataHolder.getInstance().getPostHandler();

        List<AbstractPostHandler> abstractPostHandlers = postHandlerMap.get(postHandler.getExtensionHandlerPoints());
        if (abstractPostHandlers == null) {
            abstractPostHandlers = new ArrayList<>();
            postHandlerMap.put(postHandler.getExtensionHandlerPoints(), abstractPostHandlers);
        }
        abstractPostHandlers.add(postHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractPostHandler : " + postHandler.getName());
        }
    }

    protected void unSetPostHandler(AbstractPostHandler postHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPostHandler>> postHandlerMap =
                FrameworkServiceDataHolder.getInstance().getPostHandler();

        List<AbstractPostHandler> abstractPostHandlers = postHandlerMap.get(postHandler.getExtensionHandlerPoints());
        if (abstractPostHandlers != null) {
            abstractPostHandlers.remove(postHandler);
        }
        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractPostHandler : " + postHandler.getName());
        }
    }

    protected void addSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {

        FrameworkServiceDataHolder.getInstance().getSequenceBuildFactories().add(sequenceBuildFactory);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
        }
    }

    protected void unSetSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {

        FrameworkServiceDataHolder.getInstance().getSequenceBuildFactories().remove(sequenceBuildFactory);

        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
        }
    }
}
