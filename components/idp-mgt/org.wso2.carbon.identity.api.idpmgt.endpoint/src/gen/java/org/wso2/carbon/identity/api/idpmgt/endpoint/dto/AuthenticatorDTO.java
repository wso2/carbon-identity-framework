package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ConnectorPropertyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class AuthenticatorDTO  {
  
  
  
  private List<ConnectorPropertyDTO> properties = new ArrayList<ConnectorPropertyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("properties")
  public List<ConnectorPropertyDTO> getProperties() {
    return properties;
  }
  public void setProperties(List<ConnectorPropertyDTO> properties) {
    this.properties = properties;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthenticatorDTO {\n");
    
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
