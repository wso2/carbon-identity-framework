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

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
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

    @Override
    public DebugContext resolveContext(Map<String, Object> params) throws ContextResolutionException {

        Object connectionIdObj = params.get(IdpDebugConstants.CONNECTION_ID);
        Object resourceTypeObj = params.get(IdpDebugConstants.RESOURCE_TYPE_KEY);
        if (!(connectionIdObj instanceof String) || !(resourceTypeObj instanceof String)) {
            throw new ContextResolutionException(
                    "Invalid or missing parameters: connectionId and resourceType must be non-null Strings.");
        }
        Object idpObj = params.get(IdpDebugConstants.IDENTITY_PROVIDER);
        IdentityProvider identityProvider = idpObj instanceof IdentityProvider ? (IdentityProvider) idpObj : null;
        return resolveContext((String) connectionIdObj, (String) resourceTypeObj, identityProvider);
    }

    /**
     * Resolves context using an IdP connection identifier and a pre-loaded IdP object.
     * The identityProvider may be null if not available from a prior resolution step.
     *
     * @param connectionId     The IdP resource identifier.
     * @param resourceType     The resource type.
     * @param identityProvider The already-loaded IdP object, or null.
     * @return Resolved DebugContext.
     * @throws ContextResolutionException If resolution fails.
     */
    public abstract DebugContext resolveContext(String connectionId, String resourceType,
            IdentityProvider identityProvider) throws ContextResolutionException;
}
