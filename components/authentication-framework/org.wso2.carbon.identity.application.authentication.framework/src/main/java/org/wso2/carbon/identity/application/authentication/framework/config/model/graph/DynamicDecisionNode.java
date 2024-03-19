/*
 * Copyright (c) 2017, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Decision making node on graph execution model.
 * This node will construct its child nodes upon execution on asynchronous events.
 */
public class DynamicDecisionNode extends AbstractAuthGraphNode implements AuthGraphNode {

    private static final long serialVersionUID = -2151385170280964420L;
    private Map<String, GenericSerializableJsFunction> functionMap = new HashMap<>();
    private AuthGraphNode defaultEdge;

    @Override
    public String getName() {
        //TODO: Implement this.
        return null;
    }

    public Map<String, BaseSerializableJsFunction> getFunctionMap() {

        if (isFunctionMapInstanceOfBaseSerializableJsFunction()) {
            return Collections.unmodifiableMap((Map<String, BaseSerializableJsFunction>) (Map) functionMap);
        }
        return null;
    }

    public Map<String, GenericSerializableJsFunction> getGenericFunctionMap() {

        return Collections.unmodifiableMap(functionMap);
    }

    public void addFunction(String outcome, BaseSerializableJsFunction function) {
        functionMap.put(outcome, function);
    }

    public void addGenericFunction(String outcome, GenericSerializableJsFunction function) {
        functionMap.put(outcome, function);
    }

    public AuthGraphNode getDefaultEdge() {
        return defaultEdge;
    }

    public void setDefaultEdge(AuthGraphNode defaultEdge) {
        this.defaultEdge = defaultEdge;
    }

    private boolean isFunctionMapInstanceOfBaseSerializableJsFunction() {

        if (functionMap == null || functionMap.isEmpty()) {
            return true;
        }
        // Get the first element of the map and check if that is an instance of BaseSerializableJsFunction
        return functionMap.entrySet().iterator().next().getValue() instanceof BaseSerializableJsFunction;
    }
}
