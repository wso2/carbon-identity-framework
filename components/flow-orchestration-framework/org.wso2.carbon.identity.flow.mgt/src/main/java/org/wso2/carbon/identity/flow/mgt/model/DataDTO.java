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
import java.util.List;
import java.util.Map;

/**
 * DTO class for component and step data.
 */
public class DataDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private ActionDTO action;
    private String redirectURL;
    private List<ComponentDTO> components;
    private List<String> requiredParams;
    private List<String> optionalParams;
    private Map<String, String> additionalData;
    private Map<String, Object> webAuthnData;

    public DataDTO() {

    }

    private DataDTO(Builder builder) {

        this.components = builder.components;
        this.action = builder.action;
        this.redirectURL = builder.redirectURL;
        this.requiredParams = builder.requiredParams;
        this.optionalParams = builder.optionalParams;
        this.additionalData = builder.additionalData;
        this.webAuthnData = builder.webAuthnData;
    }

    public List<ComponentDTO> getComponents() {

        return components;
    }

    public void addComponent(ComponentDTO component) {

        if (this.components == null) {
            this.components = new ArrayList<>();
        }
        this.components.add(component);
    }

    public ActionDTO getAction() {

        return action;
    }

    public void setAction(ActionDTO action) {

        this.action = action;
    }

    public String getRedirectURL() {

        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {

        this.redirectURL = redirectURL;
    }

    public List<String> getRequiredParams() {

        return requiredParams;
    }

    public List<String> getOptionalParams() {

        return optionalParams;
    }

    public Map<String, String> getAdditionalData() {

        return additionalData;
    }

    public void addAdditionalData(String key, String value) {

        if (this.additionalData == null) {
            this.additionalData = new java.util.HashMap<>();
        }
        this.additionalData.put(key, value);
    }

    public void addRequiredParam(String param) {

        if (this.requiredParams == null) {
            this.requiredParams = new ArrayList<>();
        }
        this.requiredParams.add(param);
    }

    public void addOptionalParam(String param) {

        if (this.optionalParams == null) {
            this.optionalParams = new ArrayList<>();
        }
        this.optionalParams.add(param);
    }

    public Map<String, Object> getWebAuthnData() {

        return webAuthnData;
    }

    /**
     * Builder class to build {@link DataDTO} objects.
     */
    public static class Builder {

        private List<ComponentDTO> components;
        private ActionDTO action;
        private String redirectURL;
        private List<String> requiredParams;
        private List<String> optionalParams;
        private Map<String, String> additionalData;
        private Map<String, Object> webAuthnData;

        public Builder components(List<ComponentDTO> components) {

            this.components = components;
            return this;
        }

        public Builder action(ActionDTO action) {

            this.action = action;
            return this;
        }

        public Builder url(String url) {

            this.redirectURL = url;
            return this;
        }

        public Builder requiredParams(List<String> requiredParams) {

            this.requiredParams = requiredParams;
            return this;
        }

        public Builder optionalParams(List<String> optionalParams) {

            this.optionalParams = optionalParams;
            return this;
        }

        public Builder additionalData(Map<String, String> additionalData) {

            this.additionalData = additionalData;
            return this;
        }

        public Builder webAuthnData(Map<String, Object> webAuthnData) {

            this.webAuthnData = webAuthnData;
            return this;
        }

        public DataDTO build() {

            return new DataDTO(this);
        }
    }
}
