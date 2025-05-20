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

package org.wso2.carbon.identity.workflow.mgt.util;

import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Utility class for workflow management.
 */
public class Utils {

    /**
     * Create PreparedStatement.
     *
     * @param connection db connection
     * @param sqlQuery SQL query
     * @param tenantId Tenant ID
     * @param filterResolvedForSQL resolved filter for sql
     * @param offset offset
     * @param limit limit
     * @return PreparedStatement
     * @throws SQLException
     * @throws DataAccessException
     */
    public static PreparedStatement generatePrepStmt(Connection connection, String sqlQuery, int tenantId,
                                               String filterResolvedForSQL, int offset, int limit)
            throws SQLException, DataAccessException {

        PreparedStatement prepStmt;
        if (JdbcUtils.isPostgreSQLDB()) {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
        } else {
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, filterResolvedForSQL);
            prepStmt.setInt(3, offset);
            prepStmt.setInt(4, limit);
        }
        return prepStmt;
    }

}
