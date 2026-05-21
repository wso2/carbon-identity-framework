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

import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for IdP debug protocol providers.
 */
public class IdpDebugProviderRegistry {

    private static final IdpDebugProviderRegistry INSTANCE = new IdpDebugProviderRegistry();

    private final Map<String, DebugProtocolProvider> providers = new ConcurrentHashMap<>();

    private IdpDebugProviderRegistry() {

    }

    public static IdpDebugProviderRegistry getInstance() {

        return INSTANCE;
    }

    public void addProvider(DebugProtocolProvider provider) {

        if (provider == null) {
            return;
        }
        String key = normalize(provider.getProtocolType());
        if (key != null) {
            providers.put(key, provider);
        }
    }

    public void removeProvider(DebugProtocolProvider provider) {

        if (provider == null) {
            return;
        }
        String key = normalize(provider.getProtocolType());
        if (key != null) {
            providers.remove(key);
        }
    }

    public DebugProtocolProvider getProvider(String protocolType) {

        String key = normalize(protocolType);
        if (key == null) {
            return null;
        }
        return providers.get(key);
    }

    private String normalize(String protocolType) {

        if (protocolType == null || protocolType.trim().isEmpty()) {
            return null;
        }
        return protocolType.trim().toLowerCase(Locale.ROOT);
    }
}
