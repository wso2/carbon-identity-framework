/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.internal;

import org.wso2.carbon.identity.event.services.IdentityEventService;

/**
 * Service component holder for the API resource management.
 */
public class APIResourceManagementServiceComponentHolder {

    private IdentityEventService identityEventService;

    private static final APIResourceManagementServiceComponentHolder instance =
            new APIResourceManagementServiceComponentHolder();

    private APIResourceManagementServiceComponentHolder() {

    }

    /**
     * Get the instance of APIResourceManagementServiceComponentHolder.
     *
     * @return APIResourceManagementServiceComponentHolder instance.
     */
    public static APIResourceManagementServiceComponentHolder getInstance() {

        return instance;
    }

    /**
     * Get the IdentityEventService.
     *
     * @return IdentityEventService instance.
     */
    public IdentityEventService getIdentityEventService() {

        return identityEventService;
    }

    /**
     * Set the IdentityEventService.
     *
     * @param identityEventService IdentityEventService instance.
     */
    public void setIdentityEventService(IdentityEventService identityEventService) {

        this.identityEventService = identityEventService;
    }
}
