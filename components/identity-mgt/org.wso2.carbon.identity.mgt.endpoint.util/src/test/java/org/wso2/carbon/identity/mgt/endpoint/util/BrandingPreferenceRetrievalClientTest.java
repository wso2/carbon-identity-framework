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

import org.json.JSONObject;
import org.locationtech.jts.util.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.BrandingPreferenceRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.BrandingPreferenceRetrievalClientException;
import org.wso2.carbon.utils.httpclient5.HTTPClientUtils;

import java.io.IOException;

import static org.mockito.Mockito.mockStatic;

public class BrandingPreferenceRetrievalClientTest extends RetrievalClientBaseTest {

    private final BrandingPreferenceRetrievalClient preferenceRetrievalClient =
            new BrandingPreferenceRetrievalClient();

    @BeforeTest
    public void setMockData() throws IOException {

        setMockJsonResponse(readResource("BrandingPreferenceResponse.json"));
    }

    @Test
    public void testGetPreference() throws BrandingPreferenceRetrievalClientException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HTTPClientUtils> httpclientUtil = mockStatic(HTTPClientUtils.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientUtil.when(HTTPClientUtils::createClientWithCustomHostnameVerifier).thenReturn(httpClientBuilder);
            JSONObject jsonObject = preferenceRetrievalClient.getPreference(SUPER_TENANT_DOMAIN, "ORG", null, "en-US");
            Assert.equals("centered",
                    ((JSONObject) ((JSONObject) jsonObject.get("preference")).get("layout")).get("activeLayout")
                            .toString());
        }
    }

    @Test
    public void testGetCustomTextPreference() throws BrandingPreferenceRetrievalClientException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HTTPClientUtils> httpclientUtil = mockStatic(HTTPClientUtils.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientUtil.when(HTTPClientUtils::createClientWithCustomHostnameVerifier).thenReturn(httpClientBuilder);
            preferenceRetrievalClient.getCustomTextPreference(SUPER_TENANT_DOMAIN, "ORG", "", "", "en-US");
        }
    }
}
