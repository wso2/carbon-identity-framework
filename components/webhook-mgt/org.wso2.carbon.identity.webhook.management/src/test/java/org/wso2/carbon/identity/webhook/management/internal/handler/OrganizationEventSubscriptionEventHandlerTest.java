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
package org.wso2.carbon.identity.webhook.management.internal.handler;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.bean.ModuleConfiguration;
import org.wso2.carbon.identity.event.bean.Subscription;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtServerException;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_PROP_ORGANIZATION;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_PROP_ORGANIZATION_ID;

/**
 * Unit tests for {@link OrganizationEventSubscriptionEventHandler}.
 */
@WithCarbonHome
public class OrganizationEventSubscriptionEventHandlerTest {

    private static final String HANDLER_NAME = "OrganizationEventSubscriptionEventHandler";
    private static final String ORG_ID = "b028ca17-8f89-449c-ae27-fa955e66465d";
    private static final String POST_ADD_ORGANIZATION =
            IdentityEventConstants.Event.EVENT_POST_ADD_ORGANIZATION;
    private static final String POST_DELETE_ORGANIZATION =
            IdentityEventConstants.Event.EVENT_POST_DELETE_ORGANIZATION;

    private WebhookManagementServiceImpl webhookManagementService;
    private WebhookManagementAuditLogger auditLogger;
    private OrganizationEventSubscriptionEventHandler handler;
    private MockedStatic<IdentityUtil> identityUtil;
    private Adapter originalAdapter;

    @BeforeMethod
    public void setUp() {

        // The service class builds its DAO facade when first loaded, which needs an adapter to be known.
        WebhookManagementComponentServiceHolder holder = WebhookManagementComponentServiceHolder.getInstance();
        originalAdapter = holder.getWebhookAdapter();
        holder.setWebhookAdapter(mock(Adapter.class));
        webhookManagementService = mock(WebhookManagementServiceImpl.class);
        auditLogger = mock(WebhookManagementAuditLogger.class);
        handler = new OrganizationEventSubscriptionEventHandler(webhookManagementService, auditLogger);
        handler.init(moduleConfiguration(true));
        identityUtil = mockStatic(IdentityUtil.class);
        identityUtil.when(IdentityUtil::isChildOrganizationSubscriptionEnabled).thenReturn(true);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        identityUtil.close();
        // Restored through the field, since the setter dereferences the adapter and so cannot restore none.
        Field webhookAdapter = WebhookManagementComponentServiceHolder.class.getDeclaredField("webhookAdapter");
        webhookAdapter.setAccessible(true);
        webhookAdapter.set(WebhookManagementComponentServiceHolder.getInstance(), originalAdapter);
    }

    @DataProvider(name = "organizationEvents")
    public Object[][] organizationEvents() {

        return new Object[][]{{POST_ADD_ORGANIZATION}, {POST_DELETE_ORGANIZATION}};
    }

    @Test(dataProvider = "organizationEvents")
    public void testCanHandleTheSubscribedOrganizationEvents(String eventName) {

        assertTrue(handler.canHandle(contextOf(new Event(eventName))));
    }

    @Test
    public void testCanHandleRejectsAnEventTheModuleDoesNotSubscribeTo() {

        assertFalse(handler.canHandle(contextOf(new Event("POST_UPDATE_ORGANIZATION"))));
    }

    @Test
    public void testCanHandleRejectsAContextWithoutAnEvent() {

        assertFalse(handler.canHandle(mock(MessageContext.class)));
        assertFalse(handler.canHandle(new IdentityEventMessageContext(null)));
    }

    @Test
    public void testCanHandleRejectsWhileTheModuleIsDisabled() {

        handler.init(moduleConfiguration(false));

        assertFalse(handler.canHandle(contextOf(new Event(POST_ADD_ORGANIZATION))));
    }

    @Test
    public void testCanHandleRejectsWhileChildOrganizationSubscriptionsAreDisabled() {

        identityUtil.when(IdentityUtil::isChildOrganizationSubscriptionEnabled).thenReturn(false);

        assertFalse(handler.canHandle(contextOf(new Event(POST_ADD_ORGANIZATION))));
    }

    @Test
    public void testCanHandleRejectsWithoutAModuleConfiguration() {

        OrganizationEventSubscriptionEventHandler unconfigured =
                new OrganizationEventSubscriptionEventHandler(webhookManagementService, auditLogger);

        assertFalse(unconfigured.canHandle(contextOf(new Event(POST_ADD_ORGANIZATION))));
    }

    @Test
    public void testGetName() {

        assertEquals(handler.getName(), HANDLER_NAME);
    }

