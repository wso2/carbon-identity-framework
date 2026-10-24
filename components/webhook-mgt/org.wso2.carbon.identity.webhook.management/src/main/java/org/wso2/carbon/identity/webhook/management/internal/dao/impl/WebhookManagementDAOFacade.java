/*
Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).

WSO2 LLC. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */

package org.wso2.carbon.identity.webhook.management.internal.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.PlannedOrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookRunnable;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.AdapterTypeHandler;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler.WebhookAdapterTypeHandlerFactory;
import org.wso2.carbon.identity.webhook.management.internal.util.OrganizationSubscriptionPolicyUtil;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Facade for WebhookManagementDAO to handle webhook management operations.
 */
public class WebhookManagementDAOFacade implements WebhookManagementDAO {

    private final AdapterTypeHandler handler;

    public WebhookManagementDAOFacade(WebhookManagementDAO webhookManagementDAO) {

        this.handler = WebhookAdapterTypeHandlerFactory.getHandler(webhookManagementDAO);
    }

    private AdapterTypeHandler getHandler() throws WebhookMgtClientException {

        if (handler == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_OPERATION_NOT_SUPPORTED, String.valueOf(
                            WebhookManagementComponentServiceHolder.getInstance().getWebhookAdapter()));
        }
        return handler;
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().createWebhook(webhook, tenantId);
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        return getHandler().getWebhooks(tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return getHandler().getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return getHandler().getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        getHandler().deleteWebhook(webhookId, tenantId);
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return getHandler().isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().activateWebhook(webhook, tenantId);
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().deactivateWebhook(webhook, tenantId);
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().retryWebhook(webhook, tenantId);
    }

    @Override
    public int getWebhooksCount(int tenantId) throws WebhookMgtException {

        return getHandler().getWebhooksCount(tenantId);
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().updateWebhook(webhook, tenantId);
    }

