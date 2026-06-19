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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedAPICache;
import org.wso2.carbon.identity.application.mgt.cache.AuthorizedScopesCache;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;

import java.util.Map;

/**
 * Event handler that invalidates the authorized-scopes (and authorized-API) caches of an application when API
 * resources or their scopes are mutated in the API Resource Management component. Those mutations do not flow
 * through {@code CacheBackedAuthorizedAPIDAOImpl}, so without this handler a cached authorized-scopes entry could
 * become stale after an API resource / scope is deleted or its scopes are changed.
 * <p>
 * The events carry the API resource id and tenant domain (not the affected application ids), so the handler flushes
 * the caches for the whole tenant. API resource mutations are infrequent admin operations, whereas the cache
 * optimizes the high-throughput token-issuance read path.
 */
public class AuthorizedScopesCacheInvalidationHandler extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(AuthorizedScopesCacheInvalidationHandler.class);

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        String eventName = event.getEventName();
        switch (eventName) {
            case IdentityEventConstants.Event.POST_DELETE_API_RESOURCE:
            case IdentityEventConstants.Event.POST_DELETE_API_RESOURCE_SCOPES:
            case IdentityEventConstants.Event.POST_DELETE_SCOPE:
            case IdentityEventConstants.Event.POST_PUT_API_RESOURCE_SCOPES:
            case IdentityEventConstants.Event.POST_UPDATE_API_RESOURCE:
                clearCachesForTenant(event.getEventProperties());
                break;
            default:
                break;
        }
    }

    private void clearCachesForTenant(Map<String, Object> eventProperties) {

        if (eventProperties == null) {
            LOG.warn("Event properties are null. Cannot clear authorized scopes cache.");
            return;
        }
        Object tenantDomain = eventProperties.get(IdentityEventConstants.EventProperty.TENANT_DOMAIN);
        if (!(tenantDomain instanceof String) || StringUtils.isBlank((String) tenantDomain)) {
            LOG.warn("Tenant domain is missing in event properties. Cannot clear authorized scopes cache.");
            return;
        }
        AuthorizedScopesCache.getInstance().clear((String) tenantDomain);
        AuthorizedAPICache.getInstance().clear((String) tenantDomain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cleared authorized scopes and authorized API caches for tenant domain: " + tenantDomain
                    + " due to an API resource change.");
        }
    }

    @Override
    public String getName() {

        return "authorizedScopesCacheInvalidationHandler";
    }
}
