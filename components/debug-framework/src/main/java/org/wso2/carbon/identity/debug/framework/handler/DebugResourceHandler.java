/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.handler;

import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;

import java.util.Map;

/**
 * Interface for handling debug requests for different resource types.
 * Implementations handle debugging for specific resource types (IDP, APPLICATION, CONNECTOR, etc.).
 * Each handler is responsible for:
 * 1. Determining if it can handle a specific resource type
 * 2. Processing the debug request with provided resourceId, resourceType, and properties
 * 3. Returning the debug result
 */
public interface DebugResourceHandler {

    /**
     * Determines if this handler can process requests for the given resource type.
     * 
     * @param resourceType The resource type to check (e.g., "IDP", "APPLICATION", "CONNECTOR").
     * @return true if this handler can process the resource type, false otherwise.
     */
    boolean canHandle(String resourceType);

    /**
     * Handles a debug request for the resource type this handler supports.
     * 
     * @param debugRequestContext Map containing:
     *        - resourceId: The identifier of the resource to debug (required).
     *        - resourceType: The type of resource (e.g., "IDP", "APPLICATION", "CONNECTOR") (required).
     *        - properties: Optional key-value properties for the debug request (Map<String, String>).
     *        Additional properties may include protocol-specific parameters.
     * @return Map containing debug result data, including:
     *         - authorization_url: The generated authorization URL (for IDP resources).
     *         - sessionId: Debug session identifier.
     *         - timestamp: Request processing timestamp.
     *         - status: Status of the debug request (SUCCESS or ERROR).
     * @throws DebugFrameworkException If debug processing fails.
     */
    Map<String, Object> handleDebugRequest(Map<String, Object> debugRequestContext) throws DebugFrameworkException;

    /**
     * Returns the name of this handler for logging and identification purposes.
     * 
     * @return A descriptive name of this handler (e.g., "IdpDebugResourceHandler").
     */
    String getName();
}
