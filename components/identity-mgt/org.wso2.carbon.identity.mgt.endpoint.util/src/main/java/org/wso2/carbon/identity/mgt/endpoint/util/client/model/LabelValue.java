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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a canonical value for a claim with label and value.
 */
public class LabelValue {

    private String label = null;
    private String value = null;

    /**
     * Gets the label of the canonical value.
     *
     * @return The label.
     */
    @JsonProperty("label")
    public String getLabel() {

        return label;
    }

    /**
     * Sets the label of the canonical value.
     *
     * @param label The label.
     */
    public void setLabel(String label) {

        this.label = label;
    }

    /**
     * Gets the value of the canonical value.
     *
     * @return The value.
     */
    @JsonProperty("value")
    public String getValue() {

        return value;
    }

    /**
     * Sets the value of the canonical value.
     *
     * @param value The value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LabelValue that = (LabelValue) o;
        return Objects.equals(this.label, that.label) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(label, value);
    }

    @Override
    public String toString() {

        return "CanonicalValue{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
