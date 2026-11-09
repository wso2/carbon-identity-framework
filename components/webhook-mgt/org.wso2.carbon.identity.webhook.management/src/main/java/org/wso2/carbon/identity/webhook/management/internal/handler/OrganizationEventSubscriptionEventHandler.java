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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.bean.IdentityEventMessageContext;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.organization.management.service.model.Organization;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;

import java.util.Map;

import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_PROP_ORGANIZATION;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants.EVENT_PROP_ORGANIZATION_ID;

/**
 * Keeps the organizations subscribed to ALL-policy webhook channels in line with the organization tree.
 * A failure is logged and audited, never failing the organization operation.
 */
public class OrganizationEventSubscriptionEventHandler extends AbstractEventHandler {

    private static final Log LOG = LogFactory.getLog(OrganizationEventSubscriptionEventHandler.class);
    private static final String HANDLER_NAME = "OrganizationEventSubscriptionEventHandler";
    private static final String HANDLER_ENABLED_PROPERTY = HANDLER_NAME + ".enable";

    private final WebhookManagementServiceImpl webhookManagementService;
    private final WebhookManagementAuditLogger auditLogger;

    public OrganizationEventSubscriptionEventHandler() {

        this(WebhookManagementServiceImpl.getInstance(), new WebhookManagementAuditLogger());
    }

    OrganizationEventSubscriptionEventHandler(WebhookManagementServiceImpl webhookManagementService,
                                              WebhookManagementAuditLogger auditLogger) {

        this.webhookManagementService = webhookManagementService;
        this.auditLogger = auditLogger;
    }

    @Override
    public String getName() {

        return HANDLER_NAME;
    }

    /**
     * Handles the organization events the module subscribes to, while both the module and child organization
     * subscriptions are enabled.
     */
    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {

        if (!(messageContext instanceof IdentityEventMessageContext) ||
                ((IdentityEventMessageContext) messageContext).getEvent() == null) {
            return false;
        }
        return super.canHandle(messageContext) && isHandlerEnabled();
    }

    /**
     * Whether this handler should maintain subscription rows. The module ships enabled, so the child organization
     * subscription feature flag is the switch an operator turns on.
     *
     * @return true if the module carries {@value #HANDLER_ENABLED_PROPERTY} set to true and child organization
     * subscriptions are enabled.
     */
    private boolean isHandlerEnabled() {

        if (configs == null || configs.getModuleProperties() == null) {
            return false;
        }
        return Boolean.parseBoolean(configs.getModuleProperties().getProperty(HANDLER_ENABLED_PROPERTY)) &&
                IdentityUtil.isChildOrganizationSubscriptionEnabled();
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        Map<String, Object> eventProperties = event.getEventProperties();
        if (IdentityEventConstants.Event.EVENT_POST_ADD_ORGANIZATION.equals(event.getEventName())) {
            handleOrganizationAdded(eventProperties);
        } else if (IdentityEventConstants.Event.EVENT_POST_DELETE_ORGANIZATION.equals(event.getEventName())) {
            handleOrganizationDeleted(eventProperties);
        }
    }

    private void handleOrganizationAdded(Map<String, Object> eventProperties) {

        Object organization = eventProperties.get(EVENT_PROP_ORGANIZATION);
        if (!(organization instanceof Organization) ||
                StringUtils.isBlank(((Organization) organization).getId())) {
            LOG.debug("POST_ADD_ORGANIZATION carried no organization. Skipping subscription materialisation.");
            return;
        }
        String newOrgId = ((Organization) organization).getId();
        try {
            webhookManagementService.subscribeNewOrganization(newOrgId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Materialised webhook channel subscriptions for new organization: " + newOrgId);
            }
        } catch (WebhookMgtException | IdentityRuntimeException e) {
            // The organization is created regardless, its subscriptions stay incomplete until its channels are
            // saved again, as there is no reconciliation job yet.
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
            webhookManagementService.unsubscribeDeletedOrganization(deletedOrgId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removed webhook channel subscriptions of deleted organization: " + deletedOrgId);
            }
        } catch (WebhookMgtException | IdentityRuntimeException e) {
            LOG.error("Could not remove the webhook channel subscriptions of a deleted organization. The " +
                    "organization was deleted; rows referencing it may remain. Organization: " + deletedOrgId, e);
            auditLogger.printOrganizationSubscriptionFailureAuditLog(
                    WebhookManagementAuditLogger.Operation.ORGANIZATION_UNSUBSCRIPTION_FAILED, deletedOrgId,
                    e.getMessage());
        }
    }
}
