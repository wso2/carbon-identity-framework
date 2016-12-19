/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.framework.config;

import org.wso2.carbon.identity.framework.handler.HandlerIdentifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HandlerChain {

    private String handlerChainId ;

    public static class Handler{
        private Map<String,String> handlerMetaData = new HashMap<>();
        private HandlerConfig handlerConfig ;
    }

    public static class HandlerConfig{
        private Map<Serializable, Serializable> configMap = new HashMap<>();

        public HandlerConfig(Map<Serializable, Serializable> configMap) {
            this.configMap = configMap;
        }

        public Map<Serializable, Serializable> getConfigMap() {
            return configMap;
        }
    }

    public static class MultiOptionHandlerConfig extends HandlerConfig{
        private Map<String, HandlerConfig> handlerConfigMap = new HashMap<>();

        public MultiOptionHandlerConfig(Map<Serializable, Serializable> configMap) {
            super(configMap);
        }

        
    }

}




/*


serviceProvider: wso2
handler: "customValidator"
handlerType: "default"
handlerConfig:
  url: "http://test"
handler: "multiOptionAuthenticationHandler"
handlerType: "multiOptionHandler"
handlers:
  handler: "requestPathAuthenticator"
    handlerType: "default"
    handlerConfig:
      secToken: "secToken"
  handler: "basicAuthenticator"
    handlerConfig:
      handlerType: "default"
      authEndpoint: "http://localhost:9090/authenticate"
      callback: "http://localhost:9090/gateway/callback"
handler: "customResponseBuilder"
handlerType: "default"
handlerConfig:
  test: "ContentType"
 */