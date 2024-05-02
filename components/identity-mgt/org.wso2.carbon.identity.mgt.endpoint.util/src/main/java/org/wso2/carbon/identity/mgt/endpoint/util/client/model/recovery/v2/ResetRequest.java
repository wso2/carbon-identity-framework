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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reset request to reset user password.
 */
public class ResetRequest {

    private String resetCode;
    private String flowConfirmationCode;
    private String password;
    private List<Property> properties = null;

    /**
     * Reset code sent at confirm step.
     **/
    public ResetRequest resetCode(String resetCode) {

        this.resetCode = resetCode;
        return this;
    }

    @JsonProperty("resetCode")
    public String getResetCode() {

        return resetCode;
    }

    public void setResetCode(String resetCode) {

        this.resetCode = resetCode;
    }

    /**
     * flowConfirmationCode for the recovery flow.
     **/
    public ResetRequest flowConfirmationCode(String flowConfirmationCode) {

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
     * Reset code sent at confirm step.
     **/
    public ResetRequest password(String password) {

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

    /**
     * Adds property to the request properties.
     *
     * @param propertiesItem propertiesItem.
     * @return ConfirmRequest.
     */
    public ResetRequest addPropertiesItem(Property propertiesItem) {

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
        ResetRequest recoveryRequest = (ResetRequest) o;
        return Objects.equals(this.resetCode, recoveryRequest.resetCode) &&
                Objects.equals(this.flowConfirmationCode, recoveryRequest.flowConfirmationCode) &&
                Objects.equals(this.properties, recoveryRequest.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(resetCode, flowConfirmationCode, properties);
    }

    @Override
    public String toString() {

        return "class ConfirmRequest {" +
                "    confirmationCode: " + resetCode + "\n" +
                "    otp: " + flowConfirmationCode + "\n" +
                "    properties: " + properties + "\n" +
                "}";
    }
}
