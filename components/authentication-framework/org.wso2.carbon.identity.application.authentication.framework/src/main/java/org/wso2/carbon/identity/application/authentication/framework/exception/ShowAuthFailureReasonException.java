package org.wso2.carbon.identity.application.authentication.framework.exception;

/**
 * Exception class to be used when the authentication failure reason should be shown to the user.
 */
public class ShowAuthFailureReasonException extends FrameworkException {

    public ShowAuthFailureReasonException(String message) {

        super(message);
    }

    public ShowAuthFailureReasonException(String message, Throwable cause) {

        super(message, cause);
    }

}
