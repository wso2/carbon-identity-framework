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

package org.wso2.carbon.identity.user.pre.update.profile.action.execution;

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
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.realm.UserStoreModel;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.action.api.model.UserActionRequestDTO;
import org.wso2.carbon.identity.user.pre.update.profile.action.api.model.PreUpdateProfileAction;
import org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.component.PreUpdateProfileActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.execution.PreUpdateProfileRequestBuilder;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.PreUpdateProfileEvent;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.PreUpdateProfileRequest;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.model.UpdatingUserClaim;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.CLAIM1;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.CLAIM2;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.CLAIM3;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.CLAIM4;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.CLAIM5;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.CLAIM6;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.CLAIM7;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.GROUPS;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.Claims.ROLES;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.TENANT_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.USER_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.constant.PreUpdateProfileTestConstants.USER_STORE_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ROOT_ORG_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ROOT_ORG_TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.ROOT_ORG_TENANT_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACCESSING_ORG_DEPTH;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACCESSING_ORG_HANDLE;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACCESSING_ORG_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_ACCESSING_ORG_NAME;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_RESIDENT_ORG_DEPTH;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_RESIDENT_ORG_HANDLE;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_RESIDENT_ORG_ID;
import static org.wso2.carbon.identity.user.pre.update.profile.action.util.TestConstants.TEST_RESIDENT_ORG_NAME;

@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {
        PreUpdateProfileActionServiceComponentHolder.class}, initUserStoreManager = true)
public class PreUpdateProfileRequestBuilderTest {

    private PreUpdateProfileRequestBuilder preUpdateProfileRequestBuilder;
    private UserStoreModel userStoreModel;
    private Organization userResidentOrganization;
    private org.wso2.carbon.identity.core.context.model.Organization accessingOrganization;
    private org.wso2.carbon.identity.core.context.model.RootOrganization rootOrganization;
    private ClaimMetadataManagementService claimMetadataManagementService;
    private MockedStatic<FrameworkUtils> frameworkUtils;

    @BeforeClass
    public void setUpClass() throws Exception {

        frameworkUtils = mockStatic(FrameworkUtils.class);
        frameworkUtils.when(FrameworkUtils::getMultiAttributeSeparator).thenReturn(",");

        claimMetadataManagementService = mock(ClaimMetadataManagementService.class);

        for (PreUpdateProfileTestConstants.Claims claim : PreUpdateProfileTestConstants.Claims.values()) {
            LocalClaim localClaim = getMockedLocalClaim(claim);
            when(claimMetadataManagementService.getLocalClaim(claim.getClaimURI(), TENANT_DOMAIN))
                    .thenReturn(Optional.of(localClaim));
        }

        PreUpdateProfileActionServiceComponentHolder.getInstance()
                .setClaimManagementService(claimMetadataManagementService);

        userResidentOrganization = new Organization.Builder()
                .id(TEST_RESIDENT_ORG_ID)
                .name(TEST_RESIDENT_ORG_NAME)
                .orgHandle(TEST_RESIDENT_ORG_HANDLE)
                .depth(TEST_RESIDENT_ORG_DEPTH)
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
    public void tearDownClass() {

        frameworkUtils.close();
    }

    @BeforeMethod
    public void setUp() {

        preUpdateProfileRequestBuilder = new PreUpdateProfileRequestBuilder();
        userStoreModel = createUserStoreModel();
        userStoreModel.bindToRealm();

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                PreUpdateProfileTestConstants.TENANT_DOMAIN);

        IdentityContext.getThreadLocalIdentityContext().setRootOrganization(rootOrganization);
    }

