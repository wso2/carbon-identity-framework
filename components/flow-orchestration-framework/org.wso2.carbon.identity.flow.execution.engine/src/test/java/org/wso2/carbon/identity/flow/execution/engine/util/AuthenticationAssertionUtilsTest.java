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

package org.wso2.carbon.identity.flow.execution.engine.util;

import com.nimbusds.jwt.JWTClaimsSet;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.UserAssertionUtils;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.graph.AuthenticationExecutor;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USER_ID_CLAIM;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.USER_ASSERTION_EXPIRY_PROPERTY;

/**
 * Unit tests for {@link AuthenticationAssertionUtils}.
 */
public class AuthenticationAssertionUtilsTest {

    private static final String TEST_TENANT_DOMAIN = "carbon.super";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_USER_ID = "12345";
    private static final String TEST_CONTEXT_IDENTIFIER = "test-context-123";
    private static final String TEST_SERVER_URL = "https://localhost:9443";
    private static final String TEST_SIGNED_ASSERTION = "signed.jwt.token";
    private static final String TEST_AMR_VALUE = "password";
    private static final String TEST_EXECUTOR_NAME = "password-executor";

    @Mock
    private FlowExecutionContext mockContext;

    @Mock
    private FlowUser mockFlowUser;

    @Mock
    private FlowExecutionEngineDataHolder mockDataHolder;

    @Mock
    private NodeConfig mockNodeConfig;

    @Mock
    private ExecutorDTO mockExecutorConfig;

    @Mock
    private AuthenticationExecutor mockAuthExecutor;

    private AutoCloseable mockitoCloseable;

    @BeforeMethod
    public void setUp() {

        mockitoCloseable = MockitoAnnotations.openMocks(this);
        when(mockContext.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);
        when(mockContext.getContextIdentifier()).thenReturn(TEST_CONTEXT_IDENTIFIER);
        when(mockContext.getFlowUser()).thenReturn(mockFlowUser);
        when(mockFlowUser.getUsername()).thenReturn(TEST_USERNAME);
        when(mockFlowUser.getUserId()).thenReturn(TEST_USER_ID);
        when(mockFlowUser.getUserStoreDomain()).thenReturn("PRIMARY");
    }

    @AfterMethod
    public void tearDown() {

        if (mockitoCloseable != null) {
            try {
                mockitoCloseable.close();
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testGetSignedUserAssertionSuccess() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<UserAssertionUtils> userAssertionMock = mockStatic(UserAssertionUtils.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupExecutorMocks();

            userAssertionMock.when(() -> UserAssertionUtils.generateSignedUserAssertion(
                            any(JWTClaimsSet.class), eq(TEST_TENANT_DOMAIN)))
                    .thenReturn(TEST_SIGNED_ASSERTION);

            String result = AuthenticationAssertionUtils.getSignedUserAssertion(mockContext);

            Assert.assertEquals(result, TEST_SIGNED_ASSERTION);
        }
    }

    @Test
    public void testGetSignedUserAssertionFrameworkException() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<UserAssertionUtils> userAssertionMock = mockStatic(UserAssertionUtils.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupExecutorMocks();

            FrameworkException frameworkException = new FrameworkException("Framework error");
            userAssertionMock.when(() -> UserAssertionUtils.generateSignedUserAssertion(
                            any(JWTClaimsSet.class), eq(TEST_TENANT_DOMAIN)))
                    .thenThrow(frameworkException);

            try {
                AuthenticationAssertionUtils.getSignedUserAssertion(mockContext);
                Assert.fail("Expected FlowEngineServerException to be thrown");
            } catch (FlowEngineServerException e) {
                Assert.assertNotNull(e);
            }
        }
    }

    @Test
    public void testGetSignedUserAssertionRuntimeException() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<UserAssertionUtils> userAssertionMock = mockStatic(UserAssertionUtils.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupExecutorMocks();

            FrameworkException wrappedException = new FrameworkException("Wrapped error");
            userAssertionMock.when(() -> UserAssertionUtils.generateSignedUserAssertion(
                            any(JWTClaimsSet.class), eq(TEST_TENANT_DOMAIN)))
                    .thenThrow(wrappedException);

            try {
                AuthenticationAssertionUtils.getSignedUserAssertion(mockContext);
                Assert.fail("Expected FlowEngineServerException to be thrown");
            } catch (FlowEngineServerException e) {
                Assert.assertNotNull(e);
            }
        }
    }

