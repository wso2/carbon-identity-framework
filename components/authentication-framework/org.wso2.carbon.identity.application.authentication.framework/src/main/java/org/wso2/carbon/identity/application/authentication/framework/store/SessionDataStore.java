/*
 * Copyright (c) 2014-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.SessionStorageSelector;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Class to store and retrieve authentication session data.
 * <p>
 * Implementations are pluggable. The relational {@link JDBCSessionDataStore} is used by default, and an
 * alternative store extends this class and registers itself as an OSGi {@code SessionDataStore} service
 * named by {@value SessionStorageSelector#STORE_IMPL_TYPE_PROPERTY}. Callers use {@link #getInstance()}
 * without knowing which store is active.
 */
public abstract class SessionDataStore {

    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";

    private static volatile SessionDataStore selectedStore;

    /**
     * Returns the configured session data store. The selection is resolved on first use and cached.
     *
     * @return the session data store to use.
     */
    public static SessionDataStore getInstance() {

        SessionDataStore resolved = selectedStore;
        if (resolved == null) {
            synchronized (SessionDataStore.class) {
                resolved = selectedStore;
                if (resolved == null) {
                    resolved = resolveStore();
                    selectedStore = resolved;
                }
            }
        }
        return resolved;
    }

    /**
     * Clears the cached store selection, so that the next {@link #getInstance()} resolves it again. Called
     * when a store service is registered or unregistered.
     */
    public static void invalidateSelectedStore() {

        selectedStore = null;
    }

    private static SessionDataStore resolveStore() {

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        String storeName = SessionStorageSelector.getConfiguredStoreName();
        if (SessionStorageSelector.isDefaultStoreConfigured(storeName)) {
            SessionDataStore defaultStore = dataHolder.getDefaultSessionDataStore();
            if (defaultStore == null) {
                throw IdentityRuntimeException.error("The default session data store is not available. The "
                        + "authentication framework component may not have completed activation.");
            }
            return defaultStore;
        }

        SessionDataStore sessionDataStore = dataHolder.getSessionDataStore(storeName);
        if (sessionDataStore == null) {
            throw IdentityRuntimeException.error("Configured session data store is not available: " + storeName
                    + ". Its bundle may not be installed or its service may not have been registered yet.");
        }
        return sessionDataStore;
    }

    /**
     * Returns the name this store is registered under and selected by, matched case-insensitively.
     *
     * @return the store name.
     */
    public abstract String getStoreName();

    /**
     * Whether a live (non-expired, not cleared) record exists for the given key and type.
     *
     * @param key  the record key (e.g. a session or context identifier).
     * @param type the logical record type (typically a cache name).
     * @return {@code true} if a live record exists, {@code false} otherwise.
     */
    public abstract boolean isSessionLive(String key, String type);

    /**
     * Stores a record for the given key and type, scoped to a tenant. An existing record for the
     * same key and type is replaced, and its validity/expiry is (re)set from the entry and the
     * configured session timeouts.
     *
     * @param key      the record key.
     * @param type     the logical record type.
     * @param entry    the value to store.
     * @param tenantId the owning tenant, or {@link MultitenantConstants#INVALID_TENANT_ID} if none.
     */
    public abstract void storeSessionData(String key, String type, Object entry, int tenantId);

    /**
     * Returns the stored value for the given key and type, or {@code null} if none exists or it has
     * expired.
     *
     * @param key  the record key.
     * @param type the logical record type.
     * @return the stored value, or {@code null}.
     */
    public abstract Object getSessionData(String key, String type);

    /**
     * Returns the stored record together with its metadata (tenant, type, creation time), or
     * {@code null} if none exists or it has expired.
     *
     * @param key  the record key.
     * @param type the logical record type.
     * @return the record and its metadata, or {@code null}.
     */
    public abstract SessionContextDO getSessionContextData(String key, String type);

    /**
     * Marks the record for the given key and type as cleared (logged out / invalidated) so that
     * subsequent reads no longer return it.
     *
     * @param key  the record key.
     * @param type the logical record type.
     */
    public abstract void clearSessionData(String key, String type);

    /**
     * Permanently removes the record for the given key and type, without leaving a cleared marker.
     *
     * @param key      the record key.
     * @param type     the logical record type.
     * @param nanoTime the timestamp (nanoseconds) qualifying which record instance to remove.
     */
    public abstract void removeSessionData(String key, String type, long nanoTime);

    /**
     * Removes records that have passed their validity period. Stores with native expiry may treat
     * this as a no-op.
     */
    public abstract void removeExpiredSessionData();

    /**
     * Removes temporary authentication-context data for the given key and type.
     *
     * @param key  the record key.
     * @param type the logical record type.
     */
    public abstract void removeTempAuthnContextData(String key, String type);

    // ---------------------------------------------------------------------
    // Convenience defaults and optional operations
    // ---------------------------------------------------------------------

    /**
     * Stores a record for the given key and type with no tenant scope. Convenience overload of
     * {@link #storeSessionData(String, String, Object, int)}.
     *
     * @param key   the record key.
     * @param type  the logical record type.
     * @param entry the value to store.
     */
    public void storeSessionData(String key, String type, Object entry) {

        storeSessionData(key, type, entry, MultitenantConstants.INVALID_TENANT_ID);
    }

