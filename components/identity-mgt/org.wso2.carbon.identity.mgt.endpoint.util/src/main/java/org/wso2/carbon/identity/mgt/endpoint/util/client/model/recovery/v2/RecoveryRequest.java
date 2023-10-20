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
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Request to recover username/password through given channel.
 **/
public class RecoveryRequest {

    private String recoveryCode;
    private String channelId;
    private List<Property> properties = null;

    /**
     * Recovery code for the user.
     **/
    public RecoveryRequest recoveryCode(String recoveryCode) {

        this.recoveryCode = recoveryCode;
        return this;
    }

    @JsonProperty("recoveryCode")
    public String getRecoveryCode() {

        return recoveryCode;
    }

    public void setRecoveryCode(String recoveryCode) {

        this.recoveryCode = recoveryCode;
    }

    /**
     * Id of the notification channel that user prefers to get recovery notifications.
     **/
    public RecoveryRequest channelId(String channelId) {

        this.channelId = channelId;
        return this;
    }

    @JsonProperty("channelId")
    public String getChannelId() {

        return channelId;
    }

    public void setChannelId(String channelId) {

        this.channelId = channelId;
    }

    /**
     * (OPTIONAL) Additional META properties.
     **/
    public RecoveryRequest properties(List<Property> properties) {

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

    public RecoveryRequest addPropertiesItem(Property propertiesItem) {

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
        RecoveryRequest recoveryRequest = (RecoveryRequest) o;
        return Objects.equals(this.recoveryCode, recoveryRequest.recoveryCode) &&
                Objects.equals(this.channelId, recoveryRequest.channelId) &&
                Objects.equals(this.properties, recoveryRequest.properties);
    }

    @Override
    public int hashCode() {

        return Objects.hash(recoveryCode, channelId, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class RecoveryRequest {\n");

        sb.append("    recoveryCode: ").append(toIndentedString(recoveryCode)).append("\n");
        sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
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
