/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class to store and retrieve user related data.
 */
public class UserSessionStore {

    private static final Log log = LogFactory.getLog(UserSessionStore.class);

    private static UserSessionStore instance = new UserSessionStore();
    private static final String FEDERATED_USER_DOMAIN = "FEDERATED";

    private UserSessionStore() {
    }

    public static UserSessionStore getInstance() {
        return instance;
    }

    /**
     * Method to store user and session mapping.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @param idPId      Identity Provider id
     * @throws UserSessionException if an error occurs when storing the authenticated user details to the database
     */
    public void storeUserData(String userId, String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_INSERT_USER_STORE_OPERATION)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, userName);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setString(4, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain);
            preparedStatement.setInt(5, idPId);
            preparedStatement.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while storing authenticated user details to the database table " +
                    "IDN_AUTH_USER_STORE of user: " + userName + ", Tenant Id: " + tenantId + ", User domain: " +
                    userDomain + ", Identity provider id: " + idPId, e);
        }
    }

    /**
     * Method to get the unique Id of a user from the database.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @param idPId      Identity Provider id
     * @return the user id of the user
     * @throws UserSessionException if an error occurs when retrieving the user id of the user from the database
     */
    public String getUserId(String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        String userId = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_USER_ID)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain);
            preparedStatement.setInt(4, idPId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getString(1);
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving User Id of the user: " + userName + ", Tenant Id: "
                    + tenantId + ", User domain: " + userDomain + ", Identity provider id: " + idPId, e);
        }
        return userId;
    }

    /**
     * Method to return the user Id of a user from the database.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @return the user id of the user
     * @throws UserSessionException if an error occurs when retrieving the user id of the user from the database
     */
    public String getUserId(String userName, int tenantId, String userDomain) throws UserSessionException {

        String userId = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_USER_IDS_OF_USER)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getString(1);
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving User Id of the user: " + userName + ", Tenant Id: "
                    + tenantId + ", User domain: " + userDomain, e);
        }
        return userId;
    }

    /**
     * Method to return the user Ids of the users in a given user store from the database.
     *
     * @param userDomain Name of the user Store domain
     * @param tenantId   Id of the tenant domain
     * @return the list of user Ids of users stored in the given user store
     * @throws UserSessionException if an error occurs when retrieving the user id list from the database
     */
    public List<String> getUserIdsOfUserStore(String userDomain, int tenantId) throws UserSessionException {

        List<String> userIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_USER_IDS_OF_USER_STORE)) {
            preparedStatement.setString(1, userDomain);
            preparedStatement.setInt(2, tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userIds.add(resultSet.getString(1));
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving user Ids stored in the user domain: " + userDomain
                    + ", Tenant Id: " + tenantId, e);
        }
        return userIds;
    }

    /**
     * Method to identity providers id from the IDP table.
     *
     * @param idPName name of the identity provider
     * @return id of the identity provider
     * @throws UserSessionException if an error occurs when retrieving the identity provider id list from the database
     */
    public int getIdPId(String idPName) throws UserSessionException {

        int idPId = -1;
        if (idPName.equals("LOCAL")) {
            return idPId;
        }
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_IDP_ID_OF_IDP)) {
            preparedStatement.setString(1, idPName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    idPId = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving the IdP id of: " + idPName, e);
        }
        return idPId;
    }

    /**
     * Method to store user id and session id mapping in the database table IDN_AUTH_USER_SESSION_STORE.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @throws UserSessionException if an error occurs when storing the mapping in the database
     */
    public void storeUserSessionData(String userId, String sessionId) throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_INSERT_USER_SESSION_STORE_OPERATION)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, sessionId);
            preparedStatement.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while storing mapping between user Id: " + userId +
                    " and session Id: " + sessionId, e);
        }
    }

    /**
     * Method to check whether the user id and session id mapping is already exists in the database.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    public boolean isExistingMapping(String userId, String sessionId) throws UserSessionException {

        Boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_USER_SESSION_MAP)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, sessionId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isExisting = true;
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving existing mapping between user Id: " + userId
                    + " and session Id: " + sessionId, e);
        }
        return isExisting;
    }

    /**
     * Method to get session Id list of a given user Id.
     *
     * @param userId Id of the user
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    public List<String> getSessionId(String userId) throws UserSessionException {

        List<String> sessionIdList = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_SESSION_ID_OF_USER_ID)) {
            preparedStatement.setString(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    sessionIdList.add(resultSet.getString(1));
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving session Id of user Id: " + userId, e);
        }
        return sessionIdList;
    }

    /**
     * Method used to remove the expired sessions from the database table IDN_AUTH_USER_SESSION_STORE.
     */
    public void removeExpiredSessionRecords() {

        long cleanupLimitNano = FrameworkUtils.getCurrentStandardNano() -
                TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout());
        List<String> terminatedAuthSessionIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection();
             PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_TERMINATED_SESSION_IDS)) {
            preparedStatement.setLong(1, cleanupLimitNano);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    terminatedAuthSessionIds.add(resultSet.getString(1));
                }
            }

            for (String terminatedSessionId : terminatedAuthSessionIds) {
                try (PreparedStatement preparedStatementForDelete = connection
                        .prepareStatement(SQLQueries.SQL_DELETE_TERMINATED_SESSION_DATA)) {
                    preparedStatementForDelete.setString(1, terminatedSessionId);
                    preparedStatementForDelete.addBatch();
                    preparedStatementForDelete.executeBatch();
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            log.error("Error while removing the terminated sessions from the table IDN_AUTH_USER_SESSION_STORE."
                    , e);
        }
    }
}
