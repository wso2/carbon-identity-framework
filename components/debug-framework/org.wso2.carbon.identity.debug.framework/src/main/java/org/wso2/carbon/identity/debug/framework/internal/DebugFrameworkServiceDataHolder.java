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

package org.wso2.carbon.identity.debug.framework.internal;

import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.framework.listener.DebugExecutionListener;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data holder for Debug Framework Service Component.
 * This holds references to OSGi services that are required by the debug framework.
 */
public class DebugFrameworkServiceDataHolder {

    private ClaimMetadataManagementService claimMetadataManagementService;

    /**
     * List of registered debug execution listeners.
     */
    private final List<DebugExecutionListener> debugExecutionListeners = new CopyOnWriteArrayList<>();

    /**
     * Thread-safe map storing protocol providers indexed by protocol type.
     */
    private final Map<String, DebugProtocolProvider> debugProtocolProviders = new ConcurrentHashMap<>();

    /**
     * List of registered debug callback handlers.
     */
    private final List<DebugCallbackHandler> debugCallbackHandlers = new CopyOnWriteArrayList<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private DebugFrameworkServiceDataHolder() {

    }

    /**
     * Returns the singleton instance of the data holder.
     *
     * @return the singleton instance.
     */
    public static DebugFrameworkServiceDataHolder getInstance() {

        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {

        private static final DebugFrameworkServiceDataHolder INSTANCE =
                new DebugFrameworkServiceDataHolder();
    }

    /**
     * Gets the ClaimMetadataManagementService.
     *
     * @return the ClaimMetadataManagementService instance.
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        return claimMetadataManagementService;
    }

    /**
     * Sets the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService the ClaimMetadataManagementService instance.
     */
    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }

    /**
     * Adds a debug protocol provider to the registry.
     * Called during OSGi service binding when a plugin registers a DebugProtocolProvider.
     *
     * @param provider The DebugProtocolProvider to register.
     */
    public void addDebugProtocolProvider(DebugProtocolProvider provider) {

        if (provider != null) {
            String protocolType = normalizeProtocolType(provider.getProtocolType());
            if (protocolType != null) {
                debugProtocolProviders.put(protocolType, provider);
                DebugCallbackHandler callbackHandler = provider.getCallbackHandler();
                if (callbackHandler != null) {
                    debugCallbackHandlers.add(callbackHandler);
                }
            }
        }
    }

    /**
     * Removes a debug protocol provider from the registry.
     * Called during OSGi service unbinding when a plugin deactivates.
     *
     * @param provider The DebugProtocolProvider to unregister.
     */
    public void removeDebugProtocolProvider(DebugProtocolProvider provider) {

        if (provider != null) {
            String protocolType = normalizeProtocolType(provider.getProtocolType());
            if (protocolType != null) {
                debugProtocolProviders.remove(protocolType);
                DebugCallbackHandler callbackHandler = provider.getCallbackHandler();
                if (callbackHandler != null) {
                    debugCallbackHandlers.remove(callbackHandler);
                }
            }
        }
    }

    /**
     * Adds a debug callback handler.
     *
     * @param handler DebugCallbackHandler instance.
     */
    public void addDebugCallbackHandler(DebugCallbackHandler handler) {

        if (handler != null) {
            debugCallbackHandlers.add(handler);
        }
    }

    /**
     * Removes a debug callback handler.
     *
     * @param handler DebugCallbackHandler instance.
     */
    public void removeDebugCallbackHandler(DebugCallbackHandler handler) {

        if (handler != null) {
            debugCallbackHandlers.remove(handler);
        }
    }

    /**
     * Returns a snapshot of registered callback handlers.
     *
     * @return List of DebugCallbackHandler.
     */
    public List<DebugCallbackHandler> getDebugCallbackHandlers() {

        return new CopyOnWriteArrayList<>(debugCallbackHandlers);
    }

    /**
     * Gets a debug protocol provider by protocol type.
     *
     * @param protocolType The protocol type.
     * @return The DebugProtocolProvider for the type, or null if not registered.
     */
    public DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        String normalizedType = normalizeProtocolType(protocolType);
        if (normalizedType == null) {
            return null;
        }
        return debugProtocolProviders.get(normalizedType);
    }

    /**
     * Gets all registered debug protocol providers.
     *
     * @return Map of protocol type to DebugProtocolProvider.
     */
    public Map<String, DebugProtocolProvider> getAllDebugProtocolProviders() {

        return new ConcurrentHashMap<>(debugProtocolProviders);
    }

    /**
     * Checks if a protocol provider is registered for the given type.
     *
     * @param protocolType The protocol type to check.
     * @return true if a provider is registered, false otherwise.
     */
    public boolean hasDebugProtocolProvider(String protocolType) {

        String normalizedType = normalizeProtocolType(protocolType);
        if (normalizedType == null) {
            return false;
        }
        return debugProtocolProviders.containsKey(normalizedType);
    }

    /**
     * List of registered debug protocol resolvers.
     */
    private final List<DebugProtocolResolver> debugProtocolResolvers = 
            new CopyOnWriteArrayList<>();

    /**
     * Adds a debug protocol resolver to the registry.
     * Called during OSGi service binding.
     *
     * @param resolver The DebugProtocolResolver to register.
     */
    public void addDebugProtocolResolver(DebugProtocolResolver resolver) {

        if (resolver != null) {
            debugProtocolResolvers.add(resolver);
            debugProtocolResolvers.sort(Comparator.comparingInt(DebugProtocolResolver::getOrder));
        }
    }

    /**
     * Removes a debug protocol resolver from the registry.
     * Called during OSGi service unbinding.
     *
     * @param resolver The DebugProtocolResolver to unregister.
     */
    public void removeDebugProtocolResolver(DebugProtocolResolver resolver) {

        if (resolver != null) {
            debugProtocolResolvers.remove(resolver);
        }
    }

    /**
     * Gets all registered debug protocol resolvers.
     *
     * @return List of DebugProtocolResolver.
     */
    public List<DebugProtocolResolver> getDebugProtocolResolvers() {

        return debugProtocolResolvers;
    }

    /**
     * Gets the list of debug execution listeners.
     *
     * @return List of debug execution listeners.
     */
    public List<DebugExecutionListener> getDebugExecutionListeners() {

        return debugExecutionListeners;
    }

    /**
     * Adds a debug execution listener.
     *
     * @param listener The debug execution listener to add.
     */
    public void addDebugExecutionListener(DebugExecutionListener listener) {

        if (listener != null) {
            this.debugExecutionListeners.add(listener);
            this.debugExecutionListeners.sort(Comparator.comparingInt(DebugExecutionListener::getExecutionOrderId));
        }
    }

    /**
     * Removes a debug execution listener.
     *
     * @param listener The debug execution listener to remove.
     */
    public void removeDebugExecutionListener(DebugExecutionListener listener) {

        if (listener != null) {
            this.debugExecutionListeners.remove(listener);
        }
    }

    private String normalizeProtocolType(String protocolType) {

        if (protocolType == null || protocolType.trim().isEmpty()) {
            return null;
        }
        return protocolType.trim().toLowerCase(Locale.ENGLISH);
    }

}
