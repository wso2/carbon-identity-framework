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

import org.wso2.carbon.identity.application.common.model.ServiceProvider;

/**
 * DTO of the Application. This class hold the ServiceProvider and Inbound Protocol details.
 */
public class ApplicationDTO {

    private final ServiceProvider serviceProvider;
    private final InboundProtocolsDTO inboundProtocolConfigurationDto;

    private ApplicationDTO(Builder builder) {

        this.serviceProvider = builder.serviceProvider;
        this.inboundProtocolConfigurationDto = builder.inboundProtocolConfigurationDto;
    }
    
    /**
     * Builder class for ApplicationDTO.
     */
    public static class Builder {

        private ServiceProvider serviceProvider;
        private InboundProtocolsDTO inboundProtocolConfigurationDto;

        public Builder serviceProvider(ServiceProvider serviceProvider) {

            this.serviceProvider = serviceProvider;
            return this;
        }

        public Builder inboundProtocolConfigurationDto(InboundProtocolsDTO inboundProtocolConfigurationDto) {

            this.inboundProtocolConfigurationDto = inboundProtocolConfigurationDto;
            return this;
        }

        public ApplicationDTO build() {

            if (serviceProvider == null) {
                throw new IllegalArgumentException("serviceProvider and inboundProtocolConfigurationDto cannot" +
                        " be null");
            }
            return new ApplicationDTO(this);
        }
    }

    public ServiceProvider getServiceProvider() {

        return serviceProvider;
    }

    public InboundProtocolsDTO getInboundProtocolConfigurationDto() {

        return inboundProtocolConfigurationDto;
    }
}
