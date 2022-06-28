/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.framework.common.testng.realm;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

/**
 * Simple In memory tenant manager for mocking.
 */
public class InMemoryTenantManager implements TenantManager {

    private Tenant tenant;

    public InMemoryTenantManager() {
        tenant = new Tenant();
        tenant.setId(MultitenantConstants.SUPER_TENANT_ID);
        tenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    @Override
    public int addTenant(Tenant tenant) throws org.wso2.carbon.user.api.UserStoreException {
        return 0;
    }

    @Override
    public void updateTenant(Tenant tenant) throws org.wso2.carbon.user.api.UserStoreException {

    }

    @Override
    public Tenant getTenant(int i) throws org.wso2.carbon.user.api.UserStoreException {
        return new Tenant();
    }

    @Override
    public Tenant[] getAllTenants() throws org.wso2.carbon.user.api.UserStoreException {
        return new Tenant[]{tenant};
    }

    @Override
    public Tenant[] getAllTenantsForTenantDomainStr(String s) throws org.wso2.carbon.user.api.UserStoreException {
        return new Tenant[]{tenant};
    }

    @Override
    public String getDomain(int i) throws org.wso2.carbon.user.api.UserStoreException {
        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    @Override
    public int getTenantId(String s) throws org.wso2.carbon.user.api.UserStoreException {
        return MultitenantConstants.SUPER_TENANT_ID;
    }

    @Override
    public void activateTenant(int i) throws org.wso2.carbon.user.api.UserStoreException {

    }

    @Override
    public void deactivateTenant(int i) throws org.wso2.carbon.user.api.UserStoreException {

    }

    @Override
    public boolean isTenantActive(int i) throws org.wso2.carbon.user.api.UserStoreException {
        return false;
    }

    @Override
    public void deleteTenant(int i) throws org.wso2.carbon.user.api.UserStoreException {

    }

    @Override
    public void deleteTenant(int i, boolean b) throws org.wso2.carbon.user.api.UserStoreException {

    }

    @Override
    public String getSuperTenantDomain() throws UserStoreException {
        return null;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {

    }

    @Override
    public void initializeExistingPartitions() {

    }
}
