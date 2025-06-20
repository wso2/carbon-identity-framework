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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.input.validation.mgt.test.listener;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.input.validation.mgt.internal.InputValidationDataHolder;
import org.wso2.carbon.identity.input.validation.mgt.listener.DataTypeValidationListener;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class DataTypeValidationListenerTest {

    @Mock
    private ClaimMetadataManagementService claimMetadataManagementService;
    @Mock
    private IdentityEventListenerConfig identityEventListenerConfig;

    private MockedStatic<IdentityUtil> identityUtilMockedStatic;
    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;

    private final String tenantDomain = "carbon.super";

    @BeforeClass
    public void init() {

        mockCarbonContextForTenant();

        // Initialize mocks
        claimMetadataManagementService = mock(ClaimMetadataManagementService.class);
        identityEventListenerConfig = mock(IdentityEventListenerConfig.class);
        identityUtilMockedStatic = Mockito.mockStatic(IdentityUtil.class);

        identityUtilMockedStatic.when(() -> IdentityUtil.readEventListenerProperty(
                        UserOperationEventListener.class.getName(), DataTypeValidationListener.class.getName()))
                .thenReturn(identityEventListenerConfig);
        when(identityEventListenerConfig.getEnable()).thenReturn("true");

        InputValidationDataHolder.getInstance().setClaimMetadataManagementService(claimMetadataManagementService);
    }

    @AfterClass
    public void tearDown() {

        privilegedCarbonContext.close();
        identityUtilMockedStatic.close();
    }

    @Test
    void testSetUserAttributesWithCorrectDataTypes() throws UserStoreException, ClaimMetadataException {

        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaim1 = new LocalClaim("accountNumber");
        localClaim1.setClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY, "string");
        localClaims.add(localClaim1);

        LocalClaim localClaim2 = new LocalClaim("age");
        localClaim2.setClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY, "integer");
        localClaims.add(localClaim2);

        LocalClaim localClaim3 = new LocalClaim("rate");
        localClaim3.setClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY, "12.5");
        localClaims.add(localClaim3);

        LocalClaim localClaim4 = new LocalClaim("accountType");
        localClaim4.setClaimProperty(ClaimConstants.CANONICAL_VALUES_PROPERTY,
                "[{\"label\":\"Savings\",\"value\":\"SAV\"},{\"label\":\"Current\",\"value\":\"CUR\"}]");
        localClaims.add(localClaim4);

        Map<String, String> claims = Map.of("accountNumber", "101001N", "age", "29", "rate", "12.5",
                                           "accountType", "SAV");

        when(claimMetadataManagementService.getLocalClaims(tenantDomain)).thenReturn(localClaims);

        DataTypeValidationListener dataTypeValidationListener = new DataTypeValidationListener();
        dataTypeValidationListener.doPreAddUser("user", "pass", new String[]{}, claims, "profile", null);
    }

    @Test(dataProvider = "invalidDataTypeProvider", expectedExceptions = UserStoreClientException.class)
    void testInvalidValuePassedDataType(String claimUri, String dataType, String value) throws UserStoreException,
            ClaimMetadataException {

        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaim = new LocalClaim(claimUri);
        localClaim.setClaimProperty(ClaimConstants.DATA_TYPE_PROPERTY, dataType);
        localClaims.add(localClaim);

        Map<String, String> claims = Map.of(claimUri, value);

        when(claimMetadataManagementService.getLocalClaims(tenantDomain)).thenReturn(localClaims);

        DataTypeValidationListener dataTypeValidationListener = new DataTypeValidationListener();
        dataTypeValidationListener.doPreAddUser("user", "pass", new String[]{}, claims, "profile", null);
    }

    @Test(expectedExceptions = UserStoreClientException.class)
    void testNotAllowedValuesForAttributes() throws UserStoreException, ClaimMetadataException {

        List<LocalClaim> localClaims = new ArrayList<>();
        LocalClaim localClaim = new LocalClaim("accountType");
        localClaim.setClaimProperty(ClaimConstants.CANONICAL_VALUES_PROPERTY,
                "[{\"label\":\"Savings\",\"value\":\"SAV\"},{\"label\":\"Current\",\"value\":\"CUR\"}]");
        localClaims.add(localClaim);
        Map<String, String> claims = Map.of("accountType", "FD");

        when(claimMetadataManagementService.getLocalClaims(tenantDomain)).thenReturn(localClaims);

        DataTypeValidationListener dataTypeValidationListener = new DataTypeValidationListener();
        dataTypeValidationListener.doPreAddUser("user", "pass", new String[]{}, claims, "profile", null);
    }

    @DataProvider(name = "invalidDataTypeProvider")
    public Object[][] invalidDataTypeProvider() {
        return new Object[][]{
                {"age", "integer", "Twenty Nine"},
                {"rate", "decimal", "12.5%"}
        };
    }

    private void mockCarbonContextForTenant() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);

        privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        privilegedCarbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(mockPrivilegedCarbonContext);
    }
}
