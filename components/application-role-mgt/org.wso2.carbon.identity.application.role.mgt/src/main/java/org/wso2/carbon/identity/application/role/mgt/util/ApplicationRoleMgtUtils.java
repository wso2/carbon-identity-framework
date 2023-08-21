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

package org.wso2.carbon.identity.application.role.mgt.util;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementClientException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

/**
 * Application role management util.
 */
public class ApplicationRoleMgtUtils {

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    public static NamedJdbcTemplate getNewTemplate() {

        return new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }

    /**
     * Handle server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  Data to be replaced in the error description.
     * @return ApplicationRoleManagementServerException.
     */
    public static ApplicationRoleManagementServerException handleServerException(
            ApplicationRoleMgtConstants.ErrorMessages error, Throwable e, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new ApplicationRoleManagementServerException(error.getMessage(), description, error.getCode(), e);
    }

    /**
     * Handle client exceptions.
     *
     * @param error Error message.
     * @param data  Data to be replaced in the error description.
     * @return ApplicationRoleManagementClientException.
     */
    public static ApplicationRoleManagementClientException handleClientException(
            ApplicationRoleMgtConstants.ErrorMessages error, String... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new ApplicationRoleManagementClientException(error.getMessage(), description, error.getCode());
    }
}
