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
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data holder for Debug Framework Service Component.
 * This holds references to OSGi services that are required by the debug framework.
 */
public class DebugFrameworkServiceDataHolder {

    private static final DebugFrameworkServiceDataHolder instance = new DebugFrameworkServiceDataHolder();

    private ClaimMetadataManagementService claimMetadataManagementService;

    /**
     * Thread-safe map storing protocol providers indexed by protocol type.
     * Key: Protocol type (e.g., "OAUTH2_OIDC", "GOOGLE", "SAML").
     * Value: DebugProtocolProvider implementation.
     */
    private final Map<String, DebugProtocolProvider> debugProtocolProviders = new ConcurrentHashMap<>();

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

        return instance;
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
            String protocolType = provider.getProtocolType();
            debugProtocolProviders.put(protocolType, provider);
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
            String protocolType = provider.getProtocolType();
            debugProtocolProviders.remove(protocolType);
        }
    }

    /**
     * Gets a debug protocol provider by protocol type.
     *
     * @param protocolType The protocol type (e.g., "OAUTH2_OIDC").
     * @return The DebugProtocolProvider for the type, or null if not registered.
     */
    public DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        return debugProtocolProviders.get(protocolType);
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

        return debugProtocolProviders.containsKey(protocolType);
    }

}
