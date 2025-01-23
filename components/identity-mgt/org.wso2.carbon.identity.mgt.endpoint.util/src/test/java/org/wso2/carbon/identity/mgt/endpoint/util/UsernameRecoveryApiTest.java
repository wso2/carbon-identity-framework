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

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.Pair;
import org.wso2.carbon.identity.mgt.endpoint.util.client.api.UsernameRecoveryApi;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mockStatic;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

/**
 * Test class for UsernameRecoveryApi.
 */
public class UsernameRecoveryApiTest {

    UsernameRecoveryApi usernameRecoveryApi;

    @Mock
    ApiClient apiClient;

    private MockedStatic<IdentityManagementEndpointUtil> mockedIdentityManagementEndpointUtil;

    private static final String TEST_PATH = "https://localhost:9443/api/identity/recovery/v0.9";

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        mockedIdentityManagementEndpointUtil = mockStatic(IdentityManagementEndpointUtil.class);
        mockedIdentityManagementEndpointUtil.when(() -> IdentityManagementEndpointUtil.buildEndpointUrl(anyString()))
                .thenReturn(TEST_PATH);

        usernameRecoveryApi = new UsernameRecoveryApi(apiClient);
    }

    @AfterMethod
    public void tearDown() {

        mockedIdentityManagementEndpointUtil.close();
    }

    @Test
    public void testClaimsGet() throws ApiException {

        String profileName = "selfRegistration";
        String tenantDomain = "carbon.super";

        mockedIdentityManagementEndpointUtil.when(() -> IdentityManagementEndpointUtil.getBasePath(
                anyString(), anyString(), eq(false))).thenReturn(TEST_PATH);

        when(apiClient.parameterToPairs(eq(""), eq("tenant-domain"), eq("carbon.super")))
                .thenReturn(Collections.singletonList(new Pair("tenant-domain", "carbon.super")));
        when(apiClient.parameterToPairs(eq(""), eq("profile-name"), eq("selfRegistration")))
                .thenReturn(Collections.singletonList(new Pair("profile-name", "selfRegistration")));

        usernameRecoveryApi.claimsGet(tenantDomain, false, profileName);

        ArgumentCaptor<List<Pair>> queryParamsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> basePathCaptor = ArgumentCaptor.forClass(String.class);

        verify(apiClient).setBasePath(basePathCaptor.capture());
        assertEquals(basePathCaptor.getValue(), TEST_PATH);

        verify(apiClient).invokeAPI(
                anyString(),
                eq("GET"),
                queryParamsCaptor.capture(),
                isNull(),
                anyMap(),
                anyMap(),
                isNull(),
                isNull(),
                any(String[].class),
                any());

        List<Pair> capturedQueryParams = queryParamsCaptor.getValue();

        // Assertions to verify query parameters.
        assertNotNull(capturedQueryParams);
        assertTrue(capturedQueryParams.stream()
                .anyMatch(pair -> pair.getName().equals("tenant-domain") && pair.getValue().equals(tenantDomain)));
        assertTrue(capturedQueryParams.stream()
                .anyMatch(pair -> pair.getName().equals("profile-name") && pair.getValue().equals(profileName)));
    }
}
