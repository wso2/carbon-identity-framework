package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authorization;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;


public class AuthorizationHandlerException extends FrameworkException {
    public AuthorizationHandlerException(String message) {
        super(message);
    }

    public AuthorizationHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
