package org.wso2.carbon.identity.framework.authentication.processor.handler;

import org.wso2.carbon.identity.framework.FrameworkException;


public class FrameworkHandlerException extends FrameworkException {
    public FrameworkHandlerException(String message) {
        super(message);
    }

    public FrameworkHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
