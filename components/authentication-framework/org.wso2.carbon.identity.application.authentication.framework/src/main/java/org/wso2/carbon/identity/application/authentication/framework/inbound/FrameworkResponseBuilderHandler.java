package org.wso2.carbon.identity.application.authentication.framework.inbound;


public abstract class FrameworkResponseBuilderHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }
    public abstract IdentityResponse.IdentityResponseBuilder buildResponse(IdentityMessageContext identityMessageContext);
}
