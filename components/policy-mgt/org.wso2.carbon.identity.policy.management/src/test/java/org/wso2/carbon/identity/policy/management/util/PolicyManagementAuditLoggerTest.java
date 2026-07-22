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

package org.wso2.carbon.identity.policy.management.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.policy.management.internal.util.PolicyManagementAuditLogger;
import org.wso2.carbon.utils.AuditLog;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test class for PolicyManagementAuditLogger class.
 */
public class PolicyManagementAuditLoggerTest {

    private static final String POLICY_ID = "policy-123";
    private static final String POLICY_NAME = "Test Policy";
    private static final String TENANT_DOMAIN = "carbon.super";

    private PolicyManagementAuditLogger auditLogger;
    private Policy policy;
    private MockedStatic<CarbonContext> carbonContextMockedStatic;
    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<LoggerUtils> loggerUtilsMockedStatic;

    /**
     * Set up mocks and test data.
     */
    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        auditLogger = new PolicyManagementAuditLogger();
        carbonContextMockedStatic = mockStatic(CarbonContext.class);
        identityUtil = mockStatic(IdentityUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        loggerUtilsMockedStatic = mockStatic(LoggerUtils.class);

        CarbonContext carbonContext = mock(CarbonContext.class);
        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
        when(carbonContext.getUsername()).thenReturn("admin");
        when(carbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        identityUtil.when(() -> IdentityUtil.getInitiatorId("admin", TENANT_DOMAIN)).thenReturn("admin@carbon.super");
        loggerUtilsMockedStatic.when(() -> LoggerUtils.getInitiatorType(any())).thenReturn("USER");
        loggerUtilsMockedStatic.when(() -> LoggerUtils.getMaskedContent(any())).thenAnswer(i -> "****");
        loggerUtilsMockedStatic.when(() -> LoggerUtils.jsonObjectToMap(any(JSONObject.class))).thenCallRealMethod();
        loggerUtilsMockedStatic.when(() -> LoggerUtils.triggerAuditLogEvent(any(AuditLog.AuditLogBuilder.class)))
                .then(invocation -> null);

        policy = new Policy.Builder()
                .id(POLICY_ID)
                .name(POLICY_NAME)
                .tenantDomain(TENANT_DOMAIN)
                .resources(Collections.singletonList(new RulePolicyResource.Builder()
                        .target("android")
                        .resourceId("rule-1")
                        .build()))
                .build();
    }

    /**
     * Release static mocks.
     */
    @AfterMethod
    public void tearDown() {

        carbonContextMockedStatic.close();
        identityUtil.close();
        identityTenantUtil.close();
        loggerUtilsMockedStatic.close();
    }

    /**
     * Test audit log entry creation for a policy, including the resource summary.
     */
    @Test
    public void testCreateAuditLogEntryForPolicy() throws Exception {

        JSONObject data = invokeCreateAuditLogEntry(policy);
        Assert.assertEquals(data.get("Id"), POLICY_ID);
        Assert.assertEquals(data.get("Name"), POLICY_NAME);
        Assert.assertEquals(data.get("TenantDomain"), TENANT_DOMAIN);

        JSONArray resources = (JSONArray) data.get("Resources");
        Assert.assertEquals(resources.length(), 1);
        JSONObject resourceEntry = resources.getJSONObject(0);
        Assert.assertEquals(resourceEntry.get("target"), "android");
        Assert.assertEquals(resourceEntry.get("type"), ResourceType.RULE.name());
        // The rule payload / resource ID must never be present in the audit data.
        Assert.assertFalse(resourceEntry.has("resourceId"));
        Assert.assertFalse(resourceEntry.has("rule"));
    }

    /**
     * Test printAuditLog for the add operation.
     */
    @Test
    public void testPrintAuditLogAdd() {

        auditLogger.printAuditLog(PolicyManagementAuditLogger.Operation.ADD, policy);
        // No exception means pass.
    }

    /**
     * Test printAuditLog for the update operation.
     */
    @Test
    public void testPrintAuditLogUpdate() {

        auditLogger.printAuditLog(PolicyManagementAuditLogger.Operation.UPDATE, policy);
        // No exception means pass.
    }

    /**
     * Test printAuditLog for the delete operation.
     */
    @Test
    public void testPrintAuditLogDelete() {

        auditLogger.printAuditLog(PolicyManagementAuditLogger.Operation.DELETE, policy);
        // No exception means pass.
    }

    /**
     * Test that a policy with no resources produces an empty resource array rather than failing.
     */
    @Test
    public void testCreateAuditLogEntryForPolicyWithNoResources() throws Exception {

        Policy emptyResourcePolicy = new Policy.Builder()
                .id(POLICY_ID)
                .name(POLICY_NAME)
                .tenantDomain(TENANT_DOMAIN)
                .resources(Collections.emptyList())
                .build();
        JSONObject data = invokeCreateAuditLogEntry(emptyResourcePolicy);
        JSONArray resources = (JSONArray) data.get("Resources");
        Assert.assertEquals(resources.length(), 0);
    }

    // Helper to access private createAuditLogEntry(Policy) via reflection.
    private JSONObject invokeCreateAuditLogEntry(Policy policy) throws Exception {

        java.lang.reflect.Method m =
                PolicyManagementAuditLogger.class.getDeclaredMethod("createAuditLogEntry", Policy.class);
        m.setAccessible(true);
        return (JSONObject) m.invoke(auditLogger, policy);
    }
}
