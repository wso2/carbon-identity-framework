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

import org.locationtech.jts.util.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.AuthenticatorDataRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.AuthenticatorDataRetrievalClientException;
import org.wso2.carbon.utils.httpclient5.HTTPClientUtils;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;

/**
 * Test class for AuthenticatorDataRetrievalClient.
 */
public class AuthenticatorDataRetrievalClientTest extends RetrievalClientBaseTest {

    private static final String AUTHENTICATOR_IDENTIFIER = "custom-local-authenticator";

    private final AuthenticatorDataRetrievalClient authenticatorDataRetrievalClient =
            new AuthenticatorDataRetrievalClient();

    @BeforeClass
    public void setUp() throws IOException {

        setMockJsonResponse(readResource("AuthenticatorResponse.json"));
    }

    @Test
    public void testGetAuthenticatorConfigs() throws AuthenticatorDataRetrievalClientException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HTTPClientUtils> httpclientUtil = mockStatic(HTTPClientUtils.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientUtil.when(HTTPClientUtils::createClientWithCustomHostnameVerifier).thenReturn(httpClientBuilder);

            Map<String, String> configs = authenticatorDataRetrievalClient.getAuthenticatorConfig(SUPER_TENANT_DOMAIN,
                    AUTHENTICATOR_IDENTIFIER);
            Assert.equals("https://imageGallery.com/flag.png", configs.get("image"));
            Assert.equals("Local Authenticator", configs.get("displayName"));
            Assert.equals("USER", configs.get("definedBy"));
        }
    }
}
