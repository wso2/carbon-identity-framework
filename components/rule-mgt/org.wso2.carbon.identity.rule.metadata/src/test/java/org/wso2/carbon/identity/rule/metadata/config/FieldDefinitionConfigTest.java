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

package org.wso2.carbon.identity.rule.metadata.config;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.InputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.internal.config.FieldDefinitionConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.OperatorConfig;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class FieldDefinitionConfigTest {

    private OperatorConfig operatorConfig;

    private MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic;

    @BeforeClass
    public void setUpClass() throws RuleMetadataConfigException {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(
                        "configs/valid-operators.json"))
                .getFile();
        operatorConfig = OperatorConfig.load(new File(filePath));
        ruleMetadataConfigFactoryMockedStatic = mockStatic(RuleMetadataConfigFactory.class);
        ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::getOperatorConfig)
                .thenReturn(operatorConfig);
    }

    @AfterClass
    public void tearDownClass() {

        ruleMetadataConfigFactoryMockedStatic.close();
    }

    @Test
    public void testLoadFieldDefinitionsFromValidConfig() throws RuleMetadataConfigException {

        String filePath = Objects.requireNonNull(getClass().getClassLoader().
                getResource("configs/valid-fields.json")).getFile();

        FieldDefinitionConfig fieldDefinitionConfig = FieldDefinitionConfig.load(new File(filePath), operatorConfig);

        Map<String, FieldDefinition> result = fieldDefinitionConfig.getFieldDefinitionMap();

        assertNotNull(result);
        assertEquals(result.size(), 3);

        verifyApplicationField(result);
        verifyGrantTypeField(result);
        verifyEmailField(result);
    }

    private void verifyApplicationField(Map<String, FieldDefinition> result) {

        FieldDefinition applicationFieldDefinition = result.get("application");
        assertNotNull(applicationFieldDefinition);
        assertEquals(applicationFieldDefinition.getField().getName(), "application");
        assertEquals(applicationFieldDefinition.getField().getDisplayName(), "application");

        assertEquals(applicationFieldDefinition.getOperators().size(), 2);
        assertEquals(applicationFieldDefinition.getOperators().get(0).getName(), "equals");
        assertEquals(applicationFieldDefinition.getOperators().get(0).getDisplayName(), "equals");
        assertEquals(applicationFieldDefinition.getOperators().get(1).getName(), "notEquals");
        assertEquals(applicationFieldDefinition.getOperators().get(1).getDisplayName(), "not equals");

        assertTrue(applicationFieldDefinition.getValue() instanceof OptionsReferenceValue);
        OptionsReferenceValue applicationFieldValue = (OptionsReferenceValue) applicationFieldDefinition.getValue();
        assertEquals(applicationFieldValue.getInputType(), Value.InputType.OPTIONS);
        assertEquals(applicationFieldValue.getValueType(), Value.ValueType.REFERENCE);
        assertEquals(applicationFieldValue.getValueReferenceAttribute(), "id");
        assertEquals(applicationFieldValue.getValueDisplayAttribute(), "name");

        assertEquals(applicationFieldValue.getLinks().size(), 2);
        assertEquals(applicationFieldValue.getLinks().get(0).getRel(), "values");
        assertEquals(applicationFieldValue.getLinks().get(0).getHref(), "/applications?offset=0&limit=10");
        assertEquals(applicationFieldValue.getLinks().get(0).getMethod(), "GET");
        assertEquals(applicationFieldValue.getLinks().get(1).getHref(), "/applications?filter=name+eq+*&limit=10");
        assertEquals(applicationFieldValue.getLinks().get(1).getRel(), "filter");
        assertEquals(applicationFieldValue.getLinks().get(1).getMethod(), "GET");
    }

    private void verifyGrantTypeField(Map<String, FieldDefinition> result) {

        FieldDefinition grantTypeFieldDefinition = result.get("grantType");
        assertNotNull(grantTypeFieldDefinition);
        assertEquals(grantTypeFieldDefinition.getField().getName(), "grantType");
        assertEquals(grantTypeFieldDefinition.getField().getDisplayName(), "grant type");

        assertEquals(grantTypeFieldDefinition.getOperators().size(), 2);
        assertEquals(grantTypeFieldDefinition.getOperators().get(0).getName(), "equals");
        assertEquals(grantTypeFieldDefinition.getOperators().get(0).getDisplayName(), "equals");
        assertEquals(grantTypeFieldDefinition.getOperators().get(1).getName(), "notEquals");
        assertEquals(grantTypeFieldDefinition.getOperators().get(1).getDisplayName(), "not equals");

        assertTrue(grantTypeFieldDefinition.getValue() instanceof OptionsInputValue);
        OptionsInputValue grantTypeFieldValue = (OptionsInputValue) grantTypeFieldDefinition.getValue();
        assertEquals(grantTypeFieldValue.getInputType(), Value.InputType.OPTIONS);
        assertEquals(grantTypeFieldValue.getValueType(), Value.ValueType.STRING);
    }

    private void verifyEmailField(Map<String, FieldDefinition> result) {

        FieldDefinition emailFieldDefinition = result.get("email");
        assertNotNull(emailFieldDefinition);
        assertEquals(emailFieldDefinition.getField().getName(), "email");
        assertEquals(emailFieldDefinition.getField().getDisplayName(), "user.email");

        assertEquals(emailFieldDefinition.getOperators().size(), 2);
        assertEquals(emailFieldDefinition.getOperators().get(0).getName(), "equals");
        assertEquals(emailFieldDefinition.getOperators().get(0).getDisplayName(), "equals");
        assertEquals(emailFieldDefinition.getOperators().get(1).getName(), "notEquals");
        assertEquals(emailFieldDefinition.getOperators().get(1).getDisplayName(), "not equals");

        assertTrue(emailFieldDefinition.getValue() instanceof InputValue);
        InputValue emailFieldValue = (InputValue) emailFieldDefinition.getValue();
        assertEquals(emailFieldValue.getInputType(), Value.InputType.INPUT);
        assertEquals(emailFieldValue.getValueType(), Value.ValueType.STRING);
    }

    @DataProvider(name = "invalidConfigFiles")
    public Object[][] invalidConfigFiles() {

        return new Object[][]{
                {"configs/invalid-fields-missing-field-name.json"},
                {"configs/invalid-fields-unregistered-operator.json"},
                {"configs/invalid-fields-invalid-input-type.json"},
                {"configs/invalid-fields-invalid-value-type.json"},
                {"configs/invalid-fields-missing-value-reference-attribute.json"},
                {"configs/invalid-fields-missing-value-display-attribute.json"},
                {"configs/invalid-fields-missing-links.json"},
                {"configs/invalid-fields-missing-link-href.json"},
                {"configs/invalid-fields-missing-link-method.json"},
                {"configs/invalid-fields-missing-link-rel.json"},
                {"unavailable-file.json"}
        };
    }

    @Test(dataProvider = "invalidConfigFiles", expectedExceptions = RuleMetadataConfigException.class,
            expectedExceptionsMessageRegExp = "Error while loading field definitions from file: .*")
    public void testLoadFieldDefinitionsFromInvalidConfig(String filePath) throws RuleMetadataConfigException {

        FieldDefinitionConfig.load(filePath.equals("unavailable-file.json") ? new File(filePath) :
                new File(getClass().getClassLoader().getResource(filePath).getFile()), operatorConfig);
    }

    @DataProvider(name = "validConfigFiles")
    public Object[][] validConfigFiles() {

        return new Object[][]{
                {"configs/invalid-fields-missing-field-displayname.json"},
                {"configs/invalid-fields-missing-values.json"},
        };
    }

    @Test(dataProvider = "validConfigFiles")
    public void testLoadFieldDefinitionsFromValidConfig(String filePath) throws RuleMetadataConfigException {

        try {
            FieldDefinitionConfig.load(filePath.equals("unavailable-file.json") ? new File(filePath) :
                    new File(getClass().getClassLoader().getResource(filePath).getFile()), operatorConfig);
        } catch (Exception e) {
            fail();
        }
    }
}
