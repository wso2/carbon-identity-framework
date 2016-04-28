package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;

public abstract class ResponseBuilderHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }
    public abstract IdentityResponse.IdentityResponseBuilder buildErrorResponse(IdentityMessageContext identityMessageContext);
    public abstract IdentityResponse.IdentityResponseBuilder buildRedirectResponse(IdentityMessageContext identityMessageContext);
    public abstract IdentityResponse.IdentityResponseBuilder buildResponse(IdentityMessageContext identityMessageContext);
}
