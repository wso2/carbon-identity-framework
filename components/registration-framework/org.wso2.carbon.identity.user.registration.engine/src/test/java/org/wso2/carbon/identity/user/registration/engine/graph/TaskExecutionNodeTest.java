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

package org.wso2.carbon.identity.user.registration.engine.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.internal.RegistrationFlowEngineDataHolder;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegisteringUser;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_GET_IDP_CONFIG_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_REGISTRATION_FAILURE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_RETRY;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_USER_CREATED;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_USER_ERROR;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ExecutorStatus.STATUS_USER_INPUT_REQUIRED;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_INCOMPLETE;

/**
 * Test class for TaskExecutionNode.
 */
@WithCarbonHome
public class TaskExecutionNodeTest {

    private TaskExecutionNode taskExecutionNode;
    private RegistrationContext context;
    private NodeConfig nodeConfig;
    private static final String TENANT_DOMAIN = "test.com";
    private static final String TEST_EXECUTOR = "TestExecutor";

    @Mock
    private Executor executor;

    @BeforeClass
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        taskExecutionNode = new TaskExecutionNode();

        context = new RegistrationContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setRegGraph(new RegistrationGraphConfig());
        context.setRegisteringUser(new RegisteringUser());

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

    @Test
    public void testExecutorUndefined() throws Exception {

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockStatic(
                RegistrationFlowEngineDataHolder.class)) {
            RegistrationFlowEngineDataHolder dataHolder = mock(RegistrationFlowEngineDataHolder.class);
            mocked.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolder);
            when(dataHolder.getExecutors()).thenReturn(new HashMap<>());
            NodeConfig invalidConfig = new NodeConfig.Builder().id("testNode").type("TASK_EXECUTION").build();
            taskExecutionNode.execute(context, invalidConfig);
        } catch (RegistrationEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_EXECUTOR_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testExecutorNotFound() throws Exception {

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockStatic(
                RegistrationFlowEngineDataHolder.class)) {
            RegistrationFlowEngineDataHolder dataHolder = mock(RegistrationFlowEngineDataHolder.class);
            mocked.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolder);
            when(dataHolder.getExecutors()).thenReturn(new HashMap<>());
            taskExecutionNode.execute(context, nodeConfig);
        } catch (RegistrationEngineServerException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNSUPPORTED_EXECUTOR.getCode());
        }
    }

    @Test
    public void testExecutorCompleteStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_COMPLETE);

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            Response response = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(response.getStatus(), STATUS_COMPLETE);
        }
    }

    @Test
    public void testExecutorUserCreatedStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_USER_CREATED);
        Map<String, Object> updatedClaims = new HashMap<>();
        updatedClaims.put("email", "test@example.com");
        executorResponse.setUpdatedUserClaims(updatedClaims);

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            Response response = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(response.getStatus(), STATUS_COMPLETE);
            assertEquals(context.getRegisteringUser().getClaims().get("email"), "test@example.com");
        }
    }

    @Test
    public void testExecutorRetryStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_RETRY);
        executorResponse.setRequiredData(new ArrayList<>());
        executorResponse.setErrorMessage("Retry error");

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            Response response = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            assertEquals(response.getType(), "VIEW");
            assertEquals(response.getError(), "Retry error");
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

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            Response response = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            assertEquals(response.getType(), "VIEW");
            assertNotNull(response.getRequiredData());
            assertEquals(response.getRequiredData().size(), 2);
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

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            Response response = taskExecutionNode.execute(context, nodeConfig);
            assertEquals(response.getStatus(), STATUS_INCOMPLETE);
            assertEquals(response.getType(), "REDIRECTION");
            assertEquals(response.getAdditionalInfo().get("redirectUrl"), "https://example.com");
        }
    }

    @Test
    public void testExecutorUserErrorStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_USER_ERROR);
        executorResponse.setErrorMessage("User error occurred");

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            taskExecutionNode.execute(context, nodeConfig);
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_REGISTRATION_FAILURE.getCode());
        }
    }

    @Test
    public void testExecutorUnsupportedStatus() throws Exception {

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult("UNSUPPORTED_STATUS");

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse)) {
            taskExecutionNode.execute(context, nodeConfig);
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS.getCode());
        }
    }

    @Test
    public void testIdpConfigBeingNull() throws Exception {

        ExecutorDTO executorDTO = nodeConfig.getExecutorConfig();
        executorDTO.setIdpName("invalidIDP");

        IdentityProviderManager idpManager = mock(IdentityProviderManager.class);

        try (MockedStatic<IdentityProviderManager> idpManagerStatic = mockStatic(IdentityProviderManager.class)) {
            idpManagerStatic.when(IdentityProviderManager::getInstance).thenReturn(idpManager);
            when(idpManager.getIdPByName("invalidIDP", TENANT_DOMAIN)).thenReturn(null);

            try (MockedStatic<RegistrationFlowEngineDataHolder> engineMock = mockStatic(
                    RegistrationFlowEngineDataHolder.class)) {
                RegistrationFlowEngineDataHolder dataHolder = mock(RegistrationFlowEngineDataHolder.class);
                engineMock.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolder);
                taskExecutionNode.execute(context, nodeConfig);
            }
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_GET_IDP_CONFIG_FAILURE.getCode());
        }
    }

    @Test
    public void testIdpConfigRetrieveFailure() throws Exception {

        ExecutorDTO executorDTO = nodeConfig.getExecutorConfig();
        executorDTO.setIdpName("validIdp");

        IdentityProviderManager idpManager = mock(IdentityProviderManager.class);

        try (MockedStatic<IdentityProviderManager> idpManagerStatic = mockStatic(IdentityProviderManager.class)) {
            idpManagerStatic.when(IdentityProviderManager::getInstance).thenReturn(idpManager);
            when(idpManager.getIdPByName("validIdp", TENANT_DOMAIN)).thenThrow(
                    IdentityProviderManagementException.class);

            try (MockedStatic<RegistrationFlowEngineDataHolder> engineMock = mockStatic(
                    RegistrationFlowEngineDataHolder.class)) {
                RegistrationFlowEngineDataHolder dataHolder = mock(RegistrationFlowEngineDataHolder.class);
                engineMock.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolder);
                taskExecutionNode.execute(context, nodeConfig);
            }
        } catch (RegistrationEngineException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_GET_IDP_CONFIG_FAILURE.getCode());
        }
    }

    @Test
    public void testIdpConfigRetrieval() throws Exception {

        ExecutorDTO executorDTO = nodeConfig.getExecutorConfig();
        executorDTO.setIdpName("validIdp");

        ExecutorResponse executorResponse = new ExecutorResponse();
        executorResponse.setResult(STATUS_COMPLETE);

        IdentityProvider idp = getTestIdentityProvider();

        IdentityProviderManager idpManager = mock(IdentityProviderManager.class);

        try (MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockExecutorResponseFlow(executorResponse);
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

    private MockedStatic<RegistrationFlowEngineDataHolder> mockExecutorResponseFlow(ExecutorResponse executorResponse)
            throws Exception {

        Map<String, Executor> executorMap = new HashMap<>();
        executorMap.put(TEST_EXECUTOR, executor);

        when(executor.execute(any())).thenReturn(executorResponse);
        when(executor.getName()).thenReturn(TEST_EXECUTOR);

        MockedStatic<RegistrationFlowEngineDataHolder> mocked = mockStatic(RegistrationFlowEngineDataHolder.class);
        RegistrationFlowEngineDataHolder dataHolder = mock(RegistrationFlowEngineDataHolder.class);
        mocked.when(RegistrationFlowEngineDataHolder::getInstance).thenReturn(dataHolder);
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
