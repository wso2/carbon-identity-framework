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

/**
 * Application role management service implementation.
 */
public class ApplicationRoleManagerImpl implements ApplicationRoleManager {

    private final ApplicationRoleMgtDAO applicationRoleMgtDAO =
            new CacheBackedApplicationRoleMgtDAOImpl(new ApplicationRoleMgtDAOImpl());

    @Override
    public void addApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException {

        applicationRoleMgtDAO.addApplicationRole(applicationRole, getTenantID());
    }

    @Override
    public void updateApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException {

    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId) throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRoleById(roleId, getTenantDomain());
    }

    @Override
    public ApplicationRole[] getApplicationRoles(String applicationId) throws ApplicationRoleManagementException {

        return new ApplicationRole[0];
    }

    @Override
    public void deleteApplicationRole(String roleId) throws ApplicationRoleManagementException {

    }

    private static String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private static int getTenantID() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
    }
}
