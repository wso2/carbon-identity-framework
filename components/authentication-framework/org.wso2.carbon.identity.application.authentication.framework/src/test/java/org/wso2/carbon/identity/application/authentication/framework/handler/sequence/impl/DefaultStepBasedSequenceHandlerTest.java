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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.ImpersonatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.lang.reflect.InvocationTargetException;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ADD_USER_STORE_DOMAIN_TO_GROUPS_CLAIM;
import static org.wso2.carbon.identity.core.util.IdentityUtil.getLocalGroupsClaimURI;
import static org.wso2.carbon.user.core.UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
import static org.wso2.carbon.user.core.UserCoreConstants.USER_STORE_GROUPS_CLAIM;

public class DefaultStepBasedSequenceHandlerTest {

    private static final String SUBJECT_CLAIM_URI_IN_APP_CONFIG = "subjectClaimUriFromAppConfig";
    private static final String AUTH_USER_NAME_IN_SEQUENCE_CONFIG = "userNameInSeq";
    private static final String SP_SUBJECT_CLAIM_VALUE = "spSubject";
    private static final String subjectIdentifier = "subjectID";
    private static final List<String> mappedRoles = Collections.emptyList();
    private static final String TENANT_DOMAIN = "foo.com";
    private static final String PROVISIONING_USERSTORE_CLAIM_URI = "provisioning_user_store_claimUri";
    private static final String PROVISONING_USERSTORE_BY_ID = "PROVISIONING_USER_STORE1";
    private static final String PROVISONING_USERSTORE_BY_CLAIM = "PROVISIONING_USER_STORE2";
    private static final String IDP_ROLE_CLAIM_URI = "idpClaimURI";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_1 = ",";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_2 = ",,,";

    private DefaultStepBasedSequenceHandler stepBasedSequenceHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    ApplicationDAO applicationDAO;

    @Mock
    private ApplicationMgtSystemConfig applicationMgtSystemConfig;

    @Mock
    private ApplicationAuthenticator authenticator;

    @Mock
    private RealmService mockRealmService;

    @Mock
    private TenantManager mockTenantManager;

    @Mock
    private ClaimManager mockClaimManager;

    @Mock
    private RealmConfiguration mockRealmConfiguration;

    private AuthenticationContext context;

    private ThreadLocalProvisioningServiceProvider threadLocalProvisioningSp;

