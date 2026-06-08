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
import org.wso2.carbon.identity.policy.management.api.model.PolicyRule;
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
        List<PolicyRule> rulesWithIds = new ArrayList<>();

        try {
            for (PolicyRule pr : policy.getRules()) {
                Rule createdRule = ruleManagementService.addRule(pr.getRule(), tenantDomain);
                createdRuleIds.add(createdRule.getId());
                rulesWithIds.add(new PolicyRule(pr.getId(), createdRule.getId(), pr.getPlatform(), null));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Rule added for policy platform '" + pr.getPlatform()
                            + "' with ruleId: " + createdRule.getId());
                }
            }
        } catch (RuleManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_ADDING_RULE_FOR_POLICY, e, policy.getName());
        }

        Policy policyWithRuleIds = new Policy(policy.getId(), policy.getName(), tenantDomain, rulesWithIds);

        try {
            return policyManagementDAO.addPolicy(policyWithRuleIds, tenantId);
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

        deleteRulesFromRuleManagementService(existingPolicy.getRules(), tenantDomain, ruleManagementService,
                policy.getId());

        List<String> createdRuleIds = new ArrayList<>();
        List<PolicyRule> rulesWithIds = new ArrayList<>();

        try {
            for (PolicyRule pr : policy.getRules()) {
                Rule createdRule = ruleManagementService.addRule(pr.getRule(), tenantDomain);
                createdRuleIds.add(createdRule.getId());
                rulesWithIds.add(new PolicyRule(pr.getId(), createdRule.getId(), pr.getPlatform(), null));
            }
        } catch (RuleManagementException e) {
            compensateCreatedRules(createdRuleIds, tenantDomain, ruleManagementService);
            throw PolicyManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_UPDATING_RULE_FOR_POLICY, e, policy.getId());
        }

        Policy policyWithRuleIds = new Policy(policy.getId(), policy.getName(), tenantDomain, rulesWithIds);

        try {
            return policyManagementDAO.updatePolicy(policyWithRuleIds, tenantId);
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
            deleteRulesFromRuleManagementService(existingPolicy.getRules(), tenantDomain, ruleManagementService,
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
    public List<Policy> getPolicies(int tenantId) throws PolicyManagementException {

        return policyManagementDAO.getPolicies(tenantId);
    }

    @Override
    public String getPolicyIdByName(String policyName, int tenantId) throws PolicyManagementException {

        return policyManagementDAO.getPolicyIdByName(policyName, tenantId);
    }

    private void deleteRulesFromRuleManagementService(List<PolicyRule> rules, String tenantDomain,
                                                      RuleManagementService ruleManagementService,
                                                      String policyId) {

        for (PolicyRule pr : rules) {
            try {
                ruleManagementService.deleteRule(pr.getRuleId(), tenantDomain);
            } catch (RuleManagementException e) {
                LOG.error("Failed to delete rule " + pr.getRuleId()
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
