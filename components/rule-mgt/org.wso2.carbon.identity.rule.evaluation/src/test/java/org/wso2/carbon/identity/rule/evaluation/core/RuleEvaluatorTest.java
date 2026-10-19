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

package org.wso2.carbon.identity.rule.evaluation.core;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.internal.component.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.OperatorRegistry;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluator;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FieldReference;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.api.util.RuleBuilder;
import org.wso2.carbon.identity.rule.management.internal.component.RuleManagementComponentServiceHolder;
import org.wso2.carbon.identity.rule.metadata.api.model.Field;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Link;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.api.service.RuleMetadataService;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class RuleEvaluatorTest {

    private RuleEvaluator ruleEvaluator;
    private OperatorRegistry operatorRegistry;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;

    @BeforeClass
    public void setUpClass() throws Exception {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                "configs/valid-operators.json")).getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(filePath));

        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);
        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();

        RuleMetadataService ruleMetadataService = mock(RuleMetadataService.class);
        when(ruleMetadataService.getApplicableOperatorsInExpressions()).thenReturn(
                new ArrayList<>(operatorConfig.getOperatorsMap().values()));
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);
        RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
        operatorRegistry = RuleEvaluationComponentServiceHolder.getInstance().getOperatorRegistry();

        RuleManagementComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
    }

    @BeforeMethod
    public void setUpMethod() {

        ruleEvaluator = new RuleEvaluator(operatorRegistry);
    }

    @AfterClass
    public void tearDownClass() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @DataProvider(name = "ruleEvaluationDataProvider")
    public Object[][] ruleEvaluationDataProvider() throws Exception {

        return new Object[][]{
                {createRuleWithTwoANDExpressionsUsingReferenceAndStringValueTypes(),
                        createEvaluationData("testApp", "client-credentials"), false},
                {createRuleWithTwoANDExpressionsUsingReferenceAndStringValueTypes(),
                        createEvaluationData("testapp", "authorization_code"), true},
                {createRuleWithTwoANDExpressionsUsingReferenceAndBooleanValueTypes(),
                        createEvaluationData("testapp", false), true},
                {createRuleWithTwoANDExpressionsUsingReferenceAndNumberValueTypes(),
                        createEvaluationData("testapp", 10), true},
                {createRuleWithTwoANDExpressionsAndOneORExpressionUsingReferenceAndNumberValueTypes(),
                        createEvaluationData("testapp1", 10), true},
                {createRuleWithTwoANDExpressionsAndOneORExpressionUsingReferenceAndNumberValueTypes(),
                        createEvaluationData("testapp3", 4), true},
                {createRuleWithANDExpressionUsingStringValueTypesAndContainsOperator(),
                        createEvaluationData("user@wso2.com"), true},
                {createRuleWithTwoANDExpressionsUsingListValueTypes(),
                        createEvaluationData(Arrays.asList("http://wso2.org/claims/givenname",
                                "http://wso2.org/claims/country")), true},
                {createRuleWithListValueTypeAndContainsOperator(),
                        createEvaluationDataForRoleList(Arrays.asList("role1", "role2", "role3")), true},
                {createRuleWithListValueTypeAndContainsOperator(),
                        createEvaluationDataForRoleList(Arrays.asList("role4", "role5")), false},
                {createRuleWithANDExpressionUsingStringValueTypesAndNotContainsOperator(),
                        createEvaluationData("user@example.com"), true},
                {createRuleWithANDExpressionUsingStringValueTypesAndNotContainsOperator(),
                        createEvaluationData("user@wso2.com"), false},
                {createRuleWithANDExpressionUsingStringValueTypesAndStartsWithOperator(),
                        createEvaluationData("user@wso2.com"), true},
                {createRuleWithANDExpressionUsingStringValueTypesAndStartsWithOperator(),
                        createEvaluationData("admin@wso2.com"), false},
                {createRuleWithANDExpressionUsingStringValueTypesAndEndsWithOperator(),
                        createEvaluationData("user@wso2.com"), true},
                {createRuleWithANDExpressionUsingStringValueTypesAndEndsWithOperator(),
                        createEvaluationData("user@example.com"), false},
                {createRuleWithANDExpressionUsingNumberValueTypesAndGreaterThanOperator(),
                        createEvaluationDataForRiskScore(80), true},
                {createRuleWithANDExpressionUsingNumberValueTypesAndGreaterThanOperator(),
                        createEvaluationDataForRiskScore(50), false},
                {createRuleWithANDExpressionUsingNumberValueTypesAndGreaterThanOperator(),
                        createEvaluationDataForRiskScore(30), false},
                {createRuleWithANDExpressionUsingNumberValueTypesAndLessThanOperator(),
                        createEvaluationDataForRiskScore(30), true},
                {createRuleWithANDExpressionUsingNumberValueTypesAndLessThanOperator(),
                        createEvaluationDataForRiskScore(50), false},
                {createRuleWithANDExpressionUsingNumberValueTypesAndLessThanOperator(),
                        createEvaluationDataForRiskScore(80), false}
        };
    }

    @Test(dataProvider = "ruleEvaluationDataProvider")
    public void testEvaluateRule(Rule rule, Map<String, FieldValue> evaluationData, boolean expectedResult) throws
            RuleEvaluationException {

        boolean result = ruleEvaluator.evaluate(rule, evaluationData).isRuleSatisfied();
        if (expectedResult) {
            assertTrue(result);
        } else {
            assertFalse(result);
        }
    }

    @Test(expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Field value not found for the field: application")
    public void testEvaluateFieldValueNotFound() throws Exception {

        ruleEvaluator.evaluate(createRuleWithTwoANDExpressionsUsingReferenceAndStringValueTypes(),
                Collections.emptyMap());
    }

    @Test
    public void testFailedFieldsPopulatedWhenRuleFails() throws Exception {

        RuleEvaluationResult result = ruleEvaluator.evaluate(
                createRuleWithTwoANDExpressionsUsingReferenceAndStringValueTypes(),
                createEvaluationData("testApp", "client-credentials"));

        assertFalse(result.isRuleSatisfied());
        // The rule fails on the first failing expression (application), so it is reported as a failed field.
        assertEquals(result.getFailedFields(), Collections.singletonList("application"));
    }

    @Test
    public void testFailedFieldsEmptyWhenRulePasses() throws Exception {

        RuleEvaluationResult result = ruleEvaluator.evaluate(
                createRuleWithTwoANDExpressionsUsingReferenceAndStringValueTypes(),
                createEvaluationData("testapp", "authorization_code"));

        assertTrue(result.isRuleSatisfied());
        assertTrue(result.getFailedFields().isEmpty());
    }

    private Rule createRuleWithTwoANDExpressionsUsingReferenceAndStringValueTypes() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ruleBuilder.addAndExpression(expression2);

        return ruleBuilder.build();
    }

    private Rule createRuleWithANDExpressionUsingStringValueTypesAndContainsOperator() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("email").operator("contains")
                .value(new Value(Value.Type.STRING, "wso2.com")).build();
        ruleBuilder.addAndExpression(expression1);

        return ruleBuilder.build();
    }

    private Rule createRuleWithTwoANDExpressionsUsingReferenceAndBooleanValueTypes() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("consented").operator("notEquals")
                .value(new Value(Value.Type.BOOLEAN, "true")).build();
        ruleBuilder.addAndExpression(expression2);

        return ruleBuilder.build();
    }

    private Rule createRuleWithTwoANDExpressionsUsingReferenceAndNumberValueTypes() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("riskScore").operator("equals")
                .value(new Value(Value.Type.NUMBER, "10")).build();
        ruleBuilder.addAndExpression(expression2);

        return ruleBuilder.build();
    }

    private Rule createRuleWithTwoANDExpressionsAndOneORExpressionUsingReferenceAndNumberValueTypes() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("riskScore").operator("equals")
                .value(new Value(Value.Type.NUMBER, "10")).build();
        ruleBuilder.addAndExpression(expression2);

        ruleBuilder.addOrCondition();

        Expression expression3 = new Expression.Builder().field("application").operator("notEquals")
                .value(new Value(Value.Type.REFERENCE, "testapp2")).build();
        ruleBuilder.addAndExpression(expression3);

        return ruleBuilder.build();
    }

    private Rule createRuleWithANDExpressionUsingStringValueTypesAndNotContainsOperator() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("email").operator("notContains")
                .value(new Value(Value.Type.STRING, "wso2.com")).build();
        ruleBuilder.addAndExpression(expression1);

        return ruleBuilder.build();
    }

    private Rule createRuleWithANDExpressionUsingStringValueTypesAndStartsWithOperator() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("email").operator("startsWith")
                .value(new Value(Value.Type.STRING, "user")).build();
        ruleBuilder.addAndExpression(expression1);

        return ruleBuilder.build();
    }

    private Rule createRuleWithANDExpressionUsingStringValueTypesAndEndsWithOperator() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("email").operator("endsWith")
                .value(new Value(Value.Type.STRING, "wso2.com")).build();
        ruleBuilder.addAndExpression(expression1);

        return ruleBuilder.build();
    }

    private Rule createRuleWithANDExpressionUsingNumberValueTypesAndGreaterThanOperator() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("riskScore").operator("greaterThan")
                .value(new Value(Value.Type.NUMBER, "50")).build();
        ruleBuilder.addAndExpression(expression1);

        return ruleBuilder.build();
    }

    private Rule createRuleWithANDExpressionUsingNumberValueTypesAndLessThanOperator() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("riskScore").operator("lessThan")
                .value(new Value(Value.Type.NUMBER, "50")).build();
        ruleBuilder.addAndExpression(expression1);

        return ruleBuilder.build();
    }

    private Rule createRuleWithListValueTypeAndContainsOperator() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("roles").operator("contains")
                .value(new Value(Value.Type.REFERENCE, "role1")).build();
        ruleBuilder.addAndExpression(expression);

        return ruleBuilder.build();
    }

    private Rule createRuleWithTwoANDExpressionsUsingListValueTypes() throws Exception {

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("claim").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "http://wso2.org/claims/country")).build();
        Expression expression2 = new Expression.Builder().field("claim").operator("notEquals")
                .value(new Value(Value.Type.REFERENCE, "http://wso2.org/claims/dob")).build();
        ruleBuilder.addAndExpression(expression1);
        ruleBuilder.addAndExpression(expression2);

        return ruleBuilder.build();
    }

    @Test
    public void testInHoldsWhenTheTwoSetsIntersect() throws Exception {

        Rule rule = ruleWithSingleExpression("claim", "in", new Value(Arrays.asList("b@x.com", "c@x.com")));

        assertTrue(ruleEvaluator.evaluate(rule, createEvaluationData(Arrays.asList("a@x.com", "b@x.com")))
                .isRuleSatisfied());
    }

    @Test
    public void testInDoesNotHoldWhenTheSetsAreDisjoint() throws Exception {

        Rule rule = ruleWithSingleExpression("claim", "in", new Value(Arrays.asList("z@x.com")));

        assertFalse(ruleEvaluator.evaluate(rule, createEvaluationData(Arrays.asList("a@x.com", "b@x.com")))
                .isRuleSatisfied());
    }

    /**
     * The trap membership exists to avoid. A multi-valued claim compared with contains matches
     * "b@x.com" against "ab@x.com", because one is a substring of the other. Membership does not.
     */
    @Test
    public void testInDistinguishesAValueFromASubstringOfAnother() throws Exception {

        Rule rule = ruleWithSingleExpression("claim", "in", new Value(Arrays.asList("b@x.com")));

        assertFalse(ruleEvaluator.evaluate(rule, createEvaluationData(Arrays.asList("a@x.com", "ab@x.com")))
                .isRuleSatisfied());
    }

    @Test
    public void testNotInHoldsOnlyWhenTheSetsAreDisjoint() throws Exception {

        Rule disjoint = ruleWithSingleExpression("claim", "notIn", new Value(Arrays.asList("z@x.com")));
        Rule overlapping = ruleWithSingleExpression("claim", "notIn", new Value(Arrays.asList("a@x.com")));
        Map<String, FieldValue> data = createEvaluationData(Arrays.asList("a@x.com", "b@x.com"));

        assertTrue(ruleEvaluator.evaluate(disjoint, data).isRuleSatisfied());
        assertFalse(ruleEvaluator.evaluate(overlapping, data).isRuleSatisfied());
    }

    /**
     * A right-hand side that is not a set cannot be intersected. Such a stored condition is an error, so notIn
     * cannot pass by negation and turn a malformed expression into a branch that always fires.
     */
    @Test
    public void testInAndNotInWithASingleValueAreErrors() {

        Rule inRule = ruleWithSingleExpression("claim", "in", new Value(Value.Type.STRING, "a@x.com"));
        Rule notInRule = ruleWithSingleExpression("claim", "notIn", new Value(Value.Type.STRING, "a@x.com"));
        Map<String, FieldValue> data = createEvaluationData(Arrays.asList("a@x.com"));

        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(inRule, data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(notInRule, data));
    }

    /**
     * A value that was never there must not satisfy a negated operator either, or a condition on
     * a claim the user never provided would take its branch instead of falling to the default.
     */
    @Test
    public void testInAndNotInDoNotHoldOnAMissingValue() throws Exception {

        Rule inRule = ruleWithSingleExpression("claim", "in", new Value(Arrays.asList("a@x.com")));
        Rule notInRule = ruleWithSingleExpression("claim", "notIn", new Value(Arrays.asList("a@x.com")));
        Map<String, FieldValue> data = createEvaluationData((List<String>) null);

        assertFalse(ruleEvaluator.evaluate(inRule, data).isRuleSatisfied());
        assertFalse(ruleEvaluator.evaluate(notInRule, data).isRuleSatisfied(),
                "notIn must not hold for a missing value.");
    }

    @Test
    public void testFieldComparisonHoldsWhenBothFieldsAgree() throws Exception {

        Map<String, FieldValue> data = twoFields("LK", "LK");

        assertTrue(ruleEvaluator.evaluate(comparedWithStored("equals"), data).isRuleSatisfied());
        assertFalse(ruleEvaluator.evaluate(comparedWithStored("notEquals"), data).isRuleSatisfied());
    }

    @Test
    public void testFieldComparisonNotEqualsHoldsWhenTheFieldsDiffer() throws Exception {

        Map<String, FieldValue> data = twoFields("LK", "IN");

        assertFalse(ruleEvaluator.evaluate(comparedWithStored("equals"), data).isRuleSatisfied());
        assertTrue(ruleEvaluator.evaluate(comparedWithStored("notEquals"), data).isRuleSatisfied());
    }

    /**
     * A field with no value is not known to differ from anything, so not even a negated operator holds.
     */
    @Test
    public void testFieldComparisonDoesNotHoldWhenEitherSideIsMissing() throws Exception {

        assertFalse(ruleEvaluator.evaluate(comparedWithStored("notEquals"), twoFields("LK", null))
                .isRuleSatisfied());
        assertFalse(ruleEvaluator.evaluate(comparedWithStored("notEquals"), twoFields(null, "LK"))
                .isRuleSatisfied());
    }

    @Test
    public void testFieldComparisonInIntersectsTwoSets() throws Exception {

        Rule rule = ruleWithSingleExpression("collected", "in",
                new Value(new FieldReference("stored", null)));
        Map<String, FieldValue> data = new HashMap<>();
        data.put("collected", new FieldValue("collected", Arrays.asList("a@x.com", "b@x.com")));
        data.put("stored", new FieldValue("stored", Arrays.asList("b@x.com", "c@x.com")));

        assertTrue(ruleEvaluator.evaluate(rule, data).isRuleSatisfied());
    }

    @Test
    public void testFieldComparisonInChecksASetAgainstAValue() throws Exception {

        Rule rule = ruleWithSingleExpression("collected", "in",
                new Value(new FieldReference("stored", null)));
        Map<String, FieldValue> data = new HashMap<>();
        data.put("collected", new FieldValue("collected", Arrays.asList("a@x.com", "b@x.com")));
        data.put("stored", new FieldValue("stored", "b@x.com", ValueType.STRING));

        assertTrue(ruleEvaluator.evaluate(rule, data).isRuleSatisfied());
    }

    /**
     * in and notIn compare the entries of a multi-valued field, so on a single-valued field, whether text, number or
     * boolean, they are an error rather than a condition that silently does not hold.
     */
    @Test
    public void testInAndNotInOnASingleValuedFieldAreErrors() {

        Map<String, FieldValue> data = twoFields("LK", "LK");
        data.put("riskScore", new FieldValue("riskScore", 3));
        data.put("consented", new FieldValue("consented", true));

        for (String operator : Arrays.asList("in", "notIn")) {
            expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                    ruleWithSingleExpression("collected", operator, new Value(Arrays.asList("LK", "IN"))), data));
            expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                    ruleWithSingleExpression("riskScore", operator, new Value(Arrays.asList("1", "3"))), data));
            expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                    ruleWithSingleExpression("consented", operator, new Value(Arrays.asList("true"))), data));
            expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                    comparedWithStored(operator), data));
        }
    }

    /**
     * A value type this node does not know is read as null. What its value means cannot be told, so evaluating it
     * is an error rather than a condition that silently does not hold.
     */
    @Test
    public void testValueOfAnUnknownTypeIsAnError() {

        Map<String, FieldValue> data = new HashMap<>();
        data.put("email", new FieldValue("email", "a@x.com", ValueType.STRING));

        for (String operator : Arrays.asList("equals", "notEquals")) {
            expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                    ruleWithSingleExpression("email", operator, new Value(null, "a@x.com")), data));
        }
    }

    @DataProvider(name = "valueLessOperators")
    public Object[][] valueLessOperators() {

        List<String> none = null;
        return new Object[][]{
                // value, isBlank, isNotBlank, isEmpty, isNotEmpty (null when the field is not multi-valued)
                {new FieldValue("v", (String) null, ValueType.STRING), true, false, null, null},
                {new FieldValue("v", "", ValueType.STRING), true, false, null, null},
                {new FieldValue("v", "  ", ValueType.STRING), true, false, null, null},
                {new FieldValue("v", "LK", ValueType.STRING), false, true, null, null},
                {new FieldValue("v", Collections.emptyList()), true, false, true, false},
                {new FieldValue("v", Arrays.asList("", " ")), true, false, false, true},
                {new FieldValue("v", Arrays.asList("a")), false, true, false, true},
                {new FieldValue("v", none), true, false, true, false},
                {new FieldValue("v", 5), false, true, null, null},
                {new FieldValue("v", true), false, true, null, null}
        };
    }

    @Test(dataProvider = "valueLessOperators")
    public void testValueLessOperatorsCheckTheFieldAlone(FieldValue value, boolean isBlank, boolean isNotBlank,
                                                        Boolean isEmpty, Boolean isNotEmpty) throws Exception {

        Map<String, FieldValue> data = new HashMap<>();
        data.put("v", value);

        assertEquals(evaluateWithoutValue("isBlank", data), isBlank, "isBlank");
        assertEquals(evaluateWithoutValue("isNotBlank", data), isNotBlank, "isNotBlank");
        if (isEmpty == null) {
            expectThrows(RuleEvaluationException.class, () -> evaluateWithoutValue("isEmpty", data));
            expectThrows(RuleEvaluationException.class, () -> evaluateWithoutValue("isNotEmpty", data));
            return;
        }
        assertEquals(evaluateWithoutValue("isEmpty", data), (boolean) isEmpty, "isEmpty");
        assertEquals(evaluateWithoutValue("isNotEmpty", data), (boolean) isNotEmpty, "isNotEmpty");
    }

    /**
     * A stored condition whose value does not match its operator is an error, whichever way round it is wrong.
     */
    @Test
    public void testValueThatDoesNotMatchItsOperatorIsAnError() {

        Map<String, FieldValue> data = new HashMap<>();
        data.put("v", new FieldValue("v", "LK", ValueType.STRING));

        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("v", "isBlank", new Value(Value.Type.STRING, "LK")), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("v", "equals", null), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("v", "between", new Value(Value.Type.STRING, "LK")), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("v", "equals", new Value(Arrays.asList("LK"))), data));
    }

    /**
     * Values a rule builder would have rejected are errors when found in a stored rule, never a quiet false.
     */
    @Test
    public void testMalformedStoredValuesAreErrors() {

        Map<String, FieldValue> data = new HashMap<>();
        data.put("riskScore", new FieldValue("riskScore", 5));
        data.put("consented", new FieldValue("consented", true));
        data.put("collected", new FieldValue("collected", "LK", ValueType.STRING));

        data.put("claim", new FieldValue("claim", Arrays.asList("a@x.com")));

        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("claim", "in", new Value(Arrays.asList("a@x.com", null))), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("claim", "startsWith", new Value(Value.Type.STRING, "a")), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("riskScore", "equals", new Value(Value.Type.NUMBER, "five")), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("consented", "equals", new Value(Value.Type.BOOLEAN, "yes")), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(
                ruleWithSingleExpression("collected", "equals", new Value(new FieldReference(null, null))), data));
        expectThrows(RuleEvaluationException.class, () -> ruleEvaluator.evaluate(comparedWithStored("equals"),
                data));
    }

    /**
     * A blank qualifier names no value within the field, so it reads the field's own value.
     */
    @Test
    public void testBlankQualifierReadsTheUnqualifiedValue() throws Exception {

        Map<String, FieldValue> data = new HashMap<>();
        data.put("email", new FieldValue("email", "a@x.com", ValueType.STRING));
        Rule rule = new ORCombinedRule.Builder().addRule(new ANDCombinedRule.Builder()
                .addExpression(new Expression.Builder().field("email").fieldQualifier(" ").operator("equals")
                        .value(new Value(Value.Type.STRING, "a@x.com")).build())
                .build()).build();

        assertTrue(ruleEvaluator.evaluate(rule, data).isRuleSatisfied());
    }

    private boolean evaluateWithoutValue(String operator, Map<String, FieldValue> data) throws Exception {

        Rule rule = new ORCombinedRule.Builder().addRule(new ANDCombinedRule.Builder()
                .addExpression(new Expression.Builder().field("v").operator(operator).build()).build()).build();
        return ruleEvaluator.evaluate(rule, data).isRuleSatisfied();
    }

    /**
     * A single value on the right counts as a set of one, so in / notIn check whether the field holds it.
     */
    @Test
    public void testFieldComparisonInTreatsASingleValueAsASetOfOne() throws Exception {

        Map<String, FieldValue> holds = new HashMap<>();
        holds.put("collected", new FieldValue("collected", Arrays.asList("LK", "IN")));
        holds.put("stored", new FieldValue("stored", "LK", ValueType.STRING));
        Map<String, FieldValue> lacks = new HashMap<>(holds);
        lacks.put("stored", new FieldValue("stored", "US", ValueType.STRING));

        assertTrue(ruleEvaluator.evaluate(comparedWithStored("in"), holds).isRuleSatisfied());
        assertFalse(ruleEvaluator.evaluate(comparedWithStored("notIn"), holds).isRuleSatisfied());
        assertFalse(ruleEvaluator.evaluate(comparedWithStored("in"), lacks).isRuleSatisfied());
        assertTrue(ruleEvaluator.evaluate(comparedWithStored("notIn"), lacks).isRuleSatisfied());
    }

    /**
     * equals and notEquals are not defined when either side holds several values, so neither holds; notEquals in
     * particular must not hold every time.
     */
    @Test
    public void testFieldComparisonWithASetOnEitherSideHoldsOnlyForMembership() throws Exception {

        Map<String, FieldValue> bothSets = new HashMap<>();
        bothSets.put("collected", new FieldValue("collected", Arrays.asList("a@x.com", "b@x.com")));
        bothSets.put("stored", new FieldValue("stored", Arrays.asList("c@x.com")));
        Map<String, FieldValue> setOnTheLeft = new HashMap<>();
        setOnTheLeft.put("collected", new FieldValue("collected", Arrays.asList("a@x.com")));
        setOnTheLeft.put("stored", new FieldValue("stored", "a@x.com", ValueType.STRING));

        for (Map<String, FieldValue> data : Arrays.asList(bothSets, setOnTheLeft)) {
            assertFalse(ruleEvaluator.evaluate(comparedWithStored("equals"), data).isRuleSatisfied());
            assertFalse(ruleEvaluator.evaluate(comparedWithStored("notEquals"), data).isRuleSatisfied());
        }
        assertTrue(ruleEvaluator.evaluate(comparedWithStored("notIn"), bothSets).isRuleSatisfied());
        assertTrue(ruleEvaluator.evaluate(comparedWithStored("in"), setOnTheLeft).isRuleSatisfied());
    }

    /**
     * A missing value on either side holds for no operator, and is not an error.
     */
    @Test
    public void testMissingValueOnEitherSideDoesNotHold() throws Exception {

        Map<String, FieldValue> noFieldValue = new HashMap<>();
        noFieldValue.put("email", new FieldValue("email", (String) null, ValueType.STRING));
        Map<String, FieldValue> riskScore = new HashMap<>();
        riskScore.put("riskScore", new FieldValue("riskScore", 3));

        for (String operator : Arrays.asList("equals", "notEquals")) {
            assertFalse(ruleEvaluator.evaluate(ruleWithSingleExpression("email", operator,
                    new Value(Value.Type.STRING, "a@x.com")), noFieldValue).isRuleSatisfied());
            assertFalse(ruleEvaluator.evaluate(ruleWithSingleExpression("riskScore", operator,
                    new Value(Value.Type.NUMBER, null)), riskScore).isRuleSatisfied());
        }
    }

    private Rule comparedWithStored(String operator) {

        return ruleWithSingleExpression("collected", operator, new Value(new FieldReference("stored", null)));
    }

    private Map<String, FieldValue> twoFields(String collected, String stored) {

        Map<String, FieldValue> data = new HashMap<>();
        data.put("collected", new FieldValue("collected", collected, ValueType.STRING));
        data.put("stored", new FieldValue("stored", stored, ValueType.STRING));
        return data;
    }

    private Rule ruleWithSingleExpression(String field, String operator, Value value) {

        return new ORCombinedRule.Builder()
                .addRule(new ANDCombinedRule.Builder()
                        .addExpression(new Expression.Builder()
                                .field(field).operator(operator).value(value).build())
                        .build())
                .build();
    }

    private Map<String, FieldValue> createEvaluationData(String applicationValue, String grantTypeValue) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put("application", new FieldValue("application", applicationValue, ValueType.REFERENCE));
        evaluationData.put("grantType", new FieldValue("grantType", grantTypeValue, ValueType.STRING));
        return evaluationData;
    }

    private Map<String, FieldValue> createEvaluationData(String applicationValue, boolean consentedValue) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put("application", new FieldValue("application", applicationValue, ValueType.REFERENCE));
        evaluationData.put("consented", new FieldValue("consented", consentedValue));
        return evaluationData;
    }

    private Map<String, FieldValue> createEvaluationData(String applicationValue, int riskScoreValue) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put("application", new FieldValue("application", applicationValue, ValueType.REFERENCE));
        evaluationData.put("riskScore", new FieldValue("riskScore", riskScoreValue));
        return evaluationData;
    }

    private Map<String, FieldValue> createEvaluationData(String emailValue) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put("email", new FieldValue("application", emailValue, ValueType.STRING));
        return evaluationData;
    }

    private Map<String, FieldValue> createEvaluationData(List<String> claims) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put("claim", new FieldValue("claim", claims));
        return evaluationData;
    }

    private Map<String, FieldValue> createEvaluationDataForRiskScore(int riskScoreValue) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put("riskScore", new FieldValue("riskScore", riskScoreValue));
        return evaluationData;
    }

    private Map<String, FieldValue> createEvaluationDataForRoleList(List<String> roles) {

        Map<String, FieldValue> evaluationData = new HashMap<>();
        evaluationData.put("roles", new FieldValue("roles", roles));
        return evaluationData;
    }

    private List<FieldDefinition> getMockedFieldDefinitions() {

        List<FieldDefinition> fieldDefinitionList = new ArrayList<>();

        Field applicationField = new Field("application", "application");
        List<Operator> operators = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));
        List<Link> links = Arrays.asList(new Link("/applications?offset=0&limit=10", "GET", "values"),
                new Link("/applications?filter=name+eq+*&limit=10", "GET", "filter"));
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                applicationValue = new OptionsReferenceValue.Builder().valueReferenceAttribute("id")
                .valueDisplayAttribute("name").valueType(
                        org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.REFERENCE)
                .links(links).build();
        fieldDefinitionList.add(new FieldDefinition(applicationField, operators, applicationValue));

        Field grantTypeField = new Field("grantType", "grantType");
        List<OptionsValue> optionsValues = Arrays.asList(new OptionsValue("authorization_code", "authorization code"),
                new OptionsValue("password", "password"), new OptionsValue("refresh_token", "refresh token"),
                new OptionsValue("client_credentials", "client credentials"),
                new OptionsValue("urn:ietf:params:oauth:grant-type:token-exchange", "token exchange"));
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                grantTypeValue =
                new OptionsInputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.STRING,
                        optionsValues);
        fieldDefinitionList.add(new FieldDefinition(grantTypeField, operators, grantTypeValue));

        Field consentedField = new Field("consented", "consented");
        org.wso2.carbon.identity.rule.metadata.api.model.Value consentedValue =
                new InputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.BOOLEAN);
        fieldDefinitionList.add(new FieldDefinition(consentedField, operators, consentedValue));

        Field riskScoreField = new Field("riskScore", "risk score");
        List<Operator> operatorsForRiskScore = Arrays.asList(
                new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"),
                new Operator("greaterThan", "greater than"),
                new Operator("lessThan", "less than"));
        org.wso2.carbon.identity.rule.metadata.api.model.Value riskScoreValue =
                new InputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.NUMBER);
        fieldDefinitionList.add(new FieldDefinition(riskScoreField, operatorsForRiskScore, riskScoreValue));

        Field emailField = new Field("email", "user.email");
        List<Operator> operatorsForEmail = Arrays.asList(
                new Operator("contains", "contains"),
                new Operator("notContains", "not contains"),
                new Operator("startsWith", "starts with"),
                new Operator("endsWith", "ends with"));
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                emailValue = new InputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.STRING);
        fieldDefinitionList.add(new FieldDefinition(emailField, operatorsForEmail, emailValue));

        Field claimField = new Field("claim", "claim");
        List<Operator> operatorsForClaim = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));
        List<Link> linksForClaim = Arrays.asList(new Link("/claim-dialects/local/claims?exclude-hidden-claims=true",
                "GET", "values"));
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                claimValue = new OptionsReferenceValue.Builder().valueReferenceAttribute("uri")
                .valueDisplayAttribute("name").valueType(
                        org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.REFERENCE)
                .links(linksForClaim).build();
        fieldDefinitionList.add(new FieldDefinition(claimField, operatorsForClaim, claimValue));

        Field rolesField = new Field("roles", "user.roles");
        List<Operator> operatorsForRoles = Collections.singletonList(new Operator("contains", "contains"));
        List<Link> linksForRoles = Collections.singletonList(
                new Link("/roles?offset=0&limit=10", "GET", "values"));
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                rolesValue = new OptionsReferenceValue.Builder().valueReferenceAttribute("id")
                .valueDisplayAttribute("name").valueType(
                        org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.REFERENCE)
                .links(linksForRoles).build();
        fieldDefinitionList.add(new FieldDefinition(rolesField, operatorsForRoles, rolesValue));

        return fieldDefinitionList;
    }
}
