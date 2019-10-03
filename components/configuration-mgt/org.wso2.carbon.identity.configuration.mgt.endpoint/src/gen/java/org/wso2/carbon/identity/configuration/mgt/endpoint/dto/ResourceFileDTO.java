package org.wso2.carbon.identity.configuration.mgt.endpoint.dto;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.LinkDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ResourceFileDTO  {
  
  
  @NotNull
  private LinkDTO path = null;
  
  @NotNull
  private String name = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("path")
  public LinkDTO getPath() {
    return path;
  }
  public void setPath(LinkDTO path) {
    this.path = path;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceFileDTO {\n");
    
    sb.append("  path: ").append(path).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
