package org.wso2.carbon.identity.application.authentication.framework.demo;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl
        .AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model
        .AbstractSequence;
import org.wso2.carbon.identity.core.bean.context.MessageContext;

public class DemoSequenceBuildFactory extends AbstractSequenceBuildFactory {


    @Override
    public AbstractSequence buildSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        return new DemoSequence(authenticationContext);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }
}
