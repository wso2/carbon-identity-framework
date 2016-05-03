package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

public class ResponseException extends FrameworkException {
    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
