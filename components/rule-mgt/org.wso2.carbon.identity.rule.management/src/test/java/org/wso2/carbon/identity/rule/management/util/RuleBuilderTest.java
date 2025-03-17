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

package org.wso2.carbon.identity.rule.management.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementClientException;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementServerException;
import org.wso2.carbon.identity.rule.management.api.model.ANDCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
import org.wso2.carbon.identity.rule.management.api.model.ORCombinedRule;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.api.util.RuleBuilder;
import org.wso2.carbon.identity.rule.management.internal.component.RuleManagementComponentServiceHolder;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
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
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RuleBuilderTest {

    @Mock
    RuleMetadataService ruleMetadataService;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;
    OperatorConfig operatorConfig;

    @BeforeClass
    public void setUpClass() throws Exception {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                "configs/valid-operators.json")).getFile();
        operatorConfig = OperatorConfig.load(new File(filePath));
    }

    @BeforeMethod
    public void setUpMethod() {

        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);

        MockitoAnnotations.openMocks(this);
        RuleManagementComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
    }

    @AfterMethod
    public void tearDownMethod() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Expression metadata from RuleMetadataService is null or empty.")
    public void testCreateRuleBuilderWhenExpressionMetaReturnedFromMetadataServiceIsNull()
            throws Exception {

        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(null);

        RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Expression metadata from RuleMetadataService is null or empty.")
    public void testCreateRuleBuilderWhenExpressionMetaReturnedFromMetadataServiceIsEmpty()
            throws Exception {

        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(Collections.emptyList());

        RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");
    }

    @Test(expectedExceptions = RuleManagementServerException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving expression metadata from RuleMetadataService.")
    public void testCreateRuleBuilderWhenMetadataServiceThrowsException()
            throws Exception {

        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenThrow(new RuleMetadataException("RULEMETA-60005", "Error while retrieving expression metadata.",
                        "Failed to load data from configurations."));

        RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");
    }

    @Test
    public void testCreateRuleBuilder() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");
        assertNotNull(ruleBuilder);
    }

    @Test
    public void testCreateRuleWithValidExpressions() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ruleBuilder.addAndExpression(expression2);
        Rule rule = ruleBuilder.build();

        assertNotNull(rule);
        assertNotNull(rule.getId());
        assertTrue(rule.isActive());

        ORCombinedRule orCombinedRule = assertOrCombinedRule(rule, 1);

        orCombinedRule.getRules()
                .forEach(andRule -> assertExpressions(assertAndCombinedRule(andRule, 2), expression1, expression2));
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: Field invalid is not supported")
    public void testCreateRuleWithAnExpressionWithInvalidField() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("invalid").operator("equals")
                .value(new Value(Value.Type.STRING, "value")).build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: " +
                    "Operator invalid is not supported for field application")
    public void testCreateRuleWithAnExpressionWithInvalidOperator() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("application").operator("invalid")
                .value(new Value(Value.Type.STRING, "value")).build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: " +
                    "Value invalid is not supported for field grantType")
    public void testCreateRuleWithAnExpressionWithInvalidOptionsValue() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("grantType").operator("equals").value("invalid").build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: " +
                    "Value invalid is not a valid NUMBER.")
    public void testCreateRuleWithAnExpressionWithInvalidNumberValue() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression =
                new Expression.Builder().field("riskFactor").operator("equals").value("invalid").build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: " +
                    "Value invalid is not a valid BOOLEAN.")
    public void testCreateRuleWithAnExpressionWithInvalidBooleanValue() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("status").operator("equals").value("invalid").build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: " +
                    "Value type STRING is not supported for field application")
    public void testCreateRuleWithAnExpressionWithInvalidValueType() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.STRING, "testapp")).build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Field must be provided.")
    public void testCreateRuleWithAnExpressionWithoutRequiredField() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().operator("equals").value("invalid").build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Operator must be provided.")
    public void testCreateRuleWithAnExpressionWithoutRequiredOperator() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("application").value("invalid").build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Either primitive value or Value with type must be provided.")
    public void testCreateRuleWithAnExpressionWithoutRequiredValue() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression = new Expression.Builder().field("application").operator("equals").build();
        ruleBuilder.addAndExpression(expression);

        ruleBuilder.build();
    }

    /**
     * This test case is to test the scenario where multiple validation failures occur when building a rule.
     * In this case, only the very first validation failure is expected to be thrown.
     *
     * @throws Exception Client exception when building the rule
     */
    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: Field invalid is not supported")
    public void testCreateRuleWithMultipleValidationFailures() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        // Expression with invalid field
        Expression expression1 = new Expression.Builder().field("invalid").operator("equals")
                .value(new Value(Value.Type.STRING, "value")).build();
        ruleBuilder.addAndExpression(expression1);

        // Expression with invalid operator
        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "invalid")).build();
        ruleBuilder.addAndExpression(expression2);

        ruleBuilder.addOrCondition();

        // Expression with invalid value type
        Expression expression3 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.STRING, "testapp")).build();
        ruleBuilder.addAndExpression(expression3);

        ruleBuilder.build();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: " +
                    "Maximum number of expressions combined with AND exceeded. Maximum allowed: 5 Provided: 6")
    public void testCreateRuleWithMaxAllowedExpressionsCombinedWithAND() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        for (int i = 0; i < 6; i++) {
            Expression expression = new Expression.Builder().field("application").operator("equals")
                    .value(new Value(Value.Type.REFERENCE, "testapp" + i)).build();
            ruleBuilder.addAndExpression(expression);
        }

        ruleBuilder.build();
    }

    @Test(expectedExceptions = RuleManagementClientException.class,
            expectedExceptionsMessageRegExp = "Rule validation failed: " +
                    "Maximum number of rules combined with OR exceeded. Maximum allowed: 10 Provided: 11")
    public void testCreateRuleWithMaxAllowedRulesCombinedWithOR() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        for (int i = 0; i < 11; i++) {
            Expression expression = new Expression.Builder().field("application").operator("equals")
                    .value(new Value(Value.Type.REFERENCE, "testapp" + i)).build();
            ruleBuilder.addAndExpression(expression);
            ruleBuilder.addOrCondition();
        }

        ruleBuilder.build();
    }

    @Test
    public void testCreateRuleWithValidExpressionsAndOR() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ruleBuilder.addAndExpression(expression2);

        ruleBuilder.addOrCondition();

        Expression expression3 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp2")).build();
        ruleBuilder.addAndExpression(expression3);

        Rule rule = ruleBuilder.build();

        assertNotNull(rule);
        assertNotNull(rule.getId());
        assertTrue(rule.isActive());

        ORCombinedRule orCombinedRule = assertOrCombinedRule(rule, 2);
        ANDCombinedRule andCombinedRule = assertAndCombinedRule(orCombinedRule.getRules().get(0), 2);
        assertExpressions(andCombinedRule, expression1, expression2);

        andCombinedRule = assertAndCombinedRule(orCombinedRule.getRules().get(1), 1);
        assertExpressions(andCombinedRule, expression3);
    }

    @Test
    public void testCreateRuleWithMultipleExpressionsUsingSameFieldReferenceAndOR() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);

        RuleBuilder ruleBuilder = RuleBuilder.create(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ruleBuilder.addAndExpression(expression2);

        ruleBuilder.addOrCondition();

        Expression expression3 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp1")).build();
        ruleBuilder.addAndExpression(expression3);

        Expression expression4 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "client_credentials")).build();
        ruleBuilder.addAndExpression(expression4);

        ruleBuilder.addOrCondition();

        Expression expression5 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp2")).build();
        ruleBuilder.addAndExpression(expression5);

        Rule rule = ruleBuilder.build();

        assertNotNull(rule);
        assertNotNull(rule.getId());
        assertTrue(rule.isActive());

        ORCombinedRule orCombinedRule = assertOrCombinedRule(rule, 3);
        ANDCombinedRule andCombinedRule = assertAndCombinedRule(orCombinedRule.getRules().get(0), 2);
        assertExpressions(andCombinedRule, expression1, expression2);

        andCombinedRule = assertAndCombinedRule(orCombinedRule.getRules().get(1), 2);
        assertExpressions(andCombinedRule, expression3, expression4);

        andCombinedRule = assertAndCombinedRule(orCombinedRule.getRules().get(2), 1);
        assertExpressions(andCombinedRule, expression5);
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

        Field riskFactorField = new Field("riskFactor", "riskFactor");
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                riskFactorValue =
                new InputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.NUMBER);
        fieldDefinitionList.add(new FieldDefinition(riskFactorField, operators, riskFactorValue));

        Field statusField = new Field("status", "status");
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                statusValue =
                new InputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.BOOLEAN);
        fieldDefinitionList.add(new FieldDefinition(statusField, operators, statusValue));

        return fieldDefinitionList;
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
