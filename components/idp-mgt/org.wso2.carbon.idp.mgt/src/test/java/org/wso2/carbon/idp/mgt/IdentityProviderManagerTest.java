/*
 * Copyright (c) 2025-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.idp.mgt;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.RoleManagementService;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.FileBasedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

@WithCarbonHome
//@WithRealmService(injectToSingletons = {IdpMgtServiceComponentHolder.class}, initUserStoreManager = true)
public class IdentityProviderManagerTest {

    private static final String ORGANIZATION_LOGIN_IDP_NAME = "SSO";
    private static final String JWKS_URI = "jwksUri";
    private static final String OAUTH2_TOKEN_EP_URL = "/oauth2/token";
    private static final String OAUTH2_JWKS_EP_URL = "/oauth2/jwks";
    private static final String TENANT_DOMAIN = "foo.com";
    private static final String IDP_NAME = "https://localhost/oauth2/token";

    @Mock
    private CacheBackedIdPMgtDAO dao;
    @Mock
    private FileBasedIdPMgtDAO mockFileBasedDao;

    private IdentityProviderManager identityProviderManager;

    @BeforeMethod
    public void setUpClass() throws NoSuchFieldException, IllegalAccessException {

        identityProviderManager = IdentityProviderManager.getInstance();
        // Use reflection to inject the mock dao into the static field.
        Field daoField = IdentityProviderManager.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(identityProviderManager, dao);
    }

    @BeforeTest
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test(description = "Tests get IdP by name when file based IdP is not available.")
    public void testGetIdPByName() throws IdentityProviderManagementException {

        IdentityProvider mockIdP = new IdentityProvider();
        mockIdP.setId("123");
        when(dao.getIdPByName(null, IDP_NAME, 1, TENANT_DOMAIN)).thenReturn(mockIdP);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);

            IdentityProvider result = identityProviderManager.getIdPByName(IDP_NAME, TENANT_DOMAIN, true);

            assertNotNull(result);
            assertEquals("123", result.getId());
        }
    }

    @Test(description = "Tests get SSO IDP with JWKS URI.")
    public void testGetSSOIDPWithJWKSUri() throws IdentityProviderManagementException {
        String jwtIssuer = "https://localhost/o/ba972190-391a-42a0-92e8-5eb58fbcfae3/oauth2/token";
        IdentityProvider ssoIdP = new IdentityProvider();
        ssoIdP.setId("ssoIdP");
        ssoIdP.setIdpProperties(new IdentityProviderProperty[0]);

        when(dao.getIdPByName(null, jwtIssuer, 1, TENANT_DOMAIN)).thenReturn(null);
        when(dao.getIdPByName(null, ORGANIZATION_LOGIN_IDP_NAME, 1, TENANT_DOMAIN)).thenReturn(ssoIdP);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class)) {

            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            mockedIdentityUtil.when(IdentityUtil::getHostName).thenReturn("localhost");

            IdentityProvider result = identityProviderManager.getIdPByName(jwtIssuer, TENANT_DOMAIN, true);

            assertNotNull(result);
            assertEquals("ssoIdP", result.getId());
            assertTrue(Arrays.stream(result.getIdpProperties())
                    .anyMatch(p -> JWKS_URI.equals(p.getName()) &&
                            p.getValue().equals(jwtIssuer.replace(OAUTH2_TOKEN_EP_URL, OAUTH2_JWKS_EP_URL))));
        }
    }

    @Test(description = "Test validation passes when provisioning role is blank.")
    public void testValidateOutboundProvisioningRolesWithBlankRole() throws Exception {

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("");

        invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
    }

    @Test(description = "Test validation passes for existing Internal/ roles.")
    public void testValidateOutboundProvisioningRolesWithExistingRoles() throws Exception {

        RoleManagementService mockRoleManagementService = mock(RoleManagementService.class);
        when(mockRoleManagementService.isExistingRoleName("Internal/admin", TENANT_DOMAIN)).thenReturn(true);
        when(mockRoleManagementService.isExistingRoleName("Internal/everyone", TENANT_DOMAIN)).thenReturn(true);
        IdpMgtServiceComponentHolder.getInstance().setRoleManagementService(mockRoleManagementService);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("Internal/admin,Internal/everyone");

        invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        // Should not throw any exception.
        verify(mockRoleManagementService).isExistingRoleName("Internal/admin", TENANT_DOMAIN);
        verify(mockRoleManagementService).isExistingRoleName("Internal/everyone", TENANT_DOMAIN);
    }

    @Test(description = "Test validation fails for non-existing Internal/ roles.",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testValidateOutboundProvisioningRolesWithNonExistingRole() throws Exception {

        RoleManagementService mockRoleManagementService = mock(RoleManagementService.class);
        when(mockRoleManagementService.isExistingRoleName("Internal/nonExistent", TENANT_DOMAIN)).thenReturn(false);
        IdpMgtServiceComponentHolder.getInstance().setRoleManagementService(mockRoleManagementService);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("Internal/nonExistent");

        invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
    }

    @Test(description = "Test validation passes for existing groups.")
    public void testValidateOutboundProvisioningRolesWithExistingGroup() throws Exception {

        AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
        when(mockUserStoreManager.isGroupExistWithName("engineers")).thenReturn(true);
        setupRealmService(mockUserStoreManager);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("engineers");

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        }
        // Should not throw any exception.
        verify(mockUserStoreManager).isGroupExistWithName("engineers");
    }

    @Test(description = "Test validation fails for non-existing groups.",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testValidateOutboundProvisioningRolesWithNonExistingGroup() throws Exception {

        AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
        when(mockUserStoreManager.isGroupExistWithName("nonExistentGroup")).thenReturn(false);
        setupRealmService(mockUserStoreManager);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("nonExistentGroup");

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        }
    }

    @Test(description = "Test validation passes for mixed roles and groups when all exist.")
    public void testValidateOutboundProvisioningRolesWithMixedRolesAndGroups() throws Exception {

        RoleManagementService mockRoleManagementService = mock(RoleManagementService.class);
        when(mockRoleManagementService.isExistingRoleName("Internal/admin", TENANT_DOMAIN)).thenReturn(true);
        IdpMgtServiceComponentHolder.getInstance().setRoleManagementService(mockRoleManagementService);

        AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
        when(mockUserStoreManager.isGroupExistWithName("engineers")).thenReturn(true);
        setupRealmService(mockUserStoreManager);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("Internal/admin,engineers");

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        }
        verify(mockRoleManagementService).isExistingRoleName("Internal/admin", TENANT_DOMAIN);
        verify(mockUserStoreManager).isGroupExistWithName("engineers");
    }

    @Test(description = "Test validation fails when role exists but group does not in mixed input.",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testValidateOutboundProvisioningRolesWithMixedExistingRoleAndNonExistingGroup() throws Exception {

        RoleManagementService mockRoleManagementService = mock(RoleManagementService.class);
        when(mockRoleManagementService.isExistingRoleName("Internal/admin", TENANT_DOMAIN)).thenReturn(true);
        IdpMgtServiceComponentHolder.getInstance().setRoleManagementService(mockRoleManagementService);

        AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
        when(mockUserStoreManager.isGroupExistWithName("nonExistentGroup")).thenReturn(false);
        setupRealmService(mockUserStoreManager);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("Internal/admin,nonExistentGroup");

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        }
    }

    @Test(description = "Test validation passes for domain-qualified group names.")
    public void testValidateOutboundProvisioningRolesWithDomainQualifiedGroup() throws Exception {

        AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
        when(mockUserStoreManager.isGroupExistWithName("SECONDARY/engineers")).thenReturn(true);
        setupRealmService(mockUserStoreManager);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("SECONDARY/engineers");

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        }
        verify(mockUserStoreManager).isGroupExistWithName("SECONDARY/engineers");
    }

    @Test(description = "Test Application/ prefixed entries are validated as roles, not groups.")
    public void testValidateOutboundProvisioningRolesWithApplicationRole() throws Exception {

        RoleManagementService mockRoleManagementService = mock(RoleManagementService.class);
        when(mockRoleManagementService.isExistingRoleName("Application/myAppRole", TENANT_DOMAIN)).thenReturn(true);
        IdpMgtServiceComponentHolder.getInstance().setRoleManagementService(mockRoleManagementService);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("Application/myAppRole");

        invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        verify(mockRoleManagementService).isExistingRoleName("Application/myAppRole", TENANT_DOMAIN);
    }

    @Test(description = "Test validation wraps IdentityRoleManagementException as server exception.")
    public void testValidateOutboundProvisioningRolesWithRoleManagementException() throws Exception {

        RoleManagementService mockRoleManagementService = mock(RoleManagementService.class);
        when(mockRoleManagementService.isExistingRoleName("Internal/admin", TENANT_DOMAIN))
                .thenThrow(new IdentityRoleManagementException("ROLE-00001", "Test error"));
        IdpMgtServiceComponentHolder.getInstance().setRoleManagementService(mockRoleManagementService);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("Internal/admin");

        try {
            invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
            fail("Expected IdentityProviderManagementServerException to be thrown.");
        } catch (IdentityProviderManagementServerException e) {
            assertEquals(IdPManagementConstants.ErrorMessage
                    .ERROR_CODE_VALIDATING_OUTBOUND_PROVISIONING_ROLES.getCode(), e.getErrorCode());
        }
    }

    @Test(description = "Test validation passes for multiple existing groups.")
    public void testValidateOutboundProvisioningRolesWithMultipleExistingGroups() throws Exception {

        AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
        when(mockUserStoreManager.isGroupExistWithName("engineers")).thenReturn(true);
        when(mockUserStoreManager.isGroupExistWithName("managers")).thenReturn(true);
        setupRealmService(mockUserStoreManager);

        IdentityProvider idp = new IdentityProvider();
        idp.setProvisioningRole("engineers,managers");

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            invokeValidateOutboundProvisioningRoles(idp, TENANT_DOMAIN);
        }
        verify(mockUserStoreManager).isGroupExistWithName("engineers");
        verify(mockUserStoreManager).isGroupExistWithName("managers");
    }

    /**
     * Invokes the private validateOutboundProvisioningRoles method via reflection.
     * Unwraps InvocationTargetException to throw the actual cause.
     */
    private void invokeValidateOutboundProvisioningRoles(IdentityProvider idp, String tenantDomain) throws Exception {

        Method method = IdentityProviderManager.class.getDeclaredMethod(
                "validateOutboundProvisioningRoles", IdentityProvider.class, String.class);
        method.setAccessible(true);
        try {
            method.invoke(identityProviderManager, idp, tenantDomain);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Sets up the RealmService mock so that IdPManagementServiceComponent.getRealmService() returns a mock chain
     * leading to the provided userStoreManager.
     */
    private void setupRealmService(AbstractUserStoreManager userStoreManager)
            throws org.wso2.carbon.user.api.UserStoreException {

        RealmService mockRealmService = mock(RealmService.class);
        UserRealm mockUserRealm = mock(UserRealm.class);
        RealmConfiguration mockRealmConfiguration = mock(RealmConfiguration.class);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(userStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockRealmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn("PRIMARY");
        IdpMgtServiceComponentHolder.getInstance().setRealmService(mockRealmService);
    }

    @Test(description = "Tests addResidentIdP includes ENABLE_JWT_SCOPE_AS_ARRAY property for root organization.")
    public void testAddResidentIdPIncludesJwtScopeAsArrayForNonOrganization() throws Exception {

        IdentityProvider residentIdP = new IdentityProvider();

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<OrganizationManagementUtil> mockedOrgUtil = mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdPManagementServiceComponent> mockedServiceComp =
                     mockStatic(IdPManagementServiceComponent.class)) {

            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            mockedOrgUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_DOMAIN)).thenReturn(false);
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn(null);
            mockedServiceComp.when(IdPManagementServiceComponent::getIdpMgtListeners)
                    .thenReturn(Collections.emptyList());

            identityProviderManager.addResidentIdP(residentIdP, TENANT_DOMAIN);

            FederatedAuthenticatorConfig oidcConfig = getOIDCAuthenticatorConfig(residentIdP);
            assertNotNull("OIDC authenticator config should be present", oidcConfig);

            boolean hasJwtScopeAsArray = Arrays.stream(oidcConfig.getProperties())
                    .anyMatch(p -> IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY
                            .equals(p.getName())
                            && IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY_DEFAULT
                            .equals(p.getValue()));
            assertTrue("OIDC config should contain ENABLE_JWT_SCOPE_AS_ARRAY property with default value",
                    hasJwtScopeAsArray);
        }
    }

    @Test(description = "Tests addResidentIdP excludes ENABLE_JWT_SCOPE_AS_ARRAY property for sub organization.")
    public void testAddResidentIdPExcludesJwtScopeAsArrayForOrganization() throws Exception {

        IdentityProvider residentIdP = new IdentityProvider();

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<OrganizationManagementUtil> mockedOrgUtil = mockStatic(OrganizationManagementUtil.class);
             MockedStatic<IdentityUtil> mockedIdentityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdPManagementServiceComponent> mockedServiceComp =
                     mockStatic(IdPManagementServiceComponent.class)) {

            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
            mockedOrgUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_DOMAIN)).thenReturn(true);
            mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn(null);
            mockedServiceComp.when(IdPManagementServiceComponent::getIdpMgtListeners)
                    .thenReturn(Collections.emptyList());

            identityProviderManager.addResidentIdP(residentIdP, TENANT_DOMAIN);

            FederatedAuthenticatorConfig oidcConfig = getOIDCAuthenticatorConfig(residentIdP);
            assertNotNull("OIDC authenticator config should be present", oidcConfig);

            boolean hasJwtScopeAsArray = Arrays.stream(oidcConfig.getProperties())
                    .anyMatch(p -> IdentityApplicationConstants.Authenticator.OIDC.ENABLE_JWT_SCOPE_AS_ARRAY
                            .equals(p.getName()));
            assertFalse("OIDC config should NOT contain ENABLE_JWT_SCOPE_AS_ARRAY property for organization",
                    hasJwtScopeAsArray);
        }
    }

    private FederatedAuthenticatorConfig getOIDCAuthenticatorConfig(IdentityProvider identityProvider) {

        FederatedAuthenticatorConfig[] fedAuthConfigs = identityProvider.getFederatedAuthenticatorConfigs();
        if (fedAuthConfigs == null) {
            return null;
        }
        return Arrays.stream(fedAuthConfigs)
                .filter(c -> IdentityApplicationConstants.Authenticator.OIDC.NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }
}
