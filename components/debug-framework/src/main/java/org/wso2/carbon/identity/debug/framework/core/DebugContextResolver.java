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

package org.wso2.carbon.identity.debug.framework.core;

import org.wso2.carbon.identity.debug.framework.exception.ContextResolutionException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract base class for resolving and creating debug context.
 * Extensions should implement specific context resolution logic for different authentication protocols.
 * Examples: OAuth2ContextResolver, SAMLContextResolver, etc.
 */
public abstract class DebugContextResolver {

    /**
     * Resolves and creates a debug context from the given request.
     * Implementations should validate all required parameters and configurations.
     *
     * @param request HTTP servlet request containing debug parameters.
     * @return Map containing resolved debug context data.
     * @throws ContextResolutionException If context resolution fails.
     */
    public abstract Map<String, Object> resolveContext(HttpServletRequest request) 
            throws ContextResolutionException;

    /**
     * Resolves and creates a debug context with specific parameters.
     * Implementations should validate all required parameters and configurations.
     *
     * @param idpId         Identity Provider resource ID.
     * @param authenticator Optional authenticator name.
     * @return Map containing resolved debug context data.
     * @throws ContextResolutionException If context resolution fails.
     */
    public abstract Map<String, Object> resolveContext(String idpId, String authenticator) 
            throws ContextResolutionException;

    /**
     * Validates if the resolver can handle the given request or IdP configuration.
     * Used to determine which resolver to use in a chain of responsibility pattern.
     *
     * @param idpId Identity Provider ID to check.
     * @return true if this resolver can handle the IdP, false otherwise.
     */
    public abstract boolean canResolve(String idpId);
}
