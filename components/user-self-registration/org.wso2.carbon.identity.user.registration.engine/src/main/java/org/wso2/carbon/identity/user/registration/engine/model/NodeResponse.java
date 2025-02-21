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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class to represent the response of a node in the registration sequence.
 */
public class NodeResponse {

    private String status;
    private final List<String> requiredData =  new ArrayList<>();
    private Map<String, String> additionalInfo = new HashMap<>();
    private String userAssertion;
    private Message message;
    private String pageContent;

    public NodeResponse(String status) {

        this.status = status;
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

    public void addRequiredData(List<String> requiredData) {

        this.requiredData.addAll(requiredData);
    }

    public String getUserAssertion() {

        return userAssertion;
    }

    public void setUserAssertion(String userAssertion) {

        this.userAssertion = userAssertion;
    }

    public Message getMessage() {

        return message;
    }

    public void setMessage(Message message) {

        this.message = message;
    }

    public Map<String, String> getAdditionalInfo() {

        return additionalInfo;
    }

    public void addAdditionalInfo(Map<String, String> additionalInfo) {

        this.additionalInfo = additionalInfo;
    }

    public String getPage() {

        return pageContent;
    }

    public void setPageDTO(String page) {

        this.pageContent = page;
    }
}
