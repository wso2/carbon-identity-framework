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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import java.io.Serializable;

/**
 * Serializable javascript function.
 * This is required since the next javascript execution may happen on a different node than current node, when user
 * submits a form in the browser.
 * The request may come to different node.
 * The current authentication context holds this function in serialized form.
 *
 * @param <T> Script Engine
 */
public interface GenericSerializableJsFunction<T> extends Serializable {

    void setSource(String name);

    String getSource();

    boolean isFunction();

    void setFunction(boolean function);

    Object apply(T scriptEngine, Object... params);

}
