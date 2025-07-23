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
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.model.OrganizationPolicy;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_INVALID_WEBHOOK_REQUEST_FIELD;
import static org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataConstants.ORGANIZATION_POLICY_FIELD;

/**
 * Webhook metadata validator.
 */
public class WebhookMetadataValidator {

    /**
     * Validate the organization policy name.
     *
     * @param name Organization policy name.
     * @throws WebhookMetadataClientException if the organization policy is invalid.
     */
    public void validateOrganizationPolicy(OrganizationPolicy name) throws WebhookMetadataClientException {

        if (StringUtils.isNotBlank(String.valueOf(name)) &&
                !name.equals(OrganizationPolicy.ALL_EXISTING_AND_FUTURE_ORGS) &&
                !name.equals(OrganizationPolicy.THIS_ORG_ONLY)) {
            throw WebhookMetadataExceptionHandler.handleClientException(
                    ERROR_INVALID_WEBHOOK_REQUEST_FIELD, ORGANIZATION_POLICY_FIELD);
        }
    }
}
