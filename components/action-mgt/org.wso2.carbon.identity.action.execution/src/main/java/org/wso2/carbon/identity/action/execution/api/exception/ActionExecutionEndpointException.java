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

package org.wso2.carbon.identity.action.execution.api.exception;

import org.wso2.carbon.identity.central.log.mgt.utils.TenantResourceFailure;

/**
 * Exception class for Action Execution failures caused by the external endpoint configured for the action.
 * This exception is thrown when the endpoint is unreachable, times out, or returns a response that does not
 * comply with the action contract (e.g. an unsupported HTTP status code or an unparsable payload).
 */
public class ActionExecutionEndpointException extends ActionExecutionException implements TenantResourceFailure {

    public ActionExecutionEndpointException(String message) {

        super(message);
    }

    public ActionExecutionEndpointException(String message, Throwable cause) {

        super(message, cause);
    }
}
