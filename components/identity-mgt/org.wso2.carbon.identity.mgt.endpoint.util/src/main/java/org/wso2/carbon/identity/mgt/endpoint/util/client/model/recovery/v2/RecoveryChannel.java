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

package org.wso2.carbon.identity.mgt.endpoint.util.client.model.recovery.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Object with notification channel attributes.
 **/
public class RecoveryChannel {
  
    private String id;
    private String type;
    private String value;
    private Boolean preferred;

    /**
    * Id given to the channel.
    **/
    public RecoveryChannel id(String id) {

        this.id = id;
        return this;
    }
    
    @JsonProperty("id")
    public String getId() {

        return id;
    }
    public void setId(String id) {

        this.id = id;
    }

    /**
    * Type of the chanel.
    **/
    public RecoveryChannel type(String type) {

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

    /**
    * Masked channel value.
    **/
    public RecoveryChannel value(String value) {

        this.value = value;
        return this;
    }
    
    @JsonProperty("value")
    public String getValue() {

        return value;
    }
    public void setValue(String value) {

        this.value = value;
    }

    /**
    * Whether the channel is a user preferred channel.
    **/
    public RecoveryChannel preferred(Boolean preferred) {

        this.preferred = preferred;
        return this;
    }
    
    @JsonProperty("preferred")
    public Boolean getPreferred() {

        return preferred;
    }
    public void setPreferred(Boolean preferred) {

        this.preferred = preferred;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecoveryChannel recoveryChannel = (RecoveryChannel) o;
        return Objects.equals(this.id, recoveryChannel.id) &&
            Objects.equals(this.type, recoveryChannel.type) &&
            Objects.equals(this.value, recoveryChannel.value) &&
            Objects.equals(this.preferred, recoveryChannel.preferred);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type, value, preferred);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RecoveryChannel {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    preferred: ").append(toIndentedString(preferred)).append("\n");
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
