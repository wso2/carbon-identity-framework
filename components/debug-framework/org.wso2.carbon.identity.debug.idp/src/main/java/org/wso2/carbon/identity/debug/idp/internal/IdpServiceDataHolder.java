/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.debug.idp.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data holder for the IDP debug service component.
 * Holds references to OSGi services required by the IDP debug module.
 */
public class IdpServiceDataHolder {

    private static final Log LOG = LogFactory.getLog(IdpServiceDataHolder.class);
    private static final IdpServiceDataHolder instance = new IdpServiceDataHolder();

    private final List<DebugProtocolResolver> debugProtocolResolvers = new ArrayList<>();
    private final Map<String, DebugProtocolProvider> debugProtocolProviders = new HashMap<>();

    private IdpServiceDataHolder() {
    }

    public static IdpServiceDataHolder getInstance() {
        return instance;
    }

    public List<DebugProtocolResolver> getDebugProtocolResolvers() {
        return debugProtocolResolvers;
    }

    public void addDebugProtocolResolver(DebugProtocolResolver resolver) {
        this.debugProtocolResolvers.add(resolver);
    }

    public void removeDebugProtocolResolver(DebugProtocolResolver resolver) {
        this.debugProtocolResolvers.remove(resolver);
    }

    public DebugProtocolProvider getDebugProtocolProvider(String protocolType) {
        return debugProtocolProviders.get(protocolType);
    }

    public void addDebugProtocolProvider(DebugProtocolProvider provider) {
        if (provider.getProtocolType() != null) {
            this.debugProtocolProviders.put(provider.getProtocolType(), provider);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Added DebugProtocolProvider for protocol: " + provider.getProtocolType());
            }
        }
    }

    public void removeDebugProtocolProvider(DebugProtocolProvider provider) {
        if (provider.getProtocolType() != null) {
            this.debugProtocolProviders.remove(provider.getProtocolType());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removed DebugProtocolProvider for protocol: " + provider.getProtocolType());
            }
        }
    }

    /**
     * Gets a debug protocol provider, with fallback to framework's registry if not
     * found locally.
     * This method attempts to get the provider from the local map first, then falls
     * back to
     * querying the framework's service registry through the handler registry.
     *
     * @param protocolType The protocol type identifier.
     * @return DebugProtocolProvider instance or null if not found.
     */
    public DebugProtocolProvider getDebugProtocolProviderWithFallback(String protocolType) {

        // First try local storage (from @Reference bindings).
        DebugProtocolProvider provider = getDebugProtocolProvider(protocolType);
        if (provider != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found DebugProtocolProvider in local storage for: " + protocolType);
            }
            return provider;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugProtocolProvider not found in local storage for: " + protocolType +
                    ". Attempting framework fallback...");
        }

        // Fallback: Try to get from framework's registry through reflection.
        try {
            Class<?> frameworkDataHolderClass = Class.forName(
                    "org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder");
            Object frameworkHolder = frameworkDataHolderClass.getMethod("getInstance").invoke(null);
            Object frameworkProvider = frameworkDataHolderClass
                    .getMethod("getDebugProtocolProvider", String.class)
                    .invoke(frameworkHolder, protocolType);
            if (frameworkProvider instanceof DebugProtocolProvider) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found DebugProtocolProvider in framework registry for: " + protocolType);
                }
                return (DebugProtocolProvider) frameworkProvider;
            }
        } catch (ClassNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Framework DebugFrameworkServiceDataHolder class not found (internal package)");
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error accessing framework protocol provider registry: " + e.getMessage());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("No DebugProtocolProvider found through any mechanism for: " + protocolType);
        }
        return null;
    }
}
