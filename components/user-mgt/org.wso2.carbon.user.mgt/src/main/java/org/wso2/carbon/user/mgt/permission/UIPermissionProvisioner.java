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

package org.wso2.carbon.user.mgt.permission;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects the UI (management console) permission tree declared by components and writes it to the
 * registry only when something actually needs it.
 * <p>
 * The permission collections under {@code /_system/governance/permission} carry display metadata for
 * the legacy management console permission tree. Runtime authorization does not read them - it is
 * served from the {@code UM_PERMISSION} / {@code UM_ROLE_PERMISSION} tables - so provisioning them is
 * only required when a caller asks for the tree itself (see {@code UserRealmProxy#getAllUIPermissions}
 * and {@code UserRealmProxy#getRolePermissions}).
 * <p>
 * Writing them during startup cost roughly 2,300 registry SQL statements on every boot, because each of
 * the ~272 declared nodes was checked with {@code resourceExists()} and then fetched in full just to test
 * a single display-name property. That is invisible against an embedded database but adds seconds of
 * sequential round trips against a networked one. Declarations are therefore buffered in memory here and
 * flushed on first use.
 *
 * @see ManagementPermissionsAdder
 */
public final class UIPermissionProvisioner {

    private static final Log log = LogFactory.getLog(UIPermissionProvisioner.class);

    /**
     * Set to {@code true} to restore the legacy behaviour of writing the UI permission tree to the
     * registry during server startup instead of on first use.
     */
    public static final String EAGER_PROVISIONING_PROPERTY = "carbon.ui.permissions.eager";

    /**
     * Declared permission path -&gt; display name, in declaration order so that parent collections are
     * created before their children.
     */
    private static final Map<String, String> DECLARED_PERMISSIONS = new LinkedHashMap<>();

    private static volatile boolean provisioned = false;

    private UIPermissionProvisioner() {

    }

    /**
     * Clears the buffered declarations and the provisioned flag. Only intended for tests, which need to
     * exercise the provisioning state machine more than once per JVM.
     */
    static void reset() {

        synchronized (UIPermissionProvisioner.class) {
            synchronized (DECLARED_PERMISSIONS) {
                DECLARED_PERMISSIONS.clear();
            }
            provisioned = false;
        }
    }

    /**
     * @return whether the UI permission tree should be written to the registry during startup.
     */
    public static boolean isEagerProvisioningEnabled() {

        return Boolean.parseBoolean(System.getProperty(EAGER_PROVISIONING_PROPERTY));
    }

    /**
     * Records declared permissions. The first declaration of a path wins, matching the legacy behaviour
     * where a component that found the collection already present left the existing display name alone.
     * <p>
     * The batch is written to the registry immediately when the tree has already been flushed, or when
     * {@link #EAGER_PROVISIONING_PROPERTY} is set. Bundles can start at any point in the server lifecycle,
     * including after the permission tree has been read, and a declaration that only ever landed in the
     * buffer would not reach the registry until the next restart.
     * <p>
     * This holds the class lock for the whole method so that a declaration cannot slip past the snapshot
     * taken by {@link #ensureProvisioned()} while that snapshot is being written.
     *
     * @param permissions Permission resource path to display name.
     * @throws RegistryException If the batch has to be written now and the registry write fails.
     */
    public static void declare(Map<String, String> permissions) throws RegistryException {

        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        synchronized (UIPermissionProvisioner.class) {
            synchronized (DECLARED_PERMISSIONS) {
                for (Map.Entry<String, String> permission : permissions.entrySet()) {
                    DECLARED_PERMISSIONS.putIfAbsent(permission.getKey(), permission.getValue());
                }
            }
            if (provisioned || isEagerProvisioningEnabled()) {
                writeBatch(permissions);
            }
        }
    }

    /**
     * Writes every declared permission collection to the registry, unless this has already been done.
     * <p>
     * Callers reach this while serving a request that may belong to any tenant, so the work runs inside a
     * super tenant flow. That matches the legacy startup path, which provisioned the super tenant only.
     * <p>
     * A failure leaves the provisioner un-provisioned so that a later call retries rather than silently
     * serving an incomplete tree forever.
     * <p>
     * Declarations arriving after this has run are written by {@link #declare(Map)} itself, which shares
     * this lock, so nothing can be buffered and then forgotten.
     *
     * @throws RegistryException If the permission tree could not be written to the registry.
     */
    public static void ensureProvisioned() throws RegistryException {

        if (provisioned) {
            return;
        }
        synchronized (UIPermissionProvisioner.class) {
            if (provisioned) {
                return;
            }
            if (isEagerProvisioningEnabled()) {
                // Every declaration was written to the registry as it arrived, so there is nothing to flush.
                provisioned = true;
                return;
            }
            Map<String, String> permissions;
            synchronized (DECLARED_PERMISSIONS) {
                permissions = new LinkedHashMap<>(DECLARED_PERMISSIONS);
            }
            writeBatch(permissions);
            provisioned = true;
        }
    }

    /**
     * Writes a batch of permission collections to the registry under a super tenant flow. Callers reach
     * the lazy path while serving a request that may belong to any tenant, and the legacy startup path
     * provisioned the super tenant only, so the tenant is pinned here either way.
     */
    private static void writeBatch(Map<String, String> permissions) throws RegistryException {

        if (permissions.isEmpty()) {
            return;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            Registry registry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry();
            int created = 0;
            for (Map.Entry<String, String> permission : permissions.entrySet()) {
                if (addPermission(registry, permission.getKey(), permission.getValue())) {
                    created++;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Provisioned the UI permission tree: " + created + " of " + permissions.size()
                        + " declared permissions were created in the registry.");
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Creates the permission collection if it is missing, or backfills its display name if it is present
     * without one.
     *
     * @param registry    Governance registry to write to.
     * @param path        Permission resource path.
     * @param displayName Display name for the permission.
     * @return {@code true} if the collection was created.
     * @throws RegistryException If the registry operation fails.
     */
    private static boolean addPermission(Registry registry, String path, String displayName)
            throws RegistryException {

        if (registry.resourceExists(path)) {
            Resource existingResource = registry.get(path);
            if (existingResource.getProperty(UserMgtConstants.DISPLAY_NAME) == null) {
                existingResource.setProperty(UserMgtConstants.DISPLAY_NAME, displayName);
                registry.put(path, existingResource);
            }
            return false;
        }
        Collection collection = registry.newCollection();
        collection.setProperty(UserMgtConstants.DISPLAY_NAME, displayName);
        registry.put(path, collection);
        return true;
    }
}
