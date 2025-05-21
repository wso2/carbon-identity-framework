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
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.internal.component.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.OperatorRegistry;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluator;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.FlowType;
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
                                "http://wso2.org/claims/country")), true}
        };
    }

    @Test(dataProvider = "ruleEvaluationDataProvider")
    public void testEvaluateRule(Rule rule, Map<String, FieldValue> evaluationData, boolean expectedResult) throws
            RuleEvaluationException {

        boolean result = ruleEvaluator.evaluate(rule, evaluationData);
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
        org.wso2.carbon.identity.rule.metadata.api.model.Value riskScoreValue =
                new InputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.NUMBER);
        fieldDefinitionList.add(new FieldDefinition(riskScoreField, operators, riskScoreValue));

        Field emailField = new Field("email", "user.email");
        org.wso2.carbon.identity.rule.metadata.api.model.Value
                emailValue = new InputValue(org.wso2.carbon.identity.rule.metadata.api.model.Value.ValueType.STRING);
        fieldDefinitionList.add(
                new FieldDefinition(emailField, Collections.singletonList(new Operator("contains", "contains")),
                        emailValue));

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

        return fieldDefinitionList;
    }
}
