/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.permission;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests that the UI permission tree is buffered at declaration time and only written to the registry
 * when a caller actually asks for it.
 */
public class UIPermissionProvisionerTest {

    private static final String PERMISSION_A = "/permission/admin/manage/identity/applicationmgt";
    private static final String PERMISSION_B = "/permission/admin/manage/identity/claimmgt";

    private UserRegistry registry;
    private MockedStatic<UserMgtDSComponent> dsComponent;
    private MockedStatic<PrivilegedCarbonContext> carbonContext;

    @BeforeMethod
    public void setUp() throws Exception {

        UIPermissionProvisioner.reset();
        System.clearProperty(UIPermissionProvisioner.EAGER_PROVISIONING_PROPERTY);

        registry = mock(UserRegistry.class);
        when(registry.newCollection()).thenAnswer(invocation -> mock(Collection.class));

        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getGovernanceSystemRegistry()).thenReturn(registry);

        dsComponent = mockStatic(UserMgtDSComponent.class);
        dsComponent.when(UserMgtDSComponent::getRegistryService).thenReturn(registryService);

        carbonContext = mockStatic(PrivilegedCarbonContext.class);
        carbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(mock(PrivilegedCarbonContext.class));
    }

    @AfterMethod
    public void tearDown() {

        dsComponent.close();
        carbonContext.close();
        UIPermissionProvisioner.reset();
        System.clearProperty(UIPermissionProvisioner.EAGER_PROVISIONING_PROPERTY);
    }

    @Test
    public void testDeclareDoesNotTouchTheRegistry() throws Exception {

        UIPermissionProvisioner.declare(permissions());

        verifyNoInteractions(registry);
    }

    @Test
    public void testEnsureProvisionedWritesDeclaredPermissions() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(false);

        UIPermissionProvisioner.declare(permissions());
        UIPermissionProvisioner.ensureProvisioned();

        verify(registry).put(eq(PERMISSION_A), any(Collection.class));
        verify(registry).put(eq(PERMISSION_B), any(Collection.class));
    }

    @Test
    public void testEnsureProvisionedRunsOnlyOnce() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(false);

        UIPermissionProvisioner.declare(permissions());
        UIPermissionProvisioner.ensureProvisioned();
        UIPermissionProvisioner.ensureProvisioned();
        UIPermissionProvisioner.ensureProvisioned();

        verify(registry, times(1)).put(eq(PERMISSION_A), any(Collection.class));
    }

    @Test
    public void testExistingPermissionWithDisplayNameIsLeftAlone() throws Exception {

        Resource existing = mock(Resource.class);
        when(existing.getProperty(UserMgtConstants.DISPLAY_NAME)).thenReturn("Application Management");
        when(registry.resourceExists(PERMISSION_A)).thenReturn(true);
        when(registry.get(PERMISSION_A)).thenReturn(existing);

        Map<String, String> single = new LinkedHashMap<>();
        single.put(PERMISSION_A, "Application Management");
        UIPermissionProvisioner.declare(single);
        UIPermissionProvisioner.ensureProvisioned();

        verify(registry, never()).put(eq(PERMISSION_A), any(Collection.class));
        verify(registry, never()).put(eq(PERMISSION_A), any(Resource.class));
    }

    @Test
    public void testExistingPermissionWithoutDisplayNameIsBackfilled() throws Exception {

        Resource existing = mock(Resource.class);
        when(existing.getProperty(UserMgtConstants.DISPLAY_NAME)).thenReturn(null);
        when(registry.resourceExists(PERMISSION_A)).thenReturn(true);
        when(registry.get(PERMISSION_A)).thenReturn(existing);

        Map<String, String> single = new LinkedHashMap<>();
        single.put(PERMISSION_A, "Application Management");
        UIPermissionProvisioner.declare(single);
        UIPermissionProvisioner.ensureProvisioned();

        verify(existing).setProperty(UserMgtConstants.DISPLAY_NAME, "Application Management");
        verify(registry).put(PERMISSION_A, existing);
    }

    @Test
    public void testFirstDeclarationOfAPathWins() throws Exception {

        when(registry.resourceExists(anyString())).thenReturn(false);

        Map<String, String> first = new LinkedHashMap<>();
        first.put(PERMISSION_A, "Original");
        Map<String, String> second = new LinkedHashMap<>();
        second.put(PERMISSION_A, "Replacement");

        UIPermissionProvisioner.declare(first);
        UIPermissionProvisioner.declare(second);
        UIPermissionProvisioner.ensureProvisioned();

        // The collection is created once, carrying the display name from the first declaration.
        verify(registry, times(1)).put(eq(PERMISSION_A), any(Collection.class));
    }

    @Test
    public void testEagerProvisioningWritesAtDeclarationTime() throws Exception {

        System.setProperty(UIPermissionProvisioner.EAGER_PROVISIONING_PROPERTY, "true");
        when(registry.resourceExists(anyString())).thenReturn(false);
        assertTrue(UIPermissionProvisioner.isEagerProvisioningEnabled());

        UIPermissionProvisioner.declare(permissions());

        verify(registry).put(eq(PERMISSION_A), any(Collection.class));
        verify(registry).put(eq(PERMISSION_B), any(Collection.class));
    }

    @Test
    public void testEagerProvisioningDoesNotRewriteOnFirstUse() throws Exception {

        System.setProperty(UIPermissionProvisioner.EAGER_PROVISIONING_PROPERTY, "true");
        when(registry.resourceExists(anyString())).thenReturn(false);

        UIPermissionProvisioner.declare(permissions());
        UIPermissionProvisioner.ensureProvisioned();

        // Already written as it was declared, so the lazy flush must not repeat the work.
        verify(registry, times(1)).put(eq(PERMISSION_A), any(Collection.class));
    }

    @Test
    public void testLazyProvisioningIsTheDefault() {

        assertFalse(UIPermissionProvisioner.isEagerProvisioningEnabled());
    }

    private Map<String, String> permissions() {

        Map<String, String> permissions = new LinkedHashMap<>();
        permissions.put(PERMISSION_A, "Application Management");
        permissions.put(PERMISSION_B, "Claim Management");
        return permissions;
    }
}
