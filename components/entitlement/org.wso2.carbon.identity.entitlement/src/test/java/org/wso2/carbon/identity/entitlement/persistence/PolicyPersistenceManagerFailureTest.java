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
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.persistence.cache.CacheBackedPolicyDAO;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;

/**
 * This class tests the failure scenarios of Database or Registry in Registry Policy Persistence Manager implementation.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
public class PolicyPersistenceManagerFailureTest {

    static final String SAMPLE_POLICY_STRING_1 =
            "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"  PolicyId=\"sample_policy1\" RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\" Version=\"1.0\"><Target><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">GET</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">resourceA</AttributeValue><AttributeDesignator AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Match></AllOf></AnyOf></Target><Rule Effect=\"Permit\" RuleId=\"rule1\"><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><AttributeDesignator AttributeId=\"http://wso2.org/claims/country\" Category=\"http://wso2.org/identity/user\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">Sri Lanka</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-is-in\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">Engineer</AttributeValue><AttributeDesignator AttributeId=\"http://wso2.org/claims/role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"></AttributeDesignator></Apply></Apply></Condition></Rule><Rule Effect=\"Deny\" RuleId=\"rule2\"></Rule></Policy>";
    static final String SAMPLE_POLICY_ID_1 = "sample_policy1";

    PolicyDTO samplePAPPolicy1;
    PolicyStoreDTO samplePDPPolicy1;

    @Mock
    private CacheBackedPolicyDAO mockedPolicyDAO;

    @Mock
    private Registry mockedRegistry;

    MockedStatic<EntitlementServiceComponent> entitlementServiceComponent;
    private RegistryPolicyPersistenceManager registryPolicyPersistenceManager;
    private JDBCPolicyPersistenceManager jdbcPolicyPersistenceManager;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        Properties engineProperties = new Properties();
        engineProperties.put(PDPConstants.MAX_NO_OF_POLICY_VERSIONS, "4");

        EntitlementConfigHolder mockEntitlementConfigHolder = mock(EntitlementConfigHolder.class);
        when(mockEntitlementConfigHolder.getEngineProperties()).thenReturn(engineProperties);

        entitlementServiceComponent = mockStatic(EntitlementServiceComponent.class);
        entitlementServiceComponent.when(EntitlementServiceComponent::getEntitlementConfig).
                thenReturn(mockEntitlementConfigHolder);
        entitlementServiceComponent.when(() -> EntitlementServiceComponent.getGovernanceRegistry(anyInt()))
                .thenReturn(mockedRegistry);

        Properties storeProps = new Properties();
        registryPolicyPersistenceManager = new RegistryPolicyPersistenceManager();
        storeProps.setProperty("policyStorePath", "/repository/identity/entitlement/policy/pdp/");
        registryPolicyPersistenceManager.init(storeProps);
        jdbcPolicyPersistenceManager = new JDBCPolicyPersistenceManager();
        setPrivateStaticFinalField(JDBCPolicyPersistenceManager.class, "policyDAO", mockedPolicyDAO);

        samplePAPPolicy1 = new PolicyDTO(SAMPLE_POLICY_ID_1);
        samplePAPPolicy1.setPolicy(SAMPLE_POLICY_STRING_1);
        samplePDPPolicy1 = getPDPPolicy(SAMPLE_POLICY_ID_1, SAMPLE_POLICY_STRING_1, "1", true, true, 0, false);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        entitlementServiceComponent.close();
        registryPolicyPersistenceManager = null;
        setPrivateStaticFinalField(JDBCPolicyPersistenceManager.class, "policyDAO",
                CacheBackedPolicyDAO.getInstance());
    }

    @Test
    public void testAddOrUpdatePolicyWhenDatabaseErrorHappened() throws Exception {

        doThrow(new EntitlementException("")).when(mockedPolicyDAO).insertPolicy(any(), anyInt());
        assertThrows(EntitlementException.class,
                () -> jdbcPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true));
    }

    @Test
    public void testGetPAPPolicyWhenDatabaseErrorHappened() throws Exception {

        when(mockedPolicyDAO.getPAPPolicy(anyString(), anyInt())).thenThrow(new EntitlementException(""));
        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        assertThrows(EntitlementException.class,
                () -> jdbcPolicyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test
    public void testGetActivePoliciesWhenDatabaseErrorHappened() throws Exception {

        when(mockedPolicyDAO.getAllPDPPolicies(anyInt())).thenThrow(new EntitlementException(""));
        String[] activePolicies = jdbcPolicyPersistenceManager.getActivePolicies();
        assertEquals(activePolicies.length, 0);
    }

    @Test
    public void testGetOrderedPolicyIdentifiersWhenDatabaseErrorHappened() throws Exception {

        when(mockedPolicyDAO.getAllPDPPolicies(anyInt())).thenThrow(new EntitlementException(""));
        String[] orderedPolicies = jdbcPolicyPersistenceManager.getOrderedPolicyIdentifiers();
        assertEquals(orderedPolicies.length, 0);
    }

    @Test
    public void testGetPolicyIdentifiersWhenDatabaseErrorHappened() throws Exception {

        when(mockedPolicyDAO.getPublishedPolicyIds(anyInt())).thenThrow(new EntitlementException(""));
        assertNull(jdbcPolicyPersistenceManager.getPolicyIdentifiers());
    }

    @Test
    public void testGetSearchAttributesWhenDatabaseErrorHappened() throws Exception {

        when(mockedPolicyDAO.getAllPDPPolicies(anyInt())).thenThrow(new EntitlementException(""));
        Map<String, Set<AttributeDTO>> attributes = jdbcPolicyPersistenceManager.getSearchAttributes(null, null);
        assertEquals(attributes.size(), 0);
    }

    @Test
    public void testRemovePolicyWhenDatabaseErrorHappened() throws Exception {

        doThrow(new EntitlementException("")).when(mockedPolicyDAO).deletePAPPolicy(anyString(), anyInt());
        assertThrows(EntitlementException.class,
                () -> jdbcPolicyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test
    public void testAddPdPPolicyWhenDatabaseErrorHappened() throws Exception {

        doThrow(new EntitlementException("")).when(mockedPolicyDAO).insertOrUpdatePolicy(any(), anyInt());
        assertThrows(EntitlementException.class,
                () -> jdbcPolicyPersistenceManager.addPolicy(samplePDPPolicy1));
    }

    @Test
    public void testAddOrUpdatePolicyWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.get(anyString())).thenThrow(new ResourceNotFoundException(""));
        when(mockedRegistry.newCollection()).thenReturn(new CollectionImpl());
        when(mockedRegistry.put(anyString(), any())).thenThrow(new RegistryException(""));
        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, true));
        assertThrows(EntitlementException.class,
                () -> registryPolicyPersistenceManager.addOrUpdatePolicy(samplePAPPolicy1, false));
    }

    @Test
    public void testGetPAPPolicyWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.get(anyString())).thenThrow(new RegistryException(""));
        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        assertThrows(EntitlementException.class,
                () -> registryPolicyPersistenceManager.getPAPPolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test
    public void testGetVersionsWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.get(anyString())).thenThrow(new RegistryException(""));
        String[] versions = registryPolicyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(versions.length, 0);
    }

    @Test
    public void testGetVersionsWhenRegistryErrorHappenedDueToResourceNotFound() throws Exception {

        when(mockedRegistry.get(anyString())).thenThrow(new ResourceNotFoundException(""));
        String[] versions = registryPolicyPersistenceManager.getVersions(samplePAPPolicy1.getPolicyId());
        assertEquals(versions.length, 0);
    }

    @Test
    public void testGetActivePoliciesWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        String[] activePolicies = registryPolicyPersistenceManager.getActivePolicies();
        assertEquals(activePolicies.length, 0);
    }

    @Test
    public void testGetActivePoliciesWhenResourceNotExists() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenReturn(false);
        String[] activePolicies = registryPolicyPersistenceManager.getActivePolicies();
        assertEquals(activePolicies.length, 0);
    }

    @Test
    public void testGetOrderedPolicyIdentifiersWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        String[] orderedPolicies = registryPolicyPersistenceManager.getOrderedPolicyIdentifiers();
        assertEquals(orderedPolicies.length, 0);
    }

    @Test
    public void testGetPolicyIdentifiersWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertNull(registryPolicyPersistenceManager.getPolicyIdentifiers());
    }

    @Test
    public void testGetPolicyIdentifiersWhenResourceNotExists() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenReturn(false);
        String[] policyIds = registryPolicyPersistenceManager.getPolicyIdentifiers();
        assertEquals(policyIds.length, 0);
    }

    @Test
    public void testGetSearchAttributesWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        Map<String, Set<AttributeDTO>> attributes = registryPolicyPersistenceManager.getSearchAttributes(null, null);
        assertEquals(attributes.size(), 0);
    }

    @Test
    public void testIsPolicyExistsInPAPPWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertFalse(registryPolicyPersistenceManager.isPolicyExistsInPap(samplePAPPolicy1.getPolicyId()));
    }

    @Test
    public void testRemovePolicyWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registryPolicyPersistenceManager.removePolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test
    public void testAddPdPPolicyWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertThrows(EntitlementException.class,
                () -> registryPolicyPersistenceManager.addPolicy(samplePDPPolicy1));
    }

    @Test
    public void testIsPolicyExistsInPDPWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.resourceExists(anyString())).thenThrow(new RegistryException(""));
        assertFalse(registryPolicyPersistenceManager.isPolicyExist(samplePAPPolicy1.getPolicyId()));
    }

    @Test
    public void testDeletePolicyFromPDPWhenRegistryErrorHappened() throws Exception {

        doThrow(new RegistryException("")).when(mockedRegistry).delete(anyString());
        assertFalse(registryPolicyPersistenceManager.deletePolicy(samplePAPPolicy1.getPolicyId()));
    }

    @Test
    public void testGetPublishedPolicyWhenRegistryErrorHappened() throws Exception {

        when(mockedRegistry.get(anyString())).thenThrow(new RegistryException(""));
        when(mockedRegistry.resourceExists(anyString())).thenReturn(true);
        assertNull(registryPolicyPersistenceManager.getPolicy(samplePAPPolicy1.getPolicyId()));
    }

    private PolicyStoreDTO getPDPPolicy(String id, String policy, String version, boolean active, boolean setActive,
                                        int order, boolean setOrder) {

        PolicyStoreDTO policyStoreDTO = new PolicyStoreDTO();
        if (id != null) {
            policyStoreDTO.setPolicyId(id);
        }
        if (policy != null) {
            policyStoreDTO.setPolicy(policy);
        }
        if (version != null) {
            policyStoreDTO.setVersion(version);
        }
        policyStoreDTO.setActive(active);
        policyStoreDTO.setSetActive(setActive);
        if (order != 0) {
            policyStoreDTO.setPolicyOrder(order);
        }
        policyStoreDTO.setSetOrder(setOrder);
        return policyStoreDTO;
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
