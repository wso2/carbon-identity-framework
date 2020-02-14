/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.claim.metadata.mgt.exception;

/**
 * Exception class that represents server exceptions thrown by ClaimMetadataManagementService.
 */
public class ClaimMetadataServerException extends ClaimMetadataException {

    /**
     * Constructs a new exception with an error code, detail message and throwable.
     *
     * @param errorCode The error code
     * @param message   The detail message
     * @param cause     Throwable
     */
    public ClaimMetadataServerException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
}
