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
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.Group;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for {@link WorkFlowRuleEvaluationDataProvider}.
 */
@WithCarbonHome
public class WorkFlowRuleEvaluationDataProviderTest {

    @Mock
    private AbstractUserStoreManager mockUserStoreManager;

    @Mock
    private RoleManagementService mockRoleManagementService;

    @Mock
    private UserRealm mockUserRealm;

    private MockedStatic<CarbonContext> carbonContextMockedStatic;
    private CarbonContext mockCarbonContext;
    private AutoCloseable mockCloseable;
    private WorkFlowRuleEvaluationDataProvider provider;

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_USER_ID = "user-id-123";
    private static final String TEST_ROLE_ID = "role-id-456";
    private static final String TEST_USER_STORE_DOMAIN = "PRIMARY";

    @BeforeMethod
    public void setUp() throws Exception {

        mockCloseable = MockitoAnnotations.openMocks(this);
        carbonContextMockedStatic = mockStatic(CarbonContext.class);
        mockCarbonContext = mock(CarbonContext.class);
        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
        when(mockCarbonContext.getUserRealm()).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);

        WorkflowServiceDataHolder.getInstance().setRoleManagementService(mockRoleManagementService);

        provider = new WorkFlowRuleEvaluationDataProvider();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        WorkflowServiceDataHolder.getInstance().setRoleManagementService(null);
        if (carbonContextMockedStatic != null) {
            carbonContextMockedStatic.close();
        }
        mockCloseable.close();
    }

    // ---- getSupportedFlowType ----

    @Test
    public void testGetSupportedFlowType_returnsApprovalWorkflow() {

        assertEquals(provider.getSupportedFlowType(), FlowType.APPROVAL_WORKFLOW);
    }

    // ---- user.domain ----

    @Test
    public void testGetEvaluationData_userDomain_returnsValueFromContext()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("User Store Domain", TEST_USER_STORE_DOMAIN);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("user.domain", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-1",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getName(), "user.domain");
        assertEquals(result.get(0).getValue(), TEST_USER_STORE_DOMAIN);
        assertEquals(result.get(0).getValueType(), ValueType.STRING);
    }

    @Test
    public void testGetEvaluationData_userDomain_blankInContext_returnsNullValue()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("User Store Domain", "");
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("user.domain", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-1",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertNull(result.get(0).getValue());
    }

    // ---- role.id ----

    @Test
    public void testGetEvaluationData_roleId_returnsValueFromContext()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Role ID", TEST_ROLE_ID);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("role.id", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-2",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getName(), "role.id");
        assertEquals(result.get(0).getValue(), TEST_ROLE_ID);
    }

    // ---- role.hasAssignedUsers ----

    @Test
    public void testGetEvaluationData_roleHasAssignedUsers_withUsersInContext_returnsTrue()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Users to be Added", Arrays.asList("user1", "user2"));
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("role.hasAssignedUsers", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-3",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getValue(), "true");
    }

    @Test
    public void testGetEvaluationData_roleHasAssignedUsers_noUsersInContext_returnsFalse()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Users to be Added", Collections.emptyList());
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("role.hasAssignedUsers", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-3",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.get(0).getValue(), "false");
    }

    // ---- role.hasUnassignedUsers ----

    @Test
    public void testGetEvaluationData_roleHasUnassignedUsers_withUsersInContext_returnsTrue()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Users to be Deleted", Arrays.asList("user3"));
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("role.hasUnassignedUsers", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-4",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.get(0).getValue(), "true");
    }

    @Test
    public void testGetEvaluationData_roleHasUnassignedUsers_noUsersInContext_returnsFalse()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("role.hasUnassignedUsers", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-4",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.get(0).getValue(), "false");
    }

    // ---- role.audience ----

    @Test
    public void testGetEvaluationData_roleAudience_audienceIdInContext_returnsDirectly()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Audience ID", "app-audience-789");
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("role.audience", ValueType.REFERENCE);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-5",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getValue(), "app-audience-789");
        assertEquals(result.get(0).getValueType(), ValueType.REFERENCE);
    }

    @Test
    public void testGetEvaluationData_roleAudience_fetchesFromRoleServiceWhenNotInContext()
            throws RuleEvaluationDataProviderException, IdentityRoleManagementException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Role ID", TEST_ROLE_ID);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        RoleBasicInfo mockRoleInfo = mock(RoleBasicInfo.class);
        when(mockRoleInfo.getAudienceId()).thenReturn("org-audience-999");
        when(mockRoleManagementService.getRoleBasicInfoById(TEST_ROLE_ID, TENANT_DOMAIN))
                .thenReturn(mockRoleInfo);

        Field field = new Field("role.audience", ValueType.REFERENCE);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-5",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.get(0).getValue(), "org-audience-999");
        assertEquals(result.get(0).getValueType(), ValueType.REFERENCE);
    }

    @Test
    public void testGetEvaluationData_roleAudience_noRoleIdInContext_returnsNull()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("role.audience", ValueType.REFERENCE);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-5",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertNull(result.get(0).getValue());
    }

    @Test(expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationData_roleAudience_roleServiceThrowsException_throwsDataProviderException()
            throws RuleEvaluationDataProviderException, IdentityRoleManagementException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Role ID", TEST_ROLE_ID);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        when(mockRoleManagementService.getRoleBasicInfoById(anyString(), anyString()))
                .thenThrow(new IdentityRoleManagementException("Error", "DB error"));

        Field field = new Field("role.audience", ValueType.REFERENCE);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-5",
                Collections.singletonList(field));

        provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);
    }

    // ---- user.roles ----

    @Test
    public void testGetEvaluationData_userRoles_returnsRoleIdList()
            throws Exception {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        when(mockUserStoreManager.getUserIDFromUserName(TEST_USERNAME)).thenReturn(TEST_USER_ID);
        when(mockRoleManagementService.getRoleIdListOfUser(TEST_USER_ID, TENANT_DOMAIN))
                .thenReturn(Arrays.asList("role-id-1", "role-id-2"));

        Field field = new Field("user.roles", ValueType.LIST);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-6",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getValueType(), ValueType.LIST);
        List<String> roles = (List<String>) result.get(0).getValue();
        assertEquals(roles.size(), 2);
        assertEquals(roles.get(0), "role-id-1");
    }

    @Test
    public void testGetEvaluationData_userRoles_userIdBlank_returnsEmptyList()
            throws Exception {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        when(mockUserStoreManager.getUserIDFromUserName(TEST_USERNAME)).thenReturn(null);

        Field field = new Field("user.roles", ValueType.LIST);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-6",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        List<String> roles = (List<String>) result.get(0).getValue();
        assertEquals(roles.size(), 0);
    }

    @Test(expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationData_userRoles_userStoreException_throwsDataProviderException()
            throws Exception {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        when(mockUserStoreManager.getUserIDFromUserName(TEST_USERNAME))
                .thenThrow(new org.wso2.carbon.user.core.UserStoreException("DB error"));

        Field field = new Field("user.roles", ValueType.LIST);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-6",
                Collections.singletonList(field));

        provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);
    }

    // ---- user.groups ----

    @Test
    public void testGetEvaluationData_userGroups_returnsGroupIdList()
            throws Exception {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        when(mockUserStoreManager.getUserIDFromUserName(TEST_USERNAME)).thenReturn(TEST_USER_ID);

        Group mockGroup1 = mock(Group.class);
        Group mockGroup2 = mock(Group.class);
        when(mockGroup1.getGroupID()).thenReturn("group-id-1");
        when(mockGroup2.getGroupID()).thenReturn("group-id-2");
        when(mockUserStoreManager.getGroupListOfUser(eq(TEST_USER_ID), any(), any()))
                .thenReturn(Arrays.asList(mockGroup1, mockGroup2));

        Field field = new Field("user.groups", ValueType.LIST);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-7",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getValueType(), ValueType.LIST);
        List<String> groups = (List<String>) result.get(0).getValue();
        assertEquals(groups.size(), 2);
        assertEquals(groups.get(0), "group-id-1");
    }

    @Test
    public void testGetEvaluationData_userGroups_userIdBlank_returnsEmptyList()
            throws Exception {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        when(mockUserStoreManager.getUserIDFromUserName(TEST_USERNAME)).thenReturn("");

        Field field = new Field("user.groups", ValueType.LIST);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-7",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        List<String> groups = (List<String>) result.get(0).getValue();
        assertEquals(groups.size(), 0);
    }

    // ---- Claim URI field ----

    @Test
    public void testGetEvaluationData_claimField_valueFromContextClaimsMap_returnsValue()
            throws RuleEvaluationDataProviderException {

        // Field names are prefixed with "user."; the nested claims map uses bare claim URIs as keys.
        String bareClaimUri = "http://wso2.org/claims/emailaddress";
        String fieldName = "user." + bareClaimUri;
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(bareClaimUri, "testuser@example.com");

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("Claims", claimsMap);
        contextData.put("eventType", "ADD_USER");
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field(fieldName, ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-8",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getName(), fieldName);
        assertEquals(result.get(0).getValue(), "testuser@example.com");
    }

    @Test
    public void testGetEvaluationData_claimField_fetchesFromUserStoreWhenNotInContext()
            throws Exception {

        // Field names are prefixed with "user."; the user-store result map uses bare claim URIs as keys.
        String bareClaimUri = "http://wso2.org/claims/mobile";
        String fieldName = "user." + bareClaimUri;
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("eventType", "ADD_USER");
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Map<String, String> userStoreClaimsResult = new HashMap<>();
        userStoreClaimsResult.put(bareClaimUri, "+94771234567");
        when(mockUserStoreManager.getUserClaimValues(
                eq(TEST_USERNAME), any(String[].class), anyString()))
                .thenReturn(userStoreClaimsResult);

        Field field = new Field(fieldName, ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-9",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getValue(), "+94771234567");
    }

    @Test
    public void testGetEvaluationData_claimField_nonAddUserEvent_doesNotFetchFromUserStore()
            throws RuleEvaluationDataProviderException {

        // Field names are prefixed with "user."; no fetch should happen for non ADD_USER events.
        String fieldName = "user.http://wso2.org/claims/givenname";
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("eventType", "ADD_ROLE");
        contextData.put("Username", TEST_USERNAME);
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field(fieldName, ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-10",
                Collections.singletonList(field));

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 1);
        // Value should be null since no claims in context and eventType is not ADD_USER.
        assertNull(result.get(0).getValue());
    }

    // ---- Unsupported field ----

    @Test(expectedExceptions = RuleEvaluationDataProviderException.class)
    public void testGetEvaluationData_unsupportedField_throwsDataProviderException()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        Field field = new Field("unsupported.field.xyz", ValueType.STRING);
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-11",
                Collections.singletonList(field));

        provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);
    }

    // ---- Multiple fields ----

    @Test
    public void testGetEvaluationData_multipleNonClaimFields_allResolved()
            throws RuleEvaluationDataProviderException {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("User Store Domain", "PRIMARY");
        contextData.put("Role ID", TEST_ROLE_ID);
        contextData.put("Users to be Added", Arrays.asList("user1"));
        contextData.put("Users to be Deleted", Collections.emptyList());
        FlowContext flowContext = new FlowContext(FlowType.APPROVAL_WORKFLOW, contextData);

        List<Field> fields = Arrays.asList(
                new Field("user.domain", ValueType.STRING),
                new Field("role.id", ValueType.STRING),
                new Field("role.hasAssignedUsers", ValueType.STRING),
                new Field("role.hasUnassignedUsers", ValueType.STRING)
        );
        RuleEvaluationContext ruleContext = new RuleEvaluationContext("rule-12", fields);

        List<FieldValue> result = provider.getEvaluationData(ruleContext, flowContext, TENANT_DOMAIN);

        assertEquals(result.size(), 4);
        assertEquals(result.get(0).getName(), "user.domain");
        assertEquals(result.get(0).getValue(), "PRIMARY");
        assertEquals(result.get(1).getName(), "role.id");
        assertEquals(result.get(1).getValue(), TEST_ROLE_ID);
        assertEquals(result.get(2).getName(), "role.hasAssignedUsers");
        assertEquals(result.get(2).getValue(), "true");
        assertEquals(result.get(3).getName(), "role.hasUnassignedUsers");
        assertEquals(result.get(3).getValue(), "false");
    }
}
