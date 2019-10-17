package org.wso2.carbon.identity.configuration.mgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.LinkDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ResourceTypeDTO  {
  
  
  
  private String name = null;
  
  
  private String id = null;
  
  
  private String description = null;
  
  
  private List<LinkDTO> links = new ArrayList<LinkDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("links")
  public List<LinkDTO> getLinks() {
    return links;
  }
  public void setLinks(List<LinkDTO> links) {
    this.links = links;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceTypeDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  links: ").append(links).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
