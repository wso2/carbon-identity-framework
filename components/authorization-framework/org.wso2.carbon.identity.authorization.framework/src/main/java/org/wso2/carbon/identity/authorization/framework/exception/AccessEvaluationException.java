package org.wso2.carbon.identity.authorization.framework.exception;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * The {@code AccessEvaluationException} class represents the exception that is thrown when an error occurs during
 * Fine-Grained Authorization related flows.
 */
public class AccessEvaluationException extends IdentityException {

    private String errorCode = null;

    public AccessEvaluationException(String message) {
        super(message);
    }

    public AccessEvaluationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AccessEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessEvaluationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
