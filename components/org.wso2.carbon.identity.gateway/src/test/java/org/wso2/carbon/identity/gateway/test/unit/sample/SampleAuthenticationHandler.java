package org.wso2.carbon.identity.gateway.test.unit.sample;

import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.handler.authentication.AuthenticationHandler;

public class SampleAuthenticationHandler extends AuthenticationHandler {
    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {
        throw new GatewayRuntimeException("This is an error while evaluating can handle");
    }
}
