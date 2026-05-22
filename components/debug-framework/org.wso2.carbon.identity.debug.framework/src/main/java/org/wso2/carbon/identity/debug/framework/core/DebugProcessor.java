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

import org.wso2.carbon.identity.debug.framework.model.DebugContext;

/**
 * Base class for all debug processors.
 * Subclasses implement processing logic appropriate to their resource type and flow.
 */
public abstract class DebugProcessor {

    /**
     * Hook invoked when an unexpected error occurs while processing a debug callback.
     * The base implementation is a no-op; the error is surfaced to the caller, which sends the
     * debug response and rethrows. Subclasses may override to record protocol-specific error state
     * into the context.
     *
     * @param e            The exception that occurred.
     * @param debugContext Debug context for the current debug session.
     */
    protected void handleUnexpectedError(Exception e, DebugContext debugContext) {

        // No-op by default. Subclasses may override to record protocol-specific error state.
    }
}
