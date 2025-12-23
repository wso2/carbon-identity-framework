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

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ClaimRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ClaimRetrievalClientException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ClientUtils;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.LocalClaim;
import org.wso2.carbon.utils.httpclient5.HTTPClientUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

/**
 * Unit tests for ClaimRetrievalClient.
 */
@Listeners(MockitoTestNGListener.class)
public class ClaimRetrievalClientTest {

    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String CLAIMS_API_BASE_PATH =
            "https://localhost:9443/api/server/v1/claim-dialects/local/claims";
    private static final String CUSTOM_EMPLOYEE_TYPES_CLAIM = "http://wso2.org/claims/customEmployeeTypes";
    private static final String SMSOTP_DISABLED_CLAIM = "http://wso2.org/claims/identity/smsotp_disabled";
    private static final int EXPECTED_CLAIMS_WITH_PROPERTIES = 2;
    private static final int EXPECTED_SINGLE_CLAIM = 1;
    private static final int EXPECTED_NO_CLAIMS = 0;
    private static final int EXPECTED_DISPLAY_ORDER = 20;
    private static final int EXPECTED_CANONICAL_VALUES_COUNT = 2;
    private static final int EXPECTED_PROPERTIES_COUNT = 1;
    private static final String CLAIMS_WITH_PROPERTIES_JSON = "claims/claims-with-properties.json";
    private static final String EMPTY_CLAIMS_JSON = "claims/empty-claims.json";
    private static final String SINGLE_CLAIM_WITH_CANONICAL_VALUES_JSON =
            "claims/single-claim-with-canonical-values.json";
    private final ClaimRetrievalClient claimRetrievalClient = new ClaimRetrievalClient();
    @Mock
    private IdentityManagementServiceUtil identityManagementServiceUtil;
    @Mock
    private HttpClientBuilder httpClientBuilder;
    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private HttpEntity httpEntity;
    @Mock
    private ClassicHttpResponse httpResponse;
    private String mockJsonResponse = "{}";

    @BeforeMethod
    public void setup() throws IOException {

        setupConfiguration();
    }

