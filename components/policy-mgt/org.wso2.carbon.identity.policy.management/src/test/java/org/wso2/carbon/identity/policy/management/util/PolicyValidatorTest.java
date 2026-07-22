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

package org.wso2.carbon.identity.policy.management.util;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;
import org.wso2.carbon.identity.policy.management.internal.util.PolicyValidator;

import java.util.Collections;

/**
 * Unit test class for PolicyValidator.
 * Resource level validation is covered by the model builder tests; this class covers only the policy
 * level rules the builders cannot enforce.
 */
public class PolicyValidatorTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String POLICY_NAME = "TestPolicy";

    private PolicyValidator policyValidator;

    @BeforeClass
    public void setUp() {

        policyValidator = new PolicyValidator();
    }

    @Test
    public void testValidateForAddRejectsNullPolicy() {

        try {
            policyValidator.validateForAdd(null);
            Assert.fail("Expected PolicyManagementClientException for a null policy.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
            Assert.assertTrue(e.getDescription().contains("Policy"));
        }
    }

    @Test
    public void testValidateForAddRejectsBlankName() throws PolicyManagementClientException {

        Policy policy = new Policy.Builder()
                .name("  ")
                .tenantDomain(TENANT_DOMAIN)
                .resources(Collections.emptyList())
                .build();
        try {
            policyValidator.validateForAdd(policy);
            Assert.fail("Expected PolicyManagementClientException for a blank policy name.");
        } catch (PolicyManagementClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
            Assert.assertTrue(e.getDescription().contains("Policy name"));
        }
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testValidateForAddRejectsNullName() throws PolicyManagementClientException {

        Policy policy = new Policy.Builder()
                .tenantDomain(TENANT_DOMAIN)
                .resources(Collections.emptyList())
                .build();
        policyValidator.validateForAdd(policy);
    }

    @Test
    public void testValidateForAddAcceptsValidPolicy() throws PolicyManagementClientException {

        Policy policy = new Policy.Builder()
                .name(POLICY_NAME)
                .tenantDomain(TENANT_DOMAIN)
                .resources(Collections.singletonList(
                        new RulePolicyResource.Builder().target("ios").resourceId("rule-1").build()))
                .build();
        policyValidator.validateForAdd(policy);
    }

    @Test(expectedExceptions = PolicyManagementClientException.class)
    public void testValidateForUpdateRejectsNullPolicy() throws PolicyManagementClientException {

        policyValidator.validateForUpdate(null);
    }

    @Test
    public void testValidateForUpdateAcceptsNullName() throws PolicyManagementClientException {

        // The name is immutable on update, so the API layer sends a policy without a name.
        Policy policy = new Policy.Builder()
                .id("policy-1")
                .tenantDomain(TENANT_DOMAIN)
                .resources(Collections.singletonList(
                        new RulePolicyResource.Builder().target("ios").resourceId("rule-1").build()))
                .build();
        policyValidator.validateForUpdate(policy);
    }
}
