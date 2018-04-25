/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.adaptive.auth;

import org.wso2.siddhi.core.SiddhiAppRuntime;

/**
 * Siddhi engine.
 */
public interface SiddhiEngine {

    /**
     * Deploy app to siddhi runtime.
     *
     * @param siddhiAppString Siddhi app
     * @return Status of deployment operation
     */
    boolean deployApp(String siddhiAppString);

    /**
     * Undeploy app from siddhi runtime.
     *
     * @param siddhiAppName App name
     * @return Status of undeployment operation
     */
    boolean undeployApp(String siddhiAppName);

    /**
     * Get app runtime.
     *
     * @param siddhiAppName App name
     * @return Siddhi app runtime
     */
    SiddhiAppRuntime getAppRunTime(String siddhiAppName);
}
