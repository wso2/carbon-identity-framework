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
 * Session data store: the type every caller uses, and the factory that selects the active
 * implementation.
 * <p>
 * <b>Callers are unchanged.</b> {@code SessionDataStore.getInstance()} and every public method keep
 * their exact signatures; framework code keeps calling
 * {@code SessionDataStore.getInstance().storeSessionData(...)} exactly as before. This type is now
 * the abstract supertype of the store implementations: {@link #getInstance()} returns the configured
 * concrete store &mdash; the default {@link JDBCSessionDataStore} (the relational implementation,
 * unchanged) or an externally-registered store (e.g. a Redis store) &mdash; so JDBC and any
 * alternative store sit at the same pluggable level and callers are unaware which one answers.
 * <p>
 * Selection reads {@code SessionDataStore.ImplType} (case-insensitive; default {@code "JDBC"}).
 * JDBC is used only when the property is absent or explicitly set to {@code "JDBC"}. When a
 * non-JDBC store is configured it must be registered and
 * available; resolution does <b>not</b> silently fall back to JDBC (which would split session data
 * across two stores) but fails instead, so operations fail-closed until the configured store's OSGi
 * service binds. The resolution is cached and invalidated on any store OSGi bind/unbind (see
 * {@link #invalidateSelectedStore()}), so a late-binding store is picked up without a restart.
 * <p>
 * The core data operations are abstract (each store implements them). Operation-scoped reads,
 * {@code persistSessionData}, batch clear, cleanup toggles and {@code stopService} have defaults
 * here suited to a store without the relational concepts; the JDBC store overrides them with its
 * real behavior, so nothing changes when JDBC is active.
 */
public abstract class SessionDataStore {

    private static final Log log = LogFactory.getLog(SessionDataStore.class);

    /** Config property selecting the active store. Default {@code "JDBC"}. */
    private static final String STORE_IMPL_TYPE_PROPERTY = "SessionDataStore.ImplType";
    /** Store name of the default relational implementation. */
    public static final String JDBC_STORE_NAME = "JDBC";
    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";

    public static final String DEFAULT_SESSION_STORE_TABLE_NAME = "IDN_AUTH_SESSION_STORE";
    public static final String DEFAULT_TEMP_SESSION_STORE_TABLE_NAME = "IDN_AUTH_TEMP_SESSION_STORE";

    // Cached store returned by getInstance(); invalidated on store bind/unbind.
    private static volatile SessionDataStore selectedStore;

    // ---------------------------------------------------------------------
    // Factory / selection
    // ---------------------------------------------------------------------

    /**
     * Returns the active session data store. Same accessor and return type callers already use, so
     * no caller changes. By default this is the relational {@link JDBCSessionDataStore}; if a
     * non-JDBC store is configured it must be registered as an OSGi {@code SessionDataStore}
     * service, otherwise this throws {@link IdentityRuntimeException} rather than falling back to
     * JDBC (see {@link #resolveStore()}).
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

        String configured = IdentityUtil.getProperty(STORE_IMPL_TYPE_PROPERTY);
        // JDBC is used only when no store is configured or JDBC is explicitly requested.
        if (StringUtils.isBlank(configured) || JDBC_STORE_NAME.equalsIgnoreCase(configured.trim())) {
            return JDBCSessionDataStore.getInstance();
        }

        // A non-default store is explicitly configured: it MUST be registered and available. We do
        // not fall back to JDBC here, because that would split session data across two stores —
        // records written to JDBC before the configured store binds (or during a temporary unbind)
        // would be invisible once it (re)binds. Fail closed instead: the selection is not cached
        // (see getInstance()), so once the configured store's OSGi service binds, the next call
        // resolves successfully.
        SessionDataStore external =
                FrameworkServiceDataHolder.getInstance().getSessionDataStore(configured.trim());
        if (external != null) {
            if (log.isDebugEnabled()) {
                log.debug("Resolved session data store: " + configured.trim());
            }
            return external;
        }
        throw new IdentityRuntimeException("Configured session data store '" + configured.trim()
                + "' is not available; its bundle may not be installed or its OSGi service may not "
                + "have bound yet. Refusing to fall back to the '" + JDBC_STORE_NAME + "' store to "
                + "avoid splitting session data across stores.");
    }

    // ---------------------------------------------------------------------
    // Identity & capability
    // ---------------------------------------------------------------------

    /** Unique, stable store name used to select/register this store (e.g. "JDBC", "Redis"). */
    public abstract String getStoreName();

    /**
     * Existence-only liveness probe used by the session-management API to tell whether a session is
     * still active without deserializing the payload.
     */
    public abstract boolean isSessionLive(String key, String type);

    // ---------------------------------------------------------------------
    // Core data operations (each store implements these)
    // ---------------------------------------------------------------------

    public abstract void storeSessionData(String key, String type, Object entry, int tenantId);

    public abstract Object getSessionData(String key, String type);

    public abstract SessionContextDO getSessionContextData(String key, String type);

    public abstract void clearSessionData(String key, String type);

    public abstract void removeSessionData(String key, String type, long nanoTime);

    public abstract void removeExpiredSessionData();

    public abstract void removeTempAuthnContextData(String key, String type);

    // ---------------------------------------------------------------------
    // Convenience / relational-specific operations (defaults here; JDBC overrides)
    // ---------------------------------------------------------------------

    public void storeSessionData(String key, String type, Object entry) {

        storeSessionData(key, type, entry, MultitenantConstants.INVALID_TENANT_ID);
    }

    /** Operation-scoped read. Relational concept; defaults to no result for other stores. */
    public Object getSessionData(String key, String type, String operation) {

        return null;
    }

    /** Operation-scoped read. Relational concept; defaults to no result for other stores. */
    public SessionContextDO getSessionContextData(String key, String type, String operation) {

        return null;
    }

    /** Relational append-only concept; defaults to {@code false} for other stores. */
    public boolean validateLastOperationOnSessionData(String key, String type, String requiredOperation) {

        return false;
    }

    /** Batch clear. Default clears each key individually; the JDBC store batches. */
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

    /** Relational STORE-record write; no-op default (used only by the JDBC persist worker). */
    public void persistSessionData(String key, String type, Object entry, long nanoTime, int tenantId) {

    }

    /**
     * @deprecated Now handled as part of {@link #removeExpiredSessionData()} (IDENTITY-5131).
     */
    @Deprecated
    public void removeExpiredOperationData() {

    }

    /** Whether the relational cleanup is enabled; defaults to {@code false} for other stores. */
    public boolean isSessionDataCleanupEnabled() {

        return false;
    }

    /** Stop background persist/cleanup workers; no-op default (the JDBC store overrides). */
    public void stopService() {

    }

    // ---------------------------------------------------------------------
    // Shared validity derivation (used by native-expiry stores to size their TTL)
    // ---------------------------------------------------------------------

    /**
     * Remaining validity (nanoseconds) for a record: a {@link CacheEntry}'s own validity period if
     * set, otherwise the configured cleanup timeout for the type/tenant. Shared so a native-TTL
     * store (e.g. Redis) sizes its expiry exactly as the JDBC store computes {@code EXPIRY_TIME}.
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
