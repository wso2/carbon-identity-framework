/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
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
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManagerImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.core.util.IdentityUtil.getLocalGroupsClaimURI;

/**
 * This is a test class for {@link PostAuthAssociationHandler}.
 */
public class PostAuthAssociationHandlerTest extends AbstractFrameworkTest {

    public static final String LOCAL_USER = "local-user";
    public static final String SECONDARY = "SECONDARY";
    private UIBasedConfigurationLoader configurationLoader;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PostAuthAssociationHandler postAuthAssociationHandler;
    private ServiceProvider sp;
    private IdentityConfigParser mockIdentityConfigParser;
    private static final String ORI_ROLE_1 = "Internal/everyone";
    private static final String ORI_ROLE_2 = "locnomrole";
    private static final String SP_MAPPED_ROLE_1 = "everyone";
    private static final String SP_MAPPED_ROLE_2 = "splocnomrole";

    private MockedStatic<FrameworkUtils> frameworkUtils;
    private MockedStatic<ConfigurationFacade> configurationFacade;
    private MockedStatic<ClaimMetadataHandler> claimMetadataHandler;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<AdminServicesUtil> adminServicesUtil;
    private MockedStatic<IdentityConfigParser> identityConfigParser;

    @BeforeMethod
    protected void setupSuite() throws Exception {

        initAuthenticators();

        configurationLoader = new UIBasedConfigurationLoader();
        frameworkUtils = mockStatic(FrameworkUtils.class);
        configurationFacade = mockStatic(ConfigurationFacade.class);
        claimMetadataHandler = mockStatic(ClaimMetadataHandler.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        adminServicesUtil = mockStatic(AdminServicesUtil.class);

        mockIdentityConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);
        setAuthenticatorActionEnableStatus(false);

        ConfigurationFacade mockConfigurationFacade = mock(ConfigurationFacade.class);

        configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigurationFacade);

        ClaimMetadataHandler mockClaimMetadataHandler = mock(ClaimMetadataHandler.class);
        claimMetadataHandler.when(ClaimMetadataHandler::getInstance).thenReturn(mockClaimMetadataHandler);
        Map<String, String> emptyMap = new HashMap<>();
        when(mockClaimMetadataHandler.getMappingsMapFromOtherDialectToCarbon(Mockito.anyString(),
                Mockito.anySet(), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(emptyMap);

        IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
        ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
        Mockito.doReturn(externalIdPConfig).when(mockConfigurationFacade).getIdPConfigByName(anyString(), anyString());
        frameworkUtils.when(() -> FrameworkUtils.isStepBasedSequenceHandlerExecuted(any(AuthenticationContext.class)))
                .thenCallRealMethod();
        frameworkUtils.when(() -> FrameworkUtils.prependUserStoreDomainToName(anyString()))
                .thenCallRealMethod();
        frameworkUtils.when(() -> FrameworkUtils.buildClaimMappings(anyMap())).thenCallRealMethod();
        frameworkUtils.when(() -> FrameworkUtils.getStandardDialect(anyString(), any(ApplicationConfig.class)))
                .thenCallRealMethod();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        postAuthAssociationHandler = PostAuthAssociationHandler.getInstance();
        sp = getTestServiceProvider("default-sp-1.xml");

        frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");
        ClaimHandler claimHandler = mock(ClaimHandler.class);
        Map<String, String> claims = new HashMap<>();
        claims.put("claim1", "value1");
        claims.put(FrameworkConstants.LOCAL_ROLE_CLAIM_URI, String.format("%s,%s", ORI_ROLE_1, ORI_ROLE_2));
        when(claimHandler.handleClaimMappings(any(StepConfig.class),
                any(AuthenticationContext.class), eq(null), anyBoolean())).thenReturn(claims);
        frameworkUtils.when(FrameworkUtils::getClaimHandler).thenReturn(claimHandler);
    }

    @AfterMethod
    protected void tearDown() {

        frameworkUtils.close();
        configurationFacade.close();
        claimMetadataHandler.close();
        identityTenantUtil.close();
        adminServicesUtil.close();
        identityConfigParser.close();
    }

    @Test(description = "This test case tests the Post Authentication Association handling flow with an authenticated" +
            " user via federated IDP", dataProvider = "provideTestScenarios")
    public void testHandleWithAuthenticatedUserWithFederatedIdpAssociatedToSecondaryUserStore(boolean hasSpRoleMapping)
            throws Exception {

        adminServicesUtil.when(AdminServicesUtil::getUserRealm).thenReturn(null);
        AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true, hasSpRoleMapping);
        FederatedAssociationManager federatedAssociationManager = mock(FederatedAssociationManagerImpl.class);
        frameworkUtils.when(FrameworkUtils::getFederatedAssociationManager).thenReturn(federatedAssociationManager);
        doReturn(SECONDARY + "/" + LOCAL_USER).when(federatedAssociationManager).getUserForFederatedAssociation
                (Mockito.anyString(), eq(null), Mockito.anyString());
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);

