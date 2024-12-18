/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TYPE_1;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TYPE_2;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TYPE_3;
import static org.wso2.carbon.identity.api.resource.mgt.TestDAOUtils.TEST_TYPE_INVALID;

/**
 * Test class for {@link AuthorizationDetailsTypeManager}.
 */
@WithCarbonHome
@WithRegistry
@WithRealmService(injectToSingletons = {OrganizationManagementDataHolder.class})
@WithH2Database(files = {"dbscripts/h2.sql"})
public class AuthorizationDetailsTypeManagerTest {

    private String tenantDomain;
    private APIResource apiResource;
    private AuthorizationDetailsTypeManager uut;
    private List<AuthorizationDetailsType> authorizationDetailsTypes;
    @Mock
    private IdentityEventService identityEventService;

    @BeforeClass
    public void setUp() throws APIResourceMgtException, IdentityEventException {

        uut = new AuthorizationDetailsTypeManagerImpl();
        authorizationDetailsTypes = TestDAOUtils.getAuthorizationDetailsTypes();

        identityEventService = mock(IdentityEventService.class);
        doNothing().when(identityEventService).handleEvent(any());
        APIResourceManagementServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        APIResourceManagementServiceComponentHolder.getInstance().setRichAuthorizationRequestsEnabled(true);

        tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        apiResource = APIResourceManagerImpl.getInstance()
                .addAPIResource(TestDAOUtils.createAPIResource("testApiResource1"), tenantDomain);
    }

    @Test
    public void shouldAddAuthorizationDetailsTypesSuccessfully() throws APIResourceMgtException {

        List<AuthorizationDetailsType> addedTypes =
                uut.addAuthorizationDetailsTypes(apiResource.getId(), authorizationDetailsTypes, tenantDomain);
        List<AuthorizationDetailsType> fetchedTypes =
                uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);

        assertNotNull(fetchedTypes);
        assertEquals(fetchedTypes.size(), 2);
        assertTrue(TestDAOUtils.isTypeExists(TEST_TYPE_1, fetchedTypes));

