/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.role.mgt;

import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;

/**
 * Application Role Manager.
 */
public interface ApplicationRoleManager {

    /**
     * Add application role.
     *
     * @param applicationRole Application role.
     * @throws ApplicationRoleManagementException Error occurred while adding application role.
     */
    void addApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException;

    /**
     * Update application role.
     *
     * @param applicationRole Application role.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    void updateApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException;

    /**
     * Get the application role by role id.
     *
     * @param roleId Role id.
     * @return Application role.
     * @throws ApplicationRoleManagementException Error occurred while retrieving the application role.
     */
    ApplicationRole getApplicationRoleById(String roleId) throws ApplicationRoleManagementException;

    /**
     * Get all the application roles by application id.
     *
     * @param applicationId Application id.
     * @return Application roles.
     * @throws ApplicationRoleManagementException Error occurred while retrieving the application roles of a given app.
     */
    ApplicationRole[] getApplicationRoles(String applicationId) throws ApplicationRoleManagementException;

    /**
     * Delete application role.
     *
     * @param roleId Role id.
     * @throws ApplicationRoleManagementException Error occurred while deleting the application role.
     */
    void deleteApplicationRole(String roleId) throws ApplicationRoleManagementException;
}
