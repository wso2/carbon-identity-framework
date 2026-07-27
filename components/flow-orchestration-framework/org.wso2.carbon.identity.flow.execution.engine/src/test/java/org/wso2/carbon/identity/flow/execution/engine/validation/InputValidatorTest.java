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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;

public class InputValidatorTest {

    private InputValidator inputValidator;

    @BeforeMethod
    public void setUp() {

        inputValidator = InputValidator.getInstance();
    }

    private Map<String, Set<String>> stepInputs() {

        Map<String, Set<String>> inputs = new HashMap<>();
        inputs.put("DEFAULT_ACTION", new HashSet<>(Collections.singletonList("username")));
        return inputs;
    }

    @Test
    public void testGetInstanceReturnsSingleton() {

        Assert.assertSame(InputValidator.getInstance(), inputValidator);
    }

    @Test
    public void testExecuteInputValidationReturnsNullWhenCurrentStepInputsIsNull() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("EXECUTOR_NODE").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getCurrentStepInputs()).thenReturn(null);
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    @Test
    public void testExecuteInputValidationReturnsNullWhenCurrentStepInputsIsEmpty() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("EXECUTOR_NODE").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getCurrentStepInputs()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    // -------------------------------------------------------------------------
    // Has step inputs + empty/null user input + not END → buildIncompleteResponse
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsIncompleteWhenInputMissingAndNotEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
    }

    @Test
    public void testExecuteInputValidationReturnsIncompleteWhenInputIsNullAndNotEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getUserInputData()).thenReturn(null);
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
    }

    @Test
    public void testBuildIncompleteResponseSetsStatusAndType() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
        Assert.assertNull(response.getRequiredData());
        Assert.assertNull(response.getOptionalData());
    }

    @Test
    public void testExecuteInputValidationReturnsIncompleteWhenInputEmpty() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig testNode = new NodeConfig.Builder().id("test-node-id").build();
        when(context.getCurrentNode()).thenReturn(testNode);
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
    }

    // -------------------------------------------------------------------------
    // Has step inputs + non-empty user input → validation runs
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsNullWhenValidationPasses() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_1", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_COMPLETE);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNull(response);
        }
    }

    @Test
    public void testExecuteInputValidationReturnsRetryResponseWhenValidationFails() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_1", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Validation error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            Assert.assertEquals(response.getType(), VIEW);
            Assert.assertEquals(response.getError(), "Validation error");
        }
    }

    // -------------------------------------------------------------------------
    // buildValidationRetryResponse — data forwarding and no-rollback guarantee
    // -------------------------------------------------------------------------

    @Test
    public void testBuildValidationRetryResponsePassesThroughRequiredAndOptionalData() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_1", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Validation error");
        executorResponse.setRequiredData(Arrays.asList("field1", "field2"));
        executorResponse.setOptionalData(Collections.singletonList("field3"));

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            Assert.assertEquals(response.getType(), VIEW);
            Assert.assertEquals(response.getError(), "Validation error");
            Assert.assertNotNull(response.getRequiredData());
            Assert.assertEquals(response.getRequiredData().size(), 2);
            Assert.assertNotNull(response.getOptionalData());
            Assert.assertEquals(response.getOptionalData().size(), 1);
        }
    }

    @Test
    public void testBuildValidationRetryResponseDoesNotRollBackWhenCurrentNodeHasPageMapping() {

        // When the current node itself has a page mapping, findClosestNodeWithPageMapping returns
        // the current node immediately → the "different id" condition is false → no setCurrentNode.
        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_1", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);
            inputValidator.executeInputValidation(context);
            verify(context, never()).setCurrentNode(any());
        }
    }

    @Test
    public void testBuildValidationRetryResponseRollsBackToPreviousNodeWithPageMapping() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getContextIdentifier()).thenReturn("flow-123");
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());

        NodeConfig nodeB = new NodeConfig.Builder().id("NODE_B").previousNodeId("PREV_NODE").build();
        NodeConfig prevNode = new NodeConfig.Builder().id("PREV_NODE").build();

        when(context.getCurrentNode()).thenReturn(nodeB);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_A", new StepDTO());
        graphConfig.addNodePageMapping("PREV_NODE", new StepDTO());
        graphConfig.addNodeConfig(prevNode);
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Validation error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            Assert.assertEquals(response.getType(), VIEW);
            Assert.assertEquals(response.getError(), "Validation error");
            verify(context).setCurrentNode(prevNode);
        }
    }

    @Test
    public void testFindClosestNodeTraversesThroughMultipleNodesToFindPageMapping() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getContextIdentifier()).thenReturn("flow-123");
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());

        NodeConfig nodeB = new NodeConfig.Builder().id("NODE_B").previousNodeId("MID_NODE").build();
        NodeConfig midNode = new NodeConfig.Builder().id("MID_NODE").previousNodeId("PREV_NODE").build();
        NodeConfig prevNode = new NodeConfig.Builder().id("PREV_NODE").build();

        when(context.getCurrentNode()).thenReturn(nodeB);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_A", new StepDTO());
        graphConfig.addNodePageMapping("PREV_NODE", new StepDTO());
        graphConfig.addNodeConfig(midNode);
        graphConfig.addNodeConfig(prevNode);
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Validation error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            verify(context).setCurrentNode(prevNode);
        }
    }

    @Test
    public void testFindClosestNodeBreaksWhenPreviousNodeNotInGraph() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getContextIdentifier()).thenReturn("flow-123");
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());

        NodeConfig nodeA = new NodeConfig.Builder().id("NODE_A").build();
        NodeConfig nodeB = new NodeConfig.Builder().id("NODE_B").previousNodeId("MISSING_NODE").build();

        when(context.getCurrentNode()).thenReturn(nodeA, nodeA, nodeB);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_A", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            verify(context, never()).setCurrentNode(any());
        }
    }

    @Test
    public void testFindClosestNodeReturnsCurrentWhenNoPreviousNodeId() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getContextIdentifier()).thenReturn("flow-123");
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());

        NodeConfig nodeA = new NodeConfig.Builder().id("NODE_A").build();
        NodeConfig nodeB = new NodeConfig.Builder().id("NODE_B").build();

        when(context.getCurrentNode()).thenReturn(nodeA, nodeA, nodeB);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_A", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            verify(context, never()).setCurrentNode(any());
        }
    }

    @Test
    public void testFindClosestNodeReturnsCurrentWhenNoPageMappingFoundInChain() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getContextIdentifier()).thenReturn("flow-123");
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());

        NodeConfig nodeA = new NodeConfig.Builder().id("NODE_A").build();
        NodeConfig nodeB = new NodeConfig.Builder().id("NODE_B").previousNodeId("MID_NODE").build();
        NodeConfig midNode = new NodeConfig.Builder().id("MID_NODE").build();

        when(context.getCurrentNode()).thenReturn(nodeA, nodeA, nodeB);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_A", new StepDTO());
        graphConfig.addNodeConfig(midNode);
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            verify(context, never()).setCurrentNode(any());
        }
    }

    @Test
    public void testBuildValidationRetryResponseWhenCurrentNodeBecomesNull() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getContextIdentifier()).thenReturn("flow-123");
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());

        NodeConfig nodeA = new NodeConfig.Builder().id("NODE_A").build();

        when(context.getCurrentNode()).thenReturn(nodeA, nodeA, null);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_A", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            Assert.assertEquals(response.getType(), VIEW);
            verify(context, never()).setCurrentNode(any());
        }
    }

    @Test
    public void testExecuteInputValidationReturnsNullWhenTriggeredActionHasNoInputs() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);

        Map<String, Set<String>> stepInputs = new HashMap<>();
        stepInputs.put("DEFAULT_ACTION", new HashSet<>(Collections.singletonList("username")));
        stepInputs.put("SOCIAL_LOGIN", Collections.emptySet());
        when(context.getCurrentStepInputs()).thenReturn(stepInputs);
        when(context.getCurrentActionId()).thenReturn("SOCIAL_LOGIN");
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    @Test
    public void testExecuteInputValidationReturnsNullWhenTriggeredActionNotInStepInputs() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);

        Map<String, Set<String>> stepInputs = new HashMap<>();
        stepInputs.put("DEFAULT_ACTION", new HashSet<>(Collections.singletonList("username")));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs);
        when(context.getCurrentActionId()).thenReturn("BUTTON_2");
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    @Test
    public void testExecuteInputValidationProceedsWhenTriggeredActionHasInputs() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);

        Map<String, Set<String>> stepInputs = new HashMap<>();
        stepInputs.put("DEFAULT_ACTION", new HashSet<>(Collections.singletonList("username")));
        when(context.getCurrentStepInputs()).thenReturn(stepInputs);
        when(context.getCurrentActionId()).thenReturn("DEFAULT_ACTION");
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
    }

    @Test
    public void testExecuteInputValidationProceedsNormallyWhenActionIdIsNull() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getCurrentStepInputs()).thenReturn(stepInputs());
        when(context.getCurrentActionId()).thenReturn(null);
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
    }
}