    @AfterMethod
    public void tearDown() {

        userStoreModel.unBindFromRealm();
        IdentityContext.destroyCurrentContext();
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testGetSupportedActionType() {

        assertEquals(preUpdateProfileRequestBuilder.getSupportedActionType(), ActionType.PRE_UPDATE_PROFILE);
    }

    @DataProvider(name = "flowData")
    public Object[][] flowData() {

        return new Object[][]{
                {false, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER),
                        PreUpdateProfileEvent.FlowInitiatorType.USER},
                {false, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.ADMIN),
                        PreUpdateProfileEvent.FlowInitiatorType.ADMIN},
                {false, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.APPLICATION),
                        PreUpdateProfileEvent.FlowInitiatorType.APPLICATION},
                {true, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER),
                        PreUpdateProfileEvent.FlowInitiatorType.USER},
                {true, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.ADMIN),
                        PreUpdateProfileEvent.FlowInitiatorType.ADMIN},
                {true, buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.APPLICATION),
                        PreUpdateProfileEvent.FlowInitiatorType.APPLICATION}
        };
    }

    @Test(dataProvider = "flowData")
    public void testBuildActionExecutionRequest(boolean isOrganizationFlow, Flow mockedFlow,
                                                PreUpdateProfileEvent.FlowInitiatorType expectedInitiatorType)
            throws ActionExecutionRequestBuilderException {

        if (isOrganizationFlow) {
            IdentityContext.getThreadLocalIdentityContext().setOrganization(accessingOrganization);
        }

        IdentityContext.getThreadLocalIdentityContext().enterFlow(mockedFlow);

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY,
                getMockUserActionContextWithUpdatingClaims());

        ActionExecutionRequestContext
                actionExecutionRequestContext =
                ActionExecutionRequestContext.create(getMockPreUpdateProfileActionWithClaimsConfiguredToShare());

        ActionExecutionRequest request =
                preUpdateProfileRequestBuilder.buildActionExecutionRequest(flowContext,
                        actionExecutionRequestContext);

        assertNotNull(request);
        assertEquals(request.getActionType(), ActionType.PRE_UPDATE_PROFILE);

        assertTrue(request.getEvent() instanceof PreUpdateProfileEvent);
        // Validate 'event'
        PreUpdateProfileEvent event = (PreUpdateProfileEvent) request.getEvent();
        assertEquals(event.getAction(), PreUpdateProfileEvent.Action.UPDATE);
        assertEquals(event.getInitiatorType(), expectedInitiatorType);
        assertNotNull(event.getUserStore());
        assertEquals(event.getUserStore().getId(),
                Base64.getEncoder().encodeToString(USER_STORE_DOMAIN.getBytes(StandardCharsets.UTF_8)));
        assertEquals(event.getUserStore().getName(), USER_STORE_DOMAIN);

        assertTrue(event.getRequest() instanceof PreUpdateProfileRequest);
        // Validate 'request' in 'event'
        PreUpdateProfileRequest profileUpdateRequest = (PreUpdateProfileRequest) event.getRequest();
        assertEquals(profileUpdateRequest.getAdditionalHeaders().size(), 0);
        assertEquals(profileUpdateRequest.getAdditionalParams().size(), 0);
        assertEquals(profileUpdateRequest.getClaims().size(), 5);
        assertUpdatingClaimsInRequest(profileUpdateRequest);
        // Validate 'user' in 'event'
        assertNotNull(event.getUser());
        assertEquals(event.getUser().getId(), USER_ID);
        assertEquals(event.getUser().getClaims().size(), 7);
        assertExistingClaimsInUser(event);
        assertEquals(event.getUser().getGroups().size(), 2);
        assertTrue(event.getUser().getGroups().containsAll(Arrays.asList((String[]) GROUPS.getExistingValue())));
        // Validate 'roles' in 'user' is empty as roles are not expected to be shared at this instance.
        assertEquals(event.getUser().getRoles().size(), 0);

        assertEquals(event.getTenant().getId(), String.valueOf(ROOT_ORG_TENANT_ID));
        assertEquals(event.getTenant().getName(), ROOT_ORG_TENANT_DOMAIN);

        if (isOrganizationFlow) {
            assertNotNull(event.getOrganization());
            assertEquals(event.getOrganization().getId(), accessingOrganization.getId());
            assertEquals(event.getOrganization().getName(), accessingOrganization.getName());
            assertEquals(event.getOrganization().getOrgHandle(), accessingOrganization.getOrganizationHandle());
            assertEquals(event.getOrganization().getDepth(), accessingOrganization.getDepth());
            assertEquals(event.getUser().getOrganization(), userResidentOrganization);
        } else {
            assertNull(event.getOrganization());
        }
    }

    @Test
    public void testBuildActionExecutionRequestWhenUpdatingClaimListAndClaimListToShareIsEmpty()
            throws ActionExecutionRequestBuilderException {

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY,
                getMockUserActionContextWithoutUpdatingClaims());

        ActionExecutionRequestContext
                actionExecutionRequestContext =
                ActionExecutionRequestContext.create(getMockPreUpdateProfileActionWithNoClaimsConfiguredToShare());

        ActionExecutionRequest request =
                preUpdateProfileRequestBuilder.buildActionExecutionRequest(flowContext,
                        actionExecutionRequestContext);

        assertNotNull(request);
        assertEquals(request.getActionType(), ActionType.PRE_UPDATE_PROFILE);

        assertTrue(request.getEvent() instanceof PreUpdateProfileEvent);
        // Validate 'event'
        PreUpdateProfileEvent event = (PreUpdateProfileEvent) request.getEvent();
        assertNotNull(event.getTenant());
        assertEquals(event.getTenant().getId(), String.valueOf(ROOT_ORG_TENANT_ID));
        assertEquals(event.getTenant().getName(), ROOT_ORG_TENANT_DOMAIN);
        assertNull(event.getOrganization());
        assertEquals(event.getAction(), PreUpdateProfileEvent.Action.UPDATE);
        assertEquals(event.getInitiatorType(), PreUpdateProfileEvent.FlowInitiatorType.USER);
        assertNotNull(event.getUserStore());
        assertEquals(event.getUserStore().getId(),
                Base64.getEncoder().encodeToString(USER_STORE_DOMAIN.getBytes(StandardCharsets.UTF_8)));
        assertEquals(event.getUserStore().getName(), USER_STORE_DOMAIN);

        // Validate 'request' in 'event' is empty as there are no claims to update.
        assertNull(event.getRequest());
        // Validate 'user' in 'event'
        assertNotNull(event.getUser());
        assertEquals(event.getUser().getId(), USER_ID);
        assertEquals(event.getUser().getClaims().size(), 0);
        assertEquals(event.getUser().getGroups().size(), 0);
        assertEquals(event.getUser().getRoles().size(), 0);
    }

    // Failure tests.
    // These tests are updates PreUpdateProfileActionServiceComponentHolder with corrupted mock services.
    // Thus, these tests are always expected to execute after success tests and they are dependent on each other.
    @Test(dependsOnMethods = {"testBuildActionExecutionRequest",
            "testBuildActionExecutionRequestWhenUpdatingClaimListAndClaimListToShareIsEmpty"},
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving claim metadata for claim URI: .*")
    public void testBuildActionExecutionRequestFailureWhenClaimMetadataServiceLoadingFails()
            throws Exception {

        ClaimMetadataManagementService failingClaimMetadataManagementService =
                mock(ClaimMetadataManagementService.class);
        when(failingClaimMetadataManagementService.getLocalClaim(any(), any())).thenThrow(
                new ClaimMetadataException("Error while loading claim metadata service"));

        PreUpdateProfileActionServiceComponentHolder.getInstance()
                .setClaimManagementService(failingClaimMetadataManagementService);

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY,
                getMockUserActionContextWithoutUpdatingClaims());

        ActionExecutionRequestContext
                actionExecutionRequestContext =
                ActionExecutionRequestContext.create(getMockPreUpdateProfileActionWithClaimsConfiguredToShare());

        preUpdateProfileRequestBuilder.buildActionExecutionRequest(flowContext,
                actionExecutionRequestContext);
    }

    @Test(dependsOnMethods = {"testBuildActionExecutionRequestFailureWhenClaimMetadataServiceLoadingFails"},
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "User realm is not available for tenant: carbon.super")
    public void testBuildActionExecutionRequestFailureWhenUserRealmLoadingFails() throws Exception {

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);

        when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(null);

        PreUpdateProfileActionServiceComponentHolder.getInstance().setRealmService(realmService);

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY,
                getMockUserActionContextWithoutUpdatingClaims());

        ActionExecutionRequestContext
                actionExecutionRequestContext =
                ActionExecutionRequestContext.create(getMockPreUpdateProfileActionWithNoClaimsConfiguredToShare());

        preUpdateProfileRequestBuilder.buildActionExecutionRequest(flowContext,
                actionExecutionRequestContext);
    }

    @Test(dependsOnMethods = {"testBuildActionExecutionRequestFailureWhenUserRealmLoadingFails"},
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "User store manager is not an instance of UniqueIDUserStoreManager" +
                    " for tenant: carbon.super")
    public void testBuildActionExecutionRequestFailureWhenUserStoreNotInstanceOfUniqueIDUserStoreManager()
            throws Exception {

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        UserRealm userRealm = mock(UserRealm.class);

        when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(mock(UserStoreManager.class));

        PreUpdateProfileActionServiceComponentHolder.getInstance().setRealmService(realmService);

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY,
                getMockUserActionContextWithoutUpdatingClaims());

        ActionExecutionRequestContext
                actionExecutionRequestContext =
                ActionExecutionRequestContext.create(getMockPreUpdateProfileActionWithNoClaimsConfiguredToShare());

        preUpdateProfileRequestBuilder.buildActionExecutionRequest(flowContext,
                actionExecutionRequestContext);
    }

    @Test(dependsOnMethods = {
            "testBuildActionExecutionRequestFailureWhenUserStoreNotInstanceOfUniqueIDUserStoreManager"},
            expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while loading user store manager for tenant: carbon.super")
    public void testBuildActionExecutionRequestFailureWhenUserStoreManagerLoadingFails()
            throws Exception {

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        UserRealm userRealm = mock(UserRealm.class);

        when(tenantManager.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(realmService.getTenantUserRealm(TENANT_ID)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenThrow(
                new UserStoreException("Error while loading user store manager"));

        PreUpdateProfileActionServiceComponentHolder.getInstance().setRealmService(realmService);

        IdentityContext.getThreadLocalIdentityContext()
                .enterFlow(buildMockedFlow(Flow.Name.PROFILE_UPDATE, Flow.InitiatingPersona.USER));

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY,
                getMockUserActionContextWithoutUpdatingClaims());

        ActionExecutionRequestContext
                actionExecutionRequestContext =
                ActionExecutionRequestContext.create(getMockPreUpdateProfileActionWithNoClaimsConfiguredToShare());

        preUpdateProfileRequestBuilder.buildActionExecutionRequest(flowContext,
                actionExecutionRequestContext);
    }

    private void assertUpdatingClaimsInRequest(PreUpdateProfileRequest profileUpdateRequest) {

        profileUpdateRequest.getClaims().forEach(claim -> {
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
            } else {
                throw new IllegalStateException("Unexpected value in 'request': " + claim.getUri());
            }
        });
    }

    private void assertExistingClaimsInUser(PreUpdateProfileEvent event) {

        event.getUser().getClaims().forEach(claim -> {
            if (claim.getUri().equals(CLAIM1.getClaimURI())) {
                assertExistingClaimValue(claim, CLAIM1);
                assertUpdatingClaimValue((UpdatingUserClaim) claim, CLAIM1);
            } else if (claim.getUri().equals(CLAIM2.getClaimURI())) {
                assertExistingClaimValue(claim, CLAIM2);
                assertUpdatingClaimValue((UpdatingUserClaim) claim, CLAIM2);
            } else if (claim.getUri().equals(CLAIM3.getClaimURI())) {
                assertExistingClaimValue(claim, CLAIM3);
                assertUpdatingClaimValue((UpdatingUserClaim) claim, CLAIM3);
            } else if (claim.getUri().equals(CLAIM4.getClaimURI())) {
                assertExistingClaimValue(claim, CLAIM4);
                assertUpdatingClaimValue((UpdatingUserClaim) claim, CLAIM4);
            } else if (claim.getUri().equals(CLAIM5.getClaimURI())) {
                assertExistingClaimValue(claim, CLAIM5);
                assertUpdatingClaimValue((UpdatingUserClaim) claim, CLAIM5);
            } else if (claim.getUri().equals(CLAIM6.getClaimURI())) {
                assertExistingClaimValue(claim, CLAIM6);
            } else if (claim.getUri().equals(CLAIM7.getClaimURI())) {
                assertExistingClaimValue(claim, CLAIM7);
            } else {
                throw new IllegalStateException("Unexpected value in 'user': " + claim.getUri());
            }
        });
    }

    private void assertUpdatingClaimValue(UserClaim claim,
                                          PreUpdateProfileTestConstants.Claims expectedClaim) {

        if (expectedClaim.isMultiValued()) {
            assertTrue(claim.getValue() instanceof String[] &&
                    Arrays.equals((String[]) claim.getValue(), (String[]) expectedClaim.getUpdatingValue()));
        } else {
            assertTrue(claim.getValue() instanceof String && claim.getValue().equals(expectedClaim.getUpdatingValue()));
        }
    }

    private void assertExistingClaimValue(UserClaim claim, PreUpdateProfileTestConstants.Claims expectedClaim) {

        if (expectedClaim.isMultiValued()) {
            assertTrue(claim.getValue() instanceof String[] &&
                    Arrays.equals((String[]) claim.getValue(), (String[]) expectedClaim.getExistingValue()));
        } else {
            assertTrue(claim.getValue() instanceof String && claim.getValue().equals(expectedClaim.getExistingValue()));
        }
    }

    private void assertUpdatingClaimValue(UpdatingUserClaim claim, PreUpdateProfileTestConstants.Claims expectedClaim) {

        if (expectedClaim.isMultiValued()) {
            assertTrue(
                    Arrays.equals((String[]) (claim.getUpdatingValue()), (String[]) expectedClaim.getUpdatingValue()));
        } else {
            assertTrue(claim.getUpdatingValue().equals(expectedClaim.getUpdatingValue()));
        }
    }

    private UserActionContext getMockUserActionContextWithUpdatingClaims() {

        return new UserActionContext(new UserActionRequestDTO.Builder()
                .userId(USER_ID)
                .userStoreDomain(USER_STORE_DOMAIN)
                .addClaim(CLAIM1.getClaimURI(), (String) CLAIM1.getUpdatingValue())
                .addClaim(CLAIM2.getClaimURI(), (String[]) CLAIM2.getUpdatingValue())
                .addClaim(CLAIM3.getClaimURI(), (String) CLAIM3.getUpdatingValue())
                .addClaim(CLAIM4.getClaimURI(), (String[]) CLAIM4.getUpdatingValue())
                .addClaim(CLAIM5.getClaimURI(), (String) CLAIM5.getUpdatingValue())
                .addClaim(GROUPS.getClaimURI(), (String[]) GROUPS.getUpdatingValue())
                .addClaim(ROLES.getClaimURI(), (String[]) ROLES.getUpdatingValue())
                .residentOrganization(userResidentOrganization)
                .build());
    }

    private static UserActionContext getMockUserActionContextWithoutUpdatingClaims() {

        return new UserActionContext(new UserActionRequestDTO.Builder()
                .userId(USER_ID)
                .userStoreDomain(USER_STORE_DOMAIN)
                .build());
    }

    private static PreUpdateProfileAction getMockPreUpdateProfileActionWithClaimsConfiguredToShare() {

        return new PreUpdateProfileAction.ResponseBuilder()
                .attributes(Arrays.asList(CLAIM1.getClaimURI(), CLAIM2.getClaimURI(), CLAIM3.getClaimURI(),
                        CLAIM4.getClaimURI(), CLAIM5.getClaimURI(), CLAIM6.getClaimURI(), CLAIM7.getClaimURI(),
                        GROUPS.getClaimURI(), ROLES.getClaimURI()))
                .build();
    }

    private static PreUpdateProfileAction getMockPreUpdateProfileActionWithNoClaimsConfiguredToShare() {

        return new PreUpdateProfileAction.ResponseBuilder().build();
    }

    private static Flow buildMockedFlow(Flow.Name flowName, Flow.InitiatingPersona initiatingPersona) {

        return new Flow.Builder()
                .name(flowName)
                .initiatingPersona(initiatingPersona)
                .build();
    }

    private static LocalClaim getMockedLocalClaim(PreUpdateProfileTestConstants.Claims claim) {

        return claim.isMultiValued() ? mockLocalMultiValuedClaim(claim.getClaimURI()) :
                mockLocalSingleValuedClaim(claim.getClaimURI());
    }

    private static UserStoreModel createUserStoreModel() {

        UserStoreModel userStoreModel = new UserStoreModel();
        userStoreModel.newUserBuilder()
                .withUserId(USER_ID)
                .withClaim(CLAIM1.getClaimURI(), CLAIM1.getExistingValueInUserStore())
                .withClaim(CLAIM2.getClaimURI(), CLAIM2.getExistingValueInUserStore())
                .withClaim(CLAIM3.getClaimURI(), CLAIM3.getExistingValueInUserStore())
                .withClaim(CLAIM4.getClaimURI(), CLAIM4.getExistingValueInUserStore())
                .withClaim(CLAIM5.getClaimURI(), CLAIM5.getExistingValueInUserStore())
                .withClaim(CLAIM6.getClaimURI(), CLAIM6.getExistingValueInUserStore())
                .withClaim(CLAIM7.getClaimURI(), CLAIM7.getExistingValueInUserStore())
                .withClaim(GROUPS.getClaimURI(), GROUPS.getExistingValueInUserStore())
                .withClaim(ROLES.getClaimURI(), ROLES.getExistingValueInUserStore())
                .build();
        return userStoreModel;
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
