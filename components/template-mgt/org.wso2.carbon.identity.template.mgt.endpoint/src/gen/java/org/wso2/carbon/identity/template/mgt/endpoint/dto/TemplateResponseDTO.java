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
public class TemplateResponseDTO {

    private Integer tenantId = null;

    private String templateName = null;

    private String description = null;

    private String templateScript = null;

    /**
     * The Id of the tenant which the template resides in.
     **/
    @ApiModelProperty(value = "The Id of the tenant which the template resides in.")
    @JsonProperty("tenantId")
    public Integer getTenantId() {

        return tenantId;
    }

    public void setTenantId(Integer tenantId) {

        this.tenantId = tenantId;
    }

    /**
     * The name of the template given by the admin.
     **/
    @ApiModelProperty(value = "The name of the template given by the admin.")
    @JsonProperty("templateName")
    public String getTemplateName() {

        return templateName;
    }

    public void setTemplateName(String templateName) {

        this.templateName = templateName;
    }

    /**
     * A description for the template given by the admin.
     **/
    @ApiModelProperty(value = "A description for the template given by the admin.")
    @JsonProperty("description")
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * This indicates the script of the template.
     **/
    @ApiModelProperty(value = "This indicates the script of the template.")
    @JsonProperty("templateScript")
    public String getTemplateScript() {

        return templateScript;
    }

    public void setTemplateScript(String templateScript) {

        this.templateScript = templateScript;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class TemplateResponseDTO {\n");

        sb.append("  tenantId: ").append(tenantId).append("\n");
        sb.append("  templateName: ").append(templateName).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  templateScript: ").append(templateScript).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
