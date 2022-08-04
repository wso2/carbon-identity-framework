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
import org.wso2.carbon.identity.user.profile.mgt.AssociatedAccountDTO;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.identity.user.profile.mgt.util.Constants;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
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
            IdentityDatabaseUtil.commitTransaction(dbConnection);

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new UserProfileException(String.format("Database error occurred while deleting associated ids with " +
                                                         "domain '%s'", domainName), e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(preparedStatement);
            IdentityApplicationManagementUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Persist an association entry for the given user with the given federated identifier.
     *
     * @param tenantId           tenant identifier
     * @param userStoreDomain    user store domain name
     * @param domainFreeUsername username without the domain
     * @param idpId              identity provider id
     * @param federatedUserId    federated identity id
     * @throws UserProfileException
     */
    public void createAssociation(int tenantId, String userStoreDomain, String domainFreeUsername, String idpId,
                                  String federatedUserId) throws UserProfileException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement prepStmt = connection
                    .prepareStatement(Constants.SQLQueries.ASSOCIATE_USER_ACCOUNTS)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.setString(2, idpId);
                prepStmt.setInt(3, tenantId);
                prepStmt.setString(4, federatedUserId);
                prepStmt.setString(5, userStoreDomain.toUpperCase());
                prepStmt.setString(6, domainFreeUsername);
                prepStmt.setString(7, generateUUID());
                prepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserProfileException("Error occurred while persisting account association entry for user: " +
                        domainFreeUsername + " of user store domain: " + userStoreDomain + " in tenant: " + tenantId +
                        " for federated ID: " + federatedUserId + " of IdP: " + idpId, e1);
            }
        } catch (SQLException e) {
            throw new UserProfileException("Error occurred while persisting account association entry for user: " +
                    domainFreeUsername + " of user store domain: " + userStoreDomain + " in tenant: " + tenantId + " " +
                    "for federated ID: " + federatedUserId + " of IdP: " + idpId, e);
        }
    }

    /**
     * Delete the association entry for the given user with the given federated identifier.
     *
     * @param tenantId           tenant identifier
     * @param userStoreDomain    user store domain name
     * @param domainFreeUsername username without user store domain
     * @param idpId              identity provider id
     * @param federatedUserId    federated identity id
     * @throws UserProfileException
     */
    public void deleteAssociation(int tenantId, String userStoreDomain, String domainFreeUsername, String idpId,
                                  String federatedUserId) throws UserProfileException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement prepStmt = connection
                    .prepareStatement(Constants.SQLQueries.DELETE_ASSOCIATION)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.setString(2, idpId);
                prepStmt.setInt(3, tenantId);
                prepStmt.setString(4, federatedUserId);
                prepStmt.setString(5, domainFreeUsername);
                prepStmt.setString(6, userStoreDomain);

                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserProfileException("Error occurred while removing account association entry of user: " +
                        domainFreeUsername + " of user store domain: " + userStoreDomain + " in tenant: " + tenantId +
                        " with federated ID: " + federatedUserId + " of IdP: " + idpId, e1);
            }
        } catch (SQLException e) {
            throw new UserProfileException("Error occurred while removing account association entry of user: " +
                    domainFreeUsername + " of user store domain: " + userStoreDomain + " in tenant: " + tenantId + " " +
                    "with federated ID: " + federatedUserId + " of IdP: " + idpId, e);
        }
    }

    /**
     * Delete the federated association entries for the given user.
     *
     * @param tenantId        Tenant identifier.
     * @param userStoreDomain User store domain name.
     * @param username        Username without user store domain.
     * @throws UserProfileException {@link UserProfileException}.
     */
    public void deleteFederatedAssociation(int tenantId, String userStoreDomain, String username)
            throws UserProfileException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement prepStmt = connection.prepareStatement(
                    Constants.SQLQueries.DELETE_ALL_ASSOCIATIONS_FOR_USER)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.setString(2, userStoreDomain);
                prepStmt.setString(3, username);
                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserProfileException("Error occurred while removing federated association entries of user: " +
                        username + ", of user store domain: " + userStoreDomain + ", in tenant: " + tenantId, e1);
            }
        } catch (SQLException e) {
            throw new UserProfileException("Error occurred while handling the database connection", e);
        }
    }

    /**
     * Delete the federated association entry of the given user by given federated identifier.
     *
     * @param userStoreDomain        User store domain name for the user.
     * @param username               The domain free, user name for the user.
     * @param federatedAssociationId Identifier value for the corresponding federated association. This value
     *                               stands for a unique federated association, which is composed of tenant Id, idpId
     *                               and federatedUserId.
     * @throws UserProfileException {@link UserProfileException}.
     */
    public void deleteFederatedAssociation(String userStoreDomain, String username, String federatedAssociationId)
            throws UserProfileException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try (PreparedStatement prepStmt = connection.prepareStatement(
                    Constants.SQLQueries.DELETE_ASSOCIATION_FOR_USER_BY_ID)) {
                prepStmt.setString(1, userStoreDomain);
                prepStmt.setString(2, username);
                prepStmt.setString(3, federatedAssociationId);
                prepStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserProfileException("Error occurred while removing federated association: "
                        + federatedAssociationId + ", for the user: " + username + ", in the user store " +
                        "domain: " + userStoreDomain, e1);
            }
        } catch (SQLException e) {
            throw new UserProfileException("Error occurred while handling the database connection", e);
        }
    }

    /**
     * Get association entry for the given federated identifier.
     *
     * @param tenantId        tenant identifier
     * @param idpId           identity provider id
     * @param federatedUserId federated identity id
     * @return username of the user associated with the given federated identity id
     * @throws UserProfileException
     */
    public String getUserAssociatedFor(int tenantId, String idpId, String federatedUserId) throws UserProfileException {

        String username = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt =
                connection.prepareStatement(Constants.SQLQueries.RETRIEVE_USER_ASSOCIATED)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.setString(2, idpId);
                prepStmt.setInt(3, tenantId);
                prepStmt.setString(4, federatedUserId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        String domainName = resultSet.getString(1);
                        username = resultSet.getString(2);
                        if (!UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domainName)) {
                            username = UserCoreUtil.addDomainToName(username, domainName);
                        }
                        return username;
                    }
                }
            } catch (SQLException e1) {
                throw new UserProfileException("Error occurred while retrieving user account associated for federated "
                        + "ID: " + federatedUserId + " of IdP: " + idpId + " for tenant: " + tenantId, e1);
            }
        } catch (SQLException e) {
            throw new UserProfileException("Error occurred while retrieving user account associated for federated " +
                    "ID: " + federatedUserId + " of IdP: " + idpId + " for tenant: " + tenantId, e);
        }

        return username;
    }

    /**
     * Retun a list of federated identities associated with the given user.
     *
     * @param tenantId           tenant identifier
     * @param userStoreDomain    user store domain name
     * @param domainFreeUsername username without user store domain
     * @return a list of AssociatedAccountDTO objects which includes federated identity info
     * @throws UserProfileException
     */
    public List<AssociatedAccountDTO> getAssociatedFederatedAccountsForUser(int tenantId, String userStoreDomain,
                                                                            String domainFreeUsername) throws
            UserProfileException {

        List<AssociatedAccountDTO> associatedFederatedAccounts = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
                try (PreparedStatement prepStmt = connection
                .prepareStatement(Constants.SQLQueries.RETRIEVE_ASSOCIATIONS_FOR_USER)) {
                    prepStmt.setInt(1, tenantId);
                    prepStmt.setString(2, domainFreeUsername);
                    prepStmt.setString(3, userStoreDomain);

                    try (ResultSet resultSet = prepStmt.executeQuery()) {
                        while (resultSet.next()) {
                            associatedFederatedAccounts.add(new AssociatedAccountDTO(
                                    resultSet.getString("ASSOCIATION_ID"),
                                    resultSet.getInt("ID"),
                                    resultSet.getString("NAME"),
                                    resultSet.getString("IDP_USER_ID")
                            ));
                        }
                    }
                } catch (SQLException ex) {
                    throw new UserProfileException("Error occurred while retrieving federated accounts associated for "
                            + "user: " + domainFreeUsername + " of user store domain: " + userStoreDomain +
                            " in tenant: " + tenantId, ex);
                }
        } catch (SQLException e) {
            throw new UserProfileException("Error occurred while retrieving federated accounts associated for " +
                    "user: " + domainFreeUsername + " of user store domain: " + userStoreDomain + " in tenant: " +
                    tenantId, e);
        }

        return associatedFederatedAccounts;
    }

    private static class LazyHolder {

        private static final UserProfileMgtDAO INSTANCE = new UserProfileMgtDAO();
    }

    private String generateUUID() {

        return UUID.randomUUID().toString();
    }
}
