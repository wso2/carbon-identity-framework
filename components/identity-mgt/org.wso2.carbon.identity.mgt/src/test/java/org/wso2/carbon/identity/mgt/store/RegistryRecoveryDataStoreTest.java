/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.store;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.io.File;
import java.net.URL;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the old confirmation code cleanup in {@link RegistryRecoveryDataStore}.
 *
 * <p>Stored codes are named {@code sequence___username___code}. The cleanup has to identify the username segment of
 * each resource, including for usernames that carry the delimiter themselves, otherwise the old codes of those users
 * are never invalidated.</p>
 */
public class RegistryRecoveryDataStoreTest {

    private static final int TENANT_ID = -1234;
    private static final String CODE_PATH = IdentityMgtConstants.IDENTITY_MANAGEMENT_DATA;
    private static final String CODE = "11111111-2222-3333-4444-555555555555";

    private MockedStatic<IdentityMgtConfig> identityMgtConfig;
    private MockedStatic<IdentityMgtServiceComponent> identityMgtServiceComponent;

    private UserRegistry registry;
    private Collection collection;
    private RegistryRecoveryDataStore dataStore;

    /**
     * Wires a recovery data store over a mocked registry, with registry indexing and username hashing switched off so
     * the cleanup iterates over the resource collection.
     */
    @BeforeMethod
    public void setUp() throws Exception {

        URL root = this.getClass().getClassLoader().getResource(".");
        System.setProperty("carbon.home", new File(root.getPath()).getAbsolutePath());

        IdentityMgtConfig config = mock(IdentityMgtConfig.class);
        when(config.getPoolSize()).thenReturn(0);
        when(config.getProperty(anyString())).thenReturn(null);
        identityMgtConfig = mockStatic(IdentityMgtConfig.class);
        identityMgtConfig.when(IdentityMgtConfig::getInstance).thenReturn(config);

        registry = mock(UserRegistry.class);
        collection = mock(Collection.class);
        when(registry.get(CODE_PATH.toLowerCase())).thenReturn(collection);
        when(registry.resourceExists(anyString())).thenReturn(true);

        RegistryService registryService = mock(RegistryService.class);
        when(registryService.getConfigSystemRegistry(anyInt())).thenReturn(registry);
        identityMgtServiceComponent = mockStatic(IdentityMgtServiceComponent.class);
        identityMgtServiceComponent.when(IdentityMgtServiceComponent::getRegistryService).thenReturn(registryService);


        dataStore = new RegistryRecoveryDataStore();
    }

    /**
     * Releases the static mocks used to stand in for the server runtime.
     */
    @AfterMethod
    public void tearDown() {

        identityMgtServiceComponent.close();
        identityMgtConfig.close();
        try {
            PrivilegedCarbonContext.endTenantFlow();
        } catch (Exception e) {
            // Ignore if no tenant flow was started.
        }
    }

    @DataProvider(name = "usernames")
    public Object[][] usernames() {

        return new Object[][]{
                {"normaluser"},
                {"test___user"},
                {"edge_user_"},
                {"a___b___c"},
        };
    }

    @Test(dataProvider = "usernames")
    public void testOwnCodeIsDeleted(String username) throws Exception {

        String ownResource = CODE_PATH + "/2___" + username + "___" + CODE;
        String otherResource = CODE_PATH + "/2___someoneelse___" + CODE;
        when(collection.getChildren()).thenReturn(new String[]{ownResource, otherResource});

        invalidate(username);

        verify(registry).delete(ownResource);
        verify(registry, never()).delete(otherResource);
    }

    @Test
    public void testSecondaryUserStoreCollectionIsTraversed() throws Exception {

        String secondaryCollection = CODE_PATH + "/2___secondary";
        // The nested collection resolves to nothing, which ends the traversal.
        when(collection.getChildren()).thenReturn(new String[]{secondaryCollection});
        when(registry.get(secondaryCollection.toLowerCase())).thenReturn(null);

        invalidate("secondary/someuser");

        verify(registry).get(secondaryCollection.toLowerCase());
        verify(registry, never()).delete(anyString());
    }

    private void invalidate(String username) throws Exception {

        dataStore.invalidate(username, TENANT_ID);
    }
}
