/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.identity.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Parser to parse the identity_log_tokens.properties
 */
class IdentityLogTokenParser {

    private static IdentityLogTokenParser identityLogTokenParser;
    private static Map<String, String> logTokenMap = new HashMap<>();
    private static String filePath;
    private static final Object lock = new Object();

    private static Log log = LogFactory.getLog(IdentityConfigParser.class);

    private IdentityLogTokenParser() {

        boolean readProperties = Boolean
                .valueOf(System.getProperty(IdentityConstants.IdentityTokens.READ_LOG_TOKEN_PROPERTIES));

        if (readProperties) {
            buildConfiguration();
        }
    }

    /**
     * Instantiate a new instance and get it or get the available instance.
     * @return IdentityLogTokenParser
     */
    static IdentityLogTokenParser getInstance() {

        if (identityLogTokenParser == null) {
            synchronized (lock) {
                if (identityLogTokenParser == null) {
                    identityLogTokenParser = new IdentityLogTokenParser();
                }
            }
        }
        return identityLogTokenParser;
    }

    /**
     * Get the properties as a map.
     * @return Map{String String}
     */
    Map<String, String> getLogTokenMap() {
        return logTokenMap;
    }

    private static void buildConfiguration() {

        if (filePath == null) {
            filePath = CarbonUtils.getCarbonSecurityConfigDirPath() +
                    File.separator +
                    IdentityConstants.IdentityTokens.FILE_NAME;
        }

        FileInputStream fileInput = null;

        try {
            File file = new File(filePath);
            fileInput = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fileInput);

            for (String propertyName : properties.stringPropertyNames()) {
                logTokenMap.put(propertyName, properties.getProperty(propertyName));
            }
        } catch (IOException e) {
            log.error("An error occur while reading the file", e);
        } finally {
            try {
                if (fileInput != null) {
                    fileInput.close();
                }
            } catch (IOException e) {
                log.error("Error while closing the file", e);
            }
        }
    }
}