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
 * This class able to holds ReCaptcha response token
 */
public class ReCaptchaResponseToken {

    private String token = null;

    /**
     * Return object of this class after set the token.
     *
     * @param token
     * @return
     */
    public ReCaptchaResponseToken token(String token) {

        this.token = token;
        return this;
    }

    @JsonProperty("token")
    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReCaptchaResponseToken reCaptchaResponseToken = (ReCaptchaResponseToken) o;
        return Objects.equals(this.token, reCaptchaResponseToken.token);
    }

    @Override
    public int hashCode() {

        return Objects.hash(token);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ReCaptchaResponseToken {\n");
        sb.append("    token: ").append(toIndentedString(token)).append("\n");
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
