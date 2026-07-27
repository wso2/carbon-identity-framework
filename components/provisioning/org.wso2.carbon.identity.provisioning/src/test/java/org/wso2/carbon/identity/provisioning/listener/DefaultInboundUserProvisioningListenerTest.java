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

package org.wso2.carbon.identity.provisioning.listener;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ThreadLocalProvisioningServiceProvider;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningConstants;
import org.wso2.carbon.identity.provisioning.IdentityProvisioningException;
import org.wso2.carbon.identity.provisioning.OutboundProvisioningManager;
import org.wso2.carbon.identity.provisioning.ProvisioningEntity;
import org.wso2.carbon.identity.provisioning.ProvisioningEntityType;
import org.wso2.carbon.identity.provisioning.ProvisioningOperation;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for DefaultInboundUserProvisioningListener.
 */
public class DefaultInboundUserProvisioningListenerTest {

    @Mock
    private UserStoreManager userStoreManager;

    @Mock
    private RealmConfiguration realmConfiguration;

    @Mock
    private OutboundProvisioningManager outboundProvisioningManager;

    private DefaultInboundUserProvisioningListener listener;

    private MockedStatic<OutboundProvisioningManager> outboundProvisioningManagerMockedStatic;
    private MockedStatic<UserCoreUtil> userCoreUtilMockedStatic;
    private MockedStatic<CarbonContext> carbonContextMockedStatic;
    private MockedStatic<IdentityApplicationManagementUtil> identityAppManagementUtilMockedStatic;

    private static final String OLD_ROLE_NAME = "TestRole";
    private static final String NEW_ROLE_NAME = "UpdatedTestRole";
    private static final String DOMAIN_NAME = "PRIMARY";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String DOMAIN_AWARE_NAME = DOMAIN_NAME + "/" + OLD_ROLE_NAME;
    private static final String USER_NAME = "testUser";
    private static final String USER_DOMAIN_AWARE_NAME = DOMAIN_NAME + "/" + USER_NAME;
    private static final String PROCESS_ADD_SHARED_USER = "processAddSharedUser";
    private static final String SP_NAME = "testServiceProvider";
    private static final String SP_CLAIM_DIALECT = "http://wso2.org/oidc/claim";

    @BeforeClass
    public void setUpClass() {
        // Set up system properties to avoid initialization issues.
        String carbonHome = System.getProperty("carbon.home");
        if (carbonHome == null) {
            System.setProperty("carbon.home", ".");
        }
    }

    @BeforeMethod
    public void setUp() throws IdentityProvisioningException {
        MockitoAnnotations.openMocks(this);
        listener = new DefaultInboundUserProvisioningListener();

        // Setup static mocks.
        outboundProvisioningManagerMockedStatic = Mockito.mockStatic(OutboundProvisioningManager.class);
        userCoreUtilMockedStatic = Mockito.mockStatic(UserCoreUtil.class);
        carbonContextMockedStatic = Mockito.mockStatic(CarbonContext.class);
        identityAppManagementUtilMockedStatic = Mockito.mockStatic(IdentityApplicationManagementUtil.class);

        // Mock RealmConfiguration and domain operations.
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);

