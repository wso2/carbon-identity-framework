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

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

import javax.script.ScriptEngine;

/**
 * Interface for serializer class supports Multiple JS Engines.
 */
public interface JsSerializer {

    /**
     * Serialize the object using selected serializable function.
     * @param value Object to evaluate.
     * @return Serialized Object.
     */
    Object toJsSerializable(Object value);

    /**
     * De-Serialize the object using selected serializable function.
     * @param value Serialized Object.
     * @param engine Js Engine.
     * @return De-Serialize object.
     * @throws FrameworkException FrameworkException.
     */
    Object fromJsSerializable(Object value, ScriptEngine engine) throws FrameworkException;

}
