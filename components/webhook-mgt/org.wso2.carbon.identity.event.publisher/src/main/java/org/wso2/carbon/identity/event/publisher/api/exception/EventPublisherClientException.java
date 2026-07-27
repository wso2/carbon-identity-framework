/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.event.publisher.api.exception;

/**
 * Exception class for client-side errors in event publisher.
 */
public class EventPublisherClientException extends EventPublisherException {

    /**
     * Constructs a new exception with the specified detail message, description, and error code.
     *
     * @param message     The detail message
     * @param description The description
     * @param errorCode   The error code
     */
    public EventPublisherClientException(String errorCode, String message, String description) {

        super(errorCode, message, description);
    }
}
