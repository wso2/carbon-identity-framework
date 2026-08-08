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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
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
 * Storage contract for authentication session data, and the factory that selects the active
 * implementation.
 * <p>
 * This type is backend-agnostic: it defines the operations a session data store must provide, offers
 * convenience defaults for optional operations, and resolves the configured implementation on behalf
 * of callers. Concrete stores (for example a relational or an in-memory/key-value store) extend this
 * class and are contributed as OSGi services; the built-in default store is wired in by the
 * framework. The implementation to use is chosen from the {@code SessionDataStore.ImplType}
 * configuration property and cached, so callers simply use {@link #getInstance()} without knowing
 * which backend is active.
 */
public abstract class SessionDataStore {

    private static final Log log = LogFactory.getLog(SessionDataStore.class);

    private static final String STORE_IMPL_TYPE_PROPERTY = "SessionDataStore.ImplType";
    // Identifier of the built-in default store; used when no store is configured or the default is
    // requested explicitly. It is only a configuration/selection name, not a dependency on any
    // concrete implementation.
    private static final String DEFAULT_STORE_NAME = "JDBC";
    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";

    // Cached store returned by getInstance(); invalidated on store bind/unbind.
    private static volatile SessionDataStore selectedStore;

    // ---------------------------------------------------------------------
    // Factory / selection
    // ---------------------------------------------------------------------

    /**
     * Returns the active session data store, resolving it from configuration on first use and
     * caching the result. This is the single accessor callers use, independent of which backend is
     * configured.
     * <p>
     * When no store is configured (or the default is requested), the built-in default store is
     * returned. When a non-default store is configured, it must have been contributed as an OSGi
     * {@code SessionDataStore} service; if it is not yet available this throws
     * {@link IdentityRuntimeException} rather than falling back to the default, so session data is
     * never split across two stores.
     *
     * @return the active session data store.
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
     * Drops the cached store selection so the next {@link #getInstance()} re-resolves. Called when a
     * store OSGi service binds/unbinds, or after a runtime configuration change.
     */
    public static void invalidateSelectedStore() {

        selectedStore = null;
    }

    private static SessionDataStore resolveStore() {

        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        String configured = IdentityUtil.getProperty(STORE_IMPL_TYPE_PROPERTY);

        // The default store is used only when nothing is configured or the default is requested.
        if (StringUtils.isBlank(configured) || DEFAULT_STORE_NAME.equalsIgnoreCase(configured.trim())) {
            SessionDataStore defaultStore = dataHolder.getDefaultSessionDataStore();
            if (defaultStore != null) {
                return defaultStore;
            }
            throw new IdentityRuntimeException("The default session data store is not available yet; "
                    + "the authentication framework component may not have completed activation.");
        }

        // A non-default store is explicitly configured: it MUST be registered and available. We do
        // not fall back to the default here, because that would split session data across two stores
        // — records written to the default store before the configured store binds (or during a
        // temporary unbind) would be invisible once it (re)binds. Fail closed instead: the selection
        // is not cached (see getInstance()), so once the configured store's OSGi service binds, the
        // next call resolves successfully.
        SessionDataStore external = dataHolder.getSessionDataStore(configured.trim());
        if (external != null) {
            if (log.isDebugEnabled()) {
                log.debug("Resolved session data store: " + configured.trim());
            }
            return external;
        }
        throw new IdentityRuntimeException("Configured session data store '" + configured.trim()
                + "' is not available; its bundle may not be installed or its OSGi service may not "
                + "have bound yet. Refusing to fall back to the default store to avoid splitting "
                + "session data across stores.");
    }

    // ---------------------------------------------------------------------
    // Core store contract (implemented by every backend)
    // ---------------------------------------------------------------------

    /**
     * The unique name this store registers under and is selected by (case-insensitive). Callers
     * configure {@code SessionDataStore.ImplType} with this value to activate the store.
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
