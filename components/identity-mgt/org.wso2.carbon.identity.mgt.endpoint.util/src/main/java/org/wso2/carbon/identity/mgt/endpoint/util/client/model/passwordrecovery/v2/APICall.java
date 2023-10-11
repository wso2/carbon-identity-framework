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

import java.util.Objects;

/**
 * Object that holds next API call details.
 **/
public class APICall {
  
    private String rel;
    private String href;
    private String type;

    /**
    * Next API call.
    **/
    public APICall rel(String rel) {

        this.rel = rel;
        return this;
    }
    
    @JsonProperty("rel")
    public String getRel() {

        return rel;
    }
    public void setRel(String rel) {

        this.rel = rel;
    }

    /**
    * Next API url.
    **/
    public APICall href(String href) {

        this.href = href;
        return this;
    }
    
    @JsonProperty("href")
    public String getHref() {

        return href;
    }
    public void setHref(String href) {

        this.href = href;
    }

    /**
    * HTTP method type.
    **/
    public APICall type(String type) {

        this.type = type;
        return this;
    }
    
    @JsonProperty("type")
    public String getType() {

        return type;
    }
    public void setType(String type) {

        this.type = type;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APICall apICall = (APICall) o;
        return Objects.equals(this.rel, apICall.rel) &&
            Objects.equals(this.href, apICall.href) &&
            Objects.equals(this.type, apICall.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(rel, href, type);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class APICall {\n");

        sb.append("    rel: ").append(toIndentedString(rel)).append("\n");
        sb.append("    href: ").append(toIndentedString(href)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
