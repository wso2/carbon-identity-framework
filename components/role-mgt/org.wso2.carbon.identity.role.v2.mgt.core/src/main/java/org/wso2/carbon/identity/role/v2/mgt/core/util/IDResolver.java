/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.v2.mgt.core.util;

import org.wso2.carbon.identity.role.v2.mgt.core.IdentityRoleManagementException;

/**
 * ID resolver interface.
 */
public interface IDResolver {

    /**
     * Retrieve the name for the given ID.
     *
     * @param id           ID.
     * @param tenantDomain tenant domain.
     * @return name.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getNameByID(String id, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve the name for the given ID.
     *
     * @param name         name.
     * @param tenantDomain tenant domain.
     * @return name.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getIDByName(String name, String tenantDomain) throws IdentityRoleManagementException;
}
