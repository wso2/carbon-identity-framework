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

package org.wso2.carbon.identity.sample.inbound.validator;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.handler.validator.AbstractRequestValidator;
import org.wso2.carbon.identity.gateway.exception.RequestValidatorException;
import org.wso2.carbon.identity.sample.inbound.request.SampleProtocolRequest;

public class SampleProtocolValidator extends AbstractRequestValidator {
    @Override
    public GatewayHandlerResponse validate(AuthenticationContext authenticationContext) throws
                                                                                          RequestValidatorException {
        String generateRuntimeException = authenticationContext.getIdentityRequest().getParameter
                ("generateRuntimeException");
        if(StringUtils.isNotBlank(generateRuntimeException)){
            throw new RuntimeException("Checked the exception for generate the generateRuntimeException.");
        }
        String generateClientException = authenticationContext.getIdentityRequest().getParameter
                ("generateClientException");
        if(StringUtils.isNotBlank(generateClientException)){
            throw new RequestValidatorException("Checked the exception for generate the generateRuntimeException.");
        }

        String generateGatewayClientException = authenticationContext.getIdentityRequest().getParameter
                ("generateGatewayRuntimeException");
        if(StringUtils.isNotBlank(generateGatewayClientException)){
            throw new GatewayRuntimeException("Checked the exception for generate the generateGatewayRuntimeException.");
        }




        try {
            String uniqueID = authenticationContext.getIdentityRequest().getParameter("uniqueID");
            if (StringUtils.isNotBlank(uniqueID)) {
                authenticationContext.setServiceProviderId(uniqueID);
            } else {
                authenticationContext.setServiceProviderId("travelocity.com");
            }
        } catch (GatewayClientException e) {
            throw new RequestValidatorException(e.getMessage(), e);
        }
        if (authenticationContext.getServiceProvider() == null) {
            throw new RequestValidatorException("No Service Provider Found for this Unique ID");
        }

        if (authenticationContext.getIdentityRequest().getParameter("NotProtocolCompliant") != null) {
            throw new RequestValidatorException("Error while validating request");
        }
        // Can access validator configurations.
        getValidatorConfig(authenticationContext);
        return new GatewayHandlerResponse();
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        if (messageContext instanceof GatewayMessageContext) {
            GatewayMessageContext gatewayMessageContext = (GatewayMessageContext) messageContext;
            if (gatewayMessageContext.getIdentityRequest() instanceof SampleProtocolRequest) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public String getValidatorType() {
        return "SampleProtocolRequestValidator";
    }
}
