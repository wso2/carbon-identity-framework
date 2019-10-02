package org.wso2.carbon.identity.configuration.mgt.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(description = "")
public class ResourceFileReplaceDTO {

    @NotNull
    private String id = null;

    /**
     *
     **/
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("id")
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class ResourceFileReplaceDTO {\n");

        sb.append("  id: ").append(id).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}