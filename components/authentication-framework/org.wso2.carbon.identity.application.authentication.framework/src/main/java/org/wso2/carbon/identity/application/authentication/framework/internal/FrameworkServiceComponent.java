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
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.application.authentication.framework.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.IdentityServlet;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator
        .FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator
        .LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.authenticator
        .RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authorization
        .AbstractAuthorizationHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.claim.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.extension
        .AbstractPostHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.extension
        .AbstractPreHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.extension.ExtensionHandlerPoints;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.jit.JITHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandler;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.authenticator.ApplicationAuthenticator"
 * cardinality="1..n" policy="dynamic" bind="setAuthenticator"
 * unbind="unsetAuthenticator"
 * interface="org.wso2.carbon.identity.application.authentication.framework.IdentityProcessor"
 * cardinality="0..n" policy="dynamic" bind="addIdentityProcessor"
 * unbind="removeIdentityProcessor"
 * @scr.reference name="identity.request.factory"
 * interface="org.wso2.carbon.identity.application.authentication.framework.HttpIdentityRequestFactory"
 * cardinality="0..n" policy="dynamic" bind="addHttpIdentityRequestFactory"
 * unbind="removeHttpIdentityRequestFactory"
 * @scr.reference name="identity.response.factory"
 * interface="org.wso2.carbon.identity.application.authentication.framework.HttpIdentityResponseFactory"
 * cardinality="0..n" policy="dynamic" bind="addHttpIdentityResponseFactory"
 * unbind="removeHttpIdentityResponseFactory"
 * @scr.reference name="identity.handlers.authentication"
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.AuthenticationHandler"
 * cardinality="0..n" policy="dynamic" bind="addAuthenticationHandler"
 * unbind="removeAuthenticationHandler"
 * @scr.reference name="identity.handlers.authorization"
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.handler.authorization.AbstractAuthorizationHandler"
 * cardinality="0..n" policy="dynamic" bind="addAuthorizationHandler"
 * unbind="removeAuthorizationHandler"
 * @scr.reference name="identity.handlers.jit"
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.handler.jit.JITHandler"
 * cardinality="0..n" policy="dynamic" bind="addJITHandler"
 * unbind="removeJITHandler"
 * @scr.reference name="identity.handlers.claim"
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.handler.claim.ClaimHandler"
 * cardinality="0..n" policy="dynamic" bind="addClaimHandler"
 * unbind="removeClaimHandler"
 * @scr.reference name="identity.handlers.response"
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.handler.response.AbstractResponseHandler"
 * cardinality="0..n" policy="dynamic" bind="addResponseHandler"
 * unbind="removeResponseHandler"
 * @scr.reference name="identity.handlers.extension.pre"
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.handler.extension.AbstractPreHandler"
 * cardinality="0..n" policy="dynamic" bind="addPreHandler"
 * unbind="removePreHandler"
 * @scr.reference name="identity.handlers.extension.post"
 * interface="org.wso2.carbon.identity.application.authentication.framework.processor.handler.extension.AbstractPostHandler"
 * cardinality="0..n" policy="dynamic" bind="addPostHandler"
 * unbind="removePostHandler"
 */



public class FrameworkServiceComponent {

    public static final String COMMON_SERVLET_URL = "/commonauth";
    private static final String IDENTITY_SERVLET_URL = "/identity";
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


