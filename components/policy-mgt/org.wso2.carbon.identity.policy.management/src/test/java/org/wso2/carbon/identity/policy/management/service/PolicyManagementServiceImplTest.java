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

package org.wso2.carbon.identity.policy.management.service;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.service.impl.PolicyManagementServiceImpl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PolicyManagementServiceImpl.
 */
@WithCarbonHome
@WithRealmService
public class PolicyManagementServiceImplTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = -1234;
    private static final String TEST_POLICY_NAME = "TestPolicy";
    private static final String TEST_POLICY_ID = UUID.randomUUID().toString();

    @Mock
    private PolicyManagementDAO policyManagementDAO;

    private PolicyManagementServiceImpl policyManagementService;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeClass
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        policyManagementService = PolicyManagementServiceImpl.getInstance();

        Field daoField = PolicyManagementServiceImpl.class.getDeclaredField("policyManagementDAO");
        daoField.setAccessible(true);
        daoField.set(policyManagementService, policyManagementDAO);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(TENANT_ID)).thenReturn(TENANT_DOMAIN);
    }

    @AfterClass
    public void tearDown() {

        identityTenantUtil.close();
    }

    @BeforeMethod
    public void reset() {

        org.mockito.Mockito.reset(policyManagementDAO);
    }

    @Test
    public void testAddPolicy() throws PolicyManagementException {

        Policy inputPolicy = new Policy(null, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());
        Policy savedPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.addPolicy(any(Policy.class), eq(TENANT_ID))).thenReturn(savedPolicy);
        when(policyManagementDAO.getPolicyById(any(String.class), eq(TENANT_ID))).thenReturn(savedPolicy);

        Policy result = policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getName(), TEST_POLICY_NAME);
        verify(policyManagementDAO).addPolicy(any(Policy.class), eq(TENANT_ID));
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testAddPolicy_EmptyName() throws PolicyManagementException {

        Policy inputPolicy = new Policy(null, "", TENANT_DOMAIN,
                Collections.emptyList());
        policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testAddPolicy_NullName() throws PolicyManagementException {

        Policy inputPolicy = new Policy(null, null, TENANT_DOMAIN,
                Collections.emptyList());
        policyManagementService.addPolicy(inputPolicy, TENANT_DOMAIN);
    }

    @Test
    public void testGetPolicyById() throws PolicyManagementException {

        Policy expectedPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(expectedPolicy);

        Policy result = policyManagementService.getPolicyById(TEST_POLICY_ID, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), TEST_POLICY_ID);
        Assert.assertEquals(result.getName(), TEST_POLICY_NAME);
        verify(policyManagementDAO).getPolicyById(TEST_POLICY_ID, TENANT_ID);
    }

    @Test
    public void testUpdatePolicy() throws PolicyManagementException {

        Policy existingPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());
        Policy updatedPolicy = new Policy(TEST_POLICY_ID, "UpdatedPolicy", TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID))
                .thenReturn(existingPolicy)
                .thenReturn(updatedPolicy);
        when(policyManagementDAO.updatePolicy(any(Policy.class), eq(TENANT_ID))).thenReturn(updatedPolicy);

        Policy result = policyManagementService.updatePolicy(updatedPolicy, TENANT_DOMAIN);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getName(), "UpdatedPolicy");
        verify(policyManagementDAO).updatePolicy(any(Policy.class), eq(TENANT_ID));
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testUpdatePolicy_PolicyNotFound() throws PolicyManagementException {

        Policy policy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(null);

        policyManagementService.updatePolicy(policy, TENANT_DOMAIN);
    }

    @Test
    public void testDeletePolicy() throws PolicyManagementException {

        Policy existingPolicy = new Policy(TEST_POLICY_ID, TEST_POLICY_NAME, TENANT_DOMAIN,
                Collections.emptyList());

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(existingPolicy);

        policyManagementService.deletePolicy(TEST_POLICY_ID, TENANT_DOMAIN);

        verify(policyManagementDAO).deletePolicy(TEST_POLICY_ID, TENANT_ID);
    }

    @Test
    public void testDeletePolicy_PolicyNotExists() throws PolicyManagementException {

        when(policyManagementDAO.getPolicyById(TEST_POLICY_ID, TENANT_ID)).thenReturn(null);

        policyManagementService.deletePolicy(TEST_POLICY_ID, TENANT_DOMAIN);

        verify(policyManagementDAO, org.mockito.Mockito.never()).deletePolicy(any(), eq(TENANT_ID));
    }
}
