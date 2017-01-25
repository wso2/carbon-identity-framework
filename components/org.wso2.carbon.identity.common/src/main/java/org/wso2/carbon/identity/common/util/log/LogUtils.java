/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.common.util.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.common.util.IdentityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Parser to parse the identity_log_tokens.properties.
 */
public class LogUtils {

    private static final Object lock = new Object();
    private static volatile LogUtils logUtils;
    private static Map<String, String> logTokenMap = new HashMap();
    private static String filePath;
    private static Logger logger = LoggerFactory.getLogger(LogUtils.class);

    private LogUtils() {

        boolean readProperties = Boolean
                .valueOf(System.getProperty(LogTokens.READ_LOG_TOKEN_PROPERTIES));

        if (readProperties) {
            buildConfiguration();
        }
    }

    /**
     * Instantiate a new instance and get it or get the available instance.
     *
     * @return IdentityLogTokenParser
     */
    public static LogUtils getInstance() {

        if (logUtils == null) {
            synchronized (lock) {
                if (logUtils == null) {
                    logUtils = new LogUtils();
                }
            }
        }
        return logUtils;
    }

    /**
     * Check whether the given token value is appropriate to log.
     *
     * @param tokenName Name of the token.
     * @return True if token is appropriate to log.
     */
    public static boolean isTokenLoggable(String tokenName) {
        return false;
    }

    private static void buildConfiguration() {

        if (filePath == null) {
            filePath = IdentityUtils.getIdentityConfigDirPath() + File.separator +
                    LogTokens.FILE_NAME;
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
            logger.error("An error occur while reading the file", e);
        } finally {
            try {
                if (fileInput != null) {
                    fileInput.close();
                }
            } catch (IOException e) {
                logger.error("Error while closing the file", e);
            }
        }
    }

    /**
     * Get the properties as a map.
     *
     * @return Map{String String}
     */
    Map<String, String> getLogTokenMap() {
        return logTokenMap;
    }

    /**
     * Log tokens.
     */
    public static class LogTokens {

        public static final String FILE_NAME = "identity_log_tokens.properties";
        public static final String READ_LOG_TOKEN_PROPERTIES = "Read_Log_Token_Properties";

        public static final String USER_CLAIMS = "UserClaims";
        public static final String USER_ID_TOKEN = "UserIdToken";
        public static final String XACML_REQUEST = "XACML_Request";
        public static final String XACML_RESPONSE = "XACML_Response";
        public static final String NTLM_TOKEN = "NTLM_Token";
        public static final String SAML_ASSERTION = "SAML_Assertion";
        public static final String SAML_REQUEST = "SAML_Request";
    }
}
