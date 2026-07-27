/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for flow management operations.
 * This class provides methods to handle exceptions, manage audit logs, and clear cache entries.
 */
public class FlowMgtUtils {

    private static final Log LOG = LogFactory.getLog(FlowMgtUtils.class);

    private FlowMgtUtils() {

    }

    /**
     * Handle the flow management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return OrchestrationServerException.
     */
    public static FlowMgtServerException handleServerException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowMgtServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the flow management server exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return OrchestrationServerException.
     */
    public static FlowMgtServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowMgtServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the flow management client exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return OrchestrationClientException.
     */
    public static FlowMgtClientException handleClientException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowMgtClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the flow management client exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return OrchestrationClientException.
     */
    public static FlowMgtClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new FlowMgtClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator id despite masking.
     */
    public static String getInitiatorId() {

        String initiator = null;
        String username = MultitenantUtils.getTenantAwareUsername(getUser());
        String tenantDomain = MultitenantUtils.getTenantDomain(getUser());
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }
        if (StringUtils.isBlank(initiator)) {
            if (username.equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME)) {
                // If the initiator is wso2.system, we need not mask the username.
                return LoggerUtils.Initiator.System.name();
            }
            initiator = LoggerUtils.getMaskedContent(getUser());
        }
        return initiator;
    }

    /**
     * To get the current user, who is doing the current task.
     *
     * @return current logged-in user.
     */
    private static String getUser() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotEmpty(user)) {
            user = UserCoreUtil
                    .addTenantDomainToEntry(user, CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    /**
     * Clear the cache entry for the given cache key and tenant ID,
     * and recursively clear entries in all child organizations.
     *
     * @param cacheKey  The cache key to clear.
     * @param baseCache The base cache to clear the entry from.
     * @param tenantId  The tenant ID for which the cache entry should be cleared.
     * @param <K>       Type of the cache key.
     * @param <V>       Type of the cache value.
     * @throws FlowMgtServerException If an error occurs while clearing the cache.
     */
    public static <K extends Serializable, V extends Serializable> void clearCache(
            K cacheKey, BaseCache<K, V> baseCache, int tenantId) throws FlowMgtServerException {

        String currentTenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        try {
            baseCache.clearCacheEntry(cacheKey, tenantId);
            OrganizationManager organizationManager =
                    FlowMgtServiceDataHolder.getInstance().getOrganizationManager();
            String orgId = organizationManager.resolveOrganizationId(currentTenantDomain);
            List<String> childOrgIds = organizationManager.getChildOrganizationsIds(orgId);
            CompletableFuture.runAsync(() -> {
                for (String childOrgId : childOrgIds) {
                    try {
                        String tenantDomain = organizationManager.resolveTenantDomain(childOrgId);
                        baseCache.clear(tenantDomain);
                    } catch (OrganizationManagementException e) {
                        LOG.warn("Failed to clear the cache entry for organization: " + childOrgId, e);
                    }
                }
            });
        } catch (OrganizationManagementException e) {
            throw handleServerException(Constants.ErrorMessages.ERROR_CODE_CLEAR_CACHE_FAILED, e, currentTenantDomain);
        }
    }
}
