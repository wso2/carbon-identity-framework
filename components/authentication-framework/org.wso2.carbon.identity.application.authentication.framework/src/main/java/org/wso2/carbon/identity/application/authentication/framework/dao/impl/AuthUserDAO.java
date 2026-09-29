/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the authentication user records of {@code IDN_AUTH_USER}, which stay in the relational
 * identity store regardless of the configured session store.
 */
public class AuthUserDAO {

    private static final AuthUserDAO instance = new AuthUserDAO();
    private static final String FEDERATED_USER_DOMAIN = "FEDERATED";
    private static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";
    private static final String COLUMN_USER_ID = "USER_ID";
    private static final String COLUMN_IDP_ID = "IDP_ID";

    private AuthUserDAO() {

    }

    public static AuthUserDAO getInstance() {

        return instance;
    }

    /**
     * Store a user record in {@code IDN_AUTH_USER}.
     *
     * @param userId     Unique user identifier.
     * @param userName   Username of the authenticated user.
     * @param tenantId   Tenant identifier.
     * @param userDomain User store domain name; defaults to {@code "FEDERATED"} if {@code null}.
     * @param idPId      Identity provider identifier.
     * @throws UserSessionException if the record could not be written, or a duplicate entry is found.
     */
    public void storeUserData(String userId, String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            jdbcTemplate.executeUpdate(SQLQueries.SQL_INSERT_USER_STORE_OPERATION, preparedStatement -> {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, userName);
                preparedStatement.setInt(3, tenantId);
                preparedStatement.setString(4, userDomain == null ? FEDERATED_USER_DOMAIN : userDomain.toUpperCase());
                preparedStatement.setInt(5, idPId);
            });
        } catch (DataAccessException e) {
            throw new DuplicatedAuthUserException("Error when store user data.", e);
        }
    }

    /**
     * Retrieve the unique user identifier of a user from {@code IDN_AUTH_USER}.
     *
     * @param userName   Username of the authenticated user.
     * @param tenantId   Tenant identifier.
     * @param userDomain User store domain name; defaults to {@code "FEDERATED"} if {@code null}.
     * @param idPId      Identity provider identifier.
     * @return the user identifier, or {@code null} if no matching record exists.
     * @throws UserSessionException if the lookup fails.
     */
    public String getUserId(String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            return jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_USER_ID,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    preparedStatement -> {
                        preparedStatement.setString(1, userName);
                        preparedStatement.setInt(2, tenantId);
                        preparedStatement.setString(3, userDomain == null ? FEDERATED_USER_DOMAIN :
                                userDomain.toUpperCase());
                        preparedStatement.setInt(4, idPId);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException(
                    "Error while retrieving User Id of the user: " + userName + ", Tenant Id: " + tenantId +
                    ", User domain: " + userDomain + ", Identity provider id: " + idPId, e);
        }
    }

    /**
     * Check whether the given user identifier exists in {@code IDN_AUTH_USER}.
     *
     * @param userId User identifier to check.
     * @return {@code true} if the user record exists.
     * @throws UserSessionException if the lookup fails.
     */
    public boolean isExistingUser(String userId) throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            return jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_INFO_OF_USER_ID,
                    (resultSet, rowNumber) -> resultSet.getString(1),
                    preparedStatement -> preparedStatement.setString(1, userId)) != null;
        } catch (DataAccessException e) {
            throw new UserSessionException(
                    "Error while retrieving information of user id: " + userId, e);
        }
    }

    /**
     * Retrieve the user and their associated identity provider from {@code IDN_AUTH_USER}.
     *
     * @param userId User identifier.
     * @return the authenticated user with username, tenant domain, user store domain, and federated IDP name;
     *         {@code null} if no record is found.
     * @throws UserSessionException if the lookup fails.
     */
    public AuthenticatedUser getUser(String userId) throws UserSessionException {

        if (StringUtils.isBlank(userId)) {
            throw new UserSessionException("Invalid userId: userId cannot be null or empty.");
        }
        AuthenticatedUser user = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement ps = connection.prepareStatement(SQLQueries.SQL_SELECT_USER_FROM_USER_ID)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new AuthenticatedUser();
                    user.setUserName(rs.getString(1));
                    user.setTenantDomain(IdentityTenantUtil.getTenantDomain(rs.getInt(2)));
                    user.setUserStoreDomain(rs.getString(3));
                    user.setFederatedIdPName(rs.getString(4));
                }
            }
        } catch (SQLException e) {
            throw new UserSessionException(
                    "Error while retrieving information of user id: " + userId, e);
        }
        return user;
    }

    /**
     * Retrieve the identity provider of each of the given users in a single query.
     *
     * @param userIds User identifiers.
     * @return the identity provider identifier by user identifier, without the users that have no record.
     * @throws UserSessionException if the lookup fails.
     */
    public Map<String, String> getIdpIdsByUserIds(List<String> userIds) throws UserSessionException {

        Map<String, String> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        String placeholder = String.join(", ", Collections.nCopies(userIds.size(), "?"));
        String query = SQLQueries.SQL_GET_IDP_IDS_BY_USER_ID_LIST.replace(SCOPE_LIST_PLACEHOLDER, placeholder);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            jdbcTemplate.executeQuery(query,
                    (resultSet, rowNumber) -> result.put(resultSet.getString(COLUMN_USER_ID),
                            Integer.toString(resultSet.getInt(COLUMN_IDP_ID))),
                    preparedStatement -> {
                        for (int i = 0; i < userIds.size(); i++) {
                            preparedStatement.setString(i + 1, userIds.get(i));
                        }
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving IDP IDs for user IDs.", e);
        }
        return result;
    }
}
