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

package org.wso2.carbon.identity.user.pre.update.password.action.execution.versioning.v2;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.realm.UserStoreModel;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionRequestDTO;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PasswordSharing;
import org.wso2.carbon.identity.user.pre.update.password.action.api.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.common.model.Credential;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.common.model.PasswordUpdatingUser;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v2.PreUpdatePasswordRequestBuilderV2;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.versioning.v2.model.PreUpdatePasswordEvent;
import org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.common.testng.TestConstants.TENANT_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.CLAIM1;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.CLAIM2;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.CLAIM3;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.CLAIM4;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.CLAIM5;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.CLAIM6;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.CLAIM7;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.GROUPS;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.Claims.ROLES;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.ROOT_ORG_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.ROOT_ORG_TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.ROOT_ORG_TENANT_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACCESSING_ORG_DEPTH;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACCESSING_ORG_HANDLE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACCESSING_ORG_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACCESSING_ORG_NAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ACTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_CERTIFICATE_NAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_DESCRIPTION;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_RESIDENT_ORG_DEPTH;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_RESIDENT_ORG_HANDLE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_RESIDENT_ORG_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_RESIDENT_ORG_NAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_SAMPLE_CERTIFICATE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_URL;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USERNAME;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USER_STORE_DOMAIN_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_USER_STORE_DOMAIN_NAME;

