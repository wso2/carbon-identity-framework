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
 * Represents a webhook event profile metadata.
 */
public class EventProfileMetadata {

    private String name;
    private String uri;

    /**
     * Default constructor.
     */
    public EventProfileMetadata() {

    }

    /**
     * Constructor with all parameters.
     *
     * @param name Name of the event profile
     * @param uri  URI of the event profile
     */
    public EventProfileMetadata(String name, String uri) {

        this.name = name;
        this.uri = uri;
    }

    /**
     * Get the name of the event profile.
     *
     * @return Name of the event profile
     */
    public String getName() {

        return name;
    }

    /**
     * Get the URI of the event profile.
     *
     * @return URI of the event profile
     */
    public String getUri() {

        return uri;
    }
}
