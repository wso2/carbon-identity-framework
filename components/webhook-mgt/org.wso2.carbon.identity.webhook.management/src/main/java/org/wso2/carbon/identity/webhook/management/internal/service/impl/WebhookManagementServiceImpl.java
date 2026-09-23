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
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCache;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtServerException;
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
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.service.WebhookChannelService;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private final WebhookChannelService webhookChannelService;
    private final Map<Integer, String> transmitterIssuers = new ConcurrentHashMap<>();

    private WebhookManagementServiceImpl() {

        daoFACADE = new WebhookManagementDAOFacade(new CacheBackedWebhookManagementDAO(new WebhookManagementDAOImpl()));
        webhookChannelService = new WebhookChannelServiceImpl();
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
                .tenantId(tenantId)
                .build();

        daoFACADE.createWebhook(webhookToCreate, tenantId);
        // The subscriptions cannot be written in the same transaction: the channels have no UUID until the
        // webhook row exists, and the standing instruction of an ALL policy lives in the shared database, which
        // no single transaction spans. The creation is therefore undone by hand when they fail.
        try {
            applyOrganizationSubscriptions(generatedWebhookId, webhook.getOrganizationSubscriptions(), tenantDomain);
        } catch (WebhookMgtException | IdentityRuntimeException e) {
            compensateFailedWebhookCreation(generatedWebhookId, tenantId, e);
            throw e;
        }
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

        // Read the organizations subscribed before the change: an organization dropped from a policy must have
        // its cache evicted too, and it is no longer resolvable once the subscription rows are rewritten.
        Set<Integer> previouslySubscribedTenantIds = resolveSubscribedTenantIds(webhookId, tenantId);

        daoFACADE.updateWebhook(webhook, tenantId);
        // Applied after the webhook is persisted, because a channel added by this update has no UUID to hang a
        // subscription off until its row exists. Channels absent from the list are left untouched, so a caller
        // that omits a channel's organization subscription keeps its current fanout.
        applyOrganizationSubscriptions(webhookId, webhook.getOrganizationSubscriptions(), tenantDomain);

        auditLogger.printAuditLog(WebhookManagementAuditLogger.Operation.UPDATE, webhook);
        Set<Integer> affectedTenantIds = new HashSet<>(previouslySubscribedTenantIds);
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
        // Read the channels before the deletion: their rows, and with them the subscription rows, are gone by
        // the time it returns, and the policies they hold could no longer be located.
        List<String> channelUuids = IdentityUtil.isChildOrganizationSubscriptionEnabled()
                ? webhookChannelService.getChannelUuids(webhookId, tenantId) : Collections.emptyList();
        Set<Integer> subscribedTenantIds = resolveSubscribedTenantIds(channelUuids);
        daoFACADE.deleteWebhook(webhookId, tenantId);
        // The policies live in the shared database and cannot cascade from the Identity database, so they are
        // removed explicitly once the channels are gone.
        webhookChannelService.clearSubscriptionPolicies(channelUuids);
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

        if (offset < 0) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_REQUEST, "The pagination offset must not be negative.");
        }
        if (limit <= 0) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_INVALID_REQUEST, "The pagination limit must be greater than zero.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Getting webhooks for tenant: %s with offset: %d and limit: %d",
                    tenantDomain, offset, limit));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

        // The window is currently applied after the read rather than in SQL: webhooks are configuration rather
        // than data, so the collection is small and bounded per tenant. Callers see a paged contract either way,
        // so this can move into the DAO as a LIMIT/OFFSET query without affecting them.
        List<Webhook> webhooks = daoFACADE.getWebhooks(tenantId);
        if (offset >= webhooks.size()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(webhooks.subList(offset, Math.min(offset + limit, webhooks.size())));
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
            return daoFACADE.getActiveWebhooksWithSubscribedChildOrgs(eventProfileName, eventProfileVersion,
                    channelUri, tenantId);
        }
        return daoFACADE.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
    }

    /**
     * Check whether the given tenant is a sub-organization.
     * <p>
     * Only a sub-organization can be subscribed to an ancestor's webhook, so events raised in a root
     * organization keep using the tenant-only lookup. If the check fails, this returns true: the lookup that
     * includes subscribed child organizations also returns the tenant's own webhooks, so no webhook is missed.
     *
     * @param tenantId Tenant ID of the organization that raised the event.
     * @return true if the tenant is a sub-organization, or if that cannot be determined.
     */
    private boolean isSubOrganization(int tenantId) {

        try {
            return OrganizationManagementUtil.isOrganization(tenantId);
        } catch (OrganizationManagementException e) {
            LOG.error("Error while checking whether tenant id: " + tenantId + " is an organization. " +
                    "Falling back to the lookup that includes subscribed child organizations.", e);
            return true;
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
        List<WebhookChannel> channels = new ArrayList<>();
        // The subscription record carries the channel URI and the webhook's status for it; the identity and the
        // organization policy are resolved alongside, so one pass produces the whole channel.
        for (Subscription channel : daoFACADE.getWebhookEvents(webhookId, tenantId)) {
            String channelId = webhookChannelService.getChannelUuid(webhookId, channel.getChannelUri(), tenantId);
            if (channelId == null) {
                continue;
            }
            channels.add(WebhookChannel.builder()
                    .channelUri(channel.getChannelUri())
                    .channelId(channelId)
                    .status(channel.getStatus())
                    .policy(resolveSubscriptionPolicy(channelId))
                    .build());
        }
        return channels;
    }


    /**
     * Issuer URL per owning tenant. The mapping of a tenant to its organization and to the root of
     * its hierarchy does not change for the life of the tenant, and this is read once per webhook
     * per event, so it is resolved once and kept.
     */

    @Override
    public String resolveTransmitterIssuer(int tenantId) throws WebhookMgtException {

        String cached = transmitterIssuers.get(tenantId);
        if (cached != null) {
            return cached;
        }

        String ownerTenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        try {
            // The webhook owner is always in the same hierarchy as the event being published, so the
            // root of that hierarchy is the one already resolved on this thread. Reading it from the
            // identity context avoids a second hierarchy walk on the publish path.
            String rootTenantDomain = ownerTenantDomain;
            RootOrganization rootOrganization =
                    IdentityContext.getThreadLocalIdentityContext().getRootOrganization();
            if (rootOrganization != null && StringUtils.isNotBlank(rootOrganization.getAssociatedTenantDomain())) {
                rootTenantDomain = rootOrganization.getAssociatedTenantDomain();
            }

            String path;
            if (StringUtils.equals(ownerTenantDomain, rootTenantDomain)) {
                // The root of a hierarchy is addressed by its tenant alone.
                path = "/t/" + rootTenantDomain;
            } else {
                String owningOrganizationId =
                        WebhookManagementComponentServiceHolder.getInstance().getOrganizationManager()
                                .resolveOrganizationId(ownerTenantDomain);
                path = "/t/" + rootTenantDomain + "/o/" + owningOrganizationId;
            }

            String issuer = ServiceURLBuilder.create().addPath(path).build().getAbsolutePublicURL();
            transmitterIssuers.put(tenantId, issuer);
            return issuer;
        } catch (OrganizationManagementException | URLBuilderException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR, e, ownerTenantDomain);
        }
    }


    @Override
    public ChannelSubscribedOrganizations getChannelOrganizationSubscriptions(String webhookId, String channelId,
                                                                             String tenantDomain, int offset,
                                                                             int limit) throws WebhookMgtException {

        validateChildOrganizationSubscriptionEnabled();
        // Ownership is established once here. The reads below are reached directly rather than through the
        // single-value methods, each of which would repeat this same check.
        validateChannelBelongsToWebhook(webhookId, channelId, tenantDomain);
        return ChannelSubscribedOrganizations.builder()
                .channelUri(webhookChannelService.getChannelUri(channelId))
                .policy(webhookChannelService.getSubscriptionPolicy(channelId))
                .totalCount(webhookChannelService.getSubscribedOrganizationCount(channelId))
                .organizations(webhookChannelService.getSubscribedOrganizations(channelId, offset, limit))
                .build();
    }

    @Override
    public WebhookChannel getWebhookChannel(String webhookId, String channelId, String tenantDomain)
            throws WebhookMgtException {

        // Ownership is established once here. The reads below are reached directly rather than through the
        // single-value methods, each of which would repeat this same check.
        validateChannelBelongsToWebhook(webhookId, channelId, tenantDomain);
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String channelUri = webhookChannelService.getChannelUri(channelId);
        return WebhookChannel.builder()
                .channelId(channelId)
                .channelUri(channelUri)
                .status(resolveChannelSubscriptionStatus(webhookId, channelUri, tenantId))
                .policy(resolveSubscriptionPolicy(channelId))
                .build();
    }

    /**
     * The webhook's subscription status for one of its channels.
     * <p>
     * The status is held per subscribed channel on the webhook rather than on the channel row, so it is found by
     * matching the channel URI against the webhook's subscriptions.
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

    @Override
    public void updateChannelSubscription(String webhookId, String channelId, SubscriptionPolicy policy,
                                          List<String> organizationIds, String tenantDomain)
            throws WebhookMgtException {

        validateChildOrganizationSubscriptionEnabled();
        validateChannelBelongsToWebhook(webhookId, channelId, tenantDomain);
        webhookChannelService.setSubscriptionPolicy(channelId, resolveOwningOrgId(tenantDomain), policy,
                organizationIds);
    }

    /**
     * Clear the active webhooks cache of every organization this webhook fans out to.
     * <p>
     * The cache is keyed by the tenant the event is raised in, but a webhook owned by an ancestor
     * now appears in the cached answer for each descendant that was opted in. A change to the
     * webhook therefore invalidates entries in tenants other than its own, which the cache-backed
     * DAO cannot know about — it only clears the tenant it was handed.
     * <p>
     * Best effort: the webhook change has already been committed by the time this runs, so a
     * failure here is logged rather than raised. The cost of missing one is a stale routing
     * decision until the entry is evicted, not a lost or misdelivered event.
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
     * @param channelId Channel UUID.
     * @return The policy, or NONE while child organization subscriptions are disabled, as no organization can
     * be subscribed then.
     * @throws WebhookMgtException If the policy could not be read.
     */
    private SubscriptionPolicy resolveSubscriptionPolicy(String channelId) throws WebhookMgtException {

        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            return SubscriptionPolicy.NONE;
        }
        return webhookChannelService.getSubscriptionPolicy(channelId);
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
                subscribedTenantIds.addAll(webhookChannelService.getSubscribedOrgTenantIds(channelUuid));
            } catch (WebhookMgtException | IdentityRuntimeException e) {
                LOG.warn("Could not resolve the organizations subscribed to channel: " + channelUuid +
                        ". Their active webhooks cache may serve a stale answer until it is evicted.", e);
            }
        }
        return subscribedTenantIds;
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
            return webhookChannelService.getChannelUuids(webhookId, tenantId);
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

        // Checked before the channel, so that a request naming a webhook that does not exist is told the webhook
        // is missing rather than the channel. A non-existent webhook has no channels, so the channel lookup below
        // would otherwise report the wrong resource as the cause.
        if (!isWebhookExists(webhookId, tenantId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND, webhookId);
        }
        if (!webhookChannelService.getChannelUuids(webhookId, tenantId).contains(channelId)) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_CHANNEL_NOT_FOUND, channelId, webhookId);
        }
    }

    private String resolveOwningOrgId(String tenantDomain) throws WebhookMgtException {

        try {
            return WebhookManagementComponentServiceHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);
        } catch (OrganizationManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR, e, tenantDomain);
        }
    }

    /**
     * Enforce the rule that the policy discriminates the rest of the entry: a selection must
     * actually select something. NONE and ALL ignore any organizations supplied, so nothing is
     * validated for them and the values are dropped rather than rejected.
     * <p>
     * The channel each entry names is checked here rather than where the subscription is written,
     * because the write happens after the webhook row is committed: a channel URI the webhook does
     * not subscribe to would otherwise be reported only once the webhook already exists, leaving the
     * caller with a failed request and a created webhook.
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
        for (OrganizationSubscription organizationSubscription : organizationSubscriptions) {
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

    /**
     * Undo a webhook creation whose organization subscriptions could not be applied.
     * <p>
     * Deletion goes through the facade rather than {@link #deleteWebhook(String, String)}: the facade also clears
     * the sharing policies a partially applied subscription may have written to the shared database, and it emits
     * no audit entry. The ADD entry has not been written at this point, so a DELETE entry on its own would record
     * the removal of a webhook that, as far as the audit trail is concerned, never existed.
     * <p>
     * A failure to remove it is reported rather than absorbed. The two outcomes are not equivalent to the caller:
     * once the webhook is gone the request can simply be retried, whereas a webhook left behind makes a retry fail
     * on the duplicate endpoint instead. The original failure is carried as the cause, so the reason the rollback
     * was needed is not lost, and the removal failure travels with it as a suppressed exception.
     *
     * @param webhookId       Webhook to remove.
     * @param tenantId        Tenant owning the webhook.
     * @param originalFailure Failure that made the rollback necessary, kept as the cause.
     * @throws WebhookMgtException If the webhook could not be removed.
     */
    private void compensateFailedWebhookCreation(String webhookId, int tenantId, Exception originalFailure)
            throws WebhookMgtException {

        // Read before the deletion: the channel rows, and with them the subscription rows, are gone once it
        // returns. Resolved quietly, so that a failure here cannot displace the failure being compensated for.
        List<String> channelUuids = resolveChannelUuidsQuietly(webhookId, tenantId);
        Set<Integer> subscribedTenantIds = resolveSubscribedTenantIds(channelUuids);
        try {
            daoFACADE.deleteWebhook(webhookId, tenantId);
        } catch (WebhookMgtException | IdentityRuntimeException e) {
            LOG.error("Could not remove webhook: " + webhookId + " after its organization subscriptions failed to " +
                    "apply. It remains with incomplete organization subscriptions and has to be removed manually.", e);
            WebhookMgtServerException compensationFailure = WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_CREATION_COMPENSATION_ERROR, originalFailure, webhookId);
            compensationFailure.addSuppressed(e);
            throw compensationFailure;
        }
        // A partly applied ALL policy can already have written a standing instruction, so the channels are
        // cleared here too rather than only on the ordinary delete path.
        webhookChannelService.clearSubscriptionPolicies(channelUuids);
        clearActiveWebhooksCache(subscribedTenantIds);
    }

    /**
     * Apply the organization subscriptions carried on a newly created webhook.
     * <p>
     * This runs after the webhook is persisted, because a channel has no UUID to hang a
     * subscription off until its row exists.
     * <p>
     * Nothing is applied while child organization subscriptions are disabled, so that the webhook itself is
     * still created or updated as it was before the feature existed.
     * <p>
     * A policy of NONE is applied rather than skipped. On a newly created channel it is already the
     * effective state and the writing is a no-op, but on an update it is a real transition -- removing
     * the fanout a channel currently has -- and skipping it would silently discard the request.
     */
    private void applyOrganizationSubscriptions(String webhookId,
                                                List<OrganizationSubscription> organizationSubscriptions,
                                                String tenantDomain) throws WebhookMgtException {

        if (organizationSubscriptions == null || organizationSubscriptions.isEmpty()) {
            return;
        }
        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            LOG.debug("Child organization subscriptions are disabled. Skipping the organization subscriptions " +
                    "carried on webhook: " + webhookId + ".");
            return;
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        // Resolved once: it is the same for every channel of the webhook.
        String owningOrgId = resolveOwningOrgId(tenantDomain);
        for (OrganizationSubscription organizationSubscription : organizationSubscriptions) {
            if (organizationSubscription.getPolicy() == null) {
                continue;
            }
            String channelId = webhookChannelService.getChannelUuid(
                    webhookId, organizationSubscription.getChannelUri(), tenantId);
            if (StringUtils.isEmpty(channelId)) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_CHANNEL_NOT_FOUND,
                        organizationSubscription.getChannelUri(), webhookId);
            }
            // The subscription service is called directly rather than through updateChannelSubscription: the
            // channel was just resolved from this webhook in this tenant, so it belongs to it by construction and
            // the ownership check there would repeat the lookup. That check stays for callers that supply a
            // channel id of their own.
            webhookChannelService.setSubscriptionPolicy(channelId, owningOrgId,
                    organizationSubscription.getPolicy(), organizationSubscription.getOrganizationIds());
        }
    }
}
