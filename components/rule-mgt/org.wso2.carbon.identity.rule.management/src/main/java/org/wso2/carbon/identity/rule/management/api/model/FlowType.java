/*
 * Copyright (c) 2024-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.management.api.model;

/**
 * This class is used to define the flow types in Rule Management.
 */
public enum FlowType {

    // The value is the built-in baseline for the maximum number of expressions combined with AND for the flow.
    // Most flows use the default baseline of 5, while device policy allows up to 15.
    PRE_ISSUE_ACCESS_TOKEN(5),
    PRE_UPDATE_PASSWORD(5),
    PRE_UPDATE_PROFILE(5),
    PRE_ISSUE_ID_TOKEN(5),
    APPROVAL_WORKFLOW(5),
    DEVICE_POLICY(15);

    private final int maxExpressionsCombinedWithAnd;

    FlowType(int maxExpressionsCombinedWithAnd) {

        this.maxExpressionsCombinedWithAnd = maxExpressionsCombinedWithAnd;
    }

    /**
     * Get the maximum number of expressions that can be combined with AND for this flow type.
     * This is the built-in baseline for the flow and can be overridden through the
     * {@code RuleManagement.MaxExpressionsCombinedWithAND} configuration.
     *
     * @return Maximum number of expressions that can be combined with AND.
     */
    public int getMaxExpressionsCombinedWithAnd() {

        return maxExpressionsCombinedWithAnd;
    }
}
