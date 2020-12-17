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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import java.io.Serializable;

/**
 * Serializable javascript function.
 * This is required since the next javascript execution may happen on a different node than current node, when user
 * submits a form in the browser.
 * The request may come to different node.
 * The current authentication context holds this function in serialized form.
 */
public interface SerializableJsFunction<T,U extends JsAuthenticationContext> extends Serializable {

    Object apply(T scriptEngine, U jsAuthenticationContext) ;

    void setName(String name);

    String getName();
}
