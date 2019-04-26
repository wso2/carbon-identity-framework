package org.wso2.carbon.identity.template.mgt.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class GetTemplatesResponseDTO {

    private String templateName = null;

    private String description = null;

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("templateName")
    public String getTemplateName() {

        return templateName;
    }

    public void setTemplateName(String templateName) {

        this.templateName = templateName;
    }

    /**
     *
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("description")
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class GetTemplatesResponseDTO {\n");

        sb.append("  templateName: ").append(templateName).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
