/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.CacheBackedExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.framework.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.framework.testutil.Whitebox.setInternalState;

@PrepareForTest({IdentityTenantUtil.class, ClaimMetadataEventPublisherProxy.class})
@PowerMockIgnore("jdk.internal.reflect.*")
@WithCarbonHome
public class ClaimMetadataManagementServiceImplTest extends PowerMockTestCase {

    private static final String EXTERNAL_CLAIM_DIALECT_URI = "https://wso2.org";
    private static final String EXTERNAL_CLAIM_URI = "test";
    private static final String MAPPED_LOCAL_CLAIM_URI = "http://wso2.org/claims/test";

    private final ExternalClaim externalClaim = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, EXTERNAL_CLAIM_URI,
            MAPPED_LOCAL_CLAIM_URI);

    private ClaimMetadataManagementService service;

    @BeforeMethod
    public void setup() {

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(anyString())).thenReturn(SUPER_TENANT_ID);

        mockStatic(ClaimMetadataEventPublisherProxy.class);
        when(ClaimMetadataEventPublisherProxy.getInstance()).thenReturn(mock(ClaimMetadataEventPublisherProxy.class));

        service = new ClaimMetadataManagementServiceImpl();
    }

    @Test
    public void testAddExternalClaim() throws Exception {

        CacheBackedExternalClaimDAO externalClaimDAO = Mockito.mock(CacheBackedExternalClaimDAO.class);
        when(externalClaimDAO.getExternalClaims(anyString(), anyInt())).thenReturn(new ArrayList<>());
        setInternalState(service, "externalClaimDAO", externalClaimDAO);

        service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
        verify(externalClaimDAO, times(1)).addExternalClaim(any(), anyInt());
    }

    @Test(expectedExceptions = ClaimMetadataException.class)
    public void testAddExistingExternalClaim() throws Exception {

        CacheBackedExternalClaimDAO externalClaimDAO = Mockito.mock(CacheBackedExternalClaimDAO.class);
        when(externalClaimDAO.getExternalClaims(anyString(), anyInt()))
                .thenReturn(Collections.singletonList(externalClaim));
        setInternalState(service, "externalClaimDAO", externalClaimDAO);

        service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
    }

    @Test(expectedExceptions = ClaimMetadataException.class)
    public void testAddExtClaimWithExistingLocalClaimMapping() throws Exception {

        CacheBackedExternalClaimDAO externalClaimDAO = Mockito.mock(CacheBackedExternalClaimDAO.class);
        when(externalClaimDAO.getExternalClaims(anyString(), anyInt()))
                .thenReturn(Collections.singletonList(externalClaim));
        when(externalClaimDAO.isLocalClaimMappedWithinDialect(MAPPED_LOCAL_CLAIM_URI, EXTERNAL_CLAIM_DIALECT_URI,
                SUPER_TENANT_ID)).thenReturn(Boolean.TRUE);
        setInternalState(service, "externalClaimDAO", externalClaimDAO);

        service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
    }

}
