package org.wso2.carbon.identity.gateway.framework.exception;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * Created by harsha on 5/17/16.
 */
public class FrameworkException extends IdentityException {
    protected FrameworkException(String errorDescription) {
        super(errorDescription);
    }

    protected FrameworkException(String errorDescription, Throwable cause) {
        super(errorDescription, cause);
    }
}
