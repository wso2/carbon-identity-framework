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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.wso2.carbon.identity.action.execution.api.model.Organization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Organization data sent to a Flow Extension action.
 */
public class FlowExtensionOrganization extends Organization {

    private final int depth;
    private final String description;
    private final Map<String, String> attributes;

    private FlowExtensionOrganization(Builder builder) {

        super(builder.id, builder.name, builder.orgHandle);
        this.depth = builder.depth;
        this.description = builder.description;
        this.attributes = builder.attributes.isEmpty() ? null : new LinkedHashMap<>(builder.attributes);
    }

    @Override
    public int getDepth() {

        return depth;
    }

    /**
     * Get the organization description.
     *
     * @return Organization description.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getDescription() {

        return description;
    }

    /**
     * Get the organization attributes.
     *
     * @return Organization attributes.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getAttributes() {

        return attributes == null ? null : Collections.unmodifiableMap(attributes);
    }

    /**
     * Builder for {@link FlowExtensionOrganization}.
     */
    public static class Builder {

        private String id;
        private String name;
        private String orgHandle;
        private int depth;
        private String description;
        private final Map<String, String> attributes = new LinkedHashMap<>();

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder orgHandle(String orgHandle) {

            this.orgHandle = orgHandle;
            return this;
        }

        public Builder depth(int depth) {

            this.depth = depth;
            return this;
        }

        public Builder description(String description) {

            this.description = description;
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {

            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public FlowExtensionOrganization build() {

            return new FlowExtensionOrganization(this);
        }
    }
}
