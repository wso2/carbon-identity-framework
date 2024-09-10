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

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.util.Assert;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ValidationConfigurationRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ValidationConfigurationRetrievalClientException;
import org.wso2.carbon.utils.HTTPClientUtils;

import static org.mockito.Mockito.mockStatic;

public class ValidationConfigurationRetrievalClientTest extends RetrievalClientBaseTest {

    private final ValidationConfigurationRetrievalClient validationConfigurationRetrievalClient =
            new ValidationConfigurationRetrievalClient();

    @BeforeTest
    public void setupData() {

        setMockJsonResponse("[\n" +
                "    {\n" +
                "        \"field\": \"password\",\n" +
                "        \"rules\": [\n" +
                "            {\n" +
                "                \"validator\": \"LengthValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"8\",\n" +
                "                        \"key\": \"min.length\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"validator\": \"LengthValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"30\",\n" +
                "                        \"key\": \"max.length\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"validator\": \"NumeralValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"1\",\n" +
                "                        \"key\": \"min.length\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"validator\": \"UpperCaseValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"1\",\n" +
                "                        \"key\": \"min.length\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"validator\": \"LowerCaseValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"1\",\n" +
                "                        \"key\": \"min.length\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"validator\": \"SpecialCharacterValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"1\",\n" +
                "                        \"key\": \"min.length\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"field\": \"username\",\n" +
                "        \"rules\": [\n" +
                "            {\n" +
                "                \"validator\": \"LengthValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"5\",\n" +
                "                        \"key\": \"min.length\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"value\": \"30\",\n" +
                "                        \"key\": \"max.length\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"validator\": \"AlphanumericValidator\",\n" +
                "                \"properties\": [\n" +
                "                    {\n" +
                "                        \"value\": \"true\",\n" +
                "                        \"key\": \"enable.validator\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]");
    }

    @Test
    public void testGetConfigurations() throws ValidationConfigurationRetrievalClientException {

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HTTPClientUtils> httpclientUtil = mockStatic(HTTPClientUtils.class)) {
            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientUtil.when(HTTPClientUtils::createClientWithCustomVerifier).thenReturn(httpClientBuilder);
            JSONArray jsonArray = validationConfigurationRetrievalClient.getConfigurations(SUPER_TENANT_DOMAIN);
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            jsonArray = (JSONArray) jsonObject.get("rules");
            Assert.equals("LengthValidator", ((JSONObject) jsonArray.get(0)).get("validator").toString());
        }
    }
}
