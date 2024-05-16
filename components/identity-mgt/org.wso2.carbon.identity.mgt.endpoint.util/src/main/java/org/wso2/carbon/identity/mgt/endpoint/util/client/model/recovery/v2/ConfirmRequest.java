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
 * Request to confirm recovery attempt.
 */
public class ConfirmRequest {

    private String confirmationCode;
    private String otp;
    private List<Property> properties = null;

    /**
     * Confirmation code for the request.
     **/
    public ConfirmRequest confirmationCode(String confirmationCode) {

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
     * OTP sent to the user.
     **/
    public ConfirmRequest otp(String otp) {

        this.otp = otp;
        return this;
    }

    @JsonProperty("otp")
    public String getOtp() {

        return otp;
    }

    public void setOtp(String otp) {

        this.otp = otp;
    }

    /**
     * (OPTIONAL) Additional META properties.
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

    /**
     * Adds property to the request properties.
     *
     * @param propertiesItem propertiesItem.
     * @return ConfirmRequest.
     */
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
        ConfirmRequest recoveryRequest = (ConfirmRequest) o;
        return Objects.equals(this.confirmationCode, recoveryRequest.confirmationCode) &&
                Objects.equals(this.otp, recoveryRequest.otp) &&
                Objects.equals(this.properties, recoveryRequest.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(confirmationCode, otp, properties);
    }

    @Override
    public String toString() {

        return "class ConfirmRequest {" +
                "    confirmationCode: " + confirmationCode + "\n" +
                "    otp: " + otp + "\n" +
                "    properties: " + properties + "\n" +
                "}";
    }
}
