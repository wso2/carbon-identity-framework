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

package org.wso2.carbon.identity.rule.metadata.service;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.metadata.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.config.RuleMetadataConfigFactory;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.exception.RuleMetadataServerException;
import org.wso2.carbon.identity.rule.metadata.model.Field;
import org.wso2.carbon.identity.rule.metadata.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.model.Link;
import org.wso2.carbon.identity.rule.metadata.model.Operator;
import org.wso2.carbon.identity.rule.metadata.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.model.OptionsValue;
import org.wso2.carbon.identity.rule.metadata.model.Value;
import org.wso2.carbon.identity.rule.metadata.provider.RuleMetadataProvider;
import org.wso2.carbon.identity.rule.metadata.service.impl.RuleMetadataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class RuleMetadataManagerTest {

    @Mock
    private RuleMetadataProvider metadataProvider1;
    @Mock
    private RuleMetadataProvider metadataProvider2;
    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;

    private RuleMetadataManager ruleMetadataManager;

    @BeforeClass
    public void setUpClass() throws RuleMetadataConfigException {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                        "configs/valid-operators.json"))
                .getFile();
        OperatorConfig operatorConfig = OperatorConfig.load(new File(filePath));
        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);

        MockitoAnnotations.openMocks(this);
        ruleMetadataManager = RuleMetadataManager.getInstance();
        ruleMetadataManager.registerMetadataProvider(metadataProvider1);
        ruleMetadataManager.registerMetadataProvider(metadataProvider2);
    }

    @AfterClass
    public void tearDownClass() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test
    public void testGetExpressionMetaForFlowWhenNoDuplicatingFieldDefinitionsFoundAcrossProviders()
            throws RuleMetadataException {

        List<FieldDefinition> fieldDefinitionsForMetadataProvider1 = getMockedFieldDefinitionsForMetadataProvider1();
        when(metadataProvider1.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(fieldDefinitionsForMetadataProvider1);
        List<FieldDefinition> fieldDefinitionsForMetadataProvider2 = getMockedFieldDefinitionsForMetadataProvider2();
        when(metadataProvider2.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(fieldDefinitionsForMetadataProvider2);
        List<FieldDefinition> expectedFieldDefinitions = Stream.concat(fieldDefinitionsForMetadataProvider1.stream(),
                fieldDefinitionsForMetadataProvider2.stream()).collect(Collectors.toList());

        List<FieldDefinition> result =
                ruleMetadataManager.getExpressionMetaForFlow(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        assertNotNull(result);
        assertEquals(result.size(), expectedFieldDefinitions.size());
        assertEquals(result, expectedFieldDefinitions);
    }

    @Test(expectedExceptions = RuleMetadataServerException.class,
            expectedExceptionsMessageRegExp = "Duplicate field found.")
    public void testGetExpressionMetaForFlowWhenDuplicatingFieldDefinitionsFoundAcrossProviders()
            throws RuleMetadataException {

        List<FieldDefinition> fieldDefinitionsForMetadataProvider1 = getMockedFieldDefinitionsForMetadataProvider1();
        when(metadataProvider1.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(fieldDefinitionsForMetadataProvider1);
        List<FieldDefinition> fieldDefinitionsForMetadataProvider2 =
                getDuplicatedMockedFieldDefinitionsForMetadataProvider2();
        when(metadataProvider2.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1"))
                .thenReturn(fieldDefinitionsForMetadataProvider2);

        ruleMetadataManager.getExpressionMetaForFlow(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");
    }

    private List<FieldDefinition> getMockedFieldDefinitionsForMetadataProvider1() {

        List<FieldDefinition> fieldDefinitionList = new ArrayList<>();

        Field applicationField = new Field("application", "application");
        List<Operator> operators = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));

        List<Link> links = Arrays.asList(new Link("/applications?offset=0&limit=10", "GET", "values"),
                new Link("/applications?filter=name+eq+*&limit=10", "GET", "filter"));
        Value applicationValue = new OptionsReferenceValue.Builder().valueReferenceAttribute("id")
                .valueDisplayAttribute("name").valueType(Value.ValueType.STRING).links(links).build();
        fieldDefinitionList.add(new FieldDefinition(applicationField, operators, applicationValue));

        Field grantTypeField = new Field("grantType", "grantType");
        List<OptionsValue> optionsValues = Arrays.asList(new OptionsValue("authorization_code", "authorization code"),
                new OptionsValue("password", "password"), new OptionsValue("refresh_token", "refresh token"),
                new OptionsValue("client_credentials", "client credentials"),
                new OptionsValue("urn:ietf:params:oauth:grant-type:token-exchange", "token exchange"));
        Value grantTypeValue = new OptionsInputValue(Value.ValueType.STRING, optionsValues);
        fieldDefinitionList.add(new FieldDefinition(grantTypeField, operators, grantTypeValue));

        return fieldDefinitionList;
    }

    private List<FieldDefinition> getMockedFieldDefinitionsForMetadataProvider2() {

        Field roleField = new Field("role", "role");
        List<Operator> operators = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));
        Value roleValue = new InputValue(Value.ValueType.STRING);

        return Collections.singletonList(new FieldDefinition(roleField, operators, roleValue));
    }

    private List<FieldDefinition> getDuplicatedMockedFieldDefinitionsForMetadataProvider2() {

        Field applicationField = new Field("application", "application");
        List<Operator> operators = Arrays.asList(new Operator("equals", "equals"),
                new Operator("notEquals", "not equals"));
        Value applicationValue = new InputValue(Value.ValueType.STRING);

        return Collections.singletonList(new FieldDefinition(applicationField, operators, applicationValue));
    }
}
