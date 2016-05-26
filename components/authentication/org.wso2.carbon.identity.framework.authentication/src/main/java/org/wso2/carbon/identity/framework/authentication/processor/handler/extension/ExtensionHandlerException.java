package org.wso2.carbon.identity.framework.authentication.processor.handler.extension;

import org.wso2.carbon.identity.application.authentication.framework.FrameworkException;

public class ExtensionHandlerException extends FrameworkException {
    public ExtensionHandlerException(String message) {
        super(message);
    }

    public ExtensionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
