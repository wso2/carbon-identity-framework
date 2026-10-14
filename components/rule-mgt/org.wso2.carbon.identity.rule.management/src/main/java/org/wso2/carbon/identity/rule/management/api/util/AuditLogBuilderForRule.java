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

package org.wso2.carbon.identity.rule.management.api.util;

import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;

import java.util.stream.Collectors;

/**
 * Audit log builder for rule.
 */
public class AuditLogBuilderForRule {

    private AuditLogBuilderForRule() {

    }

    /**
     * Builds a string representation of an {@link ORCombinedRule}.
     * Converts the rule into a logical expression format, where AND conditions
     * are grouped in parentheses and combined using OR.
     *
     * @param rule The rule to convert (must be an instance of {@link ORCombinedRule}).
     * @return A string representation of the rule.
     * @throws IllegalArgumentException if the rule is not an {@link ORCombinedRule}.
     */
    public static String buildRuleValue(Rule rule) {

        if (!(rule instanceof ORCombinedRule)) {
            throw new IllegalArgumentException("Expected an instance of ORCombinedRule.");
        }

        return ((ORCombinedRule) rule).getRules().stream()
                .map(andRule -> "( " + andRule.getExpressions().stream()
                        .map(expression -> expression.getField() + " " + expression.getOperator()
                                + describe(expression.getValue()))
                        .collect(Collectors.joining(" and ")) + " )")
                .collect(Collectors.joining(" or "));
    }

    private static String describe(Value value) {

        if (value == null) {
            return "";
        }
        if (value.getType() == Value.Type.FIELD && value.getFieldReference() != null) {
            return " field " + value.getFieldReference().getName();
        }
        if (value.getType() == Value.Type.LIST && value.getFieldValues() != null) {
            return " " + value.getFieldValues().stream().map(LoggerUtils::getMaskedContent)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        return " " + LoggerUtils.getMaskedContent(value.getFieldValue());
    }
}
