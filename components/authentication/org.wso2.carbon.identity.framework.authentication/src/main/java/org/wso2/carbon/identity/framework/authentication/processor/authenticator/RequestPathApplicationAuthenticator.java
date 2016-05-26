package org.wso2.carbon.identity.framework.authentication.processor.authenticator;


import org.wso2.carbon.identity.framework.authentication.context.AuthenticationContext;

public interface RequestPathApplicationAuthenticator extends ApplicationAuthenticator {
    public boolean canHandle(AuthenticationContext authenticationContext);
}
