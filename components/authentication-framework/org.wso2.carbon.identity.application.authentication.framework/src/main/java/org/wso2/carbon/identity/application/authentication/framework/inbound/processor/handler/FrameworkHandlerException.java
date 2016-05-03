package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;


public class FrameworkHandlerException extends FrameworkException {
    public FrameworkHandlerException(String message) {
        super(message);
    }

    public FrameworkHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
