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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtClientException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtRuntimeException;

/**
 * Utility class for Async Status Management.
 */
public class AsyncOperationStatusMgtExceptionHandler {

    private AsyncOperationStatusMgtExceptionHandler() {
    }

    /**
     * Throw an OrganizationManagementClientException upon client side error in organization management.
     *
     * @param error The error enum.
     * @param data  The error message data.
     * @return OrganizationManagementClientException
     */
    public static AsyncOperationStatusMgtClientException handleClientException(ErrorMessage error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new AsyncOperationStatusMgtClientException(error.getMessage(), description, error.getCode());
    }

    /**
     * Throw an AsyncStatusMgtServerException upon server side error in organization management.
     *
     * @param error The error enum.
     * @param e     The error.
     * @param data  The error message data.
     * @return AsyncStatusMgtServerException
     */
    public static AsyncOperationStatusMgtServerException handleServerException(
            ErrorMessage error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new AsyncOperationStatusMgtServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Throw Async Operation Status Management runtime exception.
     *
     * @param errorMessage Error message.
     * @param e            Throwable.
     * @throws AsyncOperationStatusMgtRuntimeException If an error occurs from the server in the runtime.
     */
    public static void throwRuntimeException(String errorMessage, Throwable e)
            throws AsyncOperationStatusMgtRuntimeException {

        throw new AsyncOperationStatusMgtRuntimeException(errorMessage, e);
    }
}
