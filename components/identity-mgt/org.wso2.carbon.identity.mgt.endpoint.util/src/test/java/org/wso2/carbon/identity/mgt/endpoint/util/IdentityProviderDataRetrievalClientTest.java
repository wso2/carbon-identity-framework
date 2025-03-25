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

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.locationtech.jts.util.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.http.client.HttpClientImpl;
import org.wso2.carbon.identity.mgt.endpoint.util.client.IdentityProviderDataRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.IdentityProviderDataRetrievalClientException;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class IdentityProviderDataRetrievalClientTest extends RetrievalClientBaseTest {

    private static final String idpName = "enterpriseIDP";

    private final IdentityProviderDataRetrievalClient identityProviderDataRetrievalClient =
            new IdentityProviderDataRetrievalClient();

    @BeforeTest
    public void setMockData() throws IOException {

        setMockJsonResponse(readResource("IdpListResponse.json"));
    }

    @Test
    public void testGetIdPImage() throws IdentityProviderDataRetrievalClientException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HttpClientImpl> httpclientImpl = mockStatic(HttpClientImpl.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientImpl.when(HttpClientImpl::createClientWithCustomVerifier).thenReturn(httpClient);
            String imageKey = identityProviderDataRetrievalClient.getIdPImage(SUPER_TENANT_DOMAIN, idpName);
            Assert.equals("assets/images/logos/enterprise.svg", imageKey);
        }
    }

    @Test
    public void testGetFederatedIdpConfigs() throws IdentityProviderDataRetrievalClientException, IOException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HttpClientImpl> httpclientImpl = mockStatic(HttpClientImpl.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientImpl.when(HttpClientImpl::createClientWithCustomVerifier).thenReturn(httpClient);

            identityProviderDataRetrievalClient.getFederatedIdpConfigs(SUPER_TENANT_DOMAIN, "idp-code", idpName,
                    Stream.of("key").collect(Collectors.toList()));
        }
    }
}
