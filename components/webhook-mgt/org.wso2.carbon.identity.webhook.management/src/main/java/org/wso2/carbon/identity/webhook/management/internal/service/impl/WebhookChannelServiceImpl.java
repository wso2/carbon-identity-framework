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

package org.wso2.carbon.identity.webhook.management.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.ResourceType;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.exception
        .ResourceSharingPolicyMgtException;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.model.ResourceSharingPolicy;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCache;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookChannelDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookChannelDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.service.WebhookChannelService;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link WebhookChannelService}.
 * <p>
 * <b>Where each half is stored.</b> UM_RESOURCE_SHARING_POLICY is not a general record of intent:
 * it records <em>what to do when a new organization appears</em>. Organization user sharing gates
 * its policy writes on exactly that — a policy row is only written when the organization scope
 * includes future organizations — and this service follows the same rule.
 * <ul>
 *   <li>{@link SubscriptionPolicy#ALL_EXISTING_AND_FUTURE_ORGS} writes one policy row, held by the owning
 *       organization, and materialises every current descendant. The policy row is the standing instruction
 *       the organization creation handler reads.</li>
 *   <li>{@link SubscriptionPolicy#SELECTED_ORGS_ONLY} writes <b>no policy row</b>. The selection is
 *       fully resolved at configuration time and no future organization can join it, so there is
 *       nothing for a standing instruction to say.</li>
 *   <li>{@link SubscriptionPolicy#NONE} writes nothing and clears everything.</li>
 * </ul>
 * The policy is therefore read back by asking whether a standing instruction exists, and falling
 * back to the materialised rows.
 * <p>
 * <b>Atomicity.</b> The policy lives in the shared database and the materialised rows in the
 * Identity database, so applying a policy is two commits with no transaction spanning them. The
 * recoverable direction is policy-written-rows-missing, which the reconciliation sweep repairs.
 */
public class WebhookChannelServiceImpl implements WebhookChannelService {

    private static final Log LOG = LogFactory.getLog(WebhookChannelServiceImpl.class);

    private static final int CHANNEL_NOT_FOUND_TENANT_ID = -1;

    private final WebhookChannelDAO webhookChannelDAO;

    public WebhookChannelServiceImpl() {

        this.webhookChannelDAO = new WebhookChannelDAOImpl();
    }

    @Override
    public SubscriptionPolicy getSubscriptionPolicy(String channelUuid) throws WebhookMgtException {

        try {
            if (getStandingInstruction(channelUuid).isPresent()) {
                return SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS;
            }
            return webhookChannelDAO.getChannelOrgSubscriptionCount(channelUuid) > 0
                    ? SubscriptionPolicy.SELECTED_ORGS_ONLY
                    : SubscriptionPolicy.NONE;
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR, e, channelUuid);
        }
    }

    @Override
    public void setSubscriptionPolicy(String channelUuid, String owningOrgId, SubscriptionPolicy policy,
                                      List<String> selectedOrgIds) throws WebhookMgtException {

        if (policy == SubscriptionPolicy.SELECTED_ORGS_ONLY) {
            if (selectedOrgIds == null || selectedOrgIds.isEmpty()) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SELECTED_ORGS_REQUIRED);
            }
            // Validated before anything is cleared below: a rejection after the clear would leave the channel
            // with no subscription at all rather than the one it had.
            validateOrganizationsAreDescendants(owningOrgId, selectedOrgIds);
        }

        // Every tenant that currently receives this channel is about to stop, start, or stay --
        // all three change the cached publish-path answer, so the set is captured before the
        // replacing and combined with the set that results from it.
        Set<Integer> affectedTenantIds =
                new HashSet<>(webhookChannelDAO.getSubscribedOrgTenantIds(channelUuid));

        // Clear whatever is currently in force before applying the new policy, so that every
        // transition is expressed as a single replace rather than a set of special cases.
        clearStandingInstruction(channelUuid);
        webhookChannelDAO.deleteChannelOrgSubscriptions(channelUuid);

        if (policy == SubscriptionPolicy.NONE) {
            clearActiveWebhooksCache(affectedTenantIds);
            return;
        }

        List<String> orgIdsToSubscribe = policy == SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS
                ? resolveDescendantOrgIds(owningOrgId)
                : selectedOrgIds;

        if (policy == SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS) {
            // Only ALL leaves a standing instruction: it is the one policy whose meaning extends to
            // organizations that do not exist yet.
            writeStandingInstruction(channelUuid, owningOrgId);
        }

        List<ChannelOrgSubscription> subscriptions = buildSubscriptions(channelUuid, orgIdsToSubscribe);
        webhookChannelDAO.addChannelOrgSubscriptions(subscriptions);

        for (ChannelOrgSubscription subscription : subscriptions) {
            affectedTenantIds.add(subscription.getSubscribedOrgTenantId());
        }
        clearActiveWebhooksCache(affectedTenantIds);
    }

    /**
     * Drop the cached publish-path answer for organizations whose channel subscriptions changed.
     * <p>
     * The active webhooks cache is keyed by the tenant an event is raised in, so a subscription
     * change made against a channel owned by an ancestor invalidates entries belonging to the
     * descendants. Nothing below this layer knows which tenants those are.
     */
    private void clearActiveWebhooksCache(Set<Integer> tenantIds) {

        for (Integer tenantId : tenantIds) {
            ActiveWebhooksCache.getInstance().clear(tenantId);
        }
    }

    @Override
    public List<ChannelOrgSubscription> getSubscribedOrganizations(String channelUuid, int offset, int limit)
            throws WebhookMgtException {

        List<ChannelOrgSubscription> subscriptions = webhookChannelDAO
                .getChannelOrgSubscriptions(channelUuid);

        int from = Math.min(Math.max(offset, 0), subscriptions.size());
        int to = limit < 0 ? subscriptions.size() : Math.min(from + limit, subscriptions.size());

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
                .topicUuid(subscription.getTopicUuid())
                .topic(subscription.getTopic())
                .orgName(orgName)
                .parentOrgId(parentOrgId)
                .build();
    }

    @Override
    public int getSubscribedOrganizationCount(String channelUuid) throws WebhookMgtException {

        return webhookChannelDAO.getChannelOrgSubscriptionCount(channelUuid);
    }

    @Override
    public void subscribeNewOrganization(String newOrgId) throws WebhookMgtException {

        List<String> ancestorOrgIds = resolveAncestorOrgIds(newOrgId);
        if (ancestorOrgIds.isEmpty()) {
            return;
        }

        List<ChannelOrgSubscription> subscriptions = new ArrayList<>();
        for (String ancestorOrgId : ancestorOrgIds) {
            for (ResourceSharingPolicy standingInstruction : getStandingInstructionsOf(ancestorOrgId)) {
                subscriptions.addAll(buildSubscriptions(standingInstruction.getResourceId(),
                        java.util.Collections.singletonList(newOrgId)));
            }
        }
        webhookChannelDAO.addChannelOrgSubscriptions(subscriptions);

        Set<Integer> affectedTenantIds = new HashSet<>();
        for (ChannelOrgSubscription subscription : subscriptions) {
            affectedTenantIds.add(subscription.getSubscribedOrgTenantId());
        }
        clearActiveWebhooksCache(affectedTenantIds);
    }

    @Override
    public void unsubscribeDeletedOrganization(String deletedOrgId) throws WebhookMgtException {

        // As a subscriber: drop its rows from every channel that was fanning out to it.
        webhookChannelDAO.deleteChannelOrgSubscriptionsByOrgId(deletedOrgId);
        // As an owner: drop the standing instructions of the channels it owned.
        clearStandingInstructionsOwnedBy(deletedOrgId);
    }

    /**
     * Remove every webhook channel standing instruction held by an organization.
     * <p>
     * The policies live in the shared database and the channels they name live in the identity
     * database, so there is no foreign key between them and nothing removes these rows when the
     * owning organization's tenant goes away.
     *
     * @param orgId Organization whose standing instructions are to be removed.
     * @throws WebhookMgtException If the policies cannot be read or removed.
     */
    private void clearStandingInstructionsOwnedBy(String orgId) throws WebhookMgtException {

        for (ResourceSharingPolicy standingInstruction : getStandingInstructionsOf(orgId)) {
            clearStandingInstruction(standingInstruction.getResourceId());
        }
    }

    // ---------------------------------------------------------------------------------------
    // Standing instructions, held in UM_RESOURCE_SHARING_POLICY.
    // ---------------------------------------------------------------------------------------

    private Optional<ResourceSharingPolicy> getStandingInstruction(String channelUuid)
            throws ResourceSharingPolicyMgtException {

        String owningOrgId = resolveOwningOrgIdQuietly(channelUuid);
        if (owningOrgId == null) {
            return Optional.empty();
        }
        return getSharingService().getResourceSharingPolicyByResourceKeys(
                ResourceType.WEBHOOK_CHANNEL.name(), channelUuid, owningOrgId, owningOrgId);
    }

    private List<ResourceSharingPolicy> getStandingInstructionsOf(String orgId) throws WebhookMgtException {

        try {
            return getSharingService().getResourceSharingPoliciesByResourceType(
                    java.util.Collections.singletonList(orgId), ResourceType.WEBHOOK_CHANNEL.name());
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR, e, orgId);
        }
    }

    private void writeStandingInstruction(String channelUuid, String owningOrgId) throws WebhookMgtException {

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

    private void clearStandingInstruction(String channelUuid) throws WebhookMgtException {

        try {
            getSharingService().deleteResourceSharingPolicyByResourceTypeAndId(
                    ResourceType.WEBHOOK_CHANNEL, channelUuid);
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SET_ERROR, e, channelUuid);
        }
    }

    @Override
    public void clearSubscriptionPolicies(List<String> channelUuids) {

        if (channelUuids == null || channelUuids.isEmpty()) {
            return;
        }
        for (String channelUuid : channelUuids) {
            try {
                clearStandingInstruction(channelUuid);
            } catch (WebhookMgtException e) {
                LOG.warn("Failed to delete the organization subscription policy of channel: " + channelUuid +
                        ". The policy is inert and will be removed by reconciliation.", e);
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Organization hierarchy.
    // ---------------------------------------------------------------------------------------

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
            int owningTenantId = webhookChannelDAO.getOwningTenantId(channelUuid);
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

    @Override
    public List<String> getChannelUuids(String webhookId, int tenantId) throws WebhookMgtException {

        return webhookChannelDAO.getChannelUuids(webhookId, tenantId);
    }

    @Override
    public String getChannelUuid(String webhookId, String channelUri, int tenantId) throws WebhookMgtException {

        return webhookChannelDAO.getChannelUuid(webhookId, channelUri, tenantId);
    }

    @Override
    public String getChannelUri(String channelUuid) throws WebhookMgtException {

        return webhookChannelDAO.getChannelUri(channelUuid);
    }

    @Override
    public List<Integer> getSubscribedOrgTenantIds(String channelUuid) throws WebhookMgtException {

        return webhookChannelDAO.getSubscribedOrgTenantIds(channelUuid);
    }
}
