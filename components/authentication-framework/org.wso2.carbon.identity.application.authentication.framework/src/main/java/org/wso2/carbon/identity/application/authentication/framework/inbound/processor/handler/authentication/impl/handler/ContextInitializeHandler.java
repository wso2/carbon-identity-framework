package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .Constants;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.context.AuthenticationRequestContext;



import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;

public class ContextInitializeHandler extends FrameworkHandler{
    @Override
    public String getName() {
        return null;
    }

    public void initialize(IdentityMessageContext identityMessageContext, Sequence sequence) {
        AuthenticationRequestContext authenticationRequestContext = getAuthenticationContext(identityMessageContext);
        authenticationRequestContext.setSequence(sequence);

    }

    protected AuthenticationRequestContext getAuthenticationContext(IdentityMessageContext identityMessageContext){
        AuthenticationRequestContext authenticationRequestContext = (AuthenticationRequestContext)identityMessageContext.getParameter(Constants.AUTHENTICATION_CONTEXT);
        if(authenticationRequestContext == null){
            synchronized (this) {
                authenticationRequestContext = new AuthenticationRequestContext();
                identityMessageContext.getParameters().put(Constants.AUTHENTICATION_CONTEXT, authenticationRequestContext);
            }
        }
        return authenticationRequestContext;
    }
}
