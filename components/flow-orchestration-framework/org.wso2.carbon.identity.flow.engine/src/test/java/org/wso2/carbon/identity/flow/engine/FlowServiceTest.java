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

package org.wso2.carbon.identity.flow.engine;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.engine.core.FlowEngine;
import org.wso2.carbon.identity.flow.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.engine.internal.FlowEngineDataHolder;
import org.wso2.carbon.identity.flow.engine.model.FlowContext;
import org.wso2.carbon.identity.flow.engine.model.FlowStep;
import org.wso2.carbon.identity.flow.engine.util.FlowEngineUtils;
import org.wso2.carbon.identity.flow.engine.validation.InputValidationListener;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.TASK_EXECUTION;

/**
 * Unit tests for FlowService.
 */
public class FlowServiceTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String TEST_CALLBACK_URL = "https://localhost:3000/myapp/callback";
    private static final String TEST_APPLICATION_ID = "testAppId";
    private static final String FLOW_TYPE = "REGISTRATION";
    private FlowContext testFlowContext;

    @Mock
    private FlowEngine engineMock;

    @BeforeClass
    public void setup() {

        MockitoAnnotations.openMocks(this);
        testFlowContext = initTestContext();
    }

    private FlowContext initTestContext() {

        FlowContext context = new FlowContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setContextIdentifier(UUID.randomUUID().toString());
        context.setFlowType(FLOW_TYPE);
        return context;
    }

    @Test(expectedExceptions = FlowEngineException.class)
    public void testInitiateDefaultFlowException() throws Exception {

        try (MockedStatic<FlowEngineUtils> utilsMockedStatic = mockStatic(
                FlowEngineUtils.class)) {
            utilsMockedStatic.when(
                            () -> FlowEngineUtils.initiateContext(anyString(), anyString(), anyString(),
                                    anyString()))
                    .thenThrow(new FlowEngineException("Failed"));
            FlowService.getInstance().executeFlow(TENANT_DOMAIN,
                    TEST_APPLICATION_ID, TEST_CALLBACK_URL, null, null, FLOW_TYPE, null);
        }
    }

    @Test(expectedExceptions = FlowEngineException.class)
    public void testInitiateDefaultFlowExecutionFailure() throws Exception {

        try (MockedStatic<FlowEngineUtils> utilsMockedStatic = mockStatic(
                FlowEngineUtils.class);
             MockedStatic<FlowEngine> engineMockedStatic = mockStatic(FlowEngine.class)) {
            utilsMockedStatic.when(
                            () -> FlowEngineUtils.initiateContext(anyString(), anyString(), anyString(),
                                    anyString()))
                    .thenReturn(testFlowContext);
            engineMockedStatic.when(FlowEngine::getInstance).thenReturn(engineMock);
            when(engineMock.execute(testFlowContext)).thenThrow(FlowEngineException.class);
            FlowService.getInstance().executeFlow(TENANT_DOMAIN,
                    TEST_APPLICATION_ID, TEST_CALLBACK_URL, null, null, FLOW_TYPE, null);
        }
    }

    @Test
    public void testInitiateDefaultFlowExecution() throws Exception {

        FlowStep expectedStep = new FlowStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        try (MockedStatic<FlowEngineUtils> utilsMockedStatic = mockStatic(
                FlowEngineUtils.class);
             MockedStatic<FlowEngine> engineMockedStatic = mockStatic(FlowEngine.class)) {
            utilsMockedStatic.when(
                            () -> FlowEngineUtils.initiateContext(anyString(), anyString(), anyString(),
                                    anyString()))
                    .thenReturn(testFlowContext);

            engineMockedStatic.when(FlowEngine::getInstance).thenReturn(engineMock);

            when(engineMock.execute(testFlowContext)).thenReturn(expectedStep);
            FlowStep returnedStep =
                    FlowService.getInstance()
                            .executeFlow(TENANT_DOMAIN, TEST_APPLICATION_ID, TEST_CALLBACK_URL, null,
                                    null, FLOW_TYPE, null);
            assertEquals(returnedStep, expectedStep);
        }
    }

    @Test
    public void testInitiateDefaultFlowExecutionWithInput() throws Exception {

        GraphConfig graphConfig = buildGraphWithDecision();
        testFlowContext.setGraphConfig(graphConfig);
        FlowEngineDataHolder.getInstance().addFlowExecutionListeners(new InputValidationListener());
        Map<String, String> userInputMap = new HashMap<>();
        userInputMap.put("input1", "value1");
        testFlowContext.getUserInputData().putAll(userInputMap);

        FlowStep expectedStep = new FlowStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        try (MockedStatic<FlowEngineUtils> utilsMockedStatic = mockStatic(
                FlowEngineUtils.class);
             MockedStatic<FlowEngine> engineMockedStatic = mockStatic(FlowEngine.class)) {
            utilsMockedStatic.when(
                            () -> FlowEngineUtils.initiateContext(anyString(), anyString(), anyString(),
                                    anyString()))
                    .thenReturn(testFlowContext);

            engineMockedStatic.when(FlowEngine::getInstance).thenReturn(engineMock);

            when(engineMock.execute(any(FlowContext.class))).thenReturn(expectedStep);
            FlowStep returnedStep =
                    FlowService.getInstance()
                            .executeFlow(TENANT_DOMAIN, TEST_APPLICATION_ID, TEST_CALLBACK_URL, null,
                                    null, FLOW_TYPE, userInputMap);
            assertEquals(returnedStep, expectedStep);
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

    @Test
    public void testContinueFlowIncompleteFlow() throws Exception {

        FlowStep expectedStep = new FlowStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        String flowId = testFlowContext.getContextIdentifier();

        try (
                MockedStatic<FlowEngineUtils> utilsMockedStatic = mockStatic(
                        FlowEngineUtils.class);
                MockedStatic<FlowEngine> engineMockedStatic = mockStatic(FlowEngine.class)
        ) {
            engineMockedStatic.when(FlowEngine::getInstance).thenReturn(engineMock);
            utilsMockedStatic.when(() -> FlowEngineUtils.retrieveFlowContextFromCache(testFlowContext.getFlowType(),
                            flowId))
                    .thenReturn(testFlowContext);
            when(engineMock.execute(testFlowContext)).thenReturn(expectedStep);

            FlowService service = FlowService.getInstance();
            FlowStep result = service.executeFlow(null, null, null,
                    flowId, null, FLOW_TYPE, new HashMap<>());

            assertEquals(result, expectedStep);
            utilsMockedStatic.verify(() -> FlowEngineUtils.addFlowContextToCache(testFlowContext));
        }
    }

    @Test
    public void testContinueFlowWithCompletion() throws Exception {

        FlowStep expectedStep = new FlowStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .flowStatus("COMPLETE")
                .build();

        String flowId = testFlowContext.getContextIdentifier();

        try (
                MockedStatic<FlowEngineUtils> utilsMockedStatic = mockStatic(
                        FlowEngineUtils.class);
                MockedStatic<FlowEngine> engineMockedStatic = mockStatic(FlowEngine.class)
        ) {
            engineMockedStatic.when(FlowEngine::getInstance).thenReturn(engineMock);
            utilsMockedStatic.when(() -> FlowEngineUtils.retrieveFlowContextFromCache(testFlowContext.getFlowType(),
                            flowId))
                    .thenReturn(testFlowContext);
            when(engineMock.execute(testFlowContext)).thenReturn(expectedStep);

            FlowService service = FlowService.getInstance();
            FlowStep result = service.executeFlow(null, null, null,
                    flowId, null, FLOW_TYPE, new HashMap<>());

            assertEquals(result, expectedStep);
            utilsMockedStatic.verify(() -> FlowEngineUtils.removeFlowContextFromCache(flowId));
        }
    }

    @Test(expectedExceptions = FlowEngineException.class)
    public void testContinueFlowThrowsException() throws Exception {

        String flowId = "flowXYZ";
        Map<String, String> inputMap = new HashMap<>();

        try (MockedStatic<FlowEngineUtils> utilsMockedStatic = mockStatic(
                FlowEngineUtils.class)
        ) {
            utilsMockedStatic.when(() -> FlowEngineUtils.retrieveFlowContextFromCache(FLOW_TYPE, flowId))
                    .thenThrow(new FlowEngineException("Failed"));
            FlowService.getInstance().executeFlow(null, null, null,
                    flowId, "actionId", FLOW_TYPE, inputMap);
        }
    }
}
