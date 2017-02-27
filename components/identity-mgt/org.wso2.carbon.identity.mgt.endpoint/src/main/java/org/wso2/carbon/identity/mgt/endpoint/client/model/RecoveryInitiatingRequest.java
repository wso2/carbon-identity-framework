/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * RecoveryInitiatingRequest
 */
public class RecoveryInitiatingRequest {

    private User user = null;
    private List<Property> properties = new ArrayList<Property>();


    /**
     **/
    public RecoveryInitiatingRequest user(User user) {
        this.user = user;
        return this;
    }


    @JsonProperty("user")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    /**
     **/
    public RecoveryInitiatingRequest properties(List<Property> properties) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecoveryInitiatingRequest recoveryInitiatingRequest = (RecoveryInitiatingRequest) o;
        return Objects.equals(this.user, recoveryInitiatingRequest.user) &&
                Objects.equals(this.properties, recoveryInitiatingRequest.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, properties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RecoveryInitiatingRequest {\n");

        sb.append("    user: ").append(toIndentedString(user)).append("\n");
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
        return o.toString().replace("\n", "\n    ");
    }
}

