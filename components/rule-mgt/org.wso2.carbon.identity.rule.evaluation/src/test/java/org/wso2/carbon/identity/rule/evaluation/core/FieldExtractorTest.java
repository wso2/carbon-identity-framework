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
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.FieldExtractor;
import org.wso2.carbon.identity.rule.management.api.model.Expression;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Link;
import org.wso2.carbon.identity.rule.metadata.api.model.Operator;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class FieldExtractorTest {

    private FieldExtractor fieldExtractor;
    private Rule mockRule;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;

    @BeforeClass
    public void setUpClass() throws Exception {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                "configs/valid-operators.json")).getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(filePath));
        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);

        mockRule = mock(Rule.class);
        when(mockRule.getExpressions()).thenReturn(getMockedExpressions());
    }

    @AfterClass
    public void tearDownClass() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test
    public void testExtractFields() throws RuleEvaluationException {

        fieldExtractor = new FieldExtractor(getMockedFieldDefinitions());

        List<Field> fields = fieldExtractor.extractFields(mockRule);
        assertNotNull(fields);
        assertEquals(fields.size(), 4);
        assertEquals(fields.get(0).getName(), "application");
        assertTrue(fields.get(0).getValueType() instanceof ValueType.ReferenceValueType);
        assertEquals(((ValueType.ReferenceValueType) fields.get(0).getValueType()).getReferenceAttribute(), "id");
        assertEquals(fields.get(1).getName(), "grantType");
        assertEquals(fields.get(1).getValueType(), ValueType.STRING);
        assertEquals(fields.get(2).getName(), "consented");
        assertEquals(fields.get(2).getValueType(), ValueType.BOOLEAN);
        assertEquals(fields.get(3).getName(), "riskScore");
        assertEquals(fields.get(3).getValueType(), ValueType.NUMBER);
    }

    @Test(expectedExceptions = RuleEvaluationException.class, expectedExceptionsMessageRegExp =
            "Field definition not found for the field: application")
    public void testExtractFieldsFieldDefinitionNotFound() throws RuleEvaluationException {

        // Initialize FieldExtractor with empty field definitions
        fieldExtractor = new FieldExtractor(new ArrayList<>());

        // Extract fields (should throw exception)
        fieldExtractor.extractFields(mockRule);
    }

    private List<FieldDefinition> getMockedFieldDefinitions() {

        List<FieldDefinition> fieldDefinitionList = new ArrayList<>();

        org.wso2.carbon.identity.rule.metadata.api.model.Field applicationField =
                new org.wso2.carbon.identity.rule.metadata.api.model.Field("application", "application");
        List<Operator> operators = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));
        List<Link> links = Arrays.asList(new Link("/applications?offset=0&limit=10", "GET", "values"),
                new Link("/applications?filter=name+eq+*&limit=10", "GET", "filter"));
        Value applicationValue = new OptionsReferenceValue.Builder().valueReferenceAttribute("id")
                .valueDisplayAttribute("name").valueType(Value.ValueType.REFERENCE).links(links).build();
        fieldDefinitionList.add(new FieldDefinition(applicationField, operators, applicationValue));

        org.wso2.carbon.identity.rule.metadata.api.model.Field
                grantTypeField = new org.wso2.carbon.identity.rule.metadata.api.model.Field("grantType", "grantType");
        List<OptionsValue> optionsValues = Arrays.asList(new OptionsValue("authorization_code", "authorization code"),
                new OptionsValue("password", "password"), new OptionsValue("refresh_token", "refresh token"),
                new OptionsValue("client_credentials", "client credentials"),
                new OptionsValue("urn:ietf:params:oauth:grant-type:token-exchange", "token exchange"));
        Value grantTypeValue = new OptionsInputValue(Value.ValueType.STRING, optionsValues);
        fieldDefinitionList.add(new FieldDefinition(grantTypeField, operators, grantTypeValue));

        org.wso2.carbon.identity.rule.metadata.api.model.Field
                consentedField = new org.wso2.carbon.identity.rule.metadata.api.model.Field("consented", "consented");
        Value consentedValue = new InputValue(Value.ValueType.BOOLEAN);
        fieldDefinitionList.add(new FieldDefinition(consentedField, operators, consentedValue));

        org.wso2.carbon.identity.rule.metadata.api.model.Field
                riskScoreField = new org.wso2.carbon.identity.rule.metadata.api.model.Field("riskScore", "risk score");
        Value riskScoreValue = new InputValue(Value.ValueType.NUMBER);
        fieldDefinitionList.add(new FieldDefinition(riskScoreField, operators, riskScoreValue));

        return fieldDefinitionList;
    }

    List<Expression> getMockedExpressions() {

        Expression expression1 = new Expression.Builder().field("application").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.REFERENCE, "testapp")).build();
        Expression expression2 = new Expression.Builder().field("grantType").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.STRING, "authorization_code"))
                .build();
        Expression expression3 = new Expression.Builder().field("consented").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.BOOLEAN, "true")).build();
        Expression expression4 = new Expression.Builder().field("riskScore").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.NUMBER, "5")).build();

        Expression expression5 = new Expression.Builder().field("application").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.REFERENCE, "testapp2")).build();
        Expression expression6 = new Expression.Builder().field("grantType").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.STRING, "password"))
                .build();
        Expression expression7 = new Expression.Builder().field("consented").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.BOOLEAN, "false")).build();
        Expression expression8 = new Expression.Builder().field("riskScore").operator("equals")
                .value(new org.wso2.carbon.identity.rule.management.api.model.Value(
                        org.wso2.carbon.identity.rule.management.api.model.Value.Type.NUMBER, "4")).build();
        return Arrays.asList(expression1, expression2, expression3, expression4, expression5, expression6, expression7,
                expression8);
    }
}
