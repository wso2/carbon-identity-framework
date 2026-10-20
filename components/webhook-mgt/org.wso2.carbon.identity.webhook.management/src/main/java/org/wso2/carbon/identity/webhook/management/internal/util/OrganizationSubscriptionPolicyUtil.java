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

package org.wso2.carbon.identity.webhook.management.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.ResourceType;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.exception
        .ResourceSharingPolicyMgtException;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.model.ResourceSharingPolicy;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Organization subscription policies of webhook channels, held in the shared database. Its DAO uses plain JDBC
 * calls, so these commit on their own connection even when called inside an identity transaction.
 */
public class OrganizationSubscriptionPolicyUtil {

    private static final Log LOG = LogFactory.getLog(OrganizationSubscriptionPolicyUtil.class);

    private OrganizationSubscriptionPolicyUtil() {

    }

    /**
     * Whether the channel holds the policy the given organization would write for it.
     *
     * @param channelUuid Channel UUID.
     * @param owningOrgId Organization owning the channel's webhook.
     * @return true if the policy is held.
     * @throws WebhookMgtException If the policy cannot be read.
     */
    public static boolean isPolicyHeld(String channelUuid, String owningOrgId) throws WebhookMgtException {

        try {
            return getSharingService().getResourceSharingPolicyByResourceKeys(
                    ResourceType.WEBHOOK_CHANNEL.name(), channelUuid, owningOrgId, owningOrgId).isPresent();
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR, e, channelUuid);
        }
    }

    /**
     * Every webhook channel policy an organization holds.
     *
     * @param orgId Organization holding the policies.
     * @return Those policies, empty when it holds none.
     * @throws WebhookMgtException If the policies cannot be read.
     */
    public static List<ResourceSharingPolicy> getPoliciesHeldBy(String orgId) throws WebhookMgtException {

        try {
            return getSharingService().getResourceSharingPoliciesByResourceType(
                    Collections.singletonList(orgId), ResourceType.WEBHOOK_CHANNEL.name());
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR, e, orgId);
        }
    }

    /**
     * Write the policy that subscribes organizations created later.
     *
     * @param channelUuid Channel UUID.
     * @param owningOrgId Organization owning the channel's webhook, which holds the policy.
     * @throws WebhookMgtException If the policy cannot be written.
     */
    public static void writePolicy(String channelUuid, String owningOrgId) throws WebhookMgtException {

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

    /**
     * Remove the policy of a channel.
     *
     * @param channelUuid Channel UUID.
     * @throws WebhookMgtException If the policy cannot be removed.
     */
    public static void clearPolicy(String channelUuid) throws WebhookMgtException {

        try {
            getSharingService().deleteResourceSharingPolicyByResourceTypeAndId(
                    ResourceType.WEBHOOK_CHANNEL, channelUuid);
        } catch (ResourceSharingPolicyMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SET_ERROR, e, channelUuid);
        }
    }

    /**
     * Remove the policies of a set of channels, logging rather than failing on any that cannot be removed.
     *
     * @param channelUuids Channels whose policies are to be removed.
     */
    public static void clearPoliciesQuietly(List<String> channelUuids) {

        if (channelUuids == null || channelUuids.isEmpty()) {
            return;
        }
        for (String channelUuid : channelUuids) {
            try {
                clearPolicy(channelUuid);
            } catch (WebhookMgtException e) {
                LOG.warn("Failed to delete the organization subscription policy of channel: " + channelUuid +
                        ". The policy is inert and will be removed by reconciliation.", e);
            }
        }
    }

    /**
     * Write back policies a failed transaction had cleared, logging rather than failing on any that cannot be.
     *
     * @param owningOrgIdsByChannelUuid Channels whose policy was cleared, against the organization that held it.
     */
    public static void restorePoliciesQuietly(Map<String, String> owningOrgIdsByChannelUuid) {

        if (owningOrgIdsByChannelUuid == null || owningOrgIdsByChannelUuid.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : owningOrgIdsByChannelUuid.entrySet()) {
            try {
                writePolicy(entry.getKey(), entry.getValue());
            } catch (WebhookMgtException e) {
                LOG.warn("Failed to restore the organization subscription policy of channel: " + entry.getKey() +
                        " after a failed transaction. The channel subscribes the organizations it already " +
                        "held, but no longer subscribes newly created ones until the policy is set again.", e);
            }
        }
    }

    private static ResourceSharingPolicyHandlerService getSharingService() {

        return WebhookManagementComponentServiceHolder.getInstance().getResourceSharingPolicyHandlerService();
    }
}
