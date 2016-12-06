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

package org.wso2.carbon.identity.gateway.processor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.IdentityProcessor;
import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.message.IdentityRequest;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.gateway.handler.authentication.MultiStepAuthenticationHandler;
import org.wso2.carbon.identity.gateway.handler.authentication.authenticator.BasicAuthenticationHandler;
import org.wso2.carbon.identity.gateway.handler.response.SAMLResponseHandler;
import org.wso2.carbon.identity.gateway.handler.validation.SAMLValidationHandler;


/*
    This processor handler the initial identity requests that comes to the Identity Gateway.
 */
public class InitRequestProcessor extends IdentityProcessor {

    private static final Logger log = LoggerFactory.getLogger(InitRequestProcessor.class);

    @SuppressWarnings("unchecked")
    @Override
    public IdentityResponse process(IdentityRequest identityRequest) throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug(getName() + " processed the Identity Request successfully.");
        }

        // Build the message context
        IdentityMessageContext context = new IdentityMessageContext(identityRequest, new IdentityResponse());

        // Build a handler chain
        SAMLValidationHandler samlValidationHandler = new SAMLValidationHandler();

        MultiStepAuthenticationHandler multiStepAuthenticationHandler = new MultiStepAuthenticationHandler();
        BasicAuthenticationHandler basicAuthenticationHandler = new BasicAuthenticationHandler();

        SAMLResponseHandler samlResponseHandler = new SAMLResponseHandler();

        samlValidationHandler.setNextHandler(multiStepAuthenticationHandler);
        multiStepAuthenticationHandler.addIdentityGatewayEventHandler(basicAuthenticationHandler);
        basicAuthenticationHandler.setNextHandler(samlResponseHandler);

        // start executing the handler chain
        samlValidationHandler.execute(context);

        return context.getIdentityResponse();
    }

    @Override
    public String getName() {
        return "InitialRequestProcessor";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean canHandle(IdentityRequest identityRequest) {
        return true;
    }
}
