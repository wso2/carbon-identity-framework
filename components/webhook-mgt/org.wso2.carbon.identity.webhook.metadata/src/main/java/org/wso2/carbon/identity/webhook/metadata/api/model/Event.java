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

/**
 * Represents an event in a webhook event profile.
 */
public class Event {

    private String name;
    private String description;
    private String uri;

    /**
     * Default constructor.
     */
    public Event() {
    }

    /**
     * Constructor with all parameters.
     *
     * @param name        Name of the event
     * @param description Description of the event
     * @param uri         URI of the event
     */
    public Event(String name, String description, String uri) {
        this.name = name;
        this.description = description;
        this.uri = uri;
    }

    /**
     * Get the name of the event.
     *
     * @return Name of the event
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the event.
     *
     * @param name Name of the event
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of the event.
     *
     * @return Description of the event
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of the event.
     *
     * @param description Description of the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the URI of the event.
     *
     * @return URI of the event
     */
    public String getUri() {
        return uri;
    }

    /**
     * Set the URI of the event.
     *
     * @param uri URI of the event
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
