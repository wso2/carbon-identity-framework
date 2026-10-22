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

package org.wso2.carbon.identity.webhook.management.util;

import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;
import org.wso2.carbon.utils.AuditLog;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit test class for WebhookManagementAuditLogger class.
 */
public class WebhookManagementAuditLoggerTest {

    private static final String ORGANIZATION_ID = "org-123";
    private static final String FAILURE_REASON = "Error while resolving the channels of the organization.";

    private WebhookManagementAuditLogger auditLogger;
    private Webhook webhook;
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
        auditLogger = new WebhookManagementAuditLogger();
        carbonContextMockedStatic = mockStatic(CarbonContext.class);
        identityUtil = mockStatic(IdentityUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        loggerUtilsMockedStatic = mockStatic(LoggerUtils.class);

        // Mock initiator and logger utils.
        CarbonContext carbonContext = mock(CarbonContext.class);
        carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(carbonContext);
        when(carbonContext.getUsername()).thenReturn("admin");
        when(carbonContext.getTenantDomain()).thenReturn("carbon.super");
        identityUtil.when(() -> IdentityUtil.getInitiatorId("admin", "carbon.super")).thenReturn("admin@carbon.super");
        loggerUtilsMockedStatic.when(() -> LoggerUtils.getInitiatorType(any())).thenReturn("USER");
        loggerUtilsMockedStatic.when(() -> LoggerUtils.getMaskedContent(any())).thenAnswer(i -> "****");
        loggerUtilsMockedStatic.when(() -> LoggerUtils.jsonObjectToMap(any(JSONObject.class))).thenCallRealMethod();
        loggerUtilsMockedStatic.when(() -> LoggerUtils.triggerAuditLogEvent(any(AuditLog.AuditLogBuilder.class)))
                .then(invocation -> null);

        // Prepare a sample webhook.
        webhook = new Webhook.Builder()
                .uuid("webhook-123")
                .endpoint("https://example.com/webhook")
                .name("Test Webhook")
                .secret("secret")
                .eventProfileName("profile")
                .eventProfileUri("uri")
                .eventProfileVersion("v1")
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(Collections.singletonList(mock(Subscription.class)))
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
     * Test audit log entry creation for a webhook.
     *
     * @throws WebhookMgtException If an error occurs while creating the audit log entry.
     */
    @Test
    public void testCreateAuditLogEntryForWebhook() throws WebhookMgtException {

        JSONObject data = invokeCreateAuditLogEntry(webhook);
        Assert.assertEquals(data.get("Id"), "webhook-123");
        Assert.assertEquals(data.get("Name"), "Test Webhook");
        Assert.assertEquals(data.get("EndpointUri"), "https://example.com/webhook");
        Assert.assertEquals(data.get("Secret"), "****");
        Assert.assertEquals(data.get("EventProfileName"), "profile");
        Assert.assertEquals(data.get("EventProfileUri"), "uri");
        Assert.assertEquals(data.get("EventProfileVersion"), "v1");
        Assert.assertEquals(data.get("Status"), WebhookStatus.ACTIVE);
        Assert.assertNotNull(data.get("CreatedAt"));
        Assert.assertNotNull(data.get("UpdatedAt"));
        Assert.assertNotNull(data.get("EventsSubscribed"));
    }

    /**
     * Test audit log entry creation for webhook ID only.
     */
    @Test
    public void testCreateAuditLogEntryForWebhookId() throws Exception {

        JSONObject data = invokeCreateAuditLogEntry();
        Assert.assertEquals(data.get("Id"), "webhook-456");
    }

    /**
     * Test printAuditLog for add operation.
     *
     * @throws WebhookMgtException If an error occurs while printing the audit log.
     */
    @Test
    public void testPrintAuditLogAdd() throws WebhookMgtException {

        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.ADD, webhook);
        // No exception means pass.
    }

