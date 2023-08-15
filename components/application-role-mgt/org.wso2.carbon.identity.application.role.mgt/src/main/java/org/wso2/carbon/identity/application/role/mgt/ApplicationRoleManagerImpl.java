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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
import org.wso2.carbon.identity.application.role.mgt.dao.impl.ApplicationRoleMgtDAOImpl;
import org.wso2.carbon.identity.application.role.mgt.dao.impl.CacheBackedApplicationRoleMgtDAOImpl;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;

import java.util.List;

import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_DUPLICATE_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.handleClientException;

/**
 * Application role management service implementation.
 */
public class ApplicationRoleManagerImpl implements ApplicationRoleManager {

    private final ApplicationRoleMgtDAO applicationRoleMgtDAO =
            new CacheBackedApplicationRoleMgtDAOImpl(new ApplicationRoleMgtDAOImpl());

    @Override
    public void addApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException {

        String tenantDomain = getTenantDomain();
        boolean existingRole =
                applicationRoleMgtDAO.isExistingRole(applicationRole.getApplicationId(), applicationRole.getRoleName(),
                        tenantDomain);
        if (existingRole) {
            throw handleClientException(ERROR_CODE_DUPLICATE_ROLE, applicationRole.getRoleName(),
                    applicationRole.getApplicationId());
        }
        applicationRoleMgtDAO.addApplicationRole(applicationRole, tenantDomain);
    }

    @Override
    public void updateApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException {

        // TODO.
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId) throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRoleById(roleId, getTenantDomain());
    }

    @Override
    public List<ApplicationRole> getApplicationRoles(String applicationId) throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRoles(applicationId);
    }

    @Override
    public void deleteApplicationRole(String roleId) throws ApplicationRoleManagementException {

        applicationRoleMgtDAO.deleteApplicationRole(roleId, getTenantDomain());
    }

    private static String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }
}
