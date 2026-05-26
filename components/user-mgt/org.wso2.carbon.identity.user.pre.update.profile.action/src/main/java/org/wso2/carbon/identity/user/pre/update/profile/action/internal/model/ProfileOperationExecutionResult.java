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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.model;

import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;

/**
 * This class represents the result of a profile operation execution.
 */
public class ProfileOperationExecutionResult {

    private final PerformableOperation operation;
    private final Status status;
    private final String message;

    public ProfileOperationExecutionResult(PerformableOperation operation, Status status, String message) {

        this.operation = operation;
        this.status = status;
        this.message = message;
    }

    public PerformableOperation getOperation() {

        return operation;
    }

    public Status getStatus() {

        return status;
    }

    public String getMessage() {

        return message;
    }

    @Override
    public String toString() {

        return "ProfileOperationExecutionResult{"
                + "operation=" + operation
                + ", status=" + status
                + ", message='" + message + '\''
                + '}';
    }

    /**
     * Enum to represent profile operation execution status.
     */
    public enum Status {
        SUCCESS,
        FAILURE
    }
}
