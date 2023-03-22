package org.wso2.carbon.identity.application.authentication.framework.exception;

/**
 * Session nonce Cookie Validation Failed Exception.
 */
public class CookieValidationFailedException extends Exception {

        public CookieValidationFailedException(String message) {
            super(message);
        }

        public CookieValidationFailedException(String message, Throwable cause) {
            super(message, cause);
        }
}
