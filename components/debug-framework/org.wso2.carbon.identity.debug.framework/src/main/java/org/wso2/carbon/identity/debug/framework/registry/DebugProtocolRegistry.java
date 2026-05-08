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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Public registry for protocol-related debug extensions.
 * This registry maintains providers, callback handlers, and resolvers for different protocols.
 */
public class DebugProtocolRegistry {

    /**
     * Singleton instance of {@link DebugProtocolRegistry}.
     */
    private static final DebugProtocolRegistry INSTANCE = new DebugProtocolRegistry();

    /**
     * Map of protocol type to its corresponding {@link DebugProtocolProvider}.
     */
    private final Map<String, DebugProtocolProvider> debugProtocolProviders = new ConcurrentHashMap<>();

    /**
     * List of registered {@link DebugCallbackHandler}s.
     */
    private final List<DebugCallbackHandler> debugCallbackHandlers = new CopyOnWriteArrayList<>();

    /**
     * List of registered {@link DebugProtocolResolver}s.
     */
    private final List<DebugProtocolResolver> debugProtocolResolvers = new ArrayList<>();

    /**
     * Lock object for synchronizing access to debugProtocolResolvers.
     */
    private final Object debugProtocolResolversLock = new Object();

    /**
     * Private constructor to prevent instantiation.
     */
    private DebugProtocolRegistry() {

    }

    /**
     * Returns the singleton instance of {@link DebugProtocolRegistry}.
     *
     * @return {@link DebugProtocolRegistry} instance.
     */
    public static DebugProtocolRegistry getInstance() {

        return INSTANCE;
    }

    /**
     * Registers a {@link DebugProtocolProvider}.
     *
     * @param provider {@link DebugProtocolProvider} to be registered.
     */
    public void addDebugProtocolProvider(DebugProtocolProvider provider) {

        updateProtocolProvider(provider, true);
    }

    /**
     * Unregisters a {@link DebugProtocolProvider}.
     *
     * @param provider {@link DebugProtocolProvider} to be unregistered.
     */
    public void removeDebugProtocolProvider(DebugProtocolProvider provider) {

        updateProtocolProvider(provider, false);
    }

    /**
     * Registers a {@link DebugCallbackHandler}.
     *
     * @param handler {@link DebugCallbackHandler} to be registered.
     */
    public void addDebugCallbackHandler(DebugCallbackHandler handler) {

        updateListEntry(debugCallbackHandlers, handler, true);
    }

    /**
     * Unregisters a {@link DebugCallbackHandler}.
     *
     * @param handler {@link DebugCallbackHandler} to be unregistered.
     */
    public void removeDebugCallbackHandler(DebugCallbackHandler handler) {

        updateListEntry(debugCallbackHandlers, handler, false);
    }

    /**
     * Returns the list of registered {@link DebugCallbackHandler}s.
     *
     * @return List of {@link DebugCallbackHandler}s.
     */
    public List<DebugCallbackHandler> getDebugCallbackHandlers() {

        return new ArrayList<>(debugCallbackHandlers);
    }

    /**
     * Returns the {@link DebugProtocolProvider} for the given protocol type.
     *
     * @param protocolType Type of the protocol.
     * @return {@link DebugProtocolProvider} if found, null otherwise.
     */
    public DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        String normalizedType = normalizeProtocolType(protocolType);
        if (normalizedType == null) {
            return null;
        }
        return debugProtocolProviders.get(normalizedType);
    }

    /**
     * Registers a {@link DebugProtocolResolver}.
     *
     * @param resolver {@link DebugProtocolResolver} to be registered.
     */
    public void addDebugProtocolResolver(DebugProtocolResolver resolver) {

        synchronized (debugProtocolResolversLock) {
            updateSortedListEntry(debugProtocolResolvers, resolver, true,
                    Comparator.comparingInt(DebugProtocolResolver::getOrder));
        }
    }

    /**
     * Unregisters a {@link DebugProtocolResolver}.
     *
     * @param resolver {@link DebugProtocolResolver} to be unregistered.
     */
    public void removeDebugProtocolResolver(DebugProtocolResolver resolver) {

        synchronized (debugProtocolResolversLock) {
            updateListEntry(debugProtocolResolvers, resolver, false);
        }
    }

    /**
     * Returns the list of registered {@link DebugProtocolResolver}s.
     *
     * @return List of {@link DebugProtocolResolver}s.
     */
    public List<DebugProtocolResolver> getDebugProtocolResolvers() {

        synchronized (debugProtocolResolversLock) {
            return new ArrayList<>(debugProtocolResolvers);
        }
    }

    /**
     * Normalizes the protocol type by trimming and converting to lowercase.
     *
     * @param protocolType Protocol type to be normalized.
     * @return Normalized protocol type.
     */
    private String normalizeProtocolType(String protocolType) {

        if (protocolType == null || protocolType.trim().isEmpty()) {
            return null;
        }
        return protocolType.trim().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Updates the protocol provider registry.
     *
     * @param provider {@link DebugProtocolProvider} to be updated.
     * @param isAdd    True if adding, false if removing.
     */
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

    /**
     * Updates a list with the given entry.
     * This prevents multiple instances of the same handler class from being registered
     * when handlers are registered both directly and via protocol providers.
     *
     * @param entries List of entries.
     * @param entry   Entry to be updated.
     * @param isAdd   True if adding, false if removing.
     * @param <T>     Type of the entry.
     */
    private <T> void updateListEntry(List<T> entries, T entry, boolean isAdd) {

        if (entry == null) {
            return;
        }

        if (isAdd) {
            // Check if the exact instance is already registered.
            boolean alreadyExists = entries.contains(entry);

            if (!alreadyExists) {
                entries.add(entry);
            }
            return;
        }

        // When removing, remove by instance to ensure only the intended entry is removed.
        // This prevents accidental removal of other instances of the same class.
        entries.remove(entry);
    }

    /**
     * Updates a sorted list with the given entry.
     *
     * @param entries    List of entries.
     * @param entry      Entry to be updated.
     * @param isAdd      True if adding, false if removing.
     * @param comparator Comparator for sorting.
     * @param <T>        Type of the entry.
     */
    private <T> void updateSortedListEntry(List<T> entries, T entry, boolean isAdd, Comparator<T> comparator) {

        updateListEntry(entries, entry, isAdd);
        if (isAdd) {
            entries.sort(comparator);
        }
    }
}
