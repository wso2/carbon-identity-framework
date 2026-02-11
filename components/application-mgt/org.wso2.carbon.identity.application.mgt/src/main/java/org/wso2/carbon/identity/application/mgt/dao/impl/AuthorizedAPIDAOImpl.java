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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
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
import org.wso2.carbon.identity.application.mgt.util.ScopeAuthorizationInfo;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.GET_AUTHORIZED_API_POLICY_ID;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.API_ID_PREFIX;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.COLUMN_NAME_API_ID;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.COLUMN_NAME_APP_ID;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.COLUMN_NAME_ID;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.COLUMN_NAME_NAME;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.COLUMN_NAME_SCOPE_ID;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_API_IDS;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_SCOPE_IDS;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_SCOPE_NAMES;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.SCOPE_ID_PREFIX;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.SCOPE_NAME_PREFIX;

/**
 * Authorized API DAO implementation class.
 */
public class AuthorizedAPIDAOImpl implements AuthorizedAPIDAO {

    private static final Log LOG = LogFactory.getLog(AuthorizedAPIDAOImpl.class);

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
        } catch (SQLException | APIResourceMgtException e) {
            if (e instanceof SQLException && isScopeConflict((SQLException) e)) {
                throw new IdentityApplicationManagementClientException("API resource or scopes are already authorized",
                        e);
            }
            throw new IdentityApplicationManagementException("SQL Error while patching authorized API. Caused by, ", e);
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
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            try {
                try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                        ApplicationMgtDBQueries.ADD_AUTHORIZED_API)) {

                    prepStmt.setString(1, applicationId);
                    prepStmt.setString(2, apiId);
                    prepStmt.setString(3, policyId);
                    prepStmt.execute();
                }

                List<ScopeAuthorizationInfo> systemApiScopes = null;

                if (CollectionUtils.isNotEmpty(scopes)) {
                    boolean isSystemAPI = APIResourceManagementUtil.isSystemAPIByAPIId(apiId);

                    if (isSystemAPI) {
                        // Shared scope authorization behaviour for system APIs.
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Using shared scope authorization for system API: %s", apiId));
                        }
                        systemApiScopes = resolveSystemApiScopesByName(dbConnection, scopes);
                        authorizeApisWithSharedScopes(dbConnection, applicationId, apiId, policyId, systemApiScopes);
                        authorizeScopes(dbConnection, applicationId, systemApiScopes);
                    } else {
                        // Direct scope authorization for non-system APIs.
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Using direct scope authorization for non-system API: %s", apiId));
                        }
                        addScopesDirectly(dbConnection, applicationId, apiId, scopes, tenantIdToSearchScopes);
                    }
                }

                if (CollectionUtils.isNotEmpty(authorizationDetailsTypes)) {
                    final List<String> authorizationDetailsTypesToAdd =
                            authorizationDetailsTypes.stream().map(AuthorizationDetailsType::getType)
                                    .collect(Collectors.toList());
                    this.addAuthorizedAuthorizationDetailsTypes(dbConnection, applicationId, apiId,
                            authorizationDetailsTypesToAdd, tenantId);
                }

                IdentityDatabaseUtil.commitTransaction(dbConnection);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(
                            "Successfully authorized API %s for application %s with %d scope authorizations", apiId,
                            applicationId, systemApiScopes != null ? systemApiScopes.size() : 0));
                }
            } catch (SQLException | APIResourceMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                if (e instanceof SQLException && isScopeConflict((SQLException) e)) {
                    throw new IdentityApplicationManagementClientException(
                            "API resource or scopes are already authorized", e);
                }
                throw new IdentityApplicationManagementException("Error while adding authorized API. Caused by, ", e);
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while obtaining database connection. Caused by, ",
                    e);
        }
    }

    /**
     * Add scopes directly using the original INSERT-SELECT approach.
     * This method filters scopes by API_ID to prevent cross-API scope associations.
     * Used for non-system APIs.
     *
     * @param dbConnection  Database connection
     * @param applicationId Application ID
     * @param apiId         API ID
     * @param scopes        List of scopes to add
     * @param tenantId      Tenant ID
     * @throws SQLException If database operation fails
     */
    private void addScopesDirectly(Connection dbConnection, String applicationId, String apiId, List<Scope> scopes,
                                   int tenantId) throws SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE)) {

            prepStmt.setString(1, applicationId);
            prepStmt.setString(2, apiId);
            prepStmt.setObject(4, tenantId);

            for (Scope scope : scopes) {
                // Only set the variable parameter (scope name)
                prepStmt.setString(3, scope.getName());
                prepStmt.addBatch();

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                            String.format("Adding scope directly: APP=%s, API=%s, SCOPE_NAME=%s", applicationId, apiId,
                                    scope.getName()));
                }
            }

            prepStmt.executeBatch();
        }
    }

    /**
     * Retrieves scopes defined for system APIs that match the given scope names.
     * Uses a single query with IN clause for optimal performance.
     *
     * @param dbConnection Database connection
     * @param scopes List of scopes to look up
     * @return List of scope authorization info for matching system API scopes
     * @throws SQLException If database operation fails
     */
    private List<ScopeAuthorizationInfo> resolveSystemApiScopesByName(Connection dbConnection, List<Scope> scopes)
            throws SQLException {

        if (CollectionUtils.isEmpty(scopes)) {
            return Collections.emptyList();
        }

        List<ScopeAuthorizationInfo> systemApiScopes = new ArrayList<>();
        List<String> scopeNamePlaceholders = new ArrayList<>();
        for (int i = 1; i <= scopes.size(); i++) {
            scopeNamePlaceholders.add(":" + SCOPE_NAME_PREFIX + i + ";");
        }

        String sqlStatement = ApplicationMgtDBQueries.GET_SYSTEM_SCOPE_IDS_BY_NAME.replace(PLACEHOLDER_SCOPE_NAMES,
                String.join(", ", scopeNamePlaceholders));

        try (NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(dbConnection, sqlStatement)) {

            for (int index = 1; index <= scopes.size(); index++) {
                namedPreparedStatement.setString(SCOPE_NAME_PREFIX + index, scopes.get(index - 1).getName());
            }

            try (ResultSet rs = namedPreparedStatement.executeQuery()) {
                while (rs.next()) {
                    String scopeId = rs.getString(COLUMN_NAME_ID);
                    String apiId = rs.getString(COLUMN_NAME_API_ID);
                    String scopeName = rs.getString(COLUMN_NAME_NAME);

                    systemApiScopes.add(new ScopeAuthorizationInfo(scopeId, apiId, scopeName));

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Found system API scope: %s (ID: %s) belonging to API: %s", scopeName,
                                scopeId, apiId));
                    }
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    String.format("Resolved %d system API scopes from %d requested scope names", systemApiScopes.size(),
                            scopes.size()));
        }

        return systemApiScopes;
    }

    /**
     * Authorizes all APIs that share the same scope names.
     * Ensures all required APIs are authorized before scope authorization.
     * This method is only applicable to system APIs as of now, as only they support shared scopes.
     */
    private void authorizeApisWithSharedScopes(Connection dbConnection, String applicationId, String primaryApiId, String policyId, List<ScopeAuthorizationInfo> sharedScopes) throws SQLException {

        Set<String> apiIdsToAuthorize = sharedScopes.stream()
                .map(ScopeAuthorizationInfo::getApiId)
                .filter(apiId -> !apiId.equals(primaryApiId))
                .collect(Collectors.toSet());

        if (apiIdsToAuthorize.isEmpty()) {
            return;
        }

        Set<String> alreadyAuthorizedApis =
                getAlreadyAuthorizedAPIs(dbConnection, applicationId, apiIdsToAuthorize, policyId);

        Set<String> apisToAdd = apiIdsToAuthorize.stream().filter(apiId -> !alreadyAuthorizedApis.contains(apiId))
                .collect(Collectors.toSet());

        if (apisToAdd.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("All %d shared scope APIs are already authorized for application %s",
                        apiIdsToAuthorize.size(), applicationId));
            }
            return;
        }

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_API)) {

            prepStmt.setString(1, applicationId);
            prepStmt.setString(3, policyId);

            for (String apiId : apisToAdd) {
                prepStmt.setString(2, apiId);
                prepStmt.addBatch();

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Auto-authorizing API %s for application %s due to shared scope", apiId,
                            applicationId));
                }
            }

            int[] results = prepStmt.executeBatch();

            // Log results.
            int newlyAuthorized = 0;
            for (int result : results) {
                if (result > 0) {
                    newlyAuthorized++;
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Auto-authorized %d new APIs (skipped %d already authorized) out of %d total shared scope APIs",
                        newlyAuthorized, alreadyAuthorizedApis.size(), apiIdsToAuthorize.size()));
            }
        }
    }

    /**
     * Get already authorized APIs from a set of API IDs in a single query.
     *
     * @param dbConnection  Database connection
     * @param applicationId Application ID
     * @param apiIds        Set of API IDs to check
     * @param policyId      Policy ID
     * @return Set of API IDs that are already authorized
     * @throws SQLException If database operation fails
     */
    private Set<String> getAlreadyAuthorizedAPIs(Connection dbConnection, String applicationId, Set<String> apiIds,
                                                 String policyId) throws SQLException {


        if (apiIds.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> authorizedApis = new HashSet<>();

        List<String> apiIdPlaceholders = new ArrayList<>();
        for (int i = 1; i <= apiIds.size(); i++) {
            apiIdPlaceholders.add(":" + API_ID_PREFIX + i + ";");
        }

        String sqlStatement = ApplicationMgtDBQueries.GET_ALREADY_AUTHORIZED_APIS.replace(PLACEHOLDER_API_IDS,
                String.join(", ", apiIdPlaceholders));

        List<String> apiIdList = new ArrayList<>(apiIds);

        try (NamedPreparedStatement namedPreparedStatement =
                     new NamedPreparedStatement(dbConnection, sqlStatement)) {

            namedPreparedStatement.setString(COLUMN_NAME_APP_ID, applicationId);
            for (int index = 1; index <= apiIdList.size(); index++) {
                namedPreparedStatement.setString(API_ID_PREFIX + index, apiIdList.get(index - 1));
            }

            try (ResultSet rs = namedPreparedStatement.executeQuery()) {
                while (rs.next()) {
                    authorizedApis.add(rs.getString(COLUMN_NAME_API_ID));
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %d already authorized APIs out of %d to check", authorizedApis.size(), apiIds.size()));
        }

        return authorizedApis;
    }

    /**
     * Authorizes the given scopes for the application.
     * Pre-filters already authorized scopes to avoid duplicate key violations.
     */
    private void authorizeScopes(Connection dbConnection, String applicationId, List<ScopeAuthorizationInfo> scopeAuths)
            throws SQLException {

        if (CollectionUtils.isEmpty(scopeAuths)) {
            return;
        }

        Set<String> alreadyAuthorizedScopes = getAlreadyAuthorizedScopes(dbConnection, applicationId, scopeAuths);
        List<ScopeAuthorizationInfo> scopesToAdd = scopeAuths.stream()
                .filter(scopeAuth -> !alreadyAuthorizedScopes.contains(scopeAuth.getScopeId()))
                .collect(Collectors.toList());

        if (scopesToAdd.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("All %d scopes are already authorized for application %s", scopeAuths.size(),
                        applicationId));
            }
            return;
        }

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE_BY_ID)) {
            prepStmt.setString(1, applicationId);
            for (ScopeAuthorizationInfo scopeAuth : scopesToAdd) {
                prepStmt.setString(2, scopeAuth.getApiId());
                prepStmt.setString(3, scopeAuth.getScopeId());
                prepStmt.addBatch();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Adding scope authorization: APP=%s, API=%s, SCOPE=%s (%s)", applicationId,
                            scopeAuth.getApiId(), scopeAuth.getScopeId(), scopeAuth.getScopeName()));
                }
            }
            prepStmt.executeBatch();
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Added %d new scope authorizations (skipped %d already authorized)",
                        scopesToAdd.size(), alreadyAuthorizedScopes.size()));
            }
        }
    }

    /**
     * Get already authorized scopes from a set of scope authorization info.
     */
    private Set<String> getAlreadyAuthorizedScopes(Connection dbConnection, String applicationId,
                                                   List<ScopeAuthorizationInfo> scopeAuths)
            throws SQLException {

        if (CollectionUtils.isEmpty(scopeAuths)) {
            return Collections.emptySet();
        }

        Set<String> authorizedScopes = new HashSet<>();
        Set<String> scopeIds = scopeAuths.stream().map(ScopeAuthorizationInfo::getScopeId).collect(Collectors.toSet());
        List<String> scopeIdPlaceholders = new ArrayList<>();

        for (int i = 1; i <= scopeIds.size(); i++) {
            scopeIdPlaceholders.add(":" + SCOPE_ID_PREFIX + i + ";");
        }

        String sqlStatement = ApplicationMgtDBQueries.GET_ALREADY_AUTHORIZED_SCOPES.replace(PLACEHOLDER_SCOPE_IDS,
                String.join(", ", scopeIdPlaceholders));
        List<String> scopeIdList = new ArrayList<>(scopeIds);

        try (NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(dbConnection, sqlStatement)) {

            namedPreparedStatement.setString(COLUMN_NAME_APP_ID, applicationId);

            for (int index = 1; index <= scopeIdList.size(); index++) {
                namedPreparedStatement.setString(SCOPE_ID_PREFIX + index, scopeIdList.get(index - 1));
            }

            try (ResultSet rs = namedPreparedStatement.executeQuery()) {
                while (rs.next()) {
                    authorizedScopes.add(rs.getString(COLUMN_NAME_SCOPE_ID));
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %d already authorized scopes out of %d to check", authorizedScopes.size(),
                    scopeIds.size()));
        }

        return authorizedScopes;
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
            throws SQLException, APIResourceMgtException {

        if (CollectionUtils.isEmpty(scopesToAdd)) {
            return;
        }

        boolean isSystemAPI = APIResourceManagementUtil.isSystemAPIByAPIId(apiId);

        if (isSystemAPI) {
            List<Scope> scopes = scopesToAdd.stream().map(name -> new Scope.ScopeBuilder().name(name).build())
                    .collect(Collectors.toList());
            String policyId = getPolicyIdForAuthorizedAPI(dbConnection, appId, apiId);
            List<ScopeAuthorizationInfo> systemApiScopes = resolveSystemApiScopesByName(dbConnection, scopes);
            authorizeApisWithSharedScopes(dbConnection, appId, apiId, policyId, systemApiScopes);
            authorizeScopes(dbConnection, appId, systemApiScopes);
        } else {
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

    /**
     * Helper to get policy ID for an authorized API
     */
    private String getPolicyIdForAuthorizedAPI(Connection dbConnection, String appId, String apiId)
            throws SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(GET_AUTHORIZED_API_POLICY_ID)) {
            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);

            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    String policyId = rs.getString("POLICY_ID");
                    return policyId != null ? policyId : "RBAC";
                }
            }
        }

        return "RBAC";  // Default to RBAC if not found.
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
     *                                                is an organization.
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
