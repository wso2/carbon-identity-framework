package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;



import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;

public class ContextInitializer extends FrameworkHandler{
    @Override
    public String getName() {
        return null;
    }

    public void initialize(AuthenticationContext authenticationContext, Sequence sequence) {

    }

}
