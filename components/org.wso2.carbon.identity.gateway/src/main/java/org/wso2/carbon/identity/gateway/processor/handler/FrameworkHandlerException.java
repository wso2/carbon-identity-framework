package org.wso2.carbon.identity.gateway.processor.handler;

import org.wso2.carbon.identity.gateway.api.FrameworkException;


public class FrameworkHandlerException extends FrameworkException {
    public FrameworkHandlerException(String message) {
        super(message);
    }

    public FrameworkHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
