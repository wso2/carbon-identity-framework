/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;

public class InboundAuthenticationManager {

    private static final Log log = LogFactory.getLog(InboundAuthenticationManager.class);

    /**
     * Get inbound request processor
     *
     * @param authenticationRequest Authentication request
     * @return Inbound authentication request processor
     * @throws FrameworkException
     */
    private InboundAuthenticationRequestProcessor getInboundRequestProcessor(
            InboundAuthenticationRequest authenticationRequest) throws FrameworkException {
        List<InboundAuthenticationRequestProcessor> requestProcessors = FrameworkServiceDataHolder.getInstance()
                .getInboundAuthenticationRequestProcessors();

        for (InboundAuthenticationRequestProcessor requestProcessor : requestProcessors) {
            if (requestProcessor.canHandle(authenticationRequest)) {
                return requestProcessor;
            }
        }
        return null;
    }

    /**
     * Get inbound response builder
     *
     * @param context  Inbound authentication context
     * @param authenticationRequest Inbound authentication request
     * @return Inbound authentication response builder
     * @throws FrameworkException
     */
    private InboundAuthenticationResponseProcessor getInboundResponseBuilder(InboundAuthenticationContext context,
            InboundAuthenticationRequest authenticationRequest) throws FrameworkException {
        List<InboundAuthenticationResponseProcessor> responseBuilders = FrameworkServiceDataHolder.getInstance()
                .getInboundAuthenticationResponseProcessors();

        for (InboundAuthenticationResponseProcessor responseBuilder : responseBuilders) {
            if (responseBuilder.canHandle(context, authenticationRequest)) {
                return responseBuilder;
            }
        }
        return null;
    }

    /**
     * Process authentication request
     *
     * @param authenticationRequest Authentication request
     * @return Inbound authentication response
     * @throws FrameworkException
     */
    public InboundAuthenticationResponse processRequest(InboundAuthenticationRequest authenticationRequest)
            throws FrameworkException {

        InboundAuthenticationRequestProcessor requestProcessor = getInboundRequestProcessor(authenticationRequest);
        if (requestProcessor != null) {
            if (log.isDebugEnabled()) {
                log.debug("Starting to process inbound authentication request : " + requestProcessor.getName());
            }
            return requestProcessor.process(authenticationRequest);
        } else {
            throw new FrameworkException("No inbound request processor found to process the request");
        }
    }

    /**
     * Process response
     *
     * @param context Inbound authentication context
     * @param authenticationRequest Inbound authentication request
     * @return Inbound authentication response
     * @throws FrameworkException
     */
    public InboundAuthenticationResponse processResponse(InboundAuthenticationContext context,
            InboundAuthenticationRequest authenticationRequest) throws FrameworkException {

        InboundAuthenticationResponseProcessor responseBuilder = getInboundResponseBuilder(context,
                authenticationRequest);
        if (responseBuilder != null) {
            if (log.isDebugEnabled()) {
                log.debug("Starting to process inbound authentication response : " + responseBuilder.getName());
            }
            return responseBuilder.processResponse(context);
        } else {
            throw new FrameworkException("No response builder found to process the response");
        }
    }

}
