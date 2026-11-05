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

package org.wso2.carbon.identity.flow.execution.engine;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.flow.execution.engine.core.FlowExecutionEngine;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.model.OTFIRetention;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.RETAIN_OTFI;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.TASK_EXECUTION;

/**
 * Unit tests for single use and retained one time flow ids in FlowExecutionService.
 */
public class RetainedFlowIdTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String FLOW_TYPE = "REGISTRATION";
    private static final String NODE_ID = "device-node";
    private static final String EXECUTOR_NAME = "DeviceRegistrationExecutor";

    private final FlowExecutionEngine engineMock = mock(FlowExecutionEngine.class);
    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMockedStatic;

    @BeforeClass
    public void setup() {

        frameworkServiceDataHolderMockedStatic = mockStatic(FrameworkServiceDataHolder.class);
        FrameworkServiceDataHolder frameworkServiceDataHolder = mock(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMockedStatic.when(FrameworkServiceDataHolder::getInstance)
                .thenReturn(frameworkServiceDataHolder);
        when(frameworkServiceDataHolder.getIdentityEventService()).thenReturn(mock(IdentityEventService.class));
        System.setProperty(CarbonBaseConstants.CARBON_HOME, this.getClass().getResource("/").getFile());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
    }

    @BeforeMethod
    public void resetMocks() {

        reset(engineMock);
    }

    @AfterClass
    public void teardown() {

        frameworkServiceDataHolderMockedStatic.close();
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testDefaultOneTimeFlowIdIsSingleUse() throws Exception {

        FlowExecutionContext context = newContext();
        String flowId = context.getContextIdentifier();
        String otfi = UUID.randomUUID().toString();

        try (MockedStatic<FlowExecutionEngineUtils> utils = mockStatic(FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engine = mockStatic(FlowExecutionEngine.class)) {
            engine.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);

            // A non retained OTFI: the context is added under the OTFI and kept under the flow id.
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
            when(engineMock.execute(context)).thenAnswer(invocation -> {
                context.setProperty(OTFI, otfi);
                return incompleteStep(flowId);
            });
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                    new HashMap<>());
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(otfi, context));
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(flowId), never());
            assertFalse(context.getProperties().containsKey(OTFI));

            // A request with the OTFI removes it and continues under the flow id.
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
            when(engineMock.execute(context)).thenReturn(incompleteStep(flowId));
            FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null,
                    FLOW_TYPE, new HashMap<>());

            assertEquals(step.getFlowId(), flowId);
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(otfi));
        }
    }

    @Test
    public void testRetainedOneTimeFlowIdSurvivesSeveralRequests() throws Exception {

        FlowExecutionContext context = newContext();
        String flowId = context.getContextIdentifier();
        String otfi = UUID.randomUUID().toString();

        try (MockedStatic<FlowExecutionEngineUtils> utils = mockStatic(FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engine = mockStatic(FlowExecutionEngine.class)) {
            engine.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);

            // The executor retains the OTFI: the context moves from the flow id to the OTFI.
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
            when(engineMock.execute(context)).thenAnswer(invocation -> {
                context.setProperty(OTFI, otfi);
                context.setProperty(RETAIN_OTFI, new OTFIRetention.Builder().executorName(EXECUTOR_NAME).build());
                return incompleteStep(flowId);
            });
            FlowExecutionStep browserStep = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null,
                    flowId, null, FLOW_TYPE, new HashMap<>());
            assertEquals(browserStep.getFlowId(), flowId);
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(otfi, context));
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(flowId));

            // Each request that sets the OTFI again stays under the OTFI.
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
            when(engineMock.execute(context)).thenAnswer(invocation -> {
                context.setProperty(OTFI, otfi);
                return incompleteStep(flowId);
            });
            for (int i = 0; i < 3; i++) {
                FlowExecutionStep deviceStep = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null,
                        otfi, null, FLOW_TYPE, new HashMap<>());
                // The device never learns the flow id.
                assertEquals(deviceStep.getFlowId(), otfi);
            }
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(otfi, context), times(4));
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(otfi), never());
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(context), never());
        }
    }

    @Test
    public void testEndingTheRetainedIdMovesTheFlowBack() throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();
        String flowId = context.getContextIdentifier();

        try (MockedStatic<FlowExecutionEngineUtils> utils = mockStatic(FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engine = mockStatic(FlowExecutionEngine.class)) {
            engine.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
            when(engineMock.execute(context)).thenAnswer(invocation -> {
                context.getProperties().remove(RETAIN_OTFI);
                return incompleteStep(flowId);
            });

            FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null,
                    FLOW_TYPE, new HashMap<>());
            // The device still only gets the OTFI, which no longer continues the flow.
            assertEquals(step.getFlowId(), otfi);
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(context));
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(otfi));
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(anyString(), any()), never());
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(flowId), never());
        }
    }

    @Test
    public void testContinuedRetentionKeepsTheOneTimeFlowIdOfTheRequest() throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();
        String flowId = context.getContextIdentifier();

        try (MockedStatic<FlowExecutionEngineUtils> utils = mockStatic(FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engine = mockStatic(FlowExecutionEngine.class)) {
            engine.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
            // The executor keeps the retention without setting the OTFI again.
            when(engineMock.execute(context)).thenReturn(incompleteStep(flowId));

            FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null,
                    FLOW_TYPE, new HashMap<>());

            assertEquals(step.getFlowId(), otfi);
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(otfi, context));
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(context), never());
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(anyString()), never());
        }
    }

    @Test
    public void testStartedRetentionWithoutOneTimeFlowIdFails() throws Exception {

        FlowExecutionContext context = newContext();
        String flowId = context.getContextIdentifier();

        try (MockedStatic<FlowExecutionEngineUtils> utils = mockStatic(FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engine = mockStatic(FlowExecutionEngine.class)) {
            engine.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
            utils.when(() -> FlowExecutionEngineUtils.handleServerException(FLOW_TYPE,
                    Constants.ErrorMessages.ERROR_CODE_RETAINED_OTFI_NOT_FOUND, EXECUTOR_NAME, flowId))
                    .thenCallRealMethod();
            when(engineMock.execute(context)).thenAnswer(invocation -> {
                context.setProperty(RETAIN_OTFI, new OTFIRetention.Builder().executorName(EXECUTOR_NAME).build());
                return incompleteStep(flowId);
            });

            try {
                FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                        new HashMap<>());
                fail("Retention without a one time flow id must fail the request.");
            } catch (FlowEngineServerException e) {
                assertEquals(e.getErrorCode(),
                        Constants.ErrorMessages.ERROR_CODE_RETAINED_OTFI_NOT_FOUND.getCode());
            }
            // Nothing is cached, and the error path ends the flow.
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(any(FlowExecutionContext.class)),
                    never());
            utils.verify(() -> FlowExecutionEngineUtils.addFlowContextToCache(anyString(), any()), never());
            utils.verify(() -> FlowExecutionEngineUtils.rollbackContext(FLOW_TYPE, flowId));
        }
    }

    @Test
    public void testErrorWithRetainedIdEndsTheFlow() throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();

        try (MockedStatic<FlowExecutionEngineUtils> utils = mockStatic(FlowExecutionEngineUtils.class);
             MockedStatic<FlowExecutionEngine> engine = mockStatic(FlowExecutionEngine.class)) {
            engine.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
            utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
            when(engineMock.execute(context)).thenThrow(
                    new FlowEngineClientException("DR-60002", "Invalid device.", "Invalid device."));

            try {
                FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null, FLOW_TYPE,
                        new HashMap<>());
                fail("The error of the device must end the flow.");
            } catch (FlowEngineException e) {
                assertEquals(e.getErrorCode(), "DR-60002");
            }
            utils.verify(() -> FlowExecutionEngineUtils.rollbackContext(FLOW_TYPE, otfi));
            utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(otfi));
        }
    }

    private static FlowExecutionContext newContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setContextIdentifier(UUID.randomUUID().toString());
        context.setFlowType(FLOW_TYPE);
        context.setCurrentNode(new NodeConfig.Builder().id(NODE_ID).type(TASK_EXECUTION).build());
        return context;
    }

    private static FlowExecutionContext retainedContext() {

        FlowExecutionContext context = newContext();
        context.setProperty(RETAIN_OTFI, new OTFIRetention.Builder().executorName(EXECUTOR_NAME).build());
        return context;
    }

    private static FlowExecutionStep incompleteStep(String flowId) {

        return new FlowExecutionStep.Builder().flowId(flowId).flowStatus("INCOMPLETE").stepType("VIEW").build();
    }
}
