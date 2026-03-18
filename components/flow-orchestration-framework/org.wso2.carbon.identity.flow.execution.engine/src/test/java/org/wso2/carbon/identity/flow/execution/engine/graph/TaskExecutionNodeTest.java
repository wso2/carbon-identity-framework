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

package org.wso2.carbon.identity.flow.execution.engine.graph;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_GET_IDP_CONFIG_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.INTERNAL_PROMPT;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.WEBAUTHN;

/**
 * Test class for TaskExecutionNode.
 */
@WithCarbonHome
public class TaskExecutionNodeTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String TEST_EXECUTOR = "TestExecutor";
    public static final String IDP_NAME = "idpName";
    private TaskExecutionNode taskExecutionNode;
    private FlowExecutionContext context;
    private NodeConfig nodeConfig;
    private AutoCloseable closeable;

    @Mock
    private AuthenticationExecutor executor;

    @BeforeClass
    public void setUp() {

        closeable = MockitoAnnotations.openMocks(this);
        taskExecutionNode = new TaskExecutionNode();

        context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setFlowUser(new FlowUser());

        ExecutorDTO executorDTO = new ExecutorDTO();
        executorDTO.setName(TEST_EXECUTOR);

        List<NodeEdge> edges = new ArrayList<>();
        edges.add(new NodeEdge("edge1", "targetNode", null));

        nodeConfig = new NodeConfig.Builder()
                .id("testNode")
                .type("TASK_EXECUTION")
                .executorConfig(executorDTO)
                .edges(edges)
                .build();
    }

    @AfterClass
    public void tearDown() throws Exception {

        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testExecutorUndefined() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockStatic(
                FlowExecutionEngineDataHolder.class)) {
            FlowExecutionEngineDataHolder dataHolder = mock(FlowExecutionEngineDataHolder.class);
            mocked.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolder);
            when(dataHolder.getExecutors()).thenReturn(new HashMap<>());
            NodeConfig invalidConfig = new NodeConfig.Builder().id("testNode").type("TASK_EXECUTION").build();
            taskExecutionNode.execute(context, invalidConfig);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_EXECUTOR_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testExecutorNotFound() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockStatic(
                FlowExecutionEngineDataHolder.class)) {
            FlowExecutionEngineDataHolder dataHolder = mock(FlowExecutionEngineDataHolder.class);
            mocked.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolder);
            when(dataHolder.getExecutors()).thenReturn(new HashMap<>());
            taskExecutionNode.execute(context, nodeConfig);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNSUPPORTED_EXECUTOR.getCode());
        }
    }

    @Test
    public void testExecutorCompleteStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_COMPLETE);

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            NodeResponse nodeResponse = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(nodeResponse.getStatus(), STATUS_COMPLETE);
        }
    }

    @Test
    public void testExecutorUserCreatedStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_COMPLETE);
        Map<String, Object> updatedClaims = new HashMap<>();
        updatedClaims.put("email", "test@example.com");
        executorResponse.setUpdatedUserClaims(updatedClaims);

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            NodeResponse nodeResponse = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(nodeResponse.getStatus(), STATUS_COMPLETE);
            assertEquals(context.getFlowUser().getClaims().get("email"), "test@example.com");
        }
    }

    @Test
    public void testExecutorRetryStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setRequiredData(new ArrayList<>());
        executorResponse.setErrorMessage("Retry error");

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            NodeResponse nodeResponse = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(nodeResponse.getStatus(), STATUS_INCOMPLETE);
            assertEquals(nodeResponse.getType(), "VIEW");
            assertEquals(nodeResponse.getError(), "Retry error");
        }
    }

    @Test
    public void testExecutorUserInputRequiredStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_USER_INPUT_REQUIRED);
        List<String> requiredData = new ArrayList<>();
        requiredData.add("attribute1");
        requiredData.add("attribute2");
        executorResponse.setRequiredData(requiredData);
        executorResponse.setAdditionalInfo(new HashMap<>());

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            NodeResponse nodeResponse = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(nodeResponse.getStatus(), STATUS_INCOMPLETE);
            assertEquals(nodeResponse.getType(), "VIEW");
            assertNotNull(nodeResponse.getRequiredData());
            assertEquals(nodeResponse.getRequiredData().size(), 2);
        }
    }

    @Test
    public void testExecutorExternalRedirectionStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_EXTERNAL_REDIRECTION);
        executorResponse.setRequiredData(new ArrayList<>());
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("redirectUrl", "https://example.com");
        executorResponse.setAdditionalInfo(additionalInfo);

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            NodeResponse nodeResponse = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(nodeResponse.getStatus(), STATUS_INCOMPLETE);
            assertEquals(nodeResponse.getType(), "REDIRECTION");
            assertEquals(nodeResponse.getAdditionalInfo().get("redirectUrl"), "https://example.com");
        }
    }

    @Test
    public void testExecutorUserErrorStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_USER_ERROR);
        executorResponse.setErrorMessage("User error occurred");

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            taskExecutionNode.execute(context, nodeConfig);
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_FLOW_FAILURE.getCode());
        }
    }

    @Test
    public void testExecutorUnsupportedStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult("UNSUPPORTED_STATUS");

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            taskExecutionNode.execute(context, nodeConfig);
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS.getCode());
        }
    }

    @Test
    public void testIdpConfigBeingNull() throws Exception {

        ExecutorDTO executorDTO = nodeConfig.getExecutorConfig();
        executorDTO.addMetadata(IDP_NAME, "invalidIDP");

        IdentityProviderManager idpManager = mock(IdentityProviderManager.class);

        try (MockedStatic<IdentityProviderManager> idpManagerStatic = mockStatic(IdentityProviderManager.class)) {
            idpManagerStatic.when(IdentityProviderManager::getInstance).thenReturn(idpManager);
            when(idpManager.getIdPByName("invalidIDP", TENANT_DOMAIN)).thenReturn(null);

            try (MockedStatic<FlowExecutionEngineDataHolder> engineMock = mockStatic(
                    FlowExecutionEngineDataHolder.class)) {
                FlowExecutionEngineDataHolder dataHolder = mock(FlowExecutionEngineDataHolder.class);
                engineMock.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolder);
                Map<String, Executor> executorMap = new HashMap<>();
                executorMap.put(TEST_EXECUTOR, executor);
                when(dataHolder.getExecutors()).thenReturn(executorMap);
                taskExecutionNode.execute(context, nodeConfig);
            }
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_GET_IDP_CONFIG_FAILURE.getCode());
        }
    }

    @Test
    public void testExecutorClientInputRequiredStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(Constants.ExecutorStatus.STATUS_CLIENT_INPUT_REQUIRED);
        List<String> requiredData = new ArrayList<>();
        requiredData.add("clientField1");
        requiredData.add("clientField2");
        executorResponse.setRequiredData(requiredData);
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("clientKey", "clientValue");
        executorResponse.setAdditionalInfo(additionalInfo);

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            NodeResponse nodeResponse = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(nodeResponse.getStatus(), STATUS_INCOMPLETE);
            assertEquals(nodeResponse.getType(), INTERNAL_PROMPT);
            assertNotNull(nodeResponse.getRequiredData());
            assertEquals(nodeResponse.getRequiredData().size(), 2);
            assertEquals(nodeResponse.getAdditionalInfo().get("clientKey"), "clientValue");
        }
    }

    @Test
    public void testExecutorWebAuthnStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(Constants.ExecutorStatus.STATUS_WEBAUTHN);
        List<String> requiredData = new ArrayList<>();
        requiredData.add("interactionField1");
        requiredData.add("interactionField2");
        executorResponse.setRequiredData(requiredData);
        Map<String, String> additionalInfo = new HashMap<>();
        additionalInfo.put("interactionData", "{\"form\":\"data\"}");
        executorResponse.setAdditionalInfo(additionalInfo);

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            NodeResponse nodeResponse = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(nodeResponse.getStatus(), STATUS_INCOMPLETE);
            assertEquals(nodeResponse.getType(), WEBAUTHN);
            assertNotNull(nodeResponse.getRequiredData());
            assertEquals(nodeResponse.getRequiredData().size(), 2);
            assertEquals(nodeResponse.getAdditionalInfo().get("interactionData"), "{\"form\":\"data\"}");
        }
    }

    @Test
    public void testIdpConfigRetrieveFailure() throws Exception {

        ExecutorDTO executorDTO = nodeConfig.getExecutorConfig();
        executorDTO.addMetadata(TaskExecutionNodeTest.IDP_NAME, "validIdp");

        IdentityProviderManager idpManager = mock(IdentityProviderManager.class);

        try (MockedStatic<IdentityProviderManager> idpManagerStatic = mockStatic(IdentityProviderManager.class)) {
            idpManagerStatic.when(IdentityProviderManager::getInstance).thenReturn(idpManager);
            when(idpManager.getIdPByName("validIdp", TENANT_DOMAIN)).thenThrow(
                    IdentityProviderManagementException.class);

            try (MockedStatic<FlowExecutionEngineDataHolder> engineMock = mockStatic(
                    FlowExecutionEngineDataHolder.class)) {
                FlowExecutionEngineDataHolder dataHolder = mock(FlowExecutionEngineDataHolder.class);
                engineMock.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolder);
                Map<String, Executor> executorMap = new HashMap<>();
                executorMap.put(TEST_EXECUTOR, executor);
                when(dataHolder.getExecutors()).thenReturn(executorMap);
                taskExecutionNode.execute(context, nodeConfig);
            }
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_GET_IDP_CONFIG_FAILURE.getCode());
        }
    }

    @Test
    public void testIdpConfigRetrieval() throws Exception {

        ExecutorDTO executorDTO = nodeConfig.getExecutorConfig();
        executorDTO.addMetadata(TaskExecutionNodeTest.IDP_NAME, "validIdp");

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_COMPLETE);

        IdentityProvider idp = getTestIdentityProvider();

        IdentityProviderManager idpManager = mock(IdentityProviderManager.class);

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse);
             MockedStatic<IdentityProviderManager> idpManagerStatic = mockStatic(IdentityProviderManager.class)) {
            idpManagerStatic.when(IdentityProviderManager::getInstance).thenReturn(idpManager);
            when(idpManager.getIdPByName("validIdp", TENANT_DOMAIN)).thenReturn(idp);

            taskExecutionNode.execute(context, nodeConfig);
            assertNotNull(context.getExternalIdPConfig());
            assertNotNull(context.getAuthenticatorProperties());
            assertEquals(context.getAuthenticatorProperties().size(), 1);
            assertEquals(context.getAuthenticatorProperties().get("property1"), "value1");
        }
    }

    @Test
    public void testHandleClientExceptionWithErrorCode() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_USER_ERROR);
        executorResponse.setErrorCode("CLIENT_ERROR_001");
        executorResponse.setErrorMessage("Client error occurred");
        executorResponse.setErrorDescription("This is a client-side error");
        executorResponse.setThrowable(new RuntimeException("Test exception"));

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            taskExecutionNode.execute(context, nodeConfig);
        } catch (FlowEngineClientException e) {
            assertEquals(e.getErrorCode(), "CLIENT_ERROR_001");
            assertEquals(e.getMessage(), "Client error occurred");
            assertEquals(e.getDescription(), "This is a client-side error");
            assertNotNull(e.getCause());
        }
    }

    @Test
    public void testHandleServerExceptionWithErrorCode() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_ERROR);
        executorResponse.setErrorCode("SERVER_ERROR_001");
        executorResponse.setErrorMessage("Server error occurred");
        executorResponse.setErrorDescription("This is a server-side error");
        executorResponse.setThrowable(new RuntimeException("Test exception"));

        try (MockedStatic<FlowExecutionEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            taskExecutionNode.execute(context, nodeConfig);
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), "SERVER_ERROR_001");
            assertEquals(e.getMessage(), "Server error occurred");
            assertEquals(e.getDescription(), "This is a server-side error");
            assertNotNull(e.getCause());
        }
    }

    private MockedStatic<FlowExecutionEngineDataHolder> mockExecutorResponseFlow(ExecutorResponse executorResponse)
            throws Exception {

        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put(TEST_EXECUTOR, executor);

        when(executor.execute(any())).thenReturn(executorResponse);
        when(executor.getName()).thenReturn(TEST_EXECUTOR);
        doCallRealMethod().when(executor).addIdpConfigsToContext(any(), any());

        MockedStatic<FlowExecutionEngineDataHolder> mocked = mockStatic(FlowExecutionEngineDataHolder.class);
        FlowExecutionEngineDataHolder dataHolder = mock(FlowExecutionEngineDataHolder.class);
        mocked.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getExecutors()).thenReturn(executorMap);

        return mocked;
    }

    private IdentityProvider getTestIdentityProvider() {

        Property[] properties = new Property[1];
        Property property = new Property();
        property.setName("property1");
        property.setValue("value1");
        properties[0] = property;

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setProperties(properties);

        IdentityProvider idp = new IdentityProvider();
        idp.setIdentityProviderName("validIdp");
        idp.setId(UUID.randomUUID().toString());
        idp.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
        return idp;
    }
}
