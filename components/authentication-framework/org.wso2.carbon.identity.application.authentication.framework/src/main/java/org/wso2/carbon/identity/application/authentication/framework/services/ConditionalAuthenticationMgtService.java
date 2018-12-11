/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Admin service to expose services related to conditional authentication.
 */
public class ConditionalAuthenticationMgtService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(SessionManagementService.class);

    /**
     * Gets the list of available functions for the conditional authentication script. This includes the core
     * functions and the custom functions that will be registered via OSGi.
     *
     * @return list of all the functions.
     */
    public String[] getAllAvailableFunctions() {

        List<String> jsFunctions = Stream.of("executeStep", "selectAcrFrom", "sendError", "Log.info", "require").collect
                (Collectors.toList());
        JsFunctionRegistry jsFunctionRegistry = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
        Map<String, Object> functionsMap = jsFunctionRegistry.getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem
                .SEQUENCE_HANDLER);
        if (functionsMap != null) {
            jsFunctions.addAll(functionsMap.keySet());
        }
        return jsFunctions.toArray(new String[0]);
    }
}
