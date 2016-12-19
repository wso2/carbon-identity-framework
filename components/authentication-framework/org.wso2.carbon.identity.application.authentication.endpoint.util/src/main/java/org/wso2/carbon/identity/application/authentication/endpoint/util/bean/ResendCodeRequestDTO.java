/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.endpoint.util.bean;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "user","properties"
})
@XmlRootElement(name = "ResendCodeRequest")
public class ResendCodeRequestDTO {
  
  
  
  private UserDTO user = null;
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();


  public UserDTO getUser() {
    return user;
  }
  public void setUser(UserDTO user) {
    this.user = user;
  }

  

  public List<PropertyDTO> getProperties() {
    return properties;
  }
  public void setProperties(List<PropertyDTO> properties) {
    this.properties = properties;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResendCodeRequestDTO {\n");
    
    sb.append("  user: ").append(user).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
