/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt.inbound.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO of the InboundProtocols. This DTO will hold all the inbound configurations details of an application.
 */
public class InboundProtocolsDTO {
    
    private final Map<String, InboundProtocolConfigurationDTO> inboundProtocolConfigurationMap = new HashMap<>();
    
    /**
     * Get the inbound protocol configuration map. This will be called from each inbound auth config handlers.
     *
     * @return Inbound protocol configuration map.
     */
    public Map<String, InboundProtocolConfigurationDTO> getInboundProtocolConfigurationMap() {
        
        return inboundProtocolConfigurationMap;
    }
    
    /**
     * Add a inbound auth config protocol to the map.
     *
     * @param inboundProtocolConfigurationDTO Inbound protocol configuration DTO.
     */
    public void addProtocolConfiguration(InboundProtocolConfigurationDTO inboundProtocolConfigurationDTO) {
        
        inboundProtocolConfigurationMap.put(inboundProtocolConfigurationDTO.fetchProtocolName(),
                inboundProtocolConfigurationDTO);
    }
    
    /**
     * Remove an inbound auth config protocol from the map.
     *
     * @param inboundProtocolConfigurationDTO Inbound protocol configuration DTO.
     * @return True if the protocol is removed successfully.
     */
    public boolean removeProtocolConfiguration(InboundProtocolConfigurationDTO inboundProtocolConfigurationDTO) {
        
        return inboundProtocolConfigurationMap.remove(inboundProtocolConfigurationDTO.fetchProtocolName()) != null;
    }
}
