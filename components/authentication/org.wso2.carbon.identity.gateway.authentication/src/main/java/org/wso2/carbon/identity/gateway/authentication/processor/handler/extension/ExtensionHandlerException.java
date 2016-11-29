package org.wso2.carbon.identity.gateway.authentication.processor.handler.extension;


import org.wso2.carbon.identity.gateway.framework.exception.FrameworkException;

public class ExtensionHandlerException extends FrameworkException {
    public ExtensionHandlerException(String message) {
        super(message);
    }

    public ExtensionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
