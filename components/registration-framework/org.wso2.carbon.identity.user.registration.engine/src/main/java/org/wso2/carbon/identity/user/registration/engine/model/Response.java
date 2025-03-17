/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model class to represent the response of a node in the registration sequence.
 */
public class Response {

    private String status;
    private String type;
    private String error;
    private final List<String> requiredData;
    private Map<String, String> additionalInfo;

    private Response(Builder builder) {

        this.status = builder.status;
        this.type = builder.type;
        this.error = builder.error;
        this.requiredData = builder.requiredData;
        this.additionalInfo = builder.additionalInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRequiredData() {

        return requiredData;
    }

    public Map<String, String> getAdditionalInfo() {

        return additionalInfo;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getError() {

        return error;
    }

    public void setError(String error) {

        this.error = error;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {

        this.additionalInfo = additionalInfo;
    }

    /**
     * Builder class to build {@link Response} objects.
     */
    public static class Builder {

        private String status;
        private String type;
        private String error;
        private List<String> requiredData;
        private Map<String, String> additionalInfo;

        public Builder status(String status){

            this.status = status;
            return this;
        }

        public Builder type(String type){

            this.type = type;
            return this;
        }

        public Builder error(String error){

            this.error = error;
            return this;
        }

        public Builder requiredData(List<String> requiredData){

            this.requiredData = requiredData;
            return this;
        }

        public Builder additionalInfo(Map<String, String> additionalInfo){

            this.additionalInfo = additionalInfo;
            return this;
        }

        public Response build(){

            return new Response(this);
        }
    }
}
