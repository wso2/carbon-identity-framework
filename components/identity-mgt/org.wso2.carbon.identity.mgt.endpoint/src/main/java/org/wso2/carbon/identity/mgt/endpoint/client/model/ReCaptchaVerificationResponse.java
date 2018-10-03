/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.mgt.endpoint.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * This class holds the status of the ReCaptcha verification.
 */
public class ReCaptchaVerificationResponse {

    private Boolean success = null;

    /**
     * Return object of this class after set the success state.
     *
     * @param success
     * @return
     */
    public ReCaptchaVerificationResponse success(boolean success) {

        this.success = success;
        return this;
    }

    @JsonProperty("success")
    public boolean getSuccess() {

        return success;
    }

    public void setSuccess(boolean success) {

        this.success = success;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReCaptchaVerificationResponse reCaptchaVerificationResponse = (ReCaptchaVerificationResponse) o;
        return Objects.equals(this.success, reCaptchaVerificationResponse.success);
    }

    @Override
    public int hashCode() {

        return Objects.hash(success);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ReCaptchaVerificationResponse {\n");
        sb.append("    success: ").append(toIndentedString(success)).append("\n");
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
