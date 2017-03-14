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

package org.wso2.carbon.identity.gateway.api.processor;

import org.wso2.carbon.identity.common.base.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;

/**
 * GatewayProcessor is the base class of Processor in gateway framework. If we want to handle a custom request, then
 * we have to write a custom processor and should accept this in canHandle method. Then when we send a request to the
 * gateway, it will pick this processor and call the process method.
 *
 * @param <T>
 */
public abstract class GatewayProcessor<T extends GatewayRequest> extends AbstractHandler {

    /**
     * Tells if this processor can handle this GatewayRequest. There can be any extended type of GatewayRequest in
     * runtime and we can check the instance of and accept it.
     *
     * @param gatewayRequest GatewayRequest
     * @return can/not handle
     */
    public abstract boolean canHandle(GatewayRequest gatewayRequest);

    /**
     * Process method can process any subclass of the GatewayRequest if the canHandle method accept it.
     *
     * @param identityRequest GatewayRequest
     * @return GatewayResponseBuilder
     */
    public abstract GatewayResponse.GatewayResponseBuilder process(T identityRequest);
}
