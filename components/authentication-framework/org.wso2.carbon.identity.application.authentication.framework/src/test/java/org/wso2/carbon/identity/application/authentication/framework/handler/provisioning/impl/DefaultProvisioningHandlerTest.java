/*
 * Copyright (c) 2017-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.wso2.carbon.identity.organization.management.service.constant.OrganizationManagementConstants.SUPER_ORG_ID;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.ROLE_WORKFLOW_CREATED;
import static org.wso2.carbon.identity.workflow.mgt.util.WorkflowErrorConstants.ErrorMessages.ERROR_CODE_ROLE_WF_USER_PENDING_APPROVAL_FOR_ROLE;

@Listeners(MockitoTestNGListener.class)
public class DefaultProvisioningHandlerTest {

    @Mock
    private RealmService mockRealmService;
    @Mock
    private TenantManager mockTenantManager;
    @Mock
    private UserRealm mockUserRealm;
    @Mock
    private UserStoreManager mockUserStoreManager;
    @Mock
    private FederatedAssociationManager mockFederatedAssociationManager;
    @Mock
    private OrganizationManager mockOrganizationManager;
    @Mock
    private RealmConfiguration mockRealmConfiguration;
    @Mock
    private RoleManagementService mockRoleManagementService;

    private DefaultProvisioningHandler provisioningHandler;

    @BeforeMethod
    public void setUp() throws Exception {

        provisioningHandler = new DefaultProvisioningHandler();
        CommonTestUtils.initPrivilegedCarbonContext();
        FrameworkServiceDataHolder.getInstance().setRealmService(mockRealmService);
        FrameworkServiceDataHolder.getInstance().setOrganizationManager(mockOrganizationManager);
        FrameworkServiceDataHolder.getInstance().setRoleManagementServiceV2(mockRoleManagementService);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testGetInstance() throws Exception {
        CommonTestUtils.testSingleton(
                DefaultProvisioningHandler.getInstance(),
                DefaultProvisioningHandler.getInstance()
        );
    }

    @Test
    public void testHandle() throws Exception {
    }

    @DataProvider(name = "associateUserEmptyInputProvider")
    public Object[][] getAssociatedUserEmptyInputs() {
        return new Object[][]{
                {"", null},
                {"", ""},
                {null, ""},
                {null, null},
        };
    }

    @Test(dataProvider = "associateUserEmptyInputProvider", expectedExceptions = FrameworkException.class)
    public void testAssociateUserEmptyInputs(String subject,
                                             String idp) throws Exception {

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            frameworkUtils.when(() -> FrameworkUtils.startTenantFlow("tenantDomain"))
                    .thenAnswer(invocation -> null);
            provisioningHandler.associateUser("dummy_user_name", "DUMMY_DOMAIN",
                    "dummy.com", subject, idp);
        }
    }

    @Test
    public void testGeneratePassword() throws Exception {
        char[] randomPassword = provisioningHandler.generatePassword();
        assertNotNull(randomPassword);
        assertEquals(randomPassword.length, 12);
    }

    @Test
    public void testResolvePassword() throws Exception {

        Map<String, String> userClaims = new HashMap<>();
        userClaims.put(FrameworkConstants.PASSWORD, "dummy_password");
        char[] resolvedPassword = provisioningHandler.resolvePassword(userClaims);
        assertEquals(resolvedPassword, "dummy_password".toCharArray());
    }

    @Test
    public void testHandleWithV2RolesOverrideAttributes() throws Exception {

        List<String> roleIdList = new ArrayList<>();
        roleIdList.add("role1");
        roleIdList.add("role2");
        String subject = "testUser";
        String userId = "12231321";
        Map<String, String> attributes = new HashMap<>();
        attributes.put(FrameworkConstants.IDP_ID, "testIdp");
        attributes.put(FrameworkConstants.ASSOCIATED_ID, "testUser");
        attributes.put(FrameworkConstants.USER_ID_CLAIM, "12231321");

        String provisioningUserStoreId = IdentityApplicationConstants.AS_IN_USERNAME_USERSTORE_FOR_JIT;
        String tenantDomain = "carbon.super";

        when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);
        when(mockTenantManager.getTenantId(tenantDomain)).thenReturn(-1234);
        when(mockRealmService.getTenantUserRealm(-1234)).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
        when(mockUserStoreManager.getSecondaryUserStoreManager("PRIMARY")).thenReturn(mockUserStoreManager);
        when(mockUserStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockUserRealm.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockRealmConfiguration.isPrimary()).thenReturn(true);
        when(mockRealmConfiguration.getAdminUserName()).thenReturn("admin");
        when(mockRealmConfiguration.getEveryOneRoleName()).thenReturn("everyone");

        when(mockUserStoreManager.isExistingUser(subject)).thenReturn(true);
        when(mockRealmConfiguration.isPrimary()).thenReturn(true);

        when(mockUserStoreManager.isExistingUser(subject)).thenReturn(true);

        Map<String, Object> threadLocalProperties = new HashMap<>();
        threadLocalProperties.put(FrameworkConstants.ATTRIBUTE_SYNC_METHOD, FrameworkConstants.OVERRIDE_ALL);
        IdentityUtil.threadLocalProperties.set(threadLocalProperties);

        when(mockFederatedAssociationManager.getUserForFederatedAssociation(tenantDomain, "testIdp", subject))
                .thenReturn(subject);
        when(mockOrganizationManager.resolveOrganizationId(tenantDomain)).thenReturn(SUPER_ORG_ID);
        when(mockRoleManagementService.getRoleIdListOfUser(userId, tenantDomain))
                .thenReturn(roleIdList);

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {

            frameworkUtils.when(() -> FrameworkUtils.resolveUserIdFromUsername(mockUserStoreManager, subject))
                    .thenReturn(userId);
            frameworkUtils.when(FrameworkUtils::getFederatedAssociationManager)
                    .thenReturn(mockFederatedAssociationManager);
            provisioningHandler.handleWithV2Roles(roleIdList, subject, attributes, provisioningUserStoreId,
                    tenantDomain);
        }

    }

    @Test
    public void testAssignUserToRoleV2_WorkflowEngaged() throws Exception {

        String subject = "testUser";
        String userId = "user-id-123";
        String tenantDomain = "carbon.super";
        String roleIdToAdd = "roleId1";
        String idp = "testIdp";
        String associatedId = "assocId1";

        List<String> rolesToAdd = new ArrayList<>(Collections.singletonList(roleIdToAdd));

        // 1. Prepare Attributes (Crucial for the association check)
        Map<String, String> attributes = new HashMap<>();
        attributes.put(FrameworkConstants.IDP_ID, idp);
        attributes.put(FrameworkConstants.ASSOCIATED_ID, associatedId);

        setupHappyPathMocks(subject, userId, tenantDomain);

        // 2. Mock: The user currently has NO roles
        when(mockRoleManagementService.getRoleIdListOfUser(userId, tenantDomain))
                .thenReturn(new ArrayList<>());

        // 3. Mock: The role exists
        when(mockRoleManagementService.isExistingRole(eq(roleIdToAdd), eq(tenantDomain)))
                .thenReturn(true);

        // 4. Mock: Simulate Workflow Engagement Exception
        org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException workflowException =
                new org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException(
                        ROLE_WORKFLOW_CREATED.getCode(), "Workflow created");

        doThrow(workflowException).when(mockRoleManagementService).updateUserListOfRole(
                eq(roleIdToAdd),
                eq(Collections.singletonList(userId)),
                anyList(),
                eq(tenantDomain));

        // 5. Mock: Federated Association (This fixes the "User already exists" error)
        // Since isExistingUser returns true, we must say this user is already associated.
        when(mockFederatedAssociationManager.getUserForFederatedAssociation(tenantDomain, idp, associatedId))
                .thenReturn(subject);

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            setupFrameworkUtilsMocks(frameworkUtils, subject, userId);

            // Execute
            provisioningHandler.handleWithV2Roles(rolesToAdd, subject, attributes, "PRIMARY", tenantDomain);

            // Assertion: Verify role update was attempted despite workflow exception
            verify(mockRoleManagementService).updateUserListOfRole(
                    eq(roleIdToAdd), eq(Collections.singletonList(userId)), anyList(), eq(tenantDomain));
        }
    }

    @Test
    public void testRemoveUserFromRoleV2_WorkflowPendingApproval() throws Exception {

        String subject = "testUser";
        String userId = "user-id-123";
        String tenantDomain = "carbon.super";
        String roleIdToRemove = "roleIdOld";
        String idp = "testIdp";
        String associatedId = "assocId1";

        // 1. Prepare Attributes (Crucial for the association check in handleUserProvisioning)
        Map<String, String> attributes = new HashMap<>();
        attributes.put(FrameworkConstants.IDP_ID, idp);
        attributes.put(FrameworkConstants.ASSOCIATED_ID, associatedId);

        // The input roles list is empty, implying we want to remove existing roles
        List<String> rolesToAdd = new ArrayList<>();

        setupHappyPathMocks(subject, userId, tenantDomain);

        // 2. Mock: User currently HAS this role
        when(mockRoleManagementService.getRoleIdListOfUser(userId, tenantDomain))
                .thenReturn(Collections.singletonList(roleIdToRemove));

        // 3. Mock: The role exists
        when(mockRoleManagementService.isExistingRole(eq(roleIdToRemove), eq(tenantDomain)))
                .thenReturn(true);

        // 4. Mock: Simulate "Pending Approval" exception using the V2 EXCEPTION
        org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException pendingException =
                new org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException(
                        ERROR_CODE_ROLE_WF_USER_PENDING_APPROVAL_FOR_ROLE.getCode(), "Pending");

        doThrow(pendingException).when(mockRoleManagementService).updateUserListOfRole(
                eq(roleIdToRemove),
                anyList(), // adding users (empty)
                eq(Collections.singletonList(userId)), // removing user
                eq(tenantDomain));

        // 5. Mock: Federated Association (Fixes the "User already exists" error)
        when(mockFederatedAssociationManager.getUserForFederatedAssociation(tenantDomain, idp, associatedId))
                .thenReturn(subject);

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            setupFrameworkUtilsMocks(frameworkUtils, subject, userId);

            // Execute
            provisioningHandler.handleWithV2Roles(rolesToAdd, subject, attributes, "PRIMARY", tenantDomain);

            // Assertion: Verify remove role was attempted despite the exception
            verify(mockRoleManagementService).updateUserListOfRole(
                    eq(roleIdToRemove), anyList(), eq(Collections.singletonList(userId)), eq(tenantDomain));
        }
    }

    @Test(expectedExceptions = FrameworkException.class)
    public void testAssignUserToRoleV2_GenericException() throws Exception {

        String subject = "testUser";
        String userId = "user-id-123";
        String tenantDomain = "carbon.super";
        List<String> rolesToAdd = Collections.singletonList("roleId1");

        setupHappyPathMocks(subject, userId, tenantDomain);
        when(mockRoleManagementService.getRoleIdListOfUser(userId, tenantDomain)).thenReturn(new ArrayList<>());
        when(mockRoleManagementService.isExistingRole(anyString(), anyString())).thenReturn(true);

        // CASE: Generic Role Exception (Not a workflow one)
        // This targets: handleWorkflowEngagement (re-throw logic)
        IdentityRoleManagementException genericException =
                new IdentityRoleManagementException("UNEXPECTED_ERROR", "Something went wrong");

        doThrow(genericException).when(mockRoleManagementService).updateUserListOfRole(
                anyString(), anyList(), anyList(), anyString());

        try (MockedStatic<FrameworkUtils> frameworkUtils = mockStatic(FrameworkUtils.class)) {
            setupFrameworkUtilsMocks(frameworkUtils, subject, userId);

            // Execute - Expecting FrameworkException
            provisioningHandler.handleWithV2Roles(rolesToAdd, subject, new HashMap<>(), "PRIMARY", tenantDomain);
        }
    }

    // --- Helper methods to reduce boilerplate in the specific tests above ---

    private void setupHappyPathMocks(String subject, String userId, String tenantDomain) throws Exception {
        when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);
        when(mockTenantManager.getTenantId(tenantDomain)).thenReturn(-1234);
        when(mockRealmService.getTenantUserRealm(-1234)).thenReturn(mockUserRealm);

        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
        when(mockUserStoreManager.getSecondaryUserStoreManager(anyString())).thenReturn(mockUserStoreManager);

        // Realm Config mocks
        when(mockUserStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockUserRealm.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockRealmConfiguration.isPrimary()).thenReturn(true);
        when(mockRealmConfiguration.getAdminUserName()).thenReturn("admin");

        // Everyone role logic mocks
        when(mockRealmConfiguration.getEveryOneRoleName()).thenReturn("everyone");
        when(mockRoleManagementService.isExistingRoleName(eq("everyone"), anyString(), anyString(), anyString()))
                .thenReturn(false); // Simplify by pretending everyone role isn't mapped V2 for this test

        when(mockUserStoreManager.isExistingUser(subject)).thenReturn(true);
        when(mockOrganizationManager.resolveOrganizationId(tenantDomain)).thenReturn(SUPER_ORG_ID);

        // ThreadLocal Setup
        Map<String, Object> threadLocalProperties = new HashMap<>();
        threadLocalProperties.put(FrameworkConstants.ATTRIBUTE_SYNC_METHOD, FrameworkConstants.OVERRIDE_ALL);
        IdentityUtil.threadLocalProperties.set(threadLocalProperties);
    }

    private void setupFrameworkUtilsMocks(MockedStatic<FrameworkUtils> frameworkUtils, String subject, String userId) {
        frameworkUtils.when(() -> FrameworkUtils.resolveUserIdFromUsername(mockUserStoreManager, subject))
                .thenReturn(userId);
        frameworkUtils.when(FrameworkUtils::getFederatedAssociationManager)
                .thenReturn(mockFederatedAssociationManager);
    }
}
