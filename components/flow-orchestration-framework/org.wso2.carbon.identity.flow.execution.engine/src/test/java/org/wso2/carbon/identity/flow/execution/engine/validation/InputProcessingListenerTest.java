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

package org.wso2.carbon.identity.flow.execution.engine.validation;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for InputProcessingListener.
 */
public class InputProcessingListenerTest {

    private static final String TASK_NODE_ID = "taskNode";
    private static final String TEST_USERNAME = "testUser";

    private InputProcessingListener listener;
    private MockedStatic<InputValidationService> mockedInputValidationService;
    private InputValidationService mockInputValidationService;

    @BeforeMethod
    public void setUp() {

        listener = new InputProcessingListener();
        mockInputValidationService = mock(InputValidationService.class);
        mockedInputValidationService = mockStatic(InputValidationService.class);
        mockedInputValidationService.when(InputValidationService::getInstance)
                .thenReturn(mockInputValidationService);
    }

    @AfterMethod
    public void tearDown() {

        mockedInputValidationService.close();
    }

    @Test
    public void testGetDefaultOrderId() {

        Assert.assertEquals(listener.getDefaultOrderId(), 1);
    }

    @Test
    public void testGetExecutionOrderId() {

        Assert.assertEquals(listener.getExecutionOrderId(), 2);
    }

    @Test
    public void testIsEnabled() {

        Assert.assertTrue(listener.isEnabled());
    }

    @Test
    public void testDoPreExecute_WhenUserInputDataIsEmpty_SkipsProcessing() throws FlowEngineException {

        FlowExecutionContext context = new FlowExecutionContext();

        boolean result = listener.doPreExecute(context);

        Assert.assertTrue(result);
        verify(mockInputValidationService, never()).prepareStepInputs(any(), any());
    }

    @Test
    public void testDoPreExecute_WhenCurrentStepInputsNotEmpty_SkipsProcessing() throws FlowEngineException {

        FlowExecutionContext context = new FlowExecutionContext();
        context.addUserInputData("username", TEST_USERNAME);
        Map<String, Set<String>> stepInputs = new HashMap<>();
        stepInputs.put("action1", new HashSet<>());
        context.setCurrentStepInputs(stepInputs);

        boolean result = listener.doPreExecute(context);

        Assert.assertTrue(result);
        verify(mockInputValidationService, never()).prepareStepInputs(any(), any());
    }

    @Test
    public void testDoPreExecute_WithNonPromptNode_UsesNodeMappedDataDTO() throws FlowEngineException {

        FlowExecutionContext context = new FlowExecutionContext();
        context.addUserInputData("username", TEST_USERNAME);

        DataDTO dataDTO = new DataDTO();
        StepDTO stepDTO = new StepDTO();
        stepDTO.setData(dataDTO);

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(TASK_NODE_ID)
                .type("TASK_EXECUTION")
                .build();

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFirstNodeId(TASK_NODE_ID);
        graphConfig.getNodeConfigs().put(TASK_NODE_ID, nodeConfig);
        graphConfig.getNodePageMappings().put(TASK_NODE_ID, stepDTO);

        context.setGraphConfig(graphConfig);

        boolean result = listener.doPreExecute(context);

        Assert.assertTrue(result);
        Assert.assertNull(context.getCurrentNode());
        verify(mockInputValidationService).prepareStepInputs(eq(dataDTO), eq(context));
    }

    @Test
    public void testDoPreExecute_WithNoMappingForNode_PassesNullDataDTO() throws FlowEngineException {

        FlowExecutionContext context = new FlowExecutionContext();
        context.addUserInputData("username", TEST_USERNAME);

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(TASK_NODE_ID)
                .type("TASK_EXECUTION")
                .build();

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFirstNodeId(TASK_NODE_ID);
        graphConfig.getNodeConfigs().put(TASK_NODE_ID, nodeConfig);

        context.setGraphConfig(graphConfig);

        boolean result = listener.doPreExecute(context);

        Assert.assertTrue(result);
        verify(mockInputValidationService).prepareStepInputs(eq(null), eq(context));
    }

    @Test
    public void testDoPreExecute_WhenFirstNodeNotInConfigs_CurrentNodeIsNull() throws FlowEngineException {

        FlowExecutionContext context = new FlowExecutionContext();
        context.addUserInputData("username", TEST_USERNAME);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFirstNodeId("nonExistentNode");

        context.setGraphConfig(graphConfig);

        boolean result = listener.doPreExecute(context);

        Assert.assertTrue(result);
        verify(mockInputValidationService).prepareStepInputs(eq(null), eq(context));
    }

    @Test
    public void testDoPostExecute_InvokesPrepareStepInputsAndClearUserInputs() throws FlowEngineException {

        DataDTO dataDTO = new DataDTO();
        FlowExecutionStep step = new FlowExecutionStep();
        step.setData(dataDTO);
        FlowExecutionContext context = new FlowExecutionContext();

        boolean result = listener.doPostExecute(step, context);

        Assert.assertTrue(result);
        verify(mockInputValidationService).prepareStepInputs(eq(dataDTO), eq(context));
        verify(mockInputValidationService).clearUserInputs(eq(context));
    }

    @Test
    public void testDoPostExecute_WithNullStepData_PassesNullToService() throws FlowEngineException {

        FlowExecutionStep step = new FlowExecutionStep();
        FlowExecutionContext context = new FlowExecutionContext();

        boolean result = listener.doPostExecute(step, context);

        Assert.assertTrue(result);
        verify(mockInputValidationService).prepareStepInputs(eq(null), eq(context));
        verify(mockInputValidationService).clearUserInputs(eq(context));
    }
}
