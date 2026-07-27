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

package org.wso2.carbon.identity.rule.management.internal.dao;

import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

/**
 * Rule Management DAO.
 * This class is used to perform CRUD operations on Rule in the datastore.
 */
public interface RuleManagementDAO {

    /**
     * Add a new Rule.
     *
     * @param rule     Rule object
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    public void addRule(Rule rule, int tenantId) throws RuleManagementException;

    /**
     * Update an existing Rule.
     *
     * @param rule     Rule object
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    public void updateRule(Rule rule, int tenantId) throws RuleManagementException;

    /**
     * Delete a Rule.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    public void deleteRule(String ruleId, int tenantId) throws RuleManagementException;

    /**
     * Get a Rule by Rule ID.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @return Rule object
     * @throws RuleManagementException Rule Management Exception
     */
    public Rule getRuleByRuleId(String ruleId, int tenantId) throws RuleManagementException;

    /**
     * Activate a Rule.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    public void activateRule(String ruleId, int tenantId) throws RuleManagementException;

    /**
     * Deactivate a Rule.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    public void deactivateRule(String ruleId, int tenantId) throws RuleManagementException;
}
