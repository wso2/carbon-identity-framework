/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.management.internal.service.impl;

import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementClientException;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
import org.wso2.carbon.identity.rule.management.internal.dao.RuleManagementDAO;
import org.wso2.carbon.identity.rule.management.internal.dao.impl.CacheBackedRuleManagementDAO;
import org.wso2.carbon.identity.rule.management.internal.dao.impl.RuleManagementDAOImpl;

/**
 * Implementation of Rule Management Service.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.rule.management.api.service.RuleManagementService",
                "service.scope=singleton"
        }
)
public class RuleManagementServiceImpl implements RuleManagementService {

    private static final RuleManagementServiceImpl ruleManagementService = new RuleManagementServiceImpl();
    private final RuleManagementDAO ruleManagementDAO;

    private RuleManagementServiceImpl() {

        ruleManagementDAO = new CacheBackedRuleManagementDAO(new RuleManagementDAOImpl());
    }

    public static RuleManagementServiceImpl getInstance() {

        return ruleManagementService;
    }

    /**
     * Add a new rule.
     *
     * @param rule         Rule to be added.
     * @param tenantDomain Tenant domain.
     * @return Added rule.
     * @throws RuleManagementException If an error occurs while adding the rule.
     */
    @Override
    public Rule addRule(Rule rule, String tenantDomain) throws RuleManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        ruleManagementDAO.addRule(rule, tenantId);
        return ruleManagementDAO.getRuleByRuleId(rule.getId(), tenantId);
    }

    /**
     * Update an existing rule.
     *
     * @param rule         Rule to be updated.
     * @param tenantDomain Tenant domain.
     * @return Updated rule.
     * @throws RuleManagementException If an error occurs while updating the rule.
     */
    @Override
    public Rule updateRule(Rule rule, String tenantDomain) throws RuleManagementException {

        validateIfRuleExists(rule.getId(), tenantDomain);

        ruleManagementDAO.updateRule(rule, IdentityTenantUtil.getTenantId(tenantDomain));
        return ruleManagementDAO.getRuleByRuleId(rule.getId(), IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Delete a rule.
     *
     * @param ruleId       Rule ID.
     * @param tenantDomain Tenant domain.
     * @throws RuleManagementException If an error occurs while deleting the rule.
     */
    @Override
    public void deleteRule(String ruleId, String tenantDomain) throws RuleManagementException {

        if (isRuleExists(ruleId, tenantDomain)) {
            ruleManagementDAO.deleteRule(ruleId, IdentityTenantUtil.getTenantId(tenantDomain));
        }
    }

    /**
     * Retrieve a rule by rule ID.
     *
     * @param ruleId       Rule ID.
     * @param tenantDomain Tenant domain.
     * @return Rule.
     * @throws RuleManagementException If an error occurs while retrieving the rule.
     */
    @Override
    public Rule getRuleByRuleId(String ruleId, String tenantDomain) throws RuleManagementException {

        return ruleManagementDAO.getRuleByRuleId(ruleId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Deactivate a rule.
     *
     * @param ruleId       Rule ID.
     * @param tenantDomain Tenant domain.
     * @return Deactivated rule.
     * @throws RuleManagementException If an error occurs while deactivating the rule.
     */
    @Override
    public Rule deactivateRule(String ruleId, String tenantDomain) throws RuleManagementException {

        validateIfRuleExists(ruleId, tenantDomain);

        ruleManagementDAO.deactivateRule(ruleId, IdentityTenantUtil.getTenantId(tenantDomain));
        return ruleManagementDAO.getRuleByRuleId(ruleId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    private void validateIfRuleExists(String ruleId, String tenantDomain) throws RuleManagementException {

        if (!isRuleExists(ruleId, tenantDomain)) {
            throw new RuleManagementClientException("Rule not found for the given rule id: " + ruleId);
        }
    }

    private boolean isRuleExists(String ruleId, String tenantDomain) throws RuleManagementException {

        Rule existingRule =
                ruleManagementDAO.getRuleByRuleId(ruleId, IdentityTenantUtil.getTenantId(tenantDomain));
        return existingRule != null;
    }
}
