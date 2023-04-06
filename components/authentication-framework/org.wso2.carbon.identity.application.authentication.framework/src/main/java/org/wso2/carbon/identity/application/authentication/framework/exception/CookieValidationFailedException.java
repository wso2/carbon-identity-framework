package org.wso2.carbon.identity.application.authentication.framework.exception;

import java.io.IOException;

/**
 * Session nonce Cookie Validation Failed Exception.
 */
public class CookieValidationFailedException extends IOException {
    public CookieValidationFailedException(String errorCode, String message) {
        super(errorCode + " : " + message);
    }
}