    @Override
    public List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                           int tenantId) throws WebhookMgtException {

        return getHandler().getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
    }

    /**
     * Whether the organization fanout is folded in is the adapter type handler's decision, so this
     * delegates like every other call rather than choosing for it.
     */
    @Override
    public List<Webhook> getActiveWebhooksForOrganization(String eventProfileName, String eventProfileVersion,
                                                          String channelUri, int eventOrgTenantId)
            throws WebhookMgtException {

        return getHandler().getActiveWebhooksForOrganization(eventProfileName, eventProfileVersion, channelUri,
                eventOrgTenantId);
    }

    @Override
    public List<Webhook> getSubscriptionAcceptedWebhooksForOrganization(String eventProfileName,
                                                                        String eventProfileVersion,
                                                                        String channelUri, int eventOrgTenantId,
                                                                        boolean includeChildOrganizationSubscriptions)
            throws WebhookMgtException {

        return getHandler().getSubscriptionAcceptedWebhooksForOrganization(eventProfileName, eventProfileVersion,
                channelUri, eventOrgTenantId, includeChildOrganizationSubscriptions);
    }

    // --- Organization subscriptions ---

    @Override
    public void addChannelOrgSubscriptions(List<ChannelOrgSubscription> subscriptions) throws WebhookMgtException {

        getHandler().addChannelOrgSubscriptions(subscriptions);
    }

    @Override
    public List<ChannelOrgSubscription> getChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException {

        return getHandler().getChannelOrgSubscriptions(channelUuid);
    }

    @Override
    public List<Integer> getSubscribedOrgTenantIds(String channelUuid) throws WebhookMgtException {

        return getHandler().getSubscribedOrgTenantIds(channelUuid);
    }

    @Override
    public int getChannelOrgSubscriptionCount(String channelUuid) throws WebhookMgtException {

        return getHandler().getChannelOrgSubscriptionCount(channelUuid);
    }

    @Override
    public void deleteChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException {

        getHandler().deleteChannelOrgSubscriptions(channelUuid);
    }

    @Override
    public void deleteChannelOrgSubscriptionsByOrgId(String subscribedOrgId) throws WebhookMgtException {

        getHandler().deleteChannelOrgSubscriptionsByOrgId(subscribedOrgId);
    }

    @Override
    public List<String> getChannelUuids(String webhookId, int tenantId) throws WebhookMgtException {

        return getHandler().getChannelUuids(webhookId, tenantId);
    }

    @Override
    public int getOwningTenantId(String channelUuid) throws WebhookMgtException {

        return getHandler().getOwningTenantId(channelUuid);
    }

    @Override
    public String getChannelUuid(String webhookId, String channelUri, int tenantId) throws WebhookMgtException {

        return getHandler().getChannelUuid(webhookId, channelUri, tenantId);
    }

    @Override
    public String getChannelUri(String channelUuid) throws WebhookMgtException {

        return getHandler().getChannelUri(channelUuid);
    }

    @Override
    public Set<Integer> createWebhook(Webhook webhook, int tenantId, List<PlannedOrganizationSubscription> planned)
            throws WebhookMgtException {

        if (planned == null || planned.isEmpty()) {
            createWebhook(webhook, tenantId);
            return Collections.emptySet();
        }
        Set<Integer> affectedTenantIds = new HashSet<>();
        List<String> policiesWritten = new ArrayList<>();
        Map<String, String> policiesCleared = new HashMap<>();
        // The channel UUID is readable inside the transaction, as the read runs on the insert's own connection.
        runInTransaction(() -> {
            createWebhook(webhook, tenantId);
            applyPlannedOrganizationSubscriptions(webhook.getId(), tenantId, planned, policiesWritten,
                    policiesCleared, affectedTenantIds);
        }, ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, policiesWritten, policiesCleared);
        return affectedTenantIds;
    }

    @Override
    public Set<Integer> updateWebhook(String webhookId, Webhook webhook, int tenantId,
                                      List<PlannedOrganizationSubscription> planned) throws WebhookMgtException {

        if (planned == null || planned.isEmpty()) {
            updateWebhook(webhook, tenantId);
            return Collections.emptySet();
        }
        Set<Integer> affectedTenantIds = new HashSet<>();
        List<String> policiesWritten = new ArrayList<>();
        Map<String, String> policiesCleared = new HashMap<>();
        // Subscriptions follow the webhook row, since a channel the update adds has no UUID until it is written.
        runInTransaction(() -> {
            updateWebhook(webhook, tenantId);
            applyPlannedOrganizationSubscriptions(webhookId, tenantId, planned, policiesWritten,
                    policiesCleared, affectedTenantIds);
        }, ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR, policiesWritten, policiesCleared);
        return affectedTenantIds;
    }

    /**
     * Write the planned organization subscriptions inside the caller's transaction. Only the sharing policy
     * escapes it, written first so a partial failure leaves a policy without rows, which the sweep repairs.
     *
     * @param webhookId         Webhook the channels belong to.
     * @param tenantId          Tenant of the organization owning the webhook.
     * @param planned           Subscriptions resolved before the transaction was opened.
     * @param policiesWritten   Collects the channels whose policy was written, for undoing.
     * @param policiesCleared   Collects the channels whose policy was cleared, against the owning organization,
     *                          so a rollback can write it back.
     * @param affectedTenantIds Collects the tenants whose cached publish-path answer is now stale.
     * @throws WebhookMgtException If a channel cannot be resolved or the subscriptions cannot be stored.
     */
    private void applyPlannedOrganizationSubscriptions(String webhookId, int tenantId,
                                                       List<PlannedOrganizationSubscription> planned,
                                                       List<String> policiesWritten,
                                                       Map<String, String> policiesCleared,
                                                       Set<Integer> affectedTenantIds) throws WebhookMgtException {

        for (PlannedOrganizationSubscription subscription : planned) {
            String channelUuid = getChannelUuid(webhookId, subscription.getChannelUri(), tenantId);
            if (StringUtils.isEmpty(channelUuid)) {
                // The channel was written in this transaction and checked against the webhook, so
                // either the UUID column is not in use while subscriptions are, or the insert did not take.
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR, subscription.getChannelUri(), webhookId);
            }
            affectedTenantIds.addAll(getSubscribedOrgTenantIds(channelUuid));

            boolean policyAlreadyHeld =
                    OrganizationSubscriptionPolicyUtil.isPolicyHeld(channelUuid, subscription.getOwningOrgId());
            boolean policyWanted = subscription.getPolicy() == SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS;

            // Rows are replaced so stale ones cannot accumulate. The policy is beyond a rollback, so it is kept when it
            // stays and recorded when cleared, which lets a rollback restore it.
            if (policyAlreadyHeld && !policyWanted) {
                OrganizationSubscriptionPolicyUtil.clearPolicy(channelUuid);
                policiesCleared.put(channelUuid, subscription.getOwningOrgId());
            }
            deleteChannelOrgSubscriptions(channelUuid);

            if (subscription.getPolicy() == SubscriptionPolicy.NONE) {
                continue;
            }
            if (policyWanted && !policyAlreadyHeld) {
                // Only ALL leaves an organization subscription policy: it is the one policy whose meaning
                // extends to organizations that do not exist yet.
                OrganizationSubscriptionPolicyUtil.writePolicy(channelUuid, subscription.getOwningOrgId());
                policiesWritten.add(channelUuid);
            }
            List<ChannelOrgSubscription> rows = new ArrayList<>();
            for (Map.Entry<String, Integer> organization : subscription.getTenantIdsByOrgId().entrySet()) {
                rows.add(ChannelOrgSubscription.builder()
                        .channelUuid(channelUuid)
                        .subscribedOrgId(organization.getKey())
                        .subscribedOrgTenantId(organization.getValue())
                        .build());
                affectedTenantIds.add(organization.getValue());
            }
            addChannelOrgSubscriptions(rows);
        }
    }

    /**
     * Run every identity database write of an operation in one transaction: carbon-database-utils binds it to
     * the thread, so the DAO's own transactions join it. A client error is unwrapped and rethrown as itself.
     *
     * @param action            Writes to perform.
     * @param errorMessage      Error to report when the transaction fails for any other reason.
     * @param policiesToUndo    Policies written inside the transaction, cleared when it rolls back.
     * @param policiesToRestore Policies cleared inside the transaction, written back when it rolls back.
     * @throws WebhookMgtException If the action fails.
     */
    private void runInTransaction(WebhookRunnable action, ErrorMessage errorMessage, List<String> policiesToUndo,
                                  Map<String, String> policiesToRestore) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                action.run();
                return null;
            });
        } catch (TransactionException e) {
            // The policies commit on their own connection in the shared database, so the rollback cannot undo them.
            OrganizationSubscriptionPolicyUtil.clearPoliciesQuietly(policiesToUndo);
            OrganizationSubscriptionPolicyUtil.restorePoliciesQuietly(policiesToRestore);
            if (e.getCause() instanceof WebhookMgtException) {
                throw (WebhookMgtException) e.getCause();
            }
            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
        }
    }
}
