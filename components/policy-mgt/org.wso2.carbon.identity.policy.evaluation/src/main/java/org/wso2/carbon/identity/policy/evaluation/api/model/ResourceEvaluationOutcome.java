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

package org.wso2.carbon.identity.policy.evaluation.api.model;

import org.wso2.carbon.identity.policy.management.api.model.ResourceType;

import java.util.Collections;
import java.util.List;

/**
 * Outcome of evaluating a single policy resource.
 */
public class ResourceEvaluationOutcome {

    private final String resourceId;
    private final ResourceType resourceType;
    private final boolean satisfied;
    private final List<String> failedFields;

    /**
     * Creates a resource evaluation outcome with no field-level detail.
     *
     * @param resourceId   ID of the underlying resource that was evaluated (e.g. a rule ID).
     * @param resourceType Type of the resource that was evaluated.
     * @param satisfied    Whether the resource's evaluation was satisfied.
     */
    public ResourceEvaluationOutcome(String resourceId, ResourceType resourceType, boolean satisfied) {

        this(resourceId, resourceType, satisfied, Collections.emptyList());
    }

    /**
     * Creates a resource evaluation outcome.
     *
     * @param resourceId   ID of the underlying resource that was evaluated (e.g. a rule ID).
     * @param resourceType Type of the resource that was evaluated.
     * @param satisfied    Whether the resource's evaluation was satisfied.
     * @param failedFields Fields that caused the evaluation to fail, or empty if satisfied or not applicable.
     */
    public ResourceEvaluationOutcome(String resourceId, ResourceType resourceType, boolean satisfied,
                                      List<String> failedFields) {

        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.satisfied = satisfied;
        this.failedFields = failedFields == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(failedFields);
    }

    public String getResourceId() {

        return resourceId;
    }

    public ResourceType getResourceType() {

        return resourceType;
    }

    public boolean isSatisfied() {

        return satisfied;
    }

    public List<String> getFailedFields() {

        return failedFields;
    }
}
