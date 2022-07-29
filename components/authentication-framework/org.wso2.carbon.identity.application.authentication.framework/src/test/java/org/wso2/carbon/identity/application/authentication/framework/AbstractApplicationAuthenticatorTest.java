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

import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@PrepareForTest({UserCoreUtil.class, FrameworkServiceDataHolder.class})
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
        when(abstractApplicationAuthenticator.retryAuthenticationEnabled(anyObject())).thenCallRealMethod();
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

        User user = (User) userObj;
        mockStatic(FrameworkServiceDataHolder.class);
        when(FrameworkServiceDataHolder.getInstance()).thenReturn(frameworkServiceDataHolder);
        when(frameworkServiceDataHolder.getAuthnDataPublisherProxy()).thenReturn(authenticationDataPublisherProxy);
        when(authenticationDataPublisherProxy.isEnabled(any())).thenReturn(true);
        doCallRealMethod().when(testApplicationAuthenticator)
                .publishAuthenticationStepAttempt(request, context, user, true);
        testApplicationAuthenticator.publishAuthenticationStepAttempt(request, context, user, isSuccess);
        if (user != null) {
            Assert.assertEquals(user.getTenantDomain(), TENANT_DOMAIN);
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

        mockStatic(UserCoreUtil.class);
        when(UserCoreUtil.getDomainFromThreadLocal()).thenReturn(threadLocalUserStoreDomain);
        String username = abstractApplicationAuthenticator.getUserStoreAppendedName(testedUserName);

        assertEquals(username, expectedUserName);
    }
}
