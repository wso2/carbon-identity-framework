/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.user.action.internal.component;

import org.wso2.carbon.identity.organization.management.service.OrganizationManager;

/**
 * Service component Holder for the User Action Service.
 */
public class UserActionServiceComponentHolder {

    private OrganizationManager organizationManager;

    public static final UserActionServiceComponentHolder INSTANCE = new UserActionServiceComponentHolder();

    private UserActionServiceComponentHolder() {

    }

    /**
     * Get the instance of PreUpdatePasswordActionActionServiceComponentHolder.
     *
     * @return ActionMgtServiceComponentHolder instance.
     */
    public static UserActionServiceComponentHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get OrganizationManager instance.
     *
     * @return OrganizationManager instance.
     */
    public OrganizationManager getOrganizationManager() {

        return organizationManager;
    }

    /**
     * Set OrganizationManager instance.
     *
     * @param organizationManager OrganizationManager instance.
     */
    public void setOrganizationManager(OrganizationManager organizationManager) {

        this.organizationManager = organizationManager;
    }
}
