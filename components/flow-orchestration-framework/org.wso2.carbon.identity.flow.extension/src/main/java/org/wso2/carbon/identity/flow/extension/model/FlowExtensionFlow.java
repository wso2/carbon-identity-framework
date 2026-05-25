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

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.wso2.carbon.identity.action.execution.api.model.User;

/**
 * Models the {@code flow} object nested inside the In-Flow Extension Event.
 * Groups flow-scoped context: the flow type, flow identifier, and the acting user.
 */
public class FlowExtensionFlow {

    private final String flowType;
    private final String flowId;
    private final User user;

    private FlowExtensionFlow(Builder builder) {

        this.flowType = builder.flowType;
        this.flowId = builder.flowId;
        this.user = builder.user;
    }

    /**
     * NON_NULL overrides the ObjectMapper-level NON_EMPTY so that an exposed flowType with no
     * context value is serialized as {@code ""} rather than omitted.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getFlowType() {

        return flowType;
    }

    public String getFlowId() {

        return flowId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public User getUser() {

        return user;
    }

    public static class Builder {

        private String flowType;
        private String flowId;
        private User user;

        public Builder flowType(String flowType) {

            this.flowType = flowType;
            return this;
        }

        public Builder flowId(String flowId) {

            this.flowId = flowId;
            return this;
        }

        public Builder user(User user) {

            this.user = user;
            return this;
        }

        public FlowExtensionFlow build() {

            return new FlowExtensionFlow(this);
        }
    }
}
