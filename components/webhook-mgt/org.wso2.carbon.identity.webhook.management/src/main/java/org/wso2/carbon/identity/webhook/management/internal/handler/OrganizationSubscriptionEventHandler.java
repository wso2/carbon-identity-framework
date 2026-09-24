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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.internal.service.WebhookChannelService;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookChannelServiceImpl;
import org.wso2.carbon.identity.webhook.management.internal.util.OrganizationSubscriptionConfig;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;

import java.util.Map;

import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_POST_ADD_ORGANIZATION;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_POST_DELETE_ORGANIZATION;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_PROP_ORGANIZATION;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_PROP_ORGANIZATION_ID;

/**
 * Keeps the organization fanout of webhook channels in line with the organization tree.
 * <p>
 * A channel whose policy is ALL means "every organization below the owner, including ones that do
 * not exist yet". That promise is kept by materialised rows in IDN_WEBHOOK_CHANNEL_ORG_SUB rather
 * than by walking the hierarchy at publish time, so the rows have to be maintained as the tree
 * changes. This handler is what maintains them:
 * <ul>
 *   <li>on POST_ADD_ORGANIZATION, the new organization is subscribed to every channel whose owner
 *       is one of its ancestors and whose policy is ALL;</li>
 *   <li>on POST_DELETE_ORGANIZATION, everything the organization leaves behind is removed — its
 *       rows as a subscriber, and the standing instructions of any channels it owned.</li>
 * </ul>
 * <p>
 * Neither branch fails the organization operation. A webhook fanout problem is not a reason to
 * refuse to create or delete an organization, so failures are recorded in the audit log and the
 * operation is allowed to complete; the resulting gap is what the reconciliation sweep is for.
 */
public class OrganizationSubscriptionEventHandler extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(OrganizationSubscriptionEventHandler.class);
    /**
     * Module name of this handler. It must match the module registered for it in
     * identity-event.properties exactly: AbstractEventHandler resolves its ModuleConfiguration by
     * this name, and a handler with no matching module block has a null configuration, which the
     * event service dereferences before it ever asks the handler whether it can handle the event.
     */
    private static final String HANDLER_NAME = "OrganizationSubscriptionEventHandler";
    private static final String HANDLER_ENABLED_PROPERTY = HANDLER_NAME + ".enable";

    private final WebhookChannelService webhookChannelService;
    private final WebhookManagementAuditLogger auditLogger;

    public OrganizationSubscriptionEventHandler() {

        this(new WebhookChannelServiceImpl(), new WebhookManagementAuditLogger());
    }

    OrganizationSubscriptionEventHandler(WebhookChannelService webhookChannelService,
                                         WebhookManagementAuditLogger auditLogger) {

        this.webhookChannelService = webhookChannelService;
        this.auditLogger = auditLogger;
    }

    @Override
    public String getName() {

        return HANDLER_NAME;
    }

    /**
     * The handler ships disabled, because the subscription rows it maintains need a database migration that a
     * deployment may not have applied yet. It stays inert until an operator turns it on.
     */
    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        if (!(messageContext instanceof IdentityEventMessageContext)) {
            return false;
        }
        Event event = ((IdentityEventMessageContext) messageContext).getEvent();
        if (event == null) {
            return false;
        }
        if (!isHandlerEnabled()) {
            LOG.debug("OrganizationSubscriptionEventHandler is disabled. Skipping event: " + event.getEventName());
            return false;
        }
        String eventName = event.getEventName();
        return EVENT_POST_ADD_ORGANIZATION.equals(eventName) || EVENT_POST_DELETE_ORGANIZATION.equals(eventName);
    }

    /**
     * Whether this handler should maintain subscription rows.
     * <p>
     * It needs its own module to be enabled in identity-event.properties, and the rest of the feature to be
     * available: with the feature off, or without the channel UUID column, the rows it writes are read by nothing.
     *
     * @return true only if the module carries {@value #HANDLER_ENABLED_PROPERTY} set to true and organization
     * subscriptions are available for channels.
     */
    private boolean isHandlerEnabled() {

        if (configs == null || configs.getModuleProperties() == null) {
            return false;
        }
        return Boolean.parseBoolean(configs.getModuleProperties().getProperty(HANDLER_ENABLED_PROPERTY)) &&
                OrganizationSubscriptionConfig.isChannelOrganizationSubscriptionSupported();
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        Map<String, Object> eventProperties = event.getEventProperties();
        if (EVENT_POST_ADD_ORGANIZATION.equals(event.getEventName())) {
            handleOrganizationAdded(eventProperties);
        } else if (EVENT_POST_DELETE_ORGANIZATION.equals(event.getEventName())) {
            handleOrganizationDeleted(eventProperties);
        }
    }

    private void handleOrganizationAdded(Map<String, Object> eventProperties) {

        Object organization = eventProperties.get(EVENT_PROP_ORGANIZATION);
        if (!(organization instanceof Organization)) {
            LOG.debug("POST_ADD_ORGANIZATION carried no organization. Skipping subscription materialisation.");
            return;
        }
        String newOrgId = ((Organization) organization).getId();
        if (StringUtils.isBlank(newOrgId)) {
            return;
        }

        try {
            webhookChannelService.subscribeNewOrganization(newOrgId);
            LOG.debug("Materialised webhook channel subscriptions for new organization: " + newOrgId);
        } catch (WebhookMgtException | RuntimeException e) {
            LOG.error("Could not subscribe the new organization to the webhook channels of its ancestors. The " +
                    "organization was created; its subscriptions are incomplete. Organization: " + newOrgId, e);
            auditLogger.printOrganizationSubscriptionFailureAuditLog(
                    WebhookManagementAuditLogger.Operation.ORGANIZATION_SUBSCRIPTION_FAILED, newOrgId,
                    e.getMessage());
        }
    }

    private void handleOrganizationDeleted(Map<String, Object> eventProperties) {

        Object organizationId = eventProperties.get(EVENT_PROP_ORGANIZATION_ID);
        if (!(organizationId instanceof String) || StringUtils.isBlank((String) organizationId)) {
            LOG.debug("POST_DELETE_ORGANIZATION carried no organization id. Skipping subscription cleanup.");
            return;
        }
        String deletedOrgId = (String) organizationId;

        try {
            webhookChannelService.unsubscribeDeletedOrganization(deletedOrgId);
            LOG.debug("Removed webhook channel subscriptions of deleted organization: " + deletedOrgId);
        } catch (WebhookMgtException | RuntimeException e) {
            LOG.error("Could not remove the webhook channel subscriptions of a deleted organization. The " +
                    "organization was deleted; rows referencing it may remain. Organization: " + deletedOrgId, e);
            auditLogger.printOrganizationSubscriptionFailureAuditLog(
                    WebhookManagementAuditLogger.Operation.ORGANIZATION_UNSUBSCRIPTION_FAILED, deletedOrgId,
                    e.getMessage());
        }
    }
}
