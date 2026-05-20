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

package org.wso2.carbon.identity.debug.idp.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.framework.registry.DebugProtocolRegistry;

import java.util.List;
import java.util.Locale;

/**
 * Routes IdP debug requests to protocol-specific components.
 */
public final class IdpDebugProtocolRouter {

    private static final Log LOG = LogFactory.getLog(IdpDebugProtocolRouter.class);

    private IdpDebugProtocolRouter() {

        // Utility class.
    }

    /**
     * Resolves the protocol provider for the specified connection.
     *
     * @param connectionId The connection ID.
     * @return DebugProtocolProvider instance, or null if not available.
     */
    public static DebugProtocolProvider resolveProvider(String connectionId) {

        String protocolType = resolveProtocolType(connectionId);
        if (StringUtils.isBlank(protocolType)) {
            return null;
        }

        DebugProtocolProvider provider = getDebugProtocolProvider(protocolType);
        if (provider != null && LOG.isDebugEnabled()) {
            LOG.debug("Resolved protocol provider for resource: " + connectionId);
        }
        return provider;
    }

    private static DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        if (StringUtils.isEmpty(protocolType)) {
            return null;
        }

        String normalizedType = normalizeProtocolType(protocolType);
        DebugProtocolProvider provider = DebugProtocolRegistry.getInstance()
                .getDebugProtocolProvider(normalizedType);

        if (LOG.isDebugEnabled()) {
            if (provider != null) {
                LOG.debug("Retrieved protocol provider for type: " + normalizedType);
            } else {
                LOG.debug("Protocol provider not found for type: " + normalizedType);
            }
        }
        return provider;
    }

    private static String resolveProtocolType(String connectionId) {

        if (StringUtils.isBlank(connectionId)) {
            LOG.warn("Connection ID is blank — cannot resolve protocol. "
                    + "Ensure a valid connection ID is provided.");
            return null;
        }

        List<DebugProtocolResolver> resolvers = DebugProtocolRegistry.getInstance().getDebugProtocolResolvers();
        for (DebugProtocolResolver resolver : resolvers) {
            String resolvedProtocol = resolver.resolveProtocol(connectionId);
            if (StringUtils.isNotBlank(resolvedProtocol)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Protocol resolved by " + resolver.getClass().getSimpleName()
                            + ": " + resolvedProtocol);
                }
                return resolvedProtocol;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("No protocol resolved for resource: " + connectionId
                    + ". Ensure a matching protocol resolver is deployed and active.");
        }
        return null;
    }

    private static String normalizeProtocolType(String protocolType) {

        if (protocolType == null) {
            return null;
        }
        return protocolType.trim().toLowerCase(Locale.ROOT);
    }
}
