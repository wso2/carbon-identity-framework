package org.wso2.carbon.identity.configuration.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.LinkDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ResourceDTO  {
  
  
  @NotNull
  private String resourceId = null;
  
  
  private String tenantDomain = null;
  
  @NotNull
  private String resourceName = null;
  
  @NotNull
  private String resourceType = null;
  
  @NotNull
  private String lastModified = null;
  
  @NotNull
  private String created = null;
  
  
  private List<AttributeDTO> attributes = new ArrayList<AttributeDTO>();
  
  
  private List<LinkDTO> files = new ArrayList<LinkDTO>();

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("resourceId")
  public String getResourceId() {
    return resourceId;
  }
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("resourceName")
  public String getResourceName() {
    return resourceName;
  }
  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("resourceType")
  public String getResourceType() {
    return resourceType;
  }
  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("lastModified")
  public String getLastModified() {
    return lastModified;
  }
  public void setLastModified(String lastModified) {
    this.lastModified = lastModified;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("created")
  public String getCreated() {
    return created;
  }
  public void setCreated(String created) {
    this.created = created;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("attributes")
  public List<AttributeDTO> getAttributes() {
    return attributes;
  }
  public void setAttributes(List<AttributeDTO> attributes) {
    this.attributes = attributes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("files")
  public List<LinkDTO> getFiles() {
    return files;
  }
  public void setFiles(List<LinkDTO> files) {
    this.files = files;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceDTO {\n");
    
    sb.append("  resourceId: ").append(resourceId).append("\n");
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("  resourceName: ").append(resourceName).append("\n");
    sb.append("  resourceType: ").append(resourceType).append("\n");
    sb.append("  lastModified: ").append(lastModified).append("\n");
    sb.append("  created: ").append(created).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("  files: ").append(files).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
