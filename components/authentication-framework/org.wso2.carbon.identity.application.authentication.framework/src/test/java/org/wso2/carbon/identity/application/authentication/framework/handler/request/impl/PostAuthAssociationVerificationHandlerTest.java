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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.MockAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManagerImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.ApplicationVersion.APP_VERSION_V2;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.ApplicationVersion.LATEST_APP_VERSION;

public class PostAuthAssociationVerificationHandlerTest extends AbstractFrameworkTest {

    public static final String PRIMARY = "PRIMARY";
    private UIBasedConfigurationLoader configurationLoader;
    private HttpServletRequest request;
    private HttpServletResponse response;
    PostAuthAssociationVerificationHandler postAuthAssociationVerificationHandler;
    private ServiceProvider sp;
    private ExternalIdPConfig externalIdPConfig;
    private FederatedAssociationManager federatedAssociationManager;
    private MockedStatic<FrameworkUtils> frameworkUtils;
    private MockedStatic<ConfigurationFacade> configurationFacade;
    private MockedStatic<ClaimMetadataHandler> claimMetadataHandler;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<AdminServicesUtil> adminServicesUtil;

    private void initAuthenticators() {

        removeAllSystemDefinedAuthenticators();
        ApplicationAuthenticatorManager authenticatorManager = ApplicationAuthenticatorManager.getInstance();
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("BasicMockAuthenticator"));
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("HwkMockAuthenticator"));
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("FptMockAuthenticator"));
    }

    @BeforeClass
    protected void setupSuite() throws Exception {

        initAuthenticators();

        configurationLoader = new UIBasedConfigurationLoader();
        frameworkUtils = mockStatic(FrameworkUtils.class);
        configurationFacade = mockStatic(ConfigurationFacade.class);
        claimMetadataHandler = mockStatic(ClaimMetadataHandler.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        adminServicesUtil = mockStatic(AdminServicesUtil.class);
        ConfigurationFacade mockConfigurationFacade = mock(ConfigurationFacade.class);

        ApplicationManagementService mockApplicationManagementService = mock(ApplicationManagementService.class);
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(mockApplicationManagementService);

        federatedAssociationManager = mock(FederatedAssociationManagerImpl.class);
        FrameworkServiceDataHolder.getInstance().setFederatedAssociationManager(federatedAssociationManager);

        configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigurationFacade);

        IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
        externalIdPConfig = new ExternalIdPConfig(identityProvider);
        doReturn(externalIdPConfig).when(mockConfigurationFacade).getIdPConfigByName(anyString(), anyString());
        frameworkUtils.when(() -> FrameworkUtils.isStepBasedSequenceHandlerExecuted(any(AuthenticationContext.class)))
                .thenCallRealMethod();
        frameworkUtils.when(() -> FrameworkUtils.prependUserStoreDomainToName(anyString()))
                .thenCallRealMethod();
        frameworkUtils.when(() -> FrameworkUtils.buildClaimMappings(anyMap())).thenCallRealMethod();
        frameworkUtils.when(() -> FrameworkUtils.getStandardDialect(anyString(), any(ApplicationConfig.class)))
                .thenCallRealMethod();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        postAuthAssociationVerificationHandler = PostAuthAssociationVerificationHandler.getInstance();
        sp = getTestServiceProvider("default-sp-1.xml");
        sp.setApplicationVersion(LATEST_APP_VERSION);
        when(mockApplicationManagementService.getServiceProvider(any(), any())).thenReturn(sp);
    }

    @Test(description = "This test case tests the Post Authentication Association Verification handler's handle method",
            dataProvider = "provideTestScenarios")
    public void testHandle(String appVersion, boolean isExternIdp, boolean isLocalSubjectMandatory, User localUser,
                           boolean isError) throws Exception {

        sp.getClaimConfig().setMappedLocalSubjectMandatory(isLocalSubjectMandatory);
        sp.setApplicationVersion(appVersion);

        adminServicesUtil.when(AdminServicesUtil::getUserRealm).thenReturn(null);
        AuthenticationContext context = processAndGetAuthenticationContext(sp, isExternIdp);

        frameworkUtils.when(() -> FrameworkUtils.isLoginFailureWithNoLocalAssociationEnabledForApp(
                        any(ServiceProvider.class)))
                .thenCallRealMethod();
        doReturn(localUser).when(federatedAssociationManager).getAssociatedLocalUser(
                anyString(), anyString(), anyString());
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
        frameworkUtils.when(
                FrameworkUtils::getStepBasedSequenceHandler).thenReturn(mock(StepBasedSequenceHandler.class));
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = null;
        boolean isPostAuthenticationHandlerError = false;
        try {
            postAuthnHandlerFlowStatus =
                    postAuthAssociationVerificationHandler.handle(request, response,
                            context);
        } catch (PostAuthenticationFailedException e) {
            isPostAuthenticationHandlerError = true;
        }
        if (!isError) {
            Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                    "Post Association handler did not fail an expected scenario.");
        } else {
            Assert.assertTrue(isPostAuthenticationHandlerError);
        }
    }

    @DataProvider(name = "provideTestScenarios")
    public Object[][] provideTestScenarios() {

        return new Object[][]{
                // appVersion, isExternIdp, isLocalSubjectMandatory, localUser, isError
                {APP_VERSION_V2, true, true, null, false},
                {LATEST_APP_VERSION, true, true, new User(), false},
                {LATEST_APP_VERSION, true, true, null, true},
                {LATEST_APP_VERSION, false, true, null, false},
        };
    }

    /**
     * To get the authentication context to call the handle method of the PostAuthAssociationVerificationHandler.
     *
     * @param sp1           Service Provider.
     * @param isExternalIdp true if the external IdP is used, false otherwise.
     * @return relevant authentication context.
     * @throws FrameworkException Framework Exception.
     */
    private AuthenticationContext processAndGetAuthenticationContext(ServiceProvider sp1, boolean isExternalIdp)
            throws FrameworkException {

        AuthenticationContext context = getAuthenticationContext(sp1);
        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        sequenceConfig.getApplicationConfig().setAlwaysSendMappedLocalSubjectId(true);
        context.setSequenceConfig(sequenceConfig);
        context.setExternalIdP(externalIdPConfig);
        context.setProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED, true);

        ApplicationAuthenticator applicationAuthenticator = mock(FederatedApplicationAuthenticator.class);
        when(applicationAuthenticator.getName()).thenReturn("Authenticator1");

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("authenticated-user");
        authenticatedUser.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        authenticatedUser.setAuthenticatedSubjectIdentifier("authenticated-user-id");
        sequenceConfig.setAuthenticatedUser(authenticatedUser);

        if (isExternalIdp) {
            context.setExternalIdP(externalIdPConfig);
        } else {
            context.setExternalIdP(null);
        }

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setApplicationAuthenticator(applicationAuthenticator);
        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            stepConfig.setAuthenticatedAutenticator(authenticatorConfig);
            stepConfig.setAuthenticatedUser(authenticatedUser);
        }
        context.setSequenceConfig(sequenceConfig);
        return context;
    }

    @AfterClass
    protected void tearDown() {

        frameworkUtils.close();
        configurationFacade.close();
        claimMetadataHandler.close();
        identityTenantUtil.close();
        adminServicesUtil.close();
    }
}
