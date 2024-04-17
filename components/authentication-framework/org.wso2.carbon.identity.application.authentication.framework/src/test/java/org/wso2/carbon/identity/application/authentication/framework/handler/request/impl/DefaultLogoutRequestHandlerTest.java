/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultStepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtService;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtServiceTest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import java.util.HashMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

@PrepareForTest({FrameworkUtils.class, SessionNonceCookieUtil.class, LoggerUtils.class,
        FileBasedConfigurationBuilder.class, ConfigurationFacade.class})
@WithCarbonHome
@PowerMockIgnore("org.mockito.*")
public class DefaultLogoutRequestHandlerTest {

    private static final String DUMMY_AUTHENTICATOR = "DummyAuthenticator";
    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    ApplicationAuthenticator mockApplicationAuthenticator;

    @Mock
    FileBasedConfigurationBuilder fileBasedConfigurationBuilder;

    @Mock
    ConfigurationFacade configurationFacade;

    @Mock
    ExternalIdPConfig mockExternalIdpConfig;
    @Spy
    DefaultLogoutRequestHandler defaultLogoutRequestHandler;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetInstance() throws Exception {

        CommonTestUtils.testSingleton(
                DefaultLogoutRequestHandler.getInstance(),
                DefaultLogoutRequestHandler.getInstance()
                                     );
    }

    @Test
    public void testStepsWithNullAuthenticatedAuthenticators() throws Exception {

        mockStatic(LoggerUtils.class);
        doNothing().when(request).setAttribute(anyString(), anyBoolean());
        when(LoggerUtils.isDiagnosticLogsEnabled()).thenReturn(true);
        AuthenticationContext context = prepareContextForTests();
        defaultLogoutRequestHandler.handle(request, response, context);
        assertEquals(context.getCurrentStep(), 3);
    }

    private AuthenticationContext prepareContextForTests()
            throws AuthenticationFailedException, LogoutFailedException, IdentityProviderManagementException {

        AuthenticationContext context = new AuthenticationContext();
        context.setContextIdentifier(String.valueOf(UUID.randomUUID()));
        addSequenceForTest(context, true);
        addApplicationConfig(context);
        context.setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        setUser(context, "admin");
        setPostAuthnMgtService();
        addPostAuthnHandler();
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getStepBasedSequenceHandler()).thenReturn(new DefaultStepBasedSequenceHandler());
        context.initializeAnalyticsData();
        context.setPreviousSessionFound(true);
        return context;
    }

    private void addSequenceForTest(AuthenticationContext context, boolean isCompleted)
            throws AuthenticationFailedException, LogoutFailedException, IdentityProviderManagementException {

        mockStatic(FileBasedConfigurationBuilder.class);
        mockStatic(ConfigurationFacade.class);
        when(ConfigurationFacade.getInstance()).thenReturn(configurationFacade);
        when(configurationFacade.getIdPConfigByName(anyString(), anyString())).thenReturn(mockExternalIdpConfig);
        when(mockApplicationAuthenticator.canHandle(request)).thenReturn(true);
        when(mockApplicationAuthenticator.getName()).thenReturn(DUMMY_AUTHENTICATOR);
        when(mockApplicationAuthenticator.process(request, response, context)).thenReturn(
                AuthenticatorFlowStatus.SUCCESS_COMPLETED);
        when(FileBasedConfigurationBuilder.getInstance()).thenReturn(fileBasedConfigurationBuilder);
        AuthenticatorConfig dummyAuthConfig = new AuthenticatorConfig(DUMMY_AUTHENTICATOR,
                true, new HashMap<>());
        when(fileBasedConfigurationBuilder.getAuthenticatorBean(anyString())).thenReturn(dummyAuthConfig);

        SequenceConfig sequenceConfig = new SequenceConfig();
        HashMap<Integer, StepConfig> stepMap = new HashMap<>();
        StepConfig stepConfig = new StepConfig();
        stepConfig.setAuthenticatedAutenticator(null);
        stepMap.put(1, stepConfig);
        StepConfig stepConfig2 = new StepConfig();
        dummyAuthConfig.setApplicationAuthenticator(mockApplicationAuthenticator);
        stepConfig2.setAuthenticatedAutenticator(dummyAuthConfig);
        stepMap.put(2, stepConfig2);
        sequenceConfig.setStepMap(stepMap);
        sequenceConfig.setCompleted(isCompleted);
        context.setSequenceConfig(sequenceConfig);
    }

    private void addApplicationConfig(AuthenticationContext context) {

        ApplicationConfig applicationConfig = new ApplicationConfig(new ServiceProvider(), SUPER_TENANT_DOMAIN_NAME);
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new
                LocalAndOutboundAuthenticationConfig();
        applicationConfig.getServiceProvider().setLocalAndOutBoundAuthenticationConfig
                (localAndOutboundAuthenticationConfig);
        context.getSequenceConfig().setApplicationConfig(applicationConfig);
    }

    private void setUser(AuthenticationContext context, String userName) {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setAuthenticatedSubjectIdentifier(userName);
        authenticatedUser.setUserId("4b4414e1-916b-4475-aaee-6b0751c29ff6");
        context.setProperty("user-tenant-domain", SUPER_TENANT_DOMAIN_NAME);
        context.getSequenceConfig().setAuthenticatedUser(authenticatedUser);
    }

    private void addPostAuthnHandler() {

        PostAuthenticationMgtServiceTest.TestPostHandlerWithRedirect postAuthenticationHandler =
                new PostAuthenticationMgtServiceTest.TestPostHandlerWithRedirect();
        postAuthenticationHandler.setEnabled(true);
        FrameworkServiceDataHolder.getInstance().addPostAuthenticationHandler(postAuthenticationHandler);
    }

    private void setPostAuthnMgtService() {

        PostAuthenticationMgtService postAuthenticationMgtService = new PostAuthenticationMgtService();
        FrameworkServiceDataHolder.getInstance().setPostAuthenticationMgtService(postAuthenticationMgtService);
    }
}
