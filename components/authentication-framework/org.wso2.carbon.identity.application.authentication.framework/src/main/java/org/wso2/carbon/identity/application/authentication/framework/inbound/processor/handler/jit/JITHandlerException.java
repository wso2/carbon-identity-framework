package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.jit;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

public class JITHandlerException extends FrameworkException {
    public JITHandlerException(String message) {
        super(message);
    }

    public JITHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
