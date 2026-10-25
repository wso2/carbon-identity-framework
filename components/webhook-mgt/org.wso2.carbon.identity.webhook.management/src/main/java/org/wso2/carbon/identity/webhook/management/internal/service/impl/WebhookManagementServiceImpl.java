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

package org.wso2.carbon.identity.webhook.management.internal.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.model.ResourceSharingPolicy;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCache;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelSubscribedOrganizations;
import org.wso2.carbon.identity.webhook.management.api.model.OrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookChannel;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.api.service.WebhookManagementService;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookMgtConstants;
import org.wso2.carbon.identity.webhook.management.internal.dao.PlannedOrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.util.OrganizationSubscriptionPolicyUtil;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of WebhookManagementService.
 * This class uses WebhookManagementFacade to handle webhook operations.
 */
public class WebhookManagementServiceImpl implements WebhookManagementService {

    private static final Log LOG = LogFactory.getLog(WebhookManagementServiceImpl.class);
    private static final WebhookManagementServiceImpl webhookManagementServiceImpl =
            new WebhookManagementServiceImpl();
    private final WebhookManagementDAO daoFACADE;
    private static final WebhookValidator WEBHOOK_VALIDATOR = new WebhookValidator();
    private static final WebhookManagementAuditLogger auditLogger = new WebhookManagementAuditLogger();


    private WebhookManagementServiceImpl() {

        daoFACADE = new WebhookManagementDAOFacade(new CacheBackedWebhookManagementDAO(new WebhookManagementDAOImpl()));
    }

    public static WebhookManagementServiceImpl getInstance() {

        return webhookManagementServiceImpl;
    }

