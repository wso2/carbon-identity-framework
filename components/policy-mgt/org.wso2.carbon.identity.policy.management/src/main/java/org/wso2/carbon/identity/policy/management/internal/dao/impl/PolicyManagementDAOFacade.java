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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyBasicInfo;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.util.PolicyManagementExceptionHandler;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for Policy Management DAO.
 * Coordinates between rule-mgt service and the policy DB layer as a single logical operation.
 * Implements best-effort saga compensation: if the DB write fails after rule-mgt succeeded,
 * we attempt to roll back rule-mgt changes.
 */
public class PolicyManagementDAOFacade implements PolicyManagementDAO {

    private static final Log LOG = LogFactory.getLog(PolicyManagementDAOFacade.class);

    private final PolicyManagementDAO policyManagementDAO;

    public PolicyManagementDAOFacade(PolicyManagementDAO policyManagementDAO) {

        this.policyManagementDAO = policyManagementDAO;
    }

    @Override
    public Policy addPolicy(Policy policy, int tenantId) throws PolicyManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        RuleManagementService ruleManagementService = PolicyMgtComponentServiceHolder
                .getInstance().getRuleManagementService();

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
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Rule added for policy target '" + pr.getTarget()
                            + "' with ruleId: " + createdRule.getId());
                }
            }
        } catch (RuleManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_ADDING_RULE_FOR_POLICY, e, policy.getName());
        }

        Policy policyWithResourceIds = new Policy(policy.getId(), policy.getName(), tenantDomain, resourcesWithIds);

        try {
            return policyManagementDAO.addPolicy(policyWithResourceIds, tenantId);
        } catch (PolicyManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw e;
        }
    }

    @Override
    public Policy updatePolicy(Policy policy, int tenantId) throws PolicyManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        RuleManagementService ruleManagementService = PolicyMgtComponentServiceHolder
                .getInstance().getRuleManagementService();

        Policy existingPolicy = policyManagementDAO.getPolicyById(policy.getId(), tenantId);

        deleteRulesFromRuleManagementService(existingPolicy.getResources(), tenantDomain, ruleManagementService,
                policy.getId());

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

        Policy policyWithResourceIds = new Policy(policy.getId(), policy.getName(), tenantDomain, resourcesWithIds);

        try {
            return policyManagementDAO.updatePolicy(policyWithResourceIds, tenantId);
        } catch (PolicyManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw e;
        }
    }

    @Override
    public void deletePolicy(String policyId, int tenantId) throws PolicyManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        RuleManagementService ruleManagementService = PolicyMgtComponentServiceHolder
                .getInstance().getRuleManagementService();

        Policy existingPolicy = policyManagementDAO.getPolicyById(policyId, tenantId);

        policyManagementDAO.deletePolicy(policyId, tenantId);

        if (existingPolicy != null) {
            deleteRulesFromRuleManagementService(existingPolicy.getResources(), tenantDomain, ruleManagementService,
                    policyId);
        }
    }

    @Override
    public Policy getPolicyById(String policyId, int tenantId) throws PolicyManagementException {

        return policyManagementDAO.getPolicyById(policyId, tenantId);
    }

    @Override
    public Policy getPolicyByName(String policyName, int tenantId) throws PolicyManagementException {

        return policyManagementDAO.getPolicyByName(policyName, tenantId);
    }

    @Override
    public List<PolicyBasicInfo> getPolicies(int tenantId, String filter, int offset, int limit)
            throws PolicyManagementException {

        return policyManagementDAO.getPolicies(tenantId, filter, offset, limit);
    }

    @Override
    public int getPolicyCount(int tenantId, String filter) throws PolicyManagementException {

        return policyManagementDAO.getPolicyCount(tenantId, filter);
    }

    @Override
    public String getPolicyIdByName(String policyName, int tenantId) throws PolicyManagementException {

        return policyManagementDAO.getPolicyIdByName(policyName, tenantId);
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
}
