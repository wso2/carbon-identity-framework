package org.wso2.carbon.identity.gateway.processor.authenticator;


import org.wso2.carbon.identity.gateway.context.AuthenticationContext;

public interface RequestPathApplicationAuthenticator extends ApplicationAuthenticator {
    public boolean canHandle(AuthenticationContext authenticationContext);
}
