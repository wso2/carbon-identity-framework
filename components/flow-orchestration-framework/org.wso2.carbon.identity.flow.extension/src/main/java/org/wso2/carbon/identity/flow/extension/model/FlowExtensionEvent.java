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
import org.wso2.carbon.identity.action.execution.api.model.Application;
import org.wso2.carbon.identity.action.execution.api.model.Event;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;

/**
 * This class models the In-Flow Extension Event.
 * It represents the event sent to the In-Flow Extension action over the Action Execution Request.
 */
public class FlowExtensionEvent extends Event {

    private final FlowExtensionFlow flow;
    private final String callbackUrl;

    private FlowExtensionEvent(Builder builder) {

        this.tenant = builder.tenant;
        this.organization = builder.organization;
        this.userStore = builder.userStore;
        this.application = builder.application;
        this.flow = builder.flow;
        this.callbackUrl = builder.callbackUrl;
    }

    /**
     * Get the flow context (type, ID, and user).
     *
     * @return The flow context object, or {@code null} if not set.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public FlowExtensionFlow getFlow() {

        return flow;
    }

    /**
     * Get the callback URL for the flow, if exposed.
     * NON_NULL overrides the ObjectMapper-level NON_EMPTY so that an exposed callbackUrl with no
     * context value is serialized as {@code ""} rather than omitted.
     *
     * @return The callback URL, or {@code null} if not exposed.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCallbackUrl() {

        return callbackUrl;
    }

    /**
     * Builder for the InFlowExtensionEvent.
     */
    public static class Builder {

        private FlowExtensionFlow flow;
        private Tenant tenant;
        private Organization organization;
        private UserStore userStore;
        private Application application;
        private String callbackUrl;

        public Builder flow(FlowExtensionFlow flow) {

            this.flow = flow;
            return this;
        }

        public Builder tenant(Tenant tenant) {

            this.tenant = tenant;
            return this;
        }

        public Builder organization(Organization organization) {

            this.organization = organization;
            return this;
        }

        public Builder userStore(UserStore userStore) {

            this.userStore = userStore;
            return this;
        }

        public Builder application(Application application) {

            this.application = application;
            return this;
        }

        public Builder callbackUrl(String callbackUrl) {

            this.callbackUrl = callbackUrl;
            return this;
        }

        public FlowExtensionEvent build() {

            return new FlowExtensionEvent(this);
        }
    }
}
