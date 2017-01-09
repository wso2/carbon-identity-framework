/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.artifact.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.artifact.ArtifactStore;
import org.wso2.carbon.identity.gateway.artifact.model.HandlerConfig;
import org.wso2.carbon.identity.gateway.artifact.model.ServiceProvider;
import org.wso2.carbon.identity.gateway.artifact.model.ServiceProviderEntry;
import org.wso2.carbon.identity.gateway.element.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.internal.DataHolder;


public class ServiceProviderManager {

    public static AbstractGatewayHandler getInitialHandler(ServiceProvider serviceProvider){
        HandlerConfig handler = serviceProvider.getServiceProvider().getHandler();
        AbstractGatewayHandler handler1 = DataHolder.getInstance().getHandler(handler.getName());
        handler1.setHandlerConfig(handler);
        return handler1 ;
    }
/*
    public static void buildServiceProviderChain(ServiceProvider serviceProvider){
        ServiceProviderEntry serviceProviderEntry = serviceProvider.getServiceProvider();
        HandlerConfig handler = serviceProviderEntry.getHandler() ;
        if(handler != null && StringUtils.isNotBlank(handler.getReference())){
            handler = buildReferenceHandler(serviceProvider, handler);
            serviceProviderEntry.setHandler(handler);
        }
        _build(serviceProvider, handler);
    }

    private static HandlerConfig buildReferenceHandler(ServiceProvider serviceProvider, HandlerConfig handler){
            HandlerChain handlerChain = ArtifactStore.getInstance().getHandlerChain(handler.getReference());
            if(handlerChain != null) {
                HandlerConfig referencedHandler = handlerChain.getHandlerChain().getHandler();
                buildReferencedHandler(handler, referencedHandler, serviceProvider);
                return referencedHandler ;
            }
            return null ;
    }

    private static void _build(ServiceProvider serviceProvider, HandlerConfig handlerEntry){
        //serviceProvider.getHandlerEntryMap().put(handlerEntry.getHandlerId(), handlerEntry);
        String reference = handlerEntry.getReference();
        if(reference != null){
            //Read Handler chain from chain store
            HandlerConfig handlerEntry1 = buildReferenceHandler(serviceProvider, handlerEntry);

        }
        HandlerConfig nextHandler = handlerEntry.nextHandler();
        if(nextHandler != null) {
            _build(serviceProvider, nextHandler);
        }

    }

    private static void buildReferencedHandler(HandlerConfig currentHandler, HandlerConfig referencedHandler,
                                               ServiceProvider serviceProvider){
        HandlerConfig lastHandler = getLastHandler(referencedHandler);
        HandlerConfig nextHandler = currentHandler.nextHandler();
        lastHandler.setNextHandler(nextHandler);
    }
    private static HandlerConfig getLastHandler(HandlerConfig handlerEntry){
        HandlerConfig lastHandler = handlerEntry ;
        while(handlerEntry.nextHandler() != null){
            lastHandler = handlerEntry.nextHandler();
            handlerEntry = handlerEntry.nextHandler();
        }
        return lastHandler ;
    }
    */
}
