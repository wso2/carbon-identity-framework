package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.claim;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

public class ClaimHandlerException extends FrameworkException {
    public ClaimHandlerException(String message) {
        super(message);
    }

    public ClaimHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
