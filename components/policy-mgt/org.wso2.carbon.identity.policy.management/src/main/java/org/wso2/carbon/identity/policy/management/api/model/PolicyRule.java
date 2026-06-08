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

package org.wso2.carbon.identity.policy.management.api.model;

import org.wso2.carbon.identity.rule.management.api.model.Rule;

/**
 * Association between a Policy and a Rule for a specific device platform.
 * One platform may have at most one rule per policy.
 */
public class PolicyRule {

    private final String id;
    private final String ruleId;
    private final String platform;
    private final Rule rule;

    public PolicyRule(String id, String ruleId, String platform, Rule rule) {

        this.id = id;
        this.ruleId = ruleId;
        this.platform = platform;
        this.rule = rule;
    }

    public String getId() {

        return id;
    }

    public String getRuleId() {

        return ruleId;
    }

    public String getPlatform() {

        return platform;
    }

    public Rule getRule() {

        return rule;
    }
}
