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

package org.wso2.carbon.identity.adaptive.auth.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.adaptive.auth.EmbeddedSiddhiEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class SiddhiAppDeployImpl implements SiddhiAppDeploy {

    private static final Log log = LogFactory.getLog(SiddhiAppDeployImpl.class);

    @Override
    public boolean deploySiddhiApp(String siddhiApp) {

        return EmbeddedSiddhiEngine.getInstance().deployApp(siddhiApp);
    }

    private String readInputStream(InputStream input) throws IOException {

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
}
