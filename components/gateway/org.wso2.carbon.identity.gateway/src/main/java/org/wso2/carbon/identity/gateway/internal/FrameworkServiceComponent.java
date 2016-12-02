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

package org.wso2.carbon.identity.gateway.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.gateway.demo.DemoSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.framework.IdentityProcessor;
import org.wso2.carbon.identity.gateway.framework.request.factory.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.framework.response.factory.FrameworkLoginResponseFactory;
import org.wso2.carbon.identity.gateway.framework.response.factory.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.gateway.framework.util.IdentityGatewayUtil;
import org.wso2.carbon.identity.gateway.processor.AuthenticationProcessor;
import org.wso2.carbon.identity.gateway.processor.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.RequestPathApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.RequestPathHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.StepHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authorization.AbstractAuthorizationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.claim.ClaimHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPostHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPreHandler;
import org.wso2.carbon.identity.gateway.processor.handler.extension.ExtensionHandlerPoints;
import org.wso2.carbon.identity.gateway.processor.handler.jit.JITHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.processor.request.factory.FrameworkLoginRequestFactory;
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
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unSetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unSetRegistryService"
 * @scr.reference name="application.authenticator"
 * interface="org.wso2.carbon.identity.gateway.processor.authenticator.ApplicationAuthenticator"
 * cardinality="0..n" policy="dynamic" bind="setAuthenticator"
 * unbind="unSetAuthenticator"
 * @scr.reference name="identity.handlers.request"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestHandler"
 * cardinality="0..n" policy="dynamic" bind="addRequestHandler"
 * unbind="unSetRequestHandler"
 * @scr.reference name="identity.handlers.authentication"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler"
 * cardinality="0..n" policy="dynamic" bind="addAuthenticationHandler"
 * unbind="unSetAuthenticationHandler"
 * @scr.reference name="identity.handlers.authorization"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.authorization.AbstractAuthorizationHandler"
 * cardinality="0..n" policy="dynamic" bind="addAuthorizationHandler"
 * unbind="unSetAuthorizationHandler"
 * @scr.reference name="identity.handlers.jit"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.jit.JITHandler"
 * cardinality="0..n" policy="dynamic" bind="addJITHandler"
 * unbind="unSetJITHandler"
 * @scr.reference name="identity.handlers.claim"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.claim.ClaimHandler"
 * cardinality="0..n" policy="dynamic" bind="addClaimHandler"
 * unbind="unSetClaimHandler"
 * @scr.reference name="identity.handlers.response"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler"
 * cardinality="0..n" policy="dynamic" bind="addResponseHandler"
 * unbind="unSetResponseHandler"
 * @scr.reference name="identity.handlers.extension.pre"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPreHandler"
 * cardinality="0..n" policy="dynamic" bind="addPreHandler"
 * unbind="unSetPreHandler"
 * @scr.reference name="identity.handlers.extension.post"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.extension.AbstractPostHandler"
 * cardinality="0..n" policy="dynamic" bind="addPostHandler"
 * unbind="unSetPostHandler"
 * @scr.reference name="identity.handlers.sequence.factory"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory"
 * cardinality="0..n" policy="dynamic" bind="addSequenceBuildFactory"
 * unbind="unSetSequenceBuildFactory"
 * @scr.reference name="identity.handlers.sequence.manager"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager"
 * cardinality="0..n" policy="dynamic" bind="addSequenceManager"
 * unbind="unSetSequenceManager"
 * @scr.reference name="identity.handlers.requestpath"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.RequestPathHandler"
 * cardinality="0..n" policy="dynamic" bind="addRequestPathHandler"
 * unbind="unSetRequestPathHandler"
 * @scr.reference name="identity.handlers.step"
 * interface="org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.StepHandler"
 * cardinality="0..n" policy="dynamic" bind="addStepHandler"
 * unbind="unSetStepHandler"
 */
public class FrameworkServiceComponent {

