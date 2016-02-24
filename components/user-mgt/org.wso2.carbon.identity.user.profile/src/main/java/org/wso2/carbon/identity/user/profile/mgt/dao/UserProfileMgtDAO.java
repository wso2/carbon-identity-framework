/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.user.profile.mgt.dao;

import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.identity.user.profile.mgt.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserProfileMgtDAO {

    private UserProfileMgtDAO() {

    }

    public static UserProfileMgtDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void updateDomainNameOfAssociations(int tenantId, String currentDomainName, String newDomainName) throws
                                                                                                             UserProfileException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(Constants.SQLQueries.UPDATE_USER_DOMAIN_NAME);

            preparedStatement.setString(1, newDomainName);
            preparedStatement.setString(2, currentDomainName);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.executeUpdate();

            if (!dbConnection.getAutoCommit()) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            throw new UserProfileException(String.format("Database error occurred while updating user domain of " +
                                                         "associated ids with domain '%s'", currentDomainName), e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(preparedStatement);
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
    }

    public void deleteAssociationsFromDomain(int tenantId, String domainName) throws
                                                                              UserProfileException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = dbConnection.prepareStatement(Constants.SQLQueries.DELETE_ASSOCIATED_ID_FROM_DOMAIN);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, domainName);
            preparedStatement.executeUpdate();

            if (!dbConnection.getAutoCommit()) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            throw new UserProfileException(String.format("Database error occurred while deleting associated ids with " +
                                                         "domain '%s'", domainName), e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(preparedStatement);
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
    }

    private static class LazyHolder {
        private static final UserProfileMgtDAO INSTANCE = new UserProfileMgtDAO();
    }
}
