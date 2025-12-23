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

import com.sun.jersey.api.client.ClientResponse;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.api.SelfRegisterApi;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Claim;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.SelfRegistrationUser;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.SelfUserRegistrationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for SelfRegisterApi class.
 */
public class SelfRegisterApiTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String CUSTOM_TENANT_DOMAIN = "wso2.com";
    private static final String POST = "POST";
    private static final String PATH_ME = "/me";
    private static final String TEST_ACCEPT_HEADER = "application/json";
    private static final String TEST_CONTENT_HEADER = "application/json";
    private static final String TEST_BASE_PATH = "https://localhost:9443/api/identity/user/v1.0";
    private static final String TEST_RESPONSE = "registration-successful";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "testuser@example.com";

    @Mock
    private ApiClient apiClient;

    private SelfRegisterApi selfRegisterApi;

    @BeforeMethod
    public void setup() {

        MockitoAnnotations.openMocks(this);

        when(apiClient.selectHeaderAccept(any())).thenReturn(TEST_ACCEPT_HEADER);
        when(apiClient.selectHeaderContentType(any())).thenReturn(TEST_CONTENT_HEADER);

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(
                            () -> IdentityManagementEndpointUtil.getBasePath(anyString(), anyString(), eq(false)))
                    .thenReturn(TEST_BASE_PATH);

            selfRegisterApi = new SelfRegisterApi(apiClient);
        }
    }

    /**
     * Test successful user self registration with super tenant domain.
     */
    @Test
    public void testMePostCallSuccessWithSuperTenantDomain() throws ApiException {

        // Create test data
        SelfUserRegistrationRequest registrationRequest = createTestRegistrationRequest(TENANT_DOMAIN);
        Map<String, String> headers = createTestHeaders();

        when(apiClient.invokeAPI(eq(PATH_ME), eq(POST), any(), eq(registrationRequest), any(), any(),
                eq(TEST_ACCEPT_HEADER), eq(TEST_CONTENT_HEADER), any(), any()))
                .thenReturn(TEST_RESPONSE);
        when(apiClient.getStatusCode()).thenReturn(ClientResponse.Status.OK.getStatusCode());

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(() -> IdentityManagementEndpointUtil.
                    getBasePath(TENANT_DOMAIN, IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH))
                    .thenReturn(TEST_BASE_PATH);

            String result = selfRegisterApi.mePostCall(registrationRequest, headers);
            assertEquals(result, TEST_RESPONSE);
        }
    }

    /**
     * Test user self registration with ACCEPTED status code returns PENDING_APPROVAL.
     */
    @Test
    public void testMePostCallWithAcceptedStatusCode() throws ApiException {

        // Create test data
        SelfUserRegistrationRequest registrationRequest = createTestRegistrationRequest(TENANT_DOMAIN);
        Map<String, String> headers = createTestHeaders();

        when(apiClient.invokeAPI(eq(PATH_ME), eq(POST), any(), eq(registrationRequest), any(), any(),
                eq(TEST_ACCEPT_HEADER), eq(TEST_CONTENT_HEADER), any(), any()))
                .thenReturn(TEST_RESPONSE);
        when(apiClient.getStatusCode()).thenReturn(ClientResponse.Status.ACCEPTED.getStatusCode());

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(() -> IdentityManagementEndpointUtil.
                    getBasePath(TENANT_DOMAIN, IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH))
                    .thenReturn(TEST_BASE_PATH);

            String result = selfRegisterApi.mePostCall(registrationRequest, headers);
            assertEquals(result, IdentityManagementEndpointConstants.PENDING_APPROVAL);
        }
    }

    /**
     * Test successful user self registration with custom tenant domain.
     */
    @Test
    public void testMePostCallSuccessWithCustomTenantDomain() throws ApiException {

        // Create test data
        SelfUserRegistrationRequest registrationRequest = createTestRegistrationRequest(CUSTOM_TENANT_DOMAIN);
        Map<String, String> headers = createTestHeaders();

        when(apiClient.invokeAPI(eq(PATH_ME), eq(POST), any(), eq(registrationRequest), any(), any(),
                eq(TEST_ACCEPT_HEADER), eq(TEST_CONTENT_HEADER), any(), any()))
                .thenReturn(TEST_RESPONSE);
        when(apiClient.getStatusCode()).thenReturn(ClientResponse.Status.OK.getStatusCode());

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(() -> IdentityManagementEndpointUtil.
                    getBasePath(CUSTOM_TENANT_DOMAIN, IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH))
                    .thenReturn(TEST_BASE_PATH);

            String result = selfRegisterApi.mePostCall(registrationRequest, headers);
            assertEquals(result, TEST_RESPONSE);
        }
    }

    /**
     * Test user self registration with null headers.
     */
    @Test
    public void testMePostCallWithNullHeaders() throws ApiException {

        // Create test data
        SelfUserRegistrationRequest registrationRequest = createTestRegistrationRequest(TENANT_DOMAIN);

        when(apiClient.invokeAPI(eq(PATH_ME), eq(POST), any(), eq(registrationRequest), any(), any(),
                eq(TEST_ACCEPT_HEADER), eq(TEST_CONTENT_HEADER), any(), any()))
                .thenReturn(TEST_RESPONSE);
        when(apiClient.getStatusCode()).thenReturn(ClientResponse.Status.OK.getStatusCode());

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(() -> IdentityManagementEndpointUtil.
                    getBasePath(TENANT_DOMAIN, IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH))
                    .thenReturn(TEST_BASE_PATH);

            String result = selfRegisterApi.mePostCall(registrationRequest, null);
            assertEquals(result, TEST_RESPONSE);
        }
    }

    /**
     * Test user self registration with user having null tenant domain (should use super tenant).
     */
    @Test
    public void testMePostCallWithNullTenantDomain() throws ApiException {

        // Create test data with null tenant domain
        SelfUserRegistrationRequest registrationRequest = createTestRegistrationRequest(null);
        Map<String, String> headers = createTestHeaders();

        when(apiClient.invokeAPI(eq(PATH_ME), eq(POST), any(), eq(registrationRequest), any(), any(),
                eq(TEST_ACCEPT_HEADER), eq(TEST_CONTENT_HEADER), any(), any()))
                .thenReturn(TEST_RESPONSE);
        when(apiClient.getStatusCode()).thenReturn(ClientResponse.Status.OK.getStatusCode());

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(() -> IdentityManagementEndpointUtil.
                    getBasePath(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, 
                            IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH))
                    .thenReturn(TEST_BASE_PATH);

            String result = selfRegisterApi.mePostCall(registrationRequest, headers);
            assertEquals(result, TEST_RESPONSE);
        }
    }

    /**
     * Test user self registration with empty tenant domain (should use super tenant).
     */
    @Test
    public void testMePostCallWithEmptyTenantDomain() throws ApiException {

        // Create test data with empty tenant domain
        SelfUserRegistrationRequest registrationRequest = createTestRegistrationRequest("");
        Map<String, String> headers = createTestHeaders();

        when(apiClient.invokeAPI(eq(PATH_ME), eq(POST), any(), eq(registrationRequest), any(), any(),
                eq(TEST_ACCEPT_HEADER), eq(TEST_CONTENT_HEADER), any(), any()))
                .thenReturn(TEST_RESPONSE);
        when(apiClient.getStatusCode()).thenReturn(ClientResponse.Status.OK.getStatusCode());

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(() -> IdentityManagementEndpointUtil.
                    getBasePath(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, 
                            IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH))
                    .thenReturn(TEST_BASE_PATH);

            String result = selfRegisterApi.mePostCall(registrationRequest, headers);
            assertEquals(result, TEST_RESPONSE);
        }
    }

    /**
     * Test exception when user parameter is null.
     */
    @Test(expectedExceptions = ApiException.class, expectedExceptionsMessageRegExp = "Missing the required parameter 'user' when calling mePost")
    public void testMePostCallWithNullUser() throws ApiException {

        Map<String, String> headers = createTestHeaders();
        selfRegisterApi.mePostCall(null, headers);
    }

    /**
     * Test API exception handling during API invocation.
     */
    @Test(expectedExceptions = ApiException.class)
    public void testMePostCallApiException() throws ApiException {

        // Create test data
        SelfUserRegistrationRequest registrationRequest = createTestRegistrationRequest(TENANT_DOMAIN);
        Map<String, String> headers = createTestHeaders();

        when(apiClient.invokeAPI(eq(PATH_ME), eq(POST), any(), eq(registrationRequest), any(), any(),
                eq(TEST_ACCEPT_HEADER), eq(TEST_CONTENT_HEADER), any(), any()))
                .thenThrow(new ApiException(500, "Internal Server Error"));

        try (MockedStatic<IdentityManagementEndpointUtil> identityManagementEndpointUtilMockedStatic =
                     mockStatic(IdentityManagementEndpointUtil.class)) {
            identityManagementEndpointUtilMockedStatic.when(() -> IdentityManagementEndpointUtil.
                    getBasePath(TENANT_DOMAIN, IdentityManagementEndpointConstants.UserInfoRecovery.USER_API_RELATIVE_PATH))
                    .thenReturn(TEST_BASE_PATH);

            selfRegisterApi.mePostCall(registrationRequest, headers);
        }
    }

    /**
     * Test getApiClient method.
     */
    @Test
    public void testGetApiClient() {

        assertEquals(selfRegisterApi.getApiClient(), apiClient);
    }

    /**
     * Test setApiClient method.
     */
    @Test
    public void testSetApiClient() {

        ApiClient newApiClient = new ApiClient();
        selfRegisterApi.setApiClient(newApiClient);
        assertEquals(selfRegisterApi.getApiClient(), newApiClient);
    }

    /**
     * Creates a test SelfUserRegistrationRequest with the specified tenant domain.
     *
     * @param tenantDomain the tenant domain to set
     * @return SelfUserRegistrationRequest instance for testing
     */
    private SelfUserRegistrationRequest createTestRegistrationRequest(String tenantDomain) {

        SelfUserRegistrationRequest request = new SelfUserRegistrationRequest();
        SelfRegistrationUser user = new SelfRegistrationUser();
        user.setUsername(TEST_USERNAME);
        user.setTenantDomain(tenantDomain);
        
        // Add email as a claim
        List<Claim> claims = new ArrayList<>();
        Claim emailClaim = new Claim();
        emailClaim.setUri("http://wso2.org/claims/emailaddress");
        emailClaim.setValue(TEST_EMAIL);
        claims.add(emailClaim);
        user.setClaims(claims);
        
        request.setUser(user);
        return request;
    }

    /**
     * Creates test headers map.
     *
     * @return Map containing test headers
     */
    private Map<String, String> createTestHeaders() {

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer test-token");
        headers.put("X-Custom-Header", "test-value");
        return headers;
    }
}
