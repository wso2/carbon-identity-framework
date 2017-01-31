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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.gateway.api.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.gateway.api.IdentityProcessor;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


@Component(
        name = "org.wso2.carbon.identity.gateway.internal.FrameworkServiceComponent",
        immediate = true
)
public class FrameworkServiceComponent {

    private Logger log = LoggerFactory.getLogger(FrameworkServiceComponent.class);


    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        //Registering processor
        //AuthenticationProcessor authenticationProcessor = new AuthenticationProcessor();
        //bundleContext.registerService(IdentityProcessor.class, authenticationProcessor, null);


        //Registering this for demo perposes only
        //bundleContext.registerService(AbstractSequenceBuildFactory.class, new DemoSequenceBuildFactory(), null);
        //bundleContext.registerService(AuthenticationHandler.class, new AuthenticationHandler(), null);
        //bundleContext.registerService(SequenceManager.class, new SequenceManager(), null);
        //bundleContext.registerService(RequestPathHandler.class, new RequestPathHandler(), null);
        //bundleContext.registerService(StepHandler.class, new StepHandler(), null);

        //bundleContext.registerService(HttpIdentityRequestFactory.class, new FrameworkLoginRequestFactory(), null);
        //bundleContext.registerService(HttpIdentityResponseFactory.class, new FrameworkLoginResponseFactory(), null);


        //FrameworkServiceDataHolder.getInstance().setBundleContext(bundleContext);


        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }
    }

    @Deactivate
    protected void stop() throws Exception {

    }





    @Reference(
            name = "application.authenticator",
            service = ApplicationAuthenticator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetAuthenticator"
    )
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

    @Reference(
            name = "identity.handlers.request",
            service = AbstractRequestHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetRequestHandler"
    )
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

    @Reference(
            name = "identity.handlers.authentication",
            service = AuthenticationHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetAuthenticationHandler"
    )
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


    @Reference(
            name = "identity.handlers.authorization",
            service = AbstractAuthorizationHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetAuthorizationHandler"
    )
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

    @Reference(
            name = "identity.handlers.jit",
            service = JITHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetJITHandler"
    )
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

    @Reference(
            name = "identity.handlers.claim",
            service = ClaimHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetClaimHandler"
    )
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


    @Reference(
            name = "identity.handlers.response",
            service = AbstractResponseHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetResponseHandler"
    )
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

    @Reference(
            name = "identity.handlers.extension.pre",
            service = AbstractPreHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetPreHandler"
    )
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


    @Reference(
            name = "identity.handlers.extension.post",
            service = AbstractPostHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetPostHandler"
    )
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

    @Reference(
            name = "identity.handlers.sequence.factory",
            service = AbstractSequenceBuildFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetSequenceBuildFactory"
    )
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


    @Reference(
            name = "identity.handlers.sequence.manager",
            service = SequenceManager.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetSequenceManager"
    )
    protected void addSequenceManager(SequenceManager sequenceManager) {

        FrameworkServiceDataHolder.getInstance().getSequenceManagers().add(sequenceManager);

        if (log.isDebugEnabled()) {
            log.debug("Added SequenceManager : " + sequenceManager.getName());
        }
    }

    protected void unSetSequenceManager(SequenceManager sequenceManager) {

        FrameworkServiceDataHolder.getInstance().getSequenceManagers().remove(sequenceManager);

        if (log.isDebugEnabled()) {
            log.debug("Removed SequenceManager : " + sequenceManager.getName());
        }
    }

    @Reference(
            name = "identity.handlers.requestpath",
            service = RequestPathHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetRequestPathHandler"
    )
    protected void addRequestPathHandler(RequestPathHandler requestPathHandler) {

        FrameworkServiceDataHolder.getInstance().getRequestPathHandlers().add(requestPathHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added RequestPathHandler : " + requestPathHandler.getName());
        }
    }

    protected void unSetRequestPathHandler(RequestPathHandler requestPathHandler) {

        FrameworkServiceDataHolder.getInstance().getRequestPathHandlers().remove(requestPathHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed RequestPathHandler : " + requestPathHandler.getName());
        }
    }

    @Reference(
            name = "identity.handlers.step",
            service = StepHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetStepHandler"
    )
    protected void addStepHandler(StepHandler stepHandler) {

        FrameworkServiceDataHolder.getInstance().getStepHandlers().add(stepHandler);

        if (log.isDebugEnabled()) {
            log.debug("Added StepHandler : " + stepHandler.getName());
        }
    }

    protected void unSetStepHandler(StepHandler stepHandler) {

        FrameworkServiceDataHolder.getInstance().getStepHandlers().remove(stepHandler);

        if (log.isDebugEnabled()) {
            log.debug("Removed StepHandler : " + stepHandler.getName());
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
