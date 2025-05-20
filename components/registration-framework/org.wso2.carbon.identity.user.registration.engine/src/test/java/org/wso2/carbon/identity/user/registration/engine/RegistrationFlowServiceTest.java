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

package org.wso2.carbon.identity.user.registration.engine;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngine;
import org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils;
import org.wso2.carbon.identity.user.registration.engine.validation.InputValidationListener;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.TASK_EXECUTION;

/**
 * Unit tests for RegistrationFlowService.
 */
public class RegistrationFlowServiceTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String TEST_CALLBACK_URL = "https://localhost:3000/myapp/callback";
    private static final String TEST_APPLICATION_ID = "testAppId";
    private RegistrationContext testRegContext;

    @Mock
    private RegistrationFlowEngine engineMock;

    @BeforeClass
    public void setup() {

        MockitoAnnotations.openMocks(this);
        testRegContext = initTestContext();
    }

    private RegistrationContext initTestContext() {

        RegistrationContext context = new RegistrationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setRegGraph(new RegistrationGraphConfig());
        context.setContextIdentifier(UUID.randomUUID().toString());
        return context;
    }

    @Test(expectedExceptions = RegistrationEngineException.class)
    public void testInitiateDefaultRegistrationFlowException() throws Exception {

        try (MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                RegistrationFlowEngineUtils.class)) {
            utilsMockedStatic.when(
                            () -> RegistrationFlowEngineUtils.initiateContext(anyString(), anyString(), anyString()))
                    .thenThrow(new RegistrationEngineException("Failed"));
            UserRegistrationFlowService.getInstance().handleRegistration(TENANT_DOMAIN,
                    TEST_APPLICATION_ID, TEST_CALLBACK_URL,null, null, null);
        }
    }

    @Test(expectedExceptions = RegistrationEngineException.class)
    public void testInitiateDefaultRegistrationFlowExecutionFailure() throws Exception {

        try (MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                RegistrationFlowEngineUtils.class);
             MockedStatic<RegistrationFlowEngine> engineMockedStatic = mockStatic(RegistrationFlowEngine.class)) {
            utilsMockedStatic.when(
                            () -> RegistrationFlowEngineUtils.initiateContext(anyString(), anyString(), anyString()))
                    .thenReturn(testRegContext);
            engineMockedStatic.when(RegistrationFlowEngine::getInstance).thenReturn(engineMock);
            when(engineMock.execute(testRegContext)).thenThrow(RegistrationEngineException.class);
            UserRegistrationFlowService.getInstance().handleRegistration(TENANT_DOMAIN,
                    TEST_APPLICATION_ID, TEST_CALLBACK_URL, null, null, null);
        }
    }

    @Test
    public void testInitiateDefaultRegistrationFlowExecution() throws Exception {

        RegistrationGraphConfig registrationGraphConfig = buildRegistrationGraphWithDecision();
        testRegContext.setRegGraph(registrationGraphConfig);
        RegistrationFlowEngineDataHolder.getInstance().addRegistrationExecutionListeners(new InputValidationListener());
        testRegContext.getUserInputData().put("input1", "value1");

        RegistrationStep expectedStep = new RegistrationStep.Builder()
                .flowId(testRegContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        try (MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                RegistrationFlowEngineUtils.class);
             MockedStatic<RegistrationFlowEngine> engineMockedStatic = mockStatic(RegistrationFlowEngine.class)) {
            utilsMockedStatic.when(
                            () -> RegistrationFlowEngineUtils.initiateContext(anyString(), anyString(), anyString()))
                    .thenReturn(testRegContext);

            engineMockedStatic.when(RegistrationFlowEngine::getInstance).thenReturn(engineMock);

            when(engineMock.execute(testRegContext)).thenReturn(expectedStep);
            RegistrationStep returnedStep =
                    UserRegistrationFlowService.getInstance()
                            .handleRegistration(TENANT_DOMAIN, TEST_APPLICATION_ID, TEST_CALLBACK_URL, null,
                                    null, null);
            assertEquals(returnedStep, expectedStep);
        }
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

    @Test
    public void testContinueFlowIncompleteFlow() throws Exception {

        RegistrationStep expectedStep = new RegistrationStep.Builder()
                .flowId(testRegContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        String flowId = testRegContext.getContextIdentifier();

        try (
                MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                        RegistrationFlowEngineUtils.class);
                MockedStatic<RegistrationFlowEngine> engineMockedStatic = mockStatic(RegistrationFlowEngine.class)
        ) {
            engineMockedStatic.when(RegistrationFlowEngine::getInstance).thenReturn(engineMock);
            utilsMockedStatic.when(() -> RegistrationFlowEngineUtils.retrieveRegContextFromCache(flowId))
                    .thenReturn(testRegContext);
            when(engineMock.execute(testRegContext)).thenReturn(expectedStep);

            UserRegistrationFlowService service = UserRegistrationFlowService.getInstance();
            RegistrationStep result = service.handleRegistration(null, null, null,
                    flowId, null, new HashMap<>());

            assertEquals(result, expectedStep);
            utilsMockedStatic.verify(() -> RegistrationFlowEngineUtils.addRegContextToCache(testRegContext));
        }
    }

    @Test
    public void testContinueFlowWithCompletion() throws Exception {

        RegistrationStep expectedStep = new RegistrationStep.Builder()
                .flowId(testRegContext.getContextIdentifier())
                .flowStatus("COMPLETE")
                .build();

        String flowId = testRegContext.getContextIdentifier();

        try (
                MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                        RegistrationFlowEngineUtils.class);
                MockedStatic<RegistrationFlowEngine> engineMockedStatic = mockStatic(RegistrationFlowEngine.class)
        ) {
            engineMockedStatic.when(RegistrationFlowEngine::getInstance).thenReturn(engineMock);
            utilsMockedStatic.when(() -> RegistrationFlowEngineUtils.retrieveRegContextFromCache(flowId))
                    .thenReturn(testRegContext);
            when(engineMock.execute(testRegContext)).thenReturn(expectedStep);

            UserRegistrationFlowService service = UserRegistrationFlowService.getInstance();
            RegistrationStep result = service.handleRegistration(null, null, null,
                    flowId, null, new HashMap<>());

            assertEquals(result, expectedStep);
            utilsMockedStatic.verify(() -> RegistrationFlowEngineUtils.removeRegContextFromCache(flowId));
        }
    }

    @Test(expectedExceptions = RegistrationEngineException.class)
    public void testContinueFlowThrowsException() throws Exception {

        String flowId = "flowXYZ";
        Map<String, String> inputMap = new HashMap<>();

        try (MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                RegistrationFlowEngineUtils.class)
        ) {
            utilsMockedStatic.when(() -> RegistrationFlowEngineUtils.retrieveRegContextFromCache(flowId))
                    .thenThrow(new RegistrationEngineException("Failed"));
            UserRegistrationFlowService.getInstance().handleRegistration(null, null, null,
                    flowId, "actionId", inputMap);
        }
    }
}
