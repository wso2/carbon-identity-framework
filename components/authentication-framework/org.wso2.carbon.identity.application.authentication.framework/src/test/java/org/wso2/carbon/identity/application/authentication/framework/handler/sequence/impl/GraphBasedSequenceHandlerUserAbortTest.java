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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.MockAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.internal.CentralLogMgtServiceComponentHolder;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for handling a step aborted by the user in the graph based sequence handler.
 */
@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons =
        {IdentityCoreServiceDataHolder.class, FrameworkServiceDataHolder.class})
@WithRegistry(injectToSingletons = {FrameworkServiceDataHolder.class})
public class GraphBasedSequenceHandlerUserAbortTest extends GraphBasedSequenceHandlerAbstractTest {

    private static final String SP_FILE_NAME = "js-sp-user-abort.xml";
    private static final String USER_ABORT_AUTHENTICATOR = "MockUserAbortAuthenticator";
    private static final long TIMEOUT_MILLIS = 30000L;

    @BeforeClass
    public void setUpMocks() {

        IdentityEventService identityEventService = mock(IdentityEventService.class);
        CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = true;
        LoggerUtils.isLogMaskingEnable = false;
    }

    @AfterClass
    public void tearDown() {

        CentralLogMgtServiceComponentHolder.getInstance().setIdentityEventService(null);
    }

    @Test(timeOut = TIMEOUT_MILLIS)
    public void testUserAbortParamOnStepWithoutCallbacksFailsAuthentication() throws Exception {

        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2);\n" +
                "    executeStep(3);\n" +
                "};";

        AuthenticationContext context = createContext(script);
        HttpServletRequest request = createRequestAbortingAfterFirstStep(context, false);
        graphBasedSequenceHandler.handle(request, mock(HttpServletResponse.class), context);

