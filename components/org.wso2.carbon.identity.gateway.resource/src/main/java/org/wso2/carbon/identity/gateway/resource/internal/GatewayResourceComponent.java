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

package org.wso2.carbon.identity.gateway.resource.internal;

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
import org.wso2.carbon.identity.gateway.api.internal.FrameworkServiceDataHolder;
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
        name = "org.wso2.carbon.identity.gateway.internal.GatewayResourceComponent",
        immediate = true
)
public class GatewayResourceComponent {

    private Logger log = LoggerFactory.getLogger(GatewayResourceComponent.class);


    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Application Authentication Framework bundle is activated");
        }
    }

    @Deactivate
    protected void stop() throws Exception {

    }



    @Reference(
            name = "identity.processor",
            service = IdentityProcessor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetIdentityProcessor"
    )
    protected void addIdentityProcessor(IdentityProcessor requestProcessor) {

        GatewayResourceDataHolder.getInstance().getIdentityProcessors().add(requestProcessor);
        Collections.sort(GatewayResourceDataHolder.getInstance().getIdentityProcessors(),
                         identityProcessor);

    }

    protected void unSetIdentityProcessor(IdentityProcessor requestProcessor) {

        GatewayResourceDataHolder.getInstance().getIdentityProcessors().remove(requestProcessor);

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
            GatewayResourceDataHolder.getInstance().getLocalApplicationAuthenticators()
                    .add((LocalApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            GatewayResourceDataHolder.getInstance().getFederatedApplicationAuthenticators()
                    .add((FederatedApplicationAuthenticator) authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            GatewayResourceDataHolder.getInstance().getRequestPathApplicationAuthenticators()
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
            GatewayResourceDataHolder.getInstance().getLocalApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof FederatedApplicationAuthenticator) {
            GatewayResourceDataHolder.getInstance().getFederatedApplicationAuthenticators().remove(authenticator);
        } else if (authenticator instanceof RequestPathApplicationAuthenticator) {
            GatewayResourceDataHolder.getInstance().getRequestPathApplicationAuthenticators().remove(authenticator);
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed application authenticator : " + authenticator.getName());
        }

    }

    @Reference(
            name = "identity.application.request.factory",
            service = HttpIdentityRequestFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetHttpIdentityRequestFactory"
    )
    protected void addHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories().add(factory);
        Collections.sort(GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories(),
                         httpIdentityRequestFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityRequestFactory(HttpIdentityRequestFactory factory) {

        GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityRequestFactory : " + factory.getName());
        }
    }

    @Reference(
            name = "identity.application.response.factory",
            service = HttpIdentityResponseFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetHttpIdentityResponseFactory"
    )
    protected void addHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        GatewayResourceDataHolder.getInstance().getHttpIdentityResponseFactories().add(factory);
        Collections.sort(GatewayResourceDataHolder.getInstance().getHttpIdentityResponseFactories(),
                         httpIdentityResponseFactory);
        if (log.isDebugEnabled()) {
            log.debug("Added HttpIdentityResponseFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityResponseFactory(HttpIdentityResponseFactory factory) {

        GatewayResourceDataHolder.getInstance().getHttpIdentityResponseFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed HttpIdentityResponseFactory : " + factory.getName());
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
