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

package org.wso2.carbon.identity.flow.execution.engine.util;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.core.FlowExecutionEngine;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.flow.execution.engine.listener.FlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.execution.engine.validation.InputValidationListener;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FIRST_NODE_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_REDIRECTION_URL_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_REQUIRED_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_WEBAUTHN_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.WEBAUTHN_DATA;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.TASK_EXECUTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.INTERNAL_PROMPT;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.WEBAUTHN;

/**
 * Unit tests for FlowExecutionEngine.
 */
@Test(singleThreaded = true)
public class FlowEngineTest {

    private FlowExecutionContext context;
    private GraphConfig defaultGraph;
    private List<FlowExecutionListener> listeners = new ArrayList<>();

    @BeforeClass
    public void setup() {

        defaultGraph = buildGraphWithDecision();
        listeners.add(new InputValidationListener());
    }

    @AfterClass
    public void tearDown() {
        // Clean up class-level resources if any
        context = null;
        defaultGraph = null;
        listeners = null;
        org.mockito.Mockito.framework().clearInlineMocks();
    }

    @Test
    public void testDecisionNodePrompt() throws Exception {

        context = initiateFlowContext();
        context.setGraphConfig(defaultGraph);
        FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(context);
        assertEquals(step.getFlowStatus(), "INCOMPLETE");
        assertEquals(step.getStepType(), "VIEW");
    }

    @Test(dependsOnMethods = {"testDecisionNodePrompt"})
    public void testDecisionNodeSelectionForPrompt() throws Exception {

        context.setCurrentActionId("button1");
        FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(context);
        assertEquals(step.getFlowStatus(), "INCOMPLETE");
        assertEquals(step.getStepType(), "VIEW");
    }