    @Test
    public void testBuildUserAssertionClaimSetWithAMRValues() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class);
             MockedStatic<UserCoreUtil> userCoreUtilMock = mockStatic(UserCoreUtil.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupUserCoreUtilMocks(userCoreUtilMock);
            setupExecutorMocks();

            JWTClaimsSet claimsSet = invokePrivateMethod("buildUserAssertionClaimSet", mockContext);

            verifyBasicClaims(claimsSet);
            verifyAMRClaims(claimsSet, Arrays.asList(TEST_AMR_VALUE));
        }
    }

    @Test
    public void testBuildUserAssertionClaimSetWithoutAMRValues() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class);
             MockedStatic<UserCoreUtil> userCoreUtilMock = mockStatic(UserCoreUtil.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupUserCoreUtilMocks(userCoreUtilMock);
            setupNonAuthExecutorMocks();

            JWTClaimsSet claimsSet = invokePrivateMethod("buildUserAssertionClaimSet", mockContext);

            verifyBasicClaims(claimsSet);
            verifyAMRClaims(claimsSet, Arrays.asList());
        }
    }

    @Test
    public void testBuildUserAssertionClaimSetWithNullAMRValue() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class);
             MockedStatic<UserCoreUtil> userCoreUtilMock = mockStatic(UserCoreUtil.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupUserCoreUtilMocks(userCoreUtilMock);
            setupExecutorMocksWithNullAMR();

            JWTClaimsSet claimsSet = invokePrivateMethod("buildUserAssertionClaimSet", mockContext);

            verifyBasicClaims(claimsSet);
            verifyAMRClaims(claimsSet, Arrays.asList());
        }
    }

    @DataProvider(name = "expiryTimeTestData")
    public Object[][] getExpiryTimeTestData() {

        return new Object[][]{
                {"5000", 5000L},
                {"-1", 2000L},
                {"0", 2000L},
                {"invalid", 2000L},
                {"", 2000L}
        };
    }

    @Test(dataProvider = "expiryTimeTestData")
    public void testCalculateUserAssertionExpiryTime(String configValue, long expectedLifetime) throws Exception {

        try (MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class)) {
            long currentTime = System.currentTimeMillis();

            identityUtilMock.when(() -> IdentityUtil.getProperty(USER_ASSERTION_EXPIRY_PROPERTY))
                    .thenReturn(configValue);

            Date result = invokePrivateMethod("calculateUserAssertionExpiryTime", currentTime);

            long expectedExpiryTime = currentTime + expectedLifetime;
            long actualDifference = Math.abs(result.getTime() - expectedExpiryTime);
            Assert.assertTrue(actualDifference <= 5000,
                    "Expected expiry time difference should be within 5 seconds, but was: " + actualDifference + "ms");
        }
    }

    @Test
    public void testPrivateConstructor() throws Exception {

        java.lang.reflect.Constructor<AuthenticationAssertionUtils> constructor =
                AuthenticationAssertionUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AuthenticationAssertionUtils instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testAssertionGeneration() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<UserAssertionUtils> userAssertionMock = mockStatic(UserAssertionUtils.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class);
             MockedStatic<UserCoreUtil> userCoreUtilMock = mockStatic(UserCoreUtil.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupUserCoreUtilMocks(userCoreUtilMock);
            setupExecutorMocks();

            userAssertionMock.when(() -> UserAssertionUtils.generateSignedUserAssertion(
                            any(JWTClaimsSet.class), eq(TEST_TENANT_DOMAIN)))
                    .thenReturn(TEST_SIGNED_ASSERTION);

            String assertion = AuthenticationAssertionUtils.getSignedUserAssertion(mockContext);

            Assert.assertNotNull(assertion);
            Assert.assertEquals(assertion, TEST_SIGNED_ASSERTION);

            JWTClaimsSet claimsSet = invokePrivateMethod("buildUserAssertionClaimSet", mockContext);
            verifyBasicClaims(claimsSet);
            verifyAMRClaims(claimsSet, Arrays.asList(TEST_AMR_VALUE));
            verifyTimingConstraints(claimsSet);
        }
    }

    @Test
    public void testAssertionGenerationWithMultipleExecutors() throws Exception {

        try (MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock = mockStatic(FlowExecutionEngineDataHolder.class);
             MockedStatic<IdentityUtil> identityUtilMock = mockStatic(IdentityUtil.class);
             MockedStatic<ServiceURLBuilder> serviceURLBuilderMock = mockStatic(ServiceURLBuilder.class);
             MockedStatic<UserCoreUtil> userCoreUtilMock = mockStatic(UserCoreUtil.class)) {

            setupBasicMocks(dataHolderMock, identityUtilMock, serviceURLBuilderMock);
            setupUserCoreUtilMocks(userCoreUtilMock);
            setupMultipleExecutorMocks();

            JWTClaimsSet claimsSet = invokePrivateMethod("buildUserAssertionClaimSet", mockContext);

            verifyAMRClaims(claimsSet, Arrays.asList("password", "otp"));
        }
    }

    private void setupCommonMockBehaviors() {

        when(mockContext.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);
        when(mockContext.getContextIdentifier()).thenReturn(TEST_CONTEXT_IDENTIFIER);
        when(mockContext.getFlowUser()).thenReturn(mockFlowUser);
        when(mockFlowUser.getUsername()).thenReturn(TEST_USERNAME);
        when(mockFlowUser.getUserId()).thenReturn(TEST_USER_ID);
    }

    private void setupBasicMocks(MockedStatic<FlowExecutionEngineDataHolder> dataHolderMock,
                                 MockedStatic<IdentityUtil> identityUtilMock,
                                 MockedStatic<ServiceURLBuilder> serviceURLBuilderMock) throws URLBuilderException {

        dataHolderMock.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(mockDataHolder);

        identityUtilMock.when(() -> IdentityUtil.getServerURL(anyString(), eq(true), eq(true)))
                .thenReturn(TEST_SERVER_URL);
        identityUtilMock.when(() -> IdentityUtil.getProperty(USER_ASSERTION_EXPIRY_PROPERTY))
                .thenReturn("5000");
        identityUtilMock.when(() -> IdentityUtil.getHostName())
                .thenReturn("localhost");

        ServiceURLBuilder mockServiceURLBuilder = mock(ServiceURLBuilder.class);
        ServiceURL mockServiceURL = mock(ServiceURL.class);

        serviceURLBuilderMock.when(ServiceURLBuilder::create).thenReturn(mockServiceURLBuilder);
        when(mockServiceURLBuilder.build(anyString())).thenReturn(mockServiceURL);
        when(mockServiceURL.getAbsolutePublicURL()).thenReturn(TEST_SERVER_URL);
    }

    private void setupUserCoreUtilMocks(MockedStatic<UserCoreUtil> userCoreUtilMock) {

        userCoreUtilMock.when(() -> UserCoreUtil.addTenantDomainToEntry(TEST_USERNAME, TEST_TENANT_DOMAIN))
                .thenReturn(TEST_USERNAME + "@" + TEST_TENANT_DOMAIN);
        userCoreUtilMock.when(() -> UserCoreUtil.addDomainToName(TEST_USERNAME + "@" + TEST_TENANT_DOMAIN, "PRIMARY"))
                .thenReturn(TEST_USERNAME + "@" + TEST_TENANT_DOMAIN);
    }

    private void setupExecutorMocks() {

        List<NodeConfig> completedNodes = createMockCompletedNodes();
        when(mockContext.getCompletedNodes()).thenReturn(completedNodes);

        Map<String, Executor> executors = new HashMap<>();
        executors.put(TEST_EXECUTOR_NAME, mockAuthExecutor);
        when(mockDataHolder.getExecutors()).thenReturn(executors);
        when(mockAuthExecutor.getAMRValue()).thenReturn(TEST_AMR_VALUE);
    }

    private void setupExecutorMocksWithNullAMR() {

        List<NodeConfig> completedNodes = createMockCompletedNodes();
        when(mockContext.getCompletedNodes()).thenReturn(completedNodes);

        Map<String, Executor> executors = new HashMap<>();
        executors.put(TEST_EXECUTOR_NAME, mockAuthExecutor);
        when(mockDataHolder.getExecutors()).thenReturn(executors);
        when(mockAuthExecutor.getAMRValue()).thenReturn(null);
    }

    private void setupNonAuthExecutorMocks() {

        List<NodeConfig> completedNodes = createMockCompletedNodesWithoutAMR();
        when(mockContext.getCompletedNodes()).thenReturn(completedNodes);

        Map<String, Executor> executors = new HashMap<>();
        executors.put(TEST_EXECUTOR_NAME, mock(Executor.class));
        when(mockDataHolder.getExecutors()).thenReturn(executors);
    }

    private void setupMultipleExecutorMocks() {

        List<NodeConfig> completedNodes = new ArrayList<>();

        NodeConfig passwordNode = mock(NodeConfig.class);
        ExecutorDTO passwordExecutorConfig = mock(ExecutorDTO.class);
        when(passwordNode.getExecutorConfig()).thenReturn(passwordExecutorConfig);
        when(passwordExecutorConfig.getName()).thenReturn("password-executor");
        completedNodes.add(passwordNode);

        NodeConfig otpNode = mock(NodeConfig.class);
        ExecutorDTO otpExecutorConfig = mock(ExecutorDTO.class);
        when(otpNode.getExecutorConfig()).thenReturn(otpExecutorConfig);
        when(otpExecutorConfig.getName()).thenReturn("otp-executor");
        completedNodes.add(otpNode);

        when(mockContext.getCompletedNodes()).thenReturn(completedNodes);

        Map<String, Executor> executors = new HashMap<>();
        AuthenticationExecutor passwordExecutor = mock(AuthenticationExecutor.class);
        AuthenticationExecutor otpExecutor = mock(AuthenticationExecutor.class);

        when(passwordExecutor.getAMRValue()).thenReturn("password");
        when(otpExecutor.getAMRValue()).thenReturn("otp");

        executors.put("password-executor", passwordExecutor);
        executors.put("otp-executor", otpExecutor);
        when(mockDataHolder.getExecutors()).thenReturn(executors);
    }

    private List<NodeConfig> createMockCompletedNodes() {

        List<NodeConfig> nodes = new ArrayList<>();
        when(mockNodeConfig.getExecutorConfig()).thenReturn(mockExecutorConfig);
        when(mockExecutorConfig.getName()).thenReturn(TEST_EXECUTOR_NAME);
        nodes.add(mockNodeConfig);
        return nodes;
    }

    private List<NodeConfig> createMockCompletedNodesWithoutAMR() {

        List<NodeConfig> nodes = new ArrayList<>();
        NodeConfig nonAuthNode = mock(NodeConfig.class);
        ExecutorDTO nonAuthExecutorConfig = mock(ExecutorDTO.class);
        when(nonAuthNode.getExecutorConfig()).thenReturn(nonAuthExecutorConfig);
        when(nonAuthExecutorConfig.getName()).thenReturn("non-auth-executor");
        nodes.add(nonAuthNode);
        return nodes;
    }

    private void verifyBasicClaims(JWTClaimsSet claimsSet) {

        Assert.assertNotNull(claimsSet);
        Assert.assertEquals(claimsSet.getSubject(), TEST_USERNAME + "@" + TEST_TENANT_DOMAIN);
        Assert.assertEquals(claimsSet.getClaim(USER_ID_CLAIM), TEST_USER_ID);
        Assert.assertEquals(claimsSet.getIssuer(), TEST_SERVER_URL);
        Assert.assertNotNull(claimsSet.getJWTID());
        Assert.assertNotNull(claimsSet.getIssueTime());
        Assert.assertNotNull(claimsSet.getExpirationTime());
    }

    private void verifyAMRClaims(JWTClaimsSet claimsSet, List<String> expectedAMRValues) {

        List<String> actualAMRValues = (List<String>) claimsSet.getClaim("amr");
        if (expectedAMRValues.isEmpty()) {
            Assert.assertTrue(actualAMRValues == null || actualAMRValues.isEmpty());
        } else {
            Assert.assertNotNull(actualAMRValues);
            Assert.assertEquals(actualAMRValues.size(), expectedAMRValues.size());
            for (String expectedValue : expectedAMRValues) {
                Assert.assertTrue(actualAMRValues.contains(expectedValue));
            }
        }
    }

    private void verifyTimingConstraints(JWTClaimsSet claimsSet) {

        Date issueTime = claimsSet.getIssueTime();
        Date expirationTime = claimsSet.getExpirationTime();
        Assert.assertNotNull(issueTime);
        Assert.assertNotNull(expirationTime);
        Assert.assertTrue(expirationTime.after(issueTime));

        long timeDifference = expirationTime.getTime() - issueTime.getTime();
        Assert.assertTrue(timeDifference > 0);
        Assert.assertTrue(timeDifference <= 10000);
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(String methodName, Object... args) throws Exception {

        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Long) {
                paramTypes[i] = long.class;
            } else {
                paramTypes[i] = args[i].getClass();
            }
        }

        Method method = AuthenticationAssertionUtils.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return (T) method.invoke(null, args);
    }
}
