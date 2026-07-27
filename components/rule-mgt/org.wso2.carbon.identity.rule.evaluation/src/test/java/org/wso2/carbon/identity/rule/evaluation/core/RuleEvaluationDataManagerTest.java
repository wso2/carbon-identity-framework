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
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.rule.evaluation.api.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.identity.rule.evaluation.internal.service.impl.RuleEvaluationDataManager;

import java.util.ArrayList;
import java.util.List;
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

        List<FieldValue> evaluationData =
                ruleEvaluationDataManager.getEvaluationData(mockRuleEvaluationContext, mockFlowContext, "tenantDomain");
        assertNotNull(evaluationData);
        assertEquals(evaluationData.size(), 2);
        assertEquals(evaluationData.get(0).getName(), "application");
        assertEquals(evaluationData.get(0).getValueType(), ValueType.REFERENCE);
        assertEquals(evaluationData.get(0).getValue(), "testApp");
        assertEquals(evaluationData.get(1).getName(), "grantType");
        assertEquals(evaluationData.get(1).getValueType(), ValueType.STRING);
        assertEquals(evaluationData.get(1).getValue(), "client-credentials");
    }

    @Test(dependsOnMethods = "testGetEvaluationData")
    public void testUnregisterRuleEvaluationDataProvider() throws Exception {

        when(mockProvider.getSupportedFlowType()).thenReturn(FlowType.PRE_ISSUE_ACCESS_TOKEN);
        ruleEvaluationDataManager.unregisterRuleEvaluationDataProvider(mockProvider);
        Map<FlowType, RuleEvaluationDataProvider> evaluationDataProviders =
                getEvaluationDataProvidersInInstance(ruleEvaluationDataManager);
        assertFalse(evaluationDataProviders.containsKey(FlowType.PRE_ISSUE_ACCESS_TOKEN));
    }

    private List<FieldValue> getMockedFieldValues() {

        List<FieldValue> fieldValues = new ArrayList<>();
        fieldValues.add(new FieldValue("application", "testApp", ValueType.REFERENCE));
        fieldValues.add(new FieldValue("grantType", "client-credentials", ValueType.STRING));
        return fieldValues;
    }

    private static Map<FlowType, RuleEvaluationDataProvider> getEvaluationDataProvidersInInstance(
            RuleEvaluationDataManager instance) throws NoSuchFieldException, IllegalAccessException {

        java.lang.reflect.Field field = instance.getClass().getDeclaredField("ruleEvaluationDataProviderMap");
        field.setAccessible(true);
        return (Map<FlowType, RuleEvaluationDataProvider>) field.get(instance);
    }
}
