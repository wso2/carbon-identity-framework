/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
 *
 */

package org.wso2.carbon.identity.client.attestation.mgt;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.ClientAttestationMetaData;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.internal.ClientAttestationMgtDataHolder;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationService;
import org.wso2.carbon.identity.client.attestation.mgt.services.ClientAttestationServiceImpl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Testing the ClientAttestationServiceImpl class
 */
@PrepareForTest({ ClientAttestationServiceImpl.class, ClientAttestationMgtDataHolder.class,
        ApplicationManagementService.class})
public class ClientAttestationServiceImplTest extends PowerMockTestCase {

    private ClientAttestationService clientAttestationService;
    private ApplicationManagementService applicationManagementService;

    @BeforeMethod
    public void setup() {

        clientAttestationService = new ClientAttestationServiceImpl();

        ClientAttestationMgtDataHolder clientAttestationMgtDataHolder = mock(ClientAttestationMgtDataHolder.class);
        mockStatic(ClientAttestationMgtDataHolder.class);
        applicationManagementService = mock(ApplicationManagementService.class);
        mockStatic(ApplicationManagementService.class);
        when(ClientAttestationMgtDataHolder.getInstance())
                .thenReturn(clientAttestationMgtDataHolder);
        when(ClientAttestationMgtDataHolder.getInstance().getApplicationManagementService())
                .thenReturn(applicationManagementService);
    }

    @DataProvider(name = "validateAttestationDataProvider")
    public Object[][] dataValidateAttestationMethod() {

        return new Object[][]{
                {
                        "", "tyHuiopdtQdlriM4df5mqCJZEa", "carbon.super", false, true,
                        null
                },
                {
                        "", "tyHuiopdtQdlriM4df5mqCJZEa", "carbon.super", true, false,
                        "App is configured to validate attestation but attestation object is empty."
                },
                {
                        "DUMMY OBJECT", "tyHuiopdtQdlriM4df5mqCJZEa", "carbon.super", true, false,
                        "Requested attestation object is not in valid format."
                }
        };
    }

    @Test(dataProvider = "validateAttestationDataProvider")
    public void validateAttestationTest(String attestationObject,
                                        String applicationResourceId,
                                        String tenantDomain,
                                        boolean isAttestationEnabled,
                                        boolean isAttested,
                                        String errorMessage) throws Exception {

        ServiceProvider testSp1 = new ServiceProvider();
        ClientAttestationMetaData clientAttestationMetaData = new ClientAttestationMetaData();
        clientAttestationMetaData.setAttestationEnabled(isAttestationEnabled);
        testSp1.setClientAttestationMetaData(clientAttestationMetaData);

        when(applicationManagementService.getApplicationByResourceId(anyString(), anyString()))
                .thenReturn(testSp1);
        ClientAttestationContext clientAttestationContext =
                clientAttestationService.validateAttestation(attestationObject,
                        applicationResourceId, tenantDomain);
        Assert.assertEquals(isAttested, clientAttestationContext.isAttested(),
                "False Client Attestation Validation");
        if (errorMessage != null) {
            Assert.assertEquals(errorMessage, clientAttestationContext.getValidationFailureMessage(),
                    "Wrong Error message.");
        }
    }

    @Test(expectedExceptions = ClientAttestationMgtException.class)
    public void validateAttestationTestForNullSP() throws Exception {

        when(applicationManagementService.getApplicationByResourceId(anyString(), anyString()))
                .thenReturn(null);
        ClientAttestationContext clientAttestationContext =
                clientAttestationService.validateAttestation(" ",
                        "tyHuiopdtQdlriM4df5mqCJZEa", "carbon.super");
    }
}
