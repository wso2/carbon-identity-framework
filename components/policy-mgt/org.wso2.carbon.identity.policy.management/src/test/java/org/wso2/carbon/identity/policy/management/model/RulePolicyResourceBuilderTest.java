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

package org.wso2.carbon.identity.policy.management.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;

/**
 * Unit test class for RulePolicyResource.Builder validation.
 */
public class RulePolicyResourceBuilderTest {

    @Test
    public void testBuildRejectsNullTarget() {

        try {
            new RulePolicyResource.Builder().resourceId("rule-1").build();
            Assert.fail("Expected PolicyManagementClientException for a null target.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
            Assert.assertTrue(e.getDescription().contains("Target"));
        }
    }

    @Test
    public void testBuildRejectsBlankTarget() {

        try {
            new RulePolicyResource.Builder().target("   ").resourceId("rule-1").build();
            Assert.fail("Expected PolicyManagementClientException for a blank target.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
            Assert.assertTrue(e.getDescription().contains("Target"));
        }
    }

    @Test
    public void testBuildAcceptsValidTarget() throws PolicyManagementClientException {

        RulePolicyResource resource = new RulePolicyResource.Builder()
                .id("row-1")
                .target("android")
                .resourceId("rule-1")
                .build();
        Assert.assertEquals(resource.getTarget(), "android");
        Assert.assertEquals(resource.getId(), "row-1");
        Assert.assertEquals(resource.getResourceId(), "rule-1");
        Assert.assertEquals(resource.getResourceType(), ResourceType.RULE);
        Assert.assertNull(resource.getRule());
    }
}
