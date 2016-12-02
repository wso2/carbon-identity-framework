package org.wso2.carbon.identity.gateway.processor.handler.authorization;

import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandlerException;


public class AuthorizationHandlerException extends FrameworkHandlerException {
    public AuthorizationHandlerException(String message) {
        super(message);
    }

    public AuthorizationHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
