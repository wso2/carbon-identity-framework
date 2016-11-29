package org.wso2.carbon.identity.gateway.authentication.processor.handler.claim;

import org.wso2.carbon.identity.gateway.authentication.processor.handler.FrameworkHandlerException;

public class ClaimHandlerException extends FrameworkHandlerException {
    public ClaimHandlerException(String message) {
        super(message);
    }

    public ClaimHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
