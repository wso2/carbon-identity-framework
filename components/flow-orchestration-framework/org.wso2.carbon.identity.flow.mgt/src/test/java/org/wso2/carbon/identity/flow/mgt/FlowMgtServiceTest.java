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

package org.wso2.carbon.identity.flow.mgt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.internal.FlowMgtServiceDataHolder;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;
import org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverService;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.OrgResourceResolverServiceImpl;
import org.wso2.carbon.identity.organization.resource.hierarchy.traverse.service.util.OrgResourceHierarchyTraverseUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.flow.mgt.Constants.ActionTypes.EXECUTOR;
import static org.wso2.carbon.identity.flow.mgt.Constants.ActionTypes.NEXT;
import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.BUTTON;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_ACTION_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_COMPONENT_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_INFO_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_FOR_BUTTON;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_TYPE;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_FIRST_NODE;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_NEXT_STEP;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_MULTIPLE_STEP_EXECUTORS;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_NEXT_ACTION_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_STEP_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ACTION_TYPE;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_STEP_TYPE;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.USER_ONBOARD;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.WEBAUTHN;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.closeH2Database;
import static org.wso2.carbon.identity.flow.mgt.TestHelperMethods.getFilePath;

/**
 * Test class for FlowMgtService.
 */
@WithCarbonHome
public class FlowMgtServiceTest {

    private static final String DB_NAME = "flow_mgt_dao_db";
    private static final String DB_SCRIPT = "identity.sql";
    private static final String FLOW_JSON = "flow.json";
    private static final String FLOW_WITH_END_NODE_JSON = "flowWithEndNode.json";
    private static final int TEST_TENANT_ID = -1234;
    private static final String TEST_TENANT_DOMAIN = "testTenant";
    private static final String TEST_ORG_ID = "testOrgId";
    private static BasicDataSource dataSource = null;
    private FlowMgtService service;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;
    private MockedStatic<FlowMgtServiceDataHolder> dataHolderMockedStatic;
    private MockedStatic<OrgResourceHierarchyTraverseUtil> traverseUtilMockedStatic;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;

