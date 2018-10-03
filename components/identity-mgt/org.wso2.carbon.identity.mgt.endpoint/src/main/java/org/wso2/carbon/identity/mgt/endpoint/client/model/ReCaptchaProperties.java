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
 * This class able to hold ReCaptcha properties.
 */
public class ReCaptchaProperties {

    private Boolean reCaptchaEnabled = null;
    private String reCaptchaKey = null;
    private String reCaptchaAPI = null;

    /**
     * Return object of this class after set the reCaptchaEnabled.
     *
     * @param reCaptchaEnabled
     * @return
     */
    public ReCaptchaProperties reCaptchaEnabled(boolean reCaptchaEnabled) {

        this.reCaptchaEnabled = reCaptchaEnabled;
        return this;
    }

    @JsonProperty("reCaptchaEnabled")
    public boolean getReCaptchaEnabled() {

        return reCaptchaEnabled;
    }

    public void setReCaptchaEnabled(boolean reCaptchaEnabled) {

        this.reCaptchaEnabled = reCaptchaEnabled;
    }

    /**
     * Return object of this class after set the reCaptchaKey.
     *
     * @param reCaptchaKey
     * @return
     */
    public ReCaptchaProperties reCaptchaKey(String reCaptchaKey) {

        this.reCaptchaKey = reCaptchaKey;
        return this;
    }

    @JsonProperty("reCaptchaKey")
    public String getReCaptchaKey() {

        return reCaptchaKey;
    }

    public void setReCaptchaKey(String reCaptchaKey) {

        this.reCaptchaKey = reCaptchaKey;
    }

    /**
     * Return object of this class after set the reCaptchaAPI.
     *
     * @param reCaptchaAPI
     * @return
     */
    public ReCaptchaProperties reCaptchaAPI(String reCaptchaAPI) {

        this.reCaptchaAPI = reCaptchaAPI;
        return this;
    }

    @JsonProperty("reCaptchaAPI")
    public String getReCaptchaAPI() {

        return reCaptchaAPI;
    }

    public void setReCaptchaAPI(String reCaptchaAPI) {

        this.reCaptchaAPI = reCaptchaAPI;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReCaptchaProperties reCaptchaProperties = (ReCaptchaProperties) o;
        return Objects.equals(this.reCaptchaEnabled, reCaptchaProperties.reCaptchaEnabled) &&
                Objects.equals(this.reCaptchaKey, reCaptchaProperties.reCaptchaKey) &&
                Objects.equals(this.reCaptchaAPI, reCaptchaProperties.reCaptchaAPI);
    }

    @Override
    public int hashCode() {

        return Objects.hash(reCaptchaEnabled, reCaptchaKey, reCaptchaKey);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ReCaptchaProperties {\n");
        sb.append("    reCaptchaEnabled: ").append(toIndentedString(reCaptchaEnabled)).append("\n");
        sb.append("    reCaptchaKey: ").append(toIndentedString(reCaptchaKey)).append("\n");
        sb.append("    reCaptchaAPI: ").append(toIndentedString(reCaptchaKey)).append("\n");
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
