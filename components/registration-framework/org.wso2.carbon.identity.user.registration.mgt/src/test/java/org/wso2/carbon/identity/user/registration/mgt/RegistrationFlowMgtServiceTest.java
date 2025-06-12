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

package org.wso2.carbon.identity.user.registration.mgt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.dbcp.BasicDataSource;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

import static org.junit.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ActionTypes.EXECUTOR;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ActionTypes.NEXT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ComponentTypes.BUTTON;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_ACTION_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_COMPONENT_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_INFO_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_FIRST_NODE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_FOR_BUTTON;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_NEXT_STEP;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_MULTIPLE_STEP_EXECUTORS;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_NEXT_ACTION_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_STEP_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ACTION_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_STEP_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.WEBAUTHN;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.USER_ONBOARD;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.user.registration.mgt.TestHelperMethods.closeH2Database;
import static org.wso2.carbon.identity.user.registration.mgt.TestHelperMethods.getFilePath;

/**
 * Test class for RegistrationFlowMgtService.
 */
public class RegistrationFlowMgtServiceTest {

    private static final String DB_NAME = "registration_flow_mgt_dao_db";
    private static final String DB_SCRIPT = "identity.sql";
    private static final String FLOW_JSON = "reg_flow.json";
    private static final int TEST_TENANT_ID = -1234;
    private static BasicDataSource dataSource = null;
    private RegistrationFlowMgtService service;

    @BeforeClass
    public void setUp() throws Exception {

        service = RegistrationFlowMgtService.getInstance();
        dataSource = TestHelperMethods.initiateH2Database(getFilePath(DB_SCRIPT), DB_NAME);
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database(dataSource);
    }

