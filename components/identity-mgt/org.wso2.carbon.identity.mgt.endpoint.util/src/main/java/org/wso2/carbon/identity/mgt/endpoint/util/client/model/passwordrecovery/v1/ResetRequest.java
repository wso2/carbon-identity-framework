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
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.UserClaim;

import java.util.ArrayList;
import java.util.List;

/**
 * Object to reset the password of a user
 **/
public class ResetRequest {

    private String resetCode = null;
    private String password = null;
    private List<Property> properties = null;

    /**
     * resetCode given by the confim API
     **/
    @JsonProperty("resetCode")
    public String getResetCode() {
        return resetCode;
    }
    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    /**
     * New password given by the user
     **/
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    /**
    * (OPTIONAL) Additional META properties
    **/
    public ResetRequest properties(List<Property> properties) {

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

    public ResetRequest addPropertiesItem(Property propertiesItem) {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(propertiesItem);
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ResetRequestDTO {\n");

        sb.append("    resetCode: ").append(toIndentedString(resetCode)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
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
