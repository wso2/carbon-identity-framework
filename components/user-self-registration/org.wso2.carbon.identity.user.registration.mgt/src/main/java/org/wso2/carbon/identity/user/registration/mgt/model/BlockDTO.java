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

import java.util.ArrayList;
import java.util.List;

/**
 * DTO class for Block.
 */
public class BlockDTO {

    private final List<ComponentDTO> components;
    private String id;

    private BlockDTO(Builder builder) {

        this.components = builder.components;
        this.id = builder.id;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public List<ComponentDTO> getComponents() {

        return components;
    }

    public void addComponents(ComponentDTO element) {

        this.components.add(element);
    }

    public static class Builder {

        private List<ComponentDTO> components = new ArrayList<>();
        private String id;

        public Builder components(List<ComponentDTO> components) {

            this.components = components;
            return this;
        }

        public Builder component(ComponentDTO component) {

            this.components.add(component);
            return this;
        }

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public BlockDTO build() {

            return new BlockDTO(this);
        }
    }
}
