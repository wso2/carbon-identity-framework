package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

public class RequestHandlerException extends FrameworkException {
    public RequestHandlerException(String message) {
        super(message);
    }

    public RequestHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
