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

package org.wso2.carbon.identity.webhook.metadata.internal.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.model.OrganizationPolicy;
import org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_INVALID_WEBHOOK_METADATA_REQUEST_FIELD;

/**
 * Webhook metadata validator.
 */
public class WebhookMetadataValidator {

    /**
     * Validate the organization policy name.
     *
     * @param organizationPolicy Organization policy enum wrapper.
     * @throws WebhookMetadataClientException if the organization policy is invalid.
     */
    public void validateOrganizationPolicy(OrganizationPolicy organizationPolicy)
            throws WebhookMetadataClientException {

        if (StringUtils.isNotBlank(String.valueOf(organizationPolicy)) &&
                !organizationPolicy.getPolicyEnum().equals(PolicyEnum.IMMEDIATE_EXISTING_AND_FUTURE_ORGS) &&
                !organizationPolicy.getPolicyEnum().equals(PolicyEnum.NO_SHARING)) {
            throw WebhookMetadataExceptionHandler.handleClientException(
                    ERROR_INVALID_WEBHOOK_METADATA_REQUEST_FIELD,
                    WebhookMetadataConstants.MetadataPropertyFields.ORGANIZATION_POLICY_FIELD);
        }
    }
}
