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


package org.wso2.carbon.identity.device.mgt.internal.component;

import org.wso2.carbon.identity.organization.management.organization.user.sharing.OrganizationUserSharingService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;

/**
 * Holds the OSGi services used by the device management component.
 */
public class DeviceMgtComponentServiceHolder {

    private static final DeviceMgtComponentServiceHolder INSTANCE = new DeviceMgtComponentServiceHolder();

    private OrganizationManager organizationManager;
    private OrganizationUserSharingService organizationUserSharingService;

    private DeviceMgtComponentServiceHolder() {

    }

    /**
     * Returns the service holder instance.
     *
     * @return Service holder instance.
     */
    public static DeviceMgtComponentServiceHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Returns the organization manager.
     *
     * @return Organization manager.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    /**
     * Sets the organization manager.
     *
     * @param organizationManager Organization manager.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }

    /**
     * Returns the organization user sharing service.
     *
     * @return Organization user sharing service.
     */
    public OrganizationUserSharingService getOrganizationUserSharingService() {

        return organizationUserSharingService;
    }

    /**
     * Sets the organization user sharing service.
     *
     * @param organizationUserSharingService Organization user sharing service.
     */
    public void setOrganizationUserSharingService(OrganizationUserSharingService organizationUserSharingService) {

        this.organizationUserSharingService = organizationUserSharingService;
    }
}
