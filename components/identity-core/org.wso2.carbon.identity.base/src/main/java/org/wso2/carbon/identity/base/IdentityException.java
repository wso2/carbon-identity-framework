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

/**
 * Used for creating checked exceptions that can be handled.
 */
public class IdentityException extends Exception {

    private static final long serialVersionUID = 725992116511551241L;
    private String errorCode = null;

    public IdentityException(String message) {
        super(message);
    }

    public IdentityException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public IdentityException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentityException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    @Deprecated
    public static IdentityException error(String message) {
        return new IdentityException(message);
    }
    @Deprecated
    public static IdentityException error(String errorCode, String message) {
        return new IdentityException(errorCode, message);
    }
    @Deprecated
    public static IdentityException error(String message, Throwable cause) {
        return new IdentityException(message, cause);
    }
    @Deprecated
    public static IdentityException error(String errorCode, String message, Throwable cause) {
        return new IdentityException(errorCode, message, cause);
    }


    public static <T extends IdentityException> T error(Class<T> exceptionClass, String message) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class).newInstance(message);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage());
        }
        return exception;
    }

    public static <T extends IdentityException> T error(Class<T> exceptionClass, String errorCode, String message) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class, String.class).newInstance(errorCode, message);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage(), e);
        }
        return exception;
    }

    public static <T extends IdentityException> T error(Class<T> exceptionClass, String message, Throwable cause) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class, Throwable.class).newInstance(message, cause);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage(), e);
        }
        return exception;
    }

    public static <T extends IdentityException> T error(Class<T> exceptionClass, String errorCode, String message,
                                                        Throwable cause) {
        T exception = null;
        try {
            exception = exceptionClass.getConstructor(String.class, String.class, Throwable.class).
                    newInstance(errorCode, message, cause);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage(), e);
        }
        return exception;
    }

    /**
     * Creates and returns an instance of a specified exception class that extends {@link IdentityException}.
     * The specified exception class should support the constructor arguments used in this method:
     * {@code (String errorCode, String message, String description, Throwable cause)}.
     *
     * @param <T>            The type of exception to be created, which must extend {@link IdentityException}.
     * @param exceptionClass The class type of the exception to be instantiated.
     * @param errorCode      The error code associated with the exception.
     * @param message        A brief message describing the error.
     * @param description    A detailed description of the error.
     * @param cause          The underlying cause of the exception.
     * @return An instance of the specified exception type with the provided details.
     * @throws IdentityRuntimeException If the specified exception class cannot be instantiated.
     */
    public static <T extends IdentityException> T error(Class<T> exceptionClass, String errorCode, String message,
                                                        String description, Throwable cause) {

        T exception;
        try {
            exception = exceptionClass.getConstructor(String.class, String.class, String.class, Throwable.class).
                    newInstance(errorCode, message, description, cause);
        } catch (Exception e) {
            throw new IdentityRuntimeException("Invalid Exception Type, " + e.getMessage(), e);
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
