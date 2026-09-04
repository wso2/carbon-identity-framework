/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.internal.rule;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for DevicePolicyEvaluationDataProvider.
 */
public class DevicePolicyEvaluationDataProviderTest {

    private DevicePolicyEvaluationDataProvider dataProvider;
    private RuleEvaluationContext ruleEvaluationContext;
    private FlowContext flowContext;

    @BeforeMethod
    public void setUp() {

        dataProvider = new DevicePolicyEvaluationDataProvider();
        ruleEvaluationContext = mock(RuleEvaluationContext.class);
        flowContext = mock(FlowContext.class);
    }

    @Test
    public void testGetSupportedFlowType() {

        assertEquals(dataProvider.getSupportedFlowType(), FlowType.DEVICE_POLICY);
    }

    @Test
    public void testGetEvaluationData() throws RuleEvaluationDataProviderException {

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("os", "macOS");
        deviceData.put("version", "15.0");
        
        when(flowContext.getContextData()).thenReturn(deviceData);
        
        List<Field> fields = new ArrayList<>();
        Field osField = new Field("os", ValueType.STRING);
        Field versionField = new Field("version", ValueType.NUMBER);
        Field unknownField = new Field("unknown", ValueType.STRING);
        
        fields.add(osField);
        fields.add(versionField);
        fields.add(unknownField);
        
        when(ruleEvaluationContext.getFields()).thenReturn(fields);
        
        List<FieldValue> evaluationData = dataProvider.getEvaluationData(ruleEvaluationContext, flowContext,
                "carbon.super");
        
        assertNotNull(evaluationData);
        assertEquals(evaluationData.size(), 2);
        assertEquals(evaluationData.get(0).getName(), "os");
        assertEquals(evaluationData.get(0).getValue(), "macOS");
        assertEquals(evaluationData.get(1).getName(), "version");
        assertEquals(evaluationData.get(1).getValue(), 15.0);
    }

    @Test(expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationDataWithInvalidNumber() throws RuleEvaluationDataProviderException {

        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("version", "invalid");
        
        when(flowContext.getContextData()).thenReturn(deviceData);
        
        List<Field> fields = new ArrayList<>();
        Field versionField = new Field("version", ValueType.NUMBER);
        fields.add(versionField);
        
        when(ruleEvaluationContext.getFields()).thenReturn(fields);
        
        dataProvider.getEvaluationData(ruleEvaluationContext, flowContext, "carbon.super");
    }
}
