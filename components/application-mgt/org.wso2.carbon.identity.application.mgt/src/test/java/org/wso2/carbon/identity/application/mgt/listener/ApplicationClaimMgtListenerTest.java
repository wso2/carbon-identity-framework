/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class ApplicationClaimMgtListenerTest {

    private ApplicationClaimMgtListener applicationClaimMgtListener;
    private ApplicationDAOImpl applicationDAO;

    @BeforeMethod
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        applicationDAO = mock(ApplicationDAOImpl.class);
        applicationClaimMgtListener = new ApplicationClaimMgtListener();

        Field applicationDAOField = ApplicationClaimMgtListener.class.getDeclaredField("applicationDAO");
        applicationDAOField.setAccessible(true);
        applicationDAOField.set(null, applicationDAO);
    }

    @Test
    public void testDoPreUpdateLocalClaimWhenSupportedByDefault()
            throws ClaimMetadataException, IdentityApplicationManagementException {

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimProperty(anyString())).thenReturn("true");

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId("testTenant")).thenReturn(1);

            boolean result = applicationClaimMgtListener.doPreUpdateLocalClaim(localClaim, SUPER_TENANT_DOMAIN_NAME);
            Assert.assertTrue(result, "Expected the method to return true when supported by default.");
            verify(applicationDAO, never()).isClaimRequestedByAnySp(any(), anyString(), anyInt());
        }
    }

    @Test
    public void testDoPreUpdateLocalClaimWhenClaimIsNotRequested() throws Exception {

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimProperty(anyString())).thenReturn("false");
        when(localClaim.getClaimURI()).thenReturn("testClaimURI");

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId("testTenant")).thenReturn(1);
            when(applicationDAO.isClaimRequestedByAnySp(null, "testClaimURI", 1)).thenReturn(false);

            boolean result = applicationClaimMgtListener.doPreUpdateLocalClaim(localClaim, "testTenant");
            Assert.assertTrue(result, "Expected the method to return true when the claim is not requested.");
        }
    }

    @Test(expectedExceptions = ClaimMetadataClientException.class)
    public void testDoPreUpdateLocalClaimWhenClaimIsRequested() throws Exception {

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimProperty(anyString())).thenReturn("false");
        when(localClaim.getClaimURI()).thenReturn("testClaimURI");

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId("testTenant")).thenReturn(1);
            when(applicationDAO.isClaimRequestedByAnySp(null, "testClaimURI", 1)).thenReturn(true);

            applicationClaimMgtListener.doPreUpdateLocalClaim(localClaim, "testTenant");
        }
    }

    @Test(expectedExceptions = ClaimMetadataException.class)
    public void testDoPreUpdateLocalClaim_ExceptionThrown() throws Exception {

        LocalClaim localClaim = mock(LocalClaim.class);
        when(localClaim.getClaimProperty(anyString())).thenReturn("false");
        when(localClaim.getClaimURI()).thenReturn("testClaimURI");

        try (MockedStatic<IdentityTenantUtil> mockedTenantUtil = mockStatic(IdentityTenantUtil.class)) {
            mockedTenantUtil.when(() -> IdentityTenantUtil.getTenantId("testTenant")).thenReturn(1);
            when(applicationDAO.isClaimRequestedByAnySp(null, "testClaimURI", 1))
                    .thenThrow(new IdentityApplicationManagementException("Test Exception"));

            applicationClaimMgtListener.doPreUpdateLocalClaim(localClaim, "testTenant");
        }
    }
}