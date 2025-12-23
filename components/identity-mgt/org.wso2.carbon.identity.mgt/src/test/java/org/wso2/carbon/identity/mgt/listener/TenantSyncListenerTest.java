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

package org.wso2.carbon.identity.mgt.listener;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants.TenantManagement;
import org.wso2.carbon.identity.mgt.dto.TenantManagementEventDTO;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;

import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.annotations.Test;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.util.Properties;

public class TenantSyncListenerTest extends TenantSyncListener {

    private TenantInfoBean mockTenantInfo;
    private SecretResolver mockSecretResolver;

    private final String serverUrl = "https://localhost:9443";

    @BeforeMethod
    void setUp() {
        // Before each test, create a fresh mock of TenantInfoBean.
        // This ensures that tests are isolated from each other.
        mockTenantInfo = mock(TenantInfoBean.class);
        when(mockTenantInfo.getTenantId()).thenReturn(12402);
        when(mockTenantInfo.getTenantDomain()).thenReturn("wso2.com");
        when(mockTenantInfo.getAdmin()).thenReturn("kim");
        when(mockTenantInfo.getAdminPassword()).thenReturn("kim123");
        when(mockTenantInfo.getEmail()).thenReturn("kim@wso2.com");
        when(mockTenantInfo.getFirstname()).thenReturn("kim");
        when(mockTenantInfo.getLastname()).thenReturn("kim");

        mockSecretResolver = mock(SecretResolver.class);
    }

    /**
     * Test case for the tenant creation event payload.
     * Verifies that all owner details, including the username, are included.
     */
    @Test
    void testBuildPayloadForTenantCreation() {
        // Method under test
        TenantManagementEventDTO resultDto = buildPayload(mockTenantInfo, TenantManagement.ACTION_CREATE,
                TenantManagement.EVENT_CREATE_TENANT_URI, serverUrl);

        // --- Assertions ---
        assertNotNull(resultDto);
        assertEquals(resultDto.getIss(), serverUrl);
        assertNotNull(resultDto.getJti());
        assertTrue(resultDto.getIat() > 0);

        // Check the 'events' map
        TenantManagementEventDTO.EventDetail eventDetail = resultDto.getEvents()
                .get(TenantManagement.EVENT_CREATE_TENANT_URI);
        assertNotNull(eventDetail);
        assertEquals(eventDetail.getAction(), TenantManagement.ACTION_CREATE);
        assertEquals(eventDetail.getInitiatorType(), TenantManagement.EVENT_INITIATOR);

        // Check the 'tenant' object
        TenantManagementEventDTO.Tenant tenant = eventDetail.getTenant();
        assertNotNull(tenant);
        assertEquals(tenant.getId(), "12402");
        assertEquals(tenant.getDomain(), "wso2.com");
        assertEquals(tenant.getRef(), serverUrl + "/api/server/v1/tenants/12402");

        // Check the 'owners' list - crucial for CREATE event
        assertNotNull(tenant.getOwners());
        assertEquals(tenant.getOwners().size(), 1);
        TenantManagementEventDTO.Owner owner = tenant.getOwners().get(0);
        assertEquals(owner.getUsername(), "kim"); // Username MUST be present for creation
        assertEquals(owner.getPassword(), "kim123");
        assertEquals(owner.getEmail(), "kim@wso2.com");

        // Lifecycle status should not be present for creation event
        assertNull(tenant.getLifecycleStatus());

    }

    /**
     * Test case for the tenant update event payload.
     * Verifies that owner details are included, but the username is excluded.
     */
    @Test
    void testBuildPayloadForTenantUpdate() {

        // Method under test
        TenantManagementEventDTO resultDto = buildPayload(mockTenantInfo, TenantManagement.ACTION_UPDATE,
                TenantManagement.EVENT_UPDATE_TENANT_URI, serverUrl);

        // --- Assertions ---
        assertNotNull(resultDto);
        TenantManagementEventDTO.EventDetail eventDetail = resultDto.getEvents()
                .get(TenantManagement.EVENT_UPDATE_TENANT_URI);
        assertNotNull(eventDetail);
        assertEquals(eventDetail.getAction(), TenantManagement.ACTION_UPDATE);

        TenantManagementEventDTO.Tenant tenant = eventDetail.getTenant();
        assertNotNull(tenant);

        // Check the 'owners' list - crucial for UPDATE event
        assertNotNull(tenant.getOwners());
        assertEquals(tenant.getOwners().size(), 1);
        TenantManagementEventDTO.Owner owner = tenant.getOwners().get(0);
        assertNull(owner.getUsername()); // Username MUST NOT be present for update
        assertEquals(owner.getPassword(), "kim123");
        assertEquals(owner.getEmail(), "kim@wso2.com");

        // Lifecycle status should not be present for update event
        assertNull(tenant.getLifecycleStatus());
    }

