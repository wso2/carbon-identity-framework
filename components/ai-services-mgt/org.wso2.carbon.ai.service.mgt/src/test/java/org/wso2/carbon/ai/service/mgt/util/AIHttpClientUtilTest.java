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

package org.wso2.carbon.ai.service.mgt.util;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.ai.service.mgt.token.AIAccessTokenManager;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.nio.file.Paths;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class AIHttpClientUtilTest {

    @Mock
    private AIAccessTokenManager mockTokenManager;

    private MockedStatic<AIHttpClientUtil> aiHttpClientUtilMockedStatic;
    private MockedStatic<AIAccessTokenManager> aiAccessTokenManagerMockedStatic;


    @BeforeMethod
    public void setUp() throws Exception {

        initMocks(this);
        setCarbonHome();
        setCarbonContextForTenant(SUPER_TENANT_DOMAIN_NAME);

        aiAccessTokenManagerMockedStatic = mockStatic(AIAccessTokenManager.class);
        aiHttpClientUtilMockedStatic = mockStatic(AIHttpClientUtil.class, Mockito.CALLS_REAL_METHODS);

        when(AIAccessTokenManager.getInstance()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken(false)).thenReturn("testToken");
        when(mockTokenManager.getClientId()).thenReturn("testClientId");
    }

    @Test
    public void testExecuteRequest_Success() throws Exception {

        String expectedResponse = "{\"result\":\"SUCCESS\"}";
        mockHttpResponse(HttpStatus.SC_OK, expectedResponse);

        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );

        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
    }

    @Test(expectedExceptions = AIClientException.class)
    public void testExecuteRequest_ClientError() throws Exception {

        mockHttpResponse(HttpStatus.SC_BAD_REQUEST, "Bad Request");
        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_ServerError() throws Exception {

        mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test
    public void testExecuteRequest_TokenRenewal() throws Exception {

        String expectedResponse = "{\"result\":\"SUCCESS\"}";
        when(mockTokenManager.getAccessToken(false)).thenReturn("oldToken");
        when(mockTokenManager.getAccessToken(true)).thenReturn("newToken");

        mockHttpResponseSequence(
                new AIHttpClientUtil.HttpResponseWrapper(HttpStatus.SC_UNAUTHORIZED, "Unauthorized"),
                new AIHttpClientUtil.HttpResponseWrapper(HttpStatus.SC_OK, expectedResponse));

        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );

        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
    }

    private void mockHttpResponse(int statusCode, String responseBody) {

        AIHttpClientUtil.HttpResponseWrapper mockResponse = new AIHttpClientUtil.HttpResponseWrapper(statusCode,
                responseBody);
        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeHttpRequest(any(), any()))
                .thenReturn(mockResponse);
    }

    private void mockHttpResponseSequence(AIHttpClientUtil.HttpResponseWrapper... responses) {
        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeHttpRequest(any(), any()))
                .thenReturn(responses[0], responses[1]);
    }

    private void setCarbonHome() {
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    private void setCarbonContextForTenant(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
    }

    @AfterMethod
    public void tearDown() {

        aiHttpClientUtilMockedStatic.close();
        aiAccessTokenManagerMockedStatic.close();
    }
}
