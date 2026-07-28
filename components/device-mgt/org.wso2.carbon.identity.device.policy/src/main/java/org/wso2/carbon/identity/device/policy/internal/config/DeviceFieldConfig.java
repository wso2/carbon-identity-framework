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

package org.wso2.carbon.identity.device.policy.internal.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * Represents a single device field entry from device-fields.json.
 * Captures the field name, the HTTP header it maps to, and the platforms it applies to.
 * Fields with no applicablePlatforms are universal (shown for all platforms).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceFieldConfig {

    private String name;
    private List<String> applicablePlatforms;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public List<String> getApplicablePlatforms() {

        return applicablePlatforms == null ? Collections.emptyList() : applicablePlatforms;
    }

    public void setApplicablePlatforms(List<String> applicablePlatforms) {

        this.applicablePlatforms = applicablePlatforms;
    }

    /**
     * Returns true if this field applies to all platforms (no restriction defined).
     */
    public boolean isUniversal() {

        return applicablePlatforms == null || applicablePlatforms.isEmpty();
    }

    /**
     * Returns true if this field applies to the given platform.
     */
    public boolean isApplicableTo(String platform) {

        return isUniversal() || applicablePlatforms.contains(platform);
    }
}
