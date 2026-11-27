/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework;

import com.nimbusds.jwt.JWTClaimsSet;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.UserAssertionUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AbstractApplicationAuthenticatorTest {

    @Mock
    AbstractApplicationAuthenticator abstractApplicationAuthenticator;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FrameworkServiceDataHolder frameworkServiceDataHolder;

    @Mock
    AuthenticationDataPublisher authenticationDataPublisherProxy;

    @Spy
    AuthenticationContext context;

    @Mock
    SequenceConfig sequenceConfig;

    private static final String AUTHENTICATOR = "AbstractAuthenticator";
    private static final String USER_NAME = "DummyUser";
    private static final String USER_STORE_NAME = "TEST.COM";
    private static final String TENANT_DOMAIN = "ABC.COM";

    abstract class TestApplicationAuthenticator extends AbstractApplicationAuthenticator
            implements FederatedApplicationAuthenticator {
    }

    @Mock
    TestApplicationAuthenticator testApplicationAuthenticator;

    @BeforeTest
    public void setUp() throws Exception {
        initMocks(this);
        when(abstractApplicationAuthenticator.retryAuthenticationEnabled()).thenCallRealMethod();
        when(abstractApplicationAuthenticator.retryAuthenticationEnabled(any())).thenCallRealMethod();
        when(abstractApplicationAuthenticator.getName()).thenReturn(AUTHENTICATOR);
        context.initializeAnalyticsData();
        when(context.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        doCallRealMethod().when(abstractApplicationAuthenticator).getUserStoreAppendedName(anyString());
        doCallRealMethod().when(abstractApplicationAuthenticator).process(request, response, context);
    }

    /**
     * Test login request processing by an authenticator. The authenticator cannot handle the login request
     */
    @Test
    public void testProcessLoginRequestCannotHandle() throws Exception {

        when(context.isLogoutRequest()).thenReturn(false);

        // cannot handle
        doReturn(false).when(abstractApplicationAuthenticator).canHandle(request);
        doReturn("AbstractApplicationAuthenticator").when(abstractApplicationAuthenticator).getName();
        doNothing().when(abstractApplicationAuthenticator).initiateAuthenticationRequest(request, response, context);

        AuthenticatorFlowStatus status = abstractApplicationAuthenticator.process(request, response, context);
        assertEquals(context.getCurrentAuthenticator(), abstractApplicationAuthenticator.getName());
        assertEquals(status, AuthenticatorFlowStatus.INCOMPLETE);
    }


    @Test
    public void testProcessLoginRequestWhenRequestNotHandled() throws Exception {

        when(context.isLogoutRequest()).thenReturn(false);

        // can handle
        doReturn(true).when(abstractApplicationAuthenticator).canHandle(request);
        // request not handled yet by framework
        when(request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)).thenReturn(true);
        doReturn("AbstractApplicationAuthenticator").when(abstractApplicationAuthenticator).getName();
        doNothing().when(abstractApplicationAuthenticator).initiateAuthenticationRequest(request, response, context);

        AuthenticatorFlowStatus status = abstractApplicationAuthenticator.process(request, response, context);
        assertEquals(context.getCurrentAuthenticator(), abstractApplicationAuthenticator.getName());
        assertEquals(status, AuthenticatorFlowStatus.INCOMPLETE);
    }


    @Test
    public void testProcessLogoutRequestCanHandleLogout() throws Exception {

        // mock a logout request
        when(context.isLogoutRequest()).thenReturn(true);

        // case1 canHandle
        doReturn(true).when(abstractApplicationAuthenticator).canHandle(request);
        doNothing().when(abstractApplicationAuthenticator).processLogoutResponse(request, response, context);

        AuthenticatorFlowStatus status = abstractApplicationAuthenticator.process(request, response, context);
        assertEquals(status, AuthenticatorFlowStatus.SUCCESS_COMPLETED);
    }

    @Test
    public void testProcessLogoutRequestCannotHandleLogout() throws Exception {

        // mock a logout request
        when(context.isLogoutRequest()).thenReturn(true);

        // case2 cantHandle
        doReturn(false).when(abstractApplicationAuthenticator).canHandle(request);
        doNothing().when(abstractApplicationAuthenticator).initiateLogoutRequest(request, response, context);

        AuthenticatorFlowStatus status = abstractApplicationAuthenticator.process(request, response, context);
        assertEquals(status, AuthenticatorFlowStatus.INCOMPLETE);
    }

    /**
     * Process request by an authenticator that does not support initiating logout requests
     *
     * @throws Exception
     */
    @Test
    public void testProcessLogoutRequestUnsupportedInitLogout() throws Exception {

        // mock a logout request
        when(context.isLogoutRequest()).thenReturn(true);
        doReturn(true).when(abstractApplicationAuthenticator).canHandle(request);
        doCallRealMethod().when(abstractApplicationAuthenticator).initiateLogoutRequest(request, response, context);

        // case1 canHandle
        AuthenticatorFlowStatus status = abstractApplicationAuthenticator.process(request, response, context);
        assertEquals(status, AuthenticatorFlowStatus.SUCCESS_COMPLETED);
    }


    /**
     * Process request by an authenticator that does not support processing logout requests
     *
     * @throws Exception
     */
    @Test
    public void testProcessLogoutRequestUnsupportedProcessLogout() throws Exception {

        when(context.isLogoutRequest()).thenReturn(true);
        doReturn(false).when(abstractApplicationAuthenticator).canHandle(request);
        doCallRealMethod().when(abstractApplicationAuthenticator).processLogoutResponse(request, response, context);

        // case2 can't Handle
        AuthenticatorFlowStatus status = abstractApplicationAuthenticator.process(request, response, context);
        assertEquals(status, AuthenticatorFlowStatus.SUCCESS_COMPLETED);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testInitiateLogoutRequest() throws Exception {

        doCallRealMethod().when(abstractApplicationAuthenticator).initiateLogoutRequest(request, response, context);
        abstractApplicationAuthenticator.initiateLogoutRequest(request, response, context);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testProcessLogoutResponse() throws Exception {

        doCallRealMethod().when(abstractApplicationAuthenticator).processLogoutResponse(request, response, context);
        abstractApplicationAuthenticator.processLogoutResponse(request, response, context);
    }

    @Test
    public void testRetryAuthenticationEnabled() throws Exception {

        doCallRealMethod().when(abstractApplicationAuthenticator).retryAuthenticationEnabled();
        Assert.assertFalse(abstractApplicationAuthenticator.retryAuthenticationEnabled());
    }

    @Test
    public void testRetryAuthenticationEnabled(AuthenticationContext context) {

        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        when(context.getCurrentAuthenticator()).thenReturn("TestAuthenticator");
        Map<String, String> authParams = new HashMap<>();
        authParams.put(AbstractApplicationAuthenticator.ENABLE_RETRY_FROM_AUTHENTICATOR, "true");
        when(context.getAuthenticatorParams("TestAuthenticator")).thenReturn(authParams);
        AuthenticationGraph graph = new AuthenticationGraph();
        graph.setEnabled(true);
        when(sequenceConfig.getAuthenticationGraph()).thenReturn(graph);
        assertTrue(abstractApplicationAuthenticator.retryAuthenticationEnabled(context));
    }


    @Test
    public void testGetClaimDialectURI() throws Exception {

        doCallRealMethod().when(abstractApplicationAuthenticator).getClaimDialectURI();
        Assert.assertNull(abstractApplicationAuthenticator.getClaimDialectURI());
    }

    @Test(dataProvider = "userProvider")
    public void testSetTenantDomainToUserName(Object userObj, boolean isSuccess) throws Exception {

        try (MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                mockStatic(FrameworkServiceDataHolder.class)) {
            User user = (User) userObj;
            frameworkServiceDataHolder.when(
                    FrameworkServiceDataHolder::getInstance).thenReturn(this.frameworkServiceDataHolder);
            when(this.frameworkServiceDataHolder.getAuthnDataPublisherProxy()).thenReturn(
                    authenticationDataPublisherProxy);
            when(authenticationDataPublisherProxy.isEnabled(any())).thenReturn(true);
            doCallRealMethod().when(testApplicationAuthenticator)
                    .publishAuthenticationStepAttempt(request, context, user, true);
            testApplicationAuthenticator.publishAuthenticationStepAttempt(request, context, user, isSuccess);
            if (user != null) {
                Assert.assertEquals(user.getTenantDomain(), TENANT_DOMAIN);
            }
        }
    }

    @DataProvider(name = "userProvider")
    public Object[][] getUsers() {

        return new Object[][]{
                {new User(), true},
                {null, false}
        };
    }

    @DataProvider(name = "usernameProvider")
    public Object[][] getUsernames() {

        String userStoreDomainAppendedName = USER_STORE_NAME + UserCoreConstants.DOMAIN_SEPARATOR + USER_NAME;

        return new Object[][]{
                {
                        // username already has a domain appended
                        userStoreDomainAppendedName, "WSO2.COM", userStoreDomainAppendedName
                },
                {
                        // setting domain from threadlocal
                        USER_NAME, USER_STORE_NAME, userStoreDomainAppendedName
                },
                {
                        // username doesn't have domain, thread local domain is empty too
                        USER_NAME, null, USER_NAME
                },
                {
                        // username doesn't have domain, thread local domain is empty too
                        USER_NAME, "", USER_NAME
                },

        };
    }


    @Test(dataProvider = "usernameProvider")
    public void testGetUserStoreAppendedName(String testedUserName,
                                             String threadLocalUserStoreDomain,
                                             String expectedUserName) throws Exception {

        try (MockedStatic<UserCoreUtil> userCoreUtil = mockStatic(UserCoreUtil.class)) {
            userCoreUtil.when(UserCoreUtil::getDomainFromThreadLocal).thenReturn(threadLocalUserStoreDomain);
            String username = abstractApplicationAuthenticator.getUserStoreAppendedName(testedUserName);

            assertEquals(username, expectedUserName);
        }
    }

    @Test
    public void testUserAssertionRejectsMismatchedUserId() throws Exception {

        JWTClaimsSet claimsSet = buildUserAssertionClaims("signed-user-id");
        when(context.getProperty(FrameworkConstants.USER_ASSERTION)).thenReturn("assertion");
        when(testApplicationAuthenticator.getName()).thenReturn(AUTHENTICATOR);
        doCallRealMethod().when(testApplicationAuthenticator)
                .isAuthenticationRequired(request, response, context);

        try (MockedStatic<UserAssertionUtils> userAssertionUtils = mockStatic(UserAssertionUtils.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            mockUserAssertionLookup(userAssertionUtils, claimsSet);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            frameworkUtils.when(() -> FrameworkUtils.resolveUserIdFromUsername(anyInt(), anyString(), anyString()))
                    .thenReturn("local-user-id");

            assertTrue(testApplicationAuthenticator.isAuthenticationRequired(request, response, context),
                    "Authentication must run when the signed user id does not match the resolved user.");
        }
    }

    @Test
    public void testUserAssertionAcceptsMatchingUserId() throws Exception {

        JWTClaimsSet claimsSet = buildUserAssertionClaims("local-user-id");
        StepConfig stepConfig = mock(StepConfig.class);
        AuthenticatorConfig authenticatorConfig = mock(AuthenticatorConfig.class);
        when(stepConfig.getAuthenticatorList()).thenReturn(Collections.singletonList(authenticatorConfig));
        when(authenticatorConfig.getName()).thenReturn(AUTHENTICATOR);
        when(authenticatorConfig.getIdps()).thenReturn(Collections.emptyMap());
        when(sequenceConfig.getStepMap()).thenReturn(Collections.singletonMap(0, stepConfig));
        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        when(context.getProperty(FrameworkConstants.USER_ASSERTION)).thenReturn("assertion");
        when(testApplicationAuthenticator.getName()).thenReturn(AUTHENTICATOR);
        doCallRealMethod().when(testApplicationAuthenticator)
                .isAuthenticationRequired(request, response, context);

        try (MockedStatic<UserAssertionUtils> userAssertionUtils = mockStatic(UserAssertionUtils.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            mockUserAssertionLookup(userAssertionUtils, claimsSet);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            frameworkUtils.when(() -> FrameworkUtils.resolveUserIdFromUsername(anyInt(), anyString(), anyString()))
                    .thenReturn("local-user-id");

            assertFalse(testApplicationAuthenticator.isAuthenticationRequired(request, response, context),
                    "The assertion must complete authentication when the signed user id matches the resolved user.");
        }
    }

    private JWTClaimsSet buildUserAssertionClaims(String userId) {

        return new JWTClaimsSet.Builder()
                .subject(USER_STORE_NAME + UserCoreConstants.DOMAIN_SEPARATOR + USER_NAME + "@" + TENANT_DOMAIN)
                .claim(FrameworkConstants.AMR, Collections.singletonList(AUTHENTICATOR))
                .claim(FrameworkConstants.USER_ID_CLAIM, userId)
                .build();
    }

    private void mockUserAssertionLookup(MockedStatic<UserAssertionUtils> userAssertionUtils,
                                         JWTClaimsSet claimsSet) {

        userAssertionUtils.when(() -> UserAssertionUtils.retrieveClaimsFromUserAssertion(anyString(), anyString()))
                .thenReturn(Optional.of(claimsSet));
    }
}
