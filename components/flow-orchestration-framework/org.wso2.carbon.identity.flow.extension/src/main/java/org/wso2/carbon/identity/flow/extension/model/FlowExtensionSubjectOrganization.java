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

import java.util.Collections;
import java.util.List;

/**
 * The organization the flow onboards, such as during a business registration, as sent to the Flow Extension action.
 * Only the exposed fields are set.
 */
public class FlowExtensionSubjectOrganization {

    private final String id;
    private final String name;
    private final String handle;
    private final List<FlowExtensionOrganizationAttribute> attributes;

    private FlowExtensionSubjectOrganization(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.handle = builder.handle;
        this.attributes = builder.attributes != null
                ? Collections.unmodifiableList(builder.attributes) : Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getId() {

        return id;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getName() {

        return name;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getHandle() {

        return handle;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<FlowExtensionOrganizationAttribute> getAttributes() {

        return attributes;
    }

    /**
     * Builder for the FlowExtensionSubjectOrganization.
     */
    public static class Builder {

        private String id;
        private String name;
        private String handle;
        private List<FlowExtensionOrganizationAttribute> attributes;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder handle(String handle) {

            this.handle = handle;
            return this;
        }

        public Builder attributes(List<FlowExtensionOrganizationAttribute> attributes) {

            this.attributes = attributes;
            return this;
        }

        public FlowExtensionSubjectOrganization build() {

            return new FlowExtensionSubjectOrganization(this);
        }
    }
}
