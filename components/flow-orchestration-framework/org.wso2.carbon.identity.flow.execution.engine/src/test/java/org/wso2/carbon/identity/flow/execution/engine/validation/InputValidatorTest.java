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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.END;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;

public class InputValidatorTest {

    private InputValidator inputValidator;

    @BeforeMethod
    public void setUp() {

        inputValidator = InputValidator.getInstance();
    }

    @Test
    public void testGetInstanceReturnsSingleton() {

        Assert.assertSame(InputValidator.getInstance(), inputValidator);
    }

    @Test
    public void testExecuteInputValidationReturnsIncompleteWhenInputMissingAndNotEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
    }

    // -------------------------------------------------------------------------
    // executeInputValidation — empty input + END node → validation runs, passes → null
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsNullWhenInputEmptyAndEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig endNode = new NodeConfig.Builder().id(END).build();
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getCurrentNode()).thenReturn(endNode);

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

    // -------------------------------------------------------------------------
    // executeInputValidation — non-empty input, validation passes → null
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsNullWhenValidationPasses() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));

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

    // -------------------------------------------------------------------------
    // executeInputValidation — validation fails (STATUS_RETRY) → retry response
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsRetryResponseWhenValidationFails() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
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
    // executeInputValidation — null input + non-END node → buildIncompleteResponse
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsIncompleteWhenInputIsNullAndNotEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getUserInputData()).thenReturn(null);
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
    }

    // -------------------------------------------------------------------------
    // executeInputValidation — null input + END node → validation runs
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationRunsValidationWhenInputIsNullAndEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig endNode = new NodeConfig.Builder().id(END).build();
        when(context.getUserInputData()).thenReturn(null);
        when(context.getCurrentNode()).thenReturn(endNode);

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
    public void testBuildValidationRetryResponsePassesThroughRequiredAndOptionalData() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
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
    public void testBuildValidationRetryResponseKeepsCurrentNodeWhenItHasPageMapping() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
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
    public void testBuildValidationRetryResponseRollsBackToAncestorWithPageMapping() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig previousNode = new NodeConfig.Builder().id("NODE_1").build();
        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_2").previousNodeId("NODE_1").build();

        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodeConfig(previousNode);
        // Only NODE_1 (ancestor) has a page mapping; NODE_2 (current) does not.
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

            // Context must roll back to the ancestor with a page mapping.
            verify(context).setCurrentNode(previousNode);
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        }
    }

    @Test
    public void testBuildValidationRetryResponseKeepsCurrentNodeWhenNoPageMappingFound() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig previousNode = new NodeConfig.Builder().id("NODE_1").build();
        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_2").previousNodeId("NODE_1").build();

        when(context.getCurrentNode()).thenReturn(currentNode);

        // No page mappings for any node.
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodeConfig(previousNode);
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            inputValidator.executeInputValidation(context);

            // findClosestNodeWithPageMapping returns currentNode itself — same id → no rollback.
            verify(context, never()).setCurrentNode(any());
        }
    }

    @Test
    public void testBuildValidationRetryResponseHandlesNullCurrentNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");
        when(context.getCurrentNode()).thenReturn(null);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            // nodeWithPageMapping is null → condition in buildValidationRetryResponse is false → no rollback.
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            verify(context, never()).setCurrentNode(any());
        }
    }

    @Test
    public void testFindClosestNodeWithPageMappingNoPreviousNodeId() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        // Current node has no previousNodeId set (null) and no page mapping.
        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = new GraphConfig();
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            // previousNodeId is null → while loop never entered → returns currentNode → same id → no rollback.
            verify(context, never()).setCurrentNode(any());
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        }
    }

    @Test
    public void testFindClosestNodeWithPageMappingFoundAtDepthGreaterThanOne() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        // Chain: NODE_0 (has page mapping) ← NODE_1 (no mapping) ← NODE_2 (no mapping, current).
        NodeConfig node0 = new NodeConfig.Builder().id("NODE_0").build();
        NodeConfig node1 = new NodeConfig.Builder().id("NODE_1").previousNodeId("NODE_0").build();
        NodeConfig node2 = new NodeConfig.Builder().id("NODE_2").previousNodeId("NODE_1").build();

        when(context.getCurrentNode()).thenReturn(node2);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodeConfig(node0);
        graphConfig.addNodeConfig(node1);
        graphConfig.addNodeConfig(node2);
        // Only NODE_0 has page mapping.
        graphConfig.addNodePageMapping("NODE_0", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setErrorMessage("Validation error");

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            // Should roll back to NODE_0 (found at depth 2).
            verify(context).setCurrentNode(node0);
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        }
    }

    @Test
    public void testFindClosestNodeWithPageMappingBreaksWhenPreviousNodeMissingFromGraph() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_2").previousNodeId("MISSING_NODE").build();
        when(context.getCurrentNode()).thenReturn(currentNode);

        // Graph has no nodes and no page mappings — previousNodeId lookup returns null.
        GraphConfig graphConfig = new GraphConfig();
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            // Loop breaks; falls back to current node — same id → no rollback.
            verify(context, never()).setCurrentNode(any());
            Assert.assertNotNull(response);
        }
    }

    @Test
    public void testFindClosestNodeWithPageMappingStopsAtMaxTraversalDepth() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        // Build a chain of 12 nodes (> DEFAULT_MAX_TRAVERSAL_DEPTH=10); none have page mappings.
        int chainLength = 12;
        NodeConfig[] nodes = new NodeConfig[chainLength];
        for (int i = 0; i < chainLength; i++) {
            NodeConfig.Builder builder = new NodeConfig.Builder().id("NODE_" + i);
            if (i > 0) {
                builder.previousNodeId("NODE_" + (i - 1));
            }
            nodes[i] = builder.build();
        }
        NodeConfig currentNode = nodes[chainLength - 1];
        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = new GraphConfig();
        for (NodeConfig node : nodes) {
            graphConfig.addNodeConfig(node);
        }
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            // Depth limit reached; falls back to current node — same id → no rollback.
            verify(context, never()).setCurrentNode(any());
            Assert.assertNotNull(response);
        }
    }

    @Test
    public void testHasPageMappingReturnsFalseWhenGraphConfigIsNull() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(currentNode);
        when(context.getGraphConfig()).thenReturn(null);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        }
    }

    @Test
    public void testHasPageMappingReturnsFalseWhenNodePageMappingsIsNull() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = mock(GraphConfig.class);
        when(graphConfig.getNodePageMappings()).thenReturn(null);
        when(graphConfig.getNodeConfigs()).thenReturn(Collections.emptyMap());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

        try (MockedStatic<InputValidationService> serviceMock = mockStatic(InputValidationService.class)) {
            InputValidationService mockService = mock(InputValidationService.class);
            serviceMock.when(InputValidationService::getInstance).thenReturn(mockService);
            when(mockService.resolveInputValidationResponse(context)).thenReturn(executorResponse);

            NodeResponse response = inputValidator.executeInputValidation(context);

            Assert.assertNotNull(response);
            Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        }
    }

    @Test
    public void testHasPageMappingReturnsFalseWhenNodeIdNotInMappings() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeConfig currentNode = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(currentNode);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("OTHER_NODE", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);

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
    public void testBuildIncompleteResponseSetsStatusAndType() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getContextIdentifier()).thenReturn("flow-123");

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
        Assert.assertNull(response.getRequiredData());
        Assert.assertNull(response.getOptionalData());
    }
}
