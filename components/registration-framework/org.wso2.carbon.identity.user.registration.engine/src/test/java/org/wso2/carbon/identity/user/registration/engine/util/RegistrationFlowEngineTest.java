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

package org.wso2.carbon.identity.user.registration.engine.util;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.user.registration.engine.listener.FlowExecutionListener;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.engine.validation.InputValidationListener;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

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
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_FIRST_NODE_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_INTERACTION_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_REDIRECTION_URL_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_REQUIRED_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.INTERACTION_DATA;
import static org.wso2.carbon.identity.user.registration.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.TASK_EXECUTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.INTERACT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.INTERNAL_PROMPT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;

/**
 * Unit tests for RegistrationFlowEngine.
 */
public class RegistrationFlowEngineTest {

    private RegistrationContext context;
    private RegistrationGraphConfig defaultGraph;
    private List<FlowExecutionListener> listeners = new ArrayList<>();

    @BeforeClass
    public void setup() {

        defaultGraph = buildRegistrationGraphWithDecision();
        listeners.add(new InputValidationListener());
    }

    @Test
    public void testDecisionNodePrompt() throws Exception {

        context = initiateRegistrationContext();
        context.setRegGraph(defaultGraph);
        RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context);
        assertEquals(step.getFlowStatus(), "INCOMPLETE");
        assertEquals(step.getStepType(), "VIEW");
    }

    @Test(dependsOnMethods = {"testDecisionNodePrompt"})
    public void testDecisionNodeSelectionForPrompt() throws Exception {

        context.setCurrentActionId("button1");
        RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context);
        assertEquals(step.getFlowStatus(), "INCOMPLETE");
        assertEquals(step.getStepType(), "VIEW");
    }

    @Test(dependsOnMethods = {"testDecisionNodeSelectionForPrompt"})
    public void testContinueAfterPrompt() throws Exception {

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status("INCOMPLETE")
                                 .type("VIEW")
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context);

            assertNotNull(step);
            assertEquals(step.getFlowStatus(), "INCOMPLETE");
            assertEquals(step.getStepType(), "VIEW");
        }
    }

    @Test(dependsOnMethods = {"testContinueAfterPrompt"})
    public void testContinueTaskExecution() throws Exception {

        try (MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                RegistrationFlowEngineUtils.class);
             MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status("COMPLETE")
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            utilsMockedStatic.when(() -> RegistrationFlowEngineUtils.resolveCompletionRedirectionUrl(context))
                    .thenReturn("https://localhost:3000/myapp/callback");
            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(context);

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

        RegistrationGraphConfig graphWithRedirection = new RegistrationGraphConfig();
        graphWithRedirection.setFirstNodeId("redirectionNode");
        graphWithRedirection.setNodeConfigs(nodeMap);

        RegistrationContext newContext = initiateRegistrationContext();
        newContext.setRegGraph(graphWithRedirection);

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(REDIRECTION)
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            RegistrationFlowEngine.getInstance().execute(newContext);
        } catch (RegistrationEngineServerException e) {
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

        RegistrationGraphConfig graphWithRedirection = new RegistrationGraphConfig();
        graphWithRedirection.setFirstNodeId("redirectionNode");
        graphWithRedirection.setNodeConfigs(nodeMap);

        RegistrationContext newContext = initiateRegistrationContext();
        newContext.setRegGraph(graphWithRedirection);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put(REDIRECT_URL, "https://test.com");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(REDIRECTION)
                                 .additionalInfo(additionalTestInfo)
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(newContext);
            assertEquals(step.getFlowStatus(), STATUS_INCOMPLETE);
            assertEquals(step.getStepType(), REDIRECTION);
            assertEquals(step.getData().getUrl(), "https://test.com");
        }
    }

    @Test
    public void testInteractionNodeResponse() throws Exception {

        NodeConfig interactionNode = new NodeConfig.Builder()
                .id("interactionNode")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("interactionNode", interactionNode);

        RegistrationGraphConfig graphWithInteraction = new RegistrationGraphConfig();
        graphWithInteraction.setFirstNodeId("interactionNode");
        graphWithInteraction.setNodeConfigs(nodeMap);

        RegistrationContext newContext = initiateRegistrationContext();
        newContext.setRegGraph(graphWithInteraction);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put(INTERACTION_DATA, "{\"field1\":\"value1\",\"field2\":\"value2\"}");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(INTERACT)
                                 .additionalInfo(additionalTestInfo)
                                 .requiredData(Arrays.asList("username", "email"))
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(newContext);
            assertEquals(step.getFlowStatus(), STATUS_INCOMPLETE);
            assertEquals(step.getStepType(), INTERACT);
            assertEquals(step.getData().getRequiredParams().size(), 2);
            assertEquals(step.getData().getAdditionalData().get(INTERACTION_DATA),
                    "{\"field1\":\"value1\",\"field2\":\"value2\"}");
        }
    }

    @Test
    public void testInteractionNodeResponseWithoutInteractionData() throws Exception {

        NodeConfig interactionNode = new NodeConfig.Builder()
                .id("interactionNodeWithoutData")
                .type(TASK_EXECUTION)
                .build();

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        nodeMap.put("interactionNodeWithoutData", interactionNode);

        RegistrationGraphConfig graphWithInteraction = new RegistrationGraphConfig();
        graphWithInteraction.setFirstNodeId("interactionNodeWithoutData");
        graphWithInteraction.setNodeConfigs(nodeMap);

        RegistrationContext newContext = initiateRegistrationContext();
        newContext.setRegGraph(graphWithInteraction);

        Map<String, String> emptyAdditionalInfo = new HashMap<>();

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(INTERACT)
                                 .additionalInfo(emptyAdditionalInfo)
                                 .requiredData(Arrays.asList("username", "email"))
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            RegistrationFlowEngine.getInstance().execute(newContext);
        } catch (RegistrationEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_INTERACTION_DATA_NOT_FOUND.getCode());
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

        RegistrationGraphConfig graphWithPrompt = new RegistrationGraphConfig();
        graphWithPrompt.setFirstNodeId("internalPromptNode");
        graphWithPrompt.setNodeConfigs(nodeMap);

        RegistrationContext newContext = initiateRegistrationContext();
        newContext.setRegGraph(graphWithPrompt);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put("someKey", "someValue");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(INTERNAL_PROMPT)
                                 .additionalInfo(additionalTestInfo)
                                 .requiredData(Arrays.asList("firstName", "lastName", "password"))
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            RegistrationStep step = RegistrationFlowEngine.getInstance().execute(newContext);
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

        RegistrationGraphConfig graphWithPrompt = new RegistrationGraphConfig();
        graphWithPrompt.setFirstNodeId("internalPromptNodeNoData");
        graphWithPrompt.setNodeConfigs(nodeMap);

        RegistrationContext newContext = initiateRegistrationContext();
        newContext.setRegGraph(graphWithPrompt);

        Map<String, String> additionalTestInfo = new HashMap<>();
        additionalTestInfo.put("someKey", "someValue");

        try (MockedConstruction<TaskExecutionNode> mocked =
                     mockConstruction(TaskExecutionNode.class, (mock, context) -> {
                         Response response = new Response.Builder()
                                 .status(STATUS_INCOMPLETE)
                                 .type(INTERNAL_PROMPT)
                                 .additionalInfo(additionalTestInfo)
                                 .build();
                         when(mock.execute(any(), any())).thenReturn(response);
                     })) {

            RegistrationFlowEngine.getInstance().execute(newContext);
        } catch (RegistrationEngineServerException e) {
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

        RegistrationGraphConfig invalidGraph = new RegistrationGraphConfig();
        invalidGraph.setFirstNodeId("nodeId");
        invalidGraph.setNodeConfigs(nodeMap);

        RegistrationContext context = initiateRegistrationContext();
        context.setRegGraph(invalidGraph);

        try {
            RegistrationFlowEngine.getInstance().execute(context);
        } catch (RegistrationEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNSUPPORTED_NODE.getCode());
        }
    }

    @Test
    public void testFirstNodeNotFound() throws Exception {

        context = initiateRegistrationContext();
        String firstNodeId = defaultGraph.getFirstNodeId();
        defaultGraph.setFirstNodeId(null);
        context.setRegGraph(defaultGraph);
        try {
            RegistrationFlowEngine.getInstance().execute(context);
        } catch (RegistrationEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_FIRST_NODE_NOT_FOUND.getCode());
        }
        // Set the first node id back.
        defaultGraph.setFirstNodeId(firstNodeId);
    }

    private RegistrationGraphConfig buildRegistrationGraphWithDecision() {

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

        RegistrationGraphConfig graph = new RegistrationGraphConfig();
        graph.setNodeConfigs(nodeMap);
        graph.setNodePageMappings(pageMappings);
        graph.setFirstNodeId("decisionNode");
        return graph;
    }

    private RegistrationContext initiateRegistrationContext() {

        RegistrationContext context = new RegistrationContext();
        context.setTenantDomain("test.com");
        context.setContextIdentifier("flow123");
        return context;
    }
}