    public void setupConfiguration() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext();
    }

    /**
     * Read a resource in the responses directory.
     *
     * @param path File path to be read.
     * @return Content of the file as a String.
     * @throws IOException Exception when file reading fails.
     */
    public String readResource(String path) throws IOException {

        path = "responses/" + path;
        try (InputStream resourceAsStream = ClaimRetrievalClientTest.class.getClassLoader().getResourceAsStream(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(resourceAsStream)) {
            StringBuilder resourceFile = new StringBuilder();

            int character;
            while ((character = bufferedInputStream.read()) != -1) {
                char value = (char) character;
                resourceFile.append(value);
            }
            return resourceFile.toString();
        }
    }

    /**
     * Helper method to perform claim retrieval with all necessary mocks setup.
     *
     * @param jsonResponse The JSON response to return from the mocked service.
     * @param claimURIs    List of claim URIs to retrieve.
     * @return Map of claim URIs to LocalClaim objects.
     * @throws ClaimRetrievalClientException If claim retrieval fails.
     */
    private Map<String, LocalClaim> performClaimRetrievalWithMockSetup(String jsonResponse, List<String> claimURIs)
            throws ClaimRetrievalClientException {

        this.mockJsonResponse = jsonResponse;

        try (MockedStatic<IdentityManagementServiceUtil> identityMgtServiceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<HTTPClientUtils> httpclientUtil = mockStatic(HTTPClientUtils.class);
             MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                     IdentityManagementEndpointUtil.class);
             MockedStatic<ClientUtils> clientUtils = mockStatic(ClientUtils.class)) {

            identityMgtServiceUtil.when(IdentityManagementServiceUtil::getInstance)
                    .thenReturn(identityManagementServiceUtil);
            httpclientUtil.when(HTTPClientUtils::createClientWithCustomHostnameVerifier).thenReturn(httpClientBuilder);
            endpointUtil.when(() -> IdentityManagementEndpointUtil.getBasePath(anyString(), anyString()))
                    .thenReturn(CLAIMS_API_BASE_PATH);
            endpointUtil.when(() -> IdentityManagementEndpointUtil.getHttpClientResponseString(any()))
                    .thenReturn(jsonResponse);
            clientUtils.when(() -> ClientUtils.setAuthorizationHeader(any())).then(invocation -> null);

            return claimRetrievalClient.getLocalClaimsByURIs(SUPER_TENANT_DOMAIN, claimURIs);
        }
    }

    /**
     * Helper method to perform claim retrieval using JSON resource files.
     *
     * @param jsonResourcePath Path to the JSON resource file.
     * @param claimURIs        List of claim URIs to retrieve.
     * @return Map of claim URIs to LocalClaim objects.
     * @throws ClaimRetrievalClientException If claim retrieval fails.
     * @throws IOException                   If reading the resource file fails.
     */
    private Map<String, LocalClaim> performClaimRetrievalWithMocks(String jsonResourcePath, List<String> claimURIs)
            throws ClaimRetrievalClientException, IOException {

        String jsonResponse = readResource(jsonResourcePath);
        return performClaimRetrievalWithMockSetup(jsonResponse, claimURIs);
    }

    /**
     * Helper method to verify that all returned claims are in the requested URI list.
     *
     * @param result    The result map from claim retrieval.
     * @param claimURIs The list of requested claim URIs.
     */
    private void verifyReturnedClaims(Map<String, LocalClaim> result, List<String> claimURIs) {

        for (Map.Entry<String, LocalClaim> entry : result.entrySet()) {
            String claimURI = entry.getKey();
            LocalClaim claim = entry.getValue();

            assertTrue(claimURIs.contains(claimURI), "Claim URI should be in the requested list.");
            assertEquals(claim.getClaimURI(), claimURI, "Claim URI should match the map key.");
        }
    }

    @DataProvider(name = "claimResponseProvider")
    public Object[][] claimResponseProvider() {

        return new Object[][]{
                {
                        CLAIMS_WITH_PROPERTIES_JSON,
                        Arrays.asList(CUSTOM_EMPLOYEE_TYPES_CLAIM, SMSOTP_DISABLED_CLAIM),
                        EXPECTED_CLAIMS_WITH_PROPERTIES
                },
                {
                        EMPTY_CLAIMS_JSON,
                        Arrays.asList(CUSTOM_EMPLOYEE_TYPES_CLAIM),
                        EXPECTED_NO_CLAIMS
                }
        };
    }

    /**
     * Tests the retrieval of local claims by URIs with various response scenarios.
     *
     * @param jsonResourcePath    Path to the JSON resource file containing test data.
     * @param claimURIs           List of claim URIs to retrieve.
     * @param expectedClaimsCount Expected number of claims in the result.
     * @throws ClaimRetrievalClientException If claim retrieval fails.
     * @throws IOException                   If reading the resource file fails.
     */
    @Test(dataProvider = "claimResponseProvider")
    public void testGetLocalClaimsByURIs(String jsonResourcePath, List<String> claimURIs, int expectedClaimsCount)
            throws ClaimRetrievalClientException, IOException {

        Map<String, LocalClaim> result = performClaimRetrievalWithMocks(jsonResourcePath, claimURIs);

        assertNotNull(result, "Result should not be null.");
        assertEquals(result.size(), expectedClaimsCount, "Number of claims should match expected count.");

        if (expectedClaimsCount > 0) {
            verifyReturnedClaims(result, claimURIs);
        }
    }

    /**
     * Tests that null claim URIs parameter returns empty result without making any API calls.
     *
     * @throws ClaimRetrievalClientException If claim retrieval fails.
     */
    @Test
    public void testGetLocalClaimsByURIs_WithNullClaimURIs_ReturnsEmptyMap() throws ClaimRetrievalClientException {

        Map<String, LocalClaim> result = claimRetrievalClient.getLocalClaimsByURIs(SUPER_TENANT_DOMAIN, null);

        assertNotNull(result, "Result should not be null.");
        assertTrue(result.isEmpty(), "Result should be empty when claimURIs is null.");
    }

    /**
     * Tests that empty claim URIs parameter returns empty result without making any API calls.
     *
     * @throws ClaimRetrievalClientException If claim retrieval fails.
     */
    @Test
    public void testGetLocalClaimsByURIs_WithEmptyClaimURIs_ReturnsEmptyMap() throws ClaimRetrievalClientException {

        Map<String, LocalClaim> result = claimRetrievalClient.getLocalClaimsByURIs(SUPER_TENANT_DOMAIN,
                Collections.emptyList());

        assertNotNull(result, "Result should not be null.");
        assertTrue(result.isEmpty(), "Result should be empty when claimURIs is empty.");
    }

    /**
     * Tests that empty response from the service returns empty result map.
     *
     * @throws ClaimRetrievalClientException If claim retrieval fails.
     */
    @Test
    public void testGetLocalClaimsByURIs_WithEmptyResponse_ReturnsEmptyMap() throws ClaimRetrievalClientException {

        List<String> claimURIs = Arrays.asList(CUSTOM_EMPLOYEE_TYPES_CLAIM);
        Map<String, LocalClaim> result = performClaimRetrievalWithMockSetup("", claimURIs);

        assertNotNull(result, "Result should not be null.");
        assertTrue(result.isEmpty(), "Result should be empty when response is empty.");
    }

    /**
     * Tests that invalid JSON response from the service throws ClaimRetrievalClientException.
     *
     * @throws ClaimRetrievalClientException Expected exception when JSON parsing fails.
     */
    @Test(expectedExceptions = ClaimRetrievalClientException.class)
    public void testGetLocalClaimsByURIs_WithInvalidJSON_ThrowsException() throws ClaimRetrievalClientException {

        List<String> claimURIs = Arrays.asList(CUSTOM_EMPLOYEE_TYPES_CLAIM);
        performClaimRetrievalWithMockSetup("invalid json", claimURIs);
    }

    /**
     * Tests detailed claim mapping including canonical values, input format, and properties.
     *
     * @throws ClaimRetrievalClientException If claim retrieval fails.
     * @throws IOException                   If reading the resource file fails.
     */
    @Test
    public void testGetLocalClaimsByURIs_WithCanonicalValues_VerifiesCompleteMapping()
            throws ClaimRetrievalClientException, IOException {

        List<String> claimURIs = Arrays.asList(CUSTOM_EMPLOYEE_TYPES_CLAIM);
        Map<String, LocalClaim> result = performClaimRetrievalWithMocks(SINGLE_CLAIM_WITH_CANONICAL_VALUES_JSON,
                claimURIs);

        assertEquals(result.size(), EXPECTED_SINGLE_CLAIM, "Should return exactly one claim.");

        LocalClaim claim = result.get(CUSTOM_EMPLOYEE_TYPES_CLAIM);
        assertNotNull(claim, "Claim should not be null.");

        verifyBasicClaimProperties(claim);
        verifyCanonicalValues(claim);
        verifyInputFormat(claim);
        verifyClaimProperties(claim);
    }

    /**
     * Helper method to verify basic claim properties.
     *
     * @param claim The LocalClaim to verify.
     */
    private void verifyBasicClaimProperties(LocalClaim claim) {

        assertEquals(claim.getClaimURI(), CUSTOM_EMPLOYEE_TYPES_CLAIM, "Claim URI should match.");
        assertEquals(claim.getDisplayName(), "Employee Type", "Display name should match.");
        assertEquals(claim.getDataType(), "string", "Data type should match.");
        assertEquals(claim.getDisplayOrder(), Integer.valueOf(EXPECTED_DISPLAY_ORDER),
                "Display order should match.");
    }

    /**
     * Helper method to verify canonical values.
     *
     * @param claim The LocalClaim to verify.
     */
    private void verifyCanonicalValues(LocalClaim claim) {

        assertNotNull(claim.getCanonicalValues(), "Canonical values should not be null.");
        assertEquals(claim.getCanonicalValues().size(), EXPECTED_CANONICAL_VALUES_COUNT,
                "Should have expected number of canonical values.");
        assertEquals(claim.getCanonicalValues().get(0).getLabel(), "Manager",
                "First canonical value label should match.");
        assertEquals(claim.getCanonicalValues().get(0).getValue(), "manager",
                "First canonical value should match.");
    }

    /**
     * Helper method to verify input format.
     *
     * @param claim The LocalClaim to verify.
     */
    private void verifyInputFormat(LocalClaim claim) {

        assertNotNull(claim.getInputFormat(), "Input format should not be null.");
        assertEquals(claim.getInputFormat().getInputType(), "radio_group",
                "Input type should match.");
    }

    /**
     * Helper method to verify claim properties.
     *
     * @param claim The LocalClaim to verify.
     */
    private void verifyClaimProperties(LocalClaim claim) {

        if (claim.getProperties() != null && !claim.getProperties().isEmpty()) {
            assertEquals(claim.getProperties().size(), EXPECTED_PROPERTIES_COUNT,
                    "Should have expected number of properties.");
            assertEquals(claim.getProperties().get(0).getKey(), "USER_CUSTOM_ATTRIBUTE",
                    "Property key should match.");
            assertEquals(claim.getProperties().get(0).getValue(), "TRUE",
                    "Property value should match.");
        }
    }

    /**
     * Tests that endpoint URL building errors are properly wrapped in ClaimRetrievalClientException.
     *
     * @throws ClaimRetrievalClientException Expected exception when endpoint URL building fails.
     */
    @Test(expectedExceptions = ClaimRetrievalClientException.class,
            expectedExceptionsMessageRegExp = ".*Error while building url.*")
    public void testGetLocalClaimsByURIs_WithEndpointException_ThrowsWrappedException()
            throws ClaimRetrievalClientException {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                IdentityManagementEndpointUtil.class)) {

            endpointUtil.when(() -> IdentityManagementEndpointUtil.getBasePath(anyString(), anyString()))
                    .thenThrow(new ApiException("Endpoint error"));

            List<String> claimURIs = Arrays.asList(CUSTOM_EMPLOYEE_TYPES_CLAIM);
            claimRetrievalClient.getLocalClaimsByURIs(SUPER_TENANT_DOMAIN, claimURIs);
        }
    }
}