    @Test(dependsOnMethods = {"testDecisionNodeSelectionForPrompt"})
    public void testContinueAfterPrompt() throws Exception {

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status("INCOMPLETE")
                                 .type("VIEW")
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(context);

            assertNotNull(step);
            assertEquals(step.getFlowStatus(), "INCOMPLETE");
            assertEquals(step.getStepType(), "VIEW");
        }
    }

    @Test(dependsOnMethods = {"testContinueAfterPrompt"})
    public void testContinueTaskExecution() throws Exception {

        try (MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                FlowExecutionEngineUtils.class);
             MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status("COMPLETE")
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            utilsMockedStatic.when(() -> FlowExecutionEngineUtils.resolveCompletionRedirectionUrl(context))
                    .thenReturn("https://localhost:3000/myapp/callback");
            FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(context);

            assertNotNull(step);
            assertEquals(step.getFlowStatus(), "COMPLETE");
        }
    }

    @Test
    public void testRedirectionNodeResponseWithoutURL() throws Exception {

        NodeConfig invalidNode = new NodeConfig.Builder()
                .id("redirectionNode")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("redirectionNode", invalidNode);

        GraphConfig graphWithRedirection = new GraphConfig();
        graphWithRedirection.setFirstNodeId("redirectionNode");
        graphWithRedirection.setNodeConfigs(nodeMap);

        FlowExecutionContext newContext = initiateFlowContext();
        newContext.setGraphConfig(graphWithRedirection);

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(REDIRECTION)
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            FlowExecutionEngine.getInstance().execute(newContext);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_REDIRECTION_URL_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testRedirectionNodeResponse() throws Exception {

        NodeConfig invalidNode = new NodeConfig.Builder()
                .id("redirectionNode")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("redirectionNode", invalidNode);

        GraphConfig graphWithRedirection = new GraphConfig();
        graphWithRedirection.setFirstNodeId("redirectionNode");
        graphWithRedirection.setNodeConfigs(nodeMap);

        FlowExecutionContext newContext = initiateFlowContext();
        newContext.setGraphConfig(graphWithRedirection);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put(REDIRECT_URL, "https://test.com");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(REDIRECTION)
                                 .additionalInfo(additionalTestInfo)
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(newContext);
            assertEquals(step.getFlowStatus(), STATUS_INCOMPLETE);
            assertEquals(step.getStepType(), REDIRECTION);
            assertEquals(step.getData().getRedirectURL(), "https://test.com");
        }
    }

    @Test
    public void testWebAuthnNodeResponse() throws Exception {

        NodeConfig webAuthnNode = new NodeConfig.Builder()
                .id("webAuthnNode")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("webAuthnNode", webAuthnNode);

        GraphConfig graphWithWebAuthn = new GraphConfig();
        graphWithWebAuthn.setFirstNodeId("webAuthnNode");
        graphWithWebAuthn.setNodeConfigs(nodeMap);

        FlowExecutionContext newContext = initiateFlowContext();
        newContext.setGraphConfig(graphWithWebAuthn);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put(WEBAUTHN_DATA, "{\"field1\":\"value1\",\"field2\":\"value2\"}");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(WEBAUTHN)
                                 .additionalInfo(additionalTestInfo)
                                 .requiredData(Arrays.asList("username", "email"))
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(newContext);
            assertEquals(step.getFlowStatus(), STATUS_INCOMPLETE);
            assertEquals(step.getStepType(), WEBAUTHN);
            assertEquals(step.getData().getRequiredParams().size(), 2);
            assertEquals(step.getData().getWebAuthnData().size(), 2);
            assertEquals(step.getData().getWebAuthnData().get("field1"), "value1");
        }
    }

    @Test
    public void testWebAuthnNodeResponseWithoutWebAuthnData() throws Exception {

        NodeConfig webAuthnNode = new NodeConfig.Builder()
                .id("webAuthnNodeWithoutData")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("webAuthnNodeWithoutData", webAuthnNode);

        GraphConfig graphWithWebAuthn = new GraphConfig();
        graphWithWebAuthn.setFirstNodeId("webAuthnNodeWithoutData");
        graphWithWebAuthn.setNodeConfigs(nodeMap);

        FlowExecutionContext newContext = initiateFlowContext();
        newContext.setGraphConfig(graphWithWebAuthn);

        Map<String, String> emptyAdditionalInfo = new HashMap<>();

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(WEBAUTHN)
                                 .additionalInfo(emptyAdditionalInfo)
                                 .requiredData(Arrays.asList("username", "email"))
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            FlowExecutionEngine.getInstance().execute(newContext);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_WEBAUTHN_DATA_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testInternalPromptResponse() throws Exception {

        NodeConfig promptNode = new NodeConfig.Builder()
                .id("internalPromptNode")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("internalPromptNode", promptNode);

        GraphConfig graphWithPrompt = new GraphConfig();
        graphWithPrompt.setFirstNodeId("internalPromptNode");
        graphWithPrompt.setNodeConfigs(nodeMap);

        FlowExecutionContext newContext = initiateFlowContext();
        newContext.setGraphConfig(graphWithPrompt);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put("someKey", "someValue");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(INTERNAL_PROMPT)
                                 .additionalInfo(additionalTestInfo)
                                 .requiredData(Arrays.asList("firstName", "lastName", "password"))
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(newContext);
            assertEquals(step.getFlowStatus(), STATUS_INCOMPLETE);
            assertEquals(step.getStepType(), INTERNAL_PROMPT);
            assertEquals(step.getData().getRequiredParams().size(), 3);
            assertEquals(step.getData().getAdditionalData().get("someKey"), "someValue");
        }
    }

    @Test
    public void testInternalPromptResponseWithoutRequiredData() throws Exception {

        NodeConfig promptNode = new NodeConfig.Builder()
                .id("internalPromptNodeNoData")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("internalPromptNodeNoData", promptNode);

        GraphConfig graphWithPrompt = new GraphConfig();
        graphWithPrompt.setFirstNodeId("internalPromptNodeNoData");
        graphWithPrompt.setNodeConfigs(nodeMap);

        FlowExecutionContext newContext = initiateFlowContext();
        newContext.setGraphConfig(graphWithPrompt);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put("someKey", "someValue");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         NodeResponse nodeResponse = new NodeResponse.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(INTERNAL_PROMPT)
                                 .additionalInfo(additionalTestInfo)
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(nodeResponse);
                     })) {

            FlowExecutionEngine.getInstance().execute(newContext);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_REQUIRED_DATA_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testUnsupportedNodeType() throws Exception {

        NodeConfig invalidNode = new NodeConfig.Builder()
                .id("nodeId")
                .type("UNSUPPORTED_NODE_TYPE")
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("nodeId", invalidNode);

        GraphConfig invalidGraph = new GraphConfig();
        invalidGraph.setFirstNodeId("nodeId");
        invalidGraph.setNodeConfigs(nodeMap);

        FlowExecutionContext context = initiateFlowContext();
        context.setGraphConfig(invalidGraph);

        try {
            FlowExecutionEngine.getInstance().execute(context);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNSUPPORTED_NODE.getCode());
        }
    }

    @Test
    public void testFirstNodeNotFound() throws Exception {

        context = initiateFlowContext();
        String firstNodeId = defaultGraph.getFirstNodeId();
        defaultGraph.setFirstNodeId(null);
        context.setGraphConfig(defaultGraph);
        try {
            FlowExecutionEngine.getInstance().execute(context);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_FIRST_NODE_NOT_FOUND.getCode());
        }
        // Set the first node id back.
        defaultGraph.setFirstNodeId(firstNodeId);
    }

    @Test
    public void testEndNodeCompletion() throws Exception {

        // Create END node config.
        NodeConfig endNode = new NodeConfig.Builder()
                .id(org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID)
                .type(PROMPT_ONLY)
                .build();

        // Create page mapping for END node.
        org.wso2.carbon.identity.flow.mgt.model.StepDTO endPage = new org.wso2.carbon.identity.flow.mgt.model.StepDTO.Builder()
                .id(org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID)
                .data(new org.wso2.carbon.identity.flow.mgt.model.DataDTO.Builder().build())
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put(org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID, endNode);
        Map<String, org.wso2.carbon.identity.flow.mgt.model.StepDTO> pageMappings = new HashMap<>();
        pageMappings.put(org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID, endPage);

        GraphConfig graph = new GraphConfig();
        graph.setNodeConfigs(nodeMap);
        graph.setNodePageMappings(pageMappings);
        graph.setFirstNodeId(org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID);

        FlowExecutionContext context = initiateFlowContext();
        context.setGraphConfig(graph);

        try (MockedStatic<org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils> utilsMockedStatic =
                     mockStatic(org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.class)) {
            utilsMockedStatic.when(() -> org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.resolveCompletionRedirectionUrl(context))
                    .thenReturn("https://localhost:3000/myapp/callback");
            FlowExecutionStep step = FlowExecutionEngine.getInstance().execute(context);
            assertNotNull(step);
            assertEquals(step.getFlowStatus(), "COMPLETE");
            assertEquals(step.getStepType(), "REDIRECTION");
            assertNotNull(step.getData());
            assertEquals(step.getData().getRedirectURL(), "https://localhost:3000/myapp/callback");
        }
    }

    private GraphConfig buildGraphWithDecision() {

        NodeConfig decisionNode = new NodeConfig.Builder()
                .id("decisionNode")
                .type(DECISION)
                .build();

        decisionNode.addEdge(new NodeEdge("decisionNode", "promptNode", "button1"));
        decisionNode.addEdge(new NodeEdge("decisionNode", "testTarget", "button2"));

        NodeConfig promptNode = new NodeConfig.Builder()
                .id("promptNode")
                .type(PROMPT_ONLY)
                .build();
        promptNode.addEdge(new NodeEdge("promptNode", "taskNode", "button1"));

        NodeConfig taskNode = new NodeConfig.Builder()
                .id("taskNode")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("decisionNode", decisionNode);
        nodeMap.put("promptNode", promptNode);
        nodeMap.put("taskNode", taskNode);

        Map<String, StepDTO> pageMappings = new HashMap<>();
        pageMappings.put("decisionNode", new StepDTO.Builder().build());
        pageMappings.put("promptNode", new StepDTO.Builder().build());
        pageMappings.put("taskNode", new StepDTO.Builder().build());

        GraphConfig graph = new GraphConfig();
        graph.setNodeConfigs(nodeMap);
        graph.setNodePageMappings(pageMappings);
        graph.setFirstNodeId("decisionNode");
        return graph;
    }

    private FlowExecutionContext initiateFlowContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("test.com");
        context.setContextIdentifier("flow123");
        return context;
    }
}