        assertNotNull(addedTypes);
        assertTrue(toTypesList(addedTypes).containsAll(toTypesList(fetchedTypes)));
    }

    @Test(dependsOnMethods = {"shouldAddAuthorizationDetailsTypesSuccessfully"},
            expectedExceptions = {APIResourceMgtException.class})
    public void shouldThrowExceptionWhenAddingDuplicateAuthorizationDetailsTypes() throws APIResourceMgtException {

        uut.addAuthorizationDetailsTypes(apiResource.getId(), authorizationDetailsTypes, tenantDomain);
    }

    @Test(expectedExceptions = {APIResourceMgtException.class})
    public void shouldThrowExceptionForInvalidAuthorizationDetailsTypeId() throws APIResourceMgtException {

        uut.addAuthorizationDetailsTypes(TEST_TYPE_INVALID, authorizationDetailsTypes, tenantDomain);
    }

    @Test(dependsOnMethods = {"shouldAddAuthorizationDetailsTypesSuccessfully"})
    public void shouldRetrieveAuthorizationDetails() throws APIResourceMgtException {

        List<AuthorizationDetailsType> fetchedTypes =
                uut.getAuthorizationDetailsTypes("type sw " + TEST_TYPE_1, tenantDomain);

        AuthorizationDetailsType type = TestDAOUtils.getByType(TEST_TYPE_1, fetchedTypes);
        assertNotNull(type);

        AuthorizationDetailsType fetchedTypesById =
                uut.getAuthorizationDetailsTypeByApiIdAndTypeId(apiResource.getId(), type.getId(), tenantDomain);
        assertNotNull(fetchedTypesById);
        assertEquals(fetchedTypesById.getType(), TEST_TYPE_1);

        assertTrue(uut.isAuthorizationDetailsTypeExists("type eq " + TEST_TYPE_2, tenantDomain));
        assertTrue(uut.isAuthorizationDetailsTypeExists(apiResource.getId(), TEST_TYPE_2, tenantDomain));

        assertFalse(uut.isAuthorizationDetailsTypeExists("type eq " + TEST_TYPE_INVALID, tenantDomain));
        assertFalse(uut.isAuthorizationDetailsTypeExists(apiResource.getId(), TEST_TYPE_INVALID, tenantDomain));
    }

    @Test(expectedExceptions = {APIResourceMgtException.class})
    public void shouldNotRetrieveAuthorizationDetailsByInvalidFilter() throws APIResourceMgtException {

        List<AuthorizationDetailsType> fetchedTypes = uut.getAuthorizationDetailsTypes("type eq invalid", tenantDomain);
        assertTrue(fetchedTypes.isEmpty());

        AuthorizationDetailsType fetchedTypeById =
                uut.getAuthorizationDetailsTypeByApiIdAndTypeId(TEST_TYPE_INVALID, TEST_TYPE_INVALID, tenantDomain);
        assertNull(fetchedTypeById);

        uut.getAuthorizationDetailsTypes("invalid SW TEST_TYPE", tenantDomain);
    }


    @Test(dependsOnMethods = {"shouldRetrieveAuthorizationDetails"})
    public void shouldDeleteAuthorizationDetailsTypeByApiIdAndTypeId() throws APIResourceMgtException {

        List<AuthorizationDetailsType> types =
                uut.getAuthorizationDetailsTypes("type EQ " + TEST_TYPE_2, tenantDomain);

        AuthorizationDetailsType type2 = TestDAOUtils.getByType(TEST_TYPE_2, types);
        assertNotNull(type2);

        uut.deleteAuthorizationDetailsTypeByApiIdAndTypeId(apiResource.getId(), type2.getId(), tenantDomain);
        assertNull(uut.getAuthorizationDetailsTypeByApiIdAndTypeId(apiResource.getId(), type2.getId(), tenantDomain));
    }

    @Test(dependsOnMethods = {"shouldDeleteAuthorizationDetailsTypeByApiIdAndTypeId"})
    public void shouldReplaceAuthorizationDetailsTypes() throws APIResourceMgtException {

        // should add new authorization details type 3
        final AuthorizationDetailsType type3 = new AuthorizationDetailsType();
        type3.setType(TEST_TYPE_3);

        uut.updateAuthorizationDetailsTypes(apiResource.getId(), Collections.emptyList(),
                Collections.singletonList(type3), tenantDomain);
        List<AuthorizationDetailsType> fetchedTypes =
                uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);

        assertNotNull(fetchedTypes);
        assertEquals(fetchedTypes.size(), 2);
        assertTrue(TestDAOUtils.isTypeExists(TEST_TYPE_3, fetchedTypes));

        // should only remove authorization details type 2 and 3
        uut.updateAuthorizationDetailsTypes(apiResource.getId(), Arrays.asList(TEST_TYPE_2, TEST_TYPE_3),
                Collections.emptyList(), tenantDomain);
        fetchedTypes = uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);

        assertNotNull(fetchedTypes);
        assertEquals(fetchedTypes.size(), 1);
        assertTrue(TestDAOUtils.isTypeExists(TEST_TYPE_1, fetchedTypes));
        assertFalse(TestDAOUtils.isTypeExists(TEST_TYPE_3, fetchedTypes));

        // should remove authorization details type 1
        uut.updateAuthorizationDetailsTypes(apiResource.getId(), Collections.singletonList(TEST_TYPE_1),
                Collections.emptyList(), tenantDomain);
        fetchedTypes = uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);

        assertNotNull(fetchedTypes);
        assertEquals(fetchedTypes.size(), 0);
        assertFalse(TestDAOUtils.isTypeExists(TEST_TYPE_1, fetchedTypes));
        assertFalse(TestDAOUtils.isTypeExists(TEST_TYPE_2, fetchedTypes));
        assertFalse(TestDAOUtils.isTypeExists(TEST_TYPE_3, fetchedTypes));

        // should only add authorization details type 1 and 2
        uut.updateAuthorizationDetailsTypes(apiResource.getId(), Collections.emptyList(), authorizationDetailsTypes,
                tenantDomain);
        fetchedTypes = uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);

        assertNotNull(fetchedTypes);
        assertEquals(fetchedTypes.size(), 2);
        assertTrue(TestDAOUtils.isTypeExists(TEST_TYPE_1, authorizationDetailsTypes));
        assertTrue(TestDAOUtils.isTypeExists(TEST_TYPE_2, authorizationDetailsTypes));
    }

    @Test(dependsOnMethods = {"shouldAddAuthorizationDetailsTypesSuccessfully"})
    public void shouldUpdateAuthorizationDetailsTypesSuccessfully() throws APIResourceMgtException {

        final String testValue = "test value";
        List<AuthorizationDetailsType> fetchedTypes
                = uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);
        AuthorizationDetailsType typeToUpdate = TestDAOUtils.getByType(TEST_TYPE_1, fetchedTypes);
        typeToUpdate.setName(testValue);
        typeToUpdate.setDescription(testValue);

        uut.updateAuthorizationDetailsType(apiResource.getId(), typeToUpdate, tenantDomain);
        fetchedTypes = uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);
        AuthorizationDetailsType updatedType = TestDAOUtils.getByType(TEST_TYPE_1, fetchedTypes);

        assertEquals(updatedType.getName(), testValue);
        assertEquals(updatedType.getDescription(), testValue);
        assertFalse(updatedType.getSchema().isEmpty());
    }

    @Test(priority = 3)
    public void shouldDeleteAllAuthorizationDetailsTypesByApiId() throws APIResourceMgtException {

        List<AuthorizationDetailsType> fetchedTypes =
                uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);
        AuthorizationDetailsType type1 = TestDAOUtils.getByType(TEST_TYPE_1, fetchedTypes);
        uut.deleteAuthorizationDetailsTypeByApiIdAndTypeId(apiResource.getId(), type1.getId(), tenantDomain);

        fetchedTypes = uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);

        assertNotNull(fetchedTypes);
        assertEquals(fetchedTypes.size(), 1);
        assertFalse(TestDAOUtils.isTypeExists(TEST_TYPE_1, fetchedTypes));
        assertTrue(TestDAOUtils.isTypeExists(TEST_TYPE_2, fetchedTypes));

        uut.deleteAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);
        fetchedTypes = uut.getAuthorizationDetailsTypesByApiId(apiResource.getId(), tenantDomain);
        assertNotNull(fetchedTypes);
        assertTrue(fetchedTypes.isEmpty());
    }

    private List<String> toTypesList(List<AuthorizationDetailsType> authorizationDetailsTypes) {

        return authorizationDetailsTypes.stream().map(AuthorizationDetailsType::getId).collect(Collectors.toList());
    }
}
