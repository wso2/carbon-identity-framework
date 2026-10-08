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
import org.wso2.carbon.identity.rule.evaluation.api.model.Field;
import org.wso2.carbon.identity.rule.evaluation.api.model.FieldValue;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationContext;
import org.wso2.carbon.identity.rule.evaluation.api.model.ValueType;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
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
     * An empty set would let notIn hold for a user with no value at all, so an absent multi-valued
     * claim has to stay an absent scalar.
     */
    @Test
    public void testAbsentMultiValuedClaimStaysAnAbsentScalar() throws Exception {

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_COLLECTED_CLAIMS, GROUPS),
                context(new FlowUser()));

        assertEquals(value.getValueType(), ValueType.STRING);
        assertNull(value.getValue());
    }

    @Test
    public void testStoredClaimBeforeTheUserExistsIsAbsent() throws Exception {

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.USER_CLAIMS, COUNTRY),
                context(new FlowUser()));

        assertNull(value.getValue());
    }

    @Test
    public void testFlowPropertyIsReadFromTheContext() throws Exception {

        FlowExecutionContext context = context(new FlowUser());
        context.setProperty("riskLevel", "high");

        FieldValue value = resolve(field(AbstractFlowRuleEvaluationDataProvider.FLOW_PROPERTIES, "riskLevel"),
                context);

        assertEquals(value.getValue(), "high");
    }

    @Test
    public void testUnknownFieldIsAnsweredAsAbsent() throws Exception {

        FieldValue value = resolve(new Field("user.unknown", ValueType.STRING), context(new FlowUser()));

        assertEquals(value.getName(), "user.unknown");
        assertNull(value.getValue());
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

    private static LocalClaim claim(String uri, boolean multiValued) {

        LocalClaim claim = new LocalClaim(uri);
        claim.setClaimProperty(ClaimConstants.MULTI_VALUED_PROPERTY, String.valueOf(multiValued));
        return claim;
    }
}