        assertAuthenticationFailedAfterFirstStep(context, request);
    }

    @Test(timeOut = TIMEOUT_MILLIS)
    public void testUserAbortParamOnStepWithoutOnUserAbortCallbackFailsAuthentication() throws Exception {

        // The decision node of step 2 has step 3 as the default edge, which should not be executed.
        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2, {\n" +
                "        onSuccess: function(context) {\n" +
                "            Log.info('Step 2 succeeded.');\n" +
                "        }\n" +
                "    });\n" +
                "    executeStep(3);\n" +
                "};";

        AuthenticationContext context = createContext(script);
        HttpServletRequest request = createRequestAbortingAfterFirstStep(context, false);
        graphBasedSequenceHandler.handle(request, mock(HttpServletResponse.class), context);

        assertAuthenticationFailedAfterFirstStep(context, request);
    }

    @Test(timeOut = TIMEOUT_MILLIS)
    public void testUserAbortParamIsNotBypassedThroughOnUserAbortCallback() throws Exception {

        // Step 3 executed by the onUserAbort callback is aborted as well, since the same request is processed.
        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2, {\n" +
                "        onUserAbort: function(context) {\n" +
                "            executeStep(3);\n" +
                "        }\n" +
                "    });\n" +
                "};";

        AuthenticationContext context = createContext(script);
        HttpServletRequest request = createRequestAbortingAfterFirstStep(context, false);
        graphBasedSequenceHandler.handle(request, mock(HttpServletResponse.class), context);

        assertAuthenticationFailedAfterFirstStep(context, request);
    }

    @Test(timeOut = TIMEOUT_MILLIS)
    public void testUserAbortParamIsNotRetried() throws Exception {

        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(2);\n" +
                "};";

        AuthenticationContext context = createContext(script);
        // Retrying the aborted step would abort it again for the same request, without an end.
        HttpServletRequest request = createRequestAbortingAfterFirstStep(context, true);
        graphBasedSequenceHandler.handle(request, mock(HttpServletResponse.class), context);

        assertAuthenticationFailedAfterFirstStep(context, request);
    }

    @Test(timeOut = TIMEOUT_MILLIS)
    public void testUserAbortFromAuthenticatorWithoutCallbacksFailsAuthentication() throws Exception {

        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(4);\n" +
                "    executeStep(3);\n" +
                "};";

        AuthenticationContext context = processWithUserAbortAuthenticator(script);

        assertTrue(context.getSequenceConfig().isCompleted());
        assertFalse(context.isRequestAuthenticated());
        assertFalse(getExecutedAuthenticators(context).contains("FptMockAuthenticator"),
                "Steps after the aborted step should not be executed.");
    }

    @Test(timeOut = TIMEOUT_MILLIS)
    public void testUserAbortFromAuthenticatorNotRoutedByScriptEndsAuthentication() throws Exception {

        // The onUserAbort callback decides the outcome. Since it does not route to another node, the flow ends
        // with the result of the steps completed before the aborted step.
        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(4, {\n" +
                "        onUserAbort: function(context) {\n" +
                "            Log.info('Step 4 is aborted.');\n" +
                "        }\n" +
                "    });\n" +
                "};";

        AuthenticationContext context = processWithUserAbortAuthenticator(script);

        assertTrue(context.getSequenceConfig().isCompleted());
        assertTrue(context.isRequestAuthenticated());
    }

    @Test(timeOut = TIMEOUT_MILLIS)
    public void testUserAbortFromAuthenticatorRoutedByScript() throws Exception {

        String script = "var onLoginRequest = function(context) {\n" +
                "    executeStep(1);\n" +
                "    executeStep(4, {\n" +
                "        onUserAbort: function(context) {\n" +
                "            executeStep(3);\n" +
                "        }\n" +
                "    });\n" +
                "};";

        AuthenticationContext context = processWithUserAbortAuthenticator(script);

        assertTrue(context.getSequenceConfig().isCompleted());
        assertTrue(context.isRequestAuthenticated());
        List<String> executedAuthenticators = getExecutedAuthenticators(context);
        assertEquals(executedAuthenticators.get(executedAuthenticators.size() - 1), "FptMockAuthenticator",
                "The step executed by the onUserAbort callback should be the last executed step.");
    }

    private AuthenticationContext createContext(String script) throws Exception {

        // The authenticator of step 4 should be available to load the sequence of the service provider.
        ApplicationAuthenticatorManager.getInstance().addSystemDefinedAuthenticator(
                new MockUserAbortAuthenticator(USER_ABORT_AUTHENTICATOR));

        ServiceProvider serviceProvider = getTestServiceProvider(SP_FILE_NAME);
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig().setContent(script);

        AuthenticationContext context = getAuthenticationContext(serviceProvider);
        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), serviceProvider);
        context.setSequenceConfig(sequenceConfig);
        UserCoreUtil.setDomainInThreadLocal("test_domain");
        return context;
    }

    /**
     * Creates a request which carries the userAbort parameter once the first step is completed.
     *
     * @param context     Authentication context.
     * @param setRetrying Whether to mark the context as retrying when the step is aborted.
     * @return Mocked request.
     */
    private HttpServletRequest createRequestAbortingAfterFirstStep(AuthenticationContext context,
                                                                   boolean setRetrying) {

        HttpServletRequest request = createMockHttpServletRequest();
        when(request.getParameter(FrameworkConstants.RequestParams.USER_ABORT)).thenAnswer(invocation -> {
            if (context.getAuthenticationStepHistory().isEmpty()) {
                return null;
            }
            if (setRetrying) {
                context.setRetrying(true);
            }
            return "true";
        });
        return request;
    }

    private AuthenticationContext processWithUserAbortAuthenticator(String script) throws Exception {

        AuthenticationContext context = createContext(script);
        graphBasedSequenceHandler.handle(createMockHttpServletRequest(), mock(HttpServletResponse.class), context);
        return context;
    }

    private void assertAuthenticationFailedAfterFirstStep(AuthenticationContext context, HttpServletRequest request) {

        assertTrue(context.getSequenceConfig().isCompleted());
        assertFalse(context.isRequestAuthenticated());
        assertEquals(getExecutedAuthenticators(context), Collections.singletonList("BasicMockAuthenticator"),
                "Only the first step should be executed.");
        assertEquals(request.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS),
                AuthenticatorFlowStatus.FAIL_COMPLETED);
    }

    private List<String> getExecutedAuthenticators(AuthenticationContext context) {

        return context.getAuthenticationStepHistory().stream()
                .map(AuthHistory::getAuthenticatorName)
                .collect(Collectors.toList());
    }

    /**
     * Authenticator which returns the user abort status.
     */
    private static class MockUserAbortAuthenticator extends MockAuthenticator {

        public MockUserAbortAuthenticator(String name) {

            super(name);
        }

        @Override
        public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                               AuthenticationContext context)
                throws AuthenticationFailedException, LogoutFailedException {

            return AuthenticatorFlowStatus.USER_ABORT;
        }
    }
}