    /**
     * Returns the stored value only if the store's most recently recorded operation for the given
     * key and type matches {@code operation}. Optional: stores that do not track per-record
     * operations return {@code null} by default.
     *
     * @param key       the record key.
     * @param type      the logical record type.
     * @param operation the operation the last write must match.
     * @return the stored value, or {@code null}.
     */
    public Object getSessionData(String key, String type, String operation) {

        return null;
    }

    /**
     * Returns the stored record and metadata only if the store's most recently recorded operation
     * for the given key and type matches {@code operation}. Optional: stores that do not track
     * per-record operations return {@code null} by default.
     *
     * @param key       the record key.
     * @param type      the logical record type.
     * @param operation the operation the last write must match.
     * @return the record and its metadata, or {@code null}.
     */
    public SessionContextDO getSessionContextData(String key, String type, String operation) {

        return null;
    }

    /**
     * Whether the store's most recently recorded operation for the given key and type equals
     * {@code requiredOperation}. Optional: stores that do not track per-record operations return
     * {@code false} by default.
     *
     * @param key               the record key.
     * @param type              the logical record type.
     * @param requiredOperation the operation to check for.
     * @return {@code true} if the last recorded operation matches, {@code false} otherwise.
     */
    public boolean validateLastOperationOnSessionData(String key, String type, String requiredOperation) {

        return false;
    }

    /**
     * Clears several records of the same type in one call. The default clears each key individually;
     * stores that support batching may override for efficiency.
     *
     * @param keys the record keys to clear.
     * @param type the logical record type.
     */
    public void clearSessionDataBatch(List<String> keys, String type) {

        if (keys == null) {
            return;
        }
        for (String key : keys) {
            if (StringUtils.isNotBlank(key)) {
                clearSessionData(key, type);
            }
        }
    }

    /**
     * Synchronously writes a record. Optional hook for stores whose {@code storeSessionData}
     * enqueues writes for asynchronous processing; the default is a no-op.
     *
     * @param key      the record key.
     * @param type     the logical record type.
     * @param entry    the value to store.
     * @param nanoTime the write timestamp (nanoseconds).
     * @param tenantId the owning tenant, or {@link MultitenantConstants#INVALID_TENANT_ID} if none.
     */
    public void persistSessionData(String key, String type, Object entry, long nanoTime, int tenantId) {

    }

    /**
     * @deprecated Now handled as part of {@link #removeExpiredSessionData()} (IDENTITY-5131).
     */
    @Deprecated
    public void removeExpiredOperationData() {

    }

    /**
     * Whether this store runs its own background cleanup of expired records. Stores with native
     * expiry, or none, return {@code false} by default.
     *
     * @return {@code true} if background cleanup is enabled.
     */
    public boolean isSessionDataCleanupEnabled() {

        return false;
    }

    /**
     * Stops any background workers (asynchronous writers, cleanup tasks) and releases resources.
     * Called on component shutdown; the default is a no-op.
     */
    public void stopService() {

    }

    // ---------------------------------------------------------------------
    // Shared validity derivation (available to stores that manage their own expiry)
    // ---------------------------------------------------------------------

    /**
     * Remaining validity (nanoseconds) for a record: a {@link CacheEntry}'s own validity period if
     * set, otherwise the configured cleanup timeout for the type/tenant. Shared so a store that
     * manages its own expiry sizes it consistently with the configured session/cleanup timeouts.
     *
     * @param entry    the value being stored.
     * @param type     the logical record type.
     * @param tenantId the owning tenant, or {@link MultitenantConstants#INVALID_TENANT_ID} if none.
     * @return the validity period in nanoseconds.
     */
    protected long getValidityPeriodNano(Object entry, String type, int tenantId) {

        long validityPeriodNano = 0L;
        if (entry instanceof CacheEntry) {
            validityPeriodNano = ((CacheEntry) entry).getValidityPeriod();
        }
        if (validityPeriodNano == 0L) {
            validityPeriodNano = getCleanupTimeout(type, tenantId);
        }
        return validityPeriodNano;
    }

    private long getCleanupTimeout(String type, int tenantId) {

        if (isTempCache(type)) {
            return TimeUnit.MINUTES.toNanos(IdentityUtil.getTempDataCleanUpTimeout());
        } else if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            int timeout = IdPManagementUtil.getRememberMeTimeout(tenantDomain);
            Optional<Integer> maximumSessionTimeout = IdPManagementUtil.getMaximumSessionTimeout(tenantDomain);
            if (maximumSessionTimeout.isPresent()) {
                timeout = Math.max(timeout, maximumSessionTimeout.get());
            }
            return TimeUnit.SECONDS.toNanos(timeout);
        } else {
            return TimeUnit.MINUTES.toNanos(IdentityUtil.getCleanUpTimeout());
        }
    }

    private boolean isTempCache(String type) {

        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, type);
        return identityCacheConfig != null && identityCacheConfig.isTemporary();
    }
}
