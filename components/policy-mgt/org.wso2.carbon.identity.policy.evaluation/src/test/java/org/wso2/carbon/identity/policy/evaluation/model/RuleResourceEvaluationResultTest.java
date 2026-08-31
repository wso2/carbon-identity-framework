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
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResource;

import java.util.Collections;

/**
 * Unit test class for RuleResourceEvaluationResult and its Builder.
 */
public class RuleResourceEvaluationResultTest {

    private PolicyResource resource() throws PolicyManagementClientException {

        return new RulePolicyResource.Builder()
                .target("ios")
                .resourceId("rule-1")
                .build();
    }

    @Test
    public void testSatisfiedResult() throws PolicyManagementClientException {

        PolicyResource res = resource();
        RuleResourceEvaluationResult result = RuleResourceEvaluationResult.satisfied(res);

        Assert.assertTrue(result.isSatisfied());
        Assert.assertEquals(result.getResource(), res);
        Assert.assertTrue(result.getFailedFields().isEmpty());
    }

    @Test
    public void testUnsatisfiedResultWithFailedFields() throws PolicyManagementClientException {

        PolicyResource res = resource();
        RuleResourceEvaluationResult result = RuleResourceEvaluationResult.unsatisfied(
                res, Collections.singletonList("device.osVersion"));

        Assert.assertFalse(result.isSatisfied());
        Assert.assertEquals(result.getResource(), res);
        Assert.assertEquals(result.getFailedFields().size(), 1);
        Assert.assertEquals(result.getFailedFields().get(0), "device.osVersion");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildRejectsNullResource() {

        new RuleResourceEvaluationResult.Builder()
                .satisfied(true)
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildRejectsUnsatisfiedWithEmptyFailedFields() throws PolicyManagementClientException {

        new RuleResourceEvaluationResult.Builder()
                .resource(resource())
                .satisfied(false)
                .failedFields(Collections.emptyList())
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildRejectsSatisfiedWithNonEmptyFailedFields() throws PolicyManagementClientException {

        new RuleResourceEvaluationResult.Builder()
                .resource(resource())
                .satisfied(true)
                .failedFields(Collections.singletonList("field"))
                .build();
    }
}