    private static final String FOO_TENANT = "foo.com";
    private static final String XY_USER_STORE_DOMAIN = "XY";

    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);
        stepBasedSequenceHandler = spy(new DefaultStepBasedSequenceHandler());
        context = spy(new AuthenticationContext());
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInstance() throws Exception {

        CommonTestUtils.testSingleton(
                DefaultStepBasedSequenceHandler.getInstance(),
                DefaultStepBasedSequenceHandler.getInstance()
        );
    }

    @DataProvider(name = "spRoleMappingDataProvider")
    public Object[][] provideSpRoleMappingData() {
        return Util.getSpRoleMappingData();
    }

    @Test(dataProvider = "spRoleMappingDataProvider")
    public void testGetServiceProviderMappedUserRoles(Map<String, String> spRoleMappings,
                                                      List<String> localUserRoles,
                                                      String multiAttributeSeparator,
                                                      String expectedRoles) throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<ApplicationMgtSystemConfig> applicationMgtSystemConfig =
                     mockStatic(ApplicationMgtSystemConfig.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(multiAttributeSeparator);

            applicationMgtSystemConfig.when(
                    ApplicationMgtSystemConfig::getInstance).thenReturn(this.applicationMgtSystemConfig);
            when(this.applicationMgtSystemConfig.getApplicationDAO()).thenReturn(applicationDAO);
            identityTenantUtil.when(IdentityTenantUtil::getRealmService).thenReturn(mockRealmService);
            when(mockRealmService.getBootstrapRealmConfiguration()).thenReturn(mockRealmConfiguration);
            SequenceConfig sequenceConfig = Util.mockSequenceConfig(spRoleMappings);
            String mappedRoles =
                    stepBasedSequenceHandler.getServiceProviderMappedUserRoles(sequenceConfig, localUserRoles);
            assertEquals(mappedRoles, expectedRoles, "Service Provider Mapped Role do not have the expect value.");
        }
    }

    @DataProvider(name = "spRoleClaimUriProvider")
    private Object[][] getSpRoleClaimUriData() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(UserCoreConstants.ROLE_CLAIM);
            return new Object[][]{
                    {"SP_ROLE_CLAIM", "SP_ROLE_CLAIM"},
                    {null, getLocalGroupsClaimURI()},
                    {"", getLocalGroupsClaimURI()}
            };
        }
    }

    /*
        Find SP mapped role claim URI among mapped claims
     */
    @Test(dataProvider = "spRoleClaimUriProvider")
    public void testGetSpRoleClaimUri(String spRoleClaimUri,
                                      String expectedRoleClaimUri) throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(UserCoreConstants.ROLE_CLAIM);
            ApplicationConfig appConfig = mock(ApplicationConfig.class);
            when(appConfig.getRoleClaim()).thenReturn(spRoleClaimUri);
            assertEquals(stepBasedSequenceHandler.getSpRoleClaimUri(appConfig), expectedRoleClaimUri);
        }
    }

    @DataProvider(name = "spClaimMappingProvider")
    public Object[][] getSpClaimMappingProvider() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(UserCoreConstants.ROLE_CLAIM);
            return new Object[][]{
                    {       // SP mapped role claim
                            new HashMap<String, String>() {{
                                put("SP_ROLE_CLAIM", getLocalGroupsClaimURI());
                            }},
                            "SP_ROLE_CLAIM"
                    },
                    {       // Role claim not among SP mapped claims
                            new HashMap<String, String>() {{
                                put("SP_CLAIM", "LOCAL_CLAIM");
                            }},
                            getLocalGroupsClaimURI()
                    },
                    {      // No SP mapped claims
                            new HashMap<>(), getLocalGroupsClaimURI()
                    },
                    {
                            null, getLocalGroupsClaimURI()
                    }
            };
        }
    }

    /*
        Get role claim URI from SP mapped claims
     */
    @Test(dataProvider = "spClaimMappingProvider")
    public void testGetSpRoleClaimUriSpMappedClaim(Map<String, String> claimMappings,
                                                   String expectedRoleClaim) throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(UserCoreConstants.ROLE_CLAIM);
            ApplicationConfig appConfig = mock(ApplicationConfig.class);
            when(appConfig.getClaimMappings()).thenReturn(claimMappings);
            String roleClaim = stepBasedSequenceHandler.getSpRoleClaimUri(appConfig);
            assertEquals(roleClaim, expectedRoleClaim);
        }
    }

    @DataProvider(name = "idpRoleClaimUriProvider")
    public Object[][] getIdpRoleClaimUriData() {

        return new Object[][]{
                {"IDP_ROLE_CLAIM", "IDP_ROLE_CLAIM"},
                {"", ""},
                {null, null}
        };
    }

    @DataProvider(name = "idpClaimMappingProvider")
    public Object[][] getIdpClaimMappingsProvider() {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(IdentityUtil::getLocalGroupsClaimURI).thenReturn(UserCoreConstants.ROLE_CLAIM);
            return new Object[][]{
                    {       // SP mapped role claim
                            new ClaimMapping[]{
                                    ClaimMapping.build(getLocalGroupsClaimURI(), "IDP_ROLE_CLAIM", "", true)
                            },
                            "IDP_ROLE_CLAIM"
                    },
                    {       // Role claim not among SP mapped claims
                            new ClaimMapping[]{
                                    ClaimMapping.build("LOCAL_CLAIM", "IDP_CLAIM", "", true)
                            },
                            null
                    },
                    {       // Role claim among claim mappings but remote claim is null
                            new ClaimMapping[]{
                                    ClaimMapping.build(getLocalGroupsClaimURI(), null, null, true)
                            },
                            null
                    },
                    {      // No IDP mapped claims
                            new ClaimMapping[0], null
                    },
                    {
                            null, null
                    }
            };
        }
    }

    @Test
    public void testHandleClaimMappings() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            ClaimHandler claimHandler = Util.mockClaimHandler();
            frameworkUtils.when(FrameworkUtils::getClaimHandler).thenReturn(claimHandler);

            Map<String, String> claims = stepBasedSequenceHandler.handleClaimMappings(
                    null,
                    new AuthenticationContext(),
                    new HashMap<>(),
                    false);
            assertNotNull(claims);
        }
    }

    @Test
    public void testHandleClaimMappingsFailed() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            ClaimHandler claimHandler = mock(ClaimHandler.class);
            doThrow(new FrameworkException("Claim Handling failed"))
                    .when(claimHandler)
                    .handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class),
                            any(Map.class), anyBoolean());

            frameworkUtils.when(FrameworkUtils::getClaimHandler).thenReturn(claimHandler);

            Map<String, String> claims = stepBasedSequenceHandler.handleClaimMappings(
                    null,
                    new AuthenticationContext(),
                    new HashMap<>(),
                    false);

            assertNotNull(claims);
            assertEquals(claims.size(), 0);
        }
    }

    /**
     * Tests that user store domain is correctly appended to groups claim based on configuration.
     * 
     * This test verifies two scenarios:
     * 1. When the configuration is disabled, the groups claim should not be modified.
     * 2. When the configuration is enabled, the user store domain should be prepended to group names.
     *
     * @throws Exception If an error occurs during claim handling.
     */
    @Test
    public void testAppendUserStoreDomainToGroupsClaim() throws Exception {

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<ClaimMetadataHandler> claimMetadataHandler = mockStatic(ClaimMetadataHandler.class);
             MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {

            // Constants for test.
            final String testUserId = "12345";
            final String secondaryUserStore = "SECONDARY";
            final String groupsClaimValue = "role1,role2,role3";
            final String commaSeparator = ",";

            // Setup static mocks.
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN_NAME))
                    .thenReturn(SUPER_TENANT_ID);
            identityUtil.when(IdentityUtil::isGroupsVsRolesSeparationImprovementsEnabled).thenReturn(true);
            identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(PRIMARY_DEFAULT_DOMAIN_NAME);

            // Setup authentication context and configs.
            AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
            SequenceConfig sequenceConfig = mock(SequenceConfig.class);
            ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
            StepConfig stepConfig = mock(StepConfig.class);
            UserRealm userRealm = mock(UserRealm.class);

            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            authenticatedUser.setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
            authenticatedUser.setUserId(testUserId);
            authenticatedUser.setUserStoreDomain(secondaryUserStore);

            when(authenticationContext.getSequenceConfig()).thenReturn(sequenceConfig);
            when(authenticationContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
            when(authenticationContext.getLastAuthenticatedUser()).thenReturn(authenticatedUser);
            when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
            when(stepConfig.getAuthenticatedUser()).thenReturn(authenticatedUser);

            Map<String, String> claimMap = new HashMap<>();
            claimMap.put(USER_STORE_GROUPS_CLAIM, USER_STORE_GROUPS_CLAIM);
            when(applicationConfig.getRequestedClaimMappings()).thenReturn(claimMap);

            // Setup realm service and user store manager.
            FrameworkServiceDataHolder.getInstance().setRealmService(mockRealmService);
            when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);
            when(mockTenantManager.getTenantId(SUPER_TENANT_DOMAIN_NAME)).thenReturn(SUPER_TENANT_ID);
            when(mockRealmService.getTenantUserRealm(SUPER_TENANT_ID)).thenReturn(userRealm);
            when(userRealm.getClaimManager()).thenReturn(mockClaimManager);

            AbstractUserStoreManager mockAbstractUserStoreManager = mock(AbstractUserStoreManager.class);
            when(userRealm.getUserStoreManager()).thenReturn(mockAbstractUserStoreManager);

            Map<String, String> claimValues = new HashMap<>();
            claimValues.put(USER_STORE_GROUPS_CLAIM, groupsClaimValue);
            doReturn(claimValues).when(mockAbstractUserStoreManager).getUserClaimValuesWithID(anyString(),
                    any(String[].class), nullable(String.class));

            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager(anyString()))
                    .thenReturn(mockAbstractUserStoreManager);
            when(mockAbstractUserStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
            when(mockRealmConfiguration.getUserStoreProperty(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR))
                    .thenReturn(commaSeparator);

            org.wso2.carbon.user.api.ClaimMapping[] claimMappings = new org.wso2.carbon.user.api.ClaimMapping[0];
            when(mockClaimManager.getAllClaimMappings(ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT))
                    .thenReturn(claimMappings);

            ClaimMetadataHandler mockClaimMetadataHandlerInstance = mock(ClaimMetadataHandler.class);
            claimMetadataHandler.when(ClaimMetadataHandler::getInstance).thenReturn(mockClaimMetadataHandlerInstance);
            when(mockClaimMetadataHandlerInstance.getMappingsMapFromOtherDialectToCarbon(
                    anyString(), any(), anyString(), anyBoolean())).thenReturn(claimMap);

            // Test scenario 1: Configuration is disabled, groups claim should not be modified.
            Map<String, String> claimsWithoutAppending = stepBasedSequenceHandler.handleClaimMappings(
                    stepConfig,
                    authenticationContext,
                    new HashMap<>(),
                    false);
            assertNotNull(claimsWithoutAppending, "Claims map should not be null.");
            assertNotNull(claimsWithoutAppending.get(USER_STORE_GROUPS_CLAIM),
                    "Groups claim should be present in the claims map.");
            assertFalse(claimsWithoutAppending.get(USER_STORE_GROUPS_CLAIM).contains(secondaryUserStore + "/"),
                    "User store domain should not be appended to groups when configuration is disabled.");

            // Setup for scenario 2.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getUserRealm()).thenReturn(userRealm);

            // Test scenario 2: Configuration is enabled, groups claim should be modified.
            identityUtil.when(() -> IdentityUtil.getProperty(ADD_USER_STORE_DOMAIN_TO_GROUPS_CLAIM))
                    .thenReturn("true");
            Map<String, String> claimsWithAppending = stepBasedSequenceHandler.handleClaimMappings(
                    stepConfig,
                    authenticationContext,
                    new HashMap<>(),
                    false);
            assertNotNull(claimsWithAppending, "Claims map should not be null.");
            assertNotNull(claimsWithAppending.get(USER_STORE_GROUPS_CLAIM),
                    "Groups claim should be present in the claims map.");
            assertTrue(claimsWithAppending.get(USER_STORE_GROUPS_CLAIM).contains(secondaryUserStore + "/"),
                    "User store domain should be appended to groups when configuration is enabled.");
        }
    }

    @DataProvider(name = "idpMappedUserRoleDataProvider")
    public Object[][] getIdpMappedUserRolesData() {

        final String[] idpRoles = new String[]{"IDP_ROLE1", "IDP_ROLE2", "IDP_ROLE3"};
        final String[] localRoles = new String[]{"LOCAL_ROLE1", "LOCAL_ROLE2", "LOCAL_ROLE3"};

        return new Object[][]{
                // AttributeValueMap, IDP Role Claim URI, Exclude Unmapped Roles, Multi Attribute Separator,
                // IDP to Local Role mappings, Expected Output
                {
                        // IDP Role Claim URI is null
                        null,
                        null,
                        true,
                        MULTI_ATTRIBUTE_SEPARATOR_1,
                        null,
                        Collections.emptyList()
                },
                {
                        // IDP Role Claim URI defined but cannot find the value in attributeValueMap
                        null,
                        IDP_ROLE_CLAIM_URI,
                        true,
                        MULTI_ATTRIBUTE_SEPARATOR_1,
                        null,
                        Collections.emptyList()
                },
                {
                        // No IDP to Local claim mappings and exclude unmapped roles
                        new HashMap<String, String>() {{
                            put(IDP_ROLE_CLAIM_URI, StringUtils.join(idpRoles, MULTI_ATTRIBUTE_SEPARATOR_1));
                        }},
                        IDP_ROLE_CLAIM_URI,
                        true,
                        MULTI_ATTRIBUTE_SEPARATOR_1,
                        null,
                        Collections.emptyList()
                },
                {
                        // No IDP to Local claim mappings and we do not exclude unmapped roles
                        new HashMap<String, String>() {{
                            put(IDP_ROLE_CLAIM_URI, StringUtils.join(idpRoles, MULTI_ATTRIBUTE_SEPARATOR_2));
                        }},
                        IDP_ROLE_CLAIM_URI,
                        false,
                        MULTI_ATTRIBUTE_SEPARATOR_2,
                        null,
                        Arrays.asList(idpRoles)
                },
                {
                        // Complete IDP to Local Role Mappings available
                        new HashMap<String, String>() {{
                            put(IDP_ROLE_CLAIM_URI, StringUtils.join(idpRoles, MULTI_ATTRIBUTE_SEPARATOR_2));
                        }},
                        IDP_ROLE_CLAIM_URI,
                        false,
                        MULTI_ATTRIBUTE_SEPARATOR_2,
                        new HashMap<String, String>() {{
                            for (int index = 0; index < idpRoles.length; index++) {
                                put(idpRoles[index], localRoles[index]);
                            }
                        }},
                        Arrays.asList(localRoles)
                },
                {
                        // Partial IDP to Local Role Mappings available and we do not exclude unmapped roles
                        new HashMap<String, String>() {{
                            put(IDP_ROLE_CLAIM_URI, StringUtils.join(idpRoles, MULTI_ATTRIBUTE_SEPARATOR_2));
                        }},
                        IDP_ROLE_CLAIM_URI,
                        false,
                        MULTI_ATTRIBUTE_SEPARATOR_2,
                        new HashMap<String, String>() {{
                            put(idpRoles[0], localRoles[0]);
                            put(idpRoles[1], localRoles[1]);
                        }},
                        Arrays.asList(localRoles[0], localRoles[1], idpRoles[2])
                },
                {
                        // Partial IDP to Local Role Mappings available and we exclude unmapped roles
                        new HashMap<String, String>() {{
                            put(IDP_ROLE_CLAIM_URI, StringUtils.join(idpRoles, MULTI_ATTRIBUTE_SEPARATOR_2));
                        }},
                        IDP_ROLE_CLAIM_URI,
                        true,
                        MULTI_ATTRIBUTE_SEPARATOR_2,
                        new HashMap<String, String>() {{
                            put(idpRoles[0], localRoles[0]);
                            put(idpRoles[1], localRoles[1]);
                        }},
                        Arrays.asList(localRoles[0], localRoles[1])
                }
        };
    }

    @Test
    public void testResetAuthenticationContext() throws Exception {

        AuthenticationContext context = new AuthenticationContext();
        context.setSubject(new AuthenticatedUser());
        context.setStateInfo(mock(AuthenticatorStateInfo.class));
        context.setExternalIdP(mock(ExternalIdPConfig.class));

        Map<String, String> authenticatorProperties = new HashMap<>();
        authenticatorProperties.put("Prop1", "Value1");

        context.setAuthenticatorProperties(authenticatorProperties);
        context.setRetryCount(3);
        context.setRetrying(true);
        context.setCurrentAuthenticator("OIDCAuthenticator");

        FrameworkUtils.resetAuthenticationContext(context);

        assertResetContext(context);
    }

    private void assertResetContext(AuthenticationContext context) {

        assertNull(context.getSubject());
        assertNull(context.getStateInfo());
        assertNull(context.getExternalIdP());
        assertEquals(context.getAuthenticatorProperties().size(), 0);
        assertEquals(context.getRetryCount(), 0);
        assertFalse(context.isRetrying());
        assertNull(context.getCurrentAuthenticator());
    }

    @DataProvider(name = "jitProvisioningDataProvider")
    public Object[][] getJitProvisioningData() {

        return new Object[][]{
                // Provisioning User Store ID , Provisioning User Store Claim URI, External Attribute Map, expected
                // userstore to which user should be provisioned to
                {
                        // Provisioning user store picked from provisioningUserStoreId
                        PROVISONING_USERSTORE_BY_ID,
                        PROVISIONING_USERSTORE_CLAIM_URI,
                        new HashMap<>(),
                        PROVISONING_USERSTORE_BY_ID
                },
                {
                        // Provisioning user store picked from external claims
                        null,
                        PROVISIONING_USERSTORE_CLAIM_URI,
                        new HashMap<String, String>() {{
                            put(PROVISIONING_USERSTORE_CLAIM_URI, PROVISONING_USERSTORE_BY_CLAIM);
                        }},
                        PROVISONING_USERSTORE_BY_CLAIM
                },
                {
                        // Provisioning user store not picked from provisioningUserStoreId or external claims
                        null,
                        PROVISIONING_USERSTORE_CLAIM_URI,
                        new HashMap<>(),
                        null
                }
        };
    }

    @Test(dataProvider = "jitProvisioningDataProvider")
    public void testHandleJitProvisioning(String provisioningUserStoreId,
                                          String provisioningUserStoreClaimUri,
                                          Map<String, String> externalAttributeValues,
                                          String expectedUserStoreToBeProvisioned) throws Exception {

        try (MockedStatic<IdentityApplicationManagementUtil> identityApplicationManagementUtil =
                mockStatic(IdentityApplicationManagementUtil.class);
             MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);) {
            context = getMockedContextForJitProvisioning(provisioningUserStoreId, provisioningUserStoreClaimUri,
                    TENANT_DOMAIN);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

            // Mock the provisioning handler
            ProvisioningHandler provisioningHandler = mock(ProvisioningHandler.class);
            doNothing().when(provisioningHandler)
                    .handle(anyList(), anyString(), anyMap(), captor.capture(), anyString());


            frameworkUtils.when(FrameworkUtils::getProvisioningHandler).thenReturn(provisioningHandler);

            identityApplicationManagementUtil.when(
                    () -> IdentityApplicationManagementUtil.setThreadLocalProvisioningServiceProvider(
                            any(ThreadLocalProvisioningServiceProvider.class))).thenAnswer(new Answer<Object>() {
                public Object answer(InvocationOnMock invocation) throws Throwable {

                    threadLocalProvisioningSp = (ThreadLocalProvisioningServiceProvider) invocation.getArguments()[0];
                    return null;
                }
            });

            stepBasedSequenceHandler
                    .handleJitProvisioning(subjectIdentifier, context, mappedRoles, externalAttributeValues);
            verify(provisioningHandler).handle(anyList(), anyString(), anyMap(), captor.capture(), anyString());

            // check whether the user is provisioned to correct user store
            assertEquals(captor.getValue(), expectedUserStoreToBeProvisioned);
            assertNotNull(threadLocalProvisioningSp);
            assertTrue(threadLocalProvisioningSp.isJustInTimeProvisioning());
            assertEquals(threadLocalProvisioningSp.getTenantDomain(), TENANT_DOMAIN);
            assertEquals(threadLocalProvisioningSp.getClaimDialect(),
                    ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT);
        }
    }

    private AuthenticationContext getMockedContextForJitProvisioning(String provisioningUserStoreId,
                                                                     String provisioningUserStoreClaimUri,
                                                                     String tenantDomain) {

        ExternalIdPConfig externalIdPConfig = spy(new ExternalIdPConfig());
        when(externalIdPConfig.getProvisioningUserStoreId()).thenReturn(provisioningUserStoreId);
        when(externalIdPConfig.getProvisioningUserStoreClaimURI()).thenReturn(provisioningUserStoreClaimUri);

        ApplicationConfig applicationConfig = new ApplicationConfig(new ServiceProvider(), SUPER_TENANT_DOMAIN_NAME);
        applicationConfig.setApplicationName("DUMMY_NAME");

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setApplicationConfig(applicationConfig);

        context.setTenantDomain(tenantDomain);
        context.setSequenceConfig(sequenceConfig);
        context.setExternalIdP(externalIdPConfig);

        return context;
    }

    @Test
    public void testHandleJitProvisioningFailure() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            final String subjectIdentifier = "subjectID";
            final List<String> mappedRoles = Collections.emptyList();
            Map<String, String> externalAttributeValues = Collections.emptyMap();

            context = getMockedContextForJitProvisioning(null, null, null);
            // Mock the provisioning handler
            ProvisioningHandler provisioningHandler = mock(ProvisioningHandler.class);
            doThrow(new FrameworkException("Provisioning Failed")).when(provisioningHandler)
                    .handle(anyList(), anyString(), anyMap(), anyString(), anyString());

            frameworkUtils.when(FrameworkUtils::getProvisioningHandler).thenReturn(provisioningHandler);

            try {
                stepBasedSequenceHandler
                        .handleJitProvisioning(subjectIdentifier, context, mappedRoles, externalAttributeValues);
            } catch (FrameworkException ex) {
                fail("Possible API change. This method did not throw any exception to outside before.");
            }
        }
    }

    /**
     * First step of the sequence is handled
     */
    @Test
    public void testHandleSingleStep() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            // mock the step handler
            StepHandler stepHandler = getMockedStepHandlerForIncompleteStep(true);

            frameworkUtils.when(FrameworkUtils::getStepHandler).thenReturn(stepHandler);

            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

            StepConfig stepConfig = new StepConfig();
            SequenceConfig sequenceConfig = new SequenceConfig();
            sequenceConfig.getStepMap().put(1, stepConfig);
            context.setSequenceConfig(sequenceConfig);

            stepBasedSequenceHandler.handle(request, response, context);
            assertFalse(context.getSequenceConfig().isCompleted());
            assertTrue(context.isRequestAuthenticated());
        }
    }

    @Test
    public void testHandleSingleStepFinish() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class);
             MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            // mock the step handler
            StepHandler stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
            frameworkUtils.when(FrameworkUtils::getStepHandler).thenReturn(stepHandler);

            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

            StepConfig stepConfig = new StepConfig();
            SequenceConfig sequenceConfig = new SequenceConfig();
            sequenceConfig.getStepMap().put(1, stepConfig);
            context.setSequenceConfig(sequenceConfig);

            doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(request, response, context);
            stepBasedSequenceHandler.handle(request, response, context);

            assertTrue(context.getSequenceConfig().isCompleted());
            assertTrue(context.isRequestAuthenticated());
            assertResetContext(context);
        }
    }

    private StepHandler getMockedStepHandlerForSuccessfulRequestAuthentication() throws Exception {

        // mock the step handler
        StepHandler stepHandler = mock(StepHandler.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthenticationContext context = invocationOnMock.getArgument(2);
                StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep());
                stepConfig.setCompleted(true);
                context.setRequestAuthenticated(true);
                return null;
            }
        }).when(stepHandler).handle(any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthenticationContext.class));

        return stepHandler;
    }

    private StepHandler getMockedStepHandlerForIncompleteStep(final boolean isRequestAuthenticated) throws Exception {

        // mock the step handler
        StepHandler stepHandler = mock(StepHandler.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthenticationContext context = invocationOnMock.getArgument(2);
                StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep());
                stepConfig.setCompleted(false);
                context.setRequestAuthenticated(isRequestAuthenticated);
                return null;
            }
        }).when(stepHandler).handle(any(HttpServletRequest.class), any(HttpServletResponse.class),
                any(AuthenticationContext.class));
        return stepHandler;
    }

    @DataProvider(name = "stepData")
    public Object[][] provideStepData() {

        return new Object[][]{
                {
                        // Intermediate step is authenticated
                        true, true
                },
                {
                        // Intermediate step failed to authenticate
                        false, false
                }
        };
    }

    @Test(dataProvider = "stepData")
    public void testHandleLastStep(boolean isRequestAuthenticated,
                                   boolean isOverallAuthenticationSucceeded) throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            StepHandler stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
            frameworkUtils.when(FrameworkUtils::getStepHandler).thenReturn(stepHandler);

            StepConfig firstStep = new StepConfig();
            firstStep.setOrder(1);

            // Second step is completed.
            StepConfig lastStep = new StepConfig();
            lastStep.setOrder(2);
            lastStep.setCompleted(true);

            SequenceConfig sequenceConfig = new SequenceConfig();
            sequenceConfig.getStepMap().put(1, firstStep);
            sequenceConfig.getStepMap().put(2, lastStep);

            doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                    (HttpServletResponse.class), any(AuthenticationContext.class));

            // currently we have completed second step
            context.setCurrentStep(2);
            context.setSequenceConfig(sequenceConfig);
            context.setRequestAuthenticated(isRequestAuthenticated);

            stepBasedSequenceHandler.handle(request, response, context);
            assertResetContext(context);
            assertEquals(context.isRequestAuthenticated(), isOverallAuthenticationSucceeded);
            assertTrue(context.getSequenceConfig().isCompleted());
        }
    }

    @Test
    public void testHandleMultiOptionStep() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            StepHandler stepHandler = getMockedStepHandlerForIncompleteStep(true);
            frameworkUtils.when(FrameworkUtils::getStepHandler).thenReturn(stepHandler);

            StepConfig firstStep = new StepConfig();
            firstStep.setOrder(1);

            // Second step is completed.
            StepConfig lastStep = new StepConfig();
            lastStep.setMultiOption(true);
            lastStep.setOrder(2);
            lastStep.setCompleted(true);

            SequenceConfig sequenceConfig = new SequenceConfig();
            sequenceConfig.getStepMap().put(1, firstStep);
            sequenceConfig.getStepMap().put(2, lastStep);

            doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                    (HttpServletResponse.class), any(AuthenticationContext.class));

            // currently we have completed second step
            context.setCurrentStep(2);
            context.setSequenceConfig(sequenceConfig);
            context.setRequestAuthenticated(false);

            stepBasedSequenceHandler.handle(request, response, context);
            assertResetContext(context);
            // Assert whether the sequence is retrying the step
            assertTrue(context.getSequenceConfig().getStepMap().get(context.getCurrentStep()).isRetrying());
            // Assert whether before retrying the context request authentication status was set to true.
            assertTrue(context.isRequestAuthenticated());

            // step handler completes the step successfully
            stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
            frameworkUtils.when(FrameworkUtils::getStepHandler).thenReturn(stepHandler);

            stepBasedSequenceHandler.handle(request, response, context);
            assertTrue(context.getSequenceConfig().isCompleted());
            assertTrue(context.isRequestAuthenticated());
        }
    }

    /*
        Even though the failed step has multi option, if the request is passive then we should not allow to retry
     */
    @Test
    public void testHandlePassiveAuthenticateWhenMultiOptionStep() throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            StepHandler stepHandler = getMockedStepHandlerForSuccessfulRequestAuthentication();
            frameworkUtils.when(FrameworkUtils::getStepHandler).thenReturn(stepHandler);

            StepConfig firstStep = new StepConfig();
            firstStep.setOrder(1);

            // Second step is completed.
            StepConfig lastStep = new StepConfig();
            lastStep.setMultiOption(true);
            lastStep.setOrder(2);
            lastStep.setCompleted(true);

            SequenceConfig sequenceConfig = new SequenceConfig();
            sequenceConfig.getStepMap().put(1, firstStep);
            sequenceConfig.getStepMap().put(2, lastStep);

            doNothing().when(stepBasedSequenceHandler).handlePostAuthentication(any(HttpServletRequest.class), any
                    (HttpServletResponse.class), any(AuthenticationContext.class));

            // currently we have completed second step
            context.setCurrentStep(2);
            context.setSequenceConfig(sequenceConfig);
            context.setPassiveAuthenticate(true);
            context.setRequestAuthenticated(false);

            stepBasedSequenceHandler.handle(request, response, context);
            assertResetContext(context);
            assertTrue(context.getSequenceConfig().isCompleted());
            assertFalse(context.getSequenceConfig().getStepMap().get(context.getCurrentStep()).isRetrying());
        }
    }

    @DataProvider(name = "postAuthenticationDataProvider")
    public Object[][] providePostAuthenticationSubjectIdentifierData() {

        return new Object[][]{
                // subjectClaimUriFromAppConfig,
                // spSubjectClaimValue,
                // appendTenantDomainToSubject,
                // appendUserStoreDomainToSubject,
                // authenticatedUserNameInSequence
                // expected subject identifier
                {
                        null,
                        null,
                        true,
                        true,
                        null,
                        null
                },
                {
                        // No SP Subject Claim Value
                        SUBJECT_CLAIM_URI_IN_APP_CONFIG,
                        null,
                        false,
                        false,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG
                },
                {
                        null,
                        null,
                        true,
                        false,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        UserCoreUtil.addTenantDomainToEntry(AUTH_USER_NAME_IN_SEQUENCE_CONFIG, FOO_TENANT)
                },
                {
                        null,
                        null,
                        false,
                        true,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        UserCoreUtil.addDomainToName(AUTH_USER_NAME_IN_SEQUENCE_CONFIG, XY_USER_STORE_DOMAIN)
                },
                {
                        null,
                        null,
                        true,
                        true,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        UserCoreUtil.addTenantDomainToEntry(
                                UserCoreUtil.addDomainToName(AUTH_USER_NAME_IN_SEQUENCE_CONFIG, XY_USER_STORE_DOMAIN),
                                FOO_TENANT
                        )
                },
                {
                        SUBJECT_CLAIM_URI_IN_APP_CONFIG,
                        null,
                        true,
                        true,
                        null,
                        null
                },
                {
                        // SP Subject Claim Value present with tenantDomain and UserStoreDomain appending enabled
                        SUBJECT_CLAIM_URI_IN_APP_CONFIG,
                        SP_SUBJECT_CLAIM_VALUE,
                        true,
                        true,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        UserCoreUtil.addDomainToName(
                                UserCoreUtil.addTenantDomainToEntry(SP_SUBJECT_CLAIM_VALUE, FOO_TENANT),
                                XY_USER_STORE_DOMAIN
                        )
                },
                {
                        // SP Subject Claim Value present with tenantDomain appending enabled
                        SUBJECT_CLAIM_URI_IN_APP_CONFIG,
                        SP_SUBJECT_CLAIM_VALUE,
                        true,
                        false,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        UserCoreUtil.addTenantDomainToEntry(SP_SUBJECT_CLAIM_VALUE, FOO_TENANT)
                },
                {
                        // SP Subject Claim Value present with userStoreDomain appending enabled
                        SUBJECT_CLAIM_URI_IN_APP_CONFIG,
                        SP_SUBJECT_CLAIM_VALUE,
                        false,
                        true,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        UserCoreUtil.addDomainToName(SP_SUBJECT_CLAIM_VALUE, XY_USER_STORE_DOMAIN)

                },
                {
                        // SP Subject Claim Value present
                        SUBJECT_CLAIM_URI_IN_APP_CONFIG,
                        SP_SUBJECT_CLAIM_VALUE,
                        false,
                        false,
                        AUTH_USER_NAME_IN_SEQUENCE_CONFIG,
                        SP_SUBJECT_CLAIM_VALUE
                }
        };
    }

    @Test(dataProvider = "postAuthenticationDataProvider")
    public void testHandlePostUserName(String subjectClaimUriFromAppConfig,
                                       String spSubjectClaimValue,
                                       boolean appendTenantDomainToSubject,
                                       boolean appendUserStoreDomainToSubject,
                                       String authenticatedUserNameInSequence,
                                       String expectedSubjectIdentifier) throws Exception {

        stepBasedSequenceHandler = new DefaultStepBasedSequenceHandler();
        ApplicationConfig applicationConfig =
                spy(new ApplicationConfig(new ServiceProvider(), SUPER_TENANT_DOMAIN_NAME));
        when(applicationConfig.getSubjectClaimUri()).thenReturn(subjectClaimUriFromAppConfig);
        when(applicationConfig.isUseTenantDomainInLocalSubjectIdentifier()).thenReturn(appendTenantDomainToSubject);
        when(applicationConfig.isUseUserstoreDomainInLocalSubjectIdentifier())
                .thenReturn(appendUserStoreDomainToSubject);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName(authenticatedUserNameInSequence);
        authenticatedUser.setTenantDomain(FOO_TENANT);
        authenticatedUser.setUserStoreDomain(XY_USER_STORE_DOMAIN);

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        StepConfig stepConfig = spy(new StepConfig());
        when(stepConfig.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(stepConfig.isSubjectIdentifierStep()).thenReturn(false);
        when(stepConfig.isSubjectAttributeStep()).thenReturn(false);
        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setApplicationAuthenticator(authenticator);
        when(stepConfig.getAuthenticatedAutenticator()).thenReturn(authenticatorConfig);
        stepConfigMap.put(1, stepConfig);
        sequenceConfig.setStepMap(stepConfigMap);
        sequenceConfig.setAuthenticatedUser(authenticatedUser);
        sequenceConfig.setApplicationConfig(applicationConfig);

        // SP subject claim value
        context.setProperty(FrameworkConstants.SERVICE_PROVIDER_SUBJECT_CLAIM_VALUE, spSubjectClaimValue);
        context.setSequenceConfig(sequenceConfig);

        stepBasedSequenceHandler.handlePostAuthentication(request, response, context);

        assertEquals(context.getSequenceConfig().getAuthenticatedUser().getUserName(),
                authenticatedUserNameInSequence);
    }

    @DataProvider
    public static Object[][] dataProviderResolveRequestedSubject() {

        return new Object[][]{
                {"userId", null, null, "userId"},
                {null, "&requested_subject=userId", null, "userId"},
                {null, null, "userId", "userId"},
                {null, null, null, null},
        };
    }

    @Test(dataProvider = "dataProviderResolveRequestedSubject")
    public void testResolveRequestedSubject(String userInReParam, String queryParams, String userInSessionContext,
                                            String results) throws Exception {

        DefaultStepBasedSequenceHandler handler = new DefaultStepBasedSequenceHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthenticationContext context = mock(AuthenticationContext.class);

        // Simulate no request param, no query param, but session context present
        when(request.getParameter("requested_subject")).thenReturn(userInReParam);
        when(context.getQueryParams()).thenReturn(queryParams);
        when(context.getSessionIdentifier()).thenReturn("session123");
        when(context.getLoginTenantDomain()).thenReturn("tenant1");

        SessionContext sessionContext = mock(SessionContext.class);
        when(sessionContext.getImpersonatedUser()).thenReturn(userInSessionContext);

        try (MockedStatic<FrameworkUtils> utils = Mockito.mockStatic(FrameworkUtils.class)) {
            utils.when(() -> FrameworkUtils.getSessionContextFromCache("session123", "tenant1"))
                    .thenReturn(sessionContext);

            Method method = DefaultStepBasedSequenceHandler.class
                    .getDeclaredMethod("resolveRequestedSubject", HttpServletRequest.class,
                            AuthenticationContext.class);
            method.setAccessible(true);
            String result = (String) method.invoke(handler, request, context);

            assertEquals(result, results);
        }
    }


    @DataProvider(name = "dataProviderExtractFromQueryParams")
    public Object[][] dataProviderExtractFromQueryParams() {

        return new Object[][]{
                {"&requested_subject=testUser", "testUser"},
                {"&requested_subject=null", null},
                {null, null},
        };
    }

    @Test(dataProvider = "dataProviderExtractFromQueryParams")
    public void testExtractFromQueryParams(String queryParam, String requestedSubject)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        DefaultStepBasedSequenceHandler handler = new DefaultStepBasedSequenceHandler();
        AuthenticationContext context = Mockito.mock(AuthenticationContext.class);

        Mockito.when(context.getQueryParams()).thenReturn(queryParam);
        Method method = DefaultStepBasedSequenceHandler.class
                .getDeclaredMethod("extractFromQueryParams", AuthenticationContext.class);
        method.setAccessible(true);
        String result = (String) method.invoke(handler, context);

        assertEquals(result, requestedSubject);
    }

    @DataProvider(name = "impersonatedUserProvider")
    public Object[][] impersonatedUserProvider() {

        return new Object[][]{
                // requestedSubject, hasSeqConfig, hasAppConfig, expectNull
                {"subject1", true, true, false},
                {"subject2", false, true, true},
                {"subject3", true, false, true},
        };
    }

    @Test(dataProvider = "impersonatedUserProvider")
    public void testGetImpersonatedUser(String requestedSubject, boolean hasSeqConfig, boolean hasAppConfig,
                                        boolean expectNull) throws Exception {

        DefaultStepBasedSequenceHandler handler = new DefaultStepBasedSequenceHandler();
        AuthenticationContext context = mock(AuthenticationContext.class);
        AuthenticatedUser impersonatingActor = mock(AuthenticatedUser.class);

        SequenceConfig sequenceConfig = hasSeqConfig ? mock(SequenceConfig.class) : null;
        ApplicationConfig applicationConfig = hasAppConfig ? mock(ApplicationConfig.class) : null;

        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        if (sequenceConfig != null) {
            when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        }
        when(context.getTenantDomain()).thenReturn("tenant1");
        when(impersonatingActor.getAccessingOrganization()).thenReturn("orgA");
        when(impersonatingActor.getUserResidentOrganization()).thenReturn("orgB");

        try (MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = Mockito.mockStatic(FrameworkUtils.class)) {
            if (!expectNull) {
                ImpersonatedUser impersonated = mock(ImpersonatedUser.class);
                frameworkUtilsMockedStatic.when(
                        () -> FrameworkUtils.getImpersonatedUser(requestedSubject, "tenant1", "orgA", "orgB",
                                applicationConfig)).thenReturn(impersonated);
            }

            Method method = DefaultStepBasedSequenceHandler.class.getDeclaredMethod(
                    "getImpersonatedUser", String.class, AuthenticationContext.class, AuthenticatedUser.class);
            method.setAccessible(true);
            Object result = method.invoke(handler, requestedSubject, context, impersonatingActor);

            if (expectNull) {
                assertNull(result);
            } else {
                assertNotNull(result);
                assertTrue(result instanceof ImpersonatedUser);
            }
        }
    }
}
