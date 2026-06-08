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

package org.wso2.carbon.identity.policy.management.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyRule;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.policy.management.api.util.PolicyManagementExceptionHandler;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.CacheBackedPolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.PolicyManagementDAOFacade;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.PolicyManagementDAOImpl;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of Policy Management Service.
 * Orchestrates between the DAO layer (DB) and rule hydration via rule-mgt service.
 */
public class PolicyManagementServiceImpl implements PolicyManagementService {

    private static final Log LOG = LogFactory.getLog(PolicyManagementServiceImpl.class);
    private static final PolicyManagementServiceImpl INSTANCE = new PolicyManagementServiceImpl();
    private final PolicyManagementDAO policyManagementDAO;

    private PolicyManagementServiceImpl() {

        policyManagementDAO = new CacheBackedPolicyManagementDAO(
                new PolicyManagementDAOFacade(new PolicyManagementDAOImpl()));
    }

    public static PolicyManagementServiceImpl getInstance() {

        return INSTANCE;
    }

    @Override
    public Policy addPolicy(Policy policy, String tenantDomain) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating policy with name: %s for tenant: %s",
                    policy.getName(), tenantDomain));
        }
        validatePolicyFields(policy);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        validateUniquePolicyName(policy.getName(), null, tenantId);

        Policy policyWithId = new Policy(
                UUID.randomUUID().toString(),
                policy.getName(),
                tenantDomain,
                policy.getRules());

        return policyManagementDAO.addPolicy(policyWithId, tenantId);
    }

    @Override
    public Policy updatePolicy(Policy policy, String tenantDomain) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating policy with ID: %s for tenant: %s",
                    policy.getId(), tenantDomain));
        }
        validatePolicyFields(policy);
        validateIfPolicyExists(policy.getId(), tenantDomain);

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        validateUniquePolicyName(policy.getName(), policy.getId(), tenantId);
        return policyManagementDAO.updatePolicy(policy, tenantId);
    }

    @Override
    public void deletePolicy(String policyId, String tenantDomain) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting policy with ID: %s for tenant: %s",
                    policyId, tenantDomain));
        }
        if (isPolicyExists(policyId, tenantDomain)) {
            policyManagementDAO.deletePolicy(policyId, IdentityTenantUtil.getTenantId(tenantDomain));
        }
    }

    @Override
    public Policy getPolicyById(String policyId, String tenantDomain) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving policy with ID: %s for tenant: %s",
                    policyId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Policy policy = policyManagementDAO.getPolicyById(policyId, tenantId);
        if (policy == null) {
            return null;
        }
        return hydrateRules(policy, tenantDomain);
    }

    @Override
    public Policy getPolicyByName(String policyName, String tenantDomain) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving policy with name: %s for tenant: %s",
                    policyName, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Policy policy = policyManagementDAO.getPolicyByName(policyName, tenantId);
        if (policy == null) {
            return null;
        }
        return hydrateRules(policy, tenantDomain);
    }

    @Override
    public List<Policy> getPolicies(String tenantDomain) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting all policies for tenant: %s", tenantDomain));
        }
        return policyManagementDAO.getPolicies(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    // Actions are not hydrated — actionIds are sufficient for the executor.
    private Policy hydrateRules(Policy policy, String tenantDomain) throws PolicyManagementException {

        List<PolicyRule> hydratedRules = new ArrayList<>();
        for (PolicyRule pr : policy.getRules()) {
            try {
                Rule rule = PolicyMgtComponentServiceHolder.getInstance()
                        .getRuleManagementService()
                        .getRuleByRuleId(pr.getRuleId(), tenantDomain);
                hydratedRules.add(new PolicyRule(pr.getId(), pr.getRuleId(), pr.getPlatform(), rule));
            } catch (RuleManagementException e) {
                throw PolicyManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
            }
        }
        return new Policy(policy.getId(), policy.getName(), policy.getTenantDomain(), hydratedRules);
    }

    private void validateIfPolicyExists(String policyId, String tenantDomain)
            throws PolicyManagementException {

        if (!isPolicyExists(policyId, tenantDomain)) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_POLICY_NOT_FOUND, policyId);
        }
    }

    private boolean isPolicyExists(String policyId, String tenantDomain) throws PolicyManagementException {

        Policy existingPolicy = policyManagementDAO.getPolicyById(
                policyId, IdentityTenantUtil.getTenantId(tenantDomain));
        return existingPolicy != null;
    }

    private void validatePolicyFields(Policy policy) throws PolicyManagementClientException {

        if (policy.getName() == null || policy.getName().trim().isEmpty()) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, "Policy name");
        }
        validateUniquePlatformsInRules(policy);
    }

    private void validateUniquePlatformsInRules(Policy policy) throws PolicyManagementClientException {

        Set<String> seenPlatforms = new HashSet<>();
        for (PolicyRule rule : policy.getRules()) {
            if (rule.getPlatform() == null) {
                continue;
            }
            String normalized = rule.getPlatform().toLowerCase(Locale.ROOT);
            if (!seenPlatforms.add(normalized)) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_DUPLICATE_PLATFORM_IN_POLICY,
                        policy.getName(), rule.getPlatform());
            }
        }
    }

    private void validateUniquePolicyName(String name, String excludePolicyId, int tenantId)
            throws PolicyManagementException {

        String existingId = policyManagementDAO.getPolicyIdByName(name, tenantId);
        if (existingId != null && !existingId.equals(excludePolicyId)) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_POLICY_ALREADY_EXISTS, name);
        }
    }
}
