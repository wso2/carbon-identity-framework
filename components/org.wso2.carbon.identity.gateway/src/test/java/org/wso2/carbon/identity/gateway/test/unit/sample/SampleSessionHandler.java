package org.wso2.carbon.identity.gateway.test.unit.sample;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.handler.session.DefaultSessionHandler;

public class SampleSessionHandler extends DefaultSessionHandler {
    @Override
    public boolean canHandle(MessageContext messageContext) {
        throw new GatewayRuntimeException("This is an exception while can handle");
    }

    @Override
    public int getPriority(MessageContext messageContext) {
        return 0;
    }
}
