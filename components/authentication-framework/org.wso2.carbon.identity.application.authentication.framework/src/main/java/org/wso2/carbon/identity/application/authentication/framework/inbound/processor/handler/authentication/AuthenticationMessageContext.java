package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;

import java.util.Map;

public class AuthenticationMessageContext extends IdentityMessageContext{
    public AuthenticationMessageContext(
            IdentityRequest request,
            Map parameters) {
        super(request, parameters);
    }
}
