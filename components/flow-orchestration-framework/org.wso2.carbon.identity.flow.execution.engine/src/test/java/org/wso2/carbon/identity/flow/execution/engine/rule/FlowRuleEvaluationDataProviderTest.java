/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.rule;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationDataProviderException;
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.Group;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.UserRealm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.expectThrows;

/**
 * Tests what a flow condition is evaluated against.
 */
public class FlowRuleEvaluationDataProviderTest {

    private static final String TENANT = "carbon.super";
    private static final String COUNTRY = "http://wso2.org/claims/country";
    private static final String GROUPS = "http://wso2.org/claims/groups";
    private static final String SEPARATOR = ",,,";

    private final RegistrationFlowRuleEvaluationDataProvider provider =
            new RegistrationFlowRuleEvaluationDataProvider();

    private ClaimMetadataManagementService claimService;
    private AbstractUserStoreManager userStoreManager;
    private MockedStatic<FrameworkUtils> frameworkUtils;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        claimService = mock(ClaimMetadataManagementService.class);
        when(claimService.getLocalClaim(anyString(), anyString())).thenReturn(Optional.empty());
        when(claimService.getLocalClaim(eq(GROUPS), anyString())).thenReturn(Optional.of(claim(GROUPS, true)));
        when(claimService.getLocalClaim(eq(COUNTRY), anyString())).thenReturn(Optional.of(claim(COUNTRY, false)));
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(claimService);

        userStoreManager = mock(AbstractUserStoreManager.class);
        UserRealm userRealm = mock(UserRealm.class);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        RealmService realmService = mock(RealmService.class);
        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        FlowExecutionEngineDataHolder.getInstance().setRealmService(realmService);

