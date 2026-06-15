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
                policy.getResources());

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
        validateUniqueTargetsPerResourceType(policy);
    }

    private void validateUniqueTargetsPerResourceType(Policy policy) throws PolicyManagementClientException {

        Set<String> seenTargets = new HashSet<>();
        for (PolicyResource resource : policy.getResources()) {
            if (resource.getTarget() == null) {
                continue;
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