    @Test
    public void testAddedOrganizationIsSubscribed() throws Exception {

        handler.handleEvent(new Event(POST_ADD_ORGANIZATION,
                propertiesOf(EVENT_PROP_ORGANIZATION, organization(ORG_ID))));

        verify(webhookManagementService).subscribeNewOrganization(ORG_ID);
        verifyNoInteractions(auditLogger);
    }

    @DataProvider(name = "addEventsWithoutAnOrganization")
    public Object[][] addEventsWithoutAnOrganization() {

        return new Object[][]{{null}, {"not an organization"}, {organization(" ")}};
    }

    @Test(dataProvider = "addEventsWithoutAnOrganization")
    public void testAddEventWithoutAnOrganizationIsSkipped(Object organization) throws Exception {

        handler.handleEvent(new Event(POST_ADD_ORGANIZATION,
                propertiesOf(EVENT_PROP_ORGANIZATION, organization)));

        verify(webhookManagementService, never()).subscribeNewOrganization(anyString());
    }

    @Test
    public void testDeletedOrganizationIsUnsubscribed() throws Exception {

        handler.handleEvent(new Event(POST_DELETE_ORGANIZATION, propertiesOf(EVENT_PROP_ORGANIZATION_ID, ORG_ID)));

        verify(webhookManagementService).unsubscribeDeletedOrganization(ORG_ID);
        verifyNoInteractions(auditLogger);
    }

    @DataProvider(name = "deleteEventsWithoutAnOrganizationId")
    public Object[][] deleteEventsWithoutAnOrganizationId() {

        return new Object[][]{{null}, {" "}, {42}};
    }

    @Test(dataProvider = "deleteEventsWithoutAnOrganizationId")
    public void testDeleteEventWithoutAnOrganizationIdIsSkipped(Object organizationId) throws Exception {

        handler.handleEvent(new Event(POST_DELETE_ORGANIZATION,
                propertiesOf(EVENT_PROP_ORGANIZATION_ID, organizationId)));

        verify(webhookManagementService, never()).unsubscribeDeletedOrganization(anyString());
    }

    /**
     * A failure is audited rather than thrown, so it never fails the organization creation.
     */
    @Test
    public void testSubscriptionFailureIsAuditedNotThrown() throws Exception {

        doThrow(new WebhookMgtServerException("cannot subscribe")).when(webhookManagementService)
                .subscribeNewOrganization(ORG_ID);

        handler.handleEvent(new Event(POST_ADD_ORGANIZATION,
                propertiesOf(EVENT_PROP_ORGANIZATION, organization(ORG_ID))));

        verify(auditLogger).printOrganizationSubscriptionFailureAuditLog(
                eq(WebhookManagementAuditLogger.Operation.ORGANIZATION_SUBSCRIPTION_FAILED), eq(ORG_ID),
                eq("cannot subscribe"));
    }

    @Test
    public void testUnsubscriptionFailureIsAuditedNotThrown() throws Exception {

        doThrow(new IdentityRuntimeException("cannot unsubscribe")).when(webhookManagementService)
                .unsubscribeDeletedOrganization(ORG_ID);

        handler.handleEvent(new Event(POST_DELETE_ORGANIZATION, propertiesOf(EVENT_PROP_ORGANIZATION_ID, ORG_ID)));

        verify(auditLogger).printOrganizationSubscriptionFailureAuditLog(
                eq(WebhookManagementAuditLogger.Operation.ORGANIZATION_UNSUBSCRIPTION_FAILED), eq(ORG_ID),
                eq("cannot unsubscribe"));
    }

    @Test
    public void testAnotherEventIsIgnored() throws Exception {

        handler.handleEvent(new Event("POST_UPDATE_ORGANIZATION",
                propertiesOf(EVENT_PROP_ORGANIZATION_ID, ORG_ID)));

        verifyNoInteractions(webhookManagementService);
    }

    private static ModuleConfiguration moduleConfiguration(boolean enabled) {

        Properties moduleProperties = new Properties();
        moduleProperties.setProperty(HANDLER_NAME + ".enable", String.valueOf(enabled));
        return new ModuleConfiguration(moduleProperties, Arrays.asList(
                new Subscription(POST_ADD_ORGANIZATION, new Properties()),
                new Subscription(POST_DELETE_ORGANIZATION, new Properties())));
    }

    private static IdentityEventMessageContext contextOf(Event event) {

        return new IdentityEventMessageContext(event);
    }

    private static Organization organization(String id) {

        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    private static Map<String, Object> propertiesOf(String key, Object value) {

        if (value == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(key, value);
        return properties;
    }
}
