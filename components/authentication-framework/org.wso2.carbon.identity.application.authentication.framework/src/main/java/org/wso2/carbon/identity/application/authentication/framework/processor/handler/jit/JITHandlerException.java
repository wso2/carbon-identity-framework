package org.wso2.carbon.identity.application.authentication.framework.processor.handler.jit;

import org.wso2.carbon.identity.application.authentication.framework.processor.handler
        .FrameworkHandlerException;

public class JITHandlerException extends FrameworkHandlerException {
    public JITHandlerException(String message) {
        super(message);
    }

    public JITHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
