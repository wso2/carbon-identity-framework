package org.wso2.carbon.identity.configuration.mgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LinkDTO  {
  
  
  
  private String href = null;
  
  
  private String rel = null;

  
  /**
   * Path to the target file.
   **/
  @ApiModelProperty(value = "Path to the target file.")
  @JsonProperty("href")
  public String getHref() {
    return href;
  }
  public void setHref(String href) {
    this.href = href;
  }

  
  /**
   * Describes how the current context is related to the target resource.
   **/
  @ApiModelProperty(value = "Describes how the current context is related to the target resource.")
  @JsonProperty("rel")
  public String getRel() {
    return rel;
  }
  public void setRel(String rel) {
    this.rel = rel;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinkDTO {\n");
    
    sb.append("  href: ").append(href).append("\n");
    sb.append("  rel: ").append(rel).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
