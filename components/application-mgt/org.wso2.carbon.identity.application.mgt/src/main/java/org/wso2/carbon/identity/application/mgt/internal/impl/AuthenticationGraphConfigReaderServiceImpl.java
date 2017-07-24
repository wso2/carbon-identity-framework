/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.mgt.internal.impl;

import org.wso2.carbon.identity.application.common.model.graph.AuthenticationGraphConfig;
import org.wso2.carbon.identity.application.mgt.AuthenticationGraphConfigReaderService;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide graph XML configuration
 */
public class AuthenticationGraphConfigReaderServiceImpl implements AuthenticationGraphConfigReaderService {

    private Map <String, AuthenticationGraphConfig> authenticationGraphsConfig = new HashMap();


    public AuthenticationGraphConfigReaderServiceImpl(Map<String, AuthenticationGraphConfig> authenticationGraphsConfig) {
        this.authenticationGraphsConfig = authenticationGraphsConfig;
    }

    @Override
    public AuthenticationGraphConfig getGraph(String name) {
        return authenticationGraphsConfig.get(name);
    }

    public String[] getAllGraphs() {

        if(!authenticationGraphsConfig.isEmpty()) {
            return authenticationGraphsConfig.keySet().toArray(new String[0]);
        }
         return new String[0];
    }
}
