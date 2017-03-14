/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.identity.common.base.handler.HandlerComparator;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;

import java.util.Collections;

/**
 * GatewayResourceComponent is the activator class for the resource bundle.
 */
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
            service = GatewayProcessor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetIdentityProcessor"
    )
    protected void addIdentityProcessor(GatewayProcessor requestProcessor) {

        GatewayResourceDataHolder.getInstance().getGatewayProcessors().add(requestProcessor);
        Collections.sort(GatewayResourceDataHolder.getInstance().getGatewayProcessors(),
                new HandlerComparator());
    }

    protected void unSetIdentityProcessor(GatewayProcessor requestProcessor) {

        GatewayResourceDataHolder.getInstance().getGatewayProcessors().remove(requestProcessor);
    }

    @Reference(
            name = "identity.application.request.factory",
            service = GatewayRequestBuilderFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetHttpIdentityRequestFactory"
    )
    protected void addHttpIdentityRequestFactory(GatewayRequestBuilderFactory factory) {

        GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories().add(factory);
        Collections.sort(GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories(),
                new HandlerComparator());
        if (log.isDebugEnabled()) {
            log.debug("Added GatewayRequestBuilderFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityRequestFactory(GatewayRequestBuilderFactory factory) {
        GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed GatewayRequestBuilderFactory : " + factory.getName());
        }
    }

    @Reference(
            name = "identity.application.response.factory",
            service = GatewayResponseBuilderFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unSetHttpIdentityResponseFactory"
    )
    protected void addHttpIdentityResponseFactory(GatewayResponseBuilderFactory factory) {

        GatewayResourceDataHolder.getInstance().getHttpIdentityResponseFactories().add(factory);
        Collections.sort(GatewayResourceDataHolder.getInstance().getHttpIdentityResponseFactories(),
                new HandlerComparator());
        if (log.isDebugEnabled()) {
            log.debug("Added GatewayResponseBuilderFactory : " + factory.getName());
        }
    }

    protected void unSetHttpIdentityResponseFactory(GatewayResponseBuilderFactory factory) {

        GatewayResourceDataHolder.getInstance().getHttpIdentityResponseFactories().remove(factory);
        if (log.isDebugEnabled()) {
            log.debug("Removed GatewayResponseBuilderFactory : " + factory.getName());
        }
    }
}
