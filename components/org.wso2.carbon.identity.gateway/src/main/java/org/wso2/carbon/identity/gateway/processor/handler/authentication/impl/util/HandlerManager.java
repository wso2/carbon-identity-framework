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
package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util;


import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.ContextInitializer;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.RequestPathHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.StepHandler;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }

    public ContextInitializer getContextInitializerHandler(AuthenticationContext authenticationContext) {
        return (ContextInitializer)getHandler(FrameworkServiceDataHolder.getInstance().getContextInitializers(), authenticationContext);
    }


    public SequenceManager getSequenceManager(AuthenticationContext authenticationContext) {
        return (SequenceManager)getHandler(FrameworkServiceDataHolder.getInstance().getSequenceManagers(), authenticationContext);
    }

    public AbstractSequenceBuildFactory getSequenceBuildFactory(AuthenticationContext authenticationContext) {
        return (AbstractSequenceBuildFactory)getHandler(FrameworkServiceDataHolder.getInstance().getSequenceBuildFactories(), authenticationContext);
    }

    public StepHandler getStepHandler(AuthenticationContext authenticationContext) {
        return (StepHandler)getHandler(FrameworkServiceDataHolder.getInstance().getStepHandlers(), authenticationContext);
    }

    public RequestPathHandler getRequestPathHandler(AuthenticationContext authenticationContext) {
        return (RequestPathHandler)getHandler(FrameworkServiceDataHolder.getInstance().getRequestPathHandlers(), authenticationContext);
    }


    private FrameworkHandler getHandler(List<? extends FrameworkHandler> frameworkHandlers,
                                                      AuthenticationContext authenticationContext){
        if(frameworkHandlers != null){
            for(FrameworkHandler frameworkHandler: frameworkHandlers){
                if(frameworkHandler.canHandle(authenticationContext)){
                    return frameworkHandler ;
                }
            }
        }
        throw new GatewayRuntimeException("Cannot find a Handler to handle this request.");
    }

}