        frameworkUtils.when(
                FrameworkUtils::getStepBasedSequenceHandler).thenReturn(Mockito.mock(StepBasedSequenceHandler.class));
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postAuthAssociationHandler.handle(request, response,
                context);
        AuthenticatedUser authUser = context.getSequenceConfig().getAuthenticatedUser();
        Assert.assertEquals(authUser.getUserName(), LOCAL_USER, "Post Association handler failed to set associated " +
                "username");
        Assert.assertEquals(authUser.getUserStoreDomain(), SECONDARY, "Post Association handler failed to set " +
                "associated user's domain");
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post Association handler failed to execute with an associated user in a secondary user store.");
        if (hasSpRoleMapping) {
            Assert.assertTrue(isSpRoleMappingSuccessful(authUser.getUserAttributes()), "SP role mapping failed.");
        }
    }

    @DataProvider(name = "provideTestScenarios")
    public Object[][] provideTestScenarios() {

        return new Object[][]{
                {false},
                {true}
        };
    }

    @Test(description = "Test resolveLocalUserLinkingStrategy method with different ClaimConfig scenarios",
            dataProvider = "provideUserLinkStrategyTestData")
    public void testResolveLocalUserLinkingStrategy(ClaimConfig claimConfig,
                                                    PostAuthAssociationHandler.UserLinkStrategy expectedStrategy) {

        try {
            Method method = PostAuthAssociationHandler.class.getDeclaredMethod("resolveLocalUserLinkingStrategy",
                    ClaimConfig.class);
            method.setAccessible(true);
            PostAuthAssociationHandler.UserLinkStrategy actualStrategy =
                    (PostAuthAssociationHandler.UserLinkStrategy) method.invoke(null, claimConfig);

            Assert.assertEquals(actualStrategy, expectedStrategy,
                    "User link strategy resolution failed for the given claim config");
        } catch (Exception e) {
            Assert.fail("Reflection access to resolveLocalUserLinkingStrategy method failed", e);
        }
    }

    @DataProvider(name = "provideUserLinkStrategyTestData")
    public Object[][] provideUserLinkStrategyTestData() {

        // Test case 1: null ClaimConfig should return DISABLED
        ClaimConfig nullClaimConfig = null;

        // Test case 2: ClaimConfig with mappedLocalSubjectMandatory = true should return MANDATORY
        ClaimConfig mandatoryClaimConfig = mock(ClaimConfig.class);
        when(mandatoryClaimConfig.isMappedLocalSubjectMandatory()).thenReturn(true);

        // Test case 3: ClaimConfig with alwaysSendMappedLocalSubjectId = true should return OPTIONAL
        ClaimConfig optionalClaimConfig = mock(ClaimConfig.class);
        when(optionalClaimConfig.isMappedLocalSubjectMandatory()).thenReturn(false);
        when(optionalClaimConfig.isAlwaysSendMappedLocalSubjectId()).thenReturn(true);

        // Test case 4: ClaimConfig with both flags false should return DISABLED
        ClaimConfig disabledClaimConfig = mock(ClaimConfig.class);
        when(disabledClaimConfig.isMappedLocalSubjectMandatory()).thenReturn(false);
        when(disabledClaimConfig.isAlwaysSendMappedLocalSubjectId()).thenReturn(false);

        return new Object[][]{
                {nullClaimConfig, PostAuthAssociationHandler.UserLinkStrategy.DISABLED},
                {mandatoryClaimConfig, PostAuthAssociationHandler.UserLinkStrategy.MANDATORY},
                {optionalClaimConfig, PostAuthAssociationHandler.UserLinkStrategy.OPTIONAL},
                {disabledClaimConfig, PostAuthAssociationHandler.UserLinkStrategy.DISABLED}
        };
    }

    /**
     * To get the authentication context and to call the handle method of the PostAuthAssociationHandler.
     *
     * @param sp1 Service Provider
     * @return relevant authentication context.
     * @throws FrameworkException Framework Exception.
     */
    private AuthenticationContext processAndGetAuthenticationContext(ServiceProvider sp1, boolean
            withAuthenticatedUser, boolean isFederated, boolean withSpRoleMapping) throws FrameworkException {

        AuthenticationContext context = getAuthenticationContext(sp1);
        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        sequenceConfig.getApplicationConfig().setAlwaysSendMappedLocalSubjectId(true);
        context.setSequenceConfig(sequenceConfig);
        context.setProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED, true);

        ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);

        if (isFederated) {
            applicationAuthenticator = mock(FederatedApplicationAuthenticator.class);
        }
        when(applicationAuthenticator.getName()).thenReturn("Authenticator1");

        if (withAuthenticatedUser) {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            authenticatedUser.setUserName("federated");
            authenticatedUser.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            authenticatedUser.setAuthenticatedSubjectIdentifier("federated");
            sequenceConfig.setAuthenticatedUser(authenticatedUser);

            AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
            authenticatorConfig.setApplicationAuthenticator(applicationAuthenticator);
            for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = entry.getValue();
                stepConfig.setAuthenticatedAutenticator(authenticatorConfig);
                stepConfig.setAuthenticatedUser(authenticatedUser);
            }
            context.setSequenceConfig(sequenceConfig);
        }

        if (withSpRoleMapping) {
            sequenceConfig.getApplicationConfig().getClaimMappings().put(getLocalGroupsClaimURI(),
                    getLocalGroupsClaimURI());
            sequenceConfig.getApplicationConfig().getServiceProvider().getClaimConfig().setLocalClaimDialect(true);
            sequenceConfig.getApplicationConfig().getRoleMappings().put(ORI_ROLE_1, SP_MAPPED_ROLE_1);
            sequenceConfig.getApplicationConfig().getRoleMappings().put(ORI_ROLE_2, SP_MAPPED_ROLE_2);
        }

        return context;
    }

    private boolean isSpRoleMappingSuccessful(Map<ClaimMapping, String> authenticatedUserAttributes) {

        for (Map.Entry<ClaimMapping, String> entry : authenticatedUserAttributes.entrySet()) {
            if (getLocalGroupsClaimURI().equals(entry.getKey().getLocalClaim().getClaimUri())) {
                List<String> roles = Arrays.asList(entry.getValue().split(","));
                return roles.size() == 2 && roles.contains(SP_MAPPED_ROLE_1) && roles.contains(SP_MAPPED_ROLE_2);
            }

        }
        return false;
    }

    @Test(description = "Test PostAuthAssociationHandler for MANDATORY user link strategy throws exception")
    public void testHandleWithMandatoryUserLinkStrategyThrowsException() throws Exception {
        // Setup ClaimConfig to return MANDATORY
        ClaimConfig mandatoryClaimConfig = mock(ClaimConfig.class);
        when(mandatoryClaimConfig.isMappedLocalSubjectMandatory()).thenReturn(true);
        when(mandatoryClaimConfig.isAlwaysSendMappedLocalSubjectId()).thenReturn(false);

        ServiceProvider mockSp = mock(ServiceProvider.class);
        when(mockSp.getClaimConfig()).thenReturn(mandatoryClaimConfig);
        String spName = "test-sp";
        String tenantDomain = "test-tenant";
        configurationFacade.when(() -> ConfigurationFacade.getInstance().getIdPConfigByName(anyString(), anyString()))
                .thenReturn(null);
        frameworkUtils.when(() -> FrameworkUtils.isStepBasedSequenceHandlerExecuted(any(AuthenticationContext.class)))
                .thenReturn(true);
        frameworkUtils.when(() -> FrameworkUtils.getMultiAttributeSeparator()).thenReturn(",");
        frameworkUtils.when(() -> FrameworkUtils.getClaimHandler()).thenReturn(mock(ClaimHandler.class));
        frameworkUtils.when(() -> FrameworkUtils.getStepBasedSequenceHandler())
                .thenReturn(mock(StepBasedSequenceHandler.class));
        frameworkUtils.when(() -> FrameworkUtils.isLoginFailureWithNoLocalAssociationEnabledForApp(
                any(ServiceProvider.class))).thenReturn(true);
        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);

        // Create minimal AuthenticationContext and SequenceConfig, avoid ApplicationConfig
        AuthenticationContext context = new AuthenticationContext();
        context.setTenantDomain(tenantDomain);
        context.setServiceProviderName(spName);
        SequenceConfig sequenceConfig = mock(SequenceConfig.class);
        when(sequenceConfig.getApplicationConfig()).thenReturn(mock(ApplicationConfig.class));
        context.setSequenceConfig(sequenceConfig);
        StepConfig stepConfig = new StepConfig();
        stepConfig.setSubjectIdentifierStep(true);
        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setApplicationAuthenticator(mock(FederatedApplicationAuthenticator.class));
        stepConfig.setAuthenticatedAutenticator(authenticatorConfig);
        Map<Integer, StepConfig> stepMap = new HashMap<>();
        stepMap.put(1, stepConfig);
        when(sequenceConfig.getStepMap()).thenReturn(stepMap);

        // Mock FrameworkServiceDataHolder and ApplicationManagementService
        ApplicationManagementService mockAppMgtService = mock(ApplicationManagementService.class);
        FrameworkServiceDataHolder mockDataHolder = mock(FrameworkServiceDataHolder.class);
        when(mockAppMgtService.getServiceProvider(spName, tenantDomain)).thenReturn(mockSp);
        when(mockDataHolder.getApplicationManagementService()).thenReturn(mockAppMgtService);
        // Static mocking for FrameworkServiceDataHolder.getInstance()
        try (MockedStatic<FrameworkServiceDataHolder> dataHolderStatic = mockStatic(FrameworkServiceDataHolder.class)) {
            dataHolderStatic.when(FrameworkServiceDataHolder::getInstance).thenReturn(mockDataHolder);

            // Should throw PostAuthenticationFailedException
            try {
                postAuthAssociationHandler.handle(request, response, context);
                Assert.fail("Expected PostAuthenticationFailedException was not thrown");
            } catch (PostAuthenticationFailedException e) {
                Assert.assertEquals(e.getErrorCode(), "80030", "Error code mismatch for mandatory user link strategy");
                Assert.assertTrue(e.getMessage().contains("Federated user is not associated with any local user."),
                        "Error message mismatch for mandatory user link strategy");
            }
        }
    }

    private void setAuthenticatorActionEnableStatus(boolean isEnabled) {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.Authentication.Enable", Boolean.toString(isEnabled));
        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);
    }

    private void initAuthenticators() {

        removeAllSystemDefinedAuthenticators();
        ApplicationAuthenticatorManager authenticatorManager = ApplicationAuthenticatorManager.getInstance();
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("BasicMockAuthenticator"));
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("HwkMockAuthenticator"));
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("FptMockAuthenticator"));
    }
}
