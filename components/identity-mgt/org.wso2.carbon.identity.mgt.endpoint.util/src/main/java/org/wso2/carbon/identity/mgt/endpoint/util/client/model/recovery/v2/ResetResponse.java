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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Response to password reset request.
 **/
public class ResetResponse {


    private String code;
    private String message;


    /**
     * Success status code.
     **/
    public ResetResponse code(String code) {

        this.code = code;
        return this;
    }


    @JsonProperty("code")
    public String getCode() {

        return code;
    }

    /**
     * Set code
     **/
    public void setCode(String code) {

        this.code = code;
    }

    /**
     * Success status message.
     * @param message message
     * @return ResendResponse
     */
    public ResetResponse message(String message) {

        this.message = message;
        return this;
    }

    /**
     * Get message
     * @return message
     */
    @JsonProperty("message")
    public String getMessage() {

        return message;
    }

    /**
     * Set message
     * @param message message
     */
    public void setMessage(String message) {

        this.message = message;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResetResponse passwordRecoveryInternalNotifyResponse = (ResetResponse) o;
        return Objects.equals(this.code, passwordRecoveryInternalNotifyResponse.code) &&
                Objects.equals(this.message, passwordRecoveryInternalNotifyResponse.message);
    }

    @Override
    public int hashCode() {

        return Objects.hash(code, message);
    }

    @Override
    public String toString() {

        return "class ResendResponse {\n" +
                "    code: " + toIndentedString(code) + "\n" +
                "    message: " + toIndentedString(message) + "\n" +
                "}";
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
