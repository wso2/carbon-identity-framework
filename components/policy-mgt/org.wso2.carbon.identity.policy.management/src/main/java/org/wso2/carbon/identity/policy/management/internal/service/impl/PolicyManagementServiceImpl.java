/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
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
import org.wso2.carbon.identity.policy.management.api.model.PolicyBasicInfo;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.service.PolicyManagementService;
import org.wso2.carbon.identity.policy.management.api.util.PolicyManagementExceptionHandler;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.CacheBackedPolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.PolicyManagementDAOImpl;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of Policy Management Service.
 * Orchestrates rule-mgt service calls and best-effort saga compensation around the DAO layer.
 */
public class PolicyManagementServiceImpl implements PolicyManagementService {

    private static final Log LOG = LogFactory.getLog(PolicyManagementServiceImpl.class);
    private final PolicyManagementDAO policyManagementDAO;

    /**
     * Default constructor used by OSGi component. Delegates to the DAO-backed constructor.
     */
    public PolicyManagementServiceImpl() {

        this(new CacheBackedPolicyManagementDAO(new PolicyManagementDAOImpl()));
    }

    /**
     * Constructor for tests or manual instantiation with a custom DAO.
     *
     * @param policyManagementDAO DAO implementation to use.
     */
    public PolicyManagementServiceImpl(PolicyManagementDAO policyManagementDAO) {

        this.policyManagementDAO = policyManagementDAO;
    }

