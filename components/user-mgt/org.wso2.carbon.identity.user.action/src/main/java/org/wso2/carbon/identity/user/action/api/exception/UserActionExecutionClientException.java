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

package org.wso2.carbon.identity.user.action.api.exception;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.core.UserStoreClientException;

/**
 * Exception class to denote user action execution related client exceptions.
 * Represents FAILED status responses from service extensions that extend User operations.
 * "error" maps to the "failureReason" field in the response.
 * "description" maps to the "failureDescription" field in the response.
 */
public class UserActionExecutionClientException extends UserStoreClientException {

    private final String error;
    private final String description;

    public UserActionExecutionClientException(String errorCode, String error, String description) {

        super(buildErrorMessage(error, description), errorCode);
        this.error = error;
        this.description = description;
    }

    public String getError() {

        return error;
    }

    public String getDescription() {

        return description;
    }

    private static String buildErrorMessage(String error, String description) {

        return StringUtils.isNotBlank(description) ? error + ". " + description : error;
    }
}
