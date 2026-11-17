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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.listener;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.identity.user.store.configuration.dao.UserStoreDAO;
import org.wso2.carbon.identity.user.store.configuration.dao.impl.DatabaseBasedUserStoreDAOImpl;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Registers the secondary user stores persisted in the database with the tenant realm.
 * <p>
 * File based user store configurations are picked up by the Carbon deployment engine, which scans the
 * {@code userstores} deployment directory of every tenant. Database based configurations have no such
 * directory, so this listener performs the equivalent step: the super tenant is loaded once the server
 * has started, and every other tenant is loaded when its configuration context is created.
 */
public class DatabaseBasedUserStoreLoader extends AbstractAxis2ConfigurationContextObserver
        implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(DatabaseBasedUserStoreLoader.class);

    @Override
    public void createdConfigurationContext(ConfigurationContext configurationContext) {

        loadUserStores(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
    }

    @Override
    public void completingServerStartup() {

        // Nothing to do before the server is up.
    }

    @Override
    public void completedServerStartup() {

        loadUserStores(MultitenantConstants.SUPER_TENANT_ID);
    }

    /**
     * Load and register the user stores persisted for the given tenant.
     *
     * @param tenantId Tenant whose user stores should be loaded.
     */
    private void loadUserStores(int tenantId) {

        if (!SecondaryUserStoreConfigurationUtil.isDatabaseBasedUserStorePersistenceEnabled()) {
            return;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            UserStoreDAO userStoreDAO = SecondaryUserStoreConfigurationUtil.getUserStoreDAO();
            if (userStoreDAO instanceof DatabaseBasedUserStoreDAOImpl) {
                // A pattern variable here is not an option: the build's checkstyle cannot parse one.
                ((DatabaseBasedUserStoreDAOImpl) userStoreDAO).loadSecondaryUserStores();
            }
        } catch (Exception e) {
            // Failing here must not prevent the tenant from loading.
            log.error("Error occurred while loading the database based user stores of tenant: " + tenantId, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
