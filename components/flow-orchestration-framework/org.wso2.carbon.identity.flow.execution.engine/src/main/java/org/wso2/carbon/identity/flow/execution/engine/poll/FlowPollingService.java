/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.flow.execution.engine.poll;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowPollingCache;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowPollingCacheEntry;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowPollingCacheKey;
import org.wso2.carbon.identity.flow.execution.engine.dao.FlowPollingDAO;
import org.wso2.carbon.identity.flow.execution.engine.dao.FlowPollingDAOImpl;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_VALIDITY_SECONDS;

/**
 * Keeps flow polling entries, so a client waiting on something outside the flow, such as another device, polls a
 * token instead of calling the flow. Entries live in the flow context store under the token's hash, bypassing the
 * flow context cache. Only the flow changes an entry, and each change clears its {@link FlowPollingCache} copy.
 */
public class FlowPollingService {

    private static final Log LOG = LogFactory.getLog(FlowPollingService.class);
    private static final String POLL_ENTRY_PREFIX = "poll_";
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final FlowPollingService INSTANCE = new FlowPollingService(new FlowPollingDAOImpl(),
            FlowPollingCache::getInstance);

    private final FlowPollingDAO flowPollingDAO;
    // Resolved on use, so that loading this class does not build the cache.
    private final Supplier<FlowPollingCache> flowPollingCache;

    /**
     * Status of a polling entry.
     */
    public enum Status {

        WAITING,
        SUCCESS,
        FAILED
    }

    FlowPollingService(FlowPollingDAO flowPollingDAO, Supplier<FlowPollingCache> flowPollingCache) {

        this.flowPollingDAO = flowPollingDAO;
        this.flowPollingCache = flowPollingCache;
    }

    public static FlowPollingService getInstance() {

        return INSTANCE;
    }

    /**
     * Create a WAITING polling entry for a flow and cache it, which is safe as nothing can change it yet.
     *
     * @param flowId Id of the flow the entry belongs to.
     * @return Token to poll the entry with.
     * @throws FlowEngineException If the entry cannot be stored.
     */
    public String create(String flowId) throws FlowEngineException {

        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String entryId = toEntryId(token);
        // Taken before the insert, so the cached copy never outlives the stored entry.
        long expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(POLL_VALIDITY_SECONDS);
        FlowPollingEntry entry = new FlowPollingEntry(flowId, Status.WAITING.name(), expiresAt);
        flowPollingDAO.addPollingEntry(entryId, resolveTenantId(), entry);
        flowPollingCache.get().addToCacheOnRead(new FlowPollingCacheKey(entryId), toCacheEntry(entry));
        return token;
    }

    /**
     * Get the status of a polling entry, read through the cache. A missing, expired or other tenant entry is FAILED.
     *
     * @param token Token of the entry.
     * @return Status of the entry.
     * @throws FlowEngineException If the entry cannot be read.
     */
    public Status getStatus(String token) throws FlowEngineException {

        if (StringUtils.isBlank(token)) {
            return Status.FAILED;
        }
        String entryId = toEntryId(token);
        FlowPollingCacheKey cacheKey = new FlowPollingCacheKey(entryId);
        FlowPollingCacheEntry cachedEntry = flowPollingCache.get().getValueFromCache(cacheKey);
        if (cachedEntry != null) {
            if (cachedEntry.isExpired(System.currentTimeMillis())) {
                flowPollingCache.get().clearCacheEntry(cacheKey);
                return Status.FAILED;
            }
            return toStatus(cachedEntry.getStatus());
        }

        FlowPollingEntry entry = readEntry(entryId);
        if (entry == null) {
            return Status.FAILED;
        }
        if (entry.getExpiresAt() > 0) {
            flowPollingCache.get().addToCacheOnRead(cacheKey, toCacheEntry(entry));
            // The flow may have changed the entry between the read and the caching, so re-read and drop a stale copy.
            FlowPollingEntry entryAfterCaching = readEntry(entryId);
            if (!entry.equals(entryAfterCaching)) {
                flowPollingCache.get().clearCacheEntry(cacheKey);
                return entryAfterCaching == null ? Status.FAILED : toStatus(entryAfterCaching.getStatus());
            }
        }
        return toStatus(entry.getStatus());
    }

    /**
     * Set a polling entry to SUCCESS without recreating a deleted one. The cache is cleared, not replaced.
     *
     * @param token Token of the entry.
     * @return True if the entry was updated, false if the token is blank or the entry is missing or expired.
     * @throws FlowEngineException If the entry cannot be updated.
     */
    public boolean markSuccess(String token) throws FlowEngineException {

        if (StringUtils.isBlank(token)) {
            return false;
        }
        String entryId = toEntryId(token);
        FlowPollingEntry entry = readEntry(entryId);
        if (entry == null) {
            LOG.debug("The polling entry does not exist anymore. Hence it is not marked as successful.");
            return false;
        }
        boolean isUpdated = flowPollingDAO.updatePollingEntry(entryId, resolveTenantId(),
                new FlowPollingEntry(entry.getFlowId(), Status.SUCCESS.name(), entry.getExpiresAt()));
        if (!isUpdated) {
            LOG.debug("The polling entry was deleted before it was marked as successful.");
        }
        flowPollingCache.get().clearCacheEntry(new FlowPollingCacheKey(entryId));
        return isUpdated;
    }

    /**
     * Delete a polling entry, and clear its cache entry.
     *
     * @param token Token of the entry.
     * @throws FlowEngineException If the entry cannot be deleted.
     */
    public void delete(String token) throws FlowEngineException {

        if (StringUtils.isBlank(token)) {
            return;
        }
        String entryId = toEntryId(token);
        flowPollingDAO.deletePollingEntry(entryId, resolveTenantId());
        flowPollingCache.get().clearCacheEntry(new FlowPollingCacheKey(entryId));
    }

    private FlowPollingEntry readEntry(String entryId) throws FlowEngineException {

        return flowPollingDAO.getPollingEntry(entryId, resolveTenantId());
    }

    private static FlowPollingCacheEntry toCacheEntry(FlowPollingEntry entry) {

        return new FlowPollingCacheEntry(entry.getStatus(), entry.getExpiresAt());
    }

    private static Status toStatus(String status) {

        if (status == null) {
            return Status.FAILED;
        }
        try {
            return Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            return Status.FAILED;
        }
    }

    private int resolveTenantId() throws FlowEngineException {

        return IdentityTenantUtil.getTenantId(FlowExecutionEngineUtils.resolveTenantDomain());
    }

    private static String toEntryId(String token) throws FlowEngineException {

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return POLL_ENTRY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw FlowExecutionEngineUtils.handleServerException(ErrorMessages.ERROR_CODE_FLOW_POLLING_FAILURE, e);
        }
    }
}
