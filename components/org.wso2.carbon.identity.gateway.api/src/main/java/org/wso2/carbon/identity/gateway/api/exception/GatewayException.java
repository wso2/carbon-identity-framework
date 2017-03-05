package org.wso2.carbon.identity.gateway.api.exception;


import org.wso2.carbon.identity.common.base.exception.IdentityException;

public class GatewayException extends IdentityException{
    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(Throwable cause) {
        super(cause);
    }

    public GatewayException(String errorCode, String message) {
        super(errorCode, message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public GatewayException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
