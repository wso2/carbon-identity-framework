/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.mgt.endpoint.util.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeIntrospectResponse {

    private User user = null;

    private String recoveryScenario = null;

    private String recoveryStep = null;

    private boolean isExpired = false;

    /**
     *
     **/
    @JsonProperty("user")
    public User getUser() {

        return user;
    }

    public void setUser(User user) {

        this.user = user;
    }

    /**
     *
     **/
    @JsonProperty("recoveryScenario")
    public String getRecoveryScenario() {

        return recoveryScenario;
    }

    public void setRecoveryScenario(String recoveryScenario) {

        this.recoveryScenario = recoveryScenario;
    }

    /**
     *
     **/
    @JsonProperty("recoveryStep")
    public String getRecoveryStep() {

        return recoveryStep;
    }

    public void setRecoveryStep(String recoveryStep) {

        this.recoveryStep = recoveryStep;
    }

    /**
     *
     **/
    @JsonProperty("isExpired")
    public boolean isExpired() {

        return isExpired;
    }

    public void setIsExpired(boolean isExpired) {

        this.isExpired = isExpired;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class CodeValidateInfoResponseDTO {\n");

        sb.append("  user: ").append(user).append("\n");
        sb.append("  recoveryScenario: ").append(recoveryScenario).append("\n");
        sb.append("  recoveryStep: ").append(recoveryStep).append("\n");
        sb.append("  isExpired: ").append(isExpired).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

}
