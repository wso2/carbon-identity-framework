package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityFrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityFrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;

public class AuthenticationHandler extends IdentityFrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public IdentityFrameworkHandlerStatus authenticate(IdentityMessageContext identityMessageContext) throws  AuthenticationException{
        return null;
    }
}