/**
 * Unit tests for PreUpdatePasswordActionRequestBuilderV2, verifying request building and related logic.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {PreUpdatePasswordActionServiceComponentHolder.class},
        initUserStoreManager = true)
public class PreUpdatePasswordActionRequestBuilderV2Test {

    private PreUpdatePasswordAction preUpdatePasswordAction;
    private PreUpdatePasswordAction preUpdatePasswordActionWithoutCert;
    private UserActionContext userActionContext;
    private UserStoreModel userStoreModel;
    private Organization userResidentOrganization;
    private org.wso2.carbon.identity.core.context.model.Organization accessingOrganization;
    private org.wso2.carbon.identity.core.context.model.RootOrganization rootOrganization;
    private final FlowContext flowContext = FlowContext.create();
    private PreUpdatePasswordRequestBuilderV2 preUpdatePasswordActionRequestBuilderV2;
    private ClaimMetadataManagementService claimMetadataManagementService;
    private MockedStatic<FrameworkUtils> frameworkUtils;

    @BeforeClass
    public void init() throws Exception {

        frameworkUtils = mockStatic(FrameworkUtils.class);
        frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");

        claimMetadataManagementService = mock(ClaimMetadataManagementService.class);

        for (TestUtil.Claims claim : TestUtil.Claims.values()) {
            LocalClaim localClaim = getMockedLocalClaim(claim);
            when(claimMetadataManagementService.getLocalClaim(claim.getClaimURI(), TENANT_DOMAIN))
                    .thenReturn(Optional.of(localClaim));
        }

        PreUpdatePasswordActionServiceComponentHolder.getInstance()
                .setClaimManagementService(claimMetadataManagementService);

        userResidentOrganization = new Organization.Builder()
                .id(TEST_RESIDENT_ORG_ID)
                .name(TEST_RESIDENT_ORG_NAME)
                .orgHandle(TEST_RESIDENT_ORG_HANDLE)
                .depth(TEST_RESIDENT_ORG_DEPTH)
                .build();

        userActionContext = new UserActionContext(new UserActionRequestDTO.Builder()
                .userId(TEST_ID)
                .password(TEST_PASSWORD.toCharArray())
                .userStoreDomain(TEST_USER_STORE_DOMAIN_NAME)
                .residentOrganization(userResidentOrganization)
                .build());

        preUpdatePasswordAction = new PreUpdatePasswordAction.ResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .actionVersion("v1")
                .description(TEST_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .passwordSharing(new PasswordSharing.Builder()
                        .format(PasswordSharing.Format.SHA256_HASHED)
                        .certificate(new Certificate.Builder()
                                .id(TEST_CERTIFICATE_ID)
                                .name(TEST_CERTIFICATE_NAME)
                                .certificateContent(TEST_SAMPLE_CERTIFICATE)
                                .build())
                        .build())
                .attributes(Arrays.asList(CLAIM1.getClaimURI(), CLAIM2.getClaimURI(), CLAIM3.getClaimURI(),
                        CLAIM4.getClaimURI(), CLAIM5.getClaimURI(), CLAIM6.getClaimURI(), CLAIM7.getClaimURI(),
                        GROUPS.getClaimURI(), ROLES.getClaimURI()))
                .build();

        preUpdatePasswordActionWithoutCert = new PreUpdatePasswordAction.ResponseBuilder()
                .id(TEST_ID)
                .name(TEST_ACTION)
                .actionVersion("v1")
                .description(TEST_DESCRIPTION)
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(TEST_URL)
                        .authentication(new Authentication.BasicAuthBuilder(TEST_USERNAME, TEST_PASSWORD).build())
                        .build())
                .passwordSharing(new PasswordSharing.Builder()
                        .format(PasswordSharing.Format.PLAIN_TEXT)
                        .build())
                .build();

        rootOrganization = new RootOrganization.Builder()
                .associatedTenantId(ROOT_ORG_TENANT_ID)
                .associatedTenantDomain(ROOT_ORG_TENANT_DOMAIN)
                .organizationId(ROOT_ORG_ID)
                .build();
        accessingOrganization = new org.wso2.carbon.identity.core.context.model.Organization.Builder()
                .id(TEST_ACCESSING_ORG_ID)
                .name(TEST_ACCESSING_ORG_NAME)
                .organizationHandle(TEST_ACCESSING_ORG_HANDLE)
                .depth(TEST_ACCESSING_ORG_DEPTH)
                .build();
    }

    @AfterClass
    public void cleanUp() {

        frameworkUtils.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        userStoreModel = createUserStoreModel();
        userStoreModel.bindToRealm();

        preUpdatePasswordActionRequestBuilderV2 = new PreUpdatePasswordRequestBuilderV2();
        flowContext.add(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT, userActionContext);

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                TENANT_DOMAIN);
        IdentityContext.getThreadLocalIdentityContext().setRootOrganization(rootOrganization);
    }

    @AfterMethod
    public void tearDown() {

        userStoreModel.unBindFromRealm();
        IdentityContext.destroyCurrentContext();
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void getSupportedActionType() {

        assertEquals(preUpdatePasswordActionRequestBuilderV2.getSupportedActionType(), ActionType.PRE_UPDATE_PASSWORD);
    }

    @DataProvider(name = "flowData")
    public Object[][] flowData() {

        return new Object[][]{
                {false, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER),
                        PreUpdatePasswordEvent.Action.UPDATE, PreUpdatePasswordEvent.FlowInitiatorType.USER},
                {false, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.APPLICATION),
                        PreUpdatePasswordEvent.Action.UPDATE, PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION},
                {false, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.UPDATE, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN},
                {false, buildMockedFlow(Flow.Name.CREDENTIAL_RESET, Flow.InitiatingPersona.USER),
                        PreUpdatePasswordEvent.Action.RESET, PreUpdatePasswordEvent.FlowInitiatorType.USER},
                {false, buildMockedFlow(Flow.Name.CREDENTIAL_RESET, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.RESET, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN},
                {false, buildMockedFlow(Flow.Name.INVITE, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.INVITE, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN},
                {false, buildMockedFlow(Flow.Name.INVITED_USER_REGISTRATION, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.INVITE, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN},
                {true, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER),
                        PreUpdatePasswordEvent.Action.UPDATE, PreUpdatePasswordEvent.FlowInitiatorType.USER},
                {true, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.APPLICATION),
                        PreUpdatePasswordEvent.Action.UPDATE, PreUpdatePasswordEvent.FlowInitiatorType.APPLICATION},
                {true, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.UPDATE, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN},
                {true, buildMockedFlow(Flow.Name.CREDENTIAL_RESET, Flow.InitiatingPersona.USER),
                        PreUpdatePasswordEvent.Action.RESET, PreUpdatePasswordEvent.FlowInitiatorType.USER},
                {true, buildMockedFlow(Flow.Name.CREDENTIAL_RESET, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.RESET, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN},
                {true, buildMockedFlow(Flow.Name.INVITE, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.INVITE, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN},
                {true, buildMockedFlow(Flow.Name.INVITED_USER_REGISTRATION, Flow.InitiatingPersona.ADMIN),
                        PreUpdatePasswordEvent.Action.INVITE, PreUpdatePasswordEvent.FlowInitiatorType.ADMIN}
        };
    }

    @Test(dataProvider = "flowData")
    public void testRequestBuilder(boolean isOrganizationFlow, Flow mockedFlow,
                                   PreUpdatePasswordEvent.Action expectedAction,
                                   PreUpdatePasswordEvent.FlowInitiatorType expectedInitiatorType)
            throws ActionExecutionRequestBuilderException {

        if (isOrganizationFlow) {
            IdentityContext.getThreadLocalIdentityContext().setOrganization(accessingOrganization);
        }

        IdentityContext.getThreadLocalIdentityContext().enterFlow(mockedFlow);
        ActionExecutionRequest actionExecutionRequest =
                preUpdatePasswordActionRequestBuilderV2.buildActionExecutionRequest(
                        flowContext, ActionExecutionRequestContext.create(preUpdatePasswordAction));

        assertNotNull(actionExecutionRequest);
        assertEquals(actionExecutionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD);
        assertTrue(actionExecutionRequest.getEvent() instanceof PreUpdatePasswordEvent);

        PreUpdatePasswordEvent preUpdatePasswordEvent = (PreUpdatePasswordEvent) actionExecutionRequest.getEvent();
        assertEquals(preUpdatePasswordEvent.getInitiatorType(), expectedInitiatorType);
        assertEquals(preUpdatePasswordEvent.getAction(), expectedAction);

        assertEquals(preUpdatePasswordEvent.getUserStore().getName(), TEST_USER_STORE_DOMAIN_NAME);
        assertEquals(preUpdatePasswordEvent.getUserStore().getId(), TEST_USER_STORE_DOMAIN_ID);

        assertTrue(preUpdatePasswordEvent.getUser() instanceof PasswordUpdatingUser);
        PasswordUpdatingUser passwordUpdatingUser = (PasswordUpdatingUser) preUpdatePasswordEvent.getUser();
        assertEquals(passwordUpdatingUser.getId(), TEST_ID);
        assertNotNull(passwordUpdatingUser.getUpdatingCredential());
        assertTrue(passwordUpdatingUser.getUpdatingCredential() instanceof String);

        assertNotNull(passwordUpdatingUser.getClaims());
        assertClaims(passwordUpdatingUser.getClaims());

        assertEquals(passwordUpdatingUser.getGroups().size(), 2);
        assertEquals(passwordUpdatingUser.getGroups(),
                Arrays.asList(GROUPS.getValueInUserStore().split(Pattern.quote(","))));
        assertEquals(passwordUpdatingUser.getRoles().size(), 0);

        assertEquals(preUpdatePasswordEvent.getTenant().getId(), String.valueOf(ROOT_ORG_TENANT_ID));
        assertEquals(preUpdatePasswordEvent.getTenant().getName(), ROOT_ORG_TENANT_DOMAIN);

        if (isOrganizationFlow) {
            assertEquals(preUpdatePasswordEvent.getOrganization().getId(), accessingOrganization.getId());
            assertEquals(preUpdatePasswordEvent.getOrganization().getName(), accessingOrganization.getName());
            assertEquals(preUpdatePasswordEvent.getOrganization().getOrgHandle(),
                    accessingOrganization.getOrganizationHandle());
            assertEquals(preUpdatePasswordEvent.getOrganization().getDepth(), accessingOrganization.getDepth());
            assertEquals(preUpdatePasswordEvent.getUser().getOrganization(), userResidentOrganization);
        } else {
            assertNull(preUpdatePasswordEvent.getOrganization());
        }
    }

    @Test(dataProvider = "flowData")
    public void testRequestBuilderWithUnEncryptedCredential(boolean isOrganizationFlow, Flow mockedFlow,
                                                            PreUpdatePasswordEvent.Action expectedAction,
                                                            PreUpdatePasswordEvent.FlowInitiatorType
                                                                        expectedInitiatorType)
            throws ActionExecutionRequestBuilderException {

        if (isOrganizationFlow) {
            IdentityContext.getThreadLocalIdentityContext().setOrganization(accessingOrganization);
        }

        IdentityContext.getThreadLocalIdentityContext().enterFlow(mockedFlow);
        ActionExecutionRequest actionExecutionRequest =
                preUpdatePasswordActionRequestBuilderV2.buildActionExecutionRequest(
                        flowContext, ActionExecutionRequestContext.create(preUpdatePasswordActionWithoutCert));

        assertNotNull(actionExecutionRequest);
        assertEquals(actionExecutionRequest.getActionType(), ActionType.PRE_UPDATE_PASSWORD);
        assertTrue(actionExecutionRequest.getEvent() instanceof PreUpdatePasswordEvent);

        PreUpdatePasswordEvent preUpdatePasswordEvent = (PreUpdatePasswordEvent) actionExecutionRequest.getEvent();
        assertEquals(preUpdatePasswordEvent.getInitiatorType(), expectedInitiatorType);
        assertEquals(preUpdatePasswordEvent.getAction(), expectedAction);

        assertEquals(preUpdatePasswordEvent.getUserStore().getName(), TEST_USER_STORE_DOMAIN_NAME);
        assertEquals(preUpdatePasswordEvent.getUserStore().getId(), TEST_USER_STORE_DOMAIN_ID);

        assertTrue(preUpdatePasswordEvent.getUser() instanceof PasswordUpdatingUser);
        PasswordUpdatingUser passwordUpdatingUser = (PasswordUpdatingUser) preUpdatePasswordEvent.getUser();
        assertEquals(passwordUpdatingUser.getId(), TEST_ID);
        assertNotNull(passwordUpdatingUser.getUpdatingCredential());
        assertTrue(passwordUpdatingUser.getUpdatingCredential() instanceof Credential);

        assertEquals(passwordUpdatingUser.getClaims().size(), 0);
        assertEquals(passwordUpdatingUser.getGroups().size(), 0);
        assertEquals(passwordUpdatingUser.getRoles().size(), 0);

        assertEquals(preUpdatePasswordEvent.getTenant().getId(), String.valueOf(ROOT_ORG_TENANT_ID));
        assertEquals(preUpdatePasswordEvent.getTenant().getName(), ROOT_ORG_TENANT_DOMAIN);

        if (isOrganizationFlow) {
            assertEquals(preUpdatePasswordEvent.getOrganization().getId(), accessingOrganization.getId());
            assertEquals(preUpdatePasswordEvent.getOrganization().getName(), accessingOrganization.getName());
            assertEquals(preUpdatePasswordEvent.getOrganization().getOrgHandle(),
                    accessingOrganization.getOrganizationHandle());
            assertEquals(preUpdatePasswordEvent.getOrganization().getDepth(), accessingOrganization.getDepth());
            assertEquals(preUpdatePasswordEvent.getUser().getOrganization(), userResidentOrganization);

        }
    }

    @Test(dependsOnMethods = { "testRequestBuilder", "testRequestBuilderWithUnEncryptedCredential" },
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving claim metadata for claim URI: .*")
    public void testRequestBuilderWithErrorFromClaimMetaDataService() throws Exception {

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));
        when(claimMetadataManagementService.getLocalClaim(any(), any()))
                .thenThrow(new ClaimMetadataException("Error while retrieving local claim."));

        preUpdatePasswordActionRequestBuilderV2.buildActionExecutionRequest(flowContext,
                ActionExecutionRequestContext.create(preUpdatePasswordAction));
    }

    @Test(dependsOnMethods = { "testRequestBuilder", "testRequestBuilderWithUnEncryptedCredential",
            "testRequestBuilderWithErrorFromClaimMetaDataService"},
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "User realm is not available for tenant: .*")
    public void testRequestBuilderFailureWhenUserRealmLoadingFails() throws Exception {

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);

        when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(null);

        PreUpdatePasswordActionServiceComponentHolder.getInstance().setRealmService(realmService);

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        preUpdatePasswordActionRequestBuilderV2.buildActionExecutionRequest(flowContext,
                ActionExecutionRequestContext.create(preUpdatePasswordAction));
    }

    @Test(dependsOnMethods = { "testRequestBuilder", "testRequestBuilderWithUnEncryptedCredential",
            "testRequestBuilderWithErrorFromClaimMetaDataService" },
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp =
                    "User store manager is not an instance of UniqueIDUserStoreManager for tenant: " + TENANT_DOMAIN)
    public void testRequestBuilderFailureWhenUserStoreNotInstanceOfUniqueIDUserStoreManager()
            throws Exception {

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        UserRealm userRealm = mock(UserRealm.class);

        when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(mock(UserStoreManager.class));

        PreUpdatePasswordActionServiceComponentHolder.getInstance().setRealmService(realmService);

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        preUpdatePasswordActionRequestBuilderV2.buildActionExecutionRequest(flowContext,
                ActionExecutionRequestContext.create(preUpdatePasswordAction));
    }

    @Test(dependsOnMethods = { "testRequestBuilder", "testRequestBuilderWithUnEncryptedCredential",
            "testRequestBuilderWithErrorFromClaimMetaDataService" },
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while loading user store manager for tenant: .*")
    public void testRequestBuilderFailureWhenUserStoreManagerLoadingFails()
            throws Exception {

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        UserRealm userRealm = mock(UserRealm.class);

        when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenThrow(new UserStoreException("Error loading user store manager"));

        PreUpdatePasswordActionServiceComponentHolder.getInstance().setRealmService(realmService);

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        preUpdatePasswordActionRequestBuilderV2.buildActionExecutionRequest(flowContext,
                ActionExecutionRequestContext.create(preUpdatePasswordAction));
    }

    private void assertClaims(List<UserClaim> claims) {

        claims.forEach(claim -> {
            if (claim.getUri().equals(CLAIM1.getClaimURI())) {
                assertUpdatingClaimValue(claim, CLAIM1);
            } else if (claim.getUri().equals(CLAIM2.getClaimURI())) {
                assertUpdatingClaimValue(claim, CLAIM2);
            } else if (claim.getUri().equals(CLAIM3.getClaimURI())) {
                assertUpdatingClaimValue(claim, CLAIM3);
            } else if (claim.getUri().equals(CLAIM4.getClaimURI())) {
                assertUpdatingClaimValue(claim, CLAIM4);
            } else if (claim.getUri().equals(CLAIM5.getClaimURI())) {
                assertUpdatingClaimValue(claim, CLAIM5);
            } else if (claim.getUri().equals(CLAIM6.getClaimURI())) {
                assertUpdatingClaimValue(claim, CLAIM6);
            } else if (claim.getUri().equals(CLAIM7.getClaimURI())) {
                assertUpdatingClaimValue(claim, CLAIM7);
            } else {
                throw new IllegalStateException("Unexpected value in 'request': " + claim.getUri());
            }
        });
    }

    private void assertUpdatingClaimValue(UserClaim claim, TestUtil.Claims expectedClaim) {

        if (expectedClaim.isMultiValued()) {
            assertTrue(claim.getValue() instanceof String[]);
            assertEquals((String[]) claim.getValue(), expectedClaim.getValueInUserStore().split(Pattern.quote(",")));
        } else {
            assertTrue(claim.getValue() instanceof String);
            assertEquals((String) claim.getValue(), expectedClaim.getValueInUserStore());
        }
    }

    private static Flow buildMockedFlow(Flow.Name flowName, Flow.InitiatingPersona initiatingPersona) {

        if (Flow.isCredentialFlow(flowName)) {
            return new Flow.CredentialFlowBuilder()
                    .name(flowName)
                    .initiatingPersona(initiatingPersona)
                    .credentialType(Flow.CredentialType.PASSWORD)
                    .build();
        }
        return new Flow.Builder()
                .name(flowName)
                .initiatingPersona(initiatingPersona)
                .build();
    }

    private static UserStoreModel createUserStoreModel() {

        UserStoreModel userStoreModel = new UserStoreModel();
        userStoreModel.newUserBuilder()
                .withUserId(TEST_ID)
                .withClaim(CLAIM1.getClaimURI(), CLAIM1.getValueInUserStore())
                .withClaim(CLAIM2.getClaimURI(), CLAIM2.getValueInUserStore())
                .withClaim(CLAIM3.getClaimURI(), CLAIM3.getValueInUserStore())
                .withClaim(CLAIM4.getClaimURI(), CLAIM4.getValueInUserStore())
                .withClaim(CLAIM5.getClaimURI(), CLAIM5.getValueInUserStore())
                .withClaim(CLAIM6.getClaimURI(), CLAIM6.getValueInUserStore())
                .withClaim(CLAIM7.getClaimURI(), CLAIM7.getValueInUserStore())
                .withClaim(GROUPS.getClaimURI(), GROUPS.getValueInUserStore())
                .withClaim(ROLES.getClaimURI(), ROLES.getValueInUserStore())
                .build();
        return userStoreModel;
    }

    private static LocalClaim getMockedLocalClaim(TestUtil.Claims claim) {

        return claim.isMultiValued() ? mockLocalMultiValuedClaim(claim.getClaimURI()) :
                mockLocalSingleValuedClaim(claim.getClaimURI());
    }

    private static LocalClaim mockLocalMultiValuedClaim(String claimUri) {

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimURI()).thenReturn(claimUri);
        when(localClaim.getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY)).thenReturn("true");

        return localClaim;
    }

    private static LocalClaim mockLocalSingleValuedClaim(String claimUri) {

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimURI()).thenReturn(claimUri);
        when(localClaim.getClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY)).thenReturn("false");

        return localClaim;
    }
}
