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

import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

/**
 * Utilities for adaptive authentication.
 */
public class Utils {

    private static final String ANNOTATION_APP_NAME = "name";

    /**
     * Get siddhi app name from app.
     *
     * @param siddhiAppString Siddhi application
     * @return Name of the siddhi application
     */
    public static String getSiddhiAppName(String siddhiAppString) {

        SiddhiApp parsedSiddhiApp = SiddhiCompiler.parse(siddhiAppString);
        return AnnotationHelper
                .getAnnotationElement(ANNOTATION_APP_NAME, null, parsedSiddhiApp.getAnnotations())
                .getValue();
    }
}
