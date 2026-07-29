/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.api.model;

import java.util.Locale;

/**
 * Supported device platforms for device policy evaluation and field applicability.
 */
public enum Platform {

    ANDROID,
    IOS,
    MACOS,
    WINDOWS;

    /**
     * Returns the {@link Platform} matching the given string value (case-insensitive).
     * Returns {@code null} if the input is null, empty, or does not match any platform.
     *
     * @param value String representation of the platform.
     * @return Matching {@link Platform} constant, or {@code null} if unknown.
     */
    public static Platform fromValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (Platform platform : Platform.values()) {
            if (platform.name().equalsIgnoreCase(value.trim())) {
                return platform;
            }
        }
        return null;
    }

    /**
     * Returns the lowercase wire representation of the platform.
     *
     * @return Lowercase platform string.
     */
    public String getValue() {

        return name().toLowerCase(Locale.ENGLISH);
    }
}
