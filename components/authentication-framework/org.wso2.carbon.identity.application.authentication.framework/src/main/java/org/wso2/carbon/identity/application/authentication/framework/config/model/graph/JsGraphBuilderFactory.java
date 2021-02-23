/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Map;

/**
 * Interface for Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of script engine and any related expensive objects.
 *
 * @param <T> scriptEngine
 */
public interface JsGraphBuilderFactory<T> {


    void init();

    JsGraphBuilder createBuilder(AuthenticationContext context, Map<Integer, StepConfig> stepConfigMapCopy);

    JsGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                     Map<Integer, StepConfig> stepConfigMap, AuthGraphNode currentNode);

    T createEngine(AuthenticationContext authenticationContext);
}
