/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.onboard.core.service.exception;

/**
 * Generic exception that is thrown when there is an error in the user onboard core services.
 */
public class UserOnboardServiceException extends Exception {

    /**
     * Creates UserOnboardCoreService Exception.
     */
    public UserOnboardServiceException() {

        super();
    }

    /**
     * Creates UserOnboardCoreService Exception.
     *
     * @param message Error message.
     */
    public UserOnboardServiceException(String message) {

        super(message);
    }

    /**
     * Creates UserOnboardCoreService Exception.
     *
     * @param throwable Error or exception.
     */
    public UserOnboardServiceException(Throwable throwable) {

        super(throwable);
    }

    /**
     * Creates UserOnboardCoreService Exception.
     *
     * @param message Error message.
     * @param throwable Errors or Exception.
     */
    public UserOnboardServiceException(String message, Throwable throwable) {

        super(message, throwable);
    }
}
