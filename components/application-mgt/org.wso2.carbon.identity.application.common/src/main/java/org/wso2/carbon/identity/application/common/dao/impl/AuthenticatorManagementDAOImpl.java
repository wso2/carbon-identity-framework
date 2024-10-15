/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.application.common.constant.AuthenticatorMgtConstants.ErrorMessages;
import org.wso2.carbon.identity.application.common.constant.AuthenticatorMgtSQLConstants;
import org.wso2.carbon.identity.application.common.dao.AuthenticatorManagementDAO;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtException;
import org.wso2.carbon.identity.application.common.exception.AuthenticatorMgtServerException;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class implements the AuthenticatorManagementDAO interface which perform CRUD operation on database.
 */
public class AuthenticatorManagementDAOImpl implements AuthenticatorManagementDAO {

    private static final Log LOG = LogFactory.getLog(AuthenticatorManagementDAOImpl.class);

    public AuthenticatorManagementDAOImpl() {
    }

    @Override
    public LocalAuthenticatorConfig addUserDefinedLocalAuthenticator(
            LocalAuthenticatorConfig authenticatorConfig, Integer tenantId) throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                    AuthenticatorMgtSQLConstants.Query.ADD_USER_DEFINED_AUTHENTICATOR)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.NAME, authenticatorConfig.getName());
            statement.setString(AuthenticatorMgtSQLConstants.Column.DISPLAY_NAME, authenticatorConfig.getDisplayName());
            statement.setString(AuthenticatorMgtSQLConstants.Column.DEFINED_BY,
                    authenticatorConfig.getDefinedByType().toString());
            statement.setString(AuthenticatorMgtSQLConstants.Column.AUTHENTICATOR_TYPE, action.getDescription());
            statement.setString(AuthenticatorMgtSQLConstants.Column.IS_ENABLED,
                    Boolean.toString(authenticatorConfig.isEnabled()));
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            addEndpointProperties(dbConnection, actionId, getEndpointProperties(action.getEndpoint().getUri(),
                    action.getEndpoint().getAuthentication().getType().name(), encryptedAuthProperties), tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getUserDefinedLocalAuthenticatorByName(dbConnection, authenticatorConfig.getName(), tenantId);
        } catch (SQLException | AuthenticatorMgtException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while adding the Authenticator of named: %s in Tenant Domain: %s. " +
                                "Rolling back added Authenticator information.", authenticatorConfig.getName(),
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);

            ErrorMessages error = ErrorMessages.ERROR_WHILE_ADDING_AUTHENTICATOR;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public LocalAuthenticatorConfig updateUserDefinedLocalAuthenticator(LocalAuthenticatorConfig
                existingAuthenticatorConfig, LocalAuthenticatorConfig updatedAuthenticatorConfig, Integer tenantId)
                throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                AuthenticatorMgtSQLConstants.Query.UPDATE_ACTION_BASIC_INFO)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.ACTION_NAME, StringUtils.isEmpty(updatingAction.getName())
                    ? existingAction.getName() : updatingAction.getName());
            statement.setString(AuthenticatorMgtSQLConstants.Column.ACTION_DESCRIPTION, updatingAction.getDescription() == null
                    ? existingAction.getDescription() : updatingAction.getDescription());
            statement.setString(AuthenticatorMgtSQLConstants.Column.ACTION_UUID, actionId);
            statement.setString(AuthenticatorMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            // Update Endpoint URI and Authentication.
            updateEndpointUriAndAuthentication(dbConnection, actionType, actionId, updatingAction, existingAction,
                    tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getUserDefinedLocalAuthenticatorByName(dbConnection, updatedAuthenticatorConfig.getName(), tenantId);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Authenticator of named: %s in Tenant Domain: %s. " +
                                "Rolling back updated Authenticator information.",
                                existingAuthenticatorConfig.getName(), IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            ErrorMessages error = ErrorMessages.ERROR_WHILE_UPDATING_AUTHENTICATOR;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public LocalAuthenticatorConfig getUserDefinedLocalAuthenticator(String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            LocalAuthenticatorConfig config = getUserDefinedLocalAuthenticatorByName(
                    dbConnection, authenticatorConfigName, tenantId);

            return config;
        } catch (SQLException e) {
            ErrorMessages error = ErrorMessages.ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        }
    }

    private LocalAuthenticatorConfig getUserDefinedLocalAuthenticatorByName(
            Connection dbConnection, String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtServerException {

        LocalAuthenticatorConfig config = null;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                AuthenticatorMgtSQLConstants.Query.GET_ACTION_BASIC_INFO_BY_ID)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.NAME, authenticatorConfigName);
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    config = new LocalAuthenticatorConfig();
                    // SET ALL PROP
                }
            }
            return config;
        } catch (SQLException e) {
            ErrorMessages error = ErrorMessages.ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        }
    }

    @Override
    public void deleteUserDefinedLocalAuthenticator(String authenticatorConfigName, Integer tenantId)
            throws AuthenticatorMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                AuthenticatorMgtSQLConstants.Query.DELETE_ACTION)) {

            statement.setString(AuthenticatorMgtSQLConstants.Column.NAME, authenticatorConfigName);
            statement.setInt(AuthenticatorMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while deleting the Authenticator of named: %s in Tenant Domain: %s. " +
                                "Rolling back deleted Authenticator information.", authenticatorConfigName,
                                IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            ErrorMessages error = ErrorMessages.ERROR_WHILE_DELETING_AUTHENTICATOR;
            throw new AuthenticatorMgtServerException(error.getMessage(), error.getMessage(), error.getCode(), e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }

    }
}
