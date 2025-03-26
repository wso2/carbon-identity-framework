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

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.http.client.HttpClientImpl;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ConfiguredAuthenticatorsRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ConfiguredAuthenticatorsRetrievalClientException;

import static org.mockito.Mockito.mockStatic;

public class ConfiguredAuthenticatorsRetrievalClientTest extends RetrievalClientBaseTest {

    private final ConfiguredAuthenticatorsRetrievalClient configuredAuthenticatorsRetrievalClient =
            new ConfiguredAuthenticatorsRetrievalClient();

    @BeforeTest
    public void setupData() {

        setMockJsonResponse("[{}]");
    }

    @Test
    public void testGetConfiguredAuthenticators() throws ConfiguredAuthenticatorsRetrievalClientException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HttpClientImpl> httpclientImpl = mockStatic(HttpClientImpl.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientImpl.when(HttpClientImpl::createClientWithCustomVerifier).thenReturn(httpClient);
            configuredAuthenticatorsRetrievalClient.getConfiguredAuthenticators("", SUPER_TENANT_DOMAIN);
        }
    }
}
