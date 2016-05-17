package org.wso2.carbon.identity.application.authentication.framework.processor.handler;

import org.wso2.carbon.identity.application.authentication.framework.FrameworkException;


public class FrameworkHandlerException extends FrameworkException {
    public FrameworkHandlerException(String message) {
        super(message);
    }

    public FrameworkHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
