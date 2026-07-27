/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.api.model;

import java.util.Map;

/**
 * This class models the IncompleteStatus.
 */
public class IncompleteStatus extends ActionExecutionStatus<Incomplete> {

    private final Incomplete incomplete;

    private IncompleteStatus(Builder builder) {

        this.status = Status.INCOMPLETE;
        this.incomplete = builder.incomplete;
        this.responseContext = builder.responseContext;
    }

    @Override
    public Incomplete getResponse() {

        return incomplete;
    }

    /**
     * This class is the builder for IncompleteStatus.
     */
    public static class Builder {

        private Incomplete incomplete;
        private Map<String, Object> responseContext;

        public Builder incomplete(Incomplete incomplete) {

            this.incomplete = incomplete;
            return this;
        }

        public Builder responseContext(Map<String, Object> responseContext) {

            this.responseContext = responseContext;
            return this;
        }

        public IncompleteStatus build() {

            return new IncompleteStatus(this);
        }
    }
}
