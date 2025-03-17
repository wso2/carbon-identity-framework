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

package org.wso2.carbon.identity.authorization.framework.exception;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * The {@code AccessEvaluationException} class represents the exception that is thrown when an error occurs during
 * Access Evaluation related flows.
 */
public class AccessEvaluationException extends IdentityException {

    private static final long serialVersionUID = 9162001723200243731L;
    private String errorCode = null;

    public AccessEvaluationException(String message) {
        super(message);
    }

    public AccessEvaluationException(String errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }

    public AccessEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessEvaluationException(String errorCode, String message, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