    /**
     * Test printAuditLog for delete operation with webhook ID.
     */
    @Test
    public void testPrintAuditLogDeleteById() {

        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.DELETE, "webhook-789");
        // No exception means pass.
    }

    /**
     * Sides of the organization lifecycle whose subscription alignment can fail, each paired with the log action
     * it is expected to be audited under.
     *
     * @return Operation and the log action it carries.
     */
    @DataProvider(name = "organizationSubscriptionFailureOperations")
    public Object[][] organizationSubscriptionFailureOperations() {

        return new Object[][]{
                {WebhookManagementAuditLogger.Operation.ORGANIZATION_SUBSCRIPTION_FAILED,
                        "organization-subscription-failed"},
                {WebhookManagementAuditLogger.Operation.ORGANIZATION_UNSUBSCRIPTION_FAILED,
                        "organization-unsubscription-failed"}
        };
    }

    /**
     * Test that a failure to bring an organization's fanout subscriptions into line is audited against the
     * organization, carrying the organization and the reason the alignment failed.
     * <p>
     * The organization lifecycle is not failed over a webhook fanout problem, so this log is the only durable
     * record of the gap. It is recorded against the organization rather than a webhook because the affected
     * channels may span several webhooks, or may not have been resolved at all, which is what the target of the
     * log is asserted for here. Both sides of the lifecycle are covered so that each keeps its own log action.
     *
     * @param operation         Side of the lifecycle that failed.
     * @param expectedLogAction Log action the operation is expected to be audited under.
     * @throws NoSuchFieldException   If the audit log builder no longer declares the inspected field.
     * @throws IllegalAccessException If the inspected field cannot be read.
     */
    @Test(dataProvider = "organizationSubscriptionFailureOperations")
    public void testPrintOrganizationSubscriptionFailure(WebhookManagementAuditLogger.Operation operation,
                                                         String expectedLogAction)
            throws NoSuchFieldException, IllegalAccessException {

        auditLogger.printOrganizationSubscriptionFailureAuditLog(operation, ORGANIZATION_ID, FAILURE_REASON);

        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();
        Assert.assertNotNull(capturedArg);
        Assert.assertEquals(extractMapByField("OrganizationId", capturedArg), ORGANIZATION_ID);
        Assert.assertEquals(extractMapByField("FailureReason", capturedArg), FAILURE_REASON);
        Assert.assertEquals(extractField("targetId", capturedArg), ORGANIZATION_ID);
        Assert.assertEquals(extractField("targetType", capturedArg), LoggerUtils.Target.Organization.name());
        Assert.assertEquals(extractField("action", capturedArg), expectedLogAction);
        Assert.assertEquals(extractField("initiatorId", capturedArg), "admin@carbon.super");
    }

    /**
     * Test that the masked username stands in as the initiator when no initiator can be resolved for the user on
     * the thread.
     * <p>
     * An organization lifecycle failure is recorded on whichever thread performed the lifecycle operation, which
     * is not guaranteed to carry a resolvable initiator. The log is still worth keeping in that case, so the
     * username is used instead and is masked on the way out rather than written in the clear.
     *
     * @throws NoSuchFieldException   If the audit log builder no longer declares the inspected field.
     * @throws IllegalAccessException If the inspected field cannot be read.
     */
    @Test
    public void testPrintOrganizationSubscriptionFailureWithMaskedInitiator()
            throws NoSuchFieldException, IllegalAccessException {

        identityUtil.when(() -> IdentityUtil.getInitiatorId("admin", "carbon.super")).thenReturn("");

        auditLogger.printOrganizationSubscriptionFailureAuditLog(
                WebhookManagementAuditLogger.Operation.ORGANIZATION_SUBSCRIPTION_FAILED, ORGANIZATION_ID,
                FAILURE_REASON);

        AuditLog.AuditLogBuilder capturedArg = captureTriggerAuditLogEventArgs();
        Assert.assertEquals(extractField("initiatorId", capturedArg), "****");
    }

    /**
     * Capture the builder handed to the audit log event, which carries everything that would be logged.
     *
     * @return Captured audit log builder.
     */
    private AuditLog.AuditLogBuilder captureTriggerAuditLogEventArgs() {

        ArgumentCaptor<AuditLog.AuditLogBuilder> auditLogBuilderCaptor =
                ArgumentCaptor.forClass(AuditLog.AuditLogBuilder.class);
        loggerUtilsMockedStatic.verify(() -> LoggerUtils.triggerAuditLogEvent(auditLogBuilderCaptor.capture()));
        return auditLogBuilderCaptor.getValue();
    }

    /**
     * Read a single entry of the data the builder would log.
     *
     * @param fieldName       Name of the data entry to read.
     * @param auditLogBuilder Builder to read it from.
     * @return Value of that entry, or null when the builder carries no such entry.
     * @throws NoSuchFieldException   If the builder no longer declares its data.
     * @throws IllegalAccessException If the data cannot be read.
     */
    private String extractMapByField(String fieldName, AuditLog.AuditLogBuilder auditLogBuilder)
            throws NoSuchFieldException, IllegalAccessException {

        Field dataField = AuditLog.AuditLogBuilder.class.getDeclaredField("data");
        dataField.setAccessible(true);
        Map<String, Object> dataMap = (Map<String, Object>) dataField.get(auditLogBuilder);
        return (String) dataMap.get(fieldName);
    }

    /**
     * Read a field of the builder itself, as opposed to an entry of the data it would log.
     *
     * @param fieldName       Name of the field to read.
     * @param auditLogBuilder Builder to read it from.
     * @return Value of that field.
     * @throws NoSuchFieldException   If the builder no longer declares the field.
     * @throws IllegalAccessException If the field cannot be read.
     */
    private String extractField(String fieldName, AuditLog.AuditLogBuilder auditLogBuilder)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = AuditLog.AuditLogBuilder.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(auditLogBuilder);
    }

    // Helper to access private createAuditLogEntry(Webhook) via reflection.
    private JSONObject invokeCreateAuditLogEntry(Webhook webhook) {

        try {
            java.lang.reflect.Method m =
                    WebhookManagementAuditLogger.class.getDeclaredMethod("createAuditLogEntry", Webhook.class);
            m.setAccessible(true);
            return (JSONObject) m.invoke(auditLogger, webhook);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private createAuditLogEntry(String) via reflection.
    private JSONObject invokeCreateAuditLogEntry() {

        try {
            java.lang.reflect.Method m =
                    WebhookManagementAuditLogger.class.getDeclaredMethod("createAuditLogEntry", String.class);
            m.setAccessible(true);
            return (JSONObject) m.invoke(auditLogger, "webhook-456");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
