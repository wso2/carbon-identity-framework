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

package org.wso2.carbon.identity.application.mgt.inbound.protocol;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.inbound.dto.InboundProtocolConfigurationDTO;
import org.wso2.carbon.identity.application.mgt.inbound.dto.InboundProtocolsDTO;

/**
 * Interface for handling inbound authentication configuration.
 */
public interface ApplicationInboundAuthConfigHandler {
    
    /**
     * Checks whether the handler can handle the given inbound protocol.
     *
     * @param inboundProtocolsDTO Inbound protocol DTO.
     * @return True if the handler can handle the given protocol.
     */
    boolean canHandle(InboundProtocolsDTO inboundProtocolsDTO);
    
    /**
     * Checks whether the handler can handle the given inbound protocol name.
     *
     * @param inboundAuthConfigName Inbound auth config name.
     * @return True if the handler can handle the given protocol.
     */
    boolean canHandle(String inboundAuthConfigName);
    
    /**
     * Handles the creation of inbound authentication configuration.
     *
     * @param application         Service provider.
     * @param inboundProtocolsDTO Inbound protocol DTO.
     * @return Inbound authentication request configuration.
     * @throws IdentityApplicationManagementException If an error occurred while creating the configuration.
     */
    InboundAuthenticationRequestConfig handleConfigCreation(ServiceProvider application,
                                                            InboundProtocolsDTO inboundProtocolsDTO)
            throws IdentityApplicationManagementException;
    
    /**
     * Handles the update of inbound authentication configuration.
     *
     * @param application                     Service provider.
     * @param inboundProtocolConfigurationDTO Inbound protocol configuration DTO.
     * @return Inbound authentication request configuration.
     * @throws IdentityApplicationManagementException If an error occurred while updating the configuration.
     */
    InboundAuthenticationRequestConfig handleConfigUpdate(
            ServiceProvider application, InboundProtocolConfigurationDTO inboundProtocolConfigurationDTO)
            throws IdentityApplicationManagementException;
    
    /**
     * Handles the deletion of inbound authentication configuration.
     *
     * @param inboundAuthKey Inbound auth key.
     * @throws IdentityApplicationManagementException If an error occurred while deleting the configuration.
     */
    void handleConfigDeletion(String inboundAuthKey) throws IdentityApplicationManagementException;
    
    /**
     * Handles the retrieval of inbound authentication configuration.
     *
     * @param inboundAuthKey Inbound auth key.
     * @return Inbound protocol configuration DTO.
     * @throws IdentityApplicationManagementException If an error occurred while retrieving the configuration.
     */
    InboundProtocolConfigurationDTO handleConfigRetrieval(String inboundAuthKey)
            throws IdentityApplicationManagementException;
}
