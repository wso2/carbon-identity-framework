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

package org.wso2.carbon.identity.debug.framework.core.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.extension.DebugResourceHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for dynamic registration and lookup of debug resource handlers.
 * This is a singleton registry that manages resource handlers for different
 * resource types
 * (e.g., IDP, Fraud Detection).
 *
 * This provides a public API for handler registration, accessible by other
 * bundles.
 */
public class DebugHandlerRegistry {

    private static final Log LOG = LogFactory.getLog(DebugHandlerRegistry.class);
    private static final DebugHandlerRegistry instance = new DebugHandlerRegistry();
    private final Map<String, DebugResourceHandler> handlers = new ConcurrentHashMap<>();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private DebugHandlerRegistry() {
    }

    /**
     * Gets the singleton instance of DebugHandlerRegistry.
     *
     * @return The singleton DebugHandlerRegistry instance.
     */
    public static DebugHandlerRegistry getInstance() {
        return instance;
    }

    /**
     * Registers a debug resource handler.
     * Called by OSGi components at startup to register handlers dynamically.
     *
     * @param resourceType The resource type identifier (e.g., "idp").
     * @param handler      The DebugResourceHandler implementation.
     */
    public void register(String resourceType, DebugResourceHandler handler) {
        if (resourceType == null || handler == null) {
            LOG.warn("Cannot register null resource type or handler");
            return;
        }

        handlers.put(resourceType.toLowerCase(), handler);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Registered debug resource handler for type: " + resourceType);
        }
    }

    /**
     * Unregisters a debug resource handler.
     * Called when OSGi components are being deactivated.
     *
     * @param resourceType The resource type identifier.
     */
    public void unregister(String resourceType) {
        if (resourceType != null) {
            handlers.remove(resourceType.toLowerCase());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unregistered debug resource handler for type: " + resourceType);
            }
        }
    }

    /**
     * Gets a registered resource handler by resource type.
     *
     * @param resourceType The resource type identifier.
     * @return The DebugResourceHandler, or null if not found.
     */
    public DebugResourceHandler getHandler(String resourceType) {
        if (resourceType == null) {
            return null;
        }
        return handlers.get(resourceType.toLowerCase());
    }

    /**
     * Checks if a resource handler is registered.
     *
     * @param resourceType The resource type identifier.
     * @return True if a handler is registered for this type, false otherwise.
     */
    public boolean isHandlerRegistered(String resourceType) {
        return resourceType != null && handlers.containsKey(resourceType.toLowerCase());
    }

    /**
     * Gets all registered resource handler types.
     *
     * @return A map of all registered resource types and their handlers.
     */
    public Map<String, DebugResourceHandler> getAllHandlers() {
        return new ConcurrentHashMap<>(handlers);
    }

    /**
     * Clears all registered resource handlers.
     * Used in testing or during shutdown.
     */
    public void clear() {
        handlers.clear();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cleared all registered debug resource handlers");
        }
    }
}
