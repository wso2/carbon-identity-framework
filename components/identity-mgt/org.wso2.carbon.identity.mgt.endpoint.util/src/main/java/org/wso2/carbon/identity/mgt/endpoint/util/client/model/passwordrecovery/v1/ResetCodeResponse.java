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

import java.util.ArrayList;
import java.util.List;

public class ResetCodeResponse {

    private String resetCode;
    private List<APICall> links = null;

    /**
     * Success status code
     **/
    public ResetCodeResponse resetCode(String resetCode) {

        this.resetCode = resetCode;
        return this;
    }

    /**
     * Password reset code to reset the password
     **/
    @JsonProperty("resetCode")
    public String getResetCode() {
        return resetCode;
    }
    public void setResetCode(String resetCode) {
        this.resetCode = resetCode;
    }

    /**
     * Contains available api calls
     **/
    @JsonProperty("links")
    public List<APICall> getLinks() {
        return links;
    }
    public void setLinks(List<APICall> links) {
        this.links = links;
    }

    /**
     * Contains available api calls
     **/
    public ResetCodeResponse links(List<APICall> links) {

        this.links = links;
        return this;
    }

    public ResetCodeResponse addLinksItem(APICall linksItem) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(linksItem);
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ResetCodeResponse {\n");

        sb.append("    resetCode: ").append(toIndentedString(resetCode)).append("\n");
        sb.append("    links: ").append(toIndentedString(links)).append("\n");
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
