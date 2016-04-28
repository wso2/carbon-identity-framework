package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication;


import org.wso2.carbon.identity.base.IdentityException;

public class AuthenticationException extends IdentityException {

    private static final long serialVersionUID = -8680134348172156343L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
