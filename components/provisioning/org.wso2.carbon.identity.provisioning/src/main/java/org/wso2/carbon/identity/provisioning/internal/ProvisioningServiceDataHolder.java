/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.entitlement.EntitlementService;
import org.wso2.carbon.identity.provisioning.AbstractProvisioningConnectorFactory;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.RolePermissionManagementService;

import java.util.HashMap;
import java.util.Map;

public class ProvisioningServiceDataHolder {

    private static ProvisioningServiceDataHolder instance = new ProvisioningServiceDataHolder();
    private RealmService realmService;
    private BundleContext bundleContext;
    private EntitlementService entitlementService;
    private RolePermissionManagementService rolePermissionManagementService;
    private Map<String, AbstractProvisioningConnectorFactory> connectorFactories = new HashMap<String, AbstractProvisioningConnectorFactory>();

    private ProvisioningServiceDataHolder() {
    }

    public static ProvisioningServiceDataHolder getInstance() {
        return instance;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setConnectorFactories(Map<String, AbstractProvisioningConnectorFactory> connectorFactories) {
        this.connectorFactories = connectorFactories;
    }

    public Map<String, AbstractProvisioningConnectorFactory> getConnectorFactories() {
        return connectorFactories;
    }

    public EntitlementService getEntitlementService() {

        return entitlementService;
    }

    public void setEntitlementService(EntitlementService entitlementService) {

        this.entitlementService = entitlementService;
    }

    public void setRolePermissionManagementService(RolePermissionManagementService rolePermissionManagementService) {

        this.rolePermissionManagementService = rolePermissionManagementService;
    }

    /**
     * Method to get rolePermissionManagementService.
     *
     * @return {RolePermissionManagementService}
     */
    public RolePermissionManagementService getRolePermissionManagementService() {

        if (rolePermissionManagementService == null) {
            throw new RuntimeException("Role permission management service cannot be found.");
        }
        return rolePermissionManagementService;
    }
}


