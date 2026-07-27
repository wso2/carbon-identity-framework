/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.compatibility.settings.core.model.metadata;

import java.time.Instant;

/**
 * Model class for Compatibility Setting Meta Data Entry.
 */
public class CompatibilitySettingMetaDataEntry {

    private Instant timestampReference;
    private String targetValue;
    private String defaultValue;

    /**
     * Get the timestamp reference for evaluation.
     *
     * @return Timestamp reference in ISO 8601 format.
     */
    public Instant getTimestampReference() {

        return timestampReference;
    }

    /**
     * Set the timestamp reference for evaluation.
     *
     * @param timestampReference Timestamp reference in ISO 8601 format.
     */
    public void setTimestampReference(Instant timestampReference) {

        this.timestampReference = timestampReference;
    }

    /**
     * Get the target value for tenants created before timestamp.
     *
     * @return Target value.
     */
    public String getTargetValue() {

        return targetValue;
    }

    /**
     * Set the target value for tenants created before timestamp.
     *
     * @param targetValue Target value.
     */
    public void setTargetValue(String targetValue) {

        this.targetValue = targetValue;
    }

    /**
     * Get the default value for tenants created after timestamp.
     *
     * @return Default value.
     */
    public String getDefaultValue() {

        return defaultValue;
    }

    /**
     * Set the default value for tenants created after timestamp.
     *
     * @param defaultValue Default value.
     */
    public void setDefaultValue(String defaultValue) {

        this.defaultValue = defaultValue;
    }

    /**
     * Override the current metadata entry with another entry's non-null values.
     *
     * @param metaDataEntry The metaDataEntry to override from.
     */
    public void override(CompatibilitySettingMetaDataEntry metaDataEntry) {

        if (metaDataEntry.getTimestampReference() != null) {
            this.timestampReference = metaDataEntry.getTimestampReference();
        }
        if (metaDataEntry.getTargetValue() != null) {
            this.targetValue = metaDataEntry.getTargetValue();
        }
        if (metaDataEntry.getDefaultValue() != null) {
            this.defaultValue = metaDataEntry.getDefaultValue();
        }
    }
}

