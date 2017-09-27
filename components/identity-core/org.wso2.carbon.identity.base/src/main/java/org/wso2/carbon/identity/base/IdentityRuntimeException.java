/*
 *  Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.base;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used for creating checked exceptions that can be handled.
 */
public class IdentityRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -5872545821846152596L;
    private String errorCode = null;


    public IdentityRuntimeException(String message) {
        super(message);
    }

    public IdentityRuntimeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public IdentityRuntimeException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public IdentityRuntimeException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public IdentityRuntimeException(String errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }

    public static IdentityRuntimeException error(String message) {
        return new IdentityRuntimeException(message);
    }

    public static IdentityRuntimeException error(String errorCode, String message) {
        return new IdentityRuntimeException(errorCode, message);
    }

    public static IdentityRuntimeException error(String message, Throwable cause) {
        return new IdentityRuntimeException(message, cause);
    }

    public static IdentityRuntimeException error(String errorCode, String message,
                                                 Throwable cause) {
        return new IdentityRuntimeException(errorCode, message, cause);
    }

    public static <T extends IdentityRuntimeException> T error(Class<T> exceptionClass, String message) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class).newInstance(message);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage());
        }
        return exception;
    }

    public static <T extends IdentityRuntimeException> T error(Class<T> exceptionClass, String errorCode, String message) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class, String.class).newInstance(errorCode, message);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage());
        }
        return exception;
    }

    public static <T extends IdentityRuntimeException> T error(Class<T> exceptionClass, String message, Throwable cause) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class, Throwable.class).newInstance(message, cause);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage());
        }
        return exception;
    }

    public static <T extends IdentityRuntimeException> T error(Class<T> exceptionClass, String errorCode, String message,
                                                               Throwable cause) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class, String.class, Throwable.class).
                    newInstance(errorCode, message, cause);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage());
        }
        return exception;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

}
