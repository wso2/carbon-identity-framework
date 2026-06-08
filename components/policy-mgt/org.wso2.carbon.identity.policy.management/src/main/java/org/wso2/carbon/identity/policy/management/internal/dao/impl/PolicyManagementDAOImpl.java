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
import org.wso2.carbon.database.utils.jdbc.NamedTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyRule;
import org.wso2.carbon.identity.policy.management.api.util.PolicyManagementExceptionHandler;
import org.wso2.carbon.identity.policy.management.internal.constant.PolicyMgtSQLConstants;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;

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

                for (PolicyRule policyRule : policy.getRules()) {
                    final String ruleRowId = UUID.randomUUID().toString();
                    template.executeInsert(
                            PolicyMgtSQLConstants.Query.ADD_POLICY_RULE,
                            preparedStatement -> {
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.ID, ruleRowId);
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_ID, policy.getId());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.RULE_ID,
                                        policyRule.getRuleId());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.PLATFORM,
                                        policyRule.getPlatform());
                            },
                            policyRule,
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
                        PolicyMgtSQLConstants.Query.DELETE_POLICY_RULES,
                        preparedStatement -> preparedStatement.setString(
                                PolicyMgtSQLConstants.Column.POLICY_ID, policy.getId()));

                for (PolicyRule policyRule : policy.getRules()) {
                    final String ruleRowId = UUID.randomUUID().toString();
                    template.executeInsert(
                            PolicyMgtSQLConstants.Query.ADD_POLICY_RULE,
                            preparedStatement -> {
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.ID, ruleRowId);
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.POLICY_ID, policy.getId());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.RULE_ID,
                                        policyRule.getRuleId());
                                preparedStatement.setString(PolicyMgtSQLConstants.Column.PLATFORM,
                                        policyRule.getPlatform());
                            },
                            policyRule,
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
                List<PolicyRule> rules = fetchPolicyRules(template, base.getId());
                return new Policy(base.getId(), base.getName(), tenantDomain, rules);
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
                List<PolicyRule> rules = fetchPolicyRules(template, base.getId());
                return new Policy(base.getId(), base.getName(), tenantDomain, rules);
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
    public List<Policy> getPolicies(int tenantId) throws PolicyManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        NamedJdbcTemplate jdbcTemplate =
                new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            List<Policy> policies = jdbcTemplate.<List<Policy>, RuntimeException>withTransaction(
                    template -> template.executeQuery(
                            PolicyMgtSQLConstants.Query.GET_ALL_POLICIES,
                            (resultSet, rowNumber) -> new Policy(
                                    resultSet.getString(PolicyMgtSQLConstants.Column.ID),
                                    resultSet.getString(PolicyMgtSQLConstants.Column.POLICY_NAME),
                                    tenantDomain,
                                    Collections.emptyList()),
                            preparedStatement -> preparedStatement.setInt(
                                    PolicyMgtSQLConstants.Column.TENANT_ID, tenantId)));
            return policies != null ? policies : Collections.emptyList();
        } catch (TransactionException e) {
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
        }
    }

    private List<PolicyRule> fetchPolicyRules(NamedTemplate<Policy> template, String policyId)
            throws DataAccessException {

        List<PolicyRule> rules = template.executeQuery(
                PolicyMgtSQLConstants.Query.GET_POLICY_RULES,
                (resultSet, rowNumber) -> new PolicyRule(
                        resultSet.getString(PolicyMgtSQLConstants.Column.ID),
                        resultSet.getString(PolicyMgtSQLConstants.Column.RULE_ID),
                        resultSet.getString(PolicyMgtSQLConstants.Column.PLATFORM),
                        null),
                preparedStatement -> preparedStatement.setString(
                        PolicyMgtSQLConstants.Column.POLICY_ID, policyId));
        return rules != null ? rules : Collections.emptyList();
    }

}
