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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for dynamic registration and lookup of debug protocol providers.
 * This is a singleton registry that manages protocol providers for plugin-based protocols.
 */
public class DebugProtocolRegistry {

    private static final Log LOG = LogFactory.getLog(DebugProtocolRegistry.class);
    private static final DebugProtocolRegistry instance = new DebugProtocolRegistry();
    private final Map<String, DebugProtocolProvider> providers = new ConcurrentHashMap<>();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private DebugProtocolRegistry() {

    }

    /**
     * Gets the singleton instance of DebugProtocolRegistry.
     *
     * @return The singleton DebugProtocolRegistry instance.
     */
    public static DebugProtocolRegistry getInstance() {

        return instance;
    }

    /**
     * Registers a debug protocol provider.
     * Called by OSGi components at startup to register protocol providers dynamically.
     *
     * @param protocolKey The unique key for the protocol (e.g., "GOOGLE", "GITHUB").
     * @param provider The DebugProtocolProvider implementation.
     */
    public void register(String protocolKey, DebugProtocolProvider provider) {

        if (protocolKey == null || provider == null) {
            LOG.warn("Cannot register null protocol key or provider");
            return;
        }

        providers.put(protocolKey, provider);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Registered debug protocol provider: " + protocolKey);
        }
    }

    /**
     * Unregisters a debug protocol provider.
     * Called when OSGi components are being deactivated.
     *
     * @param protocolKey The unique key for the protocol.
     */
    public void unregister(String protocolKey) {

        if (protocolKey != null) {
            providers.remove(protocolKey);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unregistered debug protocol provider: " + protocolKey);
            }
        }
    }

    /**
     * Gets a registered protocol provider by protocol key.
     *
     * @param protocolKey The unique key for the protocol.
     * @return The DebugProtocolProvider, or null if not found.
     */
    public DebugProtocolProvider getProvider(String protocolKey) {

        if (protocolKey == null) {
            return null;
        }
        return providers.get(protocolKey);
    }

    /**
     * Checks if a protocol provider is registered.
     *
     * @param protocolKey The unique key for the protocol.
     * @return True if a provider is registered for this key, false otherwise.
     */
    public boolean isProviderRegistered(String protocolKey) {

        return protocolKey != null && providers.containsKey(protocolKey);
    }

    /**
     * Gets all registered protocol provider keys.
     * Useful for logging and debugging.
     *
     * @return A map of all registered protocol keys and their providers.
     */
    public Map<String, DebugProtocolProvider> getAllProviders() {

        return new ConcurrentHashMap<>(providers);
    }

    /**
     * Clears all registered protocol providers.
     * Used in testing or during shutdown.
     */
    public void clear() {
        
        providers.clear();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cleared all registered debug protocol providers");
        }
    }
}