    @Override
    public Webhook createWebhook(Webhook webhook, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating webhook with endpoint: %s for tenant: %s",
                    webhook.getEndpoint(), tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (daoFACADE.isWebhookEndpointExists(webhook.getEndpoint(), tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_ALREADY_EXISTS, webhook.getEndpoint());
        }
        validateMaxWebhooksCount(tenantDomain);
        doPreAddWebhookValidations(webhook);
        String generatedWebhookId = UUID.randomUUID().toString();

        WebhookStatus status = webhook.getStatus() != null ? webhook.getStatus() : WebhookStatus.INACTIVE;

        Webhook webhookToCreate = new Webhook.Builder()
                .uuid(generatedWebhookId)
                .endpoint(webhook.getEndpoint())
                .name(webhook.getName())
                .secret(webhook.getSecret())
                .eventProfileName(webhook.getEventProfileName())
                .eventProfileUri(webhook.getEventProfileUri())
                .eventProfileVersion(webhook.getEventProfileVersion())
                .status(status)
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .eventsSubscribed(webhook.getEventsSubscribed())
                .ownerTenantDomain(tenantDomain)
                .build();

        // Resolved before the transaction opens: these read the organization database, and a nested transaction
        // reuses the identity connection already bound to the thread, whatever datasource it was built from.
        List<PlannedOrganizationSubscription> plannedSubscriptions =
                planOrganizationSubscriptions(webhook.getOrganizationSubscriptions(), tenantDomain);

        // The facade writes the webhook and its subscriptions in one transaction, undoing policies on failure.
        Set<Integer> affectedTenantIds = daoFACADE.createWebhook(webhookToCreate, tenantId, plannedSubscriptions);

        clearActiveWebhooksCache(affectedTenantIds);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.ADD, webhookToCreate);
        return daoFACADE.getWebhook(webhookToCreate.getId(), tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Subscription> getWebhookEvents(String webhookId, String tenantDomain)
            throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting events for webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        return daoFACADE.getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public Webhook updateWebhook(String webhookId, Webhook webhook, String tenantDomain)
            throws WebhookMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        doPreUpdateWebhookValidations(webhook);

        // Resolved before the transaction opens, as on the creation path: these read the organization database,
        // which must not run on the identity connection.
        List<PlannedOrganizationSubscription> plannedSubscriptions =
                planOrganizationSubscriptions(webhook.getOrganizationSubscriptions(), tenantDomain);

        // Read the organizations subscribed before the change: an organization dropped from a policy must have
        // its cache evicted too, and it is no longer resolvable once the subscription rows are rewritten.
        Set<Integer> previouslySubscribedTenantIds = resolveSubscribedTenantIds(webhookId, tenantId);
        /*
         Read before the update: a channel this update drops takes its row with it, and with the row the UUID
         its subscription policy is keyed by, so the policy could no longer be located afterwards.
         */
        List<String> channelUuidsBeforeUpdate = resolveChannelUuidsQuietly(webhookId, tenantId);

        Set<Integer> affectedTenantIds = new HashSet<>(previouslySubscribedTenantIds);
        // One transaction in the facade; a channel absent from the request keeps its current fanout.
        affectedTenantIds.addAll(daoFACADE.updateWebhook(webhookId, webhook, tenantId, plannedSubscriptions));

        // Policies of dropped channels are cleared only after the commit: clearing is irreversible, and a rollback
        // would otherwise restore channel rows with no policy behind them, which reconciliation cannot repair.
        clearSubscriptionPoliciesOfRemovedChannels(channelUuidsBeforeUpdate, webhookId, tenantId);

        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.UPDATE, webhook);
        affectedTenantIds.addAll(resolveSubscribedTenantIds(webhookId, tenantId));
        clearActiveWebhooksCache(affectedTenantIds);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        // Read before the deletion and whatever the feature setting: the rows are gone afterwards, and a policy left
        // in the shared database could never be matched to a channel again, since nothing cascades to it.
        List<String> channelUuids = resolveChannelUuidsQuietly(webhookId, tenantId);
        Set<Integer> subscribedTenantIds = resolveSubscribedTenantIds(channelUuids);
        daoFACADE.deleteWebhook(webhookId, tenantId);
        // The policies live in the shared database and cannot cascade from the Identity database, so they are
        // removed explicitly once the channels are gone.
        OrganizationSubscriptionPolicyUtil.clearPoliciesQuietly(channelUuids);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.DELETE, webhookId);
        clearActiveWebhooksCache(subscribedTenantIds);
    }

    @Override
    public List<Webhook> getWebhooks(String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting all webhooks for tenant: %s", tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        return daoFACADE.getWebhooks(tenantId);
    }

    @Override
    public List<Webhook> getWebhooks(int offset, int limit, String tenantDomain) throws WebhookMgtException {

        validatePagingArguments(offset, limit);
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting webhooks for tenant: %s with offset: %d and limit: %d",
                    tenantDomain, offset, limit));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // The window is applied after the read rather than in SQL: webhooks are configuration, so the
        // collection is small and bounded per tenant. Callers see a paged contract either way.
        List<Webhook> webhooks = daoFACADE.getWebhooks(tenantId);
        if (offset >= webhooks.size()) {
            return Collections.emptyList();
        }
        // The limit is capped by what remains rather than added to the offset: a caller may pass a limit
        // near Integer.MAX_VALUE, and offset + limit would then wrap negative.
        return new ArrayList<>(webhooks.subList(offset, offset + Math.min(limit, webhooks.size() - offset)));
    }

    @Override
    public int getWebhooksCount(String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting webhook count for tenant: %s", tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        return daoFACADE.getWebhooksCount(tenantId);
    }

    @Override
    public Webhook activateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Activating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Webhook existingWebhook = daoFACADE.getWebhook(webhookId, tenantId);
        if (existingWebhook == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.activateWebhook(existingWebhook, tenantId);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.ACTIVATE, webhookId);
        clearActiveWebhooksCacheOfSubscribedOrganizations(webhookId, tenantId);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public Webhook deactivateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deactivating webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Webhook existingWebhook = daoFACADE.getWebhook(webhookId, tenantId);
        if (existingWebhook == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.deactivateWebhook(existingWebhook, tenantId);
        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.DEACTIVATE, webhookId);
        clearActiveWebhooksCacheOfSubscribedOrganizations(webhookId, tenantId);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public Webhook retryWebhook(String webhookId, String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrying webhook with ID: %s for tenant: %s",
                    webhookId, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        Webhook existingWebhook = daoFACADE.getWebhook(webhookId, tenantId);
        if (existingWebhook == null) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        daoFACADE.retryWebhook(existingWebhook, tenantId);
        clearActiveWebhooksCacheOfSubscribedOrganizations(webhookId, tenantId);
        return daoFACADE.getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                           String tenantDomain) throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving active webhooks for channel URI: %s in tenant: %s",
                    channelUri, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (IdentityUtil.isChildOrganizationSubscriptionEnabled() && isSubOrganization(tenantId)) {
            return daoFACADE.getActiveWebhooksForOrganization(eventProfileName, eventProfileVersion,
                    channelUri, tenantId);
        }
        return daoFACADE.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
    }

    @Override
    public List<Webhook> getSubscriptionAcceptedWebhooksForOrganization(String eventProfileName,
                                                                        String eventProfileVersion,
                                                                        String channelUri, String tenantDomain)
            throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving webhooks subscribed to channel URI: %s in tenant: %s",
                    channelUri, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Ancestors' webhooks are looked for only from a sub organization, and only while the feature is on:
        // until then no channel records an organization subscription for the wider lookup to find.
        boolean includeChildOrganizationSubscriptions =
                IdentityUtil.isChildOrganizationSubscriptionEnabled() && isSubOrganization(tenantId);
        return daoFACADE.getSubscriptionAcceptedWebhooksForOrganization(eventProfileName, eventProfileVersion,
                channelUri, tenantId, includeChildOrganizationSubscriptions);
    }

    /**
     * Check whether the given tenant is a sub-organization, since only one of those can be subscribed to an
     * ancestor's webhook. Returns true on failure: the wider lookup also returns the tenant's own webhooks.
     *
     * @param tenantId Tenant ID of the organization that raised the event.
     * @return true if the tenant is a sub-organization, or if that cannot be determined.
     */
    private boolean isSubOrganization(int tenantId) {

        try {
            return OrganizationManagementUtil.isOrganization(tenantId);
        } catch (OrganizationManagementException e) {
            // Runs per event and the event still goes through the wider lookup, so a persistent failure must
            // not log a stack trace for every event; the trace is kept for debugging.
            LOG.warn("Could not check whether tenant id: " + tenantId + " is an organization: " + e.getMessage() +
                    ". Falling back to the lookup that includes subscribed child organizations.");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Organization check failure for tenant id: " + tenantId, e);
            }
            return true;
        }
    }

    private void validatePagingArguments(int offset, int limit) throws WebhookMgtClientException {

        if (offset < 0) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_REQUEST, "The pagination offset must not be negative.");
        }
        if (limit <= 0) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_REQUEST, "The pagination limit must be greater than zero.");
        }
    }

    private boolean isWebhookExists(String webhookId, int tenantId) throws WebhookMgtException {

        return daoFACADE.getWebhook(webhookId, tenantId) != null;
    }

    // Common validation for required fields except secret
    private void validateCommonWebhookFields(Webhook webhook) throws WebhookMgtException {

        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.WEBHOOK_NAME_FIELD, webhook.getName());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.ENDPOINT_URI_FIELD, webhook.getEndpoint());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.EVENT_PROFILE_NAME_FIELD, webhook.getEventProfileName());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.EVENT_PROFILE_URI_FIELD, webhook.getEventProfileUri());
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.STATUS_FIELD, String.valueOf(webhook.getStatus()));
        WEBHOOK_VALIDATOR.validateWebhookName(webhook.getName());
        WEBHOOK_VALIDATOR.validateEndpointUri(webhook.getEndpoint());
        WEBHOOK_VALIDATOR.validateChannelsSubscribed(webhook.getEventProfileName(), webhook.getEventsSubscribed());
        validateOrganizationSubscriptions(webhook);
    }

    private void doPreAddWebhookValidations(Webhook webhook) throws WebhookMgtException {

        validateCommonWebhookFields(webhook);
        WEBHOOK_VALIDATOR.validateForBlank(WebhookMgtConstants.SECRET_FIELD, webhook.getSecret());
        WEBHOOK_VALIDATOR.validateWebhookSecret(webhook.getSecret());
    }

    private void doPreUpdateWebhookValidations(Webhook webhook) throws WebhookMgtException {

        validateCommonWebhookFields(webhook);
        // Secret is optional for update
        if (StringUtils.isNotBlank(webhook.getSecret())) {
            WEBHOOK_VALIDATOR.validateWebhookSecret(webhook.getSecret());
        }
    }

    private void validateMaxWebhooksCount(String tenantDomain) throws WebhookMgtException {

        LOG.debug("Retrieving webhook count for tenant: " + tenantDomain);
        int webhooksCount = daoFACADE.getWebhooksCount(IdentityTenantUtil.getTenantId(tenantDomain));
        int maxWebhooksCount = IdentityUtil.getMaximumWebhooksPerTenant();
        if (webhooksCount >= maxWebhooksCount) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_MAXIMUM_WEBHOOKS_PER_TENANT_REACHED, String.valueOf(maxWebhooksCount));
        }
    }

    @Override
    public List<WebhookChannel> getWebhookChannels(String webhookId, String tenantDomain)
            throws WebhookMgtException {

        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            // Channels are identified by the UUID column that arrives with the organization subscription
            // migration, so there is no channel to report until the feature is enabled.
            return Collections.emptyList();
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        List<Subscription> subscribedChannels = daoFACADE.getWebhookEvents(webhookId, tenantId);
        if (subscribedChannels.isEmpty()) {
            return Collections.emptyList();
        }
        // Every channel belongs to this webhook, so they share its owning organization.
        String owningOrgId = resolveOwningOrgId(tenantDomain);
        List<WebhookChannel> channels = new ArrayList<>();
        // The subscription record carries the channel URI and the webhook's status for it; the identity and the
        // organization policy are resolved alongside, so one pass produces the whole channel.
        for (Subscription channel : subscribedChannels) {
            String channelId = daoFACADE.getChannelUuid(webhookId, channel.getChannelUri(), tenantId);
            if (channelId == null) {
                continue;
            }
            channels.add(WebhookChannel.builder()
                    .channelUri(channel.getChannelUri())
                    .channelId(channelId)
                    .status(channel.getStatus())
                    .policy(resolveSubscriptionPolicy(channelId, owningOrgId))
                    .build());
        }
        return channels;
    }

    @Override
    public ChannelSubscribedOrganizations getChannelOrganizationSubscriptions(String webhookId, String channelId,
                                                                             String tenantDomain, int offset,
                                                                             int limit) throws WebhookMgtException {

        validateChildOrganizationSubscriptionEnabled();
        validatePagingArguments(offset, limit);
        // Ownership is established once here. The reads below are reached directly rather than through the
        // single-value methods, each of which would repeat this same check.
        validateChannelBelongsToWebhook(webhookId, channelId, tenantDomain);
        // The count, the page and the SELECTED or NONE policy are taken from this one read, so they cannot disagree.
        List<ChannelOrgSubscription> subscriptions = daoFACADE.getChannelOrgSubscriptions(channelId);
        return ChannelSubscribedOrganizations.builder()
                .channelUri(daoFACADE.getChannelUri(channelId))
                .policy(getSubscriptionPolicy(channelId, resolveOwningOrgId(tenantDomain), subscriptions))
                .totalCount(subscriptions.size())
                .organizations(pageOf(subscriptions, offset, limit))
                .build();
    }

    @Override
    public WebhookChannel getWebhookChannel(String webhookId, String channelId, String tenantDomain)
            throws WebhookMgtException {

        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            // Channels are named by a column the migration adds, so while the feature is off there is no channel to
            // name; reported as not found, consistent with the plural lookup answering with none.
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_CHANNEL_NOT_FOUND, channelId, webhookId);
        }

        // Ownership is established once here. The reads below are reached directly rather than through the
        // single-value methods, each of which would repeat this same check.
        validateChannelBelongsToWebhook(webhookId, channelId, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String channelUri = daoFACADE.getChannelUri(channelId);
        return WebhookChannel.builder()
                .channelId(channelId)
                .channelUri(channelUri)
                .status(resolveChannelSubscriptionStatus(webhookId, channelUri, tenantId))
                .policy(resolveSubscriptionPolicy(channelId, resolveOwningOrgId(tenantDomain)))
                .build();
    }

    /**
     * The webhook's subscription status for one of its channels. The status is held per subscribed channel on
     * the webhook rather than on the channel row, so it is found by matching the channel URI.
     *
     * @param webhookId  Webhook UUID.
     * @param channelUri URI of the channel to look for.
     * @param tenantId   Tenant owning the webhook.
     * @return The status, or null if the webhook carries none for that channel.
     */
    private SubscriptionStatus resolveChannelSubscriptionStatus(String webhookId, String channelUri, int tenantId)
            throws WebhookMgtException {

        for (Subscription subscription : daoFACADE.getWebhookEvents(webhookId, tenantId)) {
            if (StringUtils.equals(channelUri, subscription.getChannelUri())) {
                return subscription.getStatus();
            }
        }
        return null;
    }

    /**
     * Clear the active webhooks cache of every tenant this webhook fans out to: the cache is keyed by the
     * raising tenant, and the DAO only clears the one it was handed. Best effort, as the change is committed.
     *
     * @param webhookId Webhook that changed.
     * @param tenantId  Tenant owning the webhook.
     */
    private void clearActiveWebhooksCacheOfSubscribedOrganizations(String webhookId, int tenantId) {

        clearActiveWebhooksCache(resolveSubscribedTenantIds(webhookId, tenantId));
    }

    private Set<Integer> resolveSubscribedTenantIds(String webhookId, int tenantId) {

        // No organization can be subscribed while the feature is disabled, and the subscription tables it would
        // read may not exist yet.
        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            return Collections.emptySet();
        }
        return resolveSubscribedTenantIds(resolveChannelUuidsQuietly(webhookId, tenantId));
    }

    /**
     * Organization subscription policy of a channel.
     *
     * @param channelId   Channel UUID.
     * @param owningOrgId Organization owning the channel's webhook.
     * @return The policy, or NONE while child organization subscriptions are disabled, as no organization can
     * be subscribed then.
     * @throws WebhookMgtException If the policy could not be read.
     */
    private SubscriptionPolicy resolveSubscriptionPolicy(String channelId, String owningOrgId)
            throws WebhookMgtException {

        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            return SubscriptionPolicy.NONE;
        }
        return getSubscriptionPolicy(channelId, owningOrgId);
    }

    /**
     * Reject a channel organization subscription operation that cannot be served.
     *
     * @throws WebhookMgtClientException If organization subscriptions are not available for channels.
     */
    private void validateChildOrganizationSubscriptionEnabled() throws WebhookMgtClientException {

        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_CHILD_ORG_SUBSCRIPTION_DISABLED);
        }
    }

    /**
     * Tenants subscribed to any of the given channels.
     *
     * @param channelUuids Channels to read.
     * @return Tenant identifiers, empty if none could be resolved.
     */
    private Set<Integer> resolveSubscribedTenantIds(List<String> channelUuids) {

        Set<Integer> subscribedTenantIds = new HashSet<>();
        for (String channelUuid : channelUuids) {
            try {
                subscribedTenantIds.addAll(daoFACADE.getSubscribedOrgTenantIds(channelUuid));
            } catch (WebhookMgtException | IdentityRuntimeException e) {
                LOG.warn("Could not resolve the organizations subscribed to channel: " + channelUuid +
                        ". Their active webhooks cache may serve a stale answer until it is evicted.", e);
            }
        }
        return subscribedTenantIds;
    }

    /**
     * Clears the organization subscription policies of the channels a webhook update removed. They cannot
     * cascade from the Identity database, and a channel re-added later gets its own UUID, so they would orphan.
     *
     * @param channelUuidsBeforeUpdate Channels the webhook had before the update.
     * @param webhookId                Webhook that was updated.
     * @param tenantId                 Tenant owning the webhook.
     */
    private void clearSubscriptionPoliciesOfRemovedChannels(List<String> channelUuidsBeforeUpdate, String webhookId,
                                                            int tenantId) {

        if (channelUuidsBeforeUpdate.isEmpty()) {
            return;
        }
        List<String> remainingChannelUuids = resolveChannelUuidsQuietly(webhookId, tenantId);
        List<String> removedChannelUuids = new ArrayList<>();
        for (String channelUuid : channelUuidsBeforeUpdate) {
            if (!remainingChannelUuids.contains(channelUuid)) {
                removedChannelUuids.add(channelUuid);
            }
        }
        OrganizationSubscriptionPolicyUtil.clearPoliciesQuietly(removedChannelUuids);
    }

    /**
     * Channels of a webhook, resolved without raising. Used on paths that are already handling a failure or have
     * already committed their work, where losing the channel list costs cleanup rather than correctness.
     *
     * @param webhookId Webhook to read.
     * @param tenantId  Tenant owning the webhook.
     * @return Channel identifiers, empty if they could not be resolved.
     */
    private List<String> resolveChannelUuidsQuietly(String webhookId, int tenantId) {

        try {
            return daoFACADE.getChannelUuids(webhookId, tenantId);
        } catch (WebhookMgtException | IdentityRuntimeException e) {
            LOG.warn("Could not resolve the channels of webhook: " + webhookId +
                    ". Their organization subscription policies may be left behind for reconciliation.", e);
            return Collections.emptyList();
        }
    }

    private void clearActiveWebhooksCache(Set<Integer> tenantIds) {

        for (Integer subscribedTenantId : tenantIds) {
            ActiveWebhooksCache.getInstance().clear(subscribedTenantId);
        }
    }

    private void validateChannelBelongsToWebhook(String webhookId, String channelId, String tenantDomain)
            throws WebhookMgtException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // Checked before the channel, so a request naming a webhook that does not exist is told the webhook is
        // missing: it has no channels, so the channel lookup below would blame the wrong resource.
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        if (!daoFACADE.getChannelUuids(webhookId, tenantId).contains(channelId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_CHANNEL_NOT_FOUND, channelId, webhookId);
        }
    }

    private String resolveOwningOrgId(String tenantDomain) throws WebhookMgtException {

        try {
            return getOrganizationManager().resolveOrganizationId(tenantDomain);
        } catch (OrganizationManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR, e, tenantDomain);
        }
    }

    /**
     * Enforce that a selection selects something; NONE and ALL drop any organizations supplied rather than
     * reject them. Channels are checked here, before the webhook row commits, so a bad URI cannot leave one.
     */
    private void validateOrganizationSubscriptions(Webhook webhook) throws WebhookMgtException {

        List<OrganizationSubscription> organizationSubscriptions = webhook.getOrganizationSubscriptions();
        if (organizationSubscriptions == null || organizationSubscriptions.isEmpty()
                || !IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            return;
        }
        Set<String> subscribedChannelUris = new HashSet<>();
        if (webhook.getEventsSubscribed() != null) {
            for (Subscription subscription : webhook.getEventsSubscribed()) {
                subscribedChannelUris.add(subscription.getChannelUri());
            }
        }
        Set<String> organizationSubscriptionChannelUris = new HashSet<>();
        for (OrganizationSubscription organizationSubscription : organizationSubscriptions) {
            // One entry per channel: the policy writes commit outside the transaction, and a second entry
            // for the same channel would undo the first in a way a rollback cannot restore.
            if (!organizationSubscriptionChannelUris.add(organizationSubscription.getChannelUri())) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_SUB_ORG_DUPLICATE_CHANNEL, organizationSubscription.getChannelUri());
            }
            if (organizationSubscription.getPolicy() == SubscriptionPolicy.SELECTED_ORGS_ONLY
                    && organizationSubscription.getOrganizationIds().isEmpty()) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SELECTED_ORGS_REQUIRED);
            }
            if (!subscribedChannelUris.contains(organizationSubscription.getChannelUri())) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_SUB_ORG_CHANNEL_NOT_SUBSCRIBED,
                        organizationSubscription.getChannelUri());
            }
        }
    }


    // --- Organization subscriptions. Only ALL_EXISTING_AND_FUTURE_ORGS writes a UM_RESOURCE_SHARING_POLICY row,
    // since only it must act on organizations created later; the other policies are materialised rows alone.

    /**
     * Subscribe a newly created organization to every channel whose ancestor left an organization policy.
     *
     * @param newOrgId Organization that has just been created.
     * @throws WebhookMgtException If the subscriptions cannot be resolved or stored.
     */
    public void subscribeNewOrganization(String newOrgId) throws WebhookMgtException {

        List<String> ancestorOrgIds = resolveAncestorOrgIds(newOrgId);
        if (ancestorOrgIds.isEmpty()) {
            return;
        }

        List<String> channelUuids = new ArrayList<>();
        for (String ancestorOrgId : ancestorOrgIds) {
            for (ResourceSharingPolicy orgSubscriptionPolicy :
                    OrganizationSubscriptionPolicyUtil.getPoliciesHeldBy(ancestorOrgId)) {
                String channelUuid = orgSubscriptionPolicy.getResourceId();
                // A policy can outlive its channel, as neither database cascades to the other. It is skipped and
                // reported, so one missing channel does not fail the batch for the channels that remain.
                if (daoFACADE.getChannelUri(channelUuid) == null) {
                    LOG.warn("Channel: " + channelUuid + " named by a subscription policy of organization: " +
                            ancestorOrgId + " no longer exists, so organization: " + newOrgId +
                            " was not subscribed to it. The policy is stale and can be removed.");
                    continue;
                }
                channelUuids.add(channelUuid);
            }
        }
        if (channelUuids.isEmpty()) {
            return;
        }

        // Every row is for the new organization, so its tenant is resolved once rather than once per channel.
        int newOrgTenantId = resolveTenantIdsOf(Collections.singletonList(newOrgId)).get(newOrgId);
        List<ChannelOrgSubscription> subscriptions = new ArrayList<>();
        for (String channelUuid : channelUuids) {
            subscriptions.add(ChannelOrgSubscription.builder()
                    .channelUuid(channelUuid)
                    .subscribedOrgId(newOrgId)
                    .subscribedOrgTenantId(newOrgTenantId)
                    .build());
        }
        daoFACADE.addChannelOrgSubscriptions(subscriptions);
        clearActiveWebhooksCache(Collections.singleton(newOrgTenantId));
    }

    /**
     * Remove every trace of a deleted organization, both as a subscriber and as a channel owner.
     *
     * @param deletedOrgId Organization that has just been deleted.
     * @throws WebhookMgtException If the subscriptions or the organization subscription policies cannot be removed.
     */
    public void unsubscribeDeletedOrganization(String deletedOrgId) throws WebhookMgtException {

        // As a subscriber: drop its rows from every channel that was fanning out to it.
        daoFACADE.deleteChannelOrgSubscriptionsByOrgId(deletedOrgId);
        // As an owner: drop the organization subscription policies of the channels it owned.
        clearOrgSubscriptionPoliciesOwnedBy(deletedOrgId);
    }

    /**
     * Read the organization subscription policy currently in force for a channel.
     *
     * @param channelUuid Channel UUID.
     * @param owningOrgId Organization owning the channel's webhook, resolved by the caller so a failure raises
     *                    instead of reporting an ALL channel as SELECTED.
     * @return The policy in force, NONE when nothing is subscribed.
     * @throws WebhookMgtException If the organization subscription policy cannot be read.
     */
    private SubscriptionPolicy getSubscriptionPolicy(String channelUuid, String owningOrgId)
            throws WebhookMgtException {

        // Only ALL leaves a policy behind, so the other two are told apart by the stored rows alone.
        if (owningOrgId != null && OrganizationSubscriptionPolicyUtil.isPolicyHeld(channelUuid, owningOrgId)) {
            return SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS;
        }
        return daoFACADE.getChannelOrgSubscriptionCount(channelUuid) > 0
                ? SubscriptionPolicy.SELECTED_ORGS_ONLY
                : SubscriptionPolicy.NONE;
    }

    /**
     * Read the organization subscription policy of a channel whose subscription rows are already read, so the
     * rows tell SELECTED from NONE without a count query.
     *
     * @param channelUuid   Channel UUID.
     * @param owningOrgId   Organization owning the channel's webhook.
     * @param subscriptions Every subscription of the channel.
     * @return The policy in force, NONE when nothing is subscribed.
     * @throws WebhookMgtException If the organization subscription policy cannot be read.
     */
    private SubscriptionPolicy getSubscriptionPolicy(String channelUuid, String owningOrgId,
                                                     List<ChannelOrgSubscription> subscriptions)
            throws WebhookMgtException {

        if (owningOrgId != null && OrganizationSubscriptionPolicyUtil.isPolicyHeld(channelUuid, owningOrgId)) {
            return SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS;
        }
        return subscriptions.isEmpty() ? SubscriptionPolicy.NONE : SubscriptionPolicy.SELECTED_ORGS_ONLY;
    }


    /**
     * One page of the organizations subscribed to a channel, with their display names resolved.
     *
     * @param subscriptions Every subscription of the channel.
     * @param offset        Rows to skip, already validated as not negative.
     * @param limit         Rows to return, already validated as greater than zero.
     * @return That page of subscriptions.
     */
    private List<ChannelOrgSubscription> pageOf(List<ChannelOrgSubscription> subscriptions, int offset, int limit) {

        int from = Math.min(offset, subscriptions.size());
        // Capped by what remains rather than added to from, so a limit near Integer.MAX_VALUE cannot wrap.
        int to = from + Math.min(limit, subscriptions.size() - from);

        // Resolve names for the page only: a channel under ALL can fan out to the whole tree, and resolving
        // all of it to serve one page would make the cost of a page depend on the size of the tree.
        List<ChannelOrgSubscription> resolved = new ArrayList<>(to - from);
        for (ChannelOrgSubscription subscription : subscriptions.subList(from, to)) {
            resolved.add(resolveOrganization(subscription));
        }
        return resolved;
    }

    /**
     * Add the organization's display name and parent to a stored subscription row, resolved on read because
     * the link table deliberately holds ids only. One that cannot be resolved is returned unenriched.
     *
     * @param subscription Stored subscription row.
     * @return The same subscription with name and parent filled in where they could be resolved.
     */
    private ChannelOrgSubscription resolveOrganization(ChannelOrgSubscription subscription) {

        String orgName = null;
        String parentOrgId = null;
        try {
            MinimalOrganization organization = getOrganizationManager().getMinimalOrganization(
                    subscription.getSubscribedOrgId(),
                    IdentityTenantUtil.getTenantDomain(subscription.getSubscribedOrgTenantId()));
            if (organization != null) {
                orgName = organization.getName();
                parentOrgId = organization.getParentOrganizationId();
            }
        } catch (OrganizationManagementException | RuntimeException e) {
            LOG.debug("Could not resolve the subscribed organization: " + subscription.getSubscribedOrgId(), e);
        }

        return ChannelOrgSubscription.builder()
                .channelUuid(subscription.getChannelUuid())
                .subscribedOrgTenantId(subscription.getSubscribedOrgTenantId())
                .subscribedOrgId(subscription.getSubscribedOrgId())
                .orgName(orgName)
                .parentOrgId(parentOrgId)
                .build();
    }

    /**
     * Remove every webhook channel organization subscription policy held by an organization. They live in the
     * shared database with no foreign key to the channels, so nothing else removes them with the tenant.
     *
     * @param orgId Organization whose organization subscription policies are to be removed.
     * @throws WebhookMgtException If the policies cannot be read or removed.
     */
    private void clearOrgSubscriptionPoliciesOwnedBy(String orgId) throws WebhookMgtException {

        for (ResourceSharingPolicy orgSubscriptionPolicy :
                OrganizationSubscriptionPolicyUtil.getPoliciesHeldBy(orgId)) {
            OrganizationSubscriptionPolicyUtil.clearPolicy(orgSubscriptionPolicy.getResourceId());
        }
    }

    /**
     * Confirm every selected organization sits below the channel's owner. Ids resolve globally and the publish
     * path matches on tenant alone, so an unrelated hierarchy's id would route its events to this webhook.
     *
     * @param owningOrgId    Organization that owns the channel.
     * @param selectedOrgIds Organizations named in the request.
     * @throws WebhookMgtException If any of them is not a descendant of the owner.
     */
    private void validateOrganizationsAreDescendants(String owningOrgId, List<String> selectedOrgIds)
            throws WebhookMgtException {

        Set<String> descendantOrgIds = new HashSet<>(resolveDescendantOrgIds(owningOrgId));
        for (String selectedOrgId : selectedOrgIds) {
            if (!descendantOrgIds.contains(selectedOrgId)) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_SUB_ORG_NOT_IN_HIERARCHY, selectedOrgId);
            }
        }
    }

    /**
     * Every descendant of the owning organization, at any depth. Only the identifiers are needed, so the
     * recursive id query is used rather than the one building an organization object per descendant.
     *
     * @param owningOrgId Organization whose subtree is wanted.
     * @return Identifiers of every descendant, at any depth.
     */
    private List<String> resolveDescendantOrgIds(String owningOrgId) throws WebhookMgtException {

        try {
            return getOrganizationManager().getChildOrganizationsIds(owningOrgId, true);
        } catch (OrganizationManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR, e, owningOrgId);
        }
    }

    private List<String> resolveAncestorOrgIds(String orgId) throws WebhookMgtException {

        try {
            List<String> ancestors = new ArrayList<>(getOrganizationManager().getAncestorOrganizationIds(orgId));
            // The chain includes the organization itself, which cannot be its own ancestor.
            ancestors.remove(orgId);
            return ancestors;
        } catch (OrganizationManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR, e, orgId);
        }
    }

    private OrganizationManager getOrganizationManager() {

        return WebhookManagementComponentServiceHolder.getInstance().getOrganizationManager();
    }

    /**
     * Resolve what the requested organization subscriptions amount to, without writing anything. This is the
     * half that reads the organization database, kept out of the transaction that writes the result.
     *
     * @param requested    Organization subscriptions carried on the request, may be null or empty.
     * @param tenantDomain Tenant of the organization the webhook is being created in.
     * @return One entry per requested subscription that names a policy, empty when there is nothing to apply.
     * @throws WebhookMgtException If an organization cannot be resolved, or is not a descendant of the owner.
     */
    private List<PlannedOrganizationSubscription> planOrganizationSubscriptions(
            List<OrganizationSubscription> requested, String tenantDomain) throws WebhookMgtException {

        List<PlannedOrganizationSubscription> planned = new ArrayList<>();
        if (requested == null || requested.isEmpty()) {
            return planned;
        }
        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            LOG.debug("Child organization subscriptions are disabled. The organization subscriptions carried on " +
                    "the request are skipped.");
            return planned;
        }
        // Resolved once: it is the same for every channel of the webhook.
        String owningOrgId = resolveOwningOrgId(tenantDomain);
        for (OrganizationSubscription subscription : requested) {
            if (subscription.getPolicy() == null) {
                continue;
            }
            List<String> orgIdsToSubscribe;
            if (subscription.getPolicy() == SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS) {
                orgIdsToSubscribe = resolveDescendantOrgIds(owningOrgId);
            } else if (subscription.getPolicy() == SubscriptionPolicy.SELECTED_ORGS_ONLY) {
                orgIdsToSubscribe = subscription.getOrganizationIds();
                validateOrganizationsAreDescendants(owningOrgId, orgIdsToSubscribe);
            } else {
                orgIdsToSubscribe = Collections.emptyList();
            }
            planned.add(new PlannedOrganizationSubscription(subscription.getChannelUri(), subscription.getPolicy(),
                    owningOrgId, resolveTenantIdsOf(orgIdsToSubscribe)));
        }
        return planned;
    }

    /**
     * Resolve each organization to the tenant its events are raised in, which is what the publish path matches
     * on and so what is stored. Input order is kept, so a failure names the organization that caused it.
     *
     * @param orgIds Organizations to resolve.
     * @return Each organization against its tenant id.
     * @throws WebhookMgtException If an organization's tenant cannot be resolved.
     */
    private Map<String, Integer> resolveTenantIdsOf(List<String> orgIds) throws WebhookMgtException {

        Map<String, Integer> tenantIdsByOrgId = new LinkedHashMap<>();
        if (orgIds == null) {
            return tenantIdsByOrgId;
        }
        for (String orgId : orgIds) {
            try {
                // resolveTenantId returns the tenant UUID, not the numeric id. The tenant domain is the
                // unambiguous hop between an organization and its tenant id.
                tenantIdsByOrgId.put(orgId, IdentityTenantUtil.getTenantId(
                        getOrganizationManager().resolveTenantDomain(orgId)));
            } catch (OrganizationManagementException e) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR, e, orgId);
            }
        }
        return tenantIdsByOrgId;
    }

}
