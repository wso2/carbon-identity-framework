/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.mgt.model;

import java.util.Map;

/**
 * ActionCreationModel.
 */
public class ActionCreationModel {

    private String name;
    private String description;
    private Map<String, String> endpointProperties = null;
    
    public ActionCreationModel() {
    }
    
    public ActionCreationModel(ActionCreationModelBuilder actionCreationModelBuilder) {
        
        this.name = actionCreationModelBuilder.name;
        this.description = actionCreationModelBuilder.description;
        this.endpointProperties = actionCreationModelBuilder.endpointProperties;
    }

    public String getName() {
        
        return name;
    }
    
    public void setName(String name) {
        
        this.name = name;
    }
    
    public String getDescription() {
        
        return description;
    }
    
    public void setDescription(String description) {
        
        this.description = description;
    }
    
    public Map<String, String> getEndpointProperties() {
        
        return endpointProperties;
    }
    
    public void setEndpointProperties(Map<String, String> endpoint) {
        
        this.endpointProperties = endpoint;
    }

    /**
     * ActionCreationModelBuilder.
     */
    public static class ActionCreationModelBuilder {
        
        private String name;
        private String description;
        private Map<String, String> endpointProperties;
        
        public ActionCreationModelBuilder() {
        }
        
        public ActionCreationModelBuilder name(String name) {
            
            this.name = name;
            return this;
        }
        
        public ActionCreationModelBuilder description(String description) {
            
            this.description = description;
            return this;
        }
        
        public ActionCreationModelBuilder endpointProperties(Map<String, String> endpointProperties) {
            
            this.endpointProperties = endpointProperties;
            return this;
        }
        
        public ActionCreationModel build() {
            
            return new ActionCreationModel(this);
        }
    }
}
