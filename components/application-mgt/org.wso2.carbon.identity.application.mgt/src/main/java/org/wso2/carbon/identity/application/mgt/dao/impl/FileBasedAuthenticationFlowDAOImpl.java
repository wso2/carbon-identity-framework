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

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.graph.AuthenticationGraphConfig;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the authentication flow from the file system.
 */
public class FileBasedAuthenticationFlowDAOImpl {
    private static Log log = LogFactory.getLog(FileBasedAuthenticationFlowDAOImpl.class);


    public static Map<String, AuthenticationGraphConfig> buildFileBasedFlows() {
        Map<String, AuthenticationGraphConfig> authenticationGraphConfigMap = new HashMap<>();
        String configDirPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "identity"
                + File.separator + "authentication-graphs";
        FileInputStream fileInputStream = null;
        File configDir = new File(configDirPath);
        OMElement documentElement;

        if (configDir.exists()) {

            for (final File fileEntry : configDir.listFiles()) {
                try {
                    if (!fileEntry.isDirectory()) {
                        fileInputStream = new FileInputStream(new File(fileEntry.getAbsolutePath()));
                        documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();
                        AuthenticationGraphConfig authGraph = AuthenticationGraphConfig.build(documentElement);
                        if (authGraph != null) {
                            authenticationGraphConfigMap.put(authGraph.getName(), authGraph);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while loading Authentication Graph from file system.", e);
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            log.error("Error occurred while closing file input stream for file " + configDirPath, e);
                        }
                    }
                }
            }
        }
        return authenticationGraphConfigMap;
    }

}
