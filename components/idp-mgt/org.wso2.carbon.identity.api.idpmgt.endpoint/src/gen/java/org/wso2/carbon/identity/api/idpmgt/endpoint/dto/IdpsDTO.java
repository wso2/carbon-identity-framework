package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;

import io.swagger.annotations.*;

@ApiModel(description = "")
public class IdpsDTO extends ArrayList<IDPListResponseDTO> {
  

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdpsDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
