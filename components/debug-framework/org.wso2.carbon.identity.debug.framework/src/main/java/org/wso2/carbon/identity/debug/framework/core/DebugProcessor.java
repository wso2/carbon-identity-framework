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

package org.wso2.carbon.identity.debug.framework.core;

import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

/**
 * Base class for all debug processors.
 * Subclasses implement processing logic appropriate to their resource type and flow.
 */
public abstract class DebugProcessor {

    /**
     * Handles unexpected errors by recording error state into the debug context.
     *
     * @param e            The exception that occurred.
     * @param debugContext Debug context for storing error information.
     */
    protected void handleUnexpectedError(Exception e, DebugContext debugContext) {

        debugContext.setProperty(DebugFrameworkConstants.DEBUG_PROTOCOL_CODE, null);
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_PROTOCOL_STATE, null);
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_AUTH_ERROR, "Unexpected error: " + e.getMessage());
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_AUTH_SUCCESS, DebugFrameworkConstants.FALSE_VALUE);
    }
}
