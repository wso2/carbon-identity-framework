package org.wso2.carbon.identity.configuration.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ResourceFileDTO  {
  
  
  @NotNull
  private String path = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("path")
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceFileDTO {\n");
    
    sb.append("  path: ").append(path).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
