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

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.testng.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApplicationDataRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApplicationDataRetrievalClientException;
import org.wso2.carbon.utils.httpclient5.HTTPClientUtils;

import java.io.IOException;
import java.util.Set;

import static org.mockito.Mockito.mockStatic;

/**
 * Test class for ApplicationDataRetrievalClientTest.
 */
public class ApplicationDataRetrievalClientTest extends RetrievalClientBaseTest {

    private static final String APPLICATION_IDENTIFIER = "3285d13a-59ea-4107-a2db-e2641e0bce79";

    private final ApplicationDataRetrievalClient applicationDataRetrievalClient =
            new ApplicationDataRetrievalClient();

    @BeforeClass
    public void setUp() throws IOException {

        setMockJsonResponse(readResource("ApplicationResponse.json"));
    }

    @Test
    public void testGetApplicationAuthenticatorsByAppId() throws ApplicationDataRetrievalClientException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HTTPClientUtils> httpclientUtil = mockStatic(HTTPClientUtils.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientUtil.when(HTTPClientUtils::createClientWithCustomHostnameVerifier).thenReturn(httpClientBuilder);

            Set<String> configuredAuthenticatorsSet = applicationDataRetrievalClient.getApplicationAuthenticatorsByAppId(SUPER_TENANT_DOMAIN,
                    APPLICATION_IDENTIFIER);

            Assert.assertTrue((configuredAuthenticatorsSet.contains("BasicAuthenticator:LOCAL")), "Should contain BasicAuthenticator");
            Assert.assertTrue((configuredAuthenticatorsSet.contains("OpenIDConnectAuthenticator:Microsoft")), "Should contain OIDC Microsoft");
            Assert.assertTrue((configuredAuthenticatorsSet.contains("totp:LOCAL")), "Should contain TOTP");
        }
    }
}
