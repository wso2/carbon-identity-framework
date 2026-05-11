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

package org.wso2.carbon.identity.flow.inflow.extensions.model;

import org.wso2.carbon.identity.action.execution.api.model.Application;
import org.wso2.carbon.identity.action.execution.api.model.Event;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Request;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class models the In-Flow Extension Event.
 * It represents the event sent to the In-Flow Extension action over the Action Execution Request.
 */
public class InFlowExtensionEvent extends Event {

    private final String flowType;
    private final String flowId;
    private final String callbackUrl;
    private final String portalUrl;
    private final Map<String, Object> flowProperties;

    private InFlowExtensionEvent(Builder builder) {

        this.request = builder.request;
        this.tenant = builder.tenant;
        this.organization = builder.organization;
        this.user = builder.user;
        this.userStore = builder.userStore;
        this.application = builder.application;
        this.flowType = builder.flowType;
        this.flowId = builder.flowId;
        this.callbackUrl = builder.callbackUrl;
        this.portalUrl = builder.portalUrl;
        this.flowProperties = builder.flowProperties != null ? 
                Collections.unmodifiableMap(new HashMap<>(builder.flowProperties)) : Collections.emptyMap();
    }

    /**
     * Get the flow type.
     *
     * @return The flow type (e.g., "REGISTRATION", "PASSWORD_RESET").
     */
    public String getFlowType() {

        return flowType;
    }

    /**
     * Get the flow identifier (context identifier of the executing flow).
     *
     * @return The flow identifier.
     */
    public String getFlowId() {

        return flowId;
    }

    /**
     * Get the callback URL for the flow, if exposed.
     *
     * @return The callback URL, or null if not exposed.
     */
    public String getCallbackUrl() {

        return callbackUrl;
    }

    /**
     * Get the portal URL for the flow, if exposed.
     *
     * @return The portal URL, or null if not exposed.
     */
    public String getPortalUrl() {

        return portalUrl;
    }

    /**
     * Get the flow properties/context data.
     *
     * @return Unmodifiable map of flow properties.
     */
    public Map<String, Object> getFlowProperties() {

        return flowProperties;
    }

    /**
     * Builder for the InFlowExtensionEvent.
     */
    public static class Builder {

        private Request request;
        private Tenant tenant;
        private Organization organization;
        private User user;
        private UserStore userStore;
        private Application application;
        private String flowType;
        private String flowId;
        private String callbackUrl;
        private String portalUrl;
        private Map<String, Object> flowProperties;

        public Builder request(Request request) {

            this.request = request;
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

        public Builder user(User user) {

            this.user = user;
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

        public Builder flowType(String flowType) {

            this.flowType = flowType;
            return this;
        }

        public Builder flowId(String flowId) {

            this.flowId = flowId;
            return this;
        }

        public Builder callbackUrl(String callbackUrl) {

            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder portalUrl(String portalUrl) {

            this.portalUrl = portalUrl;
            return this;
        }

        public Builder flowProperties(Map<String, Object> flowProperties) {

            this.flowProperties = flowProperties != null ? new HashMap<>(flowProperties) : null;
            return this;
        }

        public InFlowExtensionEvent build() {

            return new InFlowExtensionEvent(this);
        }
    }
}