    /**
     * Test case for the tenant activation event payload.
     * Verifies that the lifecycleStatus is correctly set to 'true'.
     */
    @Test
    void testBuildPayloadForTenantActivation() {

        // Set the tenant's active status to false for this test case.
        when(mockTenantInfo.isActive()).thenReturn(true);

        // Method under test
        TenantManagementEventDTO resultDto = buildPayload(mockTenantInfo, TenantManagement.ACTION_ACTIVATE,
                TenantManagement.EVENT_ACTIVATE_TENANT_URI, serverUrl);

        // --- Assertions ---
        assertNotNull(resultDto);
        TenantManagementEventDTO.EventDetail eventDetail = resultDto.getEvents()
                .get(TenantManagement.EVENT_ACTIVATE_TENANT_URI);
        assertNotNull(eventDetail);
        assertEquals(eventDetail.getAction(), TenantManagement.ACTION_ACTIVATE);

        TenantManagementEventDTO.Tenant tenant = eventDetail.getTenant();
        assertNotNull(tenant);

        // Check the 'lifecycleStatus' - crucial for ACTIVATE event
        assertNotNull(tenant.getLifecycleStatus());
        assertTrue(tenant.getLifecycleStatus().isActivated());

        // Owners list should not be present for activation event
        assertNull(tenant.getOwners());
    }

    /**
     * Test case for the tenant deactivation event payload.
     * Verifies that the lifecycleStatus is correctly set to 'false'.
     */
    @Test
    void testBuildPayloadForTenantDeactivation() {

        // Set the tenant's active status to false for this test case.
        when(mockTenantInfo.isActive()).thenReturn(false);

        // Method under test
        TenantManagementEventDTO resultDto = buildPayload(mockTenantInfo, TenantManagement.ACTION_DEACTIVATE,
                TenantManagement.EVENT_ACTIVATE_TENANT_URI, serverUrl);

        // --- Assertions ---
        assertNotNull(resultDto);
        TenantManagementEventDTO.EventDetail eventDetail = resultDto.getEvents()
                .get(TenantManagement.EVENT_ACTIVATE_TENANT_URI);
        assertNotNull(eventDetail);
        assertEquals(eventDetail.getAction(), TenantManagement.ACTION_DEACTIVATE);

        TenantManagementEventDTO.Tenant tenant = eventDetail.getTenant();
        assertNotNull(tenant);

        // Check the 'lifecycleStatus' - crucial for DEACTIVATE event
        assertNotNull(tenant.getLifecycleStatus());
        assertFalse(tenant.getLifecycleStatus().isActivated());

        // Owners list should not be present for deactivation event
        assertNull(tenant.getOwners());
    }

    @Test
    void testResolveSecrets() {

        // Create test properties with an encrypted value and a plain text value.
        Properties properties = new Properties();
        properties.put("password", "ENC(mySecretPassword)");
        properties.put("username", "admin");

        // We need to mock the static factory and utility methods.
        try (MockedStatic<SecretResolverFactory> mockedFactory = Mockito.mockStatic(SecretResolverFactory.class);
             MockedStatic<MiscellaneousUtil> mockedUtil = Mockito.mockStatic(MiscellaneousUtil.class)) {

            // Configure the SecretResolverFactory mock.
            // When SecretResolverFactory.create() is called, return our mock resolver.
            mockedFactory.when(() -> SecretResolverFactory.create(properties)).thenReturn(mockSecretResolver);

            // The resolver should report that it's initialized.
            when(mockSecretResolver.isInitialized()).thenReturn(true);

            // Configure the MiscellaneousUtil mock.
            // When MiscellaneousUtil.resolve is called with the encrypted value, return the decrypted value.
            mockedUtil.when(() -> MiscellaneousUtil.resolve("ENC(mySecretPassword)", mockSecretResolver))
                    .thenReturn("decryptedPassword123");
            // When called with a non-encrypted value, it should return the original value.
            mockedUtil.when(() -> MiscellaneousUtil.resolve("admin", mockSecretResolver))
                    .thenReturn("admin");

            // Execute the method under test.
            resolveSecrets(properties);

            // Verify that the password property was updated to the decrypted value.
            assertEquals(properties.getProperty("password"), "decryptedPassword123");
            // Verify that the other properties remain unchanged.
            assertEquals(properties.getProperty("username"), "admin");

            // Verify that the resolve method was called for all three properties.
            mockedUtil.verify(() -> MiscellaneousUtil
                    .resolve("ENC(mySecretPassword)", mockSecretResolver), times(1));
            mockedUtil.verify(() -> MiscellaneousUtil
                    .resolve("admin", mockSecretResolver), times(1));
        }
    }
}
