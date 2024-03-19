/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;

import java.util.Map;

/**
 * Interface for Sequence Graph Builder.
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 */
public interface JsBaseGraphBuilder {

    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    JsBaseGraphBuilder createWith(String script);

    AuthenticationGraph build();

    AuthenticationDecisionEvaluator getScriptEvaluator(BaseSerializableJsFunction fn);

    default AuthenticationDecisionEvaluator getScriptEvaluator(GenericSerializableJsFunction fn) {

        return null;
    }

    void addLongWaitProcessInternal(AsyncProcess asyncProcess,
                       Map<String, Object> parameterMap);

    void addPromptInternal(String templateId, Map<String, Object> parameters, Map<String, Object> handlers,
                                 Map<String, Object> callbacks);
}
