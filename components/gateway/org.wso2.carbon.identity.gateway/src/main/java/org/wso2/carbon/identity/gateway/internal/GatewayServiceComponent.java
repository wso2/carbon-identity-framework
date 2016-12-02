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
import org.wso2.carbon.identity.framework.IdentityProcessor;
import org.wso2.carbon.identity.framework.request.factory.HttpIdentityRequestFactory;
import org.wso2.carbon.identity.framework.response.factory.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.framework.util.IdentityGatewayUtil;
import org.wso2.carbon.identity.gateway.processor.InitRequestProcessor;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Comparator;

/**
 * @scr.component name="identity.gateway.component"
 * immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unSetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unSetRegistryService"
 */
public class GatewayServiceComponent {

    private static final Log log = LogFactory.getLog(GatewayServiceComponent.class);
    private GatewayDataHolder dataHolder = GatewayDataHolder.getInstance();
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

        // Register Initial Authentication Request Handler
        bundleContext.registerService(IdentityProcessor.class, new InitRequestProcessor(), null);
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

//    protected void setAuthenticator(ApplicationAuthenticator authenticator) {
//
//        if (authenticator instanceof LocalApplicationAuthenticator) {
//            dataHolder.getLocalApplicationAuthenticators().add((LocalApplicationAuthenticator) authenticator);
//        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
//            dataHolder.getFederatedApplicationAuthenticators().add((FederatedApplicationAuthenticator) authenticator);
//        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
//            dataHolder.getRequestPathApplicationAuthenticators().add((RequestPathApplicationAuthenticator) authenticator);
//        } else {
//            log.error("Unsupported Authenticator found : " + authenticator.getName());
//            return;
//        }
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added application authenticator : " + authenticator.getName());
//        }
//    }
//
//    protected void unSetAuthenticator(ApplicationAuthenticator authenticator) {
//
//        if (authenticator instanceof LocalApplicationAuthenticator) {
//            dataHolder.getLocalApplicationAuthenticators().remove(authenticator);
//        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
//            dataHolder.getFederatedApplicationAuthenticators().remove(authenticator);
//        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
//            dataHolder.getRequestPathApplicationAuthenticators().remove(authenticator);
//        }
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed application authenticator : " + authenticator.getName());
//        }
//
//    }
//
//    protected void addRequestHandler(AbstractRequestHandler abstractRequestHandler) {
//
//        dataHolder.getRequestHandlers().add(abstractRequestHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added AuthenticationHandler : " + abstractRequestHandler.getName());
//        }
//    }
//
//    protected void unSetRequestHandler(AbstractRequestHandler abstractRequestHandler) {
//
//        dataHolder.getRequestHandlers().remove(abstractRequestHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed AuthenticationHandler : " + abstractRequestHandler.getName());
//        }
//    }
//
//    protected void addAuthenticationHandler(AuthenticationHandler authenticationHandler) {
//
//        dataHolder.getAuthenticationHandlers().add(authenticationHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added AuthenticationHandler : " + authenticationHandler.getName());
//        }
//    }
//
//    protected void unSetAuthenticationHandler(AuthenticationHandler authenticationHandler) {
//
//        dataHolder.getAuthenticationHandlers().remove(authenticationHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed AuthenticationHandler : " + authenticationHandler.getName());
//        }
//    }
//
//    protected void addAuthorizationHandler(AbstractAuthorizationHandler authorizationHandler) {
//
//        dataHolder.getAuthorizationHandlers().add(authorizationHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added AuthorizationHandler : " + authorizationHandler.getName());
//        }
//    }
//
//    protected void unSetAuthorizationHandler(AbstractAuthorizationHandler authorizationHandler) {
//
//        dataHolder.getAuthorizationHandlers().remove(authorizationHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed AuthenticationHandler : " + authorizationHandler.getName());
//        }
//    }
//
//    protected void addJITHandler(JITHandler jitHandler) {
//
//        dataHolder.getJitHandlers().add(jitHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added JITHandler : " + jitHandler.getName());
//        }
//    }
//
//    protected void unSetJITHandler(JITHandler jitHandler) {
//
//        dataHolder.getJitHandlers().remove(jitHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed JITHandler : " + jitHandler.getName());
//        }
//    }
//
//    protected void addClaimHandler(ClaimHandler claimHandler) {
//
//        dataHolder.getClaimHandlers().add(claimHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added ClaimHandler : " + claimHandler.getName());
//        }
//    }
//
//    protected void unSetClaimHandler(ClaimHandler claimHandler) {
//
//        dataHolder.getClaimHandlers().remove(claimHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed ClaimHandler : " + claimHandler.getName());
//        }
//    }
//
//    protected void addResponseHandler(AbstractResponseHandler responseHandler) {
//
//        dataHolder.getResponseHandlers().add(responseHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added AbstractResponseHandler : " + responseHandler.getName());
//        }
//    }
//
//    protected void unSetResponseHandler(AbstractResponseHandler responseHandler) {
//
//        dataHolder.getResponseHandlers().remove(responseHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed AbstractResponseHandler : " + responseHandler.getName());
//        }
//    }
//
//    protected void addPreHandler(AbstractPreHandler preHandler) {
//
//        Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandlerMap =
//                dataHolder.getPreHandler();
//
//        List<AbstractPreHandler> abstractPreHandlers = preHandlerMap.get(preHandler.getExtensionHandlerPoints());
//        if (abstractPreHandlers == null) {
//            abstractPreHandlers = new ArrayList<>();
//            preHandlerMap.put(preHandler.getExtensionHandlerPoints(), abstractPreHandlers);
//        }
//        abstractPreHandlers.add(preHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added AbstractPreHandler : " + preHandler.getName());
//        }
//    }
//
//    protected void unSetPreHandler(AbstractPreHandler preHandler) {
//
//        Map<ExtensionHandlerPoints, List<AbstractPreHandler>> preHandlerMap =
//                dataHolder.getPreHandler();
//
//        List<AbstractPreHandler> abstractPreHandlers = preHandlerMap.get(preHandler.getExtensionHandlerPoints());
//        if (abstractPreHandlers != null) {
//            abstractPreHandlers.remove(preHandler);
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("Removed AbstractPreHandler : " + preHandler.getName());
//        }
//    }
//
//    protected void addPostHandler(AbstractPostHandler postHandler) {
//
//        Map<ExtensionHandlerPoints, List<AbstractPostHandler>> postHandlerMap =
//                dataHolder.getPostHandler();
//
//        List<AbstractPostHandler> abstractPostHandlers = postHandlerMap.get(postHandler.getExtensionHandlerPoints());
//        if (abstractPostHandlers == null) {
//            abstractPostHandlers = new ArrayList<>();
//            postHandlerMap.put(postHandler.getExtensionHandlerPoints(), abstractPostHandlers);
//        }
//        abstractPostHandlers.add(postHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added AbstractPostHandler : " + postHandler.getName());
//        }
//    }
//
//    protected void unSetPostHandler(AbstractPostHandler postHandler) {
//
//        Map<ExtensionHandlerPoints, List<AbstractPostHandler>> postHandlerMap =
//                dataHolder.getPostHandler();
//
//        List<AbstractPostHandler> abstractPostHandlers = postHandlerMap.get(postHandler.getExtensionHandlerPoints());
//        if (abstractPostHandlers != null) {
//            abstractPostHandlers.remove(postHandler);
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("Removed AbstractPostHandler : " + postHandler.getName());
//        }
//    }
//
//    protected void addSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {
//
//        dataHolder.getSequenceBuildFactories().add(sequenceBuildFactory);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
//        }
//    }
//
//    protected void unSetSequenceBuildFactory(AbstractSequenceBuildFactory sequenceBuildFactory) {
//
//        dataHolder.getSequenceBuildFactories().remove(sequenceBuildFactory);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed AbstractSequenceBuildFactory : " + sequenceBuildFactory.getName());
//        }
//    }
//
//
//    protected void addSequenceManager(SequenceManager sequenceManager) {
//
//        dataHolder.getSequenceManagers().add(sequenceManager);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added SequenceManager : " + sequenceManager.getName());
//        }
//    }
//
//    protected void unSetSequenceManager(SequenceManager sequenceManager) {
//
//        dataHolder.getSequenceManagers().remove(sequenceManager);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed SequenceManager : " + sequenceManager.getName());
//        }
//    }
//
//
//    protected void addRequestPathHandler(RequestPathHandler requestPathHandler) {
//
//        dataHolder.getRequestPathHandlers().add(requestPathHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added RequestPathHandler : " + requestPathHandler.getName());
//        }
//    }
//
//    protected void unSetRequestPathHandler(RequestPathHandler requestPathHandler) {
//
//        dataHolder.getRequestPathHandlers().remove(requestPathHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed RequestPathHandler : " + requestPathHandler.getName());
//        }
//    }
//
//
//    protected void addStepHandler(StepHandler stepHandler) {
//
//        dataHolder.getStepHandlers().add(stepHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Added StepHandler : " + stepHandler.getName());
//        }
//    }
//
//    protected void unSetStepHandler(StepHandler stepHandler) {
//
//        dataHolder.getStepHandlers().remove(stepHandler);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Removed StepHandler : " + stepHandler.getName());
//        }
//    }
}
