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

import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.core.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;

/**
 * Service interface for protocol-specific debug implementations.
 */
public interface DebugProtocolProvider {

    /**
     * Gets the protocol type identifier for this provider.
     * 
     * Used for logging, identification, and filtering providers.
     *
     * @return Protocol type string (non-null).
     */
    String getProtocolType();

    /**
     * Gets the context provider for this protocol.
     * The context provider resolves debug request parameters into a structured context map.
     *
     * @return DebugContextProvider implementation for this protocol (non-null).
     */
    DebugContextProvider getContextProvider();

    /**
     * Gets the executor for this protocol.
     * The executor generates the initial authorization URL or debug flow entry point.
     *
     * @return DebugExecutor implementation for this protocol (may be null for synchronous protocols).
     */
    DebugExecutor getExecutor();

    /**
     * Gets the processor for this protocol.
     * The processor handles OAuth/OIDC callbacks and token exchange.
     *
     * @return DebugProcessor implementation for this protocol (may be null for non-callback-based protocols).
     */
    DebugProcessor getProcessor();

    /**
     * Determines if this provider can handle the given protocol type.
     * Used for dynamic provider selection.
     *
     * @param protocolType The protocol type to check.
     * @return true if this provider supports the protocol type, false otherwise.
     */
    boolean supports(String protocolType);
}
