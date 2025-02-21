/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.api.model;

/**
 * This class models the Error entity.
 * This captures the error and error description communicated by the external service in a service error scenario.
 * This may also include the error and description of any issue that occurred while invoking the external service
 * or processing its response.
 */
public class Error {

    private final String errorMessage;
    private final String errorDescription;

    public Error(String errorMessage, String errorDescription) {

        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
    }

    public String getErrorMessage() {

        return errorMessage;
    }

    public String getErrorDescription() {

        return errorDescription;
    }
}
