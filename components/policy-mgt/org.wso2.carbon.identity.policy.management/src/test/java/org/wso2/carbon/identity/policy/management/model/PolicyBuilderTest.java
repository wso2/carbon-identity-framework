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
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit test class for Policy.Builder validation.
 */
public class PolicyBuilderTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String POLICY_NAME = "TestPolicy";

    /**
     * Stand-in resource whose type and target can be set freely, used to reach the builder checks that
     * RulePolicyResource.Builder would otherwise reject first.
     */
    private static class TestPolicyResource extends PolicyResource {

        private final ResourceType resourceType;

        TestPolicyResource(String target, ResourceType resourceType) {

            super(null, target, "resource-1");
            this.resourceType = resourceType;
        }

        @Override
        public ResourceType getResourceType() {

            return resourceType;
        }
    }

    @Test
    public void testBuildRejectsNullResource() {

        try {
            new Policy.Builder()
                    .name(POLICY_NAME)
                    .tenantDomain(TENANT_DOMAIN)
                    .resources(Collections.singletonList(null))
                    .build();
            Assert.fail("Expected PolicyManagementClientException for a null resource.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
            Assert.assertTrue(e.getDescription().contains("Resource"));
        }
    }

    @Test
    public void testBuildRejectsNullResourceType() {

        try {
            new Policy.Builder()
                    .name(POLICY_NAME)
                    .tenantDomain(TENANT_DOMAIN)
                    .resources(Collections.singletonList(new TestPolicyResource("android", null)))
                    .build();
            Assert.fail("Expected PolicyManagementClientException for a null resource type.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
            Assert.assertTrue(e.getDescription().contains("Resource type"));
        }
    }

    @Test
    public void testBuildRejectsBlankTarget() {

        try {
            new Policy.Builder()
                    .name(POLICY_NAME)
                    .tenantDomain(TENANT_DOMAIN)
                    .resources(Collections.singletonList(new TestPolicyResource("  ", ResourceType.RULE)))
                    .build();
            Assert.fail("Expected PolicyManagementClientException for a blank target.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
            Assert.assertTrue(e.getDescription().contains("Target"));
        }
    }

    @Test
    public void testBuildRejectsDuplicateTargetIgnoringCase() throws PolicyManagementClientException {

        RulePolicyResource first = new RulePolicyResource.Builder().target("ios").resourceId("rule-1").build();
        RulePolicyResource second = new RulePolicyResource.Builder().target("IOS").resourceId("rule-2").build();
        try {
            new Policy.Builder()
                    .name(POLICY_NAME)
                    .tenantDomain(TENANT_DOMAIN)
                    .resources(Arrays.asList(first, second))
                    .build();
            Assert.fail("Expected PolicyManagementClientException for a duplicate target.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_DUPLICATE_TARGET_IN_POLICY.getCode());
        }
    }

    @Test
    public void testBuildAcceptsDistinctTargets() throws PolicyManagementClientException {

        Policy policy = new Policy.Builder()
                .name(POLICY_NAME)
                .tenantDomain(TENANT_DOMAIN)
                .resources(Arrays.asList(
                        new RulePolicyResource.Builder().target("ios").resourceId("rule-1").build(),
                        new RulePolicyResource.Builder().target("android").resourceId("rule-2").build()))
                .build();
        Assert.assertEquals(policy.getResources().size(), 2);
        Assert.assertEquals(policy.getName(), POLICY_NAME);
    }

    @Test
    public void testBuildAcceptsNullResources() throws PolicyManagementClientException {

        Policy policy = new Policy.Builder()
                .name(POLICY_NAME)
                .tenantDomain(TENANT_DOMAIN)
                .build();
        Assert.assertTrue(policy.getResources().isEmpty());
    }
}
