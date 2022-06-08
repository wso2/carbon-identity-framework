package org.wso2.carbon.identity.application.authentication.framework.exception;

/**
 * This exception is used to handle the exceptions which occurred during optimization process of authentication context.
 */
public class AuthenticationContextOptimizationException extends FrameworkException {

    public AuthenticationContextOptimizationException(String message) {
        super(message);
    }

    public AuthenticationContextOptimizationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AuthenticationContextOptimizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationContextOptimizationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
