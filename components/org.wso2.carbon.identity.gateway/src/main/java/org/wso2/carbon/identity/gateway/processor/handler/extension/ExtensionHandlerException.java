package org.wso2.carbon.identity.gateway.processor.handler.extension;

import org.wso2.carbon.identity.gateway.api.FrameworkException;

public class ExtensionHandlerException extends FrameworkException {
    public ExtensionHandlerException(String message) {
        super(message);
    }

    public ExtensionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
