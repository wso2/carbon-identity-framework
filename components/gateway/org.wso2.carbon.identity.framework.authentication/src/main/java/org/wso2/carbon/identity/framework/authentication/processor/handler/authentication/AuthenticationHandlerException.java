package org.wso2.carbon.identity.framework.authentication.processor.handler.authentication;


import org.wso2.carbon.identity.framework.authentication.processor.handler.FrameworkHandlerException;

public class AuthenticationHandlerException extends FrameworkHandlerException {

    private static final long serialVersionUID = -8680134348172156343L;

    public AuthenticationHandlerException(String message) {
        super(message);
    }

    public AuthenticationHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
