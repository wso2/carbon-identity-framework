/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.api.model;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;

/**
 * Represents an Action Rule.
 * This class wraps the Rule object and provides a way to lazily load the Rule from the Rule Management Service.
 */
public class ActionRule {

    private String id;
    private String tenantDomain;
    private Rule rule;

    private ActionRule(String id, String tenantDomain) {

        this.id = id;
        this.tenantDomain = tenantDomain;
    }

    private ActionRule(Rule rule) {

        this.rule = rule;
    }

    public String getId() {

        return rule != null ? rule.getId() : id;
    }

    public Rule getRule() throws ActionMgtException {

        if (rule != null) {
            return rule;
        }

        /*
          If rule is not loaded, load it from the Rule Management Service.
          This happens when multiple actions for an action type is loaded where the ActionRule is created
          using the rule id referenced in actions data layer.
          The need to load the rule for the action comes when executing actions in chain.
          Thus, it's more efficient to load the rule lazily as rule execution depends on factors like
          action status (active/inactive), returning state of the prior action in the chain, etc.
         */
        rule = getRuleFromRuleManagementService();
        return rule;
    }

    private Rule getRuleFromRuleManagementService() throws ActionMgtServerException {

        if (StringUtils.isBlank(id) || StringUtils.isBlank(tenantDomain)) {
            /*
                This could happen if the ActionRule is created without the Rule object.
                That means the rule value is explicitly set to null.
                In such cases, loading the Rule from the Rule Management Service is not expected.
                This scenario is used to let the user remove a Rule from an Action via the Action update API.
             */
            return null;
        }

        try {
            return ActionMgtServiceComponentHolder.getInstance()
                    .getRuleManagementService()
                    .getRuleByRuleId(id, tenantDomain);
        } catch (RuleManagementException e) {
            throw new ActionMgtServerException("Error while retrieving the Rule.", e);
        }
    }

    /**
     * Create an ActionRule object with the given Rule.
     *
     * @param rule Rule object.
     * @return ActionRule object.
     */
    public static ActionRule create(Rule rule) {

        return new ActionRule(rule);
    }

    /**
     * Create an ActionRule object with the given rule ID and tenant domain.
     * rule ID is used to lazily load the Rule from the Rule Management Service.
     *
     * @param id           Rule ID. Cannot be empty.
     * @param tenantDomain Tenant domain. Cannot be empty.
     * @return ActionRule object.
     */
    public static ActionRule create(String id, String tenantDomain) {

        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("Rule ID cannot be empty.");
        }
        if (StringUtils.isBlank(tenantDomain)) {
            throw new IllegalArgumentException("Tenant domain cannot be empty.");
        }

        return new ActionRule(id, tenantDomain);
    }
}
