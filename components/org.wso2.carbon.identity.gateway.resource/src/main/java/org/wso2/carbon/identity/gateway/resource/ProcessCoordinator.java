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

package org.wso2.carbon.identity.gateway.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.resource.internal.GatewayResourceDataHolder;

import java.util.List;

public class ProcessCoordinator {

    private Logger log = LoggerFactory.getLogger(ProcessCoordinator.class);

    public GatewayResponse process(GatewayRequest gatewayRequest) throws GatewayServerException {

        GatewayProcessor processor = getIdentityProcessor(gatewayRequest);
        if (processor != null) {
            if (log.isDebugEnabled()) {
                log.debug("Starting to process GatewayProcessor : " + processor.getName());
            }
            return processor.process(gatewayRequest).build();
        } else {
            throw new GatewayRuntimeException("No GatewayProcessor found to process the request.");
        }
    }

    private GatewayProcessor getIdentityProcessor(GatewayRequest gatewayRequest) {
        List<GatewayProcessor> processors = GatewayResourceDataHolder.getInstance().getGatewayProcessors();

        for (GatewayProcessor requestProcessor : processors) {
            try {
                if (requestProcessor.canHandle(gatewayRequest)) {
                    return requestProcessor;
                }
            } catch (Exception e) {
                log.error("Error occurred while checking if " + requestProcessor.getName() + " can handle " +
                          gatewayRequest.toString());
            }
        }
        return null;
    }

}
