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

package org.wso2.carbon.identity.debug.framework.listener;

import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkResponse;

/**
 * Interface for debug execution listeners.
 * Follows the same pre/post pattern used in FlowExecutionListener.
 * Listeners are invoked before and after debug request execution.
 */
public interface DebugExecutionListener {

    /**
     * Returns the execution order ID of this listener.
     * Lower values execute first.
     *
     * @return Order ID.
     */
    int getExecutionOrderId();

    /**
     * Returns whether this listener is enabled.
     *
     * @return true if enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Post-execute method called after the debug request is processed.
     *
     * @param debugFrameworkResponse The debug response from execution.
     * @param debugFrameworkRequest  The original debug request.
     * @return true if execution should proceed, false to abort.
     * @throws DebugFrameworkException If an error occurs during post-execution.
     */
    boolean doPostExecute(DebugFrameworkResponse debugFrameworkResponse,
            DebugFrameworkRequest debugFrameworkRequest) throws DebugFrameworkException;
}
