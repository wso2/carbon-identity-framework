package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPDetailDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class IdPListDTO  {
  
  
  
  private List<IdPDetailDTO> idPs = new ArrayList<IdPDetailDTO>();

  
  /**
   * list of IdPs
   **/
  @ApiModelProperty(value = "list of IdPs")
  @JsonProperty("idPs")
  public List<IdPDetailDTO> getIdPs() {
    return idPs;
  }
  public void setIdPs(List<IdPDetailDTO> idPs) {
    this.idPs = idPs;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdPListDTO {\n");
    
    sb.append("  idPs: ").append(idPs).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
