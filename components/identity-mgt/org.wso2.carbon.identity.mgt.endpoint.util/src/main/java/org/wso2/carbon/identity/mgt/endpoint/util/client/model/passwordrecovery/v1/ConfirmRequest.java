/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request to confirm password recovery
 **/
public class ConfirmRequest {
  
    private String confirmationCode;
    private List<Property> properties = null;

    /**
     * Confirmation code given to the user.
     **/
    public ConfirmRequest confirmCode(String confirmationCode) {

        this.confirmationCode = confirmationCode;
        return this;
    }
    
    @JsonProperty("confirmationCode")
    public String getConfirmationCode() {
        return confirmationCode;
    }
    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    /**
    * (OPTIONAL) Additional META properties
    **/
    public ConfirmRequest properties(List<Property> properties) {

        this.properties = properties;
        return this;
    }
    
    @JsonProperty("properties")
    public List<Property> getProperties() {
        return properties;
    }
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public ConfirmRequest addPropertiesItem(Property propertiesItem) {
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
        ConfirmRequest confirmationRequest = (ConfirmRequest) o;
        return Objects.equals(this.confirmationCode, confirmationRequest.confirmationCode) &&
            Objects.equals(this.properties, confirmationRequest.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confirmationCode, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ConfirmRequest {\n");

        sb.append("    confirmationCode: ").append(toIndentedString(confirmationCode)).append("\n");
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
        return o.toString().replace("\n", "\n");
    }
}
