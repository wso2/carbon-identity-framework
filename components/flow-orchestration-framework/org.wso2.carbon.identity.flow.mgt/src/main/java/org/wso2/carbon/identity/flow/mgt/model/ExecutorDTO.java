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
import java.util.HashMap;
import java.util.Map;

/**
 * DTO class for Executor.
 */
public class ExecutorDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private Map<String, String> metadata;

    public ExecutorDTO() {

    }

    private ExecutorDTO(Builder builder) {

        this.name = builder.name;
        this.metadata = builder.metadata;
    }

    public ExecutorDTO(String name) {

        this.name = name;
    }

    public ExecutorDTO(String name, Map<String, String> metadata) {

        this.name = name;
        this.metadata = metadata;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Map<String, String> getMetadata() {

        return metadata;
    }

    public void addMetadata(String key, String value) {

        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public void setMetadata(Map<String, String> metadata) {

        this.metadata = metadata;
    }

    public void setIdpName(String idpName) {

        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put("idpName", idpName);
    }

    /**
     * Builder class to build {@link ExecutorDTO}.
     */
    public static class Builder {

        private String name;
        private Map<String, String> metadata;

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {

            this.metadata = metadata;
            return this;
        }

        public ExecutorDTO build() {

            return new ExecutorDTO(this);
        }
    }
}