        // Create mock instances.
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);

        // Setup static mock behavior.
        userCoreUtilMockedStatic.when(() -> UserCoreUtil.getDomainName(realmConfiguration)).thenReturn(DOMAIN_NAME);
        userCoreUtilMockedStatic.when(() -> UserCoreUtil.addDomainToName(OLD_ROLE_NAME, DOMAIN_NAME))
                .thenReturn(DOMAIN_AWARE_NAME);
        userCoreUtilMockedStatic.when(() -> UserCoreUtil.addDomainToName(USER_NAME, DOMAIN_NAME))
                .thenReturn(USER_DOMAIN_AWARE_NAME);

        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
        when(carbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

        identityAppManagementUtilMockedStatic.when(
                IdentityApplicationManagementUtil::getThreadLocalProvisioningServiceProvider)
                .thenReturn(null);

        outboundProvisioningManagerMockedStatic.when(OutboundProvisioningManager::getInstance)
                .thenReturn(outboundProvisioningManager);

        // Mock the provision method to do nothing.
        doNothing().when(outboundProvisioningManager).provision(
                any(ProvisioningEntity.class),
                anyString(),
                anyString(),
                anyString(),
                anyBoolean()
        );
    }

    @AfterMethod
    public void tearDown() {
        // Clear any thread local state set during the test to avoid leaking across tests.
        IdentityUtil.threadLocalProperties.get().remove(PROCESS_ADD_SHARED_USER);

        // Close all static mocks.
        if (outboundProvisioningManagerMockedStatic != null) {
            outboundProvisioningManagerMockedStatic.close();
        }
        if (userCoreUtilMockedStatic != null) {
            userCoreUtilMockedStatic.close();
        }
        if (carbonContextMockedStatic != null) {
            carbonContextMockedStatic.close();
        }
        if (identityAppManagementUtilMockedStatic != null) {
            identityAppManagementUtilMockedStatic.close();
        }
    }

    @Test
    public void testDoPostUpdateRoleNameVerifiesAllRequiredAttributes() throws UserStoreException {

        // Execute the actual method.
        listener.doPostUpdateRoleName(OLD_ROLE_NAME, NEW_ROLE_NAME, userStoreManager);

        // Capture the ProvisioningEntity passed to the provision method.
        ArgumentCaptor<ProvisioningEntity> entityCaptor = ArgumentCaptor.forClass(ProvisioningEntity.class);
        verify(outboundProvisioningManager).provision(
                entityCaptor.capture(),
                eq(IdentityProvisioningConstants.LOCAL_SP),
                eq(IdentityProvisioningConstants.WSO2_CARBON_DIALECT),
                eq(TENANT_DOMAIN),
                eq(false)
        );

        // Get the captured ProvisioningEntity.
        ProvisioningEntity capturedEntity = entityCaptor.getValue();

        // Verify entity type is GROUP.
        assertNotNull(capturedEntity, "ProvisioningEntity should not be null");
        assertEquals(capturedEntity.getEntityType(), ProvisioningEntityType.GROUP,
                "Entity type should be GROUP");

        // Verify entity name is domain-aware (based on old role name).
        assertEquals(capturedEntity.getEntityName(), DOMAIN_AWARE_NAME,
                "Entity name should be domain-aware name with old role name");

        // Verify operation is PATCH.
        assertEquals(capturedEntity.getOperation(), ProvisioningOperation.PATCH,
                "Operation should be PATCH");

        // Verify all required attributes are set.
        Map<ClaimMapping, List<String>> attributes = capturedEntity.getAttributes();
        assertNotNull(attributes, "Attributes should not be null");
        assertEquals(attributes.size(), 3, "Should have exactly 3 attributes");

        // Verify that the attributes contain the correct values.
        // We need to check the local claim URIs since that's what ClaimMapping.build() sets.
        boolean hasOldGroupName = false;
        boolean hasNewGroupName = false;
        boolean hasGroupClaim = false;

        for (Map.Entry<ClaimMapping, List<String>> entry : attributes.entrySet()) {
            ClaimMapping mapping = entry.getKey();
            List<String> values = entry.getValue();

            // Check by local claim URI.
            String localClaimUri = mapping.getLocalClaim() != null ?
                    mapping.getLocalClaim().getClaimUri() : null;

            if (IdentityProvisioningConstants.OLD_GROUP_NAME_CLAIM_URI.equals(localClaimUri)) {
                hasOldGroupName = true;
                assertNotNull(values, "Old group name values should not be null");
                assertEquals(values.size(), 1, "Should have one old group name value");
                assertEquals(values.get(0), OLD_ROLE_NAME,
                        "Old group name value should match the old role name");
            } else if (IdentityProvisioningConstants.NEW_GROUP_NAME_CLAIM_URI.equals(localClaimUri)) {
                hasNewGroupName = true;
                assertNotNull(values, "New group name values should not be null");
                assertEquals(values.size(), 1, "Should have one new group name value");
                assertEquals(values.get(0), NEW_ROLE_NAME,
                        "New group name value should match the new role name");
            } else if (IdentityProvisioningConstants.GROUP_CLAIM_URI.equals(localClaimUri)) {
                hasGroupClaim = true;
                assertNotNull(values, "Group values should not be null");
                assertEquals(values.size(), 1, "Should have one group value");
                assertEquals(values.get(0), NEW_ROLE_NAME,
                        "Group value should match the new role name");
            }
        }

        // Verify all three claim types are present.
        assertTrue(hasOldGroupName, "OLD_GROUP_NAME_CLAIM_URI should be present");
        assertTrue(hasNewGroupName, "NEW_GROUP_NAME_CLAIM_URI should be present");
        assertTrue(hasGroupClaim, "GROUP_CLAIM_URI should be present");
    }

    @DataProvider(name = "userCreationFlows")
    public Object[][] userCreationFlows() {
        return new Object[][]{
                {new Flow.Builder().name(Flow.Name.REGISTER).initiatingPersona(Flow.InitiatingPersona.ADMIN).build()},
                {new Flow.Builder().name(Flow.Name.JUST_IN_TIME_PROVISION)
                        .initiatingPersona(Flow.InitiatingPersona.USER).build()},
                {new Flow.Builder().name(Flow.Name.INVITE).initiatingPersona(Flow.InitiatingPersona.ADMIN).build()},
                {new Flow.Builder().name(Flow.Name.INVITED_USER_REGISTRATION)
                        .initiatingPersona(Flow.InitiatingPersona.ADMIN).build()},
        };
    }

    @Test(dataProvider = "userCreationFlows")
    public void testDoPreSetUserClaimValues_DuringUserCreationWithOnlyLastPasswordUpdateTime_SkipsProvisioning(
            Flow flow) throws UserStoreException {

        try (MockedStatic<IdentityContext> identityContextMockedStatic =
                     Mockito.mockStatic(IdentityContext.class)) {
            IdentityContext mockIdentityContext = Mockito.mock(IdentityContext.class);
            identityContextMockedStatic.when(IdentityContext::getThreadLocalIdentityContext)
                    .thenReturn(mockIdentityContext);
            when(mockIdentityContext.getCurrentFlow()).thenReturn(flow);

            Map<String, String> inboundAttributes = new HashMap<>();
            inboundAttributes.put("http://wso2.org/claims/identity/lastPasswordUpdateTime",
                    String.valueOf(System.currentTimeMillis()));

            boolean result = listener.doPreSetUserClaimValues(
                    "testUser", inboundAttributes, "default", userStoreManager);

            assertTrue(result, "Should return true without triggering provisioning");
            verify(outboundProvisioningManager, Mockito.never()).provision(
                    any(ProvisioningEntity.class), anyString(), anyString(), anyString(), anyBoolean());
        }
    }

    @Test
    public void testDoPreAddUserWhenSharedUserCreationUsesResidentOutboundProvisioningConfigs()
            throws UserStoreException {

        // A service provider is present in the thread local (e.g. the application that initiated the request),
        // but the request is a shared user creation in a sub organization. In that case the sub organization's
        // resident outbound provisioning configs (LOCAL_SP) must be used instead of the service provider's.
        IdentityUtil.threadLocalProperties.get().put(PROCESS_ADD_SHARED_USER, Boolean.TRUE);
        ThreadLocalProvisioningServiceProvider serviceProvider = mockThreadLocalServiceProvider();
        identityAppManagementUtilMockedStatic.when(
                        IdentityApplicationManagementUtil::getThreadLocalProvisioningServiceProvider)
                .thenReturn(serviceProvider);

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMockedStatic =
                     mockPrivilegedCarbonContext()) {

            boolean result = listener.doPreAddUser(USER_NAME, new StringBuffer("password"), null,
                    new HashMap<>(), "default", userStoreManager);

            assertTrue(result, "Should return true after provisioning");

            // The resident (local SP) outbound provisioning configs must be used.
            verify(outboundProvisioningManager).provision(
                    any(ProvisioningEntity.class),
                    eq(IdentityProvisioningConstants.LOCAL_SP),
                    eq(IdentityProvisioningConstants.WSO2_CARBON_DIALECT),
                    eq(TENANT_DOMAIN),
                    eq(false));

            // The service provider's outbound provisioning configs must NOT be used.
            verify(outboundProvisioningManager, Mockito.never()).provision(
                    any(ProvisioningEntity.class), eq(SP_NAME), anyString(), anyString(), anyBoolean());
        }
    }

    @Test
    public void testDoPreAddUserWhenNotSharedUserCreationUsesServiceProviderOutboundProvisioningConfigs()
            throws UserStoreException {

        // No shared user creation flag is set, so the service provider present in the thread local
        // should continue to drive the outbound provisioning configs.
        ThreadLocalProvisioningServiceProvider serviceProvider = mockThreadLocalServiceProvider();
        identityAppManagementUtilMockedStatic.when(
                        IdentityApplicationManagementUtil::getThreadLocalProvisioningServiceProvider)
                .thenReturn(serviceProvider);

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMockedStatic =
                     mockPrivilegedCarbonContext()) {

            boolean result = listener.doPreAddUser(USER_NAME, new StringBuffer("password"), null,
                    new HashMap<>(), "default", userStoreManager);

            assertTrue(result, "Should return true after provisioning");

            // The service provider's outbound provisioning configs must be used.
            verify(outboundProvisioningManager).provision(
                    any(ProvisioningEntity.class),
                    eq(SP_NAME),
                    eq(SP_CLAIM_DIALECT),
                    eq(TENANT_DOMAIN),
                    eq(true));

            // The resident (local SP) outbound provisioning configs must NOT be used.
            verify(outboundProvisioningManager, Mockito.never()).provision(
                    any(ProvisioningEntity.class), eq(IdentityProvisioningConstants.LOCAL_SP),
                    anyString(), anyString(), anyBoolean());
        }
    }

    private ThreadLocalProvisioningServiceProvider mockThreadLocalServiceProvider() {

        ThreadLocalProvisioningServiceProvider serviceProvider =
                Mockito.mock(ThreadLocalProvisioningServiceProvider.class);
        when(serviceProvider.getServiceProviderName()).thenReturn(SP_NAME);
        when(serviceProvider.getServiceProviderType()).thenReturn(null);
        when(serviceProvider.getClaimDialect()).thenReturn(SP_CLAIM_DIALECT);
        when(serviceProvider.isJustInTimeProvisioning()).thenReturn(true);
        return serviceProvider;
    }

    private MockedStatic<PrivilegedCarbonContext> mockPrivilegedCarbonContext() {

        MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMockedStatic =
                Mockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        privilegedCarbonContextMockedStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getApplicationResidentOrganizationId()).thenReturn(null);
        return privilegedCarbonContextMockedStatic;
    }
}
