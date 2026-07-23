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

package org.wso2.carbon.identity.policy.evaluation.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.ResourceEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit test class for PolicyEvaluationResult.
 * Covers the AND semantics applied across per-resource outcomes. These are asserted here rather than
 * through the service, because a policy cannot hold two resources of the same type on one target, so the
 * service can never see more than one matching resource for a target.
 */
public class PolicyEvaluationResultTest {

    private ResourceEvaluationResult result(String target, String ruleId, boolean satisfied)
            throws PolicyManagementClientException {

        PolicyResource resource = new RulePolicyResource.Builder()
                .target(target)
                .resourceId(ruleId)
                .build();
        return satisfied
                ? RuleResourceEvaluationResult.satisfied(resource)
                : RuleResourceEvaluationResult.unsatisfied(resource, Collections.emptyList());
    }

    @Test
    public void testAllSatisfiedIsSatisfied() throws PolicyManagementClientException {

        PolicyEvaluationResult evaluationResult = new PolicyEvaluationResult(Arrays.asList(
                result("ios", "rule-1", true),
                result("android", "rule-2", true)));

        Assert.assertTrue(evaluationResult.isSatisfied());
        Assert.assertEquals(evaluationResult.getResults().size(), 2);
    }

    @Test
    public void testOneUnsatisfiedFailsAll() throws PolicyManagementClientException {

        PolicyEvaluationResult evaluationResult = new PolicyEvaluationResult(Arrays.asList(
                result("ios", "rule-1", true),
                result("android", "rule-2", false)));

        Assert.assertFalse(evaluationResult.isSatisfied());
        Assert.assertEquals(evaluationResult.getResults().size(), 2);
    }

    @Test
    public void testEmptyResultsAreSatisfied() {

        PolicyEvaluationResult evaluationResult = new PolicyEvaluationResult(Collections.emptyList());

        Assert.assertTrue(evaluationResult.isSatisfied());
        Assert.assertTrue(evaluationResult.getResults().isEmpty());
    }

    @Test
    public void testNullResultsAreSatisfied() {

        PolicyEvaluationResult evaluationResult = new PolicyEvaluationResult(null);

        Assert.assertTrue(evaluationResult.isSatisfied());
        Assert.assertTrue(evaluationResult.getResults().isEmpty());
    }
}
