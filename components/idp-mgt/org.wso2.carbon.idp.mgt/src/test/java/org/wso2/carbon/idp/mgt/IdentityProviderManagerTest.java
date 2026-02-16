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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.FileBasedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@WithCarbonHome
//@WithRealmService(injectToSingletons = {IdpMgtServiceComponentHolder.class}, initUserStoreManager = true)
public class IdentityProviderManagerTest {

    private static final String ORGANIZATION_LOGIN_IDP_NAME = "SSO";
    private static final String JWKS_URI = "jwksUri";
    private static final String OAUTH2_TOKEN_EP_URL = "/oauth2/token";
    private static final String OAUTH2_JWKS_EP_URL = "/oauth2/jwks";
    private static final String TENANT_DOMAIN = "foo.com";
    private static final String IDP_NAME = "https://localhost/oauth2/token";
    private static final String TEST_TENANT_DOMAIN = "carbon.super";
    private static final String TEST_RESOURCE_ID = "test-resource-id";
    private static final String TEST_IDP_NAME = "TestIDP";
    private static final int TEST_TENANT_ID = -1234;

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

    /**
     * Test getConnectedApplications with valid parameters and filter.
     */
    @Test(description = "Test getConnectedApplications with valid filter")
    public void testGetConnectedApplicationsWithValidFilter() throws Exception {

        ConnectedAppsResult expectedResult = createConnectedAppsResult(5, 50, 10, 0);
        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(expectedResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, 0, "name sw \"test\"", TEST_TENANT_DOMAIN);

            assertNotNull(result);
            assertEquals(5, result.getApps().size());
            assertEquals(50, result.getTotalAppCount());
        }
    }

    /**
     * Test with null filter (should work without exception).
     */
    @Test(description = "Test getConnectedApplications with null filter")
    public void testGetConnectedApplicationsWithNullFilter() throws Exception {

        ConnectedAppsResult expectedResult = createConnectedAppsResult(3, 30, 10, 0);
        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(expectedResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, 0, null, TEST_TENANT_DOMAIN);

            assertNotNull(result);
        }
    }

    /**
     * Test with empty filter (should work without exception).
     */
    @Test(description = "Test getConnectedApplications with empty filter")
    public void testGetConnectedApplicationsWithEmptyFilter() throws Exception {

        ConnectedAppsResult expectedResult = createConnectedAppsResult(3, 30, 10, 0);
        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(expectedResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, 0, "", TEST_TENANT_DOMAIN);

            assertNotNull(result);
        }
    }

    /**
     * Test with complex filter expression.
     */
    @Test(description = "Test getConnectedApplications with complex filter")
    public void testGetConnectedApplicationsWithComplexFilter() throws Exception {

        ConnectedAppsResult expectedResult = createConnectedAppsResult(2, 20, 10, 0);
        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(expectedResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            String complexFilter = "name sw \"app\" and name ew \"test\"";
            ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, 0, complexFilter, TEST_TENANT_DOMAIN);

            assertNotNull(result);
        }
    }

    /**
     * Test with invalid resource ID (should throw exception).
     */
    @Test(description = "Test getConnectedApplications with invalid resource ID",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testGetConnectedApplicationsWithInvalidResourceId() throws Exception {

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(null);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, 0, "name sw \"test\"", TEST_TENANT_DOMAIN);
        }
    }

    /**
     * Test with null resource ID (should throw exception).
     */
    @Test(description = "Test getConnectedApplications with null resource ID",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testGetConnectedApplicationsWithNullResourceId() throws Exception {

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            identityProviderManager.getConnectedApplications(
                    null, 10, 0, "name sw \"test\"", TEST_TENANT_DOMAIN);
        }
    }

    /**
     * Test with empty resource ID (should throw exception).
     */
    @Test(description = "Test getConnectedApplications with empty resource ID",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testGetConnectedApplicationsWithEmptyResourceId() throws Exception {

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            identityProviderManager.getConnectedApplications(
                    "", 10, 0, "name sw \"test\"", TEST_TENANT_DOMAIN);
        }
    }

    /**
     * Test limit validation with negative limit (should throw exception).
     */
    @Test(description = "Test getConnectedApplications with negative limit",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testGetConnectedApplicationsWithNegativeLimit() throws Exception {

        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);
        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, -5, 0, "name sw \"test\"", TEST_TENANT_DOMAIN);
        }
    }

    /**
     * Test offset validation with negative offset (should throw exception).
     */
    @Test(description = "Test getConnectedApplications with negative offset",
            expectedExceptions = IdentityProviderManagementClientException.class)
    public void testGetConnectedApplicationsWithNegativeOffset() throws Exception {

        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);
        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, -1, "name sw \"test\"", TEST_TENANT_DOMAIN);
        }
    }

    /**
     * Test with pagination parameters.
     */
    @Test(description = "Test getConnectedApplications with pagination")
    public void testGetConnectedApplicationsWithPagination() throws Exception {

        ConnectedAppsResult expectedResult = createConnectedAppsResult(5, 50, 5, 10);
        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(expectedResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 5, 10, "name co \"app\"", TEST_TENANT_DOMAIN);

            assertNotNull(result);
            assertEquals(5, result.getApps().size());
            assertEquals(50, result.getTotalAppCount());
            assertEquals(5, result.getLimit());
            assertEquals(10, result.getOffSet());
        }
    }

    /**
     * Test with invalid filter format that causes parsing exception.
     */
    @Test(description = "Test getConnectedApplications with invalid filter")
    public void testGetConnectedApplicationsWithInvalidFilter() throws Exception {

        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);
        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);

        // Mock ConnectedAppsResult for when filter parsing succeeds unexpectedly.
        ConnectedAppsResult mockResult = createConnectedAppsResult(0, 0, 10, 0);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(mockResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            // Invalid filter format with unclosed quote which might cause parser exception.
            String invalidFilter = "name eq \"unclosed";

            try {
                ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                        TEST_RESOURCE_ID, 10, 0, invalidFilter, TEST_TENANT_DOMAIN);
                // If no exception thrown, parser handled it. Verify method was called.
                assertNotNull(result);
            } catch (IdentityProviderManagementClientException e) {
                // Should fail during filter parsing with ERROR_CODE_RETRIEVE_IDP.
                assertEquals(IdPManagementConstants.ErrorMessage
                        .ERROR_CODE_RETRIEVE_IDP.getCode(), e.getErrorCode());
                assertNotNull(e.getMessage());
            }
        }
    }

    /**
     * Data provider for various filter expressions.
     */
    @DataProvider(name = "filterExpressions")
    public Object[][] filterExpressions() {

        return new Object[][]{
                {"name eq \"test\""},                    // Equal operator
                {"name ne \"test\""},                    // Not equal operator
                {"name sw \"test\""},                    // Starts with operator
                {"name ew \"test\""},                    // Ends with operator
                {"name co \"test\""},                    // Contains operator
                {"name sw \"test\" and name ew \"app\""}, // AND operator
                {"name sw \"test\" or name co \"app\""}  // OR operator
        };
    }

    /**
     * Test various filter expressions.
     */
    @Test(dataProvider = "filterExpressions", description = "Test getConnectedApplications with various filters")
    public void testGetConnectedApplicationsWithVariousFilters(String filter) throws Exception {

        ConnectedAppsResult expectedResult = createConnectedAppsResult(5, 50, 10, 0);
        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(expectedResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, 0, filter, TEST_TENANT_DOMAIN);

            assertNotNull(result);
        }
    }

    /**
     * Test when no connected applications are found.
     */
    @Test(description = "Test getConnectedApplications with no results")
    public void testGetConnectedApplicationsWithNoResults() throws Exception {

        ConnectedAppsResult expectedResult = createConnectedAppsResult(0, 0, 10, 0);
        IdentityProvider mockIdp = createMockIdentityProvider(TEST_RESOURCE_ID, TEST_IDP_NAME);

        when(dao.getIdPByResourceId(TEST_RESOURCE_ID, TEST_TENANT_ID, TEST_TENANT_DOMAIN))
                .thenReturn(mockIdp);
        when(dao.getConnectedApplications(anyString(), anyInt(), anyInt(), anyList()))
                .thenReturn(expectedResult);

        try (MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TEST_TENANT_DOMAIN))
                    .thenReturn(TEST_TENANT_ID);

            ConnectedAppsResult result = identityProviderManager.getConnectedApplications(
                    TEST_RESOURCE_ID, 10, 0, "name sw \"nonexistent\"", TEST_TENANT_DOMAIN);

            assertNotNull(result);
            assertEquals(0, result.getApps().size());
            assertEquals(0, result.getTotalAppCount());
        }
    }

    /**
     * Helper method to create a mock ConnectedAppsResult.
     */
    private ConnectedAppsResult createConnectedAppsResult(int appCount, int totalCount, int limit, int offset) {

        ConnectedAppsResult result = new ConnectedAppsResult();
        List<String> apps = new ArrayList<>();
        for (int i = 0; i < appCount; i++) {
            apps.add("App" + i);
        }
        result.setApps(apps);
        result.setTotalAppCount(totalCount);
        result.setLimit(limit);
        result.setOffSet(offset);
        return result;
    }

    /**
     * Helper method to create a mock IdentityProvider.
     */
    private IdentityProvider createMockIdentityProvider(String resourceId, String idpName) {

        IdentityProvider idp = new IdentityProvider();
        idp.setResourceId(resourceId);
        idp.setIdentityProviderName(idpName);
        return idp;
    }
}
