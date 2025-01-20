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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.provider.RuleEvaluationDataProvider;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class RuleEvaluationDataManagerTest {

    private RuleEvaluationDataManager ruleEvaluationDataManager;
    private RuleEvaluationDataProvider mockProvider;
    private FlowContext mockFlowContext;
    private RuleEvaluationContext mockRuleEvaluationContext;

    @BeforeClass
    public void setUpClass() {

        ruleEvaluationDataManager = RuleEvaluationDataManager.getInstance();
        mockProvider = mock(RuleEvaluationDataProvider.class);
    }

    @BeforeMethod
    public void setUp() {

        mockFlowContext = mock(FlowContext.class);
        mockRuleEvaluationContext = mock(RuleEvaluationContext.class);
    }

    @Test
    public void testRegisterRuleEvaluationDataProvider() throws Exception {

        when(mockProvider.getSupportedFlowType()).thenReturn(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        ruleEvaluationDataManager.registerRuleEvaluationDataProvider(mockProvider);

        Map<FlowType, RuleEvaluationDataProvider> evaluationDataProviders =
                getEvaluationDataProvidersInInstance(ruleEvaluationDataManager);
        assertTrue(evaluationDataProviders.containsKey(FlowType.PRE_ISSUE_ACCESS_TOKEN));
        assertEquals(mockProvider,
                getEvaluationDataProvidersInInstance(ruleEvaluationDataManager).get(FlowType.PRE_ISSUE_ACCESS_TOKEN));
    }

    @Test(dependsOnMethods = "testRegisterRuleEvaluationDataProvider")
    public void testGetEvaluationData() throws RuleEvaluationException {

        when(mockFlowContext.getFlowType()).thenReturn(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        when(mockProvider.getSupportedFlowType()).thenReturn(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        when(mockProvider.getEvaluationData(mockRuleEvaluationContext, mockFlowContext, "tenantDomain"))
                .thenReturn(getMockedFieldValues());

        Map<String, FieldValue> evaluationData =
                ruleEvaluationDataManager.getEvaluationData(mockRuleEvaluationContext, mockFlowContext, "tenantDomain");
        assertNotNull(evaluationData);
        assertEquals(evaluationData.size(), 2);
        assertTrue(evaluationData.containsKey("application"));
        assertEquals(evaluationData.get("application").getValueType(), ValueType.REFERENCE);
        assertEquals(evaluationData.get("application").getValue(), "testApp");
        assertTrue(evaluationData.containsKey("grantType"));
        assertEquals(evaluationData.get("grantType").getValueType(), ValueType.STRING);
        assertEquals(evaluationData.get("grantType").getValue(), "client-credentials");
    }

    @Test(dependsOnMethods = "testGetEvaluationData")
    public void testUnregisterRuleEvaluationDataProvider() throws Exception {

        when(mockProvider.getSupportedFlowType()).thenReturn(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        ruleEvaluationDataManager.unregisterRuleEvaluationDataProvider(mockProvider);
        Map<FlowType, RuleEvaluationDataProvider> evaluationDataProviders =
                getEvaluationDataProvidersInInstance(ruleEvaluationDataManager);
        assertFalse(evaluationDataProviders.containsKey(FlowType.PRE_ISSUE_ACCESS_TOKEN));
    }

    private Map<String, FieldValue> getMockedFieldValues() {

        Map<String, FieldValue> fieldValues = new HashMap<>();
        fieldValues.put("application", new FieldValue("application", "testApp", ValueType.REFERENCE));
        fieldValues.put("grantType", new FieldValue("grantType", "client-credentials", ValueType.STRING));
        return fieldValues;
    }

    private static Map<FlowType, RuleEvaluationDataProvider> getEvaluationDataProvidersInInstance(
            RuleEvaluationDataManager instance) throws NoSuchFieldException, IllegalAccessException {

        java.lang.reflect.Field field = instance.getClass().getDeclaredField("ruleEvaluationDataProviderMap");
        field.setAccessible(true);
        return (Map<FlowType, RuleEvaluationDataProvider>) field.get(instance);
    }
}
