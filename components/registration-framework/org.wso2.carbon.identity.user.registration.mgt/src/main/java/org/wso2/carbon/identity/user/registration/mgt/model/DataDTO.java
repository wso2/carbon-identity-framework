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

package org.wso2.carbon.identity.user.registration.mgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO class for component and step data.
 */
public class DataDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private ActionDTO action;
    private String url;
    private List<ComponentDTO> components = new ArrayList<>();
    private List<String> requiredParams;
    private Map<String, String> additionalData;

    public DataDTO() {

    }

    private DataDTO(Builder builder) {

        this.components = builder.components;
        this.action = builder.action;
        this.url = builder.url;
        this.requiredParams = builder.requiredParams;
        this.additionalData = builder.additionalData;
    }

    public List<ComponentDTO> getComponents() {

        return components;
    }

    public void addComponent(ComponentDTO component) {

        this.components.add(component);
    }

    public ActionDTO getAction() {

        return action;
    }

    public void setAction(ActionDTO action) {

        this.action = action;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public List<String> getRequiredParams() {

        return requiredParams;
    }

    public Map<String, String> getAdditionalData() {

        return additionalData;
    }

    /**
     * Builder class to build {@link DataDTO} objects.
     */
    public static class Builder {

        private final List<ComponentDTO> components = new ArrayList<>();
        private ActionDTO action;
        private String url;
        private List<String> requiredParams;
        private Map<String, String> additionalData;

        public Builder components(List<ComponentDTO> components) {

            if (components != null && !components.isEmpty()) {
                this.components.addAll(components);
            }
            return this;
        }

        public Builder action(ActionDTO action) {

            this.action = action;
            return this;
        }

        public Builder url(String url) {

            this.url = url;
            return this;
        }

        public Builder requiredParams(List<String> requiredParams) {

            this.requiredParams = requiredParams;
            return this;
        }

        public Builder additionalData(Map<String, String> additionalData) {

            this.additionalData = additionalData;
            return this;
        }

        public DataDTO build() {

            return new DataDTO(this);
        }
    }
}
