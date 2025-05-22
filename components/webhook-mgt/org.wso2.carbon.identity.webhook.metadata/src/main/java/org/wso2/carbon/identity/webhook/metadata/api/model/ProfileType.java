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
 * Enum representing the event profile type.
 */
public enum ProfileType {
    
    /**
     * WSO2 Events profile.
     */
    WSO2_EVENT("wso2-event"),

    /**
     * CAEP Events profile.
     */
    CAEP_EVENT("caep-event"),

    /**
     * RISC Events profile.
     */
    RISC_EVENT("risc-event");

    private final String profileName;

    ProfileType(String profileName) {
        this.profileName = profileName;
    }

    /**
     * Get the profile name.
     *
     * @return Profile name
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Get the file name for the profile.
     *
     * @return File name
     */
    public String getFileName() {
        return profileName + "-profile.json";
    }
}
