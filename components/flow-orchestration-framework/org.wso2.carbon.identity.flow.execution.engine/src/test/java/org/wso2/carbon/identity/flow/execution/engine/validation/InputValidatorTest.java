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
    public void testExecuteInputValidationReturnsNullWhenNodeHasNoPageMapping() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("EXECUTOR_NODE").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());

        // GraphConfig exists but EXECUTOR_NODE is not in pageMapping (e.g. FIDO2/passkey executor node).
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("OTHER_NODE", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    @Test
    public void testExecuteInputValidationReturnsNullWhenGraphConfigIsNull() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));
        when(context.getGraphConfig()).thenReturn(null);

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    @Test
    public void testExecuteInputValidationReturnsNullWhenNodePageMappingsIsNull() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));

        GraphConfig graphConfig = mock(GraphConfig.class);
        when(graphConfig.getNodePageMappings()).thenReturn(null);
        when(context.getGraphConfig()).thenReturn(graphConfig);

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    @Test
    public void testExecuteInputValidationReturnsNullWhenNodeNotInPageMappings() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getUserInputData()).thenReturn(Collections.singletonMap("username", "alice"));

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("PAGE_ONLY_NODE", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNull(response);
    }

    // -------------------------------------------------------------------------
    // Has page mapping + empty/null input + not END → buildIncompleteResponse
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsIncompleteWhenInputMissingAndNotEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_1", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

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
        when(context.getUserInputData()).thenReturn(null);
        when(context.getContextIdentifier()).thenReturn("flow-123");

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_1", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

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
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());
        when(context.getContextIdentifier()).thenReturn("flow-123");

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping("NODE_1", new StepDTO());
        when(context.getGraphConfig()).thenReturn(graphConfig);

        NodeResponse response = inputValidator.executeInputValidation(context);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), STATUS_INCOMPLETE);
        Assert.assertEquals(response.getType(), VIEW);
        Assert.assertNull(response.getError());
        Assert.assertNull(response.getRequiredData());
        Assert.assertNull(response.getOptionalData());
    }

    // -------------------------------------------------------------------------
    // Has page mapping + empty/null input + END node → validation runs
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsNullWhenInputEmptyAndEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig endNode = new NodeConfig.Builder().id(END).build();
        when(context.getCurrentNode()).thenReturn(endNode);
        when(context.getUserInputData()).thenReturn(Collections.emptyMap());

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping(END, new StepDTO());
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
    public void testExecuteInputValidationRunsValidationWhenInputIsNullAndEndNode() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig endNode = new NodeConfig.Builder().id(END).build();
        when(context.getCurrentNode()).thenReturn(endNode);
        when(context.getUserInputData()).thenReturn(null);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.addNodePageMapping(END, new StepDTO());
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

    // -------------------------------------------------------------------------
    // Has page mapping + non-empty input → validation runs
    // -------------------------------------------------------------------------

    @Test
    public void testExecuteInputValidationReturnsNullWhenValidationPasses() {

        FlowExecutionContext context = mock(FlowExecutionContext.class);
        NodeConfig nodeConfig = new NodeConfig.Builder().id("NODE_1").build();
        when(context.getCurrentNode()).thenReturn(nodeConfig);
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
}
