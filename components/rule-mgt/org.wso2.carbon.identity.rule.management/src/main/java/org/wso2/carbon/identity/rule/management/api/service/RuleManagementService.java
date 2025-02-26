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

package org.wso2.carbon.identity.rule.management.api.service;

import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

/**
 * This interface is used to define the Rule Management Service.
 * This interface has the methods to add, update, delete, get and deactivate rules.
 */
public interface RuleManagementService {

    /**
     * Adds a new rule.
     *
     * @param rule         Rule to be added.
     * @param tenantDomain Tenant domain.
     * @return Added rule.
     * @throws RuleManagementException If an error occurs while adding the rule.
     */
    public Rule addRule(Rule rule, String tenantDomain) throws RuleManagementException;

    /**
     * Updates an existing rule.
     *
     * @param rule         Rule to be updated.
     * @param tenantDomain Tenant domain.
     * @return Updated rule.
     * @throws RuleManagementException If an error occurs while updating the rule.
     */
    public Rule updateRule(Rule rule, String tenantDomain) throws RuleManagementException;

    /**
     * Deletes a rule.
     *
     * @param ruleId       Rule ID.
     * @param tenantDomain Tenant domain.
     * @throws RuleManagementException If an error occurs while deleting the rule.
     */
    public void deleteRule(String ruleId, String tenantDomain) throws RuleManagementException;

    /**
     * Retrieves a rule by rule ID.
     *
     * @param ruleId       Rule ID.
     * @param tenantDomain Tenant domain.
     * @return Rule.
     * @throws RuleManagementException If an error occurs while retrieving the rule.
     */
    public Rule getRuleByRuleId(String ruleId, String tenantDomain) throws RuleManagementException;

    /**
     * Deactivates a rule.
     *
     * @param ruleId       Rule ID.
     * @param tenantDomain Tenant domain.
     * @return Deactivated rule.
     * @throws RuleManagementException If an error occurs while deactivating the rule.
     */
    public Rule deactivateRule(String ruleId, String tenantDomain) throws RuleManagementException;
}
