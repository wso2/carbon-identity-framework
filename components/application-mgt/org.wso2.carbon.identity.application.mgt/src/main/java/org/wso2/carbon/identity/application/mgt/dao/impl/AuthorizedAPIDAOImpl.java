/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.dao.AuthorizedAPIDAO;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authorized API DAO implementation class.
 */
public class AuthorizedAPIDAOImpl implements AuthorizedAPIDAO {

    @Override
    public void addAuthorizedAPI(String applicationId, String apiId, String policyId,
                                 List<Scope> scopes, int tenantId) throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                PreparedStatement prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_API);
                prepStmt.setString(1, applicationId);
                prepStmt.setString(2, apiId);
                prepStmt.setString(3, policyId);
                prepStmt.execute();

                prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE);
                boolean isSystemAPI = APIResourceManagementUtil.isSystemAPIByAPIId(apiId);
                for (Scope scope : scopes) {
                    prepStmt.setString(1, applicationId);
                    prepStmt.setString(2, apiId);
                    prepStmt.setString(3, scope.getName());
                    prepStmt.setObject(4, isSystemAPI ? null : tenantId);
                    prepStmt.addBatch();
                    prepStmt.clearParameters();
                }
                prepStmt.executeBatch();
                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException | APIResourceMgtException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException | APIResourceMgtException e) {
            throw new IdentityApplicationManagementException("Error while adding authorized API.", e);
        }
    }

    @Override
    public List<AuthorizedAPI> getAuthorizedAPIs(String applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            PreparedStatement prepStmt = dbConnection.prepareStatement(
                    ApplicationMgtDBQueries.GET_AUTHORIZED_APIS);
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
                if (!authorizedAPIMap.containsKey(apiId)) {
                    AuthorizedAPI.AuthorizedAPIBuilder authorizedAPIBuilder = new AuthorizedAPI.AuthorizedAPIBuilder()
                            .appId(applicationId)
                            .apiId(apiId)
                            .policyId(resultSet.getString(
                                    ApplicationConstants.ApplicationTableColumns.POLICY_ID))
                            .scopes(scope == null ? Collections.emptyList() : Collections.singletonList(scope));
                    authorizedAPIMap.put(apiId, authorizedAPIBuilder.build());
                } else {
                    if (scope == null) {
                        continue;
                    }
                    AuthorizedAPI authorizedAPI = authorizedAPIMap.get(apiId);
                    List<Scope> scopes = new ArrayList<>(authorizedAPI.getScopes());
                    scopes.add(scope);
                    authorizedAPI.setScopes(scopes);
                }
            }
            return authorizedAPIMap.values().isEmpty() ? new ArrayList<>() : new ArrayList<>(authorizedAPIMap.values());
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while fetching authorized API.", e);

        }
    }

    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                   List<String> removedScopes, int tenantId)
            throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                if (CollectionUtils.isNotEmpty(addedScopes)) {
                    PreparedStatement prepStmt = dbConnection.prepareStatement(
                            ApplicationMgtDBQueries.ADD_AUTHORIZED_SCOPE);
                    prepStmt.setString(1, appId);
                    prepStmt.setString(2, apiId);
                    prepStmt.setObject(4, tenantId == 0 ? null : tenantId);
                    for (String scope : addedScopes) {
                        prepStmt.setString(3, scope);
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }

                if (CollectionUtils.isNotEmpty(removedScopes)) {
                    PreparedStatement prepStmt = dbConnection.prepareStatement(
                            ApplicationMgtDBQueries.DELETE_AUTHORIZED_SCOPE);
                    prepStmt.setString(1, appId);
                    prepStmt.setString(2, apiId);
                    prepStmt.setInt(4, tenantId);
                    for (String scope : removedScopes) {
                        prepStmt.setString(3, scope);
                        prepStmt.addBatch();
                    }
                    prepStmt.executeBatch();
                }
                IdentityDatabaseUtil.commitTransaction(dbConnection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw e;
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while updating the authorized API.", e);
        }
    }

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

    @Override
    public AuthorizedAPI getAuthorizedAPI(String applicationId, String apiId, int tenantId)
            throws IdentityApplicationManagementException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            PreparedStatement prepStmt = dbConnection.prepareStatement(
                    ApplicationMgtDBQueries.GET_AUTHORIZED_API);
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
                } else {
                    List<Scope> scopes = new ArrayList<>(authorizedAPI.getScopes());
                    Scope scope = new Scope.ScopeBuilder()
                            .name(resultSet.getString(ApplicationConstants.ApplicationTableColumns.SCOPE_NAME)).build();
                    scopes.add(scope);
                    authorizedAPI.setScopes(scopes);
                }
            }
            return authorizedAPI;
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while getting authorized API.", e);
        }
    }
}
