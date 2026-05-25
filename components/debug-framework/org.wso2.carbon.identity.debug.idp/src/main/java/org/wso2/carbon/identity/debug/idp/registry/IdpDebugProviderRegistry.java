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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for IdP debug protocol providers.
 */
public class IdpDebugProviderRegistry {

    private static final IdpDebugProviderRegistry INSTANCE = new IdpDebugProviderRegistry();

    private final Map<String, DebugTypeProvider> providers = new ConcurrentHashMap<>();

    private IdpDebugProviderRegistry() {

    }

    public static IdpDebugProviderRegistry getInstance() {

        return INSTANCE;
    }

    public void addProvider(DebugTypeProvider provider) {

        providers.put(provider.getProtocolType(), provider);
    }

    public void removeProvider(DebugTypeProvider provider) {

        providers.remove(provider.getProtocolType());
    }

    public DebugTypeProvider getProvider(String protocolType) {

        return providers.get(protocolType);
    }
}
