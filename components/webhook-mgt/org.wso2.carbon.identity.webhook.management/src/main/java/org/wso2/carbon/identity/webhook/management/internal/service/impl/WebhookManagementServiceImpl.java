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
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.ResourceType;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.exception
        .ResourceSharingPolicyMgtException;
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
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookRunnable;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementAuditLogger;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private static final int CHANNEL_NOT_FOUND_TENANT_ID = -1;


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

        /*
         Resolved before the transaction is opened, because every one of these reads the organization database.
         A transaction is bound to the thread rather than to the template, and a nested withTransaction at a
         depth above zero reuses the connection already on the thread whatever datasource its own template was
         built from -- so an organization read made inside the identity transaction below would run against the
         identity connection. The reads are therefore done first and only their answers are carried in.
         */
        List<PlannedOrganizationSubscription> plannedSubscriptions =
                planOrganizationSubscriptions(webhook.getOrganizationSubscriptions(), tenantDomain);

        /*
         The organization subscription policies written while the transaction is open, so they can be undone if it rolls
         back. They live in the shared database and are written on their own connection, which commits itself,
         so the rollback of the identity transaction does not reach them.
         */
        List<String> orgSubscriptionPoliciesWritten = new ArrayList<>();
        Map<String, String> orgSubscriptionPoliciesCleared = new HashMap<>();
        Set<Integer> affectedTenantIds = new HashSet<>();

        // One transaction for every identity database write of the creation: the webhook row, its channel rows
        // and the organization subscriptions of those channels. The channel UUID is readable within it because
        // the read runs on the same connection as the insert that produced it.
        runInTransaction(() -> {
            daoFACADE.createWebhook(webhookToCreate, tenantId);
            applyPlannedOrganizationSubscriptions(generatedWebhookId, tenantId, plannedSubscriptions,
                    orgSubscriptionPoliciesWritten, orgSubscriptionPoliciesCleared, affectedTenantIds);
        }, ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, orgSubscriptionPoliciesWritten,
                orgSubscriptionPoliciesCleared);

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

        /*
         Resolved before the transaction is opened: these read the organization database, and a nested
         transaction reuses the connection already on the thread whatever datasource its template was built
         from. See the creation path for the whole of that reasoning.
         */
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

        List<String> orgSubscriptionPoliciesWritten = new ArrayList<>();
        Map<String, String> orgSubscriptionPoliciesCleared = new HashMap<>();
        Set<Integer> affectedTenantIds = new HashSet<>(previouslySubscribedTenantIds);

        /*
         One transaction for every identity database write of the update: the webhook row, the channels it adds
         or drops, and the organization subscriptions of the channels named in the request. A channel added here
         has no UUID until its row exists, which is why the subscriptions follow the webhook rather than
         preceding it; within one transaction the UUID is readable as soon as it is written. Channels absent
         from the request are left untouched, so a caller that omits a channel's organization subscription keeps
         its current fanout.
         */
        runInTransaction(() -> {
            daoFACADE.updateWebhook(webhook, tenantId);
            applyPlannedOrganizationSubscriptions(webhookId, tenantId, plannedSubscriptions,
                    orgSubscriptionPoliciesWritten, orgSubscriptionPoliciesCleared, affectedTenantIds);
        }, ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR, orgSubscriptionPoliciesWritten,
                orgSubscriptionPoliciesCleared);

        /*
         Cleared once the update is certain. A channel this update dropped took its subscription rows with it
         through the foreign key, but not the organization subscription policy it left in the shared
         database, which has to be removed by hand. Clearing is not reversible, so it waits until after
         the commit: were it done inside the transaction, a rollback would restore the channel rows and
         leave them with no policy to account for them, which is the direction the reconciliation sweep
         cannot repair.
         */
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
        /*
         Read before the deletion: their rows, and with them the subscription rows, are gone by the time it
         returns, and the policies they hold could no longer be located.

         Read whether or not the feature is configured on. A channel can hold a policy written while it was, and
         the configuration says whether the feature is in use, not whether what it already wrote is cleaned up.
         Leaving a policy behind strands it: nothing cascades to it from the Identity database, and a channel of
         the same URI created later is given a new UUID, so no later operation can match the policy to a channel
         again.
         */
        List<String> channelUuids = resolveChannelUuidsQuietly(webhookId, tenantId);
        Set<Integer> subscribedTenantIds = resolveSubscribedTenantIds(channelUuids);
        daoFACADE.deleteWebhook(webhookId, tenantId);
        // The policies live in the shared database and cannot cascade from the Identity database, so they are
        // removed explicitly once the channels are gone.
        clearSubscriptionPolicies(channelUuids);
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
            return daoFACADE.getActiveWebhooksWithSubscribedChildOrgs(eventProfileName, eventProfileVersion,
                    channelUri, tenantId);
        }
        return daoFACADE.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
    }

    @Override
    public List<Webhook> getWebhooksWithAcceptedChannelSubscription(String eventProfileName, String eventProfileVersion,
                                               String channelUri, String tenantDomain)
            throws WebhookMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving webhooks subscribed to channel URI: %s in tenant: %s",
                    channelUri, tenantDomain));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        /*
         The webhooks of ancestor organizations are looked for only from a sub organization, and only while the
         feature is configured on: until then a channel records no organization subscription for the widened
         lookup to find.
         */
        boolean includeChildOrganizationSubscriptions =
                IdentityUtil.isChildOrganizationSubscriptionEnabled() && isSubOrganization(tenantId);
        return daoFACADE.getWebhooksWithAcceptedChannelSubscription(eventProfileName, eventProfileVersion,
                channelUri, tenantId, includeChildOrganizationSubscriptions);
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
            String channelId = daoFACADE.getChannelUuid(webhookId, channel.getChannelUri(), tenantId);
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

    @Override
    public ChannelSubscribedOrganizations getChannelOrganizationSubscriptions(String webhookId, String channelId,
                                                                             String tenantDomain, int offset,
                                                                             int limit) throws WebhookMgtException {

        validateChildOrganizationSubscriptionEnabled();
        // Ownership is established once here. The reads below are reached directly rather than through the
        // single-value methods, each of which would repeat this same check.
        validateChannelBelongsToWebhook(webhookId, channelId, tenantDomain);
        return ChannelSubscribedOrganizations.builder()
                .channelUri(daoFACADE.getChannelUri(channelId))
                .policy(getSubscriptionPolicy(channelId))
                .totalCount(daoFACADE.getChannelOrgSubscriptionCount(channelId))
                .organizations(getSubscribedOrganizations(channelId, offset, limit))
                .build();
    }

    @Override
    public WebhookChannel getWebhookChannel(String webhookId, String channelId, String tenantDomain)
            throws WebhookMgtException {

        if (!IdentityUtil.isChildOrganizationSubscriptionEnabled()) {
            /*
             A channel is named by the UUID column that arrives with the organization subscription migration, so
             until the feature is enabled there is no channel for a request to name. Reported as a missing
             channel rather than attempted: the lookups below read that column, and the plural form of this
             operation answers with no channels at all for the same reason.
             */
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
        return getSubscriptionPolicy(channelId);
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
     * Clears the organization subscription policies of the channels a webhook update removed.
     * <p>
     * The policies live in the shared database and cannot cascade from the Identity database, so a channel
     * dropped by an update would leave its policy behind. That policy can never be matched to a channel again:
     * a channel of the same URI added later is given a UUID of its own.
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
        clearSubscriptionPolicies(removedChannelUuids);
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

        // Checked before the channel, so that a request naming a webhook that does not exist is told the webhook
        // is missing rather than the channel. A non-existent webhook has no channels, so the channel lookup below
        // would otherwise report the wrong resource as the cause.
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


    // ---------------------------------------------------------------------------------------
    // Organization subscriptions.
    //
    // Where each half is stored. UM_RESOURCE_SHARING_POLICY is not a general record of intent: it records what
    // to do when a new organization appears. Organization user sharing gates its policy writes on exactly that
    // -- a policy row is only written when the organization scope includes future organizations -- and this
    // follows the same rule.
    //   - ALL_EXISTING_AND_FUTURE_ORGS writes one policy row, held by the owning organization, and materialises
    //     every current descendant. That row is the organization subscription policy the organization
    //     creation handler reads.
    //   - SELECTED_ORGS_ONLY writes no policy row. The selection is fully resolved at configuration time and no
    //     future organization can join it, so there is nothing for a organization subscription policy to say.
    //   - NONE writes nothing and clears everything.
    // The policy is therefore read back by asking whether an organization subscription policy exists,
    // and falling back to the materialised rows.
    //
    // Atomicity. The policy lives in the shared database and the materialised rows in the Identity database, so
    // applying a policy is two commits with no transaction spanning them. The recoverable direction is
    // policy-written-rows-missing, which the reconciliation sweep repairs.
    // ---------------------------------------------------------------------------------------
    // --- Organization subscriptions ---

    /**
     * Subscribe a newly created organization to every channel whose ancestor left a organization subscription policy.
     *
     * @param newOrgId Organization that has just been created.
     * @throws WebhookMgtException If the subscriptions cannot be resolved or stored.
     */
    public void subscribeNewOrganization(String newOrgId) throws WebhookMgtException {

        List<String> ancestorOrgIds = resolveAncestorOrgIds(newOrgId);
        if (ancestorOrgIds.isEmpty()) {
            return;
        }

        List<ChannelOrgSubscription> subscriptions = new ArrayList<>();
        for (String ancestorOrgId : ancestorOrgIds) {
            for (ResourceSharingPolicy orgSubscriptionPolicy : getOrgSubscriptionPoliciesOf(ancestorOrgId)) {
                String channelUuid = orgSubscriptionPolicy.getResourceId();
                /*
                 An organization subscription policy can outlive the channel it was written for: the two are held in
                 different databases and neither cascades to the other, so a channel removed while the feature
                 was configured off leaves its policy behind. Subscribing to a channel that is gone fails
                 the insert, and since every subscription of this organization is written in one batch it would
                 take the channels that do still exist down with it. Such a policy is passed over and
                 reported, so the organization is subscribed to the rest.
                 */
                if (daoFACADE.getChannelUri(channelUuid) == null) {
                    LOG.warn("Channel: " + channelUuid + " named by a subscription policy of organization: " +
                            ancestorOrgId + " no longer exists, so organization: " + newOrgId +
                            " was not subscribed to it. The policy is stale and can be removed.");
                    continue;
                }
                subscriptions.addAll(buildSubscriptions(channelUuid, Collections.singletonList(newOrgId)));
            }
        }
        if (subscriptions.isEmpty()) {
            return;
        }
        daoFACADE.addChannelOrgSubscriptions(subscriptions);

        Set<Integer> affectedTenantIds = new HashSet<>();
        for (ChannelOrgSubscription subscription : subscriptions) {
            affectedTenantIds.add(subscription.getSubscribedOrgTenantId());
        }
        clearActiveWebhooksCache(affectedTenantIds);
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
     * @return The policy in force, NONE when nothing is subscribed.
     * @throws WebhookMgtException If the organization subscription policy cannot be read.
     */
    private SubscriptionPolicy getSubscriptionPolicy(String channelUuid) throws WebhookMgtException {

        try {
            if (findOrgSubscriptionPolicy(channelUuid).isPresent()) {
                return SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS;
            }
            return daoFACADE.getChannelOrgSubscriptionCount(channelUuid) > 0
                    ? SubscriptionPolicy.SELECTED_ORGS_ONLY
                    : SubscriptionPolicy.NONE;
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR, e, channelUuid);
        }
    }


    /**
     * One page of the organizations subscribed to a channel, with their display names resolved.
     *
     * @param channelUuid Channel UUID.
     * @param offset      Rows to skip.
     * @param limit       Rows to return, negative for all of them.
     * @return That page of subscriptions.
     * @throws WebhookMgtException If the subscriptions cannot be read.
     */
    private List<ChannelOrgSubscription> getSubscribedOrganizations(String channelUuid, int offset, int limit)
            throws WebhookMgtException {

        List<ChannelOrgSubscription> subscriptions = daoFACADE.getChannelOrgSubscriptions(channelUuid);

        int from = Math.min(Math.max(offset, 0), subscriptions.size());
        // Capped by what remains rather than added to from, so a limit near Integer.MAX_VALUE cannot wrap.
        int to = limit < 0 ? subscriptions.size() : from + Math.min(limit, subscriptions.size() - from);

        // Resolve names for the page only. A channel under ALL can fan out to every organization in
        // the tree, and resolving all of them to serve fifty would make the cost of a page depend on
        // the size of the tree.
        List<ChannelOrgSubscription> resolved = new ArrayList<>(to - from);
        for (ChannelOrgSubscription subscription : subscriptions.subList(from, to)) {
            resolved.add(resolveOrganization(subscription));
        }
        return resolved;
    }

    /**
     * Discard the organization subscription policies of a set of channels, best effort.
     *
     * @param channelUuids Channels whose policies are to be discarded.
     */
    /**
     * Whether the channel already holds an organization subscription policy.
     *
     * @param channelUuid Channel to read.
     * @return true if a policy is held for it.
     * @throws WebhookMgtException If the policy cannot be read.
     */
    private boolean hasOrgSubscriptionPolicy(String channelUuid) throws WebhookMgtException {

        try {
            return findOrgSubscriptionPolicy(channelUuid).isPresent();
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR, e, channelUuid);
        }
    }

    /**
     * Write back the organization subscription policies a rolled-back transaction had cleared.
     * <p>
     * Best effort, like the clearing it mirrors: the transaction has already failed and the caller is being
     * told so, and a policy that cannot be written back is reported for the reconciliation sweep rather than
     * replacing the original failure.
     *
     * @param owningOrgIdsByChannelUuid Channels whose policy was cleared, against the organization that held it.
     */
    private void restoreSubscriptionPolicies(Map<String, String> owningOrgIdsByChannelUuid) {

        if (owningOrgIdsByChannelUuid == null || owningOrgIdsByChannelUuid.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : owningOrgIdsByChannelUuid.entrySet()) {
            try {
                writeOrgSubscriptionPolicy(entry.getKey(), entry.getValue());
            } catch (WebhookMgtException e) {
                LOG.warn("Failed to restore the organization subscription policy of channel: " + entry.getKey() +
                        " after a failed transaction. The channel subscribes the organizations it already " +
                        "held, but no longer subscribes newly created ones until the policy is set again.", e);
            }
        }
    }

    private void clearSubscriptionPolicies(List<String> channelUuids) {

        if (channelUuids == null || channelUuids.isEmpty()) {
            return;
        }
        for (String channelUuid : channelUuids) {
            try {
                clearOrgSubscriptionPolicy(channelUuid);
            } catch (WebhookMgtException e) {
                LOG.warn("Failed to delete the organization subscription policy of channel: " + channelUuid +
                        ". The policy is inert and will be removed by reconciliation.", e);
            }
        }
    }

    /**
     * Add the organization's display name and parent to a stored subscription row.
     * <p>
     * The link table holds ids only, deliberately: a name is not identity and would go stale the
     * moment an organization is renamed. The sub-resource that lists subscribed organizations is
     * meant for humans, though, so the name and parent are resolved here, on read.
     * <p>
     * An organization that cannot be resolved is returned unenriched rather than dropped or failed
     * on. The subscription is a real stored fact; a naming lookup that cannot answer is not a
     * reason to hide it or to fail the whole page.
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
     * Remove every webhook channel organization subscription policy held by an organization.
     * <p>
     * The policies live in the shared database and the channels they name live in the identity
     * database, so there is no foreign key between them and nothing removes these rows when the
     * owning organization's tenant goes away.
     *
     * @param orgId Organization whose organization subscription policies are to be removed.
     * @throws WebhookMgtException If the policies cannot be read or removed.
     */
    private void clearOrgSubscriptionPoliciesOwnedBy(String orgId) throws WebhookMgtException {

        for (ResourceSharingPolicy orgSubscriptionPolicy : getOrgSubscriptionPoliciesOf(orgId)) {
            clearOrgSubscriptionPolicy(orgSubscriptionPolicy.getResourceId());
        }
    }

    private Optional<ResourceSharingPolicy> findOrgSubscriptionPolicy(String channelUuid)
            throws ResourceSharingPolicyMgtException {

        String owningOrgId = resolveOwningOrgIdQuietly(channelUuid);
        if (owningOrgId == null) {
            return Optional.empty();
        }
        return getSharingService().getResourceSharingPolicyByResourceKeys(
                ResourceType.WEBHOOK_CHANNEL.name(), channelUuid, owningOrgId, owningOrgId);
    }

    private List<ResourceSharingPolicy> getOrgSubscriptionPoliciesOf(String orgId) throws WebhookMgtException {

        try {
            return getSharingService().getResourceSharingPoliciesByResourceType(
                    Collections.singletonList(orgId), ResourceType.WEBHOOK_CHANNEL.name());
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR, e, orgId);
        }
    }

    private void writeOrgSubscriptionPolicy(String channelUuid, String owningOrgId) throws WebhookMgtException {

        try {
            getSharingService().addResourceSharingPolicy(new ResourceSharingPolicy.Builder()
                    .withResourceType(ResourceType.WEBHOOK_CHANNEL)
                    .withResourceId(channelUuid)
                    .withInitiatingOrgId(owningOrgId)
                    .withPolicyHoldingOrgId(owningOrgId)
                    .withSharingPolicy(PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS)
                    .build());
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SET_ERROR, e, channelUuid);
        }
    }

    private void clearOrgSubscriptionPolicy(String channelUuid) throws WebhookMgtException {

        try {
            getSharingService().deleteResourceSharingPolicyByResourceTypeAndId(
                    ResourceType.WEBHOOK_CHANNEL, channelUuid);
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SET_ERROR, e, channelUuid);
        }
    }

    /**
     * Confirm that every selected organization sits below the organization that owns the channel.
     * <p>
     * Organization identifiers are resolved globally, so without this an identifier belonging to an unrelated
     * hierarchy would resolve to its own tenant and be written as a subscriber. The publish-side lookup matches
     * subscriptions on the subscribed tenant alone, so that row would route another hierarchy's events to this
     * webhook. Ownership of the organizations named in the request is therefore established here rather than
     * assumed.
     * <p>
     * Only SELECTED_ORGS_ONLY needs it. ALL derives its organizations from the owner's own subtree, and the
     * organization lifecycle handlers write rows they resolved from the hierarchy themselves.
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
     * Every descendant of the owning organization, at any depth.
     * <p>
     * The identifiers are all that is needed here, so the recursive id query is used rather than the one that
     * builds an organization object per descendant.
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

    private List<ChannelOrgSubscription> buildSubscriptions(String channelUuid, List<String> orgIds)
            throws WebhookMgtException {

        List<ChannelOrgSubscription> subscriptions = new ArrayList<>();
        if (orgIds == null) {
            return subscriptions;
        }
        for (String orgId : orgIds) {
            try {
                subscriptions.add(ChannelOrgSubscription.builder()
                        .channelUuid(channelUuid)
                        .subscribedOrgId(orgId)
                        // resolveTenantId returns the tenant UUID, not the numeric id. The tenant
                        // domain is the unambiguous hop between an organization and its tenant id.
                        .subscribedOrgTenantId(IdentityTenantUtil.getTenantId(
                                getOrganizationManager().resolveTenantDomain(orgId)))
                        .build());
            } catch (OrganizationManagementException e) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR, e, orgId);
            }
        }
        return subscriptions;
    }

    /**
     * Resolve the organization owning the webhook a channel belongs to. Returns null rather than
     * failing, so that reading the policy of a channel that has since been deleted reports NONE
     * instead of erroring.
     */
    private String resolveOwningOrgIdQuietly(String channelUuid) {

        try {
            int owningTenantId = daoFACADE.getOwningTenantId(channelUuid);
            // -1 is the DAO's "channel not found" sentinel. It must be compared exactly: the super
            // tenant's id is -1234, so a sign test would discard every channel owned by carbon.super.
            if (owningTenantId == CHANNEL_NOT_FOUND_TENANT_ID) {
                return null;
            }
            return getOrganizationManager().resolveOrganizationId(
                    IdentityTenantUtil.getTenantDomain(owningTenantId));
        } catch (WebhookMgtException | OrganizationManagementException e) {
            LOG.debug("Could not resolve the owning organization of channel: " + channelUuid, e);
            return null;
        }
    }

    private ResourceSharingPolicyHandlerService getSharingService() {

        return WebhookManagementComponentServiceHolder.getInstance().getResourceSharingPolicyHandlerService();
    }

    private OrganizationManager getOrganizationManager() {

        return WebhookManagementComponentServiceHolder.getInstance().getOrganizationManager();
    }

    /**
     * Run every identity database write of an operation in one transaction.
     * <p>
     * carbon-database-utils binds a transaction to the thread, so the transactions the DAO opens inside this
     * one join it rather than standing alone: they share its connection and only the outermost commits. A
     * failure anywhere inside therefore rolls back all of them.
     * <p>
     * Anything the action throws arrives wrapped in a {@link TransactionException}, so a client error raised
     * inside is unwrapped here and rethrown as itself rather than reported as a server failure.
     *
     * @param action                     Writes to perform.
     * @param errorMessage               Error to report when the transaction fails for any other reason.
     * @param orgSubscriptionPoliciesToUndo Organization subscription policies written inside the
     *                                   transaction, cleared when it rolls back because their own
     *                                   commits are beyond its reach.
     * @throws WebhookMgtException If the action fails.
     */
    private void runInTransaction(WebhookRunnable action, ErrorMessage errorMessage,
                                  List<String> orgSubscriptionPoliciesToUndo,
                                  Map<String, String> orgSubscriptionPoliciesToRestore)
            throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                action.run();
                return null;
            });
        } catch (TransactionException e) {
            clearSubscriptionPolicies(orgSubscriptionPoliciesToUndo);
            restoreSubscriptionPolicies(orgSubscriptionPoliciesToRestore);
            if (e.getCause() instanceof WebhookMgtException) {
                throw (WebhookMgtException) e.getCause();
            }
            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
        }
    }

    /**
     * Resolve what the requested organization subscriptions amount to, without writing anything.
     * <p>
     * This is the half of applying a subscription that reads the organization database: the organization owning
     * the webhook, the descendants an ALL policy covers, the rejection of a selected organization that is not a
     * descendant, and the tenant each subscribed organization resolves to. It is kept out of the transaction
     * that writes the result, so that no organization read runs on the identity connection.
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
     * Resolve each organization to the tenant its events are raised in.
     * <p>
     * The publish path matches a subscription on the subscribed tenant, so the tenant is what is stored. The
     * order of the given organizations is kept, so that a failure names the organization that caused it.
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

    /**
     * Write the organization subscriptions that were planned, inside the caller's transaction.
     * <p>
     * Only the organization subscription policy escapes it, because it is held in the shared database.
     * It is written before the rows it stands for, keeping the recoverable direction of a partial failure
     * the one the reconciliation sweep repairs: a policy whose rows are missing, rather than rows that no
     * policy accounts for.
     *
     * @param webhookId                  Webhook the channels belong to.
     * @param tenantId                   Tenant of the organization owning the webhook.
     * @param planned                    Subscriptions resolved before the transaction was opened.
     * @param orgSubscriptionPoliciesWritten Collects the channels whose policy was written, for undoing.
     * @param orgSubscriptionPoliciesCleared Collects the channels whose policy was cleared, against the
     *                                   owning organization, so a rollback can write it back.
     * @param affectedTenantIds          Collects the tenants whose cached publish-path answer is now stale.
     * @throws WebhookMgtException If a channel cannot be resolved or the subscriptions cannot be stored.
     */
    private void applyPlannedOrganizationSubscriptions(String webhookId, int tenantId,
                                                       List<PlannedOrganizationSubscription> planned,
                                                       List<String> orgSubscriptionPoliciesWritten,
                                                       Map<String, String> orgSubscriptionPoliciesCleared,
                                                       Set<Integer> affectedTenantIds) throws WebhookMgtException {

        for (PlannedOrganizationSubscription subscription : planned) {
            String channelUuid = daoFACADE.getChannelUuid(webhookId, subscription.channelUri, tenantId);
            if (StringUtils.isEmpty(channelUuid)) {
                /*
                 The channel was written moments ago in this same transaction and the request was already
                 checked against the webhook's own channels, so this is not a bad request: either the channel
                 UUID column is not in use while organization subscriptions are, or the insert did not take.
                 */
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR, subscription.channelUri, webhookId);
            }
            affectedTenantIds.addAll(daoFACADE.getSubscribedOrgTenantIds(channelUuid));

            boolean policyAlreadyHeld = hasOrgSubscriptionPolicy(channelUuid);
            boolean policyWanted = subscription.policy == SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS;

            /*
             The subscription rows are expressed as a replace, as on the update path, so that a channel
             carrying stale rows from a previous attempt cannot accumulate them. The policy is not: it lives
             in the shared database and commits on its own connection, so clearing it is outside the reach of
             a rollback. It is therefore left alone when the channel already holds the policy it is going to
             keep, and recorded when it has to go, so that a rollback can put it back.
             */
            if (policyAlreadyHeld && !policyWanted) {
                clearOrgSubscriptionPolicy(channelUuid);
                orgSubscriptionPoliciesCleared.put(channelUuid, subscription.owningOrgId);
            }
            daoFACADE.deleteChannelOrgSubscriptions(channelUuid);

            if (subscription.policy == SubscriptionPolicy.NONE) {
                continue;
            }
            if (policyWanted && !policyAlreadyHeld) {
                // Only ALL leaves an organization subscription policy: it is the one policy whose meaning
                // extends to organizations that do not exist yet.
                writeOrgSubscriptionPolicy(channelUuid, subscription.owningOrgId);
                orgSubscriptionPoliciesWritten.add(channelUuid);
            }
            List<ChannelOrgSubscription> rows = new ArrayList<>();
            for (Map.Entry<String, Integer> organization : subscription.tenantIdsByOrgId.entrySet()) {
                rows.add(ChannelOrgSubscription.builder()
                        .channelUuid(channelUuid)
                        .subscribedOrgId(organization.getKey())
                        .subscribedOrgTenantId(organization.getValue())
                        .build());
                affectedTenantIds.add(organization.getValue());
            }
            daoFACADE.addChannelOrgSubscriptions(rows);
        }
    }

    /**
     * A requested organization subscription with everything the organization database had to say about it
     * already resolved, so that applying it needs only the identity database.
     */
    private static class PlannedOrganizationSubscription {

        private final String channelUri;
        private final SubscriptionPolicy policy;
        private final String owningOrgId;
        private final Map<String, Integer> tenantIdsByOrgId;

        PlannedOrganizationSubscription(String channelUri, SubscriptionPolicy policy, String owningOrgId,
                                        Map<String, Integer> tenantIdsByOrgId) {

            this.channelUri = channelUri;
            this.policy = policy;
            this.owningOrgId = owningOrgId;
            this.tenantIdsByOrgId = tenantIdsByOrgId;
        }
    }

}
