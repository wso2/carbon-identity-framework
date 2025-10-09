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

package org.wso2.carbon.identity.workflow.mgt.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.workflow.mgt.bean.Property;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for Utils class.
 */
@WithCarbonHome
public class UtilsTest {

    @Mock
    private WorkflowServiceDataHolder mockDataHolder;

    @Mock
    private ClaimMetadataManagementService mockClaimService;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    private MockedStatic<WorkflowServiceDataHolder> workflowServiceDataHolderMock;
    private MockedStatic<CarbonContext> carbonContextMock;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        workflowServiceDataHolderMock = mockStatic(WorkflowServiceDataHolder.class);
        carbonContextMock = mockStatic(CarbonContext.class);

        workflowServiceDataHolderMock.when(WorkflowServiceDataHolder::getInstance).thenReturn(mockDataHolder);

        // Mock CarbonContext statically without creating mock instance
        CarbonContext mockCarbonContextInstance = mock(CarbonContext.class);
        carbonContextMock.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContextInstance);
        when(mockCarbonContextInstance.getTenantDomain()).thenReturn("carbon.super");

        when(mockDataHolder.getClaimMetadataManagementService()).thenReturn(mockClaimService);
    }

    @AfterMethod
    public void tearDown() {

        if (workflowServiceDataHolderMock != null) {
            workflowServiceDataHolderMock.close();
        }
        if (carbonContextMock != null) {
            carbonContextMock.close();
        }
    }

    @Test
    public void testGeneratePrepStmtPostgreSQL() throws Exception {

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        try (MockedStatic<org.wso2.carbon.identity.core.util.JdbcUtils> jdbcUtilsMock =
                mockStatic(org.wso2.carbon.identity.core.util.JdbcUtils.class)) {
            jdbcUtilsMock.when(org.wso2.carbon.identity.core.util.JdbcUtils::isPostgreSQLDB).thenReturn(true);

            PreparedStatement result = Utils.generatePrepStmt(mockConnection, "SELECT * FROM table", 1,
                    "filter", 10, 20);

            assertNotNull(result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setString(2, "filter");
            verify(mockPreparedStatement).setInt(3, 20);
            verify(mockPreparedStatement).setInt(4, 10);
        }
    }

    @Test
    public void testGeneratePrepStmtNonPostgreSQL() throws Exception {

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        try (MockedStatic<org.wso2.carbon.identity.core.util.JdbcUtils> jdbcUtilsMock =
                mockStatic(org.wso2.carbon.identity.core.util.JdbcUtils.class)) {
            jdbcUtilsMock.when(org.wso2.carbon.identity.core.util.JdbcUtils::isPostgreSQLDB).thenReturn(false);

            PreparedStatement result = Utils.generatePrepStmt(mockConnection, "SELECT * FROM table", 1,
                    "filter", 10, 20);

            assertNotNull(result);
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setString(2, "filter");
            verify(mockPreparedStatement).setInt(3, 10);
            verify(mockPreparedStatement).setInt(4, 20);
        }
    }

    @Test
    public void testGetWorkflowRequestParametersWithClaims() throws Exception {

        // Setup test data
        WorkflowRequest workflowRequest = createWorkflowRequestWithClaims();
        List<LocalClaim> mockLocalClaims = createMockLocalClaims();

        when(mockClaimService.getLocalClaims(anyString())).thenReturn(mockLocalClaims);

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertTrue(result.size() >= 2, "Should have at least 2 properties from claims");

        // Verify claims are properly processed
        boolean foundEmailProperty = false;
        boolean foundFirstNameProperty = false;

        for (Property property : result) {
            if ("Email".equals(property.getKey())) {
                foundEmailProperty = true;
                assertEquals(property.getValue(), "test@example.com");
            } else if ("First Name".equals(property.getKey())) {
                foundFirstNameProperty = true;
                assertEquals(property.getValue(), "John");
            }
        }

        assertTrue(foundEmailProperty, "Should find email property with display name");
        assertTrue(foundFirstNameProperty, "Should find first name property with display name");

        verify(mockClaimService).getLocalClaims("carbon.super");
    }

    @Test
    public void testGetWorkflowRequestParametersWithRegularParameters() throws Exception {

        WorkflowRequest workflowRequest = createWorkflowRequestWithRegularParameters();

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertEquals(result.size(), 2);

        assertEquals(result.get(0).getKey(), "username");
        assertEquals(result.get(0).getValue(), "testuser");
        assertEquals(result.get(1).getKey(), "role");
        assertEquals(result.get(1).getValue(), "admin");
    }

    @Test
    public void testGetWorkflowRequestParametersSkipsCredentials() throws Exception {

        WorkflowRequest workflowRequest = createWorkflowRequestWithCredentials();

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertEquals(result.size(), 1); // Only non-credential parameter should be included
        assertEquals(result.get(0).getKey(), "username");
        assertEquals(result.get(0).getValue(), "testuser");
    }

    @Test
    public void testGetWorkflowRequestParametersSkipsTenantDomain() throws Exception {

        WorkflowRequest workflowRequest = createWorkflowRequestWithTenantDomain();

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertEquals(result.size(), 1); // Only non-tenant-domain parameter should be included
        assertEquals(result.get(0).getKey(), "username");
        assertEquals(result.get(0).getValue(), "testuser");
    }

    @Test
    public void testGetWorkflowRequestParametersWithClaimMetadataException() throws Exception {

        WorkflowRequest workflowRequest = createWorkflowRequestWithClaims();

        when(mockClaimService.getLocalClaims(anyString())).thenThrow(new ClaimMetadataException("Test exception"));

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertEquals(result.size(), 1); // Only the regular parameter should be included
        assertEquals(result.get(0).getKey(), "username");
        assertEquals(result.get(0).getValue(), "testuser");
    }

    @Test
    public void testGetWorkflowRequestParametersWithInvalidClaimsMap() throws Exception {

        WorkflowRequest workflowRequest = createWorkflowRequestWithInvalidClaimsMap();

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertEquals(result.size(), 1); // Only the regular parameter should be included
        assertEquals(result.get(0).getKey(), "username");
        assertEquals(result.get(0).getValue(), "testuser");
    }

    @Test
    public void testGetWorkflowRequestParametersWithNullValues() throws Exception {

        WorkflowRequest workflowRequest = createWorkflowRequestWithNullValues();

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertEquals(result.size(), 1); // Only the non-null parameter should be included
        assertEquals(result.get(0).getKey(), "username");
        assertEquals(result.get(0).getValue(), "testuser");
    }

    @Test
    public void testGetWorkflowRequestParametersWithEmptyClaimValues() throws Exception {

        WorkflowRequest workflowRequest = createWorkflowRequestWithEmptyClaimValues();
        List<LocalClaim> mockLocalClaims = createMockLocalClaims();

        when(mockClaimService.getLocalClaims(anyString())).thenReturn(mockLocalClaims);

        List<Property> result = Utils.getWorkflowRequestParameters(workflowRequest);

        assertNotNull(result);
        assertEquals(result.size(), 1); // Empty claim values should be skipped
        assertEquals(result.get(0).getKey(), "username");
        assertEquals(result.get(0).getValue(), "testuser");
    }

    // Helper methods to create test data

    private WorkflowRequest createWorkflowRequestWithClaims() {

        WorkflowRequest request = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<>();

        // Add claims parameter
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put("http://wso2.org/claims/emailaddress", "test@example.com");
        claimsMap.put("http://wso2.org/claims/givenname", "John");

        RequestParameter claimsParam = new RequestParameter();
        claimsParam.setName(WFConstant.CLAIMS_PROPERTY_NAME);
        claimsParam.setValue(claimsMap);
        claimsParam.setValueType(WorkflowDataType.STRING_STRING_MAP_TYPE);
        parameters.add(claimsParam);

        // Add regular parameter
        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithRegularParameters() {

        WorkflowRequest request = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<>();

        RequestParameter param1 = new RequestParameter();
        param1.setName("username");
        param1.setValue("testuser");
        param1.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(param1);

        RequestParameter param2 = new RequestParameter();
        param2.setName("role");
        param2.setValue("admin");
        param2.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(param2);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithCredentials() {

        WorkflowRequest request = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<>();

        RequestParameter credentialParam = new RequestParameter();
        credentialParam.setName(WFConstant.CREDENTIAL);
        credentialParam.setValue("secret");
        credentialParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(credentialParam);

        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithTenantDomain() {

        WorkflowRequest request = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<>();

        RequestParameter tenantParam = new RequestParameter();
        tenantParam.setName(WFConstant.TENANT_DOMAIN_PARAM_NAME);
        tenantParam.setValue("carbon.super");
        tenantParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(tenantParam);

        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithInvalidClaimsMap() {

        WorkflowRequest request = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<>();

        // Add claims parameter with invalid type (string instead of map)
        RequestParameter claimsParam = new RequestParameter();
        claimsParam.setName(WFConstant.CLAIMS_PROPERTY_NAME);
        claimsParam.setValue("invalid_claims_string"); // This will cause ClassCastException
        claimsParam.setValueType(WorkflowDataType.STRING_STRING_MAP_TYPE);
        parameters.add(claimsParam);

        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithNullValues() {

        WorkflowRequest request = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<>();

        RequestParameter nullParam = new RequestParameter();
        nullParam.setName("nullParam");
        nullParam.setValue(null);
        nullParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(nullParam);

        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithEmptyClaimValues() {

        WorkflowRequest request = new WorkflowRequest();
        List<RequestParameter> parameters = new ArrayList<>();

        // Add claims parameter with empty values
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put("http://wso2.org/claims/emailaddress", ""); // Empty value
        claimsMap.put("http://wso2.org/claims/givenname", null); // Null value

        RequestParameter claimsParam = new RequestParameter();
        claimsParam.setName(WFConstant.CLAIMS_PROPERTY_NAME);
        claimsParam.setValue(claimsMap);
        claimsParam.setValueType(WorkflowDataType.STRING_STRING_MAP_TYPE);
        parameters.add(claimsParam);

        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private List<LocalClaim> createMockLocalClaims() {

        List<LocalClaim> localClaims = new ArrayList<>();

        LocalClaim emailClaim = mock(LocalClaim.class);
        when(emailClaim.getClaimURI()).thenReturn("http://wso2.org/claims/emailaddress");
        when(emailClaim.getClaimProperty("DisplayName")).thenReturn("Email");
        localClaims.add(emailClaim);

        LocalClaim firstNameClaim = mock(LocalClaim.class);
        when(firstNameClaim.getClaimURI()).thenReturn("http://wso2.org/claims/givenname");
        when(firstNameClaim.getClaimProperty("DisplayName")).thenReturn("First Name");
        localClaims.add(firstNameClaim);

        return localClaims;
    }
}
