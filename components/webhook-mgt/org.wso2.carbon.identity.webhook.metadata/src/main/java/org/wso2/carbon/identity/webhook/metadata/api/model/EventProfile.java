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
 * Represents a webhook event profile.
 */
public class EventProfile {

    private String profile;
    private String uri;
    private List<Channel> channels;

    /**
     * Default constructor.
     */
    public EventProfile() {

    }

    /**
     * Constructor with all parameters.
     *
     * @param profile  Name of the event profile
     * @param uri      URI of the event profile
     * @param channels List of channels in the profile
     */
    public EventProfile(String profile, String uri, List<Channel> channels) {

        this.profile = profile;
        this.uri = uri;
        this.channels = channels;
    }

    /**
     * Get the name of the event profile.
     *
     * @return Name of the event profile
     */
    public String getProfile() {

        return profile;
    }

    /**
     * Get the URI of the event profile.
     *
     * @return URI of the event profile
     */
    public String getUri() {

        return uri;
    }

    /**
     * Get the list of channels in the profile.
     *
     * @return List of channels
     */
    public List<Channel> getChannels() {

        return channels;
    }
}
