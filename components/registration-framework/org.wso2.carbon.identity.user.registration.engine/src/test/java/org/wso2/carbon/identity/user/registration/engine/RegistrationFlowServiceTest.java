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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngine;
import org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import static org.testng.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RegistrationFlowService.
 */
public class RegistrationFlowServiceTest {

    private static final String TENANT_DOMAIN = "test.com";
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
            utilsMockedStatic.when(() -> RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN))
                    .thenThrow(new RegistrationEngineException("Failed"));
            UserRegistrationFlowService.getInstance().initiateDefaultRegistrationFlow(TENANT_DOMAIN);
        }
    }

    @Test(expectedExceptions = RegistrationEngineException.class)
    public void testInitiateDefaultRegistrationFlowExecutionFailure() throws Exception {

        try (MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                RegistrationFlowEngineUtils.class);
             MockedStatic<RegistrationFlowEngine> engineMockedStatic = mockStatic(RegistrationFlowEngine.class)) {
            utilsMockedStatic.when(() -> RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN))
                    .thenReturn(testRegContext);
            engineMockedStatic.when(RegistrationFlowEngine::getInstance).thenReturn(engineMock);
            when(engineMock.execute(testRegContext)).thenThrow(RegistrationEngineException.class);
            UserRegistrationFlowService.getInstance().initiateDefaultRegistrationFlow(TENANT_DOMAIN);
        }
    }

    @Test
    public void testInitiateDefaultRegistrationFlowExecution() throws Exception {

        RegistrationStep expectedStep = new RegistrationStep.Builder()
                .flowId(testRegContext.getContextIdentifier())
                .flowStatus("INCOMPLETE")
                .stepType("VIEW")
                .data(new DataDTO.Builder().components(new ArrayList<>()).url(StringUtils.EMPTY).build())
                .build();

        try (MockedStatic<RegistrationFlowEngineUtils> utilsMockedStatic = mockStatic(
                RegistrationFlowEngineUtils.class);
             MockedStatic<RegistrationFlowEngine> engineMockedStatic = mockStatic(RegistrationFlowEngine.class)) {
            utilsMockedStatic.when(() -> RegistrationFlowEngineUtils.initiateContext(TENANT_DOMAIN))
                    .thenReturn(testRegContext);

            engineMockedStatic.when(RegistrationFlowEngine::getInstance).thenReturn(engineMock);

            when(engineMock.execute(testRegContext)).thenReturn(expectedStep);
            RegistrationStep returnedStep =
                    UserRegistrationFlowService.getInstance().initiateDefaultRegistrationFlow(TENANT_DOMAIN);
            assertEquals(returnedStep, expectedStep);
        }
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
            RegistrationStep result = service.continueFlow(flowId, null, new HashMap<>());

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
            RegistrationStep result = service.continueFlow(flowId, null, new HashMap<>());

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
            UserRegistrationFlowService.getInstance().continueFlow(flowId, "actionId", inputMap);
        }
    }
}
