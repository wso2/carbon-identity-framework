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

package org.wso2.carbon.identity.gateway.element.authentication.handler;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.impl.MultiOptionStepHandler;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;

/**
 * This authenticator provides the ability to do multi option authentication in a step.
 */
public class MultiOptionAuthenticationHandler extends MultiOptionStepHandler<GatewayMessageContext> {

    private Logger logger = LoggerFactory.getLogger(MultiOptionAuthenticationHandler.class);

    public MultiOptionAuthenticationHandler(AbstractAuthenticationHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    public AbstractAuthenticationHandler nextHandler(GatewayMessageContext messageContext) {
        return null;
    }


    @Override
    protected AbstractAuthenticationHandler getSelectedHandler(GatewayMessageContext messageContext)
            throws HandlerException {

        final String OPTION = "option";

        String optionValue = (String) messageContext.getCurrentIdentityRequest().getProperty(OPTION);
        if (StringUtils.isBlank(optionValue)) {
            throw new HandlerException("No selected option parameter found.");
        }

        int option;
        try {
            option = Integer.parseInt(optionValue);
            if (option <= 0 || option > multiOptionHandlers.size()) {
                throw new HandlerException("Invalid Option : " + option);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Option '" + option + "' was selected by the user.");
            }
        } catch (NumberFormatException ex) {
            throw new HandlerException("Invalid Option provided", ex);
        }
        return (AbstractAuthenticationHandler)multiOptionHandlers.get(option - 1);
    }

    public MultiOptionAuthenticationHandler addOption(AbstractAuthenticationHandler gatewayHandler) {
        multiOptionHandlers.add(gatewayHandler);
        return this;
    }

    @Override
    public boolean canHandle(GatewayMessageContext messageContext) {

        return true;
    }
}
