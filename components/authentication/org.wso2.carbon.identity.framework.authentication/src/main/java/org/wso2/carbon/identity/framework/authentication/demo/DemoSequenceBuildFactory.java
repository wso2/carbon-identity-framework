package org.wso2.carbon.identity.framework.authentication.demo;

import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.framework.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl
        .AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.framework.authentication.processor.handler.authentication.impl.model.AbstractSequence;

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
