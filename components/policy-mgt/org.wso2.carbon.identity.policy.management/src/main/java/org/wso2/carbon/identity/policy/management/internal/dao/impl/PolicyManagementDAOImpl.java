/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.NamedTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyBasicInfo;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.policy.management.api.util.PolicyManagementExceptionHandler;
import org.wso2.carbon.identity.policy.management.internal.constant.PolicyMgtSQLConstants;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Policy Management DAO Implementation.
 */
public class PolicyManagementDAOImpl implements PolicyManagementDAO {

    private static final Log LOG = LogFactory.getLog(PolicyManagementDAOImpl.class);

    @Override
    public Policy addPolicy(Policy policy, int tenantId) throws PolicyManagementException {

        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeInsert(
                        PolicyMgtSQLConstants.Query.ADD_POLICY,
                        preparedStatement -> {
                            preparedStatement.setString(PolicyMgtSQLConstants.Column.ID, policy.getId());
                            preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_NAME, policy.getName());
                            preparedStatement.setInt(PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                        },
                        policy,
                        false);

                for (PolicyResource policyResource : policy.getResources()) {
                    final String resourceRowId = UUID.randomUUID().toString();
                    template.executeInsert(
                            PolicyMgtSQLConstants.Query.ADD_POLICY_RESOURCE,
                            preparedStatement -> {
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.ID, resourceRowId);
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_ID, policy.getId());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.TARGET,
                                        policyResource.getTarget());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.RESOURCE_TYPE,
                                        policyResource.getResourceType().name());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.RESOURCE_ID,
                                        policyResource.getResourceId());
                            },
                            policyResource,
                            false);
                }

                return null;
            });
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_ADDING_POLICY, e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Policy added with ID: " + policy.getId());
        }
        return policy;
    }

    @Override
    public Policy updatePolicy(Policy policy, int tenantId) throws PolicyManagementException {

        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeUpdate(
                        PolicyMgtSQLConstants.Query.UPDATE_POLICY,
                        preparedStatement -> {
                            preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_NAME, policy.getName());
                            preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_ID, policy.getId());
                            preparedStatement.setInt(PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                        });

                template.executeUpdate(
                        PolicyMgtSQLConstants.Query.DELETE_POLICY_RESOURCES,
                        preparedStatement -> preparedStatement.setString(
                                PolicyMgtSQLConstants.Column.POLICY_ID, policy.getId()));

                for (PolicyResource policyResource : policy.getResources()) {
                    final String resourceRowId = UUID.randomUUID().toString();
                    template.executeInsert(
                            PolicyMgtSQLConstants.Query.ADD_POLICY_RESOURCE,
                            preparedStatement -> {
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.ID, resourceRowId);
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_ID, policy.getId());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.TARGET,
                                        policyResource.getTarget());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.RESOURCE_TYPE,
                                        policyResource.getResourceType().name());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.RESOURCE_ID,
                                        policyResource.getResourceId());
                            },
                            policyResource,
                            false);
                }

                return null;
            });
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_UPDATING_POLICY, e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Policy updated with ID: " + policy.getId());
        }
        return policy;
    }

    @Override
    public void deletePolicy(String policyId, int tenantId) throws PolicyManagementException {

        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.<Void, RuntimeException>withTransaction(template -> {
                template.executeUpdate(
                        PolicyMgtSQLConstants.Query.DELETE_POLICY,
                        preparedStatement -> {
                            preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_ID, policyId);
                            preparedStatement.setInt(PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_DELETING_POLICY, e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Policy deleted with ID: " + policyId);
        }
    }

    @Override
    public Policy getPolicyById(String policyId, int tenantId) throws PolicyManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.<Policy, RuntimeException>withTransaction(template -> {
                Policy base = template.fetchSingleRecord(
                        PolicyMgtSQLConstants.Query.GET_POLICY_BY_ID,
                        (resultSet, rowNumber) -> new Policy(
                                resultSet.getString(PolicyMgtSQLConstants.Column.ID),
                                resultSet.getString(PolicyMgtSQLConstants.Column.POLICY_NAME),
                                tenantDomain, null),
                        preparedStatement -> {
                            preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_ID, policyId);
                            preparedStatement.setInt(PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                        });
                if (base == null) {
                    return null;
                }
                List<PolicyResource> resources = fetchPolicyResources(template, base.getId());
                return new Policy(base.getId(), base.getName(), tenantDomain, resources);
            });
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
        }
    }

    @Override
    public Policy getPolicyByName(String policyName, int tenantId) throws PolicyManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.<Policy, RuntimeException>withTransaction(template -> {
                Policy base = template.fetchSingleRecord(
                        PolicyMgtSQLConstants.Query.GET_POLICY_BY_NAME,
                        (resultSet, rowNumber) -> new Policy(
                                resultSet.getString(PolicyMgtSQLConstants.Column.ID),
                                resultSet.getString(PolicyMgtSQLConstants.Column.POLICY_NAME),
                                tenantDomain, null),
                        preparedStatement -> {
                            preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_NAME, policyName);
                            preparedStatement.setInt(PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                        });
                if (base == null) {
                    return null;
                }
                List<PolicyResource> resources = fetchPolicyResources(template, base.getId());
                return new Policy(base.getId(), base.getName(), tenantDomain, resources);
            });
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
        }
    }

    @Override
    public String getPolicyIdByName(String policyName, int tenantId) throws PolicyManagementException {

        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.<String, RuntimeException>withTransaction(
                    template -> template.fetchSingleRecord(
                            PolicyMgtSQLConstants.Query.CHECK_POLICY_NAME_EXISTS,
                            (resultSet, rowNumber) ->
                                    resultSet.getString(PolicyMgtSQLConstants.Column.ID),
                            preparedStatement -> {
                                preparedStatement.setString(
                                        PolicyMgtSQLConstants.Column.POLICY_NAME, policyName);
                                preparedStatement.setInt(
                                        PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                            }));
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
        }
    }

    @Override
    public List<PolicyBasicInfo> getPolicies(int tenantId, String filter, int offset, int limit)
            throws PolicyManagementException {

        // FETCH NEXT 0 ROWS (MS SQL) is invalid and an empty page is meaningless, so short-circuit.
        if (limit <= 0) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(offset, 0);
        boolean hasFilter = filter != null && !filter.trim().isEmpty();
        String filterValue = hasFilter ? "%" + filter.trim() + "%" : null;

        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            PaginationStyle style = resolvePaginationStyle();
            String query = resolvePaginatedQuery(style, hasFilter);
            List<PolicyBasicInfo> policies = jdbcTemplate.<List<PolicyBasicInfo>, RuntimeException>withTransaction(
                    template -> template.executeQuery(
                            query,
                            (resultSet, rowNumber) -> new PolicyBasicInfo(
                                    resultSet.getString(PolicyMgtSQLConstants.Column.ID),
                                    resultSet.getString(PolicyMgtSQLConstants.Column.POLICY_NAME)),
                            preparedStatement -> {
                                preparedStatement.setInt(PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                                if (hasFilter) {
                                    preparedStatement.setString(PolicyMgtSQLConstants.Column.FILTER, filterValue);
                                }
                                bindPaginationParams(preparedStatement, style, safeOffset, limit);
                            }));
            return policies != null ? policies : Collections.emptyList();
        } catch (TransactionException | DataAccessException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
        }
    }

    @Override
    public int getPolicyCount(int tenantId, String filter) throws PolicyManagementException {

        boolean hasFilter = filter != null && !filter.trim().isEmpty();
        String filterValue = hasFilter ? "%" + filter.trim() + "%" : null;
        String query = hasFilter ? PolicyMgtSQLConstants.Query.GET_POLICIES_COUNT_FILTER
                : PolicyMgtSQLConstants.Query.GET_POLICIES_COUNT;

        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            Integer count = jdbcTemplate.<Integer, RuntimeException>withTransaction(
                    template -> template.fetchSingleRecord(
                            query,
                            (resultSet, rowNumber) -> resultSet.getInt(1),
                            preparedStatement -> {
                                preparedStatement.setInt(PolicyMgtSQLConstants.Column.TENANT_ID, tenantId);
                                if (hasFilter) {
                                    preparedStatement.setString(PolicyMgtSQLConstants.Column.FILTER, filterValue);
                                }
                            }));
            return count != null ? count : 0;
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
        }
    }

    /**
     * Supported database-specific pagination dialects.
     */
    private enum PaginationStyle {
        DEFAULT, MSSQL, ORACLE, DB2
    }

    private PaginationStyle resolvePaginationStyle() throws DataAccessException {

        // H2, MySQL, MariaDB and PostgreSQL all accept the LIMIT ... OFFSET ... syntax (DEFAULT).
        if (JdbcUtils.isOracleDB()) {
            return PaginationStyle.ORACLE;
        }
        if (JdbcUtils.isDB2DB()) {
            return PaginationStyle.DB2;
        }
        if (JdbcUtils.isMSSqlDB()) {
            return PaginationStyle.MSSQL;
        }
        return PaginationStyle.DEFAULT;
    }

    private String resolvePaginatedQuery(PaginationStyle style, boolean hasFilter) {

        switch (style) {
            case ORACLE:
                return hasFilter ? PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED_FILTER_ORACLE
                        : PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED_ORACLE;
            case DB2:
                return hasFilter ? PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED_FILTER_DB2
                        : PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED_DB2;
            case MSSQL:
                return hasFilter ? PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED_FILTER_MSSQL
                        : PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED_MSSQL;
            default:
                return hasFilter ? PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED_FILTER
                        : PolicyMgtSQLConstants.Query.GET_POLICIES_PAGINATED;
        }
    }

    private void bindPaginationParams(NamedPreparedStatement preparedStatement, PaginationStyle style,
                                      int offset, int limit) throws SQLException {

        switch (style) {
            case ORACLE:
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.UPPER_BOUND, offset + limit);
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.OFFSET, offset);
                break;
            case DB2:
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.LOWER_BOUND, offset + 1);
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.UPPER_BOUND, offset + limit);
                break;
            case MSSQL:
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.OFFSET, offset);
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.LIMIT, limit);
                break;
            default:
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.LIMIT, limit);
                preparedStatement.setInt(PolicyMgtSQLConstants.Column.OFFSET, offset);
        }
    }

    private List<PolicyResource> fetchPolicyResources(NamedTemplate<Policy> template, String policyId)
            throws DataAccessException {

        List<PolicyResource> resources = template.executeQuery(
                PolicyMgtSQLConstants.Query.GET_POLICY_RESOURCES,
                (resultSet, rowNumber) -> mapPolicyResource(
                        resultSet.getString(PolicyMgtSQLConstants.Column.ID),
                        resultSet.getString(PolicyMgtSQLConstants.Column.TARGET),
                        resultSet.getString(PolicyMgtSQLConstants.Column.RESOURCE_TYPE),
                        resultSet.getString(PolicyMgtSQLConstants.Column.RESOURCE_ID)),
                preparedStatement -> preparedStatement.setString(
                        PolicyMgtSQLConstants.Column.POLICY_ID, policyId));
        return resources != null ? resources : Collections.emptyList();
    }

    // Single extension point: add a case here (and a matching subclass of PolicyResource) for each new resource type.
    private PolicyResource mapPolicyResource(String id, String target, String resourceType, String resourceId)
            throws SQLException {

        ResourceType type = ResourceType.valueOf(resourceType);
        switch (type) {
            case RULE:
                return new RulePolicyResource(id, target, resourceId, null);
            default:
                throw new SQLException("Unsupported policy resource type: " + type);
        }
    }

}