    @Test
    public void testUpdateDefaultRegistrationFlow() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            service.updateDefaultRegistrationFlow(createSampleRegistrationGraphConfig(), TEST_TENANT_ID);
        }
    }

    @Test(dependsOnMethods = {"testUpdateDefaultRegistrationFlow"})
    public void testGetRegistrationFlow() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            RegistrationFlowDTO flowDTO = service.getRegistrationFlow(TEST_TENANT_ID);
            assertNotNull(flowDTO);
            assertEquals(flowDTO.getSteps().size(), 4);
        }
    }

    @Test(dependsOnMethods = {"testUpdateDefaultRegistrationFlow"})
    public void testGetRegistrationGraph() throws Exception {

        try (MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class)) {
            identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);
            RegistrationGraphConfig regGraph = service.getRegistrationGraphConfig(TEST_TENANT_ID);
            assertNotNull(regGraph);
            assertEquals(regGraph.getNodeConfigs().size(), 5);
            assertEquals(regGraph.getFirstNodeId(), "step_1");
            assertEquals(regGraph.getNodeConfigs().get("step_1").getType(), DECISION);
        }
    }

    @DataProvider(name = "invalidStepData")
    public Object[][] invalidStepData() {

        return new Object[][] {
                {"missingData",
                        new StepDTO.Builder().id("STEP_1").type(VIEW).build(),
                        ERROR_CODE_STEP_DATA_NOT_FOUND.getCode()},

                {"missingComponent",
                        createViewStep("STEP_1", null),
                        ERROR_CODE_COMPONENT_DATA_NOT_FOUND.getCode()},

                {"missingButtonAction",
                        createViewStep("STEP_1", Collections.singletonList(createButton(null))),
                        ERROR_CODE_INVALID_ACTION_FOR_BUTTON.getCode()},

                {"missingNextInButtonAction",
                        createViewStep("STEP_1", Collections.singletonList(
                                createButton(new ActionDTO.Builder().type(NEXT).build()))),
                        ERROR_CODE_NEXT_ACTION_NOT_FOUND.getCode()},

                {"unsupportedActionInButton",
                        createViewStep("STEP_1", Collections.singletonList(
                                createButton(new ActionDTO.Builder().type("INVALID_TYPE").nextId("step_2").build()))),
                        ERROR_CODE_UNSUPPORTED_ACTION_TYPE.getCode()},

                {"redirectionStepWithoutData",
                        new StepDTO.Builder().id("STEP_1").type(REDIRECTION).build(),
                        ERROR_CODE_STEP_DATA_NOT_FOUND.getCode()},

                {"redirectionStepWithoutAction",
                        new StepDTO.Builder().id("STEP_1").type(REDIRECTION)
                                .data(new DataDTO.Builder().build()).build(),
                        ERROR_CODE_ACTION_DATA_NOT_FOUND.getCode()},

                {"redirectionStepWithInvalidActionType",
                        createRedirectionStep("STEP_1", new ActionDTO.Builder().type(NEXT).nextId("step_x").build()),
                        ERROR_CODE_INVALID_ACTION_TYPE.getCode()},

                {"redirectionStepWithUndefinedExecutor",
                        createRedirectionStep("STEP_1",
                                              new ActionDTO.Builder().type(EXECUTOR).nextId("step_x").build()),
                        ERROR_CODE_EXECUTOR_INFO_NOT_FOUND.getCode()},

                {"redirectionStepWithInvalidNext",
                        createRedirectionStep("STEP_1", createExecutorAction("step_x")),
                        ERROR_CODE_INVALID_NEXT_STEP.getCode()},

                {"redirectionStepWithoutData",
                        new StepDTO.Builder().id("STEP_1").type(WEBAUTHN).build(),
                        ERROR_CODE_STEP_DATA_NOT_FOUND.getCode()},

                {"interactStepWithoutAction",
                        new StepDTO.Builder().id("STEP_1").type(WEBAUTHN)
                                .data(new DataDTO.Builder().build()).build(),
                        ERROR_CODE_ACTION_DATA_NOT_FOUND.getCode()},
        };
    }

    @Test(dataProvider = "invalidStepData")
    public void testInvalidSteps(String name, StepDTO stepDTO, String expectedErrorCode) {

        try {
            RegistrationFlowDTO flowDTO = new RegistrationFlowDTO();
            flowDTO.setSteps(Collections.singletonList(stepDTO));
            service.updateDefaultRegistrationFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected exception not thrown: " + name);
        } catch (RegistrationFrameworkException e) {
            assertEquals(e.getErrorCode(), expectedErrorCode);
        }
    }

    @Test
    public void testMultipleExecutorsInViewStep() {

        ActionDTO action1 = createExecutorAction("step_2");
        ActionDTO action2 = createExecutorAction("step_2");

        StepDTO step = createViewStep("step_with_multiple_executors",
                                      Arrays.asList(createButton(action1), createButton(action2)));

        try {
            RegistrationFlowDTO flowDTO = new RegistrationFlowDTO();
            flowDTO.setSteps(Collections.singletonList(step));
            service.updateDefaultRegistrationFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected RegistrationFrameworkException not thrown");
        } catch (RegistrationFrameworkException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_MULTIPLE_STEP_EXECUTORS.getCode());
        }
    }

    @Test
    public void testUnsupportedStepType() {

        RegistrationFlowDTO flowDTO = new RegistrationFlowDTO();
        StepDTO step = new StepDTO.Builder().id("unsupported_step").type("UNKNOWN").build();
        flowDTO.setSteps(Collections.singletonList(step));
        try {
            service.updateDefaultRegistrationFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected RegistrationFrameworkException not thrown");
        } catch (RegistrationFrameworkException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNSUPPORTED_STEP_TYPE.getCode());
        }
    }

    @Test
    public void testMultipleFirstNodes() {

        List<StepDTO> steps = Arrays.asList(
                createViewStep("STEP_1", Collections.singletonList(
                        createButton(new ActionDTO.Builder().type(NEXT).nextId("STEP_3").build()))),
                createViewStep("STEP_2", Collections.singletonList(createButton(createExecutorAction("STEP_3")))),
                new StepDTO.Builder().id("STEP_3").type(USER_ONBOARD).build());
        RegistrationFlowDTO flowDTO = new RegistrationFlowDTO();
        flowDTO.setSteps(steps);

        try {
            service.updateDefaultRegistrationFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected to have multiple first node exception. But it is not thrown.");
        } catch (RegistrationFrameworkException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_INVALID_FIRST_NODE.getCode());
        }
    }

    @Test
    public void testFirstNodeNotDefined() {

        List<StepDTO> steps = Arrays.asList(
                createViewStep("STEP_1", Collections.singletonList(
                        createButton(new ActionDTO.Builder().type(NEXT).nextId("STEP_2").build()))),
                createViewStep("STEP_2", Collections.singletonList(
                        createButton(createExecutorAction("STEP_3")))),
                createRedirectionStep("STEP_3", createExecutorAction("STEP_1"))
        );
        RegistrationFlowDTO flowDTO = new RegistrationFlowDTO();
        flowDTO.setSteps(steps);
        try {
            service.updateDefaultRegistrationFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected to have first node undefined. But it is not thrown.");
        } catch (RegistrationFrameworkException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_INVALID_FIRST_NODE.getCode());
        }
    }

    // Helper methods.

    private StepDTO createViewStep(String id, List<ComponentDTO> components) {

        return new StepDTO.Builder()
                .id(id)
                .type(VIEW)
                .data(new DataDTO.Builder().components(components).build())
                .build();
    }

    private StepDTO createRedirectionStep(String id, ActionDTO action) {

        return new StepDTO.Builder()
                .id(id)
                .type(REDIRECTION)
                .data(new DataDTO.Builder().action(action).build())
                .build();
    }

    private ComponentDTO createButton(ActionDTO action) {

        return new ComponentDTO.Builder()
                .id("btn_id")
                .type(BUTTON)
                .action(action)
                .build();
    }

    private ActionDTO createExecutorAction(String nextId) {

        return new ActionDTO.Builder()
                .type(EXECUTOR)
                .nextId(nextId)
                .executor(new ExecutorDTO("testExecutor", "testIDP"))
                .build();
    }

    private static RegistrationFlowDTO createSampleRegistrationGraphConfig() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(new File(getFilePath(FLOW_JSON)), RegistrationFlowDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while reading the JSON file.", e);
        }
    }
}
