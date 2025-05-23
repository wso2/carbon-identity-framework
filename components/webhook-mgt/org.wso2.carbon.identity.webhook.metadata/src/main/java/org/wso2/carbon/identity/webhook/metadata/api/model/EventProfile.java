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

import java.util.Map;

/**
 * This class represents the webhook event profile metadata.
 */
public class EventProfile {

    private Map<String, EventChannel> channels;

    /**
     * Get the channels of the event profile.
     *
     * @return Map of channel ID to channel metadata
     */
    public Map<String, EventChannel> getChannels() {
        return channels;
    }

    /**
     * Set the channels of the event profile.
     *
     * @param channels Map of channel ID to channel metadata
     */
    public void setChannels(Map<String, EventChannel> channels) {
        this.channels = channels;
    }
}
