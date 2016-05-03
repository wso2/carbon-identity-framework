package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.jit;

import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler
        .FrameworkHandlerException;

public class JITHandlerException extends FrameworkHandlerException {
    public JITHandlerException(String message) {
        super(message);
    }

    public JITHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
