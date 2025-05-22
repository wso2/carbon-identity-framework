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

import java.util.List;

/**
 * This class represents the event channel metadata in an event profile.
 */
public class EventChannel {

    private String name;
    private String description;
    private String uri;
    private List<EventType> events;

    /**
     * Get the name of the event channel.
     *
     * @return Name of the event channel
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the event channel.
     *
     * @param name Name of the event channel
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of the event channel.
     *
     * @return Description of the event channel
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the event channel.
     *
     * @param description Description of the event channel
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the URI of the event channel.
     *
     * @return URI of the event channel
     */
    public String getUri() {
        return uri;
    }

    /**
     * Set the URI of the event channel.
     *
     * @param uri URI of the event channel
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Get the events in the event channel.
     *
     * @return List of event types in this channel
     */
    public List<EventType> getEvents() {
        return events;
    }

    /**
     * Set the events in the event channel.
     *
     * @param events List of event types in this channel
     */
    public void setEvents(List<EventType> events) {
        this.events = events;
    }
}
