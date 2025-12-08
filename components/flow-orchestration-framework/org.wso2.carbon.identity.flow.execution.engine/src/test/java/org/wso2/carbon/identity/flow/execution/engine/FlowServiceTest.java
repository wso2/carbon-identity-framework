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

package org.wso2.carbon.identity.flow.execution.engine;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.UserAssertionUtils;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.flow.execution.engine.core.FlowExecutionEngine;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.execution.engine.validation.InputValidationListener;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.TASK_EXECUTION;

/**
 * Unit tests for FlowExecutionService.
 */
public class FlowServiceTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String TEST_APPLICATION_ID = "testAppId";
    private static final String FLOW_TYPE = "REGISTRATION";
    private static final String DEFAULT_MY_ACCOUNT_URL = "https://localhost:9443/myaccount";
    private FlowExecutionContext testFlowContext;

    @Mock
    private FlowExecutionEngine engineMock;

    private AutoCloseable autoCloseable;

    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMockedStatic;

    @BeforeClass
    public void setup() throws Exception {

        autoCloseable = MockitoAnnotations.openMocks(this);
        testFlowContext = initTestContext();

        ServiceURL serviceURL = mock(ServiceURL.class);
        when(serviceURL.getAbsolutePublicURL()).thenReturn(DEFAULT_MY_ACCOUNT_URL);
        frameworkServiceDataHolderMockedStatic = mockStatic(FrameworkServiceDataHolder.class);

        IdentityEventService mockIdentityEventService = mock(IdentityEventService.class);
        FrameworkServiceDataHolder mockFrameworkServiceDataHolder = mock(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMockedStatic.when(FrameworkServiceDataHolder::getInstance)
                .thenReturn(mockFrameworkServiceDataHolder);
        when(mockFrameworkServiceDataHolder.getIdentityEventService()).thenReturn(mockIdentityEventService);

        System.setProperty(CarbonBaseConstants.CARBON_HOME, this.getClass().getResource("/").getFile());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
    }

    private FlowExecutionContext initTestContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setContextIdentifier(UUID.randomUUID().toString());
        context.setFlowType(FLOW_TYPE);
        return context;
    }

    @Test(expectedExceptions = FlowEngineException.class)
    public void testInitiateDefaultFlowException() throws Exception {

        try (MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                FlowExecutionEngineUtils.class)) {
            utilsMockedStatic.when(
                            () -> FlowExecutionEngineUtils.initiateContext(anyString(), anyString(), anyString()))
                    .thenThrow(new FlowEngineException("Failed"));
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN,
                    TEST_APPLICATION_ID, null, null, FLOW_TYPE, null);
        }
    }

    @Test(expectedExceptions = FlowEngineException.class)
    public void testInitiateDefaultFlowExecutionFailure() throws Exception {

        try (MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engineMockedStatic = mockStatic(FlowExecutionEngine.class)) {
            utilsMockedStatic.when(
                            () -> FlowExecutionEngineUtils.initiateContext(anyString(), anyString(), anyString()))
                    .thenReturn(testFlowContext);
            engineMockedStatic.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
            when(engineMock.execute(testFlowContext)).thenThrow(FlowEngineException.class);
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN,
                    TEST_APPLICATION_ID, null, null, FLOW_TYPE, null);
        }
    }

    @Test
    public void testInitiateDefaultFlowExecution() throws Exception {

        FlowExecutionStep expectedStep = new FlowExecutionStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        try (MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engineMockedStatic = mockStatic(FlowExecutionEngine.class)) {
            utilsMockedStatic.when(
                            () -> FlowExecutionEngineUtils.initiateContext(anyString(), anyString(), anyString()))
                    .thenReturn(testFlowContext);

            engineMockedStatic.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);

            when(engineMock.execute(testFlowContext)).thenReturn(expectedStep);
            FlowExecutionStep returnedStep =
                    FlowExecutionService.getInstance()
                            .executeFlow(TENANT_DOMAIN, TEST_APPLICATION_ID, null, null,
                                    FLOW_TYPE, null);
            assertEquals(returnedStep, expectedStep);
        }
    }

    @Test
    public void testInitiateDefaultFlowExecutionWithInput() throws Exception {

        GraphConfig graphConfig = buildGraphWithDecision();
        testFlowContext.setGraphConfig(graphConfig);
        FlowExecutionEngineDataHolder.getInstance().addFlowExecutionListeners(new InputValidationListener());
        Map<String, String> userInputMap = new HashMap<>();
        userInputMap.put("input1", "value1");
        testFlowContext.getUserInputData().putAll(userInputMap);

        FlowExecutionStep expectedStep = new FlowExecutionStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        try (MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engineMockedStatic = mockStatic(FlowExecutionEngine.class)) {
            utilsMockedStatic.when(
                            () -> FlowExecutionEngineUtils.initiateContext(anyString(), anyString(), anyString()))
                    .thenReturn(testFlowContext);

            engineMockedStatic.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);

            when(engineMock.execute(any(FlowExecutionContext.class))).thenReturn(expectedStep);
            FlowExecutionStep returnedStep =
                    FlowExecutionService.getInstance()
                            .executeFlow(TENANT_DOMAIN, TEST_APPLICATION_ID, null, null,
                                    FLOW_TYPE, userInputMap);
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

        FlowExecutionStep expectedStep = new FlowExecutionStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        String flowId = testFlowContext.getContextIdentifier();

        try (
                MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                        FlowExecutionEngineUtils.class);
                MockedStatic<FlowExecutionEngine> engineMockedStatic = mockStatic(FlowExecutionEngine.class)
        ) {
            engineMockedStatic.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
            utilsMockedStatic.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId))
                    .thenReturn(testFlowContext);
            when(engineMock.execute(testFlowContext)).thenReturn(expectedStep);

            FlowExecutionService service = FlowExecutionService.getInstance();
            FlowExecutionStep result = service.executeFlow(null, null,
                    flowId, null, FLOW_TYPE, new HashMap<>());

            assertEquals(result, expectedStep);
            utilsMockedStatic.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(testFlowContext));
        }
    }

    @Test
    public void testContinueFlowWithCompletion() throws Exception {

        FlowExecutionStep expectedStep = new FlowExecutionStep.Builder()
                .flowId(testFlowContext.getContextIdentifier())
                .data(new DataDTO.Builder().additionalData(new HashMap<>()).build())
                .flowStatus("COMPLETE")
                .build();

        String flowId = testFlowContext.getContextIdentifier();

        try (
                MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                        FlowExecutionEngineUtils.class);
                MockedStatic<FlowExecutionEngine> engineMockedStatic = mockStatic(FlowExecutionEngine.class);
                MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class);
                MockedStatic<UserAssertionUtils> autoLoginAssertionUtilsMockedStatic = mockStatic(
                        UserAssertionUtils.class);
        ) {
            engineMockedStatic.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
            utilsMockedStatic.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId))
                    .thenReturn(testFlowContext);
            identityUtilMockedStatic.when(() -> IdentityUtil.getServerURL(anyString(), anyBoolean(), anyBoolean()))
                    .thenReturn(StringUtils.EMPTY);
            autoLoginAssertionUtilsMockedStatic.when(() -> UserAssertionUtils.
                            generateSignedUserAssertion(any(), anyString())).thenReturn("signedAssertion");
            when(engineMock.execute(testFlowContext)).thenReturn(expectedStep);
            FlowExecutionService service = FlowExecutionService.getInstance();
            FlowExecutionStep result = service.executeFlow(null, null, flowId, null,
                    FLOW_TYPE, new HashMap<>());

            assertEquals(result, expectedStep);
            utilsMockedStatic.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(flowId));
        }
    }

    @Test(expectedExceptions = FlowEngineException.class)
    public void testContinueFlowThrowsException() throws Exception {

        String flowId = "flowXYZ";
        Map<String, String> inputMap = new HashMap<>();

        try (MockedStatic<FlowExecutionEngineUtils> utilsMockedStatic = mockStatic(
                FlowExecutionEngineUtils.class)
        ) {
            utilsMockedStatic.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId))
                    .thenThrow(new FlowEngineException("Failed"));
            FlowExecutionService.getInstance().executeFlow(null, null,
                    flowId, "actionId", FLOW_TYPE, inputMap);
        }
    }

    @AfterClass
    public void teardown() throws Exception {

        if (frameworkServiceDataHolderMockedStatic != null) {
            frameworkServiceDataHolderMockedStatic.close();
        }

        if (autoCloseable != null) {
            autoCloseable.close();
        }
    }
}
