/*
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

/**
 * Interface to resolve the debug protocol for a given resource.
 * Implementations of this interface can be registered as OSGi services to
 * dynamically contribute protocol resolution logic.
 */
public interface DebugProtocolResolver {

    /**
     * Resolves the debug protocol for the given resource ID.
     *
     * @param resourceId The resource ID to resolve the protocol for.
     * @return The resolved protocol type (e.g., "OAUTH2_OIDC", "SAML"), or null if not resolved.
     */
    String resolveProtocol(String resourceId);

    /**
     * Gets the order of execution for this resolver.
     * Resolvers are executed in ascending order of this value.
     *
     * @return The order of execution.
     */
    int getOrder();
}
