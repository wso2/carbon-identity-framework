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

package org.wso2.carbon.identity.external.api.token.handler.api.model;

import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;

/**
 * Model class for Operation Result of token response processing.
 */
public class OperationResult {

    private final OperationType operationType;
    private final boolean isSuccess;
    private final Error error;

    private OperationResult(OperationType operationType, boolean isSuccess, Error error) {

        this.operationType = operationType;
        this.isSuccess = isSuccess;
        this.error = error;
    }

    /**
     * Create a successful OperationResult.
     *
     * @param type Operation Type.
     * @return OperationResult instance representing success.
     */
    public static OperationResult success(OperationType type) {

        return new OperationResult(type, true, null);
    }

    /**
     * Create a failed OperationResult.
     *
     * @param type         Operation Type.
     * @param errorMessage Error Message enum.
     * @param data         Additional data for error description.
     * @param e            Throwable cause of the failure.
     * @return OperationResult instance representing failure.
     */
    public static OperationResult failure(OperationType type, ErrorMessage errorMessage, String data, Throwable e) {

        return new OperationResult(type, false, new Error(errorMessage, data, e));
    }

    public OperationType getOperationType() {

        return operationType;
    }

    public boolean isSuccess() {

        return isSuccess;
    }

    public Error getError() {

        return error;
    }

    /**
     * Model class for Error.
     */
    public static class Error {

        private final String errorCode;
        private final String errorMessage;
        private final String errorDescription;
        private final Throwable throwable;

        public Error(ErrorMessage error, String descriptionData, Throwable e) {

            if (error == null) {
                throw new IllegalArgumentException("The Error message cannot be null.");
            }
            errorCode = error.getCode();
            errorMessage = error.getMessage();
            errorDescription = String.format(error.getDescription(), descriptionData);
            throwable = e;
        }

        public String getErrorCode() {

            return errorCode;
        }

        public String getErrorMessage() {

            return errorMessage;
        }

        public String getErrorDescription() {

            return errorDescription;
        }

        public Throwable getThrowable() {

            return throwable;
        }
    }

    /**
     * Enum for Operation Types.
     */
    public enum OperationType {

        ACCESS_TOKEN_PARSING("access_token"),
        REFRESH_TOKEN_PARSING("refresh_token");

        private final String tokenName;

        OperationType(String tokenName) {

            this.tokenName = tokenName;
        }

        public String getTokenName() {

            return tokenName;
        }
    }
}
