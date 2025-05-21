/*
 * Copyright (c) 2023-2025, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.api.resource.mgt.model.APIResourceSearchResult;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.APIResourceTypes;
import static org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_RETRIEVING_ROOT_ORGANIZATION_TENANT_DOMAIN;

@WithAxisConfiguration
@WithCarbonHome
@WithRegistry
@WithRealmService(injectToSingletons = {OrganizationManagementDataHolder.class})
@WithH2Database(files = {"dbscripts/h2.sql"})
public class APIResourceManagerTest {

    private final String postFix1 = "test1";
    private final String postFix2 = "test2";
    private final String postFix3 = "test3";
    private final String apiResourceID = "sampleAPIResourceID";

    private String tenantDomain;
    private String subOrgTenantDomain;
    private APIResourceManager apiResourceManager;
    @Mock
    private IdentityEventService identityEventService;

    @BeforeMethod
    public void setUp() throws IdentityEventException {

        apiResourceManager = APIResourceManagerImpl.getInstance();
        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        identityEventService = mock(IdentityEventService.class);
        doNothing().when(identityEventService).handleEvent(any());
        APIResourceManagementServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        APIResourceManagementServiceComponentHolder.getInstance().setRichAuthorizationRequestsEnabled(true);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        removeTestAPIResources();
    }

    @DataProvider(name = "getAPIResourceDataProvider")
    public Object[][] getAPIResourceDataProvider() {

        return new Object[][]{
                {null, null, 2, null, "ASC", 2},
                {null, null, 3, null, "DESC", 3},
        };
    }

    @Test(dataProvider = "getAPIResourceDataProvider")
    public void testGetAPIResource(String after, String before, Integer limit, String filter, String sortOrder,
                                   int expected) throws Exception {

        addTestAPIResources();
        APIResourceSearchResult apiResourceSearchResult = apiResourceManager.getAPIResources(after, before, limit,
                filter, sortOrder, tenantDomain);
        Assert.assertNotNull(apiResourceSearchResult.getAPIResources());
        Assert.assertEquals(apiResourceSearchResult.getAPIResources().size(), expected);
    }

    @Test
    public void testGetAPIResourceById() throws Exception {

        APIResource createdAPIResource = apiResourceManager.addAPIResource(
                createAPIResource(postFix1, APIResourceTypes.BUSINESS), tenantDomain);
        APIResource apiResource = apiResourceManager.getAPIResourceById(createdAPIResource.getId(), tenantDomain);
        Assert.assertNotNull(apiResource);
        Assert.assertEquals(apiResource.getId(), createdAPIResource.getId());
    }

    @DataProvider(name = "getAPIResourceByIdForSubOrgDataProvider")
    public Object[][] getAPIResourceByIdForSubOrgDataProvider() {

        return new Object[][]{
                {APIResourceTypes.BUSINESS, true},
                {APIResourceTypes.ORGANIZATION, true},
                {APIResourceTypes.CONSOLE_ORG_FEATURE, true},
                {APIResourceTypes.CONSOLE_ORG_LEVEL, true},
                {APIResourceTypes.SYSTEM, false},
                {APIResourceTypes.TENANT, false},
                {APIResourceTypes.CONSOLE_FEATURE, false}
        };
    }

    @Test(dataProvider = "getAPIResourceByIdForSubOrgDataProvider")
    public void testGetAPIResourceByIdForSubOrg(String apiResourceType, boolean isAccessibleToSubOrg) throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(subOrgTenantDomain))
                    .thenReturn(true);
            organizationManagementUtil.when(() -> OrganizationManagementUtil
                            .getRootOrgTenantDomainBySubOrgTenantDomain(subOrgTenantDomain)).thenReturn(tenantDomain);

            APIResource createdAPIResource = apiResourceManager.addAPIResource(createAPIResource(postFix1,
                    apiResourceType), tenantDomain);
            APIResource apiResource = apiResourceManager.getAPIResourceById(createdAPIResource.getId(),
                    subOrgTenantDomain);

            if (isAccessibleToSubOrg) {
                Assert.assertNotNull(apiResource);
                Assert.assertEquals(apiResource.getId(), createdAPIResource.getId());
            } else {
                Assert.assertNull(apiResource);
            }
        }
    }

    @Test
    public void testGetAPIResourceByInvalidIdForSubOrg() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(subOrgTenantDomain))
                    .thenReturn(true);
            organizationManagementUtil.when(() -> OrganizationManagementUtil
                    .getRootOrgTenantDomainBySubOrgTenantDomain(subOrgTenantDomain)).thenReturn(tenantDomain);

            APIResource apiResource = apiResourceManager.getAPIResourceById(apiResourceID, subOrgTenantDomain);
            Assert.assertNull(apiResource);
        }
    }

    @Test
    public void testGetAPIResourceByIdForSubOrgException() throws Exception {

        try (MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {

            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(subOrgTenantDomain))
                    .thenReturn(true);
            organizationManagementUtil.when(() -> OrganizationManagementUtil
                            .getRootOrgTenantDomainBySubOrgTenantDomain(subOrgTenantDomain))
                    .thenThrow(new OrganizationManagementException("Test exception."));

            try {
                apiResourceManager.getAPIResourceById(apiResourceID, subOrgTenantDomain);
                Assert.fail("Expected OrganizationManagementException to be thrown.");
            } catch (APIResourceMgtException e) {
                Assert.assertTrue(e.getMessage()
                        .contains(ERROR_CODE_ERROR_WHILE_RETRIEVING_ROOT_ORGANIZATION_TENANT_DOMAIN.getMessage()));
                Assert.assertTrue(e.getCause() instanceof OrganizationManagementException);
            }
        }
    }

    @DataProvider(name = "addAPIResourceDataProvider")
    public Object[][] addAPIResourceDataProvider() {

        APIResource apiResource1 = createAPIResource("1", APIResourceTypes.BUSINESS);
        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name 2")
                .identifier("testAPIResource identifier 2")
                .type("BUSINESS");
        APIResource apiResource2 = apiResourceBuilder.build();

        return new Object[][]{
                // API resource with scopes.
                {apiResource1},
                // API resource with only the identifier.
                {apiResource2}
        };
    }

    @Test(dataProvider = "addAPIResourceDataProvider")
    public void testAddAPIResource(APIResource apiResource) throws Exception {

        APIResourceMgtClientException exception = null;
        try {
            apiResourceManager.addAPIResource(apiResource, tenantDomain);
        } catch (APIResourceMgtClientException e) {
            exception = e;
        }
        Assert.assertNull(exception);
        Assert.assertNotNull(apiResourceManager.getAPIResourceByIdentifier(apiResource.getIdentifier(), tenantDomain));
    }

    @DataProvider(name = "addAPIResourceExceptionDataProvider")
    public Object[][] addAPIResourceExceptionDataProvider() {

        APIResource apiResource1 = createAPIResource(postFix1, APIResourceTypes.BUSINESS);

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name 2")
                .description("testAPIResource description 2");
        APIResource apiResource2 = apiResourceBuilder.build();

        return new Object[][]{
                // Duplicate API resource.
                {apiResource1},
                // API resource without identifier.
                {apiResource2}
        };
    }

    @Test(dataProvider = "addAPIResourceExceptionDataProvider")
    public void testAddAPIResourceException(APIResource apiResource) throws Exception {

        addTestAPIResources();
        APIResourceMgtException exception = null;
        try {
            apiResourceManager.addAPIResource(apiResource, tenantDomain);
        } catch (APIResourceMgtException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }

    @Test
    public void testDeleteAPIResourceById() throws Exception {

        APIResource createdAPIResource = apiResourceManager.addAPIResource(
                createAPIResource(postFix1, APIResourceTypes.BUSINESS), tenantDomain);
        apiResourceManager.deleteAPIResourceById(createdAPIResource.getId(), tenantDomain);
        Assert.assertNull(apiResourceManager.getAPIResourceById(createdAPIResource.getId(), tenantDomain));
    }

    @DataProvider
    public Object[][] updateAPIResourceTestData() {

        APIResource apiResource1 = createAPIResource(postFix1, APIResourceTypes.BUSINESS);
        APIResource apiResource2 = createAPIResource(postFix2, APIResourceTypes.BUSINESS);

        return new Object[][]{
                // Update API resource with scopes.
                {apiResource1, null, null, Arrays.asList(createScope("updated1"),
                        createScope("update2"))},
                // Update API resource with name and description.
                {apiResource2, "test2 updated name", "test2 updated description", null}
        };
    }

    @Test(dataProvider = "updateAPIResourceTestData")
    public void testUpdateAPIResource(APIResource apiResource, String updatedName, String updatedDescription,
                                      List<Scope> addedScopes) throws Exception {

        APIResource createdAPIResource = apiResourceManager.addAPIResource(apiResource, tenantDomain);
        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .id(createdAPIResource.getId())
                .name(createdAPIResource.getName())
                .description(createdAPIResource.getDescription())
                .scopes(createdAPIResource.getScopes())
                .type(createdAPIResource.getType())
                .identifier(createdAPIResource.getIdentifier())
                .requiresAuthorization(createdAPIResource.isAuthorizationRequired());

        if (updatedName != null) {
            apiResourceBuilder.name(updatedName);
        }
        if (updatedDescription != null) {
            apiResourceBuilder.description(updatedDescription);
        }
        createdAPIResource = apiResourceBuilder.build();
        apiResourceManager.updateAPIResource(createdAPIResource, addedScopes, null, tenantDomain);
        APIResource updatedAPIResource = apiResourceManager.getAPIResourceById(createdAPIResource.getId(),
                tenantDomain);
        if (addedScopes != null) {
            Assert.assertEquals(updatedAPIResource.getScopes().size(), 4);
        }
        if (updatedName != null) {
            Assert.assertEquals(updatedAPIResource.getName(), updatedName);
        }
        if (updatedDescription != null) {
            Assert.assertEquals(updatedAPIResource.getDescription(), updatedDescription);
        }
    }

    @DataProvider(name = "getAPIResourceByIdentifierDataProvider")
    public Object[][] getAPIResourceByIdentifierDataProvider() {

        return new Object[][]{
                {"testAPIResource identifier test1"}
        };
    }

    @Test(dataProvider = "getAPIResourceByIdentifierDataProvider")
    public void testGetAPIResourceByIdentifier(String identifier) throws Exception {

        apiResourceManager.addAPIResource(createAPIResource(postFix1, APIResourceTypes.BUSINESS), tenantDomain);
        APIResource apiResource = apiResourceManager.getAPIResourceByIdentifier(identifier, tenantDomain);
        Assert.assertNotNull(apiResource);
    }

    @Test
    public void testGetAPIScopesById() throws Exception {

        APIResource createdAPIResource = apiResourceManager.addAPIResource(
                createAPIResource(postFix1, APIResourceTypes.BUSINESS), tenantDomain);
        List<Scope> scopes = apiResourceManager.getAPIScopesById(createdAPIResource.getId(), tenantDomain);
        Assert.assertNotNull(scopes);
    }

    @Test
    public void testDeleteAPIScopesById() throws Exception {

        APIResource createdAPIResource = apiResourceManager.addAPIResource(
                createAPIResource(postFix1, APIResourceTypes.BUSINESS), tenantDomain);
        apiResourceManager.deleteAPIScopesById(createdAPIResource.getId(), tenantDomain);
        List<Scope> scopes = apiResourceManager.getAPIScopesById(createdAPIResource.getId(), tenantDomain);
        Assert.assertTrue(scopes.isEmpty());
    }

    @Test
    public void testDeleteAPIScopeByScopeId() throws Exception {

        APIResource createdAPIResource = apiResourceManager.addAPIResource(
                createAPIResource(postFix1, APIResourceTypes.BUSINESS), tenantDomain);
        List<Scope> scopes = createdAPIResource.getScopes();
        apiResourceManager.deleteAPIScopeByScopeName(createdAPIResource.getId(), scopes.get(0).getName(), tenantDomain);
        scopes = apiResourceManager.getAPIScopesById(createdAPIResource.getId(), tenantDomain);
        Assert.assertEquals(scopes.size(), 1);
    }

    @DataProvider(name = "putScopesDataProvider")
    public Object[][] putScopesDataProvider() {

        APIResource apiResource1 = createAPIResource(postFix1, APIResourceTypes.BUSINESS);
        APIResource apiResource2 = createAPIResource(postFix2, APIResourceTypes.BUSINESS);

        return new Object[][]{
                // Update API resource with scopes.
                {apiResource1, Arrays.asList(createScope("updated1"), createScope("update2"))},
                // Update API resource with name and description.
                {apiResource2, null}
        };
    }

    @Test(dataProvider = "putScopesDataProvider")
    public void testPutScopes(APIResource apiResource, List<Scope> scopes) throws Exception {

        APIResource createdAPIResource = apiResourceManager.addAPIResource(apiResource, tenantDomain);
        apiResourceManager.putScopes(createdAPIResource.getId(), createdAPIResource.getScopes(), scopes, tenantDomain);
        APIResource updatedAPIResource = apiResourceManager.getAPIResourceById(createdAPIResource.getId(),
                tenantDomain);
        if (scopes != null) {
            Assert.assertEquals(updatedAPIResource.getScopes().size(), 2);
        }
    }

    @DataProvider(name = "getScopesByTenantIdDataProvider")
    public Object[][] getScopesByTenantIdDataProvider() {

        return new Object[][]{
                {null, 6},
                {"name co 1", 2},
                {"name eq testScopeOne test1", 1},
                {"name sw test", 6}
        };
    }

    @Test(dataProvider = "getScopesByTenantIdDataProvider")
    public void testGetScopesByTenantId(String filter, int expected) throws Exception {

        addTestAPIResources();
        List<Scope> scopes = apiResourceManager.getScopesByTenantDomain(tenantDomain, filter);
        Assert.assertEquals(scopes.size(), expected);
    }

    /**
     * Create scope with the given name.
     *
     * @param name Name of the scope.
     * @return Scope.
     */
    private static Scope createScope(String name) {

        Scope.ScopeBuilder scopeBuilder = new Scope.ScopeBuilder()
                .name(name)
                .displayName("displayName " + name)
                .description("description " + name);
        return scopeBuilder.build();
    }

    /**
     * Create API resource with the given postfix.
     *
     * @param postFix Postfix to be appended to each API resource and scope information.
     * @return API resource.
     */
    private static APIResource createAPIResource(String postFix, String type) {

        List<Scope> scopes = new ArrayList<>();
        scopes.add(createScope("testScopeOne " + postFix));
        scopes.add(createScope("testScopeTwo " + postFix));

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name " + postFix)
                .identifier("testAPIResource identifier " + postFix)
                .description("testAPIResource description " + postFix)
                .type(type)
                .requiresAuthorization(true)
                .scopes(scopes);
        return apiResourceBuilder.build();
    }

    private void addTestAPIResources() throws Exception {

        APIResource apiResource1 = createAPIResource(postFix1, APIResourceTypes.BUSINESS);
        APIResource apiResource2 = createAPIResource(postFix2, APIResourceTypes.BUSINESS);
        APIResource apiResource3 = createAPIResource(postFix3, APIResourceTypes.BUSINESS);
        apiResourceManager.addAPIResource(apiResource1, tenantDomain);
        apiResourceManager.addAPIResource(apiResource2, tenantDomain);
        apiResourceManager.addAPIResource(apiResource3, tenantDomain);
    }

    private void removeTestAPIResources() throws Exception {

        apiResourceManager.getAPIResources(null, null, 10, null, "ASC", tenantDomain)
                .getAPIResources().forEach(
                        apiResource -> {
                            try {
                                apiResourceManager.deleteAPIResourceById(apiResource.getId(), tenantDomain);
                            } catch (APIResourceMgtException e) {
                                Assert.fail("Error while deleting API resource: " + apiResource.getIdentifier(), e);
                            }
                        }
                );
    }
}
