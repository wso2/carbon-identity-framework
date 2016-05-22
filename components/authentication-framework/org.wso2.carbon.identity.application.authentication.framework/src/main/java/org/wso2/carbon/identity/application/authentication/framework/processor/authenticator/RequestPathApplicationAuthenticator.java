package org.wso2.carbon.identity.application.authentication.framework.processor.authenticator;


import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

public interface RequestPathApplicationAuthenticator extends ApplicationAuthenticator {
    public boolean canHandle(AuthenticationContext authenticationContext);
}
