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

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.PreferenceRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.PreferenceRetrievalClientException;
import org.wso2.carbon.utils.HTTPClientUtils;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Listeners(MockitoTestNGListener.class)
public class PreferenceRetrievalClientTest {

    private PreferenceRetrievalClient preferenceRetrievalClient;
    private PreferenceRetrievalClient preferenceRetrievalClientSpy;
    private String tenantDomain;
    private String callabackURL;

    @BeforeClass
    public void setup() throws PreferenceRetrievalClientException {

        this.tenantDomain = "admin";
        this.callabackURL = "test";
        this.preferenceRetrievalClient = new PreferenceRetrievalClient();
        this.preferenceRetrievalClientSpy = Mockito.spy(preferenceRetrievalClient);

        Mockito.doReturn(true).when(preferenceRetrievalClientSpy).checkPreference(anyString(),
                anyString(), anyString(), Mockito.anyBoolean());
        Mockito.doReturn(Optional.of("test")).when(preferenceRetrievalClientSpy).getPropertyValue(anyString(),
                anyString(), anyString(), anyString());
        Mockito.doReturn(true).when(preferenceRetrievalClientSpy).checkMultiplePreference(anyString(), anyString(),
                any());

    }

    @Test
    public void testCheckPreference() throws PreferenceRetrievalClientException {

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     Mockito.mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(anyString(), anyString()))
                    .thenReturn("https://localhost:9443/api/server/v1/identity-governance/preferences");

            CloseableHttpResponse closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);

            CloseableHttpClient closeableHttpClient = Mockito.mock(CloseableHttpClient.class);
            Mockito.when(closeableHttpClient.execute(any())).thenReturn(closeableHttpResponse);

            StatusLine statusLine = Mockito.mock(StatusLine.class);
            Mockito.when(statusLine.getStatusCode()).thenReturn(201);
            Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);

            HttpClientBuilder httpClientBuilder = Mockito.mock(HttpClientBuilder.class);
            Mockito.when(httpClientBuilder.build()).thenReturn(closeableHttpClient);
            try (MockedStatic<HTTPClientUtils> httpClientUtilsMockedStatic = Mockito.mockStatic(
                    HTTPClientUtils.class)) {
                httpClientUtilsMockedStatic.when(HTTPClientUtils::createClientWithCustomVerifier)
                        .thenReturn(httpClientBuilder);

                IdentityManagementServiceUtil identityManagementServiceUtil =
                        Mockito.mock(IdentityManagementServiceUtil.class);
                Mockito.when(identityManagementServiceUtil.getAppName()).thenReturn("testAppName");
                Mockito.when(identityManagementServiceUtil.getAppPassword()).thenReturn("testPassword".toCharArray());

                try (MockedStatic<IdentityManagementServiceUtil> identityManagementServiceUtilMockedStatic =
                             Mockito.mockStatic(IdentityManagementServiceUtil.class)) {

                    identityManagementServiceUtilMockedStatic.when(IdentityManagementServiceUtil::getInstance)
                            .thenReturn(identityManagementServiceUtil);
                    assertTrue(preferenceRetrievalClient.checkSelfRegistration(tenantDomain));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testCheckSelfRegistration() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkSelfRegistration(tenantDomain));
    }

    @Test
    public void testCheckSelfRegistrationOnLockCreation() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkSelfRegistrationLockOnCreation(tenantDomain));
    }

    @Test
    public void testCheckSelfRegistrationSendConfirmationOnCreation() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkSelfRegistrationSendConfirmationOnCreation(tenantDomain));
    }

    @Test
    public void testCheckUsernameRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkUsernameRecovery(tenantDomain));
    }

    @Test
    public void testCheckEmailBasedUsernameRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkEmailBasedUsernameRecovery(tenantDomain));
    }

    @Test
    public void testCheckSMSBasedUsernameRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkSMSBasedUsernameRecovery(tenantDomain));
    }

    @Test
    public void testCheckNotificationBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkNotificationBasedPasswordRecovery(tenantDomain));
    }

    @Test
    public void testCheckEmailLinkBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkEmailLinkBasedPasswordRecovery(tenantDomain));
    }

    @Test
    public void testCheckSMSOTPBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkSMSOTPBasedPasswordRecovery(tenantDomain));
    }

    @Test
    public void testCheckQuestionBasedPasswordRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkQuestionBasedPasswordRecovery(tenantDomain));
    }

    @Test
    public void testCheckMultiAttributeLogin() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkMultiAttributeLogin(tenantDomain));
    }

    @Test
    public void testCheckTypingDNA() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkTypingDNA(tenantDomain));
    }

    @Test
    public void testCheckAutoLoginAfterSelfRegistrationEnabled() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkAutoLoginAfterSelfRegistrationEnabled(tenantDomain));
    }

    @Test
    public void testCheckAutoLoginAfterPasswordRecoveryEnabled() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkAutoLoginAfterPasswordRecoveryEnabled(tenantDomain));
    }

    @Test
    public void testCheckMultiAttributeLoginProperty() throws PreferenceRetrievalClientException {

        assertEquals(preferenceRetrievalClientSpy.checkMultiAttributeLoginProperty(tenantDomain), "test");
    }

    @Test
    public void testCheckMultiAttributeLoginPropertyNull() throws PreferenceRetrievalClientException {

        Mockito.doReturn(Optional.empty()).when(preferenceRetrievalClientSpy).getPropertyValue(anyString(), anyString(),
                anyString(), anyString());
        assertNull(preferenceRetrievalClientSpy.checkMultiAttributeLoginProperty(tenantDomain));
    }

    @Test
    public void testCheckIfRecoveryCallbackURLValid() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkIfRecoveryCallbackURLValid(tenantDomain, callabackURL));
    }

    @Test
    public void testCheckIfSelfRegCallbackURLValid() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkIfSelfRegCallbackURLValid(tenantDomain, callabackURL));
    }

    @Test
    public void testCheckIfLiteRegCallbackURLValid() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkIfLiteRegCallbackURLValid(tenantDomain, callabackURL));
    }

    @Test
    public void checkCheckPasswordRecovery() throws PreferenceRetrievalClientException {

        assertTrue(preferenceRetrievalClientSpy.checkPasswordRecovery(tenantDomain));
    }
}
