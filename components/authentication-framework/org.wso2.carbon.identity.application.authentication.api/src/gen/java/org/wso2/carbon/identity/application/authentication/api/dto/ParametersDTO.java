/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.api.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.application.authentication.api.dto.ParameterDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ParametersDTO  {
  
  
  
  private List<ParameterDTO> parameters = new ArrayList<ParameterDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("parameters")
  public List<ParameterDTO> getParameters() {
    return parameters;
  }
  public void setParameters(List<ParameterDTO> parameters) {
    this.parameters = parameters;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ParametersDTO {\n");
    
    sb.append("  parameters: ").append(parameters).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
