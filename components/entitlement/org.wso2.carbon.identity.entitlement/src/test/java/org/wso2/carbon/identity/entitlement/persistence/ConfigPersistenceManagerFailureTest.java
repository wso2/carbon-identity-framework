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

package org.wso2.carbon.identity.entitlement.persistence;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.persistence.cache.CacheBackedConfigDAO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.DENY_OVERRIDES;
import static org.wso2.carbon.identity.entitlement.PDPConstants.Algorithms.PERMIT_OVERRIDES;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

/**
 * This class tests the failure scenarios of Database or Registry in ConfigPersistenceManager implementations.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
public class ConfigPersistenceManagerFailureTest {

    @Mock
    private CacheBackedConfigDAO mockedConfigDAO;

    @Mock
    private Registry mockedRegistry;

    @Mock
    private Collection mockedCollection;

    MockedStatic<EntitlementServiceComponent> entitlementServiceComponent;

    private JDBCConfigPersistenceManager jdbcConfigPersistenceManager;
    private RegistryConfigPersistenceManager registryConfigPersistenceManager;
    private HybridConfigPersistenceManager hybridConfigPersistenceManager;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        jdbcConfigPersistenceManager = new JDBCConfigPersistenceManager();
        setPrivateStaticFinalField(JDBCConfigPersistenceManager.class, "configDAO", mockedConfigDAO);

        entitlementServiceComponent = mockStatic(EntitlementServiceComponent.class);
        entitlementServiceComponent.when(() -> EntitlementServiceComponent.getGovernanceRegistry(anyInt()))
                .thenReturn(mockedRegistry);
        registryConfigPersistenceManager = new RegistryConfigPersistenceManager();

        hybridConfigPersistenceManager = new HybridConfigPersistenceManager();
        setPrivateStaticFinalField(HybridConfigPersistenceManager.class, "configDAO", mockedConfigDAO);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        entitlementServiceComponent.close();
        setPrivateStaticFinalField(JDBCConfigPersistenceManager.class, "configDAO",
                CacheBackedConfigDAO.getInstance());
        setPrivateStaticFinalField(HybridConfigPersistenceManager.class, "configDAO",
                CacheBackedConfigDAO.getInstance());
    }

    @Test
    public void testGetAlgorithmWhenDatabaseErrorHappened() throws Exception {

        when(mockedConfigDAO.getPolicyCombiningAlgorithm(anyInt())).thenThrow(new EntitlementException(""));
        String globalPolicyAlgorithmName = jdbcConfigPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(globalPolicyAlgorithmName, DENY_OVERRIDES);
    }

    @Test
    public void testAddAlgorithmWhenResourceCheckFailed() throws Exception {

        when(mockedConfigDAO.getPolicyCombiningAlgorithm(anyInt())).thenThrow(new EntitlementException(""));
        jdbcConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(PERMIT_OVERRIDES);
        verify(mockedConfigDAO, never()).updatePolicyCombiningAlgorithm(anyString(), anyInt());
        verify(mockedConfigDAO, times(1)).insertPolicyCombiningAlgorithm(PERMIT_OVERRIDES, SUPER_TENANT_ID);
    }

    @Test
    public void testAddAlgorithmWhenDatabaseErrorHappened() throws Exception {

        when(mockedConfigDAO.getPolicyCombiningAlgorithm(anyInt())).thenReturn(null);
        doThrow(new EntitlementException("")).when(mockedConfigDAO)
                .insertPolicyCombiningAlgorithm(anyString(), anyInt());
        assertThrows(EntitlementException.class,
                () -> jdbcConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(PERMIT_OVERRIDES));
    }

    @Test
    public void testGetAlgorithmWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        String actualAlgorithm = registryConfigPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(actualAlgorithm, DENY_OVERRIDES);
    }

    @Test
    public void testAddAlgorithmWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registryConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(PERMIT_OVERRIDES));
    }

    @Test
    public void testDeleteAlgorithmWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        doThrow(new RegistryException("")).when(mockedRegistry).delete(anyString());
        assertThrows(EntitlementException.class, () -> registryConfigPersistenceManager.deleteGlobalPolicyAlgorithm());
    }

    @Test
    public void testGetAlgorithmViaHybridManagerWhenDatabaseErrorHappened() throws Exception {

        when(mockedConfigDAO.getPolicyCombiningAlgorithm(anyInt())).thenThrow(new EntitlementException(""));
        when(mockedRegistry.resourceExists(anyString())).thenReturn(false);
        String globalPolicyAlgorithmName = hybridConfigPersistenceManager.getGlobalPolicyAlgorithmName();
        assertEquals(globalPolicyAlgorithmName, DENY_OVERRIDES);
    }

    @Test
    public void testAddAlgorithmViaHybridManagerWhenRegistryResourceDeletionFailed() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        when(mockedRegistry.get(anyString())).thenReturn(mockedCollection);
        when(mockedCollection.getProperty(anyString())).thenReturn(DENY_OVERRIDES);
        doThrow(new RegistryException("")).when(mockedRegistry).delete(anyString());
        hybridConfigPersistenceManager.addOrUpdateGlobalPolicyAlgorithm(PERMIT_OVERRIDES);
    }

    private static void setPrivateStaticFinalField(Class<?> clazz, String fieldName, Object newValue)
            throws ReflectiveOperationException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
