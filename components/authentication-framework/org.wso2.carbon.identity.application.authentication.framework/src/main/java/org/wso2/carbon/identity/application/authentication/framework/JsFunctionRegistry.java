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

package org.wso2.carbon.identity.application.authentication.framework;

import java.util.Map;

/**
 * Registry to add or remove custom functions implemented in Java to javascript based execution engine.
 */
public interface JsFunctionRegistry {

    /**
     * Identifies the user-programmable subsystem.
     */
    enum Subsystem {
        SEQUENCE_HANDLER
    }

    /**
     * Register the custom function with the given name to the given subsystem.
     * @param subsystem
     * @param functionName
     * @param function
     */
    void register(Subsystem subsystem, String functionName, Object function);

    /**
     * Get the function map registered for the given sub system
     * @param subsystem Subsystem of which the function map is retrieved
     * @return function map
     */
    Map<String, Object> getSubsystemFunctionsMap(Subsystem subsystem);

    /**
     * Removes the custom functions with the given name from the given subsystem.
     * All the functions contributed will be removed, irrespective of what component registered it.
     *
     * @param subsystem
     * @param functionName
     */
    void deRegister(Subsystem subsystem, String functionName);
}
