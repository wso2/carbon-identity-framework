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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for ConfigurationManager to manage JS execution alert configurations per tenant.
 * This class handles storing and retrieving tenant violation counts and blocked status.
 */
public class TenantViolationStore {

    private static final Log LOG = LogFactory.getLog(TenantViolationStore.class);

    // Resource type for JS execution alerts.
    private static final String RESOURCE_TYPE_NAME = "JSExecutionAlerts";

    // Attribute keys.
    private static final String ATTR_VIOLATION_COUNT = "violationCount";
    private static final String ATTR_BLOCKED = "blocked";
    private static final String ATTR_BLOCK_TIMESTAMP = "blockTimestamp";
    private static final String ATTR_LAST_VIOLATION_TIMESTAMP = "lastViolationTimestamp";

    /**
     * Get the violation count for a tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return The violation count, or 0 if not found.
     */
    public int getViolationCount(String tenantDomain) {

        try {
            Resource resource = getConfigurationManager().getResource(RESOURCE_TYPE_NAME, tenantDomain, false);
            if (resource != null && resource.getAttributes() != null) {
                for (Attribute attribute : resource.getAttributes()) {
                    if (ATTR_VIOLATION_COUNT.equals(attribute.getKey())) {
                        return Integer.parseInt(attribute.getValue());
                    }
                }
            }
        } catch (ConfigurationManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Failed to retrieve violation count for tenant: %s", tenantDomain), e);
            }
        } catch (NumberFormatException e) {
            LOG.error(String.format("Invalid violation count format for tenant: %s", tenantDomain), e);
        }
        return 0;
    }

    /**
     * Update the violation count for a tenant.
     *
     * @param tenantDomain   The tenant domain.
     * @param violationCount The new violation count.
     */
    public void updateViolationCount(String tenantDomain, int violationCount) {

        try {
            List<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute(ATTR_VIOLATION_COUNT, String.valueOf(violationCount)));
            attributes.add(new Attribute(ATTR_LAST_VIOLATION_TIMESTAMP, String.valueOf(System.currentTimeMillis())));

            // Preserve existing blocked status.
            boolean isBlocked = isBlocked(tenantDomain);
            long blockTimestamp = getBlockTimestamp(tenantDomain);
            attributes.add(new Attribute(ATTR_BLOCKED, String.valueOf(isBlocked)));
            if (isBlocked) {
                attributes.add(new Attribute(ATTR_BLOCK_TIMESTAMP, String.valueOf(blockTimestamp)));
            }

            ResourceAdd resourceAdd = new ResourceAdd();
            resourceAdd.setName(tenantDomain);
            resourceAdd.setAttributes(attributes);

            getConfigurationManager().replaceResource(RESOURCE_TYPE_NAME, resourceAdd);

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Updated violation count for tenant %s: %d", tenantDomain, violationCount));
            }
        } catch (ConfigurationManagementException e) {
            LOG.error(String.format("Failed to update violation count for tenant: %s", tenantDomain), e);
        }
    }

    /**
     * Check if a tenant is blocked.
     *
     * @param tenantDomain The tenant domain.
     * @return True if the tenant is blocked, false otherwise.
     */
    public boolean isBlocked(String tenantDomain) {

        try {
            Resource resource = getConfigurationManager().getResource(RESOURCE_TYPE_NAME, tenantDomain, false);
            if (resource != null && resource.getAttributes() != null) {
                for (Attribute attribute : resource.getAttributes()) {
                    if (ATTR_BLOCKED.equals(attribute.getKey())) {
                        return Boolean.parseBoolean(attribute.getValue());
                    }
                }
            }
        } catch (ConfigurationManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Failed to retrieve blocked status for tenant: %s", tenantDomain), e);
            }
        }
        return false;
    }

    /**
     * Block a tenant.
     *
     * @param tenantDomain The tenant domain.
     */
    public void blockTenant(String tenantDomain) {

        try {
            List<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute(ATTR_BLOCKED, String.valueOf(true)));
            attributes.add(new Attribute(ATTR_BLOCK_TIMESTAMP, String.valueOf(System.currentTimeMillis())));

            // Preserve existing violation count.
            int violationCount = getViolationCount(tenantDomain);
            attributes.add(new Attribute(ATTR_VIOLATION_COUNT, String.valueOf(violationCount)));

            long lastViolationTimestamp = getLastViolationTimestamp(tenantDomain);
            if (lastViolationTimestamp > 0) {
                attributes.add(new Attribute(ATTR_LAST_VIOLATION_TIMESTAMP, String.valueOf(lastViolationTimestamp)));
            }

            ResourceAdd resourceAdd = new ResourceAdd();
            resourceAdd.setName(tenantDomain);
            resourceAdd.setAttributes(attributes);

            getConfigurationManager().replaceResource(RESOURCE_TYPE_NAME, resourceAdd);

            LOG.warn(String.format("Tenant '%s' has been blocked via configuration management.", tenantDomain));
        } catch (ConfigurationManagementException e) {
            LOG.error(String.format("Failed to block tenant: %s", tenantDomain), e);
        }
    }

    /**
     * Unblock a tenant.
     *
     * @param tenantDomain The tenant domain.
     */
    public void unblockTenant(String tenantDomain) {

        try {
            getConfigurationManager().deleteResource(RESOURCE_TYPE_NAME, tenantDomain);
            LOG.info(String.format("Tenant '%s' has been unblocked and configuration removed.", tenantDomain));
        } catch (ConfigurationManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Failed to unblock tenant: %s", tenantDomain), e);
            }
        }
    }

    /**
     * Get the block timestamp for a tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return The block timestamp in milliseconds, or 0 if not found.
     */
    public long getBlockTimestamp(String tenantDomain) {

        try {
            Resource resource = getConfigurationManager().getResource(RESOURCE_TYPE_NAME, tenantDomain, false);
            if (resource != null && resource.getAttributes() != null) {
                for (Attribute attribute : resource.getAttributes()) {
                    if (ATTR_BLOCK_TIMESTAMP.equals(attribute.getKey())) {
                        return Long.parseLong(attribute.getValue());
                    }
                }
            }
        } catch (ConfigurationManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Failed to retrieve block timestamp for tenant: %s", tenantDomain), e);
            }
        } catch (NumberFormatException e) {
            LOG.error(String.format("Invalid block timestamp format for tenant: %s", tenantDomain), e);
        }
        return 0;
    }

    /**
     * Get the last violation timestamp for a tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return The last violation timestamp in milliseconds, or 0 if not found.
     */
    private long getLastViolationTimestamp(String tenantDomain) {

        try {
            Resource resource = getConfigurationManager().getResource(RESOURCE_TYPE_NAME, tenantDomain, false);
            if (resource != null && resource.getAttributes() != null) {
                for (Attribute attribute : resource.getAttributes()) {
                    if (ATTR_LAST_VIOLATION_TIMESTAMP.equals(attribute.getKey())) {
                        return Long.parseLong(attribute.getValue());
                    }
                }
            }
        } catch (ConfigurationManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Failed to retrieve last violation timestamp for tenant: %s",
                        tenantDomain), e);
            }
        } catch (NumberFormatException e) {
            LOG.error(String.format("Invalid last violation timestamp format for tenant: %s", tenantDomain), e);
        }
        return 0;
    }

    /**
     * Clear all alert data for a tenant.
     *
     * @param tenantDomain The tenant domain.
     */
    public void clearTenantData(String tenantDomain) {

        try {
            getConfigurationManager().deleteResource(RESOURCE_TYPE_NAME, tenantDomain);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cleared alert data for tenant: %s", tenantDomain));
            }
        } catch (ConfigurationManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Failed to clear alert data for tenant: %s", tenantDomain), e);
            }
        }
    }

    /**
     * Get the ConfigurationManager instance.
     *
     * @return ConfigurationManager instance.
     */
    private ConfigurationManager getConfigurationManager() {

        return FrameworkServiceDataHolder.getInstance().getConfigurationManager();
    }
}
