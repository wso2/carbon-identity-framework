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
 * Class for ClaimProfileProperty.
 */
public class ClaimProfileProperty {

    private Boolean required;
    private Boolean readOnly;
    private Boolean supportedByDefault;

    @JsonProperty("required")
    public Boolean getRequired() {

        return required;
    }

    public void setRequired(Boolean required) {

        this.required = required;
    }

    @JsonProperty("readOnly")
    public Boolean getReadOnly() {

        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {

        this.readOnly = readOnly;
    }

    @JsonProperty("supportedByDefault")
    public Boolean getSupportedByDefault() {

        return supportedByDefault;
    }

    public void setSupportedByDefault(Boolean supportedByDefault) {

        this.supportedByDefault = supportedByDefault;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClaimProfileProperty claimProfileProperty = (ClaimProfileProperty) o;
        return Objects.equals(required, claimProfileProperty.required) &&
                Objects.equals(readOnly, claimProfileProperty.readOnly) &&
                Objects.equals(supportedByDefault, claimProfileProperty.supportedByDefault);
    }

    @Override
    public int hashCode() {

        return Objects.hash(required, readOnly, supportedByDefault);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ClaimProfileProperty {\n");
        sb.append("    required: ").append(toIndentedString(required)).append("\n");
        sb.append("    readOnly: ").append(toIndentedString(readOnly)).append("\n");
        sb.append("    supportedByDefault: ").append(toIndentedString(supportedByDefault)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
