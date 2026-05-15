/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.extension;

import org.wso2.carbon.identity.debug.framework.core.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkClientException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkResponse;

/**
 * Interface for handling debug requests for different resource types.
 * Each resource type (IdP, Application, Connector, etc.) should have its own
 * implementation.
 */
public interface DebugResourceHandler {

    /**
     * Handles a debug request for a specific resource type using typed classes.
     * The handler is responsible for:
     * Loading the resource configuration from the database.
     * Detecting the protocol used by the resource.
     * Getting the appropriate protocol provider.
     * Executing the debug flow.
     *
     * @param debugFrameworkRequest The debug request containing resourceId,
     *                     resource type and other parameters.
     * @return DebugFrameworkResponse containing the execution result.
     * @throws DebugFrameworkClientException If the request has validation errors.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    DebugFrameworkResponse handleDebugRequest(DebugFrameworkRequest debugFrameworkRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException;

    /**
     * Retrieves the debug processor for a given resource.
     *
     * @param resourceId The resource ID associated with the callback.
     * @return DebugProcessor instance, or null if not available.
     */
    default DebugProcessor getProcessor(String resourceId) {

        return null;
    }

    /**
     * Retrieves the debug executor for a given resource.
     *
     * @param resourceId The resource ID.
     * @return DebugExecutor instance, or null if not available.
     */
    default DebugExecutor getExecutor(String resourceId) {

        return null;
    }

}
