package org.wso2.carbon.identity.gateway.authentication.processor.authenticator;


import org.wso2.carbon.identity.gateway.authentication.context.AuthenticationContext;

public interface RequestPathApplicationAuthenticator extends ApplicationAuthenticator {
    public boolean canHandle(AuthenticationContext authenticationContext);
}