    @BeforeClass
    public void setUp() throws Exception {

        service = FlowMgtService.getInstance();
        dataSource = TestHelperMethods.initiateH2Database(getFilePath(DB_SCRIPT), DB_NAME);
        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantDomain(TEST_TENANT_ID))
                .thenReturn(TEST_TENANT_DOMAIN);
        identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                .thenReturn(TEST_TENANT_ID);
        OrganizationManager mockOrganizationManager = mock(OrganizationManager.class);
        OrgResourceResolverService mockOrgResourceResolverService = new OrgResourceResolverServiceImpl();
        FlowMgtServiceDataHolder mockFlowMgtServiceDataHolder = mock(FlowMgtServiceDataHolder.class);
        when(mockOrganizationManager.resolveOrganizationId(TEST_TENANT_DOMAIN)).thenReturn(TEST_ORG_ID);
        when(mockOrganizationManager.resolveTenantDomain(TEST_ORG_ID)).thenReturn(TEST_TENANT_DOMAIN);
        when(mockOrganizationManager.getAncestorOrganizationIds(TEST_ORG_ID))
                .thenReturn(Collections.singletonList(TEST_ORG_ID));
        when(mockFlowMgtServiceDataHolder.getOrganizationManager()).thenReturn(mockOrganizationManager);
        when(mockFlowMgtServiceDataHolder.getOrgResourceResolverService()).thenReturn(mockOrgResourceResolverService);
        dataHolderMockedStatic = mockStatic(FlowMgtServiceDataHolder.class);
        dataHolderMockedStatic.when(FlowMgtServiceDataHolder::getInstance).thenReturn(mockFlowMgtServiceDataHolder);
        traverseUtilMockedStatic = mockStatic(OrgResourceHierarchyTraverseUtil.class);
        traverseUtilMockedStatic.when(OrgResourceHierarchyTraverseUtil::getOrganizationManager)
                .thenReturn(mockOrganizationManager);
        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                .thenReturn(dataSource);
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database(dataSource);
        if (identityTenantUtilMockedStatic != null) {
            identityTenantUtilMockedStatic.close();
        }
        if (dataHolderMockedStatic != null) {
            dataHolderMockedStatic.close();
        }
        if (traverseUtilMockedStatic != null) {
            traverseUtilMockedStatic.close();
        }
        if (identityDatabaseUtilMockedStatic != null) {
            identityDatabaseUtilMockedStatic.close();
        }
    }

    @Test
    public void testUpdateFlow() throws Exception {

        try (MockedStatic<FlowMgtUtils> flowMgtUtils = mockStatic(FlowMgtUtils.class);
             MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {

            loggerUtils.when(() -> LoggerUtils.triggerAuditLogEvent(any())).thenAnswer(inv -> null);
            flowMgtUtils.when(FlowMgtUtils::getInitiatorId).thenReturn(LoggerUtils.Initiator.System.name());

            service.updateFlow(createSampleGraphConfig(), TEST_TENANT_ID);
        }
    }

    @Test(dependsOnMethods = {"testUpdateFlow"})
    public void testGetFlow() throws Exception {

        FlowDTO flowDTO = service.getFlow("SELF_REGISTRATION", TEST_TENANT_ID);
        assertNotNull(flowDTO);
        assertEquals(flowDTO.getSteps().size(), 4);
    }

    @Test(dependsOnMethods = {"testUpdateFlow"})
    public void testGetRegistrationGraph() throws Exception {

        GraphConfig regGraph = service.getGraphConfig("SELF_REGISTRATION", TEST_TENANT_ID);
        assertNotNull(regGraph);
        assertEquals(regGraph.getNodeConfigs().size(), 5);
        assertEquals(regGraph.getFirstNodeId(), "step_1");
        assertEquals(regGraph.getNodeConfigs().get("step_1").getType(), DECISION);
    }

    @Test(dependsOnMethods = {"testGetFlow", "testGetRegistrationGraph"})
    public void testUpdateFlowWithEnd() throws Exception {

        try (MockedStatic<FlowMgtUtils> flowMgtUtils = mockStatic(FlowMgtUtils.class);
             MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {

            loggerUtils.when(() -> LoggerUtils.triggerAuditLogEvent(any())).thenAnswer(inv -> null);
            flowMgtUtils.when(FlowMgtUtils::getInitiatorId).thenReturn(LoggerUtils.Initiator.System.name());

            service.updateFlow(createSampleGraphConfigWithEndNode(), TEST_TENANT_ID);
        }
    }

    @Test(dependsOnMethods = {"testUpdateFlowWithEnd"})
    public void testGetFlowWithEnd() throws Exception {

        FlowDTO flowDTO = service.getFlow("SELF_REGISTRATION", TEST_TENANT_ID);
        assertNotNull(flowDTO);
        assertEquals(flowDTO.getSteps().size(), 2);
    }

    @DataProvider(name = "invalidStepData")
    public Object[][] invalidStepData() {

        return new Object[][]{
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
            FlowDTO flowDTO = new FlowDTO();
            flowDTO.setSteps(Collections.singletonList(stepDTO));
            service.updateFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected exception not thrown: " + name);
        } catch (FlowMgtFrameworkException e) {
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
            FlowDTO flowDTO = new FlowDTO();
            flowDTO.setSteps(Collections.singletonList(step));
            service.updateFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected OrchestrationFrameworkException not thrown");
        } catch (FlowMgtFrameworkException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_MULTIPLE_STEP_EXECUTORS.getCode());
        }
    }

    @Test
    public void testUnsupportedStepType() {

        FlowDTO flowDTO = new FlowDTO();
        StepDTO step = new StepDTO.Builder().id("unsupported_step").type("UNKNOWN").build();
        flowDTO.setSteps(Collections.singletonList(step));
        try {
            service.updateFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected OrchestrationFrameworkException not thrown");
        } catch (FlowMgtFrameworkException e) {
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
        FlowDTO flowDTO = new FlowDTO();
        flowDTO.setSteps(steps);

        try {
            service.updateFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected to have multiple first node exception. But it is not thrown.");
        } catch (FlowMgtFrameworkException e) {
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
        FlowDTO flowDTO = new FlowDTO();
        flowDTO.setSteps(steps);
        try {
            service.updateFlow(flowDTO, TEST_TENANT_ID);
            fail("Expected to have first node undefined. But it is not thrown.");
        } catch (FlowMgtFrameworkException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_INVALID_FIRST_NODE.getCode());
        }
    }

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

        Map<String, String> metadata = new HashMap<>();
        metadata.put(Constants.IDP_NAME, "testIdp");
        return new ActionDTO.Builder()
                .type(EXECUTOR)
                .nextId(nextId)
                .executor(new ExecutorDTO("testExecutor", metadata))
                .build();
    }

    private static FlowDTO createSampleGraphConfig() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            FlowDTO flowDTO = objectMapper.readValue(new File(getFilePath(FLOW_JSON)), FlowDTO.class);
            flowDTO.setFlowType("SELF_REGISTRATION");
            return flowDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error while reading the JSON file.", e);
        }
    }

    private static FlowDTO createSampleGraphConfigWithEndNode() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            FlowDTO flowDTO = objectMapper.readValue(new File(getFilePath(FLOW_WITH_END_NODE_JSON)), FlowDTO.class);
            flowDTO.setFlowType("SELF_REGISTRATION");
            return flowDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error while reading the JSON file.", e);
        }
    }
}
