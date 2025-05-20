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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtServerException;

import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.MICROSOFT;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ORACLE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtExceptionHandler.handleServerException;

/**
 * Util Methods.
 */
public class AsyncOperationStatusMgtDbUtil {

    private static boolean isDBTypeOf(String dbType) throws AsyncOperationStatusMgtServerException {

        try {
            NamedJdbcTemplate jdbcTemplate = getNewTemplate();
            return jdbcTemplate.getDriverName().toLowerCase().contains(dbType) ||
                    jdbcTemplate.getDatabaseProductName().toLowerCase().contains(dbType);
        } catch (DataAccessException e) {
            throw handleServerException(ErrorMessage.ERROR_CODE_INVALID_REQUEST_BODY, e);
        }
    }

    public static boolean isMSSqlDB() throws AsyncOperationStatusMgtServerException {

        return isDBTypeOf(MICROSOFT);
    }

    public static boolean isOracleDB() throws AsyncOperationStatusMgtServerException {

        return isDBTypeOf(ORACLE);
    }

    /**
     * Get a new Jdbc Template.
     *
     * @return a new Jdbc Template.
     */
    public static NamedJdbcTemplate getNewTemplate() {

        return new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
    }
}
