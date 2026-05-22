/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a resolved debug context containing all necessary information
 * for executing a debug flow.
 * This model provides type-safe access to context properties.
 */
public class DebugContext {

    private static final String RESOURCE_TYPE_KEY = "resourceType";

    private String resourceType;
    private final Map<String, Object> properties = new HashMap<>();

    public DebugContext() {

    }

    /**
     * Creates a DebugContext from a Map.
     *
     * @param contextMap Map containing context properties.
     * @return DebugContext instance.
     */
    public static DebugContext buildFromMap(Map<String, Object> contextMap) {

        DebugContext context = new DebugContext();

        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            if (RESOURCE_TYPE_KEY.equals(entry.getKey())) {
                if (entry.getValue() != null) {
                    context.setResourceType(String.valueOf(entry.getValue()));
                }
            } else {
                context.setProperty(entry.getKey(), entry.getValue());
            }
        }
        return context;
    }

    public String getResourceType() {

        return resourceType;
    }

    public void setResourceType(String resourceType) {

        this.resourceType = resourceType;
    }

    public Object getProperty(String key) {

        return this.properties.get(key);
    }

    public void setProperty(String key, Object value) {

        if (key == null) {
            return;
        }
        this.properties.put(key, value);
    }

    /**
     * Gets all properties as a map. Returns a defensive copy.
     *
     * @return Map of all properties.
     */
    public Map<String, Object> getProperties() {

        return new HashMap<>(this.properties);
    }
}
