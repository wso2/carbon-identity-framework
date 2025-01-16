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

package org.wso2.carbon.identity.application.mgt.ai;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.ai.service.mgt.util.AIHttpClientUtil;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.core.UserStoreException;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

/**
 * Test class for LoginFlowAIManager.
 */
public class LoginFlowAIManagerTest {

    private MockedStatic<AIHttpClientUtil> aiHttpClientUtilMockedStatic;

    @InjectMocks
    private LoginFlowAIManagerImpl loginFlowAIManager;

    @BeforeMethod
    public void setUp() throws UserStoreException {

        openMocks(this);
        setCarbonHome();
        setCarbonContextForTenant(SUPER_TENANT_DOMAIN_NAME, SUPER_TENANT_ID);
        aiHttpClientUtilMockedStatic = mockStatic(AIHttpClientUtil.class);
    }

    @Test
    public void testGenerateAuthenticationSequenceSuccess() throws Exception {

        Map<String, Object> response = new HashMap<>();
        response.put("operation_id", "12345");
        mockSuccessfulResponse(response);
        String result = loginFlowAIManager.generateAuthenticationSequence("Need username and password as " +
                "the first step", new JSONArray(), new JSONObject());
        Assert.assertEquals(result, "12345");
    }

    @Test
    public void testGetAuthenticationSequenceGenerationStatusSuccess() throws Exception {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "COMPLETED");
        mockSuccessfulResponse(response);
        Object result = loginFlowAIManager.getAuthenticationSequenceGenerationStatus("operation123");

        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        Assert.assertEquals(resultMap.get("status"), "COMPLETED");
    }

    @Test
    public void testGetAuthenticationSequenceGenerationResultSuccess() throws Exception {

        Map<String, Object> response = new HashMap<>();
        response.put("result", "SUCCESS");
        mockSuccessfulResponse(response);
        Object result = loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");

        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
    }

    private void mockSuccessfulResponse(Map<String, Object> responseBody) {

        aiHttpClientUtilMockedStatic.when(() -> AIHttpClientUtil.executeRequest(
                any(), any(), any(), any()
        )).thenReturn(responseBody);
    }


    private void setCarbonHome() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes",
                "repository").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    private void setCarbonContextForTenant(String tenantDomain, int tenantId) throws UserStoreException {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        InMemoryRealmService testSessionRealmService = new InMemoryRealmService(tenantId);
        IdentityTenantUtil.setRealmService(testSessionRealmService);
    }

    @AfterMethod
    public void tearDown() {

        aiHttpClientUtilMockedStatic.close();
        PrivilegedCarbonContext.endTenantFlow();
    }
}
