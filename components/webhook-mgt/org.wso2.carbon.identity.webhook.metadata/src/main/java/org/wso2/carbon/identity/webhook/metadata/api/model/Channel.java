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
 * Represents a channel in a webhook event profile.
 */
public class Channel {

    private String name;
    private String description;
    private String uri;
    private List<Event> events;

    /**
     * Default constructor.
     */
    public Channel() {

    }

    /**
     * Constructor with all parameters.
     *
     * @param name        Name of the channel
     * @param description Description of the channel
     * @param uri         URI of the channel
     * @param events      List of events in the channel
     */
    public Channel(String name, String description, String uri, List<Event> events) {

        this.name = name;
        this.description = description;
        this.uri = uri;
        this.events = events;
    }

    /**
     * Get the name of the channel.
     *
     * @return Name of the channel
     */
    public String getName() {

        return name;
    }

    /**
     * Get the description of the channel.
     *
     * @return Description of the channel
     */
    public String getDescription() {

        return description;
    }

    /**
     * Get the URI of the channel.
     *
     * @return URI of the channel
     */
    public String getUri() {

        return uri;
    }

    /**
     * Get the list of events in the channel.
     *
     * @return List of events
     */
    public List<Event> getEvents() {

        return events;
    }
}