    @Override
    public Policy addPolicy(Policy policy, String tenantDomain) throws PolicyManagementException {

        validatePolicyFields(policy);
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating policy with name: %s for tenant: %s",
                    policy.getName(), tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        validateUniquePolicyName(policy.getName(), null, tenantId);

        Policy policyWithId = new Policy(
                UUID.randomUUID().toString(),
                policy.getName(),
                tenantDomain,
                policy.getResources());

        RuleManagementService ruleManagementService =
                PolicyMgtComponentServiceHolder.getInstance().getRuleManagementService();
        List<String> createdRuleIds = new ArrayList<>();
        List<PolicyResource> resourcesWithIds = new ArrayList<>();

        try {
            for (PolicyResource pr : policyWithId.getResources()) {
                if (pr.getResourceType() != ResourceType.RULE) {
                    // ACTION (and future types): resourceId already references an existing resource.
                    resourcesWithIds.add(pr);
                    continue;
                }
                Rule createdRule = ruleManagementService.addRule(pr.getRule(), tenantDomain);
                createdRuleIds.add(createdRule.getId());
                resourcesWithIds.add(new PolicyResource(
                        pr.getId(), pr.getTarget(), ResourceType.RULE, createdRule.getId(), null));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Rule added for policy target '" + pr.getTarget()
                            + "' with ruleId: " + createdRule.getId());
                }
            }
        } catch (RuleManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_ADDING_RULE_FOR_POLICY, e, policyWithId.getName());
        }

        Policy policyWithResourceIds = new Policy(
                policyWithId.getId(), policyWithId.getName(), tenantDomain, resourcesWithIds);

        try {
            return policyManagementDAO.addPolicy(policyWithResourceIds, tenantId);
        } catch (PolicyManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw e;
        }
    }

    @Override
    public Policy updatePolicy(Policy policy, String tenantDomain) throws PolicyManagementException {

        validatePolicyFields(policy);
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating policy with ID: %s for tenant: %s",
                    policy.getId(), tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Policy existingPolicy = policyManagementDAO.getPolicyById(policy.getId(), tenantId);
        if (existingPolicy == null) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_POLICY_NOT_FOUND, policy.getId());
        }
        validateUniquePolicyName(policy.getName(), policy.getId(), tenantId);

        RuleManagementService ruleManagementService =
                PolicyMgtComponentServiceHolder.getInstance().getRuleManagementService();

        // Create the new rules first so the old rules remain intact until the DB commit succeeds. The old rules
        // are only deleted after the policy is durably updated, keeping the operation recoverable on any failure.
        List<String> createdRuleIds = new ArrayList<>();
        List<PolicyResource> resourcesWithIds = new ArrayList<>();

        try {
            for (PolicyResource pr : policy.getResources()) {
                if (pr.getResourceType() != ResourceType.RULE) {
                    // ACTION (and future types): resourceId already references an existing resource.
                    resourcesWithIds.add(pr);
                    continue;
                }
                Rule createdRule = ruleManagementService.addRule(pr.getRule(), tenantDomain);
                createdRuleIds.add(createdRule.getId());
                resourcesWithIds.add(new PolicyResource(
                        pr.getId(), pr.getTarget(), ResourceType.RULE, createdRule.getId(), null));
            }
        } catch (RuleManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_UPDATING_RULE_FOR_POLICY, e, policy.getId());
        }

        Policy policyWithResourceIds = new Policy(
                policy.getId(), policy.getName(), tenantDomain, resourcesWithIds);

        Policy updatedPolicy;
        try {
            updatedPolicy = policyManagementDAO.updatePolicy(policyWithResourceIds, tenantId);
        } catch (PolicyManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw e;
        }

        // DB commit succeeded; the old rules are now safe to remove (best-effort).
        deleteRulesFromRuleManagementService(existingPolicy.getResources(), tenantDomain, ruleManagementService,
                policy.getId());

        return updatedPolicy;
    }

    @Override
    public void deletePolicy(String policyId, String tenantDomain) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting policy with ID: %s for tenant: %s",
                    policyId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        RuleManagementService ruleManagementService =
                PolicyMgtComponentServiceHolder.getInstance().getRuleManagementService();
        Policy existingPolicy = policyManagementDAO.getPolicyById(policyId, tenantId);
        if (existingPolicy == null) {
            return;
        }
        policyManagementDAO.deletePolicy(policyId, tenantId);
        deleteRulesFromRuleManagementService(existingPolicy.getResources(), tenantDomain, ruleManagementService,
                policyId);
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
        return hydrateResources(policy, tenantDomain);
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
        return hydrateResources(policy, tenantDomain);
    }

    @Override
    public List<PolicyBasicInfo> getPolicies(String tenantDomain, String filter, int offset, int limit)
            throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Listing policies for tenant: %s with filter: %s, offset: %d, limit: %d",
                    tenantDomain, filter, offset, limit));
        }
        return policyManagementDAO.getPolicies(
                IdentityTenantUtil.getTenantId(tenantDomain), filter, offset, limit);
    }

    @Override
    public int getPolicyCount(String tenantDomain, String filter) throws PolicyManagementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Counting policies for tenant: %s with filter: %s", tenantDomain, filter));
        }
        return policyManagementDAO.getPolicyCount(IdentityTenantUtil.getTenantId(tenantDomain), filter);
    }

    // Only RULE resources are hydrated; actions are referenced by id and need no hydration here.
    private Policy hydrateResources(Policy policy, String tenantDomain) throws PolicyManagementException {

        List<PolicyResource> hydratedResources = new ArrayList<>();
        for (PolicyResource pr : policy.getResources()) {
            if (pr.getResourceType() != ResourceType.RULE) {
                hydratedResources.add(pr);
                continue;
            }
            try {
                Rule rule = PolicyMgtComponentServiceHolder.getInstance()
                        .getRuleManagementService()
                        .getRuleByRuleId(pr.getResourceId(), tenantDomain);
                hydratedResources.add(new PolicyResource(
                        pr.getId(), pr.getTarget(), ResourceType.RULE, pr.getResourceId(), rule));
            } catch (RuleManagementException e) {
                throw PolicyManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_WHILE_RETRIEVING_POLICY, e);
            }
        }
        return new Policy(policy.getId(), policy.getName(), policy.getTenantDomain(), hydratedResources);
    }

    private void deleteRulesFromRuleManagementService(List<PolicyResource> resources, String tenantDomain,
                                                      RuleManagementService ruleManagementService,
                                                      String policyId) {

        for (PolicyResource pr : resources) {
            if (pr.getResourceType() != ResourceType.RULE) {
                continue;
            }
            try {
                ruleManagementService.deleteRule(pr.getResourceId(), tenantDomain);
            } catch (RuleManagementException e) {
                LOG.error("Failed to delete rule " + pr.getResourceId()
                        + " from rule-mgt for policy " + policyId + ". Rule may be orphaned.", e);
            }
        }
    }

    private void compensateCreatedRules(List<String> ruleIds, String tenantDomain,
                                        RuleManagementService ruleManagementService) {

        for (String ruleId : ruleIds) {
            try {
                ruleManagementService.deleteRule(ruleId, tenantDomain);
            } catch (RuleManagementException ex) {
                LOG.error("Saga compensation failed: could not delete rule " + ruleId
                        + " from rule-mgt after policy persistence failure.", ex);
            }
        }
    }

    private void validatePolicyFields(Policy policy) throws PolicyManagementClientException {

        if (policy == null) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, "Policy");
        }
        if (policy.getName() == null || policy.getName().trim().isEmpty()) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, "Policy name");
        }
        validateUniqueTargetsPerResourceType(policy);
    }

    private void validateUniqueTargetsPerResourceType(Policy policy) throws PolicyManagementClientException {

        Set<String> seenTargets = new HashSet<>();
        for (PolicyResource resource : policy.getResources()) {
            if (resource == null || resource.getTarget() == null) {
                continue;
            }
            if (resource.getResourceType() == null) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, "Resource type");
            }
            String key = resource.getResourceType().name() + "|"
                    + resource.getTarget().toLowerCase(Locale.ROOT);
            if (!seenTargets.add(key)) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_DUPLICATE_PLATFORM_IN_POLICY,
                        policy.getName(), resource.getTarget());
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
