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

package org.wso2.carbon.identity.authorization.framework.model;

import java.util.Map;

/**
 * The {@code AccessEvaluationResponse} class is a model class for the Access Evaluation response returned in an
 * Access Evaluation request from an Authorization Engine. This follows the AuthZEN Evaluation response format.
 */
public class AccessEvaluationResponse {

    private final boolean decision;
    private Map<String, Object> context;

    /**
     * Constructs an {@code AccessEvaluationResponse} object with the decision.
     *
     * @param decision The decision of the access evaluation.
     */
    public AccessEvaluationResponse(boolean decision) {

        this.decision = decision;
    }

    /**
     * Sets the context of the access evaluation response.
     *
     * @param context The context of the access evaluation response.
     */
    public void setContext(Map<String, Object> context) {

        this.context = context;
    }

    /**
     * Returns the decision of the access evaluation.
     *
     * @return The decision of the access evaluation.
     */
    public boolean getDecision() {

        return decision;
    }

    /**
     * Returns the context of the access evaluation response.
     *
     * @return The context of the access evaluation response.
     */
    public Map<String, Object> getContext() {

        return context;
    }
}
