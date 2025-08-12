/*
 * Copyright (c) 2018-2025, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.MockAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
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
import org.wso2.carbon.identity.application.common.model.AccountLookupAttributeMappingConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManagerImpl;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is a test class for {@link JITProvisioningPostAuthenticationHandler}.
 */
@Listeners(MockitoTestNGListener.class)
public class JITProvisioningPostAuthenticationHandlerTest extends AbstractFrameworkTest {

    private UIBasedConfigurationLoader configurationLoader;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JITProvisioningPostAuthenticationHandler postJITProvisioningHandler;
    private ServiceProvider sp;

    @Mock
    private FrameworkServiceDataHolder mockFrameworkServiceDataHolder;

    private MockedStatic<FrameworkUtils> frameworkUtils;
    private MockedStatic<ConfigurationFacade> configurationFacade;
    private MockedStatic<CarbonUtils> carbonUtils;
    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMockedStatic;

    private IdentityConfigParser mockIdentityConfigParser;
    private MockedStatic<IdentityConfigParser> identityConfigParser;

    @BeforeClass
    protected void setupSuite() throws XMLStreamException, IdentityProviderManagementException {

        initAuthenticators();
        configurationLoader = new UIBasedConfigurationLoader();

        frameworkUtils = mockStatic(FrameworkUtils.class);
        configurationFacade = mockStatic(ConfigurationFacade.class);
        ConfigurationFacade mockConfigurationFacade = mock(ConfigurationFacade.class);

        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = false;
        configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigurationFacade);
        IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
        ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
        Mockito.doReturn(externalIdPConfig).when(mockConfigurationFacade).getIdPConfigByName(anyString(), anyString());
        frameworkUtils.when(() -> FrameworkUtils
                        .isStepBasedSequenceHandlerExecuted(any(AuthenticationContext.class)))
                .thenCallRealMethod();
        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        postJITProvisioningHandler = JITProvisioningPostAuthenticationHandler.getInstance();
        sp = getTestServiceProvider("default-sp-1.xml");
        carbonUtils = mockStatic(CarbonUtils.class);
        privilegedCarbonContextMockedStatic = mockStatic(PrivilegedCarbonContext.class);

        mockIdentityConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);
        setAuthenticatorActionEnableStatus(false);
    }

    @AfterClass
    protected void cleanup() {
        frameworkUtils.close();
        configurationFacade.close();
        carbonUtils.close();
        privilegedCarbonContextMockedStatic.close();
        identityConfigParser.close();
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow without an authenticated user")
    public void testHandleWithoutAuthenticatedUser() throws FrameworkException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, false, false);
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler.handle(request, response,
                context);
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post JIT provisioning handler executed without having a authenticated user");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow with an authenticated user")
    public void testHandleWithAuthenticatedUserWithoutFederatedIdp() throws FrameworkException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, true, false);

        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler
                .handle(request, response, context);
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post JIT provisioning handler executed while having a authenticated user without federated "
                        + "authenticator");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow with an authenticated user")
    public void testHandleWithAuthenticatedUserWithFederatedIdp() throws FrameworkException,
            FederatedAssociationManagerException, UserStoreException, XMLStreamException,
            IdentityProviderManagementException {

        try (MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            frameworkServiceDataHolder.when(
                    FrameworkServiceDataHolder::getInstance).thenReturn(mockFrameworkServiceDataHolder);
            AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true);
            FederatedAssociationManager federatedAssociationManager = mock(FederatedAssociationManagerImpl.class);
            frameworkUtils.when(FrameworkUtils::getFederatedAssociationManager).thenReturn(federatedAssociationManager);
            frameworkUtils.when(
                            FrameworkUtils::getStepBasedSequenceHandler)
                    .thenReturn(Mockito.mock(StepBasedSequenceHandler.class));

            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);

            // Need to mock getIdPConfigByName with a null parameter.
            ConfigurationFacade mockConfigurationFacade = mock(ConfigurationFacade.class);
            configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigurationFacade);
            IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
            ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
            doReturn(externalIdPConfig).when(mockConfigurationFacade).getIdPConfigByName(eq(null), anyString());

            PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler
                    .handle(request, response, context);
            Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                    "Post JIT provisioning handler executed while having a authenticated user without federated "
                            + "authenticator");
        }
    }

    @DataProvider(name = "UserAccountStatusDataProvider")
    public Object[][] userAccountStatusDataProvider() {

        return new Object[][] {
                { FrameworkConstants.AccountStatus.PENDING_LR , true, PostAuthnHandlerFlowStatus.INCOMPLETE },
                { FrameworkConstants.AccountStatus.PENDING_EV , true, PostAuthnHandlerFlowStatus.INCOMPLETE },
                { FrameworkConstants.AccountStatus.PENDING_SR , true, PostAuthnHandlerFlowStatus.INCOMPLETE },
                { FrameworkConstants.AccountStatus.PENDING_AP , true, PostAuthnHandlerFlowStatus.INCOMPLETE },
                { FrameworkConstants.AccountStatus.PENDING_LR , true, PostAuthnHandlerFlowStatus.INCOMPLETE },
                { null , true, PostAuthnHandlerFlowStatus.INCOMPLETE },
                { null, false, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED },
        };
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow with an associated user",
            dataProvider = "UserAccountStatusDataProvider")
    public void testHandleWithAuthenticatedUserWithPendingVerification(String testAccountState, boolean isAccountLocked,
            PostAuthnHandlerFlowStatus expectedResult) throws FrameworkException,
            FederatedAssociationManagerException, UserStoreException, XMLStreamException,
            IdentityProviderManagementException {

        try (MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            frameworkServiceDataHolder.when(
                    FrameworkServiceDataHolder::getInstance).thenReturn(mockFrameworkServiceDataHolder);
            AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true);
            FederatedAssociationManager federatedAssociationManager = mock(FederatedAssociationManagerImpl.class);
            frameworkUtils.when(FrameworkUtils::getFederatedAssociationManager).thenReturn(federatedAssociationManager);
            frameworkUtils.when(
                            FrameworkUtils::getStepBasedSequenceHandler)
                    .thenReturn(Mockito.mock(StepBasedSequenceHandler.class));
            doReturn("TestUser").when(federatedAssociationManager).getUserForFederatedAssociation(any(),
                    any(), any());

            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            RealmService mockRealmService = mock(RealmService.class);
            UserRealm mockRealm = mock(UserRealm.class);
            when(mockFrameworkServiceDataHolder.getRealmService()).thenReturn(mockRealmService);
            when(mockRealmService.getTenantUserRealm(1)).thenReturn(mockRealm);

            Map<String, String>  userClaimsMap = new HashMap<>();
            if (isAccountLocked) {
                userClaimsMap.put(FrameworkConstants.ACCOUNT_LOCKED_CLAIM_URI, "true");
            }
            if (testAccountState != null) {
                userClaimsMap.put(FrameworkConstants.ACCOUNT_STATE_CLAIM_URI, testAccountState);
            }
            UserStoreManager  mockUserStoreManager = mock(UserStoreManager.class);
            when(mockRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
            when(mockUserStoreManager.getUserClaimValues(eq("TestUser"), any(), anyString()))
                    .thenReturn(userClaimsMap);
            frameworkUtils.when(() -> FrameworkUtils.appendQueryParamsStringToUrl(anyString(), any()))
                    .thenReturn("https://localhost:9443/test");

            // Need to mock getIdPConfigByName with a null parameter.
            ConfigurationFacade mockConfigurationFacade = mock(ConfigurationFacade.class);
            configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigurationFacade);
            IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
            ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
            doReturn(externalIdPConfig).when(mockConfigurationFacade).getIdPConfigByName(eq(null), anyString());

            PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler
                    .handle(request, response, context);
            Assert.assertEquals(postAuthnHandlerFlowStatus, expectedResult, "JIT provisioning handler executed with "
                    + "an associated user with account status: " + testAccountState + " and account locked status: "
                    + isAccountLocked);
        }
    }

    @DataProvider(name = "usernameAutoFillDataProvider")
    public Object[][] usernameAutoFillDataProvider() {

        return new Object[][] {
                {false, true, true},
                {true, false, false},
                {true, true, true}
        };
    }

    @Test(description = "This test case verifies that the username attribute auto-fill functionality works correctly"
            + " when username and password provisioning are enabled.", dataProvider = "usernameAutoFillDataProvider")
    public void testUsernameAutoFillingFunctionality(boolean isUsernameModifiable, boolean isUsernameAutoFillEnabled,
                                                     boolean usernameShouldContainInURL)
            throws FrameworkException, XMLStreamException, IdentityProviderManagementException, IOException {

        try (MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityCoreServiceComponent> identityCoreServiceComponentMockedStatic = mockStatic(
                     IdentityCoreServiceComponent.class)) {
            frameworkServiceDataHolder.when(
                    FrameworkServiceDataHolder::getInstance).thenReturn(mockFrameworkServiceDataHolder);
            AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true);
            FederatedAssociationManager federatedAssociationManager = mock(FederatedAssociationManagerImpl.class);
            frameworkUtils.when(FrameworkUtils::getFederatedAssociationManager).thenReturn(federatedAssociationManager);
            frameworkUtils.when(
                            FrameworkUtils::getStepBasedSequenceHandler)
                    .thenReturn(mock(StepBasedSequenceHandler.class));
            frameworkUtils.when(() -> FrameworkUtils.getMissingClaims(any()))
                    .thenReturn(new String[] {"test-claim", "test-claim1"});
            frameworkUtils.when(FrameworkUtils::isUsernameFieldAutofillWithSubjectAttr)
                    .thenReturn(isUsernameAutoFillEnabled);

            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);

            carbonUtils.when(() -> CarbonUtils.getTransportProxyPort(any(AxisConfiguration.class), any()))
                    .thenReturn(101010);
            carbonUtils.when(CarbonUtils::getManagementTransport).thenReturn("https");

            // Need to mock getIdPConfigByName with a null parameter.
            ConfigurationFacade mockConfigurationFacade = mock(ConfigurationFacade.class);
            configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigurationFacade);
            IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
            JustInTimeProvisioningConfig justInTimeProvisioningConfig =
                    identityProvider.getJustInTimeProvisioningConfig();
            justInTimeProvisioningConfig.setPromptConsent(true);
            justInTimeProvisioningConfig.setModifyUserNameAllowed(isUsernameModifiable);
            identityProvider.setJustInTimeProvisioningConfig(justInTimeProvisioningConfig);
            ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
            doReturn(externalIdPConfig).when(mockConfigurationFacade).getIdPConfigByName(eq(null), anyString());

            ConfigurationContextService configurationContextService = mock(ConfigurationContextService.class);
            ConfigurationContext configurationContext = mock(ConfigurationContext.class);
            when(configurationContextService.getServerConfigContext()).thenReturn(configurationContext);
            identityCoreServiceComponentMockedStatic.when(IdentityCoreServiceComponent::getConfigurationContextService)
                    .thenReturn(configurationContextService);

            PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
            when(privilegedCarbonContext.getTenantDomain()).thenReturn("test-domain");
            privilegedCarbonContextMockedStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                    .thenReturn(privilegedCarbonContext);

            HttpServletResponse mockResponse = mock(HttpServletResponse.class);
            postJITProvisioningHandler.handle(request, mockResponse, context);

            ArgumentCaptor<String> uiRedirectionUrl = ArgumentCaptor.forClass(String.class);
            verify(mockResponse).sendRedirect(uiRedirectionUrl.capture());
            boolean urlContainsUsernameParam = uiRedirectionUrl.getValue().contains("username=test");
            if (usernameShouldContainInURL) {
                Assert.assertTrue(urlContainsUsernameParam);
            } else {
                Assert.assertFalse(urlContainsUsernameParam);
            }
        }
    }

    @DataProvider(name = "accountLookupClaimMappingDataProvider")
    public Object[][] accountLookupClaimMappingDataProvider() {

        return new Object[][]{
                {null, new HashMap<>(), new HashMap<>()},
                {new AccountLookupAttributeMappingConfig[0], new HashMap<>(), new HashMap<>()},
                {createAccountLookupMappings(), createFederatedClaimValues(), createExpectedLocalClaims()},
                {createAccountLookupMappingsWithBlankAttributes(), createFederatedClaimValues(), new HashMap<>()},
                {createAccountLookupMappings(), new HashMap<>(), new HashMap<>()}
        };
    }

    @Test(description = "Test the getLocalClaimsForAccountLookup method with various inputs",
            dataProvider = "accountLookupClaimMappingDataProvider")
    public void testGetLocalClaimsForAccountLookup(AccountLookupAttributeMappingConfig[] mappings,
                                                   Map<ClaimMapping, String> federatedClaims,
                                                   Map<String, String> expectedLocalClaims)
            throws Exception {

        // Use reflection to access the private method
        Method getLocalClaimsForAccountLookupMethod = JITProvisioningPostAuthenticationHandler.class
                .getDeclaredMethod("getLocalClaimsForAccountLookup", Map.class,
                        AccountLookupAttributeMappingConfig[].class);
        getLocalClaimsForAccountLookupMethod.setAccessible(true);

        Map<String, String> result = (Map<String, String>) getLocalClaimsForAccountLookupMethod
                .invoke(postJITProvisioningHandler, federatedClaims, mappings);

        Assert.assertEquals(result, expectedLocalClaims,
                "Local claims for account lookup should match expected values");
    }

    @Test(description = "Test the getLocalUser method with multiple users found scenario")
    public void testGetLocalUserWithMultipleUsersFound() throws Exception {

        try (MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {

            frameworkServiceDataHolder.when(FrameworkServiceDataHolder::getInstance)
                    .thenReturn(mockFrameworkServiceDataHolder);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);

            RealmService mockRealmService = mock(RealmService.class);
            UserRealm mockRealm = mock(UserRealm.class);
            AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
            AbstractUserStoreManager mockPrimaryUserStoreManager = mock(AbstractUserStoreManager.class);

            when(mockFrameworkServiceDataHolder.getRealmService()).thenReturn(mockRealmService);
            when(mockRealmService.getTenantUserRealm(1)).thenReturn(mockRealm);
            when(mockRealm.getUserStoreManager()).thenReturn(mockPrimaryUserStoreManager);

            // Mock the secondary user store manager return for PRIMARY domain
            when(mockPrimaryUserStoreManager.getSecondaryUserStoreManager("PRIMARY")).thenReturn(mockUserStoreManager);

            // Mock multiple users found scenario
            List<org.wso2.carbon.user.core.common.User> multipleUsers = new ArrayList<>();
            multipleUsers.add(mock(org.wso2.carbon.user.core.common.User.class));
            multipleUsers.add(mock(org.wso2.carbon.user.core.common.User.class));

            when(mockUserStoreManager.getUserListWithID(anyString(), anyString(), any()))
                    .thenReturn(multipleUsers);

            Map<String, String> localClaims = new HashMap<>();
            localClaims.put("email", "test@example.com");

            // Use reflection to access the private method
            Method getLocalUserMethod = JITProvisioningPostAuthenticationHandler.class
                    .getDeclaredMethod("getLocalUser", String.class, String.class, Map.class);
            getLocalUserMethod.setAccessible(true);

            try {
                getLocalUserMethod.invoke(postJITProvisioningHandler, "test-tenant", "PRIMARY", localClaims);
                Assert.fail("Expected PostAuthenticationFailedException due to multiple users found");
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                Assert.assertTrue(cause instanceof PostAuthenticationFailedException,
                        "Expected PostAuthenticationFailedException. Actual: " + cause.getClass().getSimpleName());
                PostAuthenticationFailedException exception = (PostAuthenticationFailedException) cause;
                // Check that the exception indicates a multiple matching accounts error
                Assert.assertTrue(exception.getMessage().contains("Multiple matching local accounts") ||
                                exception.getMessage().contains("Multiple users found") ||
                                exception.getErrorCode().equals("80037"),
                        "Exception should indicate multiple matching accounts error. Actual message: " +
                                exception.getMessage() + ", Error code: " + exception.getErrorCode());
            }
        }
    }

    @Test(description = "Test the email username lookup functionality when user exists")
    public void testEmailUsernameLookupWithExistingUser() throws Exception {

        try (MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolder =
                     mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(
                     org.wso2.carbon.identity.core.util.IdentityUtil.class)) {

            frameworkServiceDataHolder.when(FrameworkServiceDataHolder::getInstance)
                    .thenReturn(mockFrameworkServiceDataHolder);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            identityUtil.when(() -> org.wso2.carbon.identity.core.util.IdentityUtil.getProperty(anyString()))
                    .thenReturn("false");

            AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true);
            FederatedAssociationManager federatedAssociationManager = mock(FederatedAssociationManagerImpl.class);
            frameworkUtils.when(FrameworkUtils::getFederatedAssociationManager).thenReturn(federatedAssociationManager);
            frameworkUtils.when(FrameworkUtils::getStepBasedSequenceHandler)
                    .thenReturn(mock(StepBasedSequenceHandler.class));

            RealmService mockRealmService = mock(RealmService.class);
            UserRealm mockRealm = mock(UserRealm.class);
            AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
            AbstractUserStoreManager mockPrimaryUserStoreManager = mock(AbstractUserStoreManager.class);

            when(mockFrameworkServiceDataHolder.getRealmService()).thenReturn(mockRealmService);
            when(mockRealmService.getTenantUserRealm(1)).thenReturn(mockRealm);
            when(mockRealm.getUserStoreManager()).thenReturn(mockPrimaryUserStoreManager);
            when(mockPrimaryUserStoreManager.getSecondaryUserStoreManager("PRIMARY")).thenReturn(mockUserStoreManager);

            when(mockUserStoreManager.isExistingUser(anyString())).thenReturn(true);

            org.wso2.carbon.user.core.common.User mockUser = mock(org.wso2.carbon.user.core.common.User.class);
            when(mockUser.getUsername()).thenReturn("testuser");
            when(mockUser.getUserStoreDomain()).thenReturn("PRIMARY");
            when(mockUser.getTenantDomain()).thenReturn("carbon.super");
            when(mockUser.getDomainQualifiedUsername()).thenReturn("PRIMARY/testuser");

            List<org.wso2.carbon.user.core.common.User> userList = new ArrayList<>();
            userList.add(mockUser);
            when(mockUserStoreManager.getUser(null, "test@example.com")).thenReturn(mockUser);

            ConfigurationFacade mockConfigurationFacade = mock(ConfigurationFacade.class);
            configurationFacade.when(ConfigurationFacade::getInstance).thenReturn(mockConfigurationFacade);
            IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
            identityProvider.getJustInTimeProvisioningConfig().setAssociateLocalUserEnabled(true);
            // Clear account lookup mappings to trigger email lookup
            identityProvider.getJustInTimeProvisioningConfig().setAccountLookupAttributeMappings(null);
            ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
            doReturn(externalIdPConfig).when(mockConfigurationFacade).getIdPConfigByName(eq(null), anyString());

            Map<String, String> localClaimValues = new HashMap<>();
            localClaimValues.put("http://wso2.org/claims/emailaddress", "test@example.com");
            context.setProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES, localClaimValues);

            PostAuthnHandlerFlowStatus result = postJITProvisioningHandler.handle(request, response, context);
            Assert.assertEquals(result, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                    "Handler should complete successfully when email username lookup finds existing user");
        }
    }

    /**
     * Creates account lookup attribute mappings for testing.
     *
     * @return Array of AccountLookupAttributeMappingConfig.
     */
    private AccountLookupAttributeMappingConfig[] createAccountLookupMappings() {

        AccountLookupAttributeMappingConfig mapping1 = new AccountLookupAttributeMappingConfig();
        mapping1.setFederatedAttribute("remote_email");
        mapping1.setLocalAttribute("local_email");

        AccountLookupAttributeMappingConfig mapping2 = new AccountLookupAttributeMappingConfig();
        mapping2.setFederatedAttribute("remote_username");
        mapping2.setLocalAttribute("local_username");

        return new AccountLookupAttributeMappingConfig[]{mapping1, mapping2};
    }

    /**
     * Creates account lookup attribute mappings with blank attributes for testing.
     *
     * @return Array of AccountLookupAttributeMappingConfig with blank attributes.
     */
    private AccountLookupAttributeMappingConfig[] createAccountLookupMappingsWithBlankAttributes() {

        AccountLookupAttributeMappingConfig mapping1 = new AccountLookupAttributeMappingConfig();
        mapping1.setFederatedAttribute("");
        mapping1.setLocalAttribute("local_email");

        AccountLookupAttributeMappingConfig mapping2 = new AccountLookupAttributeMappingConfig();
        mapping2.setFederatedAttribute("remote_username");
        mapping2.setLocalAttribute("");

        return new AccountLookupAttributeMappingConfig[]{mapping1, mapping2};
    }

    /**
     * Creates a map of federated claims for testing.
     *
     * @return Map of ClaimMapping to claim values.
     */
    private Map<ClaimMapping, String> createFederatedClaimValues() {

        Map<ClaimMapping, String> federatedClaims = new HashMap<>();

        ClaimMapping emailClaimMapping = ClaimMapping.build("remote_email", "remote_email", null, false);
        ClaimMapping usernameClaimMapping = ClaimMapping.build("remote_username", "remote_username", null, false);
        ClaimMapping otherClaimMapping = ClaimMapping.build("remote_other", "remote_other", null, false);

        federatedClaims.put(emailClaimMapping, "test@example.com");
        federatedClaims.put(usernameClaimMapping, "testuser");
        federatedClaims.put(otherClaimMapping, "othervalue");

        return federatedClaims;
    }

    /**
     * Creates expected local claims for testing.
     *
     * @return Map of expected local claims.
     */
    private Map<String, String> createExpectedLocalClaims() {

        Map<String, String> expectedClaims = new HashMap<>();
        expectedClaims.put("local_email", "test@example.com");
        expectedClaims.put("local_username", "testuser");
        return expectedClaims;
    }

    /**
     * To get the authentication context and to call the handle method of the PostJitProvisioningHandler.
     *
     * @param sp1 Service Provider
     * @return relevant authentication context.
     * @throws FrameworkException Framwork Exception.
     */
    private AuthenticationContext processAndGetAuthenticationContext(ServiceProvider sp1, boolean
            withAuthenticatedUser, boolean isFederated) throws FrameworkException {

        AuthenticationContext context = getAuthenticationContext(sp1);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);
        context.setProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED, true);

        ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);

        if (isFederated) {
            applicationAuthenticator = mock(FederatedApplicationAuthenticator.class);
        }
        lenient().when(applicationAuthenticator.getName()).thenReturn("Authenticator1");

        if (withAuthenticatedUser) {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            authenticatedUser.setUserName("test");
            authenticatedUser.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            authenticatedUser.setAuthenticatedSubjectIdentifier("test");
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

        UserCoreUtil.setDomainInThreadLocal("test_domain");
        return context;
    }

    private void initAuthenticators() {

        removeAllSystemDefinedAuthenticators();
        ApplicationAuthenticatorManager authenticatorManager = ApplicationAuthenticatorManager.getInstance();
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("BasicMockAuthenticator"));
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("HwkMockAuthenticator"));
        authenticatorManager.addSystemDefinedAuthenticator(new MockAuthenticator("FptMockAuthenticator"));
    }

    private void setAuthenticatorActionEnableStatus(boolean isEnabled) {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.Authentication.Enable", Boolean.toString(isEnabled));
        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);
    }
}
