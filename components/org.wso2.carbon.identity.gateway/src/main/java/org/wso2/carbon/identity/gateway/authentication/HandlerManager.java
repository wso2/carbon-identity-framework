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
package org.wso2.carbon.identity.gateway.authentication;


import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.authentication.executer.AbstractExecutionHandler;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }

    public RequestPathHandler getRequestPathHandler(AuthenticationContext authenticationContext) {
        return (RequestPathHandler) getHandler(GatewayServiceHolder.getInstance().getRequestPathHandlers(),
                authenticationContext);
    }

    public AbstractSequenceBuildFactory getSequenceBuildFactory(AuthenticationContext authenticationContext) {
        return (AbstractSequenceBuildFactory) getHandler(GatewayServiceHolder.getInstance().getSequenceBuildFactories(), authenticationContext);
    }

    public SequenceManager getSequenceManager(AuthenticationContext authenticationContext) {
        return (SequenceManager) getHandler(GatewayServiceHolder.getInstance().getSequenceManagers(),
                authenticationContext);
    }

    public StepHandler getStepHandler(AuthenticationContext authenticationContext) {
        return (StepHandler) getHandler(GatewayServiceHolder.getInstance().getStepHandlers(), authenticationContext);
    }

    public AbstractExecutionHandler getExecutionHandler(AuthenticationContext authenticationContext) {
        return (AbstractExecutionHandler) getHandler(GatewayServiceHolder.getInstance().getExecutionHandlers(), authenticationContext);
    }

    private AbstractGatewayHandler getHandler(List<? extends AbstractGatewayHandler> frameworkHandlers,
                                              AuthenticationContext authenticationContext) {
        if (frameworkHandlers != null) {
            for (AbstractGatewayHandler abstractGatewayHandler : frameworkHandlers) {
                if (abstractGatewayHandler.canHandle(authenticationContext)) {
                    return abstractGatewayHandler;
                }
            }
        }
        String errorMessage = "Cannot find a Handler to handle this request, getHandler";
        throw new GatewayRuntimeException(errorMessage);
    }
}
