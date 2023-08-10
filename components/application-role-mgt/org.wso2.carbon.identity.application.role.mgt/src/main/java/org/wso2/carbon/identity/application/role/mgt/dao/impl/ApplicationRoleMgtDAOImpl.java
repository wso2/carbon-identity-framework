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

package org.wso2.carbon.identity.application.role.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;

import java.util.List;

import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_INSERT_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME;
import static org.wso2.carbon.identity.application.role.mgt.constants.SQLConstants.SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.getNewTemplate;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.handleServerException;

/**
 * Application role DAO implementation.
 */
public class ApplicationRoleMgtDAOImpl implements ApplicationRoleMgtDAO {

    private static final Log LOG = LogFactory.getLog(ApplicationRoleMgtDAOImpl.class);

    @Override
    public ApplicationRole addApplicationRole(ApplicationRole applicationRole, int TenantID)
            throws ApplicationRoleManagementServerException {

        NamedJdbcTemplate namedJdbcTemplate = getNewTemplate();
        try {
            namedJdbcTemplate.withTransaction(template -> {
                template.executeInsert(SQLConstants.ADD_APPLICATION_ROLE, namedPreparedStatement -> {
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_ID, applicationRole.getRoleId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_APP_ID, applicationRole.getApplicationId());
                    namedPreparedStatement.setString(DB_SCHEMA_COLUMN_NAME_ROLE_NAME, applicationRole.getRoleName());
                    namedPreparedStatement.setInt(DB_SCHEMA_COLUMN_NAME_TENANT_ID, TenantID);
                }, null, false);
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_CODE_INSERT_ROLE, e, applicationRole.getRoleName(),
                    applicationRole.getApplicationId());
        }
        return applicationRole;
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId, String tenantDomain) {

        return null;
    }

    @Override
    public List<ApplicationRole> getApplicationRoles(String applicationId) {

        return null;
    }

    @Override
    public void updateApplicationRole(String applicationId, String roleId, String tenantDomain) {

    }

    @Override
    public void deleteApplicationRole(String applicationId, String roleId, String tenantDomain) {

    }
}
