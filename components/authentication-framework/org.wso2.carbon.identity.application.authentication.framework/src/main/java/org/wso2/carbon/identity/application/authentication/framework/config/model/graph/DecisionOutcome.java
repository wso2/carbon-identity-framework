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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.common.model.graph.Link;

import java.io.Serializable;

public class DecisionOutcome implements Serializable {

    private static final long serialVersionUID = -6026213391911495321L;
    private AuthGraphNode destination;
    private Link config;

    public DecisionOutcome(AuthGraphNode destination, Link config) {
        this.destination = destination;
        this.config = config;
    }

    public AuthGraphNode getDestination() {
        return destination;
    }

    public String getName() {
        return config == null ? null : config.getName();
    }

    public Link getConfig() {
        return config;
    }
}
