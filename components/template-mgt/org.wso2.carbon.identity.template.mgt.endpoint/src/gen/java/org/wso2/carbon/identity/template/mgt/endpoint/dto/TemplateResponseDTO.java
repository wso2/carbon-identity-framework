package org.wso2.carbon.identity.template.mgt.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "")
public class TemplateResponseDTO {

    private String tenantDomain = null;

    private String templateName = null;

    private String description = null;

    private String templateScript = null;

    /**
     * The domain name of the tenant which the template resides in.
     **/
    @ApiModelProperty(value = "The domain name of the tenant which the template resides in.")
    @JsonProperty("tenantDomain")
    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
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

        sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
        sb.append("  templateName: ").append(templateName).append("\n");
        sb.append("  description: ").append(description).append("\n");
        sb.append("  templateScript: ").append(templateScript).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
