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

    private String eventName;
    private String eventDescription;
    private String eventUri;

    /**
     * Default constructor.
     */
    public Event() {

    }

    /**
     * Constructor with all parameters.
     *
     * @param eventName        Name of the event
     * @param eventDescription Description of the event
     * @param eventUri         URI of the event
     */
    public Event(String eventName, String eventDescription, String eventUri) {

        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventUri = eventUri;
    }

    /**
     * Get the name of the event.
     *
     * @return Name of the event
     */
    public String getEventName() {

        return eventName;
    }

    /**
     * Get the description of the event.
     *
     * @return Description of the event
     */
    public String getEventDescription() {

        return eventDescription;
    }

    /**
     * Get the URI of the event.
     *
     * @return URI of the event
     */
    public String getEventUri() {

        return eventUri;
    }
}
