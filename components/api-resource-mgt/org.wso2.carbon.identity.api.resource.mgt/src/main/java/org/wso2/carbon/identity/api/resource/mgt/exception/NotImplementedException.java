/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.api.resource.mgt.exception;

/**
 * A custom runtime exception to indicate that a method or functionality is not yet implemented.
 */
public class NotImplementedException extends RuntimeException {

    /**
     * Constructs a new NotImplementedException with no detail message or cause.
     * <p>
     * This constructor is typically used when no additional information is required
     * to explain why the exception is thrown.
     */
    public NotImplementedException() {

        super();
    }

    /**
     * Constructs a new NotImplementedException with a detailed message and the root cause.
     *
     * @param message A descriptive message explaining why this exception is thrown.
     * @param cause   The underlying cause of this exception.
     */
    public NotImplementedException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Constructs a new NotImplementedException with a detailed message.
     *
     * @param message A descriptive message explaining why this exception is thrown.
     */
    public NotImplementedException(String message) {

        super(message);
    }

    /**
     * Constructs a new NotImplementedException with the root cause.
     *
     * @param cause The underlying cause of this exception.
     */
    public NotImplementedException(Throwable cause) {

        super(cause);
    }
}
