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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;

public class AbstractAppAuthSkipRetryTest {

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

    @Test
    public void testProcessRetryLogic() throws Exception {

        // Mock necessary behavior
        when(context.isLogoutRequest()).thenReturn(false);
        when(abstractApplicationAuthenticator.retryAuthenticationEnabled()).thenReturn(true);
        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        when(context.getCurrentAuthenticator()).thenReturn("TestAuthenticator");
        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        Map<String, String> parameterMap = new HashMap<>();
        authenticatorConfig.setParameterMap(parameterMap);
        when(abstractApplicationAuthenticator.getAuthenticatorConfig()).thenReturn(authenticatorConfig);
        Map<String, String> authParams = new HashMap<>();
        authParams.put(AbstractApplicationAuthenticator.ENABLE_RETRY_FROM_AUTHENTICATOR, "true");
        when(context.getAuthenticatorParams("TestAuthenticator")).thenReturn(authParams);
        AuthenticationGraph graph = new AuthenticationGraph();
        graph.setEnabled(true);
        when(sequenceConfig.getAuthenticationGraph()).thenReturn(graph);

        when(context.getProperty(AbstractApplicationAuthenticator.SKIP_RETRY_FROM_AUTHENTICATOR)).thenReturn(true);
        when(request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)).thenReturn(false);
        doReturn(false).when(abstractApplicationAuthenticator).isRedirectToMultiOptionPageOnFailure();
        doReturn(true).when(abstractApplicationAuthenticator).canHandle(request);

        // Setting context properties
        context.setProperty(AbstractApplicationAuthenticator.SKIP_RETRY_FROM_AUTHENTICATOR, true);

        // Mocking the behavior of processAuthenticationResponse
        doThrow(new AuthenticationFailedException("")).when(abstractApplicationAuthenticator)
                .processAuthenticationResponse(request, response, context);

        // Mock authenticator name
        doReturn("AbstractApplicationAuthenticator").when(abstractApplicationAuthenticator).getName();

        // since retry is skipped, expect an exception.
        assertThrows(AuthenticationFailedException.class, () -> {
            abstractApplicationAuthenticator.process(request, response, context);
        });
        assertFalse(context.isRetrying());
    }
}
