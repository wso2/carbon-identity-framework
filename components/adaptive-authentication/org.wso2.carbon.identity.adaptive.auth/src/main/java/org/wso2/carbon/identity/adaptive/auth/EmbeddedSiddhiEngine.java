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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.adaptive.auth.internal.AdaptiveDataHolder;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.concurrent.ConcurrentMap;

/**
 * Embedded siddhi engine.
 */
public class EmbeddedSiddhiEngine implements SiddhiEngine {

    private static final String ANNOTATION_APP_NAME = "name";

    private Log log = LogFactory.getLog(EmbeddedSiddhiEngine.class);

    public boolean deployApp(String siddhiAppString) {

        if (log.isDebugEnabled()) {
            log.debug("Loading Siddhi App = " + siddhiAppString);
        }
        try {
            String siddhiAppName = getSiddhiAppName(siddhiAppString);
            if (siddhiAppAlreadyExists(siddhiAppName)) {
                log.error("Siddhi App is not created. A siddhi app with name: " + siddhiAppName + " already exists.");
                return false;
            } else {
                SiddhiAppRuntime siddhiAppRuntime = AdaptiveDataHolder.getInstance().getSiddhiManager()
                        .createSiddhiAppRuntime(siddhiAppString);
                if (siddhiAppRuntime != null) {
                    siddhiAppRuntime.start();
                }
            }
        } catch (Exception e) {
            log.error("Error in loading siddhi app ", e);
            return false;
        }
        return true;
    }

    public boolean undeployApp(String siddhiAppName) {

        SiddhiAppRuntime siddhiAppRuntime = AdaptiveDataHolder.getInstance().getSiddhiManager().getSiddhiAppRuntime
                (siddhiAppName);
        if (siddhiAppRuntime != null) {
            siddhiAppRuntime.shutdown();
            return true;
        }
        return false;
    }

    private boolean siddhiAppAlreadyExists(String siddhiAppName) {

        return getSiddhiAppRuntimeMap().containsKey(siddhiAppName);
    }

    private ConcurrentMap<String, SiddhiAppRuntime> getSiddhiAppRuntimeMap() {

        return AdaptiveDataHolder.getInstance().getSiddhiManager().getSiddhiAppRuntimeMap();
    }

    private String getSiddhiAppName(String siddhiAppString) {

        SiddhiApp parsedSiddhiApp = SiddhiCompiler.parse(siddhiAppString);
        return AnnotationHelper.getAnnotationElement(ANNOTATION_APP_NAME, null, parsedSiddhiApp.getAnnotations())
                .getValue();
    }

    public SiddhiAppRuntime getAppRunTime(String siddhiAppName) {

        return AdaptiveDataHolder.getInstance().getSiddhiManager().getSiddhiAppRuntime(siddhiAppName);
    }
}
