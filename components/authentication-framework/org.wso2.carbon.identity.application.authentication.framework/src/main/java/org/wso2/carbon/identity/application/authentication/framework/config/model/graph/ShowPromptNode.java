/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Waiting node while showing a prompt to the user to get user input. Child nodes of this node is build after
 * executing the asynchronous events.
 */
public class ShowPromptNode extends DynamicDecisionNode implements AuthGraphNode {

    public static final String PRE_HANDLER = "preHandler";

    private static final long serialVersionUID = -5644595996095910601L;
    private String templateId;
    private Map<String, Serializable> data;
    private Map<String, Object> parameters;
    private Map<String, BaseSerializableJsFunction> handlerMap = new HashMap<>();

    public String getTemplateId() {

        return templateId;
    }

    public void setTemplateId(String templateId) {

        this.templateId = templateId;
    }

    public Map<String, Serializable> getData() {

        return data;
    }

    public void setData(Map<String, Serializable> data) {

        this.data = data;
    }

    public Map<String, Object> getParameters() {

        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {

        this.parameters = parameters;
    }

    public Map<String, BaseSerializableJsFunction> getHandlerMap() {

        return Collections.unmodifiableMap(handlerMap);
    }

    public void setHandlerMap(Map<String, BaseSerializableJsFunction> handlerMap) {

        this.handlerMap = handlerMap;
    }

    public void addHandler(String outcome, BaseSerializableJsFunction function) {
        handlerMap.put(outcome, function);
    }

    @Override
    public String getName() {

        return "ShowPromptNode";
    }
}
