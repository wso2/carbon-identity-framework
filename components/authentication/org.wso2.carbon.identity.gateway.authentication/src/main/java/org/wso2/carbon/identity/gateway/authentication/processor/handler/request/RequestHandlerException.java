package org.wso2.carbon.identity.gateway.authentication.processor.handler.request;

import org.wso2.carbon.identity.gateway.authentication.processor.handler.FrameworkHandlerException;

public class RequestHandlerException extends FrameworkHandlerException {
    public RequestHandlerException(String message) {
        super(message);
    }

    public RequestHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
