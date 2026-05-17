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

import org.wso2.carbon.identity.debug.framework.exception.ContextResolutionException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

import java.util.Map;

/**
 * Abstract base class for resolving and creating debug context.
 */
public abstract class DebugContextProvider {

    /**
     * Resolves and creates a debug context from a generic parameter map.
     *
     * @param params Protocol-specific parameters required for context resolution.
     * @return DebugContext containing resolved debug context data.
     * @throws ContextResolutionException If context resolution fails.
     */
    public abstract DebugContext resolveContext(Map<String, Object> params)
            throws ContextResolutionException;

    /**
     * Validates if this provider can handle the given parameter set.
     *
     * @param params Protocol-specific parameters to check.
     * @return true if this provider can handle the parameters, false otherwise.
     */
    public abstract boolean canHandle(Map<String, Object> params);
}
