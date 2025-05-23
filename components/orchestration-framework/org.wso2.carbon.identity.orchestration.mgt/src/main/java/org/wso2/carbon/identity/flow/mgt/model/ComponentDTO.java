/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO class for component in the step.
 */
public class ComponentDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String id;
    private String category;
    private String type;
    private String identifier;
    private String variant;
    private ActionDTO action;
    private Map<String, Object> configs = new HashMap<>();
    private List<ComponentDTO> components = new ArrayList<>();

    public ComponentDTO() {

    }

    private ComponentDTO(Builder builder) {

        this.id = builder.id;
        this.category = builder.category;
        this.type = builder.type;
        this.identifier = builder.identifier;
        this.variant = builder.variant;
        this.configs = builder.configs;
        this.components = builder.components;
        this.action = builder.action;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public Map<String, Object> getConfigs() {

        return configs;
    }

    public void addConfig(String key, Object value) {

        this.configs.put(key, value);
    }

    public ActionDTO getAction() {

        return action;
    }

    public void setAction(ActionDTO action) {

        this.action = action;
    }

    public String getIdentifier() {

        return identifier;
    }

    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {

        this.category = category;
    }

    public List<ComponentDTO> getComponents() {

        return components;
    }

    public void setComponents(List<ComponentDTO> components) {

        this.components = components;
    }

    public String getVariant() {

        return variant;
    }

    public void setVariant(String variant) {

        this.variant = variant;
    }

    /**
     * Builder class to build {@link ComponentDTO} objects.
     */
    public static class Builder {

        private Map<String, Object> configs = new HashMap<>();
        private List<ComponentDTO> components = new ArrayList<>();
        private String id;
        private String category;
        private String type;
        private String identifier;
        private String variant;
        private ActionDTO action;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder category(String category) {

            this.category = category;
            return this;
        }

        public Builder type(String type) {

            this.type = type;
            return this;
        }

        public Builder identifier(String identifier) {

            this.identifier = identifier;
            return this;
        }

        public Builder variant(String variant) {

            this.variant = variant;
            return this;
        }

        public Builder configs(Map<String, Object> configs) {

            if (configs != null && !configs.isEmpty()) {
                this.configs.putAll(configs);
            }
            this.configs = configs;
            return this;
        }

        public Builder components(List<ComponentDTO> components) {

            if (components != null && !components.isEmpty()) {
                this.components.addAll(components);
            }
            this.components = components;
            return this;
        }

        public Builder action(ActionDTO action) {

            this.action = action;
            return this;
        }

        public ComponentDTO build() {

            return new ComponentDTO(this);
        }
    }
}
