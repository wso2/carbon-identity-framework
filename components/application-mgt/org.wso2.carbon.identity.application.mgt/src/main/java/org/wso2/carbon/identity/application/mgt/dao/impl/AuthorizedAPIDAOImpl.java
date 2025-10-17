/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.api.resource.mgt.util.AuthorizationDetailsTypesUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.dao.AuthorizedAPIDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authorized API DAO implementation class.
 */
public class AuthorizedAPIDAOImpl implements AuthorizedAPIDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAuthorizedAPI(String applicationId, String apiId, String policyId,
                                 List<Scope> scopes, int tenantId) throws IdentityApplicationManagementException {

        this.addAuthorizedAPI(applicationId, apiId, policyId, scopes, Collections.emptyList(), tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuthorizedAPI> getAuthorizedAPIs(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            PreparedStatement prepStmt = dbConnection.prepareStatement(buildGetAuthorizedAPIsSqlStatement());
            prepStmt.setString(1, applicationId);
            ResultSet resultSet = prepStmt.executeQuery();
            Map<String, AuthorizedAPI> authorizedAPIMap = new HashMap<>();
            while (resultSet.next()) {
                String apiId = resultSet.getString(ApplicationConstants.ApplicationTableColumns.API_ID);
                String scopeName = resultSet.getString(ApplicationConstants.ApplicationTableColumns.SCOPE_NAME);
                Scope scope = null;
                if (scopeName != null) {
                    scope = new Scope.ScopeBuilder().name(scopeName).build();
                }
                final AuthorizationDetailsType type = this.buildAuthorizationDetailsType(resultSet);

                if (!authorizedAPIMap.containsKey(apiId)) {
                    AuthorizedAPI.AuthorizedAPIBuilder authorizedAPIBuilder = new AuthorizedAPI.AuthorizedAPIBuilder()
                            .appId(applicationId)
                            .apiId(apiId)
                            .policyId(resultSet.getString(
                                    ApplicationConstants.ApplicationTableColumns.POLICY_ID))
                            .scopes(scope == null ? Collections.emptyList() : Collections.singletonList(scope))
                            .authorizationDetailsTypes(type == null
                                    ? Collections.emptyList() : Collections.singletonList(type));
                    authorizedAPIMap.put(apiId, authorizedAPIBuilder.build());
                } else {
                    AuthorizedAPI authorizedAPI = authorizedAPIMap.get(apiId);
                    addScopeToAuthorizedApi(authorizedAPI, scope);
                    addAuthorizationDetailsTypeToAuthorizedApi(authorizedAPI, type);
                }
            }
            return authorizedAPIMap.values().isEmpty() ? new ArrayList<>() : new ArrayList<>(authorizedAPIMap.values());
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while fetching authorized API.", e);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                   List<String> removedScopes, int tenantId)
            throws IdentityApplicationManagementException {

        this.patchAuthorizedAPI(appId, apiId, addedScopes, removedScopes, Collections.emptyList(),
                Collections.emptyList(), tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAuthorizedAPI(String appId, String apiId, int tenantId)
            throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            PreparedStatement prepStmt = dbConnection.prepareStatement(
                    ApplicationMgtDBQueries.DELETE_AUTHORIZED_API_BY_API_ID);
            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);
            prepStmt.execute();
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while deleting the authorized API.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuthorizedScopes> getAuthorizedScopes(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            PreparedStatement prepStmt = dbConnection.prepareStatement(
                    ApplicationMgtDBQueries.GET_AUTHORIZED_SCOPES);
            prepStmt.setString(1, applicationId);
            ResultSet resultSet = prepStmt.executeQuery();
            Map<String, AuthorizedScopes> authorizedScopesMap = new HashMap<>();
            while (resultSet.next()) {
                String policyId = resultSet.getString(ApplicationConstants.ApplicationTableColumns.POLICY_ID);
                if (!authorizedScopesMap.containsKey(policyId)) {
                    AuthorizedScopes.AuthorizedScopesBuilder authorizedScopesBuilder =
                            new AuthorizedScopes.AuthorizedScopesBuilder()
                                    .policyId(policyId)
                                    .scopes(Collections.singletonList(resultSet.getString(
                                            ApplicationConstants.ApplicationTableColumns.SCOPE_NAME)));
                    authorizedScopesMap.put(policyId, authorizedScopesBuilder.build());
                } else {
                    AuthorizedScopes authorizedScopes = authorizedScopesMap.get(policyId);
                    List<String> scopes = new ArrayList<>(authorizedScopes.getScopes());
                    scopes.add(resultSet.getString(ApplicationConstants.ApplicationTableColumns.SCOPE_NAME));
                    authorizedScopes.setScopes(scopes);
                }
            }
            return authorizedScopesMap.values().isEmpty() ? new ArrayList<>() :
                    new ArrayList<>(authorizedScopesMap.values());
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while getting authorized scopes.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizedAPI getAuthorizedAPI(String applicationId, String apiId, int tenantId)
            throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            PreparedStatement prepStmt = dbConnection.prepareStatement(buildGetAuthorizedAPISqlStatement());
            prepStmt.setString(1, applicationId);
            prepStmt.setString(2, apiId);
            ResultSet resultSet = prepStmt.executeQuery();
            AuthorizedAPI authorizedAPI = null;
            while (resultSet.next()) {
                if (authorizedAPI == null) {
                    authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                            .apiId(apiId)
                            .appId(applicationId)
                            .policyId(resultSet.getString(ApplicationConstants.ApplicationTableColumns.POLICY_ID))
                            .build();
                    if (resultSet.getString(ApplicationConstants.ApplicationTableColumns.SCOPE_NAME) != null) {
                        Scope scope = new Scope.ScopeBuilder()
                                .name(resultSet.getString(ApplicationConstants.ApplicationTableColumns.SCOPE_NAME))
                                .build();
                        authorizedAPI.setScopes(Collections.singletonList(scope));
                    }
                    addAuthorizationDetailsTypeToAuthorizedApi(authorizedAPI, buildAuthorizationDetailsType(resultSet));
                } else {
                    Scope scope = new Scope.ScopeBuilder()
                            .name(resultSet.getString(ApplicationConstants.ApplicationTableColumns.SCOPE_NAME)).build();
                    addScopeToAuthorizedApi(authorizedAPI, scope);
                    addAuthorizationDetailsTypeToAuthorizedApi(authorizedAPI, buildAuthorizationDetailsType(resultSet));
                }
            }
            return authorizedAPI;
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while getting authorized API.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAuthorizedAPI(String applicationId, AuthorizedAPI authorizedAPI, int tenantId)
            throws IdentityApplicationManagementException {

        this.addAuthorizedAPI(applicationId, authorizedAPI.getAPIId(), authorizedAPI.getPolicyId(),
                authorizedAPI.getScopes(), authorizedAPI.getAuthorizationDetailsTypes(), tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> scopesToAdd, List<String> scopesToRemove,
                                   List<String> authorizationDetailsTypesToAdd,
                                   List<String> authorizationDetailsTypesToRemove, int tenantId)
            throws IdentityApplicationManagementException {

        int tenantIdToSearchScopes = isOrganization(tenantId) ? getPrimaryOrgTenantID(tenantId) : tenantId;
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                this.addScopes(dbConnection, appId, apiId, scopesToAdd, tenantIdToSearchScopes);

                this.deleteScopes(dbConnection, appId, apiId, scopesToRemove, tenantIdToSearchScopes);

                this.addAuthorizedAuthorizationDetailsTypes(dbConnection, appId, apiId,
                        authorizationDetailsTypesToAdd, tenantId);

                this.deleteAuthorizedAuthorizationDetailsTypes(dbConnection, appId, apiId,
                        authorizationDetailsTypesToRemove, tenantId);

                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            if (isScopeConflict(e)) {
                throw new IdentityApplicationManagementClientException(
                        "API resource or scopes are already authorized", e);
            }
            throw new IdentityApplicationManagementException("Error while adding authorized API. Caused by, ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuthorizationDetailsType> getAuthorizedAuthorizationDetailsTypes(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return authorizationDetailsTypes;
        }

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = dbConnection
                     .prepareStatement(ApplicationMgtDBQueries.GET_AUTHORIZED_AUTHORIZATION_DETAILS_TYPES)) {

            prepStmt.setString(1, applicationId);
            ResultSet resultSet = prepStmt.executeQuery();

            while (resultSet.next()) {
                AuthorizationDetailsType authorizationDetailsType =
                        this.buildAuthorizationDetailsTypeWithSchema(resultSet);
                if (authorizationDetailsType != null) {
                    authorizationDetailsTypes.add(authorizationDetailsType);
                }
            }

            return authorizationDetailsTypes;
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while retrieving " +
                    "authorized authorization details types. Caused by, ", e);
        }
    }

    private void addAuthorizedAPI(String applicationId, String apiId, String policyId, List<Scope> scopes,
                                  List<AuthorizationDetailsType> authorizationDetailsTypes, int tenantId)
            throws IdentityApplicationManagementException {

        int tenantIdToSearchScopes = isOrganization(tenantId) ? getPrimaryOrgTenantID(tenantId) : tenantId;
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_API);
                prepStmt.setString(1, applicationId);
                prepStmt.setString(2, apiId);
                prepStmt.setString(3, policyId);
                prepStmt.execute();

                if (CollectionUtils.isNotEmpty(scopes)) {
                    prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE);
                    boolean isSystemAPI = APIResourceManagementUtil.isSystemAPIByAPIId(apiId);
                    for (Scope scope : scopes) {
                        prepStmt.setString(1, applicationId);
                        prepStmt.setString(2, apiId);
                        prepStmt.setString(3, scope.getName());
                        prepStmt.setObject(4, isSystemAPI ? null : tenantIdToSearchScopes);
                        prepStmt.addBatch();
                        prepStmt.clearParameters();
                    }
                    prepStmt.executeBatch();
                }

                if (CollectionUtils.isNotEmpty(authorizationDetailsTypes)) {
                    final List<String> authorizationDetailsTypesToAdd = authorizationDetailsTypes.stream()
                            .map(AuthorizationDetailsType::getType).collect(Collectors.toList());
                    this.addAuthorizedAuthorizationDetailsTypes(dbConnection, applicationId, apiId,
                            authorizationDetailsTypesToAdd, tenantId);
                }

                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException | APIResourceMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            if (isScopeConflict(e)) {
                throw new IdentityApplicationManagementClientException(
                        "API resource or scopes are already authorized", e);
            }
            throw new IdentityApplicationManagementException("Error while adding authorized API. Caused by, ", e);
        } catch (APIResourceMgtException e) {
            throw new IdentityApplicationManagementException("Error while adding authorized API. Caused by, ", e);
        }
    }

    private boolean isScopeConflict(SQLException e) {

        if (e instanceof SQLIntegrityConstraintViolationException ||
            (e.getNextException() instanceof SQLIntegrityConstraintViolationException)) {
            return true;
        }
        // Handle constraint violations in JDBC drivers which don't throw SQLIntegrityConstraintViolationException.
        return e.getMessage() != null && e.getMessage().toUpperCase().contains("AUTHORIZED_SCOPE_UNIQUE");
    }

    private void addScopes(Connection dbConnection, String appId, String apiId, List<String> scopesToAdd, int tenantId)
            throws SQLException {

        if (CollectionUtils.isNotEmpty(scopesToAdd)) {
            PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE);
            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);
            prepStmt.setObject(4, tenantId == 0 ? null : tenantId);
            for (String scope : scopesToAdd) {
                prepStmt.setString(3, scope);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    private void deleteScopes(Connection dbConnection, String appId, String apiId,
                              List<String> scopesToRemove, int tenantId) throws SQLException {

        if (CollectionUtils.isNotEmpty(scopesToRemove)) {
            PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.DELETE_AUTHORIZED_SCOPE);
            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);
            prepStmt.setInt(4, tenantId);
            for (String scope : scopesToRemove) {
                prepStmt.setString(3, scope);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    /**
     * Adds a list of authorized authorization details types for a given application and API.
     *
     * @param dbConnection                   The database connection.
     * @param appId                          The ID of the application.
     * @param apiId                          The ID of the API.
     * @param authorizationDetailsTypesToAdd The list of authorization details types to be added.
     * @param tenantId                       The tenant ID.
     * @throws SQLException If an error occurs while executing the database operations.
     */
    private void addAuthorizedAuthorizationDetailsTypes(Connection dbConnection, String appId, String apiId,
                                                        List<String> authorizationDetailsTypesToAdd, int tenantId)
            throws SQLException {

        if (CollectionUtils.isEmpty(authorizationDetailsTypesToAdd) ||
                AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return;
        }

        try (PreparedStatement prepStmt = dbConnection
                .prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_AUTHORIZATION_DETAILS_TYPES)) {

            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);
            prepStmt.setObject(4, tenantId == 0 ? null : tenantId);
            for (String detailsType : authorizationDetailsTypesToAdd) {
                prepStmt.setString(3, detailsType);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    /**
     * Deletes a list of authorized authorization details types for a given application and API.
     *
     * @param dbConnection                      The database connection.
     * @param appId                             The ID of the application.
     * @param apiId                             The ID of the API.
     * @param authorizationDetailsTypesToRemove The list of authorization details types to be removed.
     * @param tenantId                          The tenant ID.
     * @throws SQLException If an error occurs while executing the database operations.
     */
    private void deleteAuthorizedAuthorizationDetailsTypes(Connection dbConnection, String appId, String apiId,
                                                           List<String> authorizationDetailsTypesToRemove, int tenantId)
            throws SQLException {

        if (CollectionUtils.isEmpty(authorizationDetailsTypesToRemove) ||
                AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return;
        }

        try (PreparedStatement prepStmt = dbConnection
                .prepareStatement(ApplicationMgtDBQueries.DELETE_AUTHORIZED_AUTHORIZATION_DETAILS_TYPES)) {

            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);
            prepStmt.setObject(4, tenantId == 0 ? null : tenantId);
            for (String detailsType : authorizationDetailsTypesToRemove) {
                prepStmt.setString(3, detailsType);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        }
    }

    /**
     * Builds an {@link AuthorizationDetailsType} object by populating its fields using the provided {@link ResultSet}.
     * Additionally, sets the schema for the {@link AuthorizationDetailsType} object by parsing the
     * schema information from the result set.
     *
     * @param resultSet The result set containing search data.
     * @return An {@link AuthorizationDetailsType} object with schema set, or {@code null} if no valid
     * authorization details type can be built.
     * @throws SQLException If an error occurs while retrieving data from the result set.
     */
    private AuthorizationDetailsType buildAuthorizationDetailsTypeWithSchema(ResultSet resultSet) throws SQLException {

        final AuthorizationDetailsType authorizationDetailsType = this.buildAuthorizationDetailsType(resultSet);

        if (authorizationDetailsType != null) {
            authorizationDetailsType.setSchema(AuthorizationDetailsTypesUtil.parseSchema(resultSet
                    .getString(ApplicationConstants.ApplicationTableColumns.AUTHORIZATION_DETAILS_SCHEMA)));
        }

        return authorizationDetailsType;
    }

    /**
     * Builds an {@link AuthorizationDetailsType} object using the provided {@link ResultSet}.
     *
     * @param resultSet The result set containing search data.
     * @return An {@link AuthorizationDetailsType} object if the required fields are present and
     * rich authorization requests are enabled, otherwise {@code null}.
     * @throws SQLException If an error occurs while retrieving data from the result set.
     */
    private AuthorizationDetailsType buildAuthorizationDetailsType(final ResultSet resultSet) throws SQLException {

        if (AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()) {
            return null;
        }

        final String authorizationDetailsTypeId =
                resultSet.getString(ApplicationConstants.ApplicationTableColumns.AUTHORIZATION_DETAILS_ID);

        if (StringUtils.isBlank(authorizationDetailsTypeId)) {
            return null;
        }

        return new AuthorizationDetailsType.AuthorizationDetailsTypesBuilder()
                .id(authorizationDetailsTypeId)
                .type(resultSet.getString(ApplicationConstants.ApplicationTableColumns.AUTHORIZATION_DETAILS_TYPE))
                .name(resultSet.getString(ApplicationConstants.ApplicationTableColumns.AUTHORIZATION_DETAILS_NAME))
                .build();
    }

    private void addScopeToAuthorizedApi(AuthorizedAPI authorizedAPI, Scope newScope) {

        if (newScope == null) {
            return;
        }

        final List<Scope> scopes = authorizedAPI.getScopes() == null
                ? new ArrayList<>()
                : new ArrayList<>(authorizedAPI.getScopes());

        scopes.removeIf(scope -> scope.getName().equals(newScope.getName()));
        scopes.add(newScope);
        authorizedAPI.setScopes(scopes);
    }

    private void addAuthorizationDetailsTypeToAuthorizedApi(final AuthorizedAPI authorizedAPI,
                                                            final AuthorizationDetailsType authorizationDetailsType) {

        if (authorizationDetailsType == null || authorizedAPI == null) {
            return;
        }

        final List<AuthorizationDetailsType> authorizationDetailsTypes =
                CollectionUtils.isEmpty(authorizedAPI.getAuthorizationDetailsTypes()) ? new ArrayList<>()
                        : new ArrayList<>(authorizedAPI.getAuthorizationDetailsTypes());

        authorizationDetailsTypes.removeIf(type -> StringUtils.isBlank(type.getId()));
        authorizationDetailsTypes.removeIf(type -> type.getType().equals(authorizationDetailsType.getType()));

        authorizationDetailsTypes.add(authorizationDetailsType);
        authorizedAPI.setAuthorizationDetailsTypes(authorizationDetailsTypes);
    }

    private static String buildGetAuthorizedAPISqlStatement() {

        return AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()
                ? ApplicationMgtDBQueries.GET_AUTHORIZED_API
                : ApplicationMgtDBQueries.GET_AUTHORIZED_API_WITH_AUTHORIZATION_DETAILS;
    }

    private static String buildGetAuthorizedAPIsSqlStatement() {

        return AuthorizationDetailsTypesUtil.isRichAuthorizationRequestsDisabled()
                ? ApplicationMgtDBQueries.GET_AUTHORIZED_APIS
                : ApplicationMgtDBQueries.GET_AUTHORIZED_APIS_WITH_AUTHORIZATION_DETAILS;
    }

    /**
     * Check whether the tenant id is an organization.
     *
     * @param tenantId Tenant Id.
     * @return True if the tenant id is an organization.
     * @throws IdentityApplicationManagementException If an error occurred while checking whether the tenant id
     * is an organization.
     */
    private boolean isOrganization(int tenantId) throws IdentityApplicationManagementException {

        try {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            return OrganizationManagementUtil.isOrganization(tenantDomain);
        } catch (OrganizationManagementException e) {
            throw new IdentityApplicationManagementException("Error while checking whether the tenant id is an " +
                    "organization.", e);
        }
    }

    /**
     * Get the primary org tenant id of the org with given tenant id.
     *
     * @param tenantId Tenant Id.
     * @return Primary org tenant id.
     * @throws IdentityApplicationManagementException If an error occurred while getting primary org tenant id.
     */
    private int getPrimaryOrgTenantID(int tenantId) throws IdentityApplicationManagementException {

        int primaryOrgTenantId;
        try {
            OrganizationManager organizationManager =
                    ApplicationManagementServiceComponentHolder.getInstance().getOrganizationManager();
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            String orgId = organizationManager.resolveOrganizationId(tenantDomain);
            String primaryOrgId = organizationManager.getPrimaryOrganizationId(orgId);
            String primaryOrgTenantDomain = organizationManager.resolveTenantDomain(primaryOrgId);
            primaryOrgTenantId = IdentityTenantUtil.getTenantId(primaryOrgTenantDomain);
        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while retrieving the primary organization tenant domain for the tenant " +
                    "id: " + tenantId;
            throw new IdentityApplicationManagementException(errorMessage, e);
        }
        return primaryOrgTenantId;
    }
}
