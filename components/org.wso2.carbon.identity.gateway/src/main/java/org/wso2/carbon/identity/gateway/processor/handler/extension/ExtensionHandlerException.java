package org.wso2.carbon.identity.gateway.processor.handler.extension;

import org.wso2.carbon.identity.gateway.api.FrameworkServerException;

public class ExtensionHandlerException extends FrameworkServerException {
    public ExtensionHandlerException(String message) {
        super(message);
    }

    public ExtensionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
