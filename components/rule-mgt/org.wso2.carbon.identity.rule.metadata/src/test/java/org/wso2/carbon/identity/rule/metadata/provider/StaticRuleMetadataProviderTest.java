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

package org.wso2.carbon.identity.rule.metadata.provider;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataServerException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsInputValue;
import org.wso2.carbon.identity.rule.metadata.api.model.OptionsReferenceValue;
import org.wso2.carbon.identity.rule.metadata.api.model.Value;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;
import org.wso2.carbon.identity.rule.metadata.internal.provider.impl.StaticRuleMetadataProvider;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class StaticRuleMetadataProviderTest {

    private MockedStatic<CarbonUtils> carbonUtilsMockedStatic;

    @BeforeClass
    public void setUpClass() {

        carbonUtilsMockedStatic = mockStatic(CarbonUtils.class);
        when(CarbonUtils.getCarbonHome())
                .thenReturn(getClass().getClassLoader().getResource("configs").getFile());

    }

    @AfterClass
    public void tearDownClass() {

        carbonUtilsMockedStatic.close();
    }

    @Test
    public void testLoadStaticMetadata() throws RuleMetadataServerException {

        StaticRuleMetadataProvider provider = StaticRuleMetadataProvider.loadStaticMetadata();
        assertNotNull(provider);
    }

    @Test(expectedExceptions = RuleMetadataServerException.class,
            expectedExceptionsMessageRegExp = "Error while loading static rule metadata.")
    public void testLoadStaticMetadataThrowsException() throws RuleMetadataServerException {

        try (MockedStatic<RuleMetadataConfigFactory> ruleMetadataConfigFactoryMockedStatic = mockStatic(
                RuleMetadataConfigFactory.class)) {
            ruleMetadataConfigFactoryMockedStatic.when(RuleMetadataConfigFactory::load)
                    .thenThrow(new RuleMetadataConfigException("Failed to read configs"));
            StaticRuleMetadataProvider.loadStaticMetadata();
        }
    }

    @Test
    public void testGetExpressionMeta() throws RuleMetadataException {

        StaticRuleMetadataProvider provider = StaticRuleMetadataProvider.loadStaticMetadata();
        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, "tenant1");

        assertNotNull(result);
        assertEquals(result.size(), 2);

        FieldDefinition applicationFieldDefinition = result.get(0);
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
        assertEquals(applicationFieldValue.getLinks().get(0).getHref(),
                "/applications?excludeSystemPortals=true&offset=0&limit=10");
        assertEquals(applicationFieldValue.getLinks().get(0).getRel(), "values");
        assertEquals(applicationFieldValue.getLinks().get(0).getMethod(), "GET");
        assertEquals(applicationFieldValue.getLinks().get(1).getHref(),
                "/applications?excludeSystemPortals=true&filter=name+eq+*&limit=10");
        assertEquals(applicationFieldValue.getLinks().get(1).getRel(), "filter");
        assertEquals(applicationFieldValue.getLinks().get(1).getMethod(), "GET");

        FieldDefinition grantTypeFieldDefinition = result.get(1);
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
}