    private static final Log log = LogFactory.getLog(FrameworkServiceComponent.class);
    private FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
    private static Comparator<IdentityProcessor> identityProcessor =
            new Comparator<IdentityProcessor>() {

                @Override
                public int compare(IdentityProcessor identityProcessor1,
                                   IdentityProcessor identityProcessor2) {
                    return IdentityGatewayUtil.comparePriority(identityProcessor1.getPriority(), identityProcessor2.getPriority());
                }
            };
    private static Comparator<HttpIdentityRequestFactory> httpIdentityRequestFactory =
            new Comparator<HttpIdentityRequestFactory>() {

                @Override
                public int compare(HttpIdentityRequestFactory factory1,
                                   HttpIdentityRequestFactory factory2) {
                    return IdentityGatewayUtil.comparePriority(factory1.getPriority(), factory2.getPriority());
                }
            };
    private static Comparator<HttpIdentityResponseFactory> httpIdentityResponseFactory =
            new Comparator<HttpIdentityResponseFactory>() {

                @Override
                public int compare(HttpIdentityResponseFactory factory1,
                                   HttpIdentityResponseFactory factory2) {
                    return IdentityGatewayUtil.comparePriority(factory1.getPriority(), factory2.getPriority());
                }
            };


    protected void activate(ComponentContext ctxt) {
        BundleContext bundleContext = ctxt.getBundleContext();

        //Registering processor
        AuthenticationProcessor authenticationProcessor = new AuthenticationProcessor();
        bundleContext.registerService(IdentityProcessor.class, authenticationProcessor, null);


        //Registering this for demo perposes only
        bundleContext.registerService(AbstractSequenceBuildFactory.class, new DemoSequenceBuildFactory(), null);
        bundleContext.registerService(AuthenticationHandler.class, new AuthenticationHandler(), null);
        bundleContext.registerService(SequenceManager.class, new SequenceManager(), null);
        bundleContext.registerService(RequestPathHandler.class, new RequestPathHandler(), null);
        bundleContext.registerService(StepHandler.class, new StepHandler(), null);

        bundleContext.registerService(HttpIdentityRequestFactory.class, new FrameworkLoginRequestFactory(), null);
        bundleContext.registerService(HttpIdentityResponseFactory.class, new FrameworkLoginResponseFactory(), null);


        dataHolder.setBundleContext(bundleContext);


        if (log.isDebugEnabled()) {
            log.debug("Identity Gateway bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Gateway bundle is deactivated");
        }

        dataHolder.setBundleContext(null);
    }


    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the Identity Gateway bundle");
        }
        dataHolder.setRealmService(realmService);
    }

    protected void unSetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unSet in the Identity Gateway bundle");
        }
        dataHolder.setRealmService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is set in the Identity Gateway bundle");
        }
        dataHolder.setRegistryService(registryService);
    }

    protected void unSetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is unSet in the Identity Gateway bundle");
        }
        dataHolder.setRegistryService(null);
    }

    protected void setAuthenticator(ApplicationAuthenticator authenticator) {

        if (authenticator instanceof LocalApplicationAuthenticator) {
            dataHolder.getLocalApplicationAuthenticators().add((LocalApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            dataHolder.getFederatedApplicationAuthenticators().add((FederatedApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            dataHolder.getRequestPathApplicationAuthenticators().add((RequestPathApplicationAuthenticator) authenticator);
        } else {
            log.error("Unsupported Authenticator found : " + authenticator.getName());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Added application authenticator : " + authenticator.getName());
        }
    }

    protected void unSetAuthenticator(ApplicationAuthenticator authenticator) {

        if (authenticator instanceof LocalApplicationAuthenticator) {
            dataHolder.getLocalApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            dataHolder.getFederatedApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            dataHolder.getRequestPathApplicationAuthenticators().remove(authenticator);
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed application authenticator : " + authenticator.getName());
        }

    }


    protected void addHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        dataHolder.getHttpIdentityRequestFactories().add(factory);
        Collections.sort(dataHolder.getHttpIdentityRequestFactories(),
                httpIdentityRequestFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        dataHolder.getHttpIdentityRequestFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void addHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        dataHolder.getHttpIdentityResponseFactories().add(factory);
        Collections.sort(dataHolder.getHttpIdentityResponseFactories(),
                httpIdentityResponseFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        dataHolder.getHttpIdentityResponseFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void addRequestHandler(AbstractRequestHandler abstractRequestHandler) {

        dataHolder.getRequestHandlers().add(abstractRequestHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthenticationHandler : " + abstractRequestHandler.getName());
        }
    }

    protected void unSetRequestHandler(AbstractRequestHandler abstractRequestHandler) {

        dataHolder.getRequestHandlers().remove(abstractRequestHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + abstractRequestHandler.getName());
        }
    }

    protected void addAuthenticationHandler(AuthenticationHandler authenticationHandler) {

        dataHolder.getAuthenticationHandlers().add(authenticationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthenticationHandler : " + authenticationHandler.getName());
        }
    }

    protected void unSetAuthenticationHandler(AuthenticationHandler authenticationHandler) {

        dataHolder.getAuthenticationHandlers().remove(authenticationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + authenticationHandler.getName());
        }
    }

    protected void addAuthorizationHandler(AbstractAuthorizationHandler authorizationHandler) {

        dataHolder.getAuthorizationHandlers().add(authorizationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AuthorizationHandler : " + authorizationHandler.getName());
        }
    }

    protected void unSetAuthorizationHandler(AbstractAuthorizationHandler authorizationHandler) {

        dataHolder.getAuthorizationHandlers().remove(authorizationHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AuthenticationHandler : " + authorizationHandler.getName());
        }
    }

    protected void addJITHandler(JITHandler jitHandler) {

        dataHolder.getJitHandlers().add(jitHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added JITHandler : " + jitHandler.getName());
        }
    }

    protected void unSetJITHandler(JITHandler jitHandler) {

        dataHolder.getJitHandlers().remove(jitHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed JITHandler : " + jitHandler.getName());
        }
    }

    protected void addClaimHandler(ClaimHandler claimHandler) {

        dataHolder.getClaimHandlers().add(claimHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added ClaimHandler : " + claimHandler.getName());
        }
    }

    protected void unSetClaimHandler(ClaimHandler claimHandler) {

        dataHolder.getClaimHandlers().remove(claimHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed ClaimHandler : " + claimHandler.getName());
        }
    }

    protected void addResponseHandler(AbstractResponseHandler responseHandler) {

        dataHolder.getResponseHandlers().add(responseHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractResponseHandler : " + responseHandler.getName());
        }
    }

    protected void unSetResponseHandler(AbstractResponseHandler responseHandler) {

        dataHolder.getResponseHandlers().remove(responseHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractResponseHandler : " + responseHandler.getName());
        }
    }

    protected void addPreHandler(AbstractPreHandler preHandler) {

        Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandlerMap =
                dataHolder.getPreHandler();

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
                dataHolder.getPreHandler();

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
                dataHolder.getPostHandler();

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
                dataHolder.getPostHandler();

        List<AbstractPostHandler> abstractPostHandlers = postHandlerMap.get(postHandler.getExtensionHandlerPoints());
        if (abstractPostHandlers != null) {
            abstractPostHandlers.remove(postHandler);
        }
        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractPostHandler : " + postHandler.getName());
        }
    }

    protected void addSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {

        dataHolder.getSequenceBuildFactories().add(sequenceBuildFactory);

        if (log.isDebugEnabled()) {
            log.debug("Added AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
        }
    }

    protected void unSetSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {

        dataHolder.getSequenceBuildFactories().remove(sequenceBuildFactory);

        if (log.isDebugEnabled()) {
            log.debug("Removed AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
        }
    }


    protected void addSequenceManager(SequenceManager sequenceManager) {

        dataHolder.getSequenceManagers().add(sequenceManager);

        if (log.isDebugEnabled()) {
            log.debug("Added SequenceManager : " + sequenceManager.getName());
        }
    }

    protected void unSetSequenceManager(SequenceManager sequenceManager) {

        dataHolder.getSequenceManagers().remove(sequenceManager);

        if (log.isDebugEnabled()) {
            log.debug("Removed SequenceManager : " + sequenceManager.getName());
        }
    }


    protected void addRequestPathHandler(RequestPathHandler requestPathHandler) {

        dataHolder.getRequestPathHandlers().add(requestPathHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added RequestPathHandler : " + requestPathHandler.getName());
        }
    }

    protected void unSetRequestPathHandler(RequestPathHandler requestPathHandler) {

        dataHolder.getRequestPathHandlers().remove(requestPathHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed RequestPathHandler : " + requestPathHandler.getName());
        }
    }


    protected void addStepHandler(StepHandler stepHandler) {

        dataHolder.getStepHandlers().add(stepHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added StepHandler : " + stepHandler.getName());
        }
    }

    protected void unSetStepHandler(StepHandler stepHandler) {

        dataHolder.getStepHandlers().remove(stepHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed StepHandler : " + stepHandler.getName());
        }
    }
}
