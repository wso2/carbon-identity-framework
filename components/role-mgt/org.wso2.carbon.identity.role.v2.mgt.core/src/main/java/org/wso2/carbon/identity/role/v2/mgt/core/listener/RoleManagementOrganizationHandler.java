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

package org.wso2.carbon.identity.role.v2.mgt.core.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.util.RoleManagementUtils;

import java.util.Map;

/**
 * Event handler for organization management operations to clear role basic info cache.
 * This is necessary because RoleBasicInfo contains audienceName (organization name)
 * and when organization name changes or organization is deleted, cached role info becomes stale.
 */
public class RoleManagementOrganizationHandler extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(RoleManagementOrganizationHandler.class);
    private static final String EVENT_POST_UPDATE_ORGANIZATION = "POST_UPDATE_ORGANIZATION";
    private static final String EVENT_POST_PATCH_ORGANIZATION = "POST_PATCH_ORGANIZATION";
    private static final String EVENT_PRE_DELETE_ORGANIZATION = "PRE_DELETE_ORGANIZATION";
    private static final String EVENT_PROP_ORGANIZATION_ID = "ORGANIZATION_ID";

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        String eventName = event.getEventName();
        Map<String, Object> eventProperties = event.getEventProperties();

        switch (eventName) {
            case EVENT_POST_UPDATE_ORGANIZATION:
            case EVENT_POST_PATCH_ORGANIZATION:
            case EVENT_PRE_DELETE_ORGANIZATION:
                clearRoleBasicInfoCache(eventProperties);
                break;
            default:
                break;
        }
    }

    /**
     * Clear role basic info cache for organization.
     * This is necessary because RoleBasicInfo contains audienceName (organization name)
     * and when organization name changes or organization is deleted, cached role info becomes stale.
     *
     * @param eventProperties Event properties containing organization ID.
     */
    private void clearRoleBasicInfoCache(Map<String, Object> eventProperties) {

        String organizationId = (String) eventProperties.get(EVENT_PROP_ORGANIZATION_ID);
        if (StringUtils.isBlank(organizationId)) {
            LOG.warn("Organization ID is missing in event properties. Cannot clear role basic info cache.");
            return;
        }
        try {
            OrganizationManager organizationManager = RoleManagementServiceComponentHolder.getInstance()
                    .getOrganizationManager();
            String tenantDomain = organizationManager.resolveTenantDomain(organizationId);
            RoleManagementUtils.clearRoleBasicInfoCacheByTenant(tenantDomain);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Cleared role basic info cache for tenant: " + tenantDomain +
                        " for organization: " + organizationId);
            }
        } catch (OrganizationManagementException e) {
            LOG.error("Error clearing role basic info cache for organization: " + organizationId, e);
        }
    }

    @Override
    public String getName() {

        return "RoleManagementOrganizationHandler";
    }
}
