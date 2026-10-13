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

package org.wso2.carbon.identity.rule.management.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.api.util.AuditLogBuilderForRule;

import java.util.Arrays;

public class AuditLogBuilderForRuleTest {

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Expected an instance of ORCombinedRule.")
    public void testCreateAuditLogWithoutRule() {
        AuditLogBuilderForRule.buildRuleValue(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Expected an instance of ORCombinedRule.")
    public void testCreateAuditLogWithDifferentRuleInstance() {

        AuditLogBuilderForRule.buildRuleValue(buildMockANDCombinedRule());
    }

    @Test
    public void testCreateRuleValue() {

        Assert.assertEquals(AuditLogBuilderForRule.buildRuleValue(buildMockORCombinedRule()),
                "( grantType equals a****************e ) or ( grantType equals a****************e )");
    }

    private ANDCombinedRule buildMockANDCombinedRule() {
        Expression expression = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();

        return new ANDCombinedRule.Builder().addExpression(expression).build();
    }

    private ORCombinedRule buildMockORCombinedRule() {
        return new ORCombinedRule.Builder().addRule(buildMockANDCombinedRule()).addRule(buildMockANDCombinedRule())
                .build();
    }

    @Test
    public void testCreateRuleValueForEveryKindOfValue() {

        ORCombinedRule rule = new ORCombinedRule.Builder().addRule(new ANDCombinedRule.Builder()
                .addExpression(new Expression.Builder().field("user.claims").operator("isBlank").build())
                .addExpression(new Expression.Builder().field("grantType").operator("in")
                        .value(new Value(Arrays.asList("authorization_code", "refresh_token"))).build())
                .addExpression(new Expression.Builder().field("user.collectedClaims").operator("notEquals")
                        .value(new Value(new FieldReference("user.claims", "country"))).build())
                .build()).build();

        Assert.assertEquals(AuditLogBuilderForRule.buildRuleValue(rule), "( user.claims isBlank and grantType in ["
                + LoggerUtils.getMaskedContent("authorization_code") + ", "
                + LoggerUtils.getMaskedContent("refresh_token")
                + "] and user.collectedClaims notEquals field user.claims )");
    }
}
