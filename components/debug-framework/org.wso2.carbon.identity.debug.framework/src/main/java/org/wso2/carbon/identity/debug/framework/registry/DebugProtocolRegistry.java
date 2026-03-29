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

package org.wso2.carbon.identity.debug.framework.registry;

import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Public registry for protocol-related debug extensions.
 */
public class DebugProtocolRegistry {

    private static final DebugProtocolRegistry INSTANCE = new DebugProtocolRegistry();

    private final Map<String, DebugProtocolProvider> debugProtocolProviders = new ConcurrentHashMap<>();
    private final List<DebugCallbackHandler> debugCallbackHandlers = new CopyOnWriteArrayList<>();
    private final List<DebugProtocolResolver> debugProtocolResolvers = new CopyOnWriteArrayList<>();

    private DebugProtocolRegistry() {

    }

    public static DebugProtocolRegistry getInstance() {

        return INSTANCE;
    }

    public void addDebugProtocolProvider(DebugProtocolProvider provider) {

        updateProtocolProvider(provider, true);
    }

    public void removeDebugProtocolProvider(DebugProtocolProvider provider) {

        updateProtocolProvider(provider, false);
    }

    public void addDebugCallbackHandler(DebugCallbackHandler handler) {

        updateListEntry(debugCallbackHandlers, handler, true);
    }

    public void removeDebugCallbackHandler(DebugCallbackHandler handler) {

        updateListEntry(debugCallbackHandlers, handler, false);
    }

    public List<DebugCallbackHandler> getDebugCallbackHandlers() {

        return new CopyOnWriteArrayList<>(debugCallbackHandlers);
    }

    public DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        String normalizedType = normalizeProtocolType(protocolType);
        if (normalizedType == null) {
            return null;
        }
        return debugProtocolProviders.get(normalizedType);
    }

    public Map<String, DebugProtocolProvider> getAllDebugProtocolProviders() {

        return new ConcurrentHashMap<>(debugProtocolProviders);
    }

    public void addDebugProtocolResolver(DebugProtocolResolver resolver) {

        updateSortedListEntry(debugProtocolResolvers, resolver, true,
                Comparator.comparingInt(DebugProtocolResolver::getOrder));
    }

    public void removeDebugProtocolResolver(DebugProtocolResolver resolver) {

        updateListEntry(debugProtocolResolvers, resolver, false);
    }

    public List<DebugProtocolResolver> getDebugProtocolResolvers() {

        return new CopyOnWriteArrayList<>(debugProtocolResolvers);
    }

    private String normalizeProtocolType(String protocolType) {

        if (protocolType == null || protocolType.trim().isEmpty()) {
            return null;
        }
        return protocolType.trim().toLowerCase(Locale.ENGLISH);
    }

    private void updateProtocolProvider(DebugProtocolProvider provider, boolean isAdd) {

        if (provider == null) {
            return;
        }

        String protocolType = normalizeProtocolType(provider.getProtocolType());
        if (protocolType == null) {
            return;
        }

        if (isAdd) {
            debugProtocolProviders.put(protocolType, provider);
        } else {
            debugProtocolProviders.remove(protocolType);
        }

        updateListEntry(debugCallbackHandlers, provider.getCallbackHandler(), isAdd);
    }

    private <T> void updateListEntry(List<T> entries, T entry, boolean isAdd) {

        if (entry == null) {
            return;
        }

        if (isAdd) {
            if (!entries.contains(entry)) {
                entries.add(entry);
            }
            return;
        }

        entries.remove(entry);
    }

    private <T> void updateSortedListEntry(List<T> entries, T entry, boolean isAdd, Comparator<T> comparator) {

        updateListEntry(entries, entry, isAdd);
        if (isAdd) {
            entries.sort(comparator);
        }
    }
}
