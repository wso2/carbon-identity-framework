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

package org.wso2.carbon.identity.rule.management.internal.dao.impl;

/**
 * Represents a rule in the data layer.
 * This class has the rule JSON and the active status of the rule which is stored in the database.
 */
public class RuleData {

    private String ruleJson;
    private boolean isActive;

    public String getRuleJson() {

        return ruleJson;
    }

    public void setRuleJson(String ruleJson) {

        this.ruleJson = ruleJson;
    }

    public boolean isActive() {

        return isActive;
    }

    public void setActive(boolean active) {

        isActive = active;
    }
}
