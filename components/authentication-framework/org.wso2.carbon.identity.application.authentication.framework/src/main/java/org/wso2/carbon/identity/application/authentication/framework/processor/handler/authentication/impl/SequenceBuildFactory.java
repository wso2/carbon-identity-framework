package org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl;


import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl
        .model.Sequence;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;


public class SequenceBuildFactory extends FrameworkHandler {
    public Sequence buildSequence(AuthenticationContext authenticationContext) throws AuthenticationHandlerException {
        ServiceProvider serviceProvider = authenticationContext.getServiceProvider();
        Sequence sequence = new Sequence(serviceProvider);
        return sequence;
    }

    @Override
    public String getName() {
        return null;
    }
}
