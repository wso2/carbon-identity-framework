/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a webhook adapter with its properties.
 */
public class Adapter {

    private final String name;
    private final AdapterType type;
    private final boolean enabled;
    private final Map<String, String> properties;

    private Adapter(Builder builder) {

        this.name = builder.name;
        this.type = builder.type;
        this.enabled = builder.enabled;
        this.properties = builder.properties != null ?
                Collections.unmodifiableMap(new HashMap<>(builder.properties)) : Collections.emptyMap();
    }

    public String getName() {

        return name;
    }

    public AdapterType getType() {

        return type;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public Map<String, String> getProperties() {

        return properties;
    }

    /**
     * Builder for creating instances of {@link Adapter}.
     */
    public static class Builder {

        private String name;
        private AdapterType type;
        private boolean enabled;
        private Map<String, String> properties = new HashMap<>();

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder type(AdapterType type) {

            this.type = type;
            return this;
        }

        public Builder enabled(boolean enabled) {

            this.enabled = enabled;
            return this;
        }

        public Builder properties(Map<String, String> properties) {

            this.properties = properties != null ? new HashMap<>(properties) : new HashMap<>();
            return this;
        }

        public Builder addProperty(String key, String value) {

            this.properties.put(key, value);
            return this;
        }

        public Adapter build() {

            return new Adapter(this);
        }
    }
}
