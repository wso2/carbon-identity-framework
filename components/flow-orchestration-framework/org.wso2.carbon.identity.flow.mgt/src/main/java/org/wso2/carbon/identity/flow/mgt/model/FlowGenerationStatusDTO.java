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

package org.wso2.carbon.identity.flow.mgt.model;

/**
 * DTO for Flow Generation Status.
 */
public class FlowGenerationStatusDTO {

    private final boolean optimizingQuery;
    private final boolean fetchingSamples;
    private final boolean generatingFlow;
    private final boolean completed;

    public FlowGenerationStatusDTO(boolean optimizingQuery, boolean fetchingSamples,
                                   boolean generatingFlow, boolean completed) {

        this.optimizingQuery = optimizingQuery;
        this.fetchingSamples = fetchingSamples;
        this.generatingFlow = generatingFlow;
        this.completed = completed;
    }

    public boolean isOptimizingQuery() {

        return optimizingQuery;
    }

    public boolean isFetchingSamples() {

        return fetchingSamples;
    }

    public boolean isGeneratingFlow() {

        return generatingFlow;
    }

    public boolean isCompleted() {

        return completed;
    }

    /**
     * Builder class for FlowGenerationStatusDTO.
     */
    public static class Builder {

        private boolean optimizingQuery;
        private boolean fetchingSamples;
        private boolean generatingFlow;
        private boolean completed;

        public Builder optimizingQuery(boolean optimizingQuery) {

            this.optimizingQuery = optimizingQuery;
            return this;
        }

        public Builder fetchingSamples(boolean fetchingSamples) {

            this.fetchingSamples = fetchingSamples;
            return this;
        }

        public Builder generatingFlow(boolean generatingFlow) {

            this.generatingFlow = generatingFlow;
            return this;
        }

        public Builder completed(boolean completed) {

            this.completed = completed;
            return this;
        }

        public FlowGenerationStatusDTO build() {

            return new FlowGenerationStatusDTO(optimizingQuery, fetchingSamples, generatingFlow, completed);
        }
    }
}
