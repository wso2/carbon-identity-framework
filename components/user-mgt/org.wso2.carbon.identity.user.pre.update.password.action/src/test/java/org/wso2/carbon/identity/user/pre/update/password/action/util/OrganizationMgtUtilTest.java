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

package org.wso2.carbon.identity.user.pre.update.password.action.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.util.OrganizationMgtUtil;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TENANT_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TENANT_UUID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ORG_DEPTH;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ORG_HANDLE;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ORG_ID;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TEST_ORG_NAME;

/**
 * Unit tests for OrganizationMgtUtil class.
 */
public class OrganizationMgtUtilTest {

    @Mock
    private OrganizationManager organizationManager;
    private MockedStatic<OrganizationManagementUtil> organizationManagementUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private BasicOrganization basicOrganization;

    @BeforeClass
    public void init() {

        basicOrganization = new BasicOrganization();
        basicOrganization.setId(TEST_ORG_ID);
        basicOrganization.setName(TEST_ORG_NAME);
        basicOrganization.setOrganizationHandle(TEST_ORG_HANDLE);
    }

    @BeforeMethod
    public void setUp() {

        organizationManager = mock(OrganizationManager.class);
        PreUpdatePasswordActionServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(TENANT_DOMAIN);

        organizationManagementUtil = mockStatic(OrganizationManagementUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
    }

    @AfterMethod
    public void tearDown() {

        PrivilegedCarbonContext.endTenantFlow();
        organizationManagementUtil.close();
        identityTenantUtil.close();
    }

    @Test
    public void testIsOrganization() throws Exception {

        organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_ID))
                .thenReturn(true);
        Assert.assertTrue(OrganizationMgtUtil.isOrganization());
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while checking if the tenant is an organization.")
    public void testIsOrganizationWithError() throws Exception {

        organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyInt()))
                .thenThrow(new OrganizationManagementException("Error checking organization type."));
        OrganizationMgtUtil.isOrganization();
    }

    @Test
    public void testGetOrganization() throws Exception {

        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(TEST_ORG_ID);
        when(organizationManager.getBasicOrganizationDetailsByOrgIDs(any()))
                .thenReturn(Collections.singletonMap(TEST_ORG_ID, basicOrganization));
        when(organizationManager.getOrganizationDepthInHierarchy(TEST_ORG_ID)).thenReturn(TEST_ORG_DEPTH);

        Organization result = OrganizationMgtUtil.getOrganization();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), TEST_ORG_ID);
        Assert.assertEquals(result.getName(), TEST_ORG_NAME);
        Assert.assertEquals(result.getOrgHandle(), TEST_ORG_HANDLE);
        Assert.assertEquals(result.getDepth(), TEST_ORG_DEPTH);
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while retrieving organization information.")
    public void testGetOrganizationWithError() throws Exception {

        when(organizationManager.resolveOrganizationId(anyString())).thenReturn(TEST_ORG_ID);
        when(organizationManager.getBasicOrganizationDetailsByOrgIDs(any()))
                .thenReturn(Collections.emptyMap());
        when(organizationManager.getOrganizationDepthInHierarchy(TEST_ORG_ID)).thenReturn(TEST_ORG_DEPTH);

        OrganizationMgtUtil.getOrganization();
    }

    @Test
    public void testResolveTenant() throws Exception {

        when(organizationManager.getPrimaryOrganizationId(TEST_ORG_ID)).thenReturn(TENANT_UUID);
        when(organizationManager.resolveTenantDomain(TENANT_UUID)).thenReturn(TENANT_DOMAIN);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        Tenant result = OrganizationMgtUtil.resolveTenant(TEST_ORG_ID);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), String.valueOf(TENANT_ID));
        Assert.assertEquals(result.getName(), TENANT_DOMAIN);
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while resolving tenant for organization ID: " + TEST_ORG_ID)
    public void testResolveTenantWithError() throws Exception {

        when(organizationManager.getPrimaryOrganizationId(TEST_ORG_ID)).thenReturn(TENANT_UUID);
        when(organizationManager.resolveTenantDomain(TENANT_UUID)).thenThrow(new OrganizationManagementException(
                "Error resolving tenant domain."));

        OrganizationMgtUtil.resolveTenant(TEST_ORG_ID);
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class,
            expectedExceptionsMessageRegExp = "Error while resolving tenant for organization ID: " + TEST_ORG_ID)
    public void testResolveTenantWithErrorResolvingTenantId() throws Exception {

        when(organizationManager.getPrimaryOrganizationId(TEST_ORG_ID)).thenReturn(TENANT_UUID);
        when(organizationManager.resolveTenantDomain(TENANT_UUID)).thenReturn(TENANT_DOMAIN);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN))
                .thenThrow(new IdentityRuntimeException("Error getting tenant ID."));

        OrganizationMgtUtil.resolveTenant(TEST_ORG_ID);
    }
}
