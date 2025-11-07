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
package org.wso2.carbon.identity.fraud.detection.core.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for Event Configuration.
 */
public class EventConfigDTO implements Serializable {

    private static final long serialVersionUID = 2190530787554682955L;
    private boolean enabled;
    private Map<String, String> properties;
    private static final Gson gson = new Gson();

    /**
     * Private default constructor.
     */
    private EventConfigDTO() {

    }

    /**
     * Constructor to create EventConfigDTO with enabled status.
     *
     * @param enabled Whether the event is enabled.
     */
    public EventConfigDTO(boolean enabled) {

        this.enabled = enabled;
        this.properties = new HashMap<>();
    }

    /**
     * Returns the enabled status of the event.
     *
     * @return Whether the event is enabled.
     */
    public boolean isEnabled() {

        return enabled;
    }

    /**
     * Sets the enabled status of the event.
     *
     * @param enabled Whether the event is enabled.
     */
    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    /**
     * Returns the properties of the event.
     *
     * @return Map of properties.
     */
    public Map<String, String> getProperties() {

        return properties;
    }

    /**
     * Sets the properties of the event.
     *
     * @param properties Map of properties.
     */
    public void setProperties(Map<String, String> properties) {

        this.properties = properties;
    }

    /**
     * Adds a property to the event.
     *
     * @param key   Property key.
     * @param value Property value.
     */
    public void addProperty(String key, String value) {

        this.properties.put(key, value);
    }

    /**
     * Converts the EventConfigDTO to a JSON string.
     *
     * @return JSON representation of the EventConfigDTO.
     */
    @Override
    public String toString() {

        return gson.toJson(this);
    }

    /**
     * Creates an EventConfigDTO from a JSON string.
     *
     * @param jsonString JSON representation of the EventConfigDTO.
     * @return EventConfigDTO object or null if parsing fails.
     */
    public static EventConfigDTO fromJson(String jsonString) {

        try {
            return gson.fromJson(jsonString, EventConfigDTO.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
