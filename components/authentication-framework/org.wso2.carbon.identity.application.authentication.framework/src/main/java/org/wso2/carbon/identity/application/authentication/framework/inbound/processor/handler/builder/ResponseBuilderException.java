package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.builder;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

public class ResponseBuilderException extends FrameworkException {
    public ResponseBuilderException(String message) {
        super(message);
    }

    public ResponseBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
