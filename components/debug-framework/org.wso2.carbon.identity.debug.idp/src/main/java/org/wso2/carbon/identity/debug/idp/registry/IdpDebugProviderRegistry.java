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

package org.wso2.carbon.identity.debug.idp.registry;

import org.wso2.carbon.identity.debug.framework.extension.DebugTypeProvider;
import org.wso2.carbon.identity.debug.idp.extension.IdpDebugTypeProvider;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for IdP debug type providers.
 *
 * <p>Providers are stored in a list rather than a name-keyed map. Resolution is done by
 * iterating the list and delegating to each provider's
 * {@link IdpDebugTypeProvider#supportsAuthenticator(String)} predicate.
 */
public class IdpDebugProviderRegistry {

    private static final IdpDebugProviderRegistry INSTANCE = new IdpDebugProviderRegistry();

    private final CopyOnWriteArrayList<IdpDebugTypeProvider> providers = new CopyOnWriteArrayList<>();

    private IdpDebugProviderRegistry() {

    }

    public static IdpDebugProviderRegistry getInstance() {

        return INSTANCE;
    }

    public void addProvider(IdpDebugTypeProvider provider) {

        providers.addIfAbsent(provider);
    }

    public void removeProvider(IdpDebugTypeProvider provider) {

        providers.remove(provider);
    }

    /**
     * Finds the first registered provider that supports the given authenticator name.
     *
     * @param authenticatorName the authenticator name from the IdP's federated authenticator config.
     * @return the matching {@link DebugTypeProvider}, or {@code null} if none found.
     */
    public DebugTypeProvider resolve(String authenticatorName) {

        return providers.stream()
                .filter(p -> p.supportsAuthenticator(authenticatorName))
                .findFirst()
                .orElse(null);
    }
}
