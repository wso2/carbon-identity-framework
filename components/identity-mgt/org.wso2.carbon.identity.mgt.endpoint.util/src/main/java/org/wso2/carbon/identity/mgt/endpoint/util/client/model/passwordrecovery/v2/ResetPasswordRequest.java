/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model.passwordrecovery.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request to reset the password of a user.
 **/
public class ResetPasswordRequest {

    private String resetCode;
    private String flowConfirmationCode;
    private String password;
    private List<Property> properties = null;

    /**
     * ResetCode given by the confirm API.
     **/
    public ResetPasswordRequest resetCode(String resetCode) {

        this.resetCode = resetCode;
        return this;
    }

    @JsonProperty("resetCodee")
    public String getResetCode() {

        return resetCode;
    }

    public void setResetCode(String resetCode) {

        this.resetCode = resetCode;
    }

    /**
     * Confirmation code of the recovery flow.
     **/
    public ResetPasswordRequest flowConfirmationCode(String flowConfirmationCode) {

        this.flowConfirmationCode = flowConfirmationCode;
        return this;
    }

    @JsonProperty("flowConfirmationCode")
    public String getFlowConfirmationCode() {

        return flowConfirmationCode;
    }

    public void setFlowConfirmationCode(String flowConfirmationCode) {

        this.flowConfirmationCode = flowConfirmationCode;
    }

    /**
     * New password given by the user.
     **/
    public ResetPasswordRequest password(String password) {

        this.password = password;
        return this;
    }

    @JsonProperty("password")
    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    /**
     * (OPTIONAL) Additional META properties.
     **/
    public ResetPasswordRequest properties(List<Property> properties) {

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

    public ResetPasswordRequest addPropertiesItem(Property propertiesItem) {

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
        ResetPasswordRequest resetPasswordRequest = (ResetPasswordRequest) o;
        return Objects.equals(this.resetCode, resetPasswordRequest.resetCode) &&
                Objects.equals(this.flowConfirmationCode, resetPasswordRequest.flowConfirmationCode) &&
                Objects.equals(this.password, resetPasswordRequest.password) &&
                Objects.equals(this.properties, resetPasswordRequest.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(resetCode, flowConfirmationCode, password, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ResetPasswordRequest {\n");

        sb.append("    resetCode: ").append(toIndentedString(resetCode)).append("\n");
        sb.append("    flowConfirmationCode: ").append(toIndentedString(flowConfirmationCode)).append("\n");
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
