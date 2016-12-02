package org.wso2.carbon.identity.gateway.demo;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;

public class DemoSequenceBuildFactory extends AbstractSequenceBuildFactory {


    @Override
    public AbstractSequence buildSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        DemoSequence demoSequence = null;
        try {
            demoSequence = new DemoSequence(authenticationContext);
        } catch (IdentityApplicationManagementException e) {
            e.printStackTrace();
        }
        return demoSequence;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }
}
