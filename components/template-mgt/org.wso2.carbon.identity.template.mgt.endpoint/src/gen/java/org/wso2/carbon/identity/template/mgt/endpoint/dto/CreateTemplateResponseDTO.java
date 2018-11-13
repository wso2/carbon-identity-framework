/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class CreateTemplateResponseDTO {

    private String token = null;

    private String createdBy = null;

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("token")
    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("createdBy")
    public String getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(String createdBy) {

        this.createdBy = createdBy;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class CreateTemplateResponseDTO {\n");

        sb.append("  token: ").append(token).append("\n");
        sb.append("  createdBy: ").append(createdBy).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
