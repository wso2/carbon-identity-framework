package org.wso2.carbon.identity.configuration.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ResourceFileDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String file = null;

  
  /**
   * Describes the name of the file.
   **/
  @ApiModelProperty(required = true, value = "Describes the name of the file.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * Provide the location of the file
   **/
  @ApiModelProperty(value = "Provide the location of the file")
  @JsonProperty("file")
  public String getFile() {
    return file;
  }
  public void setFile(String file) {
    this.file = file;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceFileDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  file: ").append(file).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
