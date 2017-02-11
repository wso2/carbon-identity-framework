package org.wso2.carbon.identity.gateway.processor.handler;

import org.wso2.carbon.identity.gateway.api.FrameworkServerException;


public class FrameworkHandlerException extends FrameworkServerException {
    public FrameworkHandlerException(String message) {
        super(message);
    }

    public FrameworkHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
