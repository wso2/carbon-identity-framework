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

package org.wso2.carbon.identity.rule.evaluation.service.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.identity.rule.evaluation.internal.component.RuleEvaluationComponentServiceHolder;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluationDataManager;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluationServiceImpl;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.api.model.Value;
import org.wso2.carbon.identity.rule.management.api.service.RuleManagementService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RuleEvaluationServiceImplTest {

    private OperatorConfig operatorConfig;
    private RuleManagementService ruleManagementService;
    private RuleMetadataService ruleMetadataService;
    private RuleEvaluationDataManager ruleEvaluationDataManager;
    private RuleEvaluationDataProvider ruleEvaluationDataProvider;
    private RuleEvaluationServiceImpl ruleEvaluationService;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;

    @BeforeClass
    public void setUpClass() throws Exception {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                "configs/valid-operators.json")).getFile();
        operatorConfig = OperatorConfig.load(new File(filePath));

        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {

        List<FieldDefinition> mockedFieldDefinitions = getMockedFieldDefinitions();

        ruleMetadataService = mock(RuleMetadataService.class);
        when(ruleMetadataService.getApplicableOperatorsInExpressions()).thenReturn(
                new ArrayList<>(operatorConfig.getOperatorsMap().values()));
        when(ruleMetadataService.getExpressionMeta(
                org.wso2.carbon.identity.rule.metadata.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(mockedFieldDefinitions);
        RuleEvaluationComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);
        RuleManagementComponentServiceHolder.getInstance().setRuleMetadataService(ruleMetadataService);

        ruleManagementService = mock(RuleManagementService.class);
        RuleEvaluationComponentServiceHolder.getInstance().setRuleManagementService(ruleManagementService);

        ruleEvaluationDataManager = RuleEvaluationDataManager.getInstance();
        ruleEvaluationDataProvider = mock(RuleEvaluationDataProvider.class);
        when(ruleEvaluationDataProvider.getSupportedFlowType()).thenReturn(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        when(ruleEvaluationDataProvider.getEvaluationData(any(), any(), any())).thenReturn(getMockedFieldValues());
        ruleEvaluationDataManager.registerRuleEvaluationDataProvider(ruleEvaluationDataProvider);

        ruleEvaluationService = new RuleEvaluationServiceImpl();
    }

    @AfterClass
    public void tearDownClass() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test
    public void testEvaluateRuleSuccess() throws Exception {

        String tenantDomain = "tenant1";
        Rule rule = createRule(tenantDomain);
        String ruleId = rule.getId();
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(rule);

        RuleEvaluationResult result = ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);

        assertNotNull(result);
        assertEquals(result.getRuleId(), ruleId);
        assertTrue(result.isRuleSatisfied());
    }

    @Test
    public void testEvaluateInactiveRule() throws Exception {

        String tenantDomain = "tenant1";
        String ruleId = "rule1";

        Rule mockRule = mock(Rule.class);
        when(mockRule.getId()).thenReturn(ruleId);
        when(mockRule.isActive()).thenReturn(false);
        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(mockRule);

        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        RuleEvaluationResult result = ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);

        assertNotNull(result);
        assertEquals(result.getRuleId(), ruleId);
        assertFalse(result.isRuleSatisfied());
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Rule not found for the given ruleId: rule1")
    public void testFailureWhenRuleNotFound() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(null);
        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving the Rule.")
    public void testFailureWithRuleManagementExceptionWhenRetrievingRule() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenThrow(
                new RuleManagementException("Error"));

        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Expression metadata from RuleMetadataService is null or empty.")
    public void testFailureForNullOrEmptyMetadata() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(ruleId);
        when(rule.isActive()).thenReturn(true);
        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(rule);

        when(ruleMetadataService.getExpressionMeta(any(), any())).thenReturn(null);

        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    @Test(dependsOnMethods = "testEvaluateRuleSuccess",
            expectedExceptions = RuleEvaluationException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving expression metadata from RuleMetadataService.")
    public void testFailureWithRuleMetadataExceptionWhenRetrievingMetadata() throws Exception {

        String ruleId = "rule1";
        String tenantDomain = "tenant1";
        FlowContext flowContext = new FlowContext(FlowType.PRE_ISSUE_ACCESS_TOKEN, Collections.emptyMap());

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(ruleId);
        when(rule.isActive()).thenReturn(true);

        when(ruleManagementService.getRuleByRuleId(ruleId, tenantDomain)).thenReturn(rule);
        when(ruleMetadataService.getExpressionMeta(any(), any())).thenThrow(
                new RuleMetadataException("Error", "ErrorMessage", "ErrorDescription"));

        ruleEvaluationService.evaluate(ruleId, flowContext, tenantDomain);
    }

    private Rule createRule(String tenantDomain) throws Exception {

        RuleBuilder ruleBuilder =
                RuleBuilder.create(org.wso2.carbon.identity.rule.management.api.model.FlowType.PRE_ISSUE_ACCESS_TOKEN,
                        tenantDomain);

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new Value(Value.Type.REFERENCE, "testapp")).build();
        ruleBuilder.addAndExpression(expression1);

        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new Value(Value.Type.STRING, "authorization_code")).build();
        ruleBuilder.addAndExpression(expression2);

        return ruleBuilder.build();
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

        return fieldDefinitionList;
    }

    private List<FieldValue> getMockedFieldValues() {

        List<FieldValue> fieldValues = new ArrayList<>();
        fieldValues.add(new FieldValue("application", "testapp", ValueType.REFERENCE));
        fieldValues.add(new FieldValue("grantType", "authorization_code", ValueType.STRING));
        return fieldValues;
    }
}
