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

package org.wso2.carbon.identity.webhook.metadata.util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataClientException;
import org.wso2.carbon.identity.webhook.metadata.api.model.OrganizationPolicy;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataValidator;

public class WebhookMetadataValidatorTest {

    private WebhookMetadataValidator validator;

    @BeforeClass
    public void setUpClass() {

        validator = new WebhookMetadataValidator();
    }

    @Test
    public void testValidateOrganizationPolicy_ImmediateExistingAndFutureOrgs() throws Exception {

        OrganizationPolicy policy = new OrganizationPolicy(PolicyEnum.IMMEDIATE_EXISTING_AND_FUTURE_ORGS);
        validator.validateOrganizationPolicy(policy);
        // No exception expected
    }

    @Test
    public void testValidateOrganizationPolicy_NoSharing() throws Exception {

        OrganizationPolicy policy = new OrganizationPolicy(PolicyEnum.NO_SHARING);
        validator.validateOrganizationPolicy(policy);
        // No exception expected
    }

    @Test(expectedExceptions = WebhookMetadataClientException.class)
    public void testValidateOrganizationPolicy_InvalidPolicy() throws Exception {

        OrganizationPolicy policy = new OrganizationPolicy(PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS);
        validator.validateOrganizationPolicy(policy);
    }
}
