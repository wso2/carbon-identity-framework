/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2;

import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request to resend the recovery code.
 **/
public class ResendRequest {

    private String resendCode;
    private List<Property> properties = null;

    /**
     * Resend code for the user.
     **/
    public ResendRequest resendCode(String resendCode) {

        this.resendCode = resendCode;
        return this;
    }

    /**
     * Get resendCode
     *
     * @return resendCode
     **/
    @JsonProperty("resendCode")
    public String getResendCode() {

        return resendCode;
    }

    /**
     * Set resendCode
     *
     * @param resendCode
     **/
    public void setResendCode(String resendCode) {

        this.resendCode = resendCode;
    }

    /**
     * (OPTIONAL) Additional META properties.
     **/
    public ResendRequest properties(List<Property> properties) {

        this.properties = properties;
        return this;
    }

    /**
     * Get meta properties
     *
     * @return List<Property>
     **/
    @JsonProperty("properties")
    public List<Property> getProperties() {

        return properties;
    }

    /**
     * Set meta properties
     *
     * @param properties
     **/
    public void setProperties(List<Property> properties) {

        this.properties = properties;
    }

    /**
     * Add meta properties
     *
     * @param propertiesItem
     * @return ResendRequest
     **/
    public ResendRequest addPropertiesItem(Property propertiesItem) {

        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(propertiesItem);
        return this;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResendRequest resendRequest = (ResendRequest) o;
        return Objects.equals(this.resendCode, resendRequest.resendCode) &&
                Objects.equals(this.properties, resendRequest.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(resendCode, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ResendRequest {\n");

        sb.append("    resendCode: ").append(toIndentedString(resendCode)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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