        Servlet identityServlet = new ContextPathServletAdaptor(new IdentityServlet(),
                IDENTITY_SERVLET_URL);
        try {
            httpService.registerServlet(IDENTITY_SERVLET_URL, identityServlet, null, null);
        } catch (Exception e) {
            String errMsg = "Error when registering servlets via the HttpService.";
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        //Default HttpIdentityRequestFactory is registered.
        HttpIdentityRequestFactory httpIdentityRequestFactory = new HttpIdentityRequestFactory();
        addHttpIdentityRequestFactory(httpIdentityRequestFactory);

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


        if (authenticator instanceof LocalApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getLocalApplicationAuthenticators().add(authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getFederatedApplicationAuthenticators().add(authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            FrameworkServiceDataHolder.getInstance().getRequestPathApplicationAuthenticators().add(authenticator);
        }

        if (log.isDebugEnabled()) {
            log.debug("Added application authenticator : " + authenticator.getName());
        }
    }

    protected void unsetAuthenticator(ApplicationAuthenticator authenticator) {

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

    protected void addIdentityProcessor(IdentityProcessor requestProcessor) {

        FrameworkServiceDataHolder.getInstance().getIdentityProcessors().add(requestProcessor);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getIdentityProcessors(),
                identityProcessor);

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

    protected void addHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityRequestFactories(),
                httpIdentityRequestFactory);
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

    protected void addHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories().add(factory);
        Collections.sort(FrameworkServiceDataHolder.getInstance().getHttpIdentityResponseFactories(),
                         httpIdentityResponseFactory);
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



    protected void addAuthenticationHandler(AuthenticationHandler authenticationHandler) {

        FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers().add(authenticationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthenticationHandler : " + authenticationHandler.getName());
        }
    }

    protected void removeAuthenticationHandler(AuthenticationHandler authenticationHandler) {

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

    protected void removeAuthorizationHandler(AbstractAuthorizationHandler authorizationHandler) {

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

    protected void removeJITHandler(JITHandler jitHandler) {

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

    protected void removeClaimHandler(ClaimHandler claimHandler) {

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

    protected void removeResponseHandler(AbstractResponseHandler responseHandler) {

        FrameworkServiceDataHolder.getInstance().getResponseHandlers().remove(responseHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractResponseHandler : " + responseHandler.getName());
        }
    }


    protected void addPreHandler(AbstractPreHandler preHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandlerMap = FrameworkServiceDataHolder.getInstance().getPreHandler();

        List<AbstractPreHandler> abstractPreHandlers = preHandlerMap.get(preHandler.getExtensionHandlerPoints());
        if(abstractPreHandlers == null){
            abstractPreHandlers = new ArrayList<>();
            preHandlerMap.put(preHandler.getExtensionHandlerPoints(),abstractPreHandlers);
        }
        abstractPreHandlers.add(preHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractPreHandler : " + preHandler.getName());
        }
    }

    protected void removePreHandler(AbstractPreHandler preHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandlerMap = FrameworkServiceDataHolder.getInstance().getPreHandler();

        List<AbstractPreHandler> abstractPreHandlers = preHandlerMap.get(preHandler.getExtensionHandlerPoints());
        if(abstractPreHandlers != null) {
            abstractPreHandlers.remove(preHandler);
        }
        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractPreHandler : " + preHandler.getName());
        }
    }



    protected void addPostHandler(AbstractPostHandler postHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPostHandler>> postHandlerMap  = FrameworkServiceDataHolder.getInstance().getPostHandler();

        List<AbstractPostHandler> abstractPostHandlers = postHandlerMap.get(postHandler.getExtensionHandlerPoints());
        if(abstractPostHandlers == null){
            abstractPostHandlers = new ArrayList<>();
            postHandlerMap.put(postHandler.getExtensionHandlerPoints(),abstractPostHandlers);
        }
        abstractPostHandlers.add(postHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractPostHandler : " + postHandler.getName());
        }
    }

    protected void removePostHandler(AbstractPostHandler postHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPostHandler>> postHandlerMap  = FrameworkServiceDataHolder.getInstance().getPostHandler();

        List<AbstractPostHandler> abstractPostHandlers = postHandlerMap.get(postHandler.getExtensionHandlerPoints());
        if(abstractPostHandlers != null) {
            abstractPostHandlers.remove(postHandler);
        }
        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractPostHandler : " + postHandler.getName());
        }
    }



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
}
