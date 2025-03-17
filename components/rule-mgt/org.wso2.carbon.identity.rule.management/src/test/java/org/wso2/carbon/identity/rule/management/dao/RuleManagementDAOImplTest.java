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

package org.wso2.carbon.identity.rule.management.dao;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.internal.dao.impl.RuleManagementDAOImpl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class RuleManagementDAOImplTest {

    public static final int TENANT_ID = 1;
    private Rule createdRule;
    RuleManagementDAOImpl ruleManagementDAOImpl = new RuleManagementDAOImpl();

    @Test
    public void testAddRule() throws RuleManagementException {

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ANDCombinedRule andCombinedRule1 =
                new ANDCombinedRule.Builder().addExpression(expression1).addExpression(expression2).build();

        Expression expression3 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp2")).build();
        ANDCombinedRule andCombinedRule2 =
                new ANDCombinedRule.Builder().addExpression(expression3).build();

        ORCombinedRule orCombinedRule =
                new ORCombinedRule.Builder().addRule(andCombinedRule1).addRule(andCombinedRule2).build();

        ruleManagementDAOImpl.addRule(orCombinedRule, TENANT_ID);

        createdRule = ruleManagementDAOImpl.getRuleByRuleId(orCombinedRule.getId(), TENANT_ID);
        assertNotNull(createdRule);
        assertEquals(orCombinedRule.getId(), createdRule.getId());
        assertTrue(createdRule.isActive());

        ORCombinedRule retrievedORCombinedRule = assertOrCombinedRule(createdRule, 2);
        ANDCombinedRule retrievedAndCombinedRule1 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(0), 2);
        assertExpressions(retrievedAndCombinedRule1, expression1, expression2);
        ANDCombinedRule retrievedAndCombinedRule2 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(1), 1);
        assertExpressions(retrievedAndCombinedRule2, expression3);
    }

    @Test(dependsOnMethods = {"testAddRule"})
    public void testAddRuleWithMultipleExpressionsUsingSameFieldReferenceAndOR() throws RuleManagementException {

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();

        ANDCombinedRule andCombinedRule1 =
                new ANDCombinedRule.Builder().addExpression(expression1).addExpression(expression2).build();

        Expression expression3 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();

        Expression expression4 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "client_credentials")).build();

        ANDCombinedRule andCombinedRule2 =
                new ANDCombinedRule.Builder().addExpression(expression3).addExpression(expression4).build();

        Expression expression5 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp2")).build();
        ANDCombinedRule andCombinedRule3 =
                new ANDCombinedRule.Builder().addExpression(expression5).build();

        ORCombinedRule orCombinedRule =
                new ORCombinedRule.Builder().addRule(andCombinedRule1).addRule(andCombinedRule2)
                        .addRule(andCombinedRule3).build();

        ruleManagementDAOImpl.addRule(orCombinedRule, TENANT_ID);

        createdRule = ruleManagementDAOImpl.getRuleByRuleId(orCombinedRule.getId(), TENANT_ID);
        assertNotNull(createdRule);
        assertEquals(orCombinedRule.getId(), createdRule.getId());
        assertTrue(createdRule.isActive());

        ORCombinedRule retrievedORCombinedRule = assertOrCombinedRule(createdRule, 3);
        ANDCombinedRule retrievedAndCombinedRule1 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(0), 2);
        assertExpressions(retrievedAndCombinedRule1, expression1, expression2);
        ANDCombinedRule retrievedAndCombinedRule2 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(1), 2);
        assertExpressions(retrievedAndCombinedRule2, expression3, expression4);
        ANDCombinedRule retrievedAndCombinedRule3 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(2), 1);
        assertExpressions(retrievedAndCombinedRule3, expression5);
    }

    @Test(dependsOnMethods = {"testAddRuleWithMultipleExpressionsUsingSameFieldReferenceAndOR"})
    public void testUpdateRule() throws RuleManagementException {

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "password")).build();
        ANDCombinedRule andCombinedRule1 =
                new ANDCombinedRule.Builder().addExpression(expression1).addExpression(expression2).build();

        Expression expression3 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp2")).build();
        ANDCombinedRule andCombinedRule2 =
                new ANDCombinedRule.Builder().addExpression(expression3).build();

        Expression expression4 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp4")).build();
        ANDCombinedRule andCombinedRule3 =
                new ANDCombinedRule.Builder().addExpression(expression4).build();

        ORCombinedRule orCombinedRule =
                new ORCombinedRule.Builder().setId(createdRule.getId()).addRule(andCombinedRule1)
                        .addRule(andCombinedRule2)
                        .addRule(andCombinedRule3).build();

        ruleManagementDAOImpl.updateRule(orCombinedRule, TENANT_ID);

        Rule updatedRule = ruleManagementDAOImpl.getRuleByRuleId(createdRule.getId(), TENANT_ID);
        assertNotNull(updatedRule);
        assertEquals(createdRule.getId(), updatedRule.getId());
        assertTrue(updatedRule.isActive());

        ORCombinedRule retrievedORCombinedRule = assertOrCombinedRule(updatedRule, 3);
        ANDCombinedRule retrievedAndCombinedRule1 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(0), 2);
        assertExpressions(retrievedAndCombinedRule1, expression1, expression2);
        ANDCombinedRule retrievedAndCombinedRule2 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(1), 1);
        assertExpressions(retrievedAndCombinedRule2, expression3);
        ANDCombinedRule retrievedAndCombinedRule3 = assertAndCombinedRule(retrievedORCombinedRule.getRules().get(2), 1);
        assertExpressions(retrievedAndCombinedRule3, expression4);
    }

    @Test(dependsOnMethods = {"testUpdateRule"})
    public void testDeactivateRule() throws RuleManagementException {

        ruleManagementDAOImpl.deactivateRule(createdRule.getId(), TENANT_ID);
        Rule deactivatedRule = ruleManagementDAOImpl.getRuleByRuleId(createdRule.getId(), TENANT_ID);
        assertNotNull(deactivatedRule);
        assertEquals(deactivatedRule.getId(), createdRule.getId());
        assertFalse(deactivatedRule.isActive());
    }

    @Test(dependsOnMethods = {"testDeactivateRule"})
    public void testActivateRule() throws RuleManagementException {

        ruleManagementDAOImpl.activateRule(createdRule.getId(), TENANT_ID);
        Rule activatedRule = ruleManagementDAOImpl.getRuleByRuleId(createdRule.getId(), TENANT_ID);
        assertNotNull(activatedRule);
        assertEquals(activatedRule.getId(), createdRule.getId());
        assertTrue(activatedRule.isActive());
    }

    @Test(dependsOnMethods = {"testActivateRule"})
    public void testDeleteRule() throws RuleManagementException {

        ruleManagementDAOImpl.deleteRule(createdRule.getId(), TENANT_ID);
        Rule deletedRule = ruleManagementDAOImpl.getRuleByRuleId(createdRule.getId(), TENANT_ID);
        assertNull(deletedRule);
    }

    private static void assertExpressions(ANDCombinedRule andCombinedRule, Expression... expressions) {

        assertEquals(andCombinedRule.getExpressions().size(), expressions.length);
        for (int i = 0; i < expressions.length; i++) {
            assertEquals(andCombinedRule.getExpressions().get(i).getField(), expressions[i].getField());
            assertEquals(andCombinedRule.getExpressions().get(i).getOperator(), expressions[i].getOperator());
            assertEquals(andCombinedRule.getExpressions().get(i).getValue().getType(),
                    expressions[i].getValue().getType());
            assertEquals(andCombinedRule.getExpressions().get(i).getValue().getFieldValue(),
                    expressions[i].getValue().getFieldValue());
        }
    }

    private static ANDCombinedRule assertAndCombinedRule(Rule andRule, int expectedExpressionsSize) {

        assertTrue(andRule instanceof ANDCombinedRule);

        ANDCombinedRule andCombinedRule = (ANDCombinedRule) andRule;
        assertNotNull(andCombinedRule.getId());
        assertTrue(andCombinedRule.isActive());
        assertNotNull(andCombinedRule.getExpressions());
        assertEquals(andCombinedRule.getExpressions().size(), expectedExpressionsSize);
        return andCombinedRule;
    }

    private static ORCombinedRule assertOrCombinedRule(Rule rule, int expectedRulesSize) {

        assertTrue(rule instanceof ORCombinedRule);
        ORCombinedRule orCombinedRule = (ORCombinedRule) rule;
        assertNotNull(orCombinedRule.getRules());
        assertEquals(orCombinedRule.getRules().size(), expectedRulesSize);
        return orCombinedRule;
    }
}
