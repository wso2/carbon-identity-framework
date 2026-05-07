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

package org.wso2.carbon.identity.debug.idp.core;

import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.exception.ContextResolutionException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

import java.util.Map;

/**
 * Abstract base for IdP-specific context providers.
 * Bridges the generic framework's Map-based API to the
 * IdP-specific connectionId concept.
 */
public abstract class IdpDebugContextProvider extends DebugContextProvider {

    public static final String RESOURCE_TYPE_KEY = "resourceType";

    @Override
    public DebugContext resolveContext(Map<String, Object> params)
            throws ContextResolutionException {
        Object rawConnectionId = params.get(IdpDebugConstants.CONNECTION_ID);
        Object rawResourceType = params.get(RESOURCE_TYPE_KEY);
        if (!(rawConnectionId instanceof String) || !(rawResourceType instanceof String)) {
            throw new ContextResolutionException(
                    "Invalid or missing parameters: connectionId and resourceType must be non-null Strings.");
        }
        String connectionId = (String) rawConnectionId;
        String resourceType = (String) rawResourceType;
        return resolveContext(connectionId, resourceType);
    }

    @Override
    public boolean canResolve(Map<String, Object> params) {
        String connectionId = (String) params.get(IdpDebugConstants.CONNECTION_ID);
        return canResolve(connectionId);
    }

    /**
     * Resolves context using an IdP connection identifier.
     *
     * @param connectionId The IdP resource identifier.
     * @param resourceType The resource type.
     * @return Resolved DebugContext.
     * @throws ContextResolutionException If resolution fails.
     */
    public abstract DebugContext resolveContext(String connectionId, String resourceType)
            throws ContextResolutionException;

    /**
     * Returns whether this provider can resolve the given IdP connection.
     *
     * @param connectionId The IdP resource identifier.
     * @return true if resolvable.
     */
    public abstract boolean canResolve(String connectionId);
}
