/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.rule;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataException;
import org.wso2.carbon.identity.rule.metadata.api.model.FieldDefinition;
import org.wso2.carbon.identity.rule.metadata.api.model.FlowType;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link ApprovalWorkflowMetadataProvider}.
 */
@WithCarbonHome
public class ApprovalWorkflowMetadataProviderTest {

    @Mock
    private ClaimMetadataManagementService mockClaimService;

    private AutoCloseable mockCloseable;
    private ApprovalWorkflowMetadataProvider provider;
    private static final String TENANT_DOMAIN = "carbon.super";

    @BeforeClass
    public void initRuleMetadata() throws RuleMetadataConfigException {

        RuleMetadataConfigFactory.load();
    }

    @BeforeMethod
    public void setUp() {

        mockCloseable = MockitoAnnotations.openMocks(this);
        WorkflowServiceDataHolder.getInstance().setClaimMetadataManagementService(mockClaimService);
        provider = new ApprovalWorkflowMetadataProvider();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        WorkflowServiceDataHolder.getInstance().setClaimMetadataManagementService(null);
        mockCloseable.close();
    }

    @Test
    public void testGetExpressionMeta_nonApprovalFlowType_returnsEmptyList() throws RuleMetadataException {

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.PRE_ISSUE_ACCESS_TOKEN, TENANT_DOMAIN);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetExpressionMeta_preUpdatePasswordFlowType_returnsEmptyList() throws RuleMetadataException {

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.PRE_UPDATE_PASSWORD, TENANT_DOMAIN);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetExpressionMeta_noClaims_returnsOnlyStaticFields()
            throws RuleMetadataException, ClaimMetadataException {

        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Collections.emptyList());

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        assertNotNull(result);
        // Only the 11 static fields from WorkflowRuleFieldRegistry should be present.
        int staticFieldCount = WorkflowRuleFieldRegistry.FIELDS.size();
        assertEquals(result.size(), staticFieldCount);
    }

    @Test
    public void testGetExpressionMeta_withNullClaimsResult_returnsOnlyStaticFields()
            throws RuleMetadataException, ClaimMetadataException {

        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(null);

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        int staticFieldCount = WorkflowRuleFieldRegistry.FIELDS.size();
        assertEquals(result.size(), staticFieldCount);
    }

    @Test
    public void testGetExpressionMeta_withTwoClaims_returnsStaticFieldsPlusFourDynamicFields()
            throws RuleMetadataException, ClaimMetadataException {

        LocalClaim claim1 = new LocalClaim("http://wso2.org/claims/givenname");
        LocalClaim claim2 = new LocalClaim("http://wso2.org/claims/lastname");
        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Arrays.asList(claim1, claim2));

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        // 11 static fields + 2 claims * 2 prefixes (user. and initiator.) = 15.
        int expectedSize = WorkflowRuleFieldRegistry.FIELDS.size() + (2 * 2);
        assertEquals(result.size(), expectedSize);
    }

    @Test
    public void testGetExpressionMeta_claimFieldsContainUserAndInitiatorPrefixes()
            throws RuleMetadataException, ClaimMetadataException {

        LocalClaim claim = new LocalClaim("http://wso2.org/claims/emailaddress");
        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Collections.singletonList(claim));

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        // Collect claim-related field names.
        long userClaimFields = result.stream()
                .filter(fd -> fd.getField().getName().startsWith("user.http://wso2.org/claims/"))
                .count();
        long initiatorClaimFields = result.stream()
                .filter(fd -> fd.getField().getName().startsWith("initiator.http://wso2.org/claims/"))
                .count();

        assertEquals(userClaimFields, 1);
        assertEquals(initiatorClaimFields, 1);
    }

    @Test
    public void testGetExpressionMeta_claimWithDisplayName_usesDisplayName()
            throws RuleMetadataException, ClaimMetadataException {

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.DISPLAY_NAME_PROPERTY, "Email Address");
        LocalClaim claim = new LocalClaim("http://wso2.org/claims/emailaddress",
                Collections.emptyList(), claimProperties);
        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Collections.singletonList(claim));

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        // Find the user-prefixed claim field.
        FieldDefinition userClaimField = result.stream()
                .filter(fd -> fd.getField().getName().startsWith("user.http://wso2.org/claims/emailaddress"))
                .findFirst()
                .orElse(null);

        assertNotNull(userClaimField);
        // Display name should be prefixed with "user." and contain the claim's display name.
        assertTrue(userClaimField.getField().getDisplayName().contains("Email Address"));
    }

    @Test
    public void testGetExpressionMeta_claimWithoutDisplayName_fallsBackToClaimUri()
            throws RuleMetadataException, ClaimMetadataException {

        // LocalClaim without display name property.
        LocalClaim claim = new LocalClaim("http://wso2.org/claims/country");
        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Collections.singletonList(claim));

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        FieldDefinition userClaimField = result.stream()
                .filter(fd -> fd.getField().getName().startsWith("user.http://wso2.org/claims/country"))
                .findFirst()
                .orElse(null);

        assertNotNull(userClaimField);
        // Display name should fall back to the claim URI.
        assertTrue(userClaimField.getField().getDisplayName().contains("http://wso2.org/claims/country"));
    }

    @Test(expectedExceptions = RuleMetadataException.class)
    public void testGetExpressionMeta_claimServiceThrowsException_throwsRuleMetadataException()
            throws RuleMetadataException, ClaimMetadataException {

        when(mockClaimService.getLocalClaims(anyString())).thenThrow(
                new ClaimMetadataException("Error fetching claims"));

        provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);
    }

    @Test
    public void testGetExpressionMeta_claimFieldsHaveEqualsNotEqualsContainsOperators()
            throws RuleMetadataException, ClaimMetadataException {

        LocalClaim claim = new LocalClaim("http://wso2.org/claims/mobile");
        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Collections.singletonList(claim));

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        FieldDefinition claimField = result.stream()
                .filter(fd -> fd.getField().getName().startsWith("user.http://wso2.org/claims/mobile"))
                .findFirst()
                .orElse(null);

        assertNotNull(claimField);
        assertEquals(claimField.getOperators().size(), 3);
        List<String> opNames = claimField.getOperators().stream()
                .map(op -> op.getName())
                .collect(java.util.stream.Collectors.toList());
        assertTrue(opNames.contains("equals"));
        assertTrue(opNames.contains("notEquals"));
        assertTrue(opNames.contains("contains"));
    }

    @Test
    public void testGetExpressionMeta_staticFieldsAreFirstInList()
            throws RuleMetadataException, ClaimMetadataException {

        LocalClaim claim = new LocalClaim("http://wso2.org/claims/username");
        when(mockClaimService.getLocalClaims(TENANT_DOMAIN)).thenReturn(Collections.singletonList(claim));

        List<FieldDefinition> result = provider.getExpressionMeta(FlowType.APPROVAL_WORKFLOW, TENANT_DOMAIN);

        // The first N fields should be the static fields.
        int staticFieldCount = WorkflowRuleFieldRegistry.FIELDS.size();
        List<String> staticFieldNames = new java.util.ArrayList<>(WorkflowRuleFieldRegistry.FIELDS.keySet());
        for (int i = 0; i < staticFieldCount; i++) {
            assertEquals(result.get(i).getField().getName(), staticFieldNames.get(i));
        }
    }
}
