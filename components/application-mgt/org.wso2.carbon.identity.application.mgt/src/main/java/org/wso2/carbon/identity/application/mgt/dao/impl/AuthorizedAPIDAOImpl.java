/*
 * Copyright (c) 2023-2026, WSO2 LLC. (http://www.wso2.com).
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
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_SCOPE_IDS_FOR_DELETION;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_SCOPE_IDS_TO_EXCLUDE;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_SCOPE_NAMES;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_SCOPE_NAMES_FOR_DELETION;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.PLACEHOLDER_SCOPE_NAMES_FOR_SCOPE_DELETION;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.SCOPE_ID_PREFIX;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.SCOPE_ID_PREFIX_DEL;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.SCOPE_NAME_PREFIX;
import static org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationMgtDBQueries.SQLPlaceholders.SCOPE_NAME_PREFIX_DEL;

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

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            this.addAuthorizedAPIWithScopes(dbConnection, applicationId, apiId, policyId, scopes,
                    Collections.emptyList(), tenantId);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while obtaining database connection.", e);
        }
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

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            deleteAuthorizedAPIWithScopes(dbConnection, appId, apiId, tenantId);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while obtaining database connection.", e);
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

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            this.addAuthorizedAPIWithScopes(dbConnection, applicationId, authorizedAPI.getAPIId(),
                    authorizedAPI.getPolicyId(), authorizedAPI.getScopes(),
                    authorizedAPI.getAuthorizationDetailsTypes(), tenantId);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while obtaining database connection.", e);
        }
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

    /**
     * Adds an authorized API along with its scopes and authorization details types in a single transaction.
     * This method ensures atomicity of the entire operation, so that either all changes are committed or none at all,
     * maintaining data integrity.
     */
    private void addAuthorizedAPIWithScopes(Connection dbConnection, String applicationId, String apiId,
                                            String policyId, List<Scope> scopes,
                                            List<AuthorizationDetailsType> authorizationDetailsTypes, int tenantId)
            throws IdentityApplicationManagementException {

        int scopeTenantId = isOrganization(tenantId) ? getPrimaryOrgTenantID(tenantId) : tenantId;

        try {
            authorizeAPI(dbConnection, applicationId, apiId, policyId);

            if (CollectionUtils.isNotEmpty(scopes)) {
                // System APIs reuse scopes across them and require auto-authorization of related APIs.
                // Non-system APIs have unique scopes per API and use standard authorization path.
                if (APIResourceManagementUtil.isSystemAPIByAPIId(apiId)) {
                    authorizeScopesForSystemApis(dbConnection, applicationId, apiId, policyId, scopes, scopeTenantId);
                } else {
                    authorizeScopesByNames(dbConnection, applicationId, apiId, scopes, scopeTenantId);
                }
            }

            if (CollectionUtils.isNotEmpty(authorizationDetailsTypes)) {
                final List<String> authorizationDetailsTypesToAdd = authorizationDetailsTypes.stream()
                        .map(AuthorizationDetailsType::getType).collect(Collectors.toList());
                this.addAuthorizedAuthorizationDetailsTypes(dbConnection, applicationId, apiId,
                        authorizationDetailsTypesToAdd, tenantId);
            }

            IdentityDatabaseUtil.commitTransaction(dbConnection);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Successfully authorized API %s for application %s with %d scope authorizations", apiId,
                        applicationId, scopes == null ? 0 : scopes.size()));
            }
        } catch (SQLException | APIResourceMgtException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            if (e instanceof SQLException && isScopeConflict((SQLException) e)) {
                throw new IdentityApplicationManagementClientException(
                        "Duplicate authorized scopes found while adding authorized API.", e);
            }
            throw new IdentityApplicationManagementException("Error while adding authorized API.", e);
        }
    }

    /**
     * Insert API authorization record into the database.
     * This is a low-level helper method that performs the actual INSERT operation.
     * Note: Callers must ensure the API is not already authorized before calling this method.
     * Duplicate checking is handled at higher levels via getAlreadyAuthorizedAPIs.
     */
    private void authorizeAPI(Connection dbConnection, String applicationId, String apiId, String policyId)
            throws SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_API)) {
            prepStmt.setString(1, applicationId);
            prepStmt.setString(2, apiId);
            prepStmt.setString(3, policyId);
            prepStmt.execute();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Inserted API authorization: app=%s, api=%s, policy=%s",
                    applicationId, apiId, policyId));
        }
    }

    /**
     * Authorizes scopes for system APIs by resolving scope IDs and ensuring all APIs sharing the same scopes are
     * authorized. This method handles the complexity of shared scopes for system APIs, ensuring data integrity and
     * optimal performance.
     */
    private void authorizeScopesForSystemApis(Connection dbConnection, String applicationId, String apiId,
                                              String policyId, List<Scope> scopes, int tenantId)
            throws SQLException, IdentityApplicationManagementException {

        List<ScopeAuthorizationInfo> systemApiScopes = resolveSystemApiScopesByName(dbConnection, scopes);
        boolean hasSharedScopes = systemApiScopes.stream().anyMatch(scopeInfo -> !scopeInfo.getApiId().equals(apiId));

        if (hasSharedScopes) {
            // Scopes are reused across system API versions. Auto-authorize related APIs.
            authorizeApisWithReusedScopes(dbConnection, applicationId, apiId, policyId, systemApiScopes);

            List<ScopeAuthorizationInfo> missingScopeAuths =
                    getMissingScopeAuthorizations(dbConnection, applicationId, systemApiScopes);
            if (missingScopeAuths.isEmpty()) {
                // All reused scope using APIs already have these scopes - nothing to authorize.
                throw new IdentityApplicationManagementClientException(
                        "Duplicate authorized scopes found while adding authorized API.");
            }

            // Inconsistent state detected (e.g., migration). Authorize only missing scopes, grouped by API to
            // correctly handle scopes across different system APIs.
            authorizeScopesByIds(dbConnection, applicationId, missingScopeAuths);
        } else {
            // Scopes are unique to this API. Use standard path to propagate conflicts to the client.
            authorizeScopesByNames(dbConnection, applicationId, apiId, scopes, tenantId);
        }
    }

    /**
     * Checks if any of the given reused scopes are missing authorization for the application.
     * Used to detect inconsistent state across shared system APIs.
     *
     * @return A list of {@link ScopeAuthorizationInfo} entries that are not yet authorized for the application.
     */
    private List<ScopeAuthorizationInfo> getMissingScopeAuthorizations(Connection dbConnection, String applicationId,
                                                                       List<ScopeAuthorizationInfo> scopeAuths)
            throws SQLException {

        Set<String> alreadyAuthorizedScopes = getAlreadyAuthorizedScopes(dbConnection, applicationId, scopeAuths);
        return scopeAuths.stream().filter(scopeAuth -> !alreadyAuthorizedScopes.contains(scopeAuth.getScopeId()))
                .collect(Collectors.toList());
    }

    /**
     * Authorizes the given scopes for the application and API directly by scope names.
     * This method is used for non-system APIs that do not support shared scopes.
     * It performs individual inserts for each scope without pre-fetching scope IDs, relying on database constraints
     * to prevent duplicates.
     */
    private void authorizeScopesByNames(Connection dbConnection, String applicationId, String apiId,
                                         List<Scope> scopes, int tenantId) throws SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE)) {

            prepStmt.setString(1, applicationId);
            prepStmt.setString(2, apiId);
            prepStmt.setObject(4, tenantId);

            for (Scope scope : scopes) {
                prepStmt.setString(3, scope.getName());
                prepStmt.addBatch();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Adding scope: APP=%s, API=%s, SCOPE_NAME=%s",
                            applicationId, apiId, scope.getName()));
                }
            }

            prepStmt.executeBatch();
        }
    }

    /**
     * Authorizes scopes using their resolved scope IDs.
     * Used for system APIs to avoid duplicate scope name resolution across shared API versions.
     */
    private void authorizeScopesByIds(Connection dbConnection, String applicationId,
                                      List<ScopeAuthorizationInfo> scopeAuths) throws SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE_BY_ID)) {

            prepStmt.setString(1, applicationId);

            for (ScopeAuthorizationInfo scopeAuth : scopeAuths) {
                prepStmt.setString(2, scopeAuth.getApiId());
                prepStmt.setString(3, scopeAuth.getScopeId());
                prepStmt.addBatch();

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Adding scope authorization: APP=%s, API=%s, SCOPE=%s (%s)",
                            applicationId, scopeAuth.getApiId(), scopeAuth.getScopeId(),
                            scopeAuth.getScopeName()));
                }
            }

            prepStmt.executeBatch();
        }
    }

    /**
     * Retrieves scopes defined for system APIs that match the given scope names.
     * Uses a single query with IN clause for optimal performance.
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
    private void authorizeApisWithReusedScopes(Connection dbConnection, String applicationId, String primaryApiId,
                                               String policyId, List<ScopeAuthorizationInfo> sharedScopes)
            throws SQLException {

        Set<String> apiIdsToAuthorize = sharedScopes.stream()
                .map(ScopeAuthorizationInfo::getApiId)
                .filter(apiId -> !apiId.equals(primaryApiId))
                .collect(Collectors.toSet());

        if (apiIdsToAuthorize.isEmpty()) {
            return;
        }

        Set<String> alreadyAuthorizedApis = getAlreadyAuthorizedAPIs(dbConnection, applicationId, apiIdsToAuthorize);

        Set<String> apisToAdd = apiIdsToAuthorize.stream()
                .filter(apiId -> !alreadyAuthorizedApis.contains(apiId))
                .collect(Collectors.toSet());

        if (apisToAdd.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("All %d shared scope APIs are already authorized for application %s",
                        apiIdsToAuthorize.size(), applicationId));
            }
            return;
        }

        for (String apiId : apisToAdd) {
            authorizeAPI(dbConnection, applicationId, apiId, policyId);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Auto-authorized API %s for application %s due to shared scope", apiId,
                        applicationId));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(
                    "Auto-authorized %d new APIs (skipped %d already authorized) out of %d total shared scope APIs",
                    apisToAdd.size(), alreadyAuthorizedApis.size(), apiIdsToAuthorize.size()));
        }
    }

    /**
     * Get already authorized APIs from a set of API IDs in a single query.
     *
     * @param dbConnection  Database connection
     * @param applicationId Application ID
     * @param apiIds        Set of API IDs to check
     * @return Set of API IDs that are already authorized
     * @throws SQLException If database operation fails
     */
    private Set<String> getAlreadyAuthorizedAPIs(Connection dbConnection, String applicationId, Set<String> apiIds)
            throws SQLException {

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

        try (NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(dbConnection, sqlStatement)) {

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
            LOG.debug(String.format("Found %d already authorized APIs out of %d to check", authorizedApis.size(),
                    apiIds.size()));
        }

        return authorizedApis;
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
            throws SQLException, APIResourceMgtException, IdentityApplicationManagementException {

        if (CollectionUtils.isEmpty(scopesToAdd)) {
            return;
        }

        boolean isSystemAPI = APIResourceManagementUtil.isSystemAPIByAPIId(apiId);
        List<Scope> scopes = scopesToAdd.stream().map(name -> new Scope.ScopeBuilder().name(name).build())
                .collect(Collectors.toList());

        if (isSystemAPI) {
            String policyId = getPolicyIdForAuthorizedAPI(dbConnection, appId, apiId);
            authorizeScopesForSystemApis(dbConnection, appId, apiId, policyId, scopes, tenantId);
        } else {
            authorizeScopesByNames(dbConnection, appId, apiId, scopes, tenantId);
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

    /**
     * Deletes an authorized API and handles shared scope cleanup for system APIs.
     * For system APIs with shared scopes:
     * 1. Identifies scopes being deleted from the target API
     * 2. Finds and deletes matching shared scopes from other system APIs
     * 3. Deletes the target API
     * 4. Finally removes other APIs if they have no remaining scopes
     * For non-system APIs, it simply deletes the API which CASCADE deletes its scopes.
     */
    private void deleteAuthorizedAPIWithScopes(Connection dbConnection, String appId, String apiId, int tenantId)
            throws IdentityApplicationManagementException {

        try {
            boolean isSystemAPI = APIResourceManagementUtil.isSystemAPIByAPIId(apiId);
            if (isSystemAPI) {
                handleSystemAPIDeletion(dbConnection, appId, apiId);
            } else {
                deleteAPIAuthorization(dbConnection, appId, apiId);
            }
            IdentityDatabaseUtil.commitTransaction(dbConnection);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Successfully deleted authorized API %s for application %s", apiId, appId));
            }
        } catch (SQLException | APIResourceMgtException e) {
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw new IdentityApplicationManagementException("Error while deleting the authorized API.", e);
        }
    }

    /**
     * Delete an API authorization record.
     * This will CASCADE delete all associated scope authorizations.
     */
    private void deleteAPIAuthorization(Connection dbConnection, String appId, String apiId) throws SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ApplicationMgtDBQueries.DELETE_AUTHORIZED_API_BY_API_ID)) {

            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);

            int deletedCount = prepStmt.executeUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        String.format("Deleted API authorization for API %s (affected rows: %d)", apiId, deletedCount));
            }
        }
    }

    /**
     * Handles deletion of a system API with unique and shared scope cleanup.
     */
    private void handleSystemAPIDeletion(Connection dbConnection, String appId, String apiId)
            throws SQLException {

        // Step 1: Get scopes that will be deleted from this API.
        List<ScopeAuthorizationInfo> scopesToDelete = getScopesForAPI(dbConnection, appId, apiId);

        if (CollectionUtils.isEmpty(scopesToDelete)) {
            deleteAPIAuthorization(dbConnection, appId, apiId);
            return;
        }

        // Step 2: Find shared system API scopes with matching names.
        List<ScopeAuthorizationInfo> sharedScopesToDelete =
                findSharedSystemAPIScopesByNames(dbConnection, scopesToDelete);

        // Step 3: Delete shared scope authorizations from other APIs.
        if (!sharedScopesToDelete.isEmpty()) {
            deleteSharedScopeAuthorizations(dbConnection, appId, sharedScopesToDelete);
        }

        // Step 4: Delete the target API (CASCADE deletes its own scopes).
        deleteAPIAuthorization(dbConnection, appId, apiId);
    }

    /**
     * Delete shared scope authorizations from other APIs.
     */
    private void deleteSharedScopeAuthorizations(Connection dbConnection, String appId,
                                                 List<ScopeAuthorizationInfo> scopesToDelete)
            throws SQLException {

        List<String> scopeIds =
                scopesToDelete.stream().map(ScopeAuthorizationInfo::getScopeId).collect(Collectors.toList());

        List<String> scopeIdPlaceholders = new ArrayList<>();
        for (int i = 1; i <= scopeIds.size(); i++) {
            scopeIdPlaceholders.add(":" + SCOPE_ID_PREFIX_DEL + i + ";");
        }

        String sqlStatement = ApplicationMgtDBQueries.DELETE_AUTHORIZED_SCOPES_BY_SCOPE_IDS
                .replace(PLACEHOLDER_SCOPE_IDS_FOR_DELETION, String.join(", ", scopeIdPlaceholders));

        try (NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(dbConnection, sqlStatement)) {

            namedPreparedStatement.setString("APP_ID", appId);
            for (int i = 1; i <= scopeIds.size(); i++) {
                namedPreparedStatement.setString(SCOPE_ID_PREFIX_DEL + i, scopeIds.get(i - 1));
            }

            int deletedCount = namedPreparedStatement.executeUpdate();

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Deleted %d shared scope authorizations from other APIs", deletedCount));
            }
        }
    }

    /**
     * Get scopes associated with an API authorization.
     */
    private List<ScopeAuthorizationInfo> getScopesForAPI(Connection dbConnection, String appId, String apiId)
            throws SQLException {

        List<ScopeAuthorizationInfo> scopes = new ArrayList<>();

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ApplicationMgtDBQueries.GET_AUTHORIZED_SCOPES_FOR_API)) {

            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);

            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    scopes.add(new ScopeAuthorizationInfo(
                            rs.getString("ID"),
                            apiId,
                            rs.getString("NAME")
                    ));
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %d scopes for API %s to be deleted", scopes.size(), apiId));
        }

        return scopes;
    }

    /**
     * Find shared system API scopes that match the given scope names.
     */
    private List<ScopeAuthorizationInfo> findSharedSystemAPIScopesByNames(Connection dbConnection,
                                                                     List<ScopeAuthorizationInfo> scopesToDelete)
            throws SQLException {

        List<ScopeAuthorizationInfo> sharedScopes = new ArrayList<>();
        List<String> scopeNames = scopesToDelete.stream()
                .map(ScopeAuthorizationInfo::getScopeName)
                .collect(Collectors.toList());
        List<String> originalScopeIds = scopesToDelete.stream()
                .map(ScopeAuthorizationInfo::getScopeId)
                .collect(Collectors.toList());

        List<String> scopeNamePlaceholders = new ArrayList<>();
        for (int i = 1; i <= scopeNames.size(); i++) {
            scopeNamePlaceholders.add(":" + SCOPE_NAME_PREFIX_DEL + i + ";");
        }
        List<String> scopeIdPlaceholders = new ArrayList<>();
        for (int i = 1; i <= originalScopeIds.size(); i++) {
            scopeIdPlaceholders.add(":" + SCOPE_ID_PREFIX_DEL + i + ";");
        }

        String sqlStatement = ApplicationMgtDBQueries.GET_SHARED_SYSTEM_API_SCOPE_IDS_BY_NAMES
                .replace(PLACEHOLDER_SCOPE_NAMES_FOR_DELETION, String.join(", ", scopeNamePlaceholders))
                .replace(PLACEHOLDER_SCOPE_IDS_TO_EXCLUDE, String.join(", ", scopeIdPlaceholders));

        try (NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(dbConnection, sqlStatement)) {

            for (int i = 1; i <= scopeNames.size(); i++) {
                namedPreparedStatement.setString(SCOPE_NAME_PREFIX_DEL + i, scopeNames.get(i - 1));
            }
            for (int i = 1; i <= originalScopeIds.size(); i++) {
                namedPreparedStatement.setString(SCOPE_ID_PREFIX_DEL + i, originalScopeIds.get(i - 1));
            }

            try (ResultSet rs = namedPreparedStatement.executeQuery()) {
                while (rs.next()) {
                    sharedScopes.add(new ScopeAuthorizationInfo(
                            rs.getString("ID"),
                            rs.getString("API_ID"),
                            rs.getString("NAME")
                    ));
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %d shared scopes to delete across other system APIs", sharedScopes.size()));
        }

        return sharedScopes;
    }

    private void deleteScopes(Connection dbConnection, String appId, String apiId,
                              List<String> scopesToRemove, int tenantId) throws SQLException, APIResourceMgtException {

        if (CollectionUtils.isEmpty(scopesToRemove)) {
            return;
        }
        boolean isSystemAPI = APIResourceManagementUtil.isSystemAPIByAPIId(apiId);
        if (isSystemAPI) {
            handleSystemAPIScopeAuthorizationDeletion(dbConnection, appId, apiId, scopesToRemove);
        } else {
            deleteScopesAuthorization(dbConnection, appId, apiId, scopesToRemove, tenantId);
        }
    }

    /**
     * Deletes scopes from an API.
     * Uses scope names to identify and delete scopes.
     */
    private void deleteScopesAuthorization(Connection dbConnection, String appId, String apiId, List<String> scopeNames,
                                           int tenantId) throws SQLException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ApplicationMgtDBQueries.DELETE_AUTHORIZED_SCOPE)) {

            prepStmt.setString(1, appId);
            prepStmt.setString(2, apiId);
            prepStmt.setInt(4, tenantId);

            for (String scopeName : scopeNames) {
                prepStmt.setString(3, scopeName);
                prepStmt.addBatch();
            }

            int[] results = prepStmt.executeBatch();

            if (LOG.isDebugEnabled()) {
                int deletedCount = 0;
                for (int result : results) {
                    if (result > 0) {
                        deletedCount++;
                    }
                }
                LOG.debug(String.format("Deleted %d scopes from non-system API %s", deletedCount, apiId));
            }
        }
    }

    /**
     * Handles deletion of scopes from a system API with shared scope cleanup.
     * For system APIs with shared scopes:
     * 1. Identifies scopes being deleted from the target API
     * 2. Finds and deletes matching shared scopes from other system APIs
     * 3. Checks if the target API has any remaining scopes, deletes it if orphaned
     */
    private void handleSystemAPIScopeAuthorizationDeletion(Connection dbConnection, String appId, String apiId,
                                              List<String> scopeNames) throws SQLException {

        List<ScopeAuthorizationInfo> scopesToDelete = getScopesByNamesForAPI(dbConnection, apiId, scopeNames);

        if (scopesToDelete.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("No scopes found to delete for API %s with names: %s", apiId,
                        String.join(", ", scopeNames)));
            }
            return;
        }

        List<String> scopeIds =
                scopesToDelete.stream().map(ScopeAuthorizationInfo::getScopeId).collect(Collectors.toList());
        deleteScopeAuthorizationsByIds(dbConnection, appId, apiId, scopeIds);

        List<ScopeAuthorizationInfo> sharedScopesToDelete =
                findSharedSystemAPIScopesByNames(dbConnection, scopesToDelete);

        if (!sharedScopesToDelete.isEmpty()) {
            deleteSharedScopeAuthorizationsByScope(dbConnection, appId, sharedScopesToDelete);
        }
    }

    /**
     * Get scope IDs and names for specific scope names within an API.
     */
    private List<ScopeAuthorizationInfo> getScopesByNamesForAPI(Connection dbConnection, String apiId,
                                                           List<String> scopeNames) throws SQLException {

        List<ScopeAuthorizationInfo> scopes = new ArrayList<>();
        List<String> scopeNamePlaceholders = new ArrayList<>();
        for (int i = 1; i <= scopeNames.size(); i++) {
            scopeNamePlaceholders.add(":" + SCOPE_NAME_PREFIX_DEL + i + ";");
        }

        String sqlStatement = ApplicationMgtDBQueries.GET_SCOPE_IDS_BY_NAMES_FOR_API.replace(
                PLACEHOLDER_SCOPE_NAMES_FOR_SCOPE_DELETION, String.join(", ", scopeNamePlaceholders));

        try (NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(dbConnection, sqlStatement)) {

            namedPreparedStatement.setString(COLUMN_NAME_API_ID, apiId);
            for (int i = 1; i <= scopeNames.size(); i++) {
                namedPreparedStatement.setString(SCOPE_NAME_PREFIX_DEL + i, scopeNames.get(i - 1));
            }

            try (ResultSet rs = namedPreparedStatement.executeQuery()) {
                while (rs.next()) {
                    scopes.add(new ScopeAuthorizationInfo(
                            rs.getString("ID"),
                            apiId,
                            rs.getString("NAME")
                    ));
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %d scopes to delete from API %s", scopes.size(), apiId));
        }

        return scopes;
    }

    /**
     * Delete specific scope authorizations by scope IDs from an API.
     */
    private void deleteScopeAuthorizationsByIds(Connection dbConnection, String appId, String apiId,
                                                List<String> scopeIds) throws SQLException {

        List<String> scopeIdPlaceholders = new ArrayList<>();
        for (int i = 0; i < scopeIds.size(); i++) {
            scopeIdPlaceholders.add("?");
        }

        String sqlStatement =
                ApplicationMgtDBQueries.DELETE_AUTHORIZED_SCOPES_BY_IDS.replace(PLACEHOLDER_SCOPE_IDS_FOR_DELETION,
                        String.join(", ", scopeIdPlaceholders));

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStatement)) {
            int paramIndex = 1;
            prepStmt.setString(paramIndex++, appId);
            prepStmt.setString(paramIndex++, apiId);

            for (String scopeId : scopeIds) {
                prepStmt.setString(paramIndex++, scopeId);
            }

            int deletedCount = prepStmt.executeUpdate();

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Deleted %d scope authorizations from API %s", deletedCount, apiId));
            }
        }
    }

    /**
     * Delete shared scope authorizations from other APIs.
     * Groups deletions by API for efficiency.
     */
    private void deleteSharedScopeAuthorizationsByScope(Connection dbConnection, String appId,
                                                        List<ScopeAuthorizationInfo> sharedScopes) throws SQLException {

        Map<String, List<String>> scopesByApi = new HashMap<>();
        for (ScopeAuthorizationInfo scope : sharedScopes) {
            scopesByApi.computeIfAbsent(scope.getApiId(), k -> new ArrayList<>())
                    .add(scope.getScopeId());
        }

        for (Map.Entry<String, List<String>> apiWithScopes : scopesByApi.entrySet()) {
            deleteScopeAuthorizationsByIds(dbConnection, appId, apiWithScopes.getKey(), apiWithScopes.getValue());
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