        frameworkUtils = mockStatic(FrameworkUtils.class);
        frameworkUtils.when(() -> FrameworkUtils.getMultiAttributeSeparator(any())).thenReturn(SEPARATOR);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(-1234);
    }

    @AfterMethod
    public void tearDown() {

        frameworkUtils.close();
        identityTenantUtil.close();
        FlowExecutionEngineDataHolder.getInstance().setClaimMetadataManagementService(null);
        FlowExecutionEngineDataHolder.getInstance().setRealmService(null);
        FlowExecutionEngineDataHolder.getInstance().setRoleManagementService(null);
    }

    @Test
    public void testMultiValuedCollectedClaimIsSplitIntoASet() throws Exception {

        FlowUser user = new FlowUser();
        user.addClaim(GROUPS, "admin" + SEPARATOR + "staff");

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_COLLECTED_CLAIMS, GROUPS),
                context(user));

        assertEquals(value.getValueType(), ValueType.LIST);
        assertEquals(value.getValue(), Arrays.asList("admin", "staff"));
        assertEquals(value.getQualifier(), GROUPS);
    }

    @Test
    public void testMultiValuedStoredClaimIsSplitIntoASet() throws Exception {

        FlowUser user = new FlowUser();
        user.setUserId("user-id");
        Map<String, String> stored = new HashMap<>();
        stored.put(GROUPS, "admin" + SEPARATOR + "staff");
        when(userStoreManager.getUserClaimValuesWithID(eq("user-id"), any(String[].class), any()))
                .thenReturn(stored);

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_CLAIMS, GROUPS),
                context(user));

        assertEquals(value.getValueType(), ValueType.LIST);
        assertEquals(value.getValue(), Arrays.asList("admin", "staff"));
    }

    @Test
    public void testSingleValuedClaimStaysAString() throws Exception {

        FlowUser user = new FlowUser();
        user.addClaim(COUNTRY, "LK" + SEPARATOR + "IN");

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_COLLECTED_CLAIMS, COUNTRY),
                context(user));

        assertEquals(value.getValueType(), ValueType.STRING);
        assertEquals(value.getValue(), "LK" + SEPARATOR + "IN");
    }

    /**
     * An absent multi-valued claim is a set that is not there rather than an empty one: isEmpty holds on it, notIn
     * does not, and it is still read as multi-valued.
     */
    @Test
    public void testAbsentMultiValuedClaimIsAnAbsentSet() throws Exception {

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_COLLECTED_CLAIMS, GROUPS),
                context(new FlowUser()));

        assertEquals(value.getValueType(), ValueType.LIST);
        assertNull(value.getValue());
    }

    /**
     * An empty value is no value, so it never matches a comparison and isBlank holds on it.
     */
    @Test
    public void testEmptyCollectedClaimIsNoValue() throws Exception {

        FlowUser user = new FlowUser();
        user.addClaim(COUNTRY, "");

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_COLLECTED_CLAIMS, COUNTRY),
                context(user));

        assertEquals(value.getValueType(), ValueType.STRING);
        assertNull(value.getValue());
    }

    /**
     * What is stored for a user the flow has not identified yet cannot be known, which is not the same as nothing
     * being stored, so it is an error rather than an empty value.
     */
    @Test
    public void testStoredClaimBeforeTheUserIsIdentifiedIsAnError() {

        expectThrows(RuleEvaluationDataProviderException.class, () -> resolve(
                field(AbstractFlowRuleEvaluationDataProvider.USER_CLAIMS, COUNTRY), context(new FlowUser())));
    }

    @Test
    public void testStoredClaimThatCannotBeReadIsAnError() throws Exception {

        when(userStoreManager.getUserClaimValuesWithID(anyString(), any(), any()))
                .thenThrow(new UserStoreException("store is down"));

        expectThrows(RuleEvaluationDataProviderException.class, () -> resolve(
                field(AbstractFlowRuleEvaluationDataProvider.USER_CLAIMS, COUNTRY), context(existingUser())));
    }

    @Test
    public void testApplicationIsTheIdOfTheApplicationTheFlowRunsFor() throws Exception {

        FlowExecutionContext context = context(new FlowUser());
        context.setApplicationId("app-1");

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.APPLICATION, null), context);

        assertEquals(value.getValueType(), ValueType.REFERENCE);
        assertEquals(value.getValue(), "app-1");
    }

    @Test
    public void testUserDomainIsTheUsersOwnStore() throws Exception {

        FlowUser user = existingUser();
        user.setUserStoreDomain("ldap");

        assertEquals(resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_DOMAIN, null), context(user))
                .getValue(), "LDAP");
    }

    /**
     * A user with no recorded domain is in the primary store, named as the tenant names it -- DEFAULT on
     * Asgardeo, for instance.
     */
    @Test
    public void testUserDomainFallsBackToThePrimaryStore() throws Exception {

        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn("default");
        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);

        assertEquals(resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_DOMAIN, null),
                context(existingUser())).getValue(), "DEFAULT");
    }

    @Test
    public void testUserDomainBeforeTheUserIsIdentifiedIsAnError() {

        expectThrows(RuleEvaluationDataProviderException.class, () -> resolve(
                field(AbstractFlowRuleEvaluationDataProvider.USER_DOMAIN, null), context(new FlowUser())));
    }

    @Test
    public void testGroupsAreTheIdsOfTheUsersGroups() throws Exception {

        List<Group> groups = Arrays.asList(group("group-1"), group("group-2"));
        when(userStoreManager.getGroupListOfUser("user-1", null, null)).thenReturn(groups);

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_GROUPS, null),
                context(existingUser()));

        assertEquals(value.getValueType(), ValueType.LIST);
        assertEquals(value.getValue(), Arrays.asList("group-1", "group-2"));
    }

    /**
     * A role reached through a group is as assigned as one given directly, and each role is counted once.
     */
    @Test
    public void testRolesIncludeThoseAssignedThroughGroups() throws Exception {

        List<Group> groups = Collections.singletonList(group("group-1"));
        when(userStoreManager.getGroupListOfUser("user-1", null, null)).thenReturn(groups);
        RoleManagementService roleService = mock(RoleManagementService.class);
        when(roleService.getRoleIdListOfUser("user-1", TENANT)).thenReturn(Collections.singletonList("role-1"));
        when(roleService.getRoleIdListOfGroups(Collections.singletonList("group-1"), TENANT))
                .thenReturn(Arrays.asList("role-2", "role-1"));
        FlowExecutionEngineDataHolder.getInstance().setRoleManagementService(roleService);

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_ROLES, null),
                context(existingUser()));

        assertEquals(value.getValueType(), ValueType.LIST);
        assertEquals(value.getValue(), Arrays.asList("role-1", "role-2"));
    }

    /**
     * Before the user is identified, the groups and roles cannot be known. Reading them as empty would let isEmpty
     * hold for a user who may have many, so it is an error.
     */
    @Test
    public void testGroupsAndRolesBeforeTheUserIsIdentifiedAreAnError() {

        FlowExecutionEngineDataHolder.getInstance().setRoleManagementService(mock(RoleManagementService.class));

        for (String name : new String[]{AbstractFlowRuleEvaluationDataProvider.USER_GROUPS,
                AbstractFlowRuleEvaluationDataProvider.USER_ROLES}) {
            expectThrows(RuleEvaluationDataProviderException.class,
                    () -> resolve(field(name, null), context(new FlowUser())));
        }
    }

    /**
     * A user with no groups has an empty set, so isEmpty holds and notIn holds for any group.
     */
    @Test
    public void testIdentifiedUserWithNoGroupsHasAnEmptySet() throws Exception {

        when(userStoreManager.getGroupListOfUser("user-1", null, null)).thenReturn(Collections.emptyList());

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_GROUPS, null),
                context(existingUser()));

        assertEquals(value.getValueType(), ValueType.LIST);
        assertEquals(value.getValue(), Collections.emptyList());
    }

    @Test
    public void testGroupsThatCannotBeReadAreAnError() throws Exception {

        when(userStoreManager.getGroupListOfUser("user-1", null, null))
                .thenThrow(new UserStoreException("store is down"));

        expectThrows(RuleEvaluationDataProviderException.class, () -> resolve(
                field(AbstractFlowRuleEvaluationDataProvider.USER_GROUPS, null), context(existingUser())));
    }

    @Test
    public void testUnknownFieldIsAnError() {

        expectThrows(RuleEvaluationDataProviderException.class, () -> resolve(
                new Field("user.unknown", ValueType.STRING), context(new FlowUser())));
    }

    private FieldValue resolve(Field field, FlowExecutionContext context) throws Exception {

        Map<String, Object> contextData = new HashMap<>();
        contextData.put(AbstractFlowRuleEvaluationDataProvider.FLOW_EXECUTION_CONTEXT, context);
        List<FieldValue> values = provider.getEvaluationData(
                new RuleEvaluationContext("rule", Collections.singletonList(field)),
                new FlowContext(FlowType.REGISTRATION, contextData), TENANT);
        assertEquals(values.size(), 1);
        return values.get(0);
    }

    private static FlowExecutionContext context(FlowUser user) {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT);
        context.setFlowUser(user);
        return context;
    }

    private static Field field(String name, String key) {

        return new Field(name, key, ValueType.STRING);
    }

    private static FlowUser existingUser() {

        FlowUser user = new FlowUser();
        user.setUserId("user-1");
        return user;
    }

    private static Group group(String id) {

        Group group = mock(Group.class);
        when(group.getGroupID()).thenReturn(id);
        return group;
    }

    private static LocalClaim claim(String uri, boolean multiValued) {

        LocalClaim claim = new LocalClaim(uri);
        claim.setClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY, String.valueOf(multiValued));
        return claim;
    }
}
