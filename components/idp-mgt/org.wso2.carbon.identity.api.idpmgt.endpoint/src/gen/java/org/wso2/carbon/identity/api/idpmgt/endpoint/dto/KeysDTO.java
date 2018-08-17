package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Keys related configurations
 **/


@ApiModel(description = "Keys related configurations")
public class KeysDTO  {
  
  
  
  private List<String> idpCertificates = new ArrayList<String>();
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("idpCertificates")
  public List<String> getIdpCertificates() {
    return idpCertificates;
  }
  public void setIdpCertificates(List<String> idpCertificates) {
    this.idpCertificates = idpCertificates;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("properties")
  public List<PropertyDTO> getProperties() {
    return properties;
  }
  public void setProperties(List<PropertyDTO> properties) {
    this.properties = properties;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeysDTO {\n");
    
    sb.append("  idpCertificates: ").append(idpCertificates).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
