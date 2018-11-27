package org.wso2.carbon.identity.template.mgt.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class AddTemplateResponseDTO {

    private String tenantId = null;

    private String name = null;

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("tenantId")
    public String getTenantId() {

        return tenantId;
    }

    public void setTenantId(String tenantId) {

        this.tenantId = tenantId;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("name")
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class AddTemplateResponseDTO {\n");

        sb.append("  tenantId: ").append(tenantId).append("\n");
        sb.append("  name: ").append(name).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
