package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.base.IdentityException;

public class AuthenticationHandlerException extends FrameworkException {

    private static final long serialVersionUID = -8680134348172156343L;

    public AuthenticationHandlerException(String message) {
        super(message);
    }

    public AuthenticationHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
